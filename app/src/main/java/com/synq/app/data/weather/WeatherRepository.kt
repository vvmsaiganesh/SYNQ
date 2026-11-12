package com.synq.app.data.weather

import java.net.URLEncoder
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.OkHttpClient
import okhttp3.Request

/** One point in the "later today" outlook strip. */
data class ForecastCard(
    val time: String,
    val temperatureC: Double?,
    val humidityPct: Double?,
    val uvIndex: Double?,
    val airQualityAqi: Double?,
)

/** Everything the home screen needs from a live conditions fetch. */
data class WeatherSnapshot(
    val temperatureC: Double,
    val humidityPct: Double,
    val uvIndex: Double?,
    val airQualityAqi: Double?,
    val locationLabel: String?,
    val updatedAtMillis: Long,
    val forecast: List<ForecastCard>,
)

sealed class CitySearchResult {
    data class Success(val lat: Double, val lon: Double, val label: String) : CitySearchResult()
    data class NotFound(val query: String) : CitySearchResult()
    data class Error(val message: String) : CitySearchResult()
}

sealed class ConditionsResult {
    data class Success(val snapshot: WeatherSnapshot) : ConditionsResult()
    data class Error(val message: String) : ConditionsResult()
}

/**
 * Live weather, air quality and geocoding, all from key-free public APIs:
 *  - Open-Meteo forecast + geocoding + air quality
 *  - BigDataCloud reverse geocoding
 */
class WeatherRepository {

    private val json = Json { ignoreUnknownKeys = true }

    private val client = OkHttpClient.Builder()
        .connectTimeout(8, TimeUnit.SECONDS)
        .readTimeout(8, TimeUnit.SECONDS)
        .callTimeout(12, TimeUnit.SECONDS)
        .build()

    suspend fun searchCity(query: String): CitySearchResult {
        val trimmed = query.trim()
        if (trimmed.isBlank()) return CitySearchResult.NotFound(trimmed)
        return try {
            val encoded = URLEncoder.encode(trimmed, "UTF-8")
            val url = "https://geocoding-api.open-meteo.com/v1/search" +
                "?name=$encoded&count=1&language=en&format=json"
            val body = getJson(url) ?: return CitySearchResult.Error(
                "Couldn't reach the weather service. Check your connection and try again."
            )
            val results = body["results"]?.jsonArray
            val first = results?.firstOrNull()?.jsonObject
                ?: return CitySearchResult.NotFound(trimmed)
            val lat = first["latitude"]?.jsonPrimitive?.doubleOrNull
            val lon = first["longitude"]?.jsonPrimitive?.doubleOrNull
            if (lat == null || lon == null) return CitySearchResult.NotFound(trimmed)
            val name = first["name"]?.jsonPrimitive?.contentOrNullSafe() ?: trimmed
            val region = first["admin1"]?.jsonPrimitive?.contentOrNullSafe()
            val country = first["country"]?.jsonPrimitive?.contentOrNullSafe()
            val label = listOfNotNull(
                name,
                region?.takeIf { it.isNotBlank() && !it.equals(name, true) }
                    ?: country?.takeIf { it.isNotBlank() },
            ).joinToString(", ")
            CitySearchResult.Success(lat, lon, label)
        } catch (e: Exception) {
            CitySearchResult.Error("Couldn't search for that city. Check your connection and try again.")
        }
    }

    suspend fun fetchConditions(lat: Double, lon: Double, labelOverride: String?): ConditionsResult {
        return try {
            val forecastUrl = "https://api.open-meteo.com/v1/forecast" +
                "?latitude=$lat&longitude=$lon" +
                "&current=temperature_2m,relative_humidity_2m,is_day,weather_code" +
                "&hourly=temperature_2m,relative_humidity_2m,uv_index" +
                "&forecast_days=2&timezone=auto"
            val forecastBody = getJson(forecastUrl)
                ?: return ConditionsResult.Error(
                    "Couldn't reach the weather service. Check your connection and try again."
                )

            val current = forecastBody["current"]?.jsonObject
            val temperature = current?.get("temperature_2m")?.jsonPrimitive?.doubleOrNull
            val humidity = current?.get("relative_humidity_2m")?.jsonPrimitive?.doubleOrNull
            if (temperature == null || humidity == null) {
                return ConditionsResult.Error("Live weather is unavailable for this location right now.")
            }

            val hourly = forecastBody["hourly"]?.jsonObject
            val times = hourly?.get("time")?.jsonArray
            val temps = hourly?.get("temperature_2m")?.jsonArray
            val humidities = hourly?.get("relative_humidity_2m")?.jsonArray
            val uvs = hourly?.get("uv_index")?.jsonArray

            // Align the hourly window to the location's own clock, not the phone's.
            val locationNow = current["time"]?.jsonPrimitive?.contentOrNullSafe()
                ?.let {
                    runCatching {
                        LocalDateTime.parse(
                            it,
                            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm", Locale.US),
                        )
                    }.getOrNull()
                }
                ?: LocalDateTime.now()
            val nowIndex = closestHourIndex(times, locationNow)
            val uvNow = uvs?.getOrNullSafe(nowIndex)?.jsonPrimitive?.doubleOrNull

            val airQuality = try {
                val aqiUrl = "https://air-quality-api.open-meteo.com/v1/air-quality" +
                    "?latitude=$lat&longitude=$lon&current=us_aqi&timezone=auto"
                getJson(aqiUrl)?.get("current")?.jsonObject
                    ?.get("us_aqi")?.jsonPrimitive?.doubleOrNull
            } catch (e: Exception) {
                null
            }

            val label = labelOverride?.takeIf { it.isNotBlank() } ?: "Current location"
            val forecast = buildForecast(times, temps, humidities, uvs, airQuality, nowIndex)

            ConditionsResult.Success(
                WeatherSnapshot(
                    temperatureC = temperature,
                    humidityPct = humidity,
                    uvIndex = uvNow,
                    airQualityAqi = airQuality,
                    locationLabel = label,
                    updatedAtMillis = System.currentTimeMillis(),
                    forecast = forecast,
                )
            )
        } catch (e: Exception) {
            ConditionsResult.Error("Couldn't load live conditions. Check your connection and try again.")
        }
    }

