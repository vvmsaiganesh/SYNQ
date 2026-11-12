package com.synq.app.data.risk

import kotlin.math.roundToInt

enum class RiskLevel { LOW, MODERATE, HIGH, VERY_HIGH, UNKNOWN }

enum class DataQuality { FULL, PARTIAL, LIMITED, NONE }

enum class Factor {
    TEMPERATURE,
    HUMIDITY,
    UV,
    AIR_QUALITY,
    SLEEP,
    RHR,
    ACTIVITY,
}

data class RiskInputs(
    val temperatureC: Double? = null,
    val humidityPct: Double? = null,
    val uvIndex: Double? = null,
    val airQualityAqi: Double? = null,
    val sleepHours: Double? = null,
    val restingHeartRate: Double? = null,
    val activityMinutes: Double? = null,
    val baselineSleepHours: Double? = null,
    val baselineRestingHeartRate: Double? = null,
)

data class Contributor(
    val factor: Factor,
    val label: String,
    val score: Double,
)

data class RiskResult(
    val riskScore: Int,
    val riskLevel: RiskLevel,
    val dataQuality: DataQuality,
    val contributors: List<Contributor>,
    val recommendations: List<String>,
    val missingFactors: List<Factor>,
)

/** A forecast hour, scored with the same heuristic as the live conditions. */
data class ForecastPoint(
    val time: String,
    val temperatureC: Double?,
    val humidityPct: Double?,
    val uvIndex: Double?,
    val airQualityAqi: Double?,
)

/**
 * Heuristic heat/sun risk engine.
 *
 * Weights mirror PRODUCT.md: temperature 25%, humidity 15%, UV 15%, air quality 10%,
 * sleep 15%, resting heart rate 10%, activity 10%. When the Python ML service is
 * reachable its score is used as the final number; otherwise the weighted heuristic
 * is the fallback. Missing signals are excluded and the remaining weights renormalise.
 */
object RiskEngine {

    private val WEIGHTS = mapOf(
        Factor.TEMPERATURE to 0.25,
        Factor.HUMIDITY to 0.15,
        Factor.UV to 0.15,
        Factor.AIR_QUALITY to 0.10,
        Factor.SLEEP to 0.15,
        Factor.RHR to 0.10,
        Factor.ACTIVITY to 0.10,
    )

    val RISK_LEVEL_TEXT = mapOf(
        RiskLevel.LOW to "Low risk",
        RiskLevel.MODERATE to "Moderate risk",
        RiskLevel.HIGH to "High risk",
        RiskLevel.VERY_HIGH to "Very high risk",
        RiskLevel.UNKNOWN to "Unknown",
    )

    val MISSING_LABELS = mapOf(
        Factor.TEMPERATURE to "Temperature",
        Factor.HUMIDITY to "Humidity",
        Factor.UV to "UV index",
        Factor.AIR_QUALITY to "Air quality",
        Factor.SLEEP to "Sleep",
        Factor.RHR to "Resting heart rate",
        Factor.ACTIVITY to "Activity",
    )

    private val FACTOR_LABELS = mapOf(
        Factor.TEMPERATURE to "Heat",
        Factor.HUMIDITY to "Humidity",
        Factor.UV to "UV exposure",
        Factor.AIR_QUALITY to "Air quality",
        Factor.SLEEP to "Poor sleep",
        Factor.RHR to "Elevated heart rate",
        Factor.ACTIVITY to "Recent exertion",
    )

    val FACTOR_RECOMMENDATIONS = mapOf(
        Factor.TEMPERATURE to "Shift strenuous outdoor plans to early morning or after sunset, and take shade breaks often.",
        Factor.HUMIDITY to "High humidity slows sweat evaporation — pace yourself and drink water before you feel thirsty.",
        Factor.UV to "Cover up, wear sunscreen and sunglasses, and stay out of direct sun during the midday peak.",
        Factor.AIR_QUALITY to "Air quality is poor — keep outdoor time short and avoid heavy breathing near traffic.",
        Factor.SLEEP to "You are running low on rest, so keep today's outdoor effort light and add extra breaks.",
        Factor.RHR to "Your resting heart rate is elevated — treat today as a lower-effort day and monitor how you feel.",
        Factor.ACTIVITY to "You have already been active today; hydrate and give yourself recovery time before more exertion.",
    )

