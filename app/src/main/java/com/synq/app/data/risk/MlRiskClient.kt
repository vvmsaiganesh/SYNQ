package com.synq.app.data.risk

import java.util.concurrent.TimeUnit
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

/**
 * Client for the FastAPI risk model (main.py). The service is optional: if it is cold,
 * unreachable, or returns an error, the heuristic [RiskEngine] takes over.
 *
 * The Render service is defined in render.yaml as `vitalis-ml`.
 */
object MlRiskClient {

    private const val BASE_URL = "https://vitalis-ml.onrender.com"

    private val json = Json { ignoreUnknownKeys = true }
    private val mediaType = "application/json; charset=utf-8".toMediaType()

    private val client = OkHttpClient.Builder()
        .connectTimeout(4, TimeUnit.SECONDS)
        .readTimeout(6, TimeUnit.SECONDS)
        .callTimeout(8, TimeUnit.SECONDS)
        .build()

    suspend fun predict(inputs: RiskInputs): Double? = withContext(Dispatchers.IO) {
        val row = buildJsonObject {
            inputs.temperatureC?.let { put("temperatureC", it) }
            inputs.humidityPct?.let { put("humidityPct", it) }
            inputs.uvIndex?.let { put("uvIndex", it) }
            inputs.airQualityAqi?.let { put("airQualityAqi", it) }
            inputs.sleepHours?.let { put("sleepHours", it) }
            inputs.restingHeartRate?.let { put("restingHeartRate", it) }
            inputs.activityMinutes?.let { put("activityMinutes", it) }
            inputs.baselineSleepHours?.let { put("baselineSleepHours", it) }
            inputs.baselineRestingHeartRate?.let { put("baselineRestingHeartRate", it) }
        }
        if (row.isEmpty()) return@withContext null

        val payload = buildJsonObject { put("row", row) }.toString()
        val request = Request.Builder()
            .url("$BASE_URL/predict")
            .post(payload.toRequestBody(mediaType))
            .build()

        runCatching {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@use null
                val text = response.body?.string() ?: return@use null
                json.parseToJsonElement(text).jsonObject["risk_score"]
                    ?.jsonPrimitive?.doubleOrNull
            }
        }.onSuccess { score ->
            Log.d("MlRiskClient", "predict ok: $score")
        }.onFailure { error ->
            Log.w("MlRiskClient", "predict failed: ${error.message}")
        }.getOrNull()
    }
}
