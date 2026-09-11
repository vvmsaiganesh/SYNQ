package com.synq.app.ui.theme

import androidx.compose.ui.graphics.Color
import com.synq.app.data.risk.RiskLevel

// ── M3 Surface hierarchy (from finalui.html tailwind config) ──
val SynqBackground = Color(0xFFFBF8F5)
val SynqSurface = Color(0xFFF6EFEA)
val SynqSurfaceHigh = Color(0xFFEFE7E0)
val SynqSurfaceHighest = Color(0xFFE6DCD4)
val SynqCard = Color(0xFFFFFFFF)

// ── Primary warm brown ──
val SynqPrimary = Color(0xFF6D381E)
val SynqOnPrimary = Color(0xFFFFFFFF)
val SynqPrimaryContainer = Color(0xFFF7BC9F)
val SynqOnPrimaryContainer = Color(0xFF3B1705)

// ── Terracotta accent ──
val Terracotta = Color(0xFFB95D34)
val TerracottaDark = Color(0xFF6B3A22)
val TerracottaLight = Color(0xFFA85A32)

// ── Sand / neutral tones ──
val SandLight = Color(0xFFEADCCF)
val SandBorder = Color(0xFFE6D5C3)
val SandCard = Color(0xFFF4ECE5)

// ── Tonal hero gradient ──
val TonalHero = Color(0xFFFFE0CF)
val TonalHeroMid = Color(0xFFFFDAC5)

// ── Olive / tertiary ──
val Olive = Color(0xFF5B6241)
val OliveContainer = Color(0xFFE0E7BD)

// ── Text hierarchy ──
val TextPrimary = Color(0xFF301F17)
val TextSecondary = Color(0xFF7E695E)
val TextMuted = Color(0xFF8C766C)

// ── Risk-specific functional colors ──
val RiskLow = Color(0xFF4B7B55)
val RiskLowBg = Color(0xFFE7F2E8)
val RiskModerate = Color(0xFFA86C20)
val RiskModerateBg = Color(0xFFFDF0DC)
val RiskHigh = Color(0xFFD6592C)
val RiskHighBg = Color(0xFFFBE7E0)
val RiskVeryHigh = Color(0xFFA32F2F)
val RiskVeryHighBg = Color(0xFFF9E4E4)
val RiskUnknownBg = Color(0xFFEBE1D8)
val RiskUnknownText = Color(0xFF5C483F)
val RiskGood = Color(0xFF37C6A0)
val RiskBad = Color(0xFFFF5C6C)
val RiskAmber = Color(0xFFF2B33D)

// ── Wearable semantic tints ──
val RoseHeart = Color(0xFFE57373)
val SkyHydration = Color(0xFF4B6E82)
val EmeraldGreen = Color(0xFF2E7D32)

// ── Hero card gradient stops ──
val TonalHeroEnd = Color(0xFFFAD0B9)
val HeroBorder = Color(0xFFF5C5AB)
val HeroDivider = Color(0xFFF0BCA2)

// ── Wearable metric card colours (from HTML) ──
val WearableCardBg = Color(0xCCFFFFFF)
val WearableCardBorder = Color(0xFFEBE1D8)
val WearableLabel = Color(0xFF665348)
val WearableValue = Color(0xFF2B1B14)
val WearableUnit = Color(0xFF7E695E)
val WearableSubtitle = Color(0xFF8C766C)

val WearableIconSteps = Color(0xFFA86445)
val WearableIconActive = Color(0xFFB95D34)
val WearableIconSleep = Color(0xFF5B6241)
val WearableIconRhr = Color(0xFFB95D34)
val WearableIconHydration = Color(0xFF4B6E82)
val WearableIconSync = Color(0xFF4B7B55)

val BadgeLightExertion = Color(0xFFFDF0DC)
val BadgeLightExertionText = Color(0xFFA86C20)
val BadgeRestful = Color(0xFFE2F0E4)
val BadgeRestfulText = Color(0xFF22572D)
val BadgeBaseline = Color(0xFFEBE1D8)
val BadgeBaselineText = Color(0xFF5C483F)
val BadgeConnected = Color(0xFFE2F0E4)
val BadgeConnectedText = Color(0xFF22572D)
val BadgeConnectedDot = Color(0xFF398047)

// ── Metric tile icon circle ──
val IconCircleBg = Color(0xFFEADCCF)
val MetricTileBg = Color(0xFFF4ECE5)
val MetricTileBorder = Color(0xFFE9DFD7)

// ── Wearable header icon circle ──
val IconCircleHeader = Color(0xFFE5D7CC)