    fun calculate(inputs: RiskInputs, mlScore: Double? = null): RiskResult {
        val available = mutableListOf<Pair<Factor, Double>>()
        inputs.temperatureC?.let { available += Factor.TEMPERATURE to temperatureScore(it) }
        inputs.humidityPct?.let { available += Factor.HUMIDITY to humidityScore(it) }
        inputs.uvIndex?.let { available += Factor.UV to uvScore(it) }
        inputs.airQualityAqi?.let { available += Factor.AIR_QUALITY to airQualityScore(it) }
        inputs.sleepHours?.let { available += Factor.SLEEP to sleepScore(it, inputs.baselineSleepHours) }
        inputs.restingHeartRate?.let { available += Factor.RHR to rhrScore(it, inputs.baselineRestingHeartRate) }
        inputs.activityMinutes?.let { available += Factor.ACTIVITY to activityScore(it) }

        val missing = Factor.entries.filter { factor -> available.none { it.first == factor } }
        val dataQuality = when {
            available.size >= 6 -> DataQuality.FULL
            available.size >= 4 -> DataQuality.PARTIAL
            available.size >= 1 -> DataQuality.LIMITED
            else -> DataQuality.NONE
        }

        val heuristic = weightedScore(available)
        val finalScore = mlScore?.coerceIn(0.0, 100.0) ?: heuristic

        if (finalScore == null) {
            return RiskResult(
                riskScore = 0,
                riskLevel = RiskLevel.UNKNOWN,
                dataQuality = DataQuality.NONE,
                contributors = emptyList(),
                recommendations = listOf(
                    "Search for a city or allow location access so Synq can score today's conditions.",
                ),
                missingFactors = missing,
            )
        }

        val contributors = available
            .map { (factor, score) ->
                Contributor(
                    factor = factor,
                    label = FACTOR_LABELS.getValue(factor),
                    score = score,
                )
            }
            .sortedByDescending { it.score }

        val roundedScore = finalScore.roundToInt().coerceIn(0, 100)
        return RiskResult(
            riskScore = roundedScore,
            riskLevel = levelFor(roundedScore.toDouble()),
            dataQuality = dataQuality,
            contributors = contributors,
            recommendations = recommendationsFor(contributors),
            missingFactors = missing,
        )
    }

    fun environmentalScore(point: ForecastPoint): Double? {
        val values = mutableListOf<Pair<Factor, Double>>()
        point.temperatureC?.let { values += Factor.TEMPERATURE to temperatureScore(it) }
        point.humidityPct?.let { values += Factor.HUMIDITY to humidityScore(it) }
        point.uvIndex?.let { values += Factor.UV to uvScore(it) }
        point.airQualityAqi?.let { values += Factor.AIR_QUALITY to airQualityScore(it) }
        return weightedScore(values)
    }

    fun timeAdvice(points: List<ForecastPoint>): String? {
        if (points.isEmpty()) return null
        val scored = points.mapNotNull { point ->
            environmentalScore(point)?.let { point.time to it }
        }
        if (scored.isEmpty()) return null
        val safest = scored.minByOrNull { it.second } ?: return null
        val riskiest = scored.maxByOrNull { it.second } ?: return null
        return when {
            riskiest.second < 30.0 ->
                "Conditions stay comfortable for the next few hours."
            safest.second < 40.0 && riskiest.second - safest.second >= 10.0 ->
                "Better outdoor window around ${safest.first}, when conditions ease."
            riskiest.second >= 60.0 ->
                "Risk stays elevated through the evening — keep outdoor time short and shaded."
            else -> null
        }
    }

    fun buildSummary(result: RiskResult, top: List<Contributor>): String {
        if (result.riskLevel == RiskLevel.UNKNOWN) {
            return "Add live weather and optional health data to see today's personal outdoor risk."
        }
        if (top.isEmpty()) {
            return "Conditions look manageable, but keep water nearby and use shade where you can."
        }
        val lead = top.first()
        return when (result.riskLevel) {
            RiskLevel.VERY_HIGH ->
                "${lead.label} is pushing today's risk very high. Keep outdoor plans minimal, stay cool, and check on anyone who is vulnerable."
            RiskLevel.HIGH ->
                "${lead.label} is the main driver today. Shorten outdoor workouts, take shade breaks, and hydrate steadily."
            RiskLevel.MODERATE ->
                "${lead.label} is nudging today's risk up. Pace yourself outside and keep water within reach."
            RiskLevel.LOW ->
                "Conditions are relatively comfortable. ${lead.label} is the main factor to keep an eye on, but nothing extreme."
            RiskLevel.UNKNOWN ->
                "Add live weather and optional health data to see today's personal outdoor risk."
        }
    }