    suspend fun reverseGeocodeLabel(lat: Double, lon: Double): String? {
        return try {
            val url = "https://api.bigdatacloud.net/data/reverse-geocode-client" +
                "?latitude=$lat&longitude=$lon&localityLanguage=en"
            val body = getJson(url) ?: return null
            val city = body["city"]?.jsonPrimitive?.contentOrNullSafe()
            val locality = body["locality"]?.jsonPrimitive?.contentOrNullSafe()
            val region = body["principalSubdivision"]?.jsonPrimitive?.contentOrNullSafe()
            val name = city?.takeIf { it.isNotBlank() }
                ?: locality?.takeIf { it.isNotBlank() }
                ?: return null
            if (!region.isNullOrBlank() && !region.equals(name, true)) "$name, $region" else name
        } catch (e: Exception) {
            null
        }
    }

    private fun buildForecast(
        times: JsonArray?,
        temps: JsonArray?,
        humidities: JsonArray?,
        uvs: JsonArray?,
        airQuality: Double?,
        nowIndex: Int,
    ): List<ForecastCard> {
        if (times == null || temps == null || humidities == null) return emptyList()
        val parser = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm", Locale.US)
        val labelFormat = DateTimeFormatter.ofPattern("h a", Locale.US)
        val cards = mutableListOf<ForecastCard>()
        val offsets = listOf(2, 4, 6)
        for (offset in offsets) {
            val index = nowIndex + offset
            if (index !in 0 until times.size) continue
            val rawTime = runCatching {
                LocalDateTime.parse(times[index].jsonPrimitive.content, parser)
            }.getOrNull() ?: continue
            val temp = temps.getOrNullSafe(index)?.jsonPrimitive?.doubleOrNull
            val humidity = humidities.getOrNullSafe(index)?.jsonPrimitive?.doubleOrNull
            val uv = uvs?.getOrNullSafe(index)?.jsonPrimitive?.doubleOrNull
            cards += ForecastCard(
                time = rawTime.format(labelFormat),
                temperatureC = temp,
                humidityPct = humidity,
                uvIndex = uv,
                airQualityAqi = airQuality,
            )
        }
        return cards
    }

    private fun closestHourIndex(times: JsonArray?, now: LocalDateTime): Int {
        if (times == null || times.isEmpty()) return 0
        val parser = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm", Locale.US)
        var bestIndex = 0
        var bestDelta = Long.MAX_VALUE
        times.forEachIndexed { index, element ->
            val parsed = runCatching {
                LocalDateTime.parse(element.jsonPrimitive.content, parser)
            }.getOrNull() ?: return@forEachIndexed
            val delta = kotlin.math.abs(java.time.Duration.between(now, parsed).toMinutes())
            if (delta < bestDelta) {
                bestDelta = delta
                bestIndex = index
            }
        }
        return bestIndex
    }

    private suspend fun getJson(url: String): JsonObject? = withContext(Dispatchers.IO) {
        val request = Request.Builder().url(url).get().build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) return@withContext null
            val text = response.body.string()
            json.parseToJsonElement(text).jsonObject
        }
    }
}

private fun kotlinx.serialization.json.JsonPrimitive.contentOrNullSafe(): String? =
    if (isString) content else content

private fun JsonArray.getOrNullSafe(index: Int): kotlinx.serialization.json.JsonElement? =
    if (index in 0 until size) this[index] else null