val IconTemperature = Color(0xFF7E4C33)
val IconHumidity = Color(0xFF4B6E82)
val IconUv = Color(0xFFB07B28)
val IconAirQuality = Color(0xFF4B7B55)

// ── Section card surfaces ──
val SectionCardBg = Color(0xFFF6EFEA)
val SectionCardBorder = Color(0xFFECE0D6)

// ── Buttons ──
val ButtonPrimary = Color(0xFF6D381E)
val ButtonRefresh = Color(0xFFEADCCF)
val ButtonRefreshText = Color(0xFF4D2817)
val ButtonSearch = Color(0xFFF3E6DC)
val ButtonSearchText = Color(0xFF4A2616)

// ── Quick pills ──
val PillBg = Color(0xFFEFE3DA)
val PillText = Color(0xFF553C31)

// ── Controls panel ──
val ControlsBg = Color(0xB3EFE7E0)
val ControlsBorder = Color(0xFFE5DACF)
val ControlsDivider = Color(0xFFE3D6C9)

// ── Input fields ──
val InputBorder = Color(0xFFDECBC1)
val InputBorderFocus = SynqPrimary
val InputBg = Color.White
val InputBorderDark = Color(0xFFD5C2B6)
val ButtonSave = Color(0xFFE2D4C8)
val ButtonSaveText = Color(0xFF4D2817)

// ── Toggle ──
val ToggleOff = Color(0xFFD2BEB1)
val ToggleOn = Color(0xFF6D381E)

// ── Insights / contributor card ──
val ContributorCardBg = Color(0xB3FFFFFF)
val ContributorCardBorder = Color(0xFFE9DFD6)
val ContributorLabel = Color(0xFF3E2417)
val ContributorScore = Color(0xFFB95D34)
val ContributorExplanation = Color(0xFF534036)
val ContributorCounted = Color(0xFF8E786E)

// ── Later today outlook ──
val OutlookBarGreen = Color(0xFF4B7B55)
val OutlookBarOlive = Color(0xFF8A893D)
val OutlookTimeLabel = Color(0xFF553C31)
val OutlookScore = Color(0xFF2F1B12)
val OutlookCallout = Color(0xFFEBE1D7)
val OutlookCalloutText = Color(0xFF463025)
val OutlookCalloutIcon = Color(0xFF7A4F39)

// ── Footer ──
val FooterDivider = Color(0xFFE8DCD4)
val FooterText = Color(0xFF867064)
val FooterTextFaint = Color(0xFFA38D82)
val FooterBadge = Color(0xFF5A3826)

// ── Progress bars ──
val ProgressBarTrack = Color(0xFFEADCCF)
val ProgressBarFillSteps = Color(0xFFB95D34)
val ProgressBarFillHydration = Color(0xFF4B6E82)

// ── Status line ──
val StatusLocating = Terracotta
val StatusError = Color(0xFFBA1A1A)
val StatusIdle = TextMuted

// ── Risk ring background arc ──
val RingAccent = Color(0xFF357266)
val RingTrack = Color(0xFFF3E7D8)

// ── Wordmark dot (emerald-600 from finalui.html / appicon.html) ──
val WordmarkDot = Color(0xFF059669)

val RiskGradientStops: List<Pair<Int, Color>> = listOf(
    0 to RiskLow,
    45 to RiskModerate,
    70 to RiskHigh,
    100 to RiskVeryHigh,
)

fun colorForScore(score: Int): Color {
    val clamped = score.coerceIn(0, 100)
    for (i in 0 until RiskGradientStops.size - 1) {
        val (s0, c0) = RiskGradientStops[i]
        val (s1, c1) = RiskGradientStops[i + 1]
        if (clamped in s0..s1) {
            val t = if (s1 == s0) 0f else (clamped - s0).toFloat() / (s1 - s0).toFloat()
            return Color(
                red = c0.red + (c1.red - c0.red) * t,
                green = c0.green + (c1.green - c0.green) * t,
                blue = c0.blue + (c1.blue - c0.blue) * t,
            )
        }
    }
    return RiskGradientStops.last().second
}

data class RiskPillColors(val background: Color, val foreground: Color)

fun riskPillColors(level: RiskLevel): RiskPillColors = when (level) {
    RiskLevel.LOW -> RiskPillColors(RiskLowBg, RiskLow)
    RiskLevel.MODERATE -> RiskPillColors(RiskModerateBg, RiskModerate)
    RiskLevel.HIGH -> RiskPillColors(RiskHighBg, RiskHigh)
    RiskLevel.VERY_HIGH -> RiskPillColors(RiskVeryHighBg, RiskVeryHigh)
    RiskLevel.UNKNOWN -> RiskPillColors(RiskUnknownBg, RiskUnknownText)
}