    fun hydrationTargetMl(
        temperatureC: Double?,
        humidityPct: Double?,
        activityMinutes: Double?,
    ): Int? {
        if (temperatureC == null) return null
        var target = 2000.0
        if (temperatureC > 20.0) target += (temperatureC - 20.0) * 65.0
        if ((humidityPct ?: 0.0) > 55.0) target += ((humidityPct ?: 0.0) - 55.0) * 8.0
        activityMinutes?.let { target += it.coerceAtMost(240.0) * 6.0 }
        return (target.coerceIn(1500.0, 4800.0) / 50.0).roundToInt() * 50
    }

    private fun levelFor(score: Double): RiskLevel = when {
        score < 25.0 -> RiskLevel.LOW
        score < 50.0 -> RiskLevel.MODERATE
        score < 75.0 -> RiskLevel.HIGH
        else -> RiskLevel.VERY_HIGH
    }

    private fun weightedScore(values: List<Pair<Factor, Double>>): Double? {
        if (values.isEmpty()) return null
        var weighted = 0.0
        var totalWeight = 0.0
        for ((factor, score) in values) {
            val weight = WEIGHTS.getValue(factor)
            weighted += score * weight
            totalWeight += weight
        }
        return if (totalWeight <= 0.0) null else weighted / totalWeight
    }

    private fun recommendationsFor(contributors: List<Contributor>): List<String> {
        if (contributors.isEmpty()) return emptyList()
        return contributors.take(3).map { FACTOR_RECOMMENDATIONS.getValue(it.factor) }
    }

    // Each score runs 0 (comfortable) → 100 (risky).

    private fun temperatureScore(celsius: Double): Double = when {
        celsius <= 5.0 -> 10.0
        celsius <= 15.0 -> 10.0 + (celsius - 5.0) * 1.0
        celsius <= 24.0 -> 20.0 + (celsius - 15.0) * 1.0
        celsius <= 30.0 -> 30.0 + (celsius - 24.0) * 5.0
        celsius <= 35.0 -> 60.0 + (celsius - 30.0) * 5.0
        celsius <= 40.0 -> 85.0 + (celsius - 35.0) * 3.0
        else -> 100.0
    }

    private fun humidityScore(humidity: Double): Double = when {
        humidity < 30.0 -> 20.0
        humidity <= 55.0 -> 20.0 + (humidity - 30.0) * 0.8
        humidity <= 70.0 -> 40.0 + (humidity - 55.0) * 1.5
        humidity <= 85.0 -> 62.5 + (humidity - 70.0) * 1.5
        else -> 90.0
    }

    private fun uvScore(uv: Double): Double = when {
        uv <= 2.0 -> 5.0
        uv <= 5.0 -> 20.0 + (uv - 2.0) * 8.0
        uv <= 7.0 -> 45.0 + (uv - 5.0) * 12.0
        uv <= 10.0 -> 70.0 + (uv - 7.0) * 8.0
        else -> 95.0
    }

    private fun airQualityScore(aqi: Double): Double = when {
        aqi <= 50.0 -> 10.0
        aqi <= 100.0 -> 30.0 + (aqi - 50.0) * 0.4
        aqi <= 150.0 -> 50.0 + (aqi - 100.0) * 0.5
        aqi <= 200.0 -> 75.0 + (aqi - 150.0) * 0.3
        else -> 95.0
    }

    private fun sleepScore(hours: Double, baseline: Double?): Double {
        var score = when {
            hours >= 8.0 -> 5.0
            hours >= 7.0 -> 15.0
            hours >= 6.0 -> 35.0
            hours >= 5.0 -> 55.0
            hours >= 4.0 -> 72.0
            else -> 90.0
        }
        if (baseline != null && baseline > 0.0) {
            val deficit = baseline - hours
            if (deficit > 0.0) score += (deficit * 6.0).coerceAtMost(15.0)
        }
        return score.coerceIn(0.0, 100.0)
    }

    private fun rhrScore(bpm: Double, baseline: Double?): Double {
        var score = when {
            bpm < 55.0 -> 10.0
            bpm < 65.0 -> 18.0
            bpm < 75.0 -> 30.0
            bpm < 85.0 -> 45.0
            bpm < 95.0 -> 62.0
            bpm < 105.0 -> 80.0
            else -> 92.0
        }
        if (baseline != null && baseline > 0.0) {
            val delta = bpm - baseline
            if (delta > 0.0) score += (delta * 1.2).coerceAtMost(18.0)
        }
        return score.coerceIn(0.0, 100.0)
    }

    private fun activityScore(minutes: Double): Double = when {
        minutes < 20.0 -> 15.0
        minutes < 45.0 -> 30.0
        minutes < 90.0 -> 45.0
        minutes < 150.0 -> 62.0
        minutes < 240.0 -> 80.0
        else -> 92.0
    }
}
