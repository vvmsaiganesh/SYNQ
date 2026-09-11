package com.synq.app.ui

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.layout.FirstBaseline
import androidx.compose.ui.layout.LastBaseline
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.MeasurePolicy
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.synq.app.data.risk.DataQuality
import com.synq.app.data.risk.Factor
import com.synq.app.data.risk.RiskLevel
import com.synq.app.data.risk.RiskEngine
import com.synq.app.data.risk.ForecastPoint
import com.synq.app.data.weather.ForecastCard
import com.synq.app.ui.components.ContributorRow
import com.synq.app.ui.components.MetricTile
import com.synq.app.ui.components.RecommendationRow
import com.synq.app.ui.components.RefreshButton
import com.synq.app.ui.components.RiskRing
import com.synq.app.ui.components.WearableMetricCard
import com.synq.app.ui.theme.BadgeBaseline
import com.synq.app.ui.theme.BadgeBaselineText
import com.synq.app.ui.theme.BadgeConnected
import com.synq.app.ui.theme.BadgeConnectedDot
import com.synq.app.ui.theme.BadgeConnectedText
import com.synq.app.ui.theme.BadgeLightExertion
import com.synq.app.ui.theme.BadgeLightExertionText
import com.synq.app.ui.theme.BadgeRestful
import com.synq.app.ui.theme.BadgeRestfulText
import com.synq.app.ui.theme.ButtonPrimary
import com.synq.app.ui.theme.ButtonSave
import com.synq.app.ui.theme.ButtonSaveText
import com.synq.app.ui.theme.ControlsBg
import com.synq.app.ui.theme.ControlsBorder
import com.synq.app.ui.theme.ControlsDivider
import com.synq.app.ui.theme.FooterBadge
import com.synq.app.ui.theme.FooterDivider
import com.synq.app.ui.theme.FooterText
import com.synq.app.ui.theme.FooterTextFaint
import com.synq.app.ui.theme.HeroBorder
import com.synq.app.ui.theme.HeroDivider
import com.synq.app.ui.theme.IconAirQuality
import com.synq.app.ui.theme.IconCircleHeader
import com.synq.app.ui.theme.IconHumidity
import com.synq.app.ui.theme.IconTemperature
import com.synq.app.ui.theme.IconUv
import com.synq.app.ui.theme.InputBorder
import com.synq.app.ui.theme.InputBorderDark
import com.synq.app.ui.theme.MetricTileBg
import com.synq.app.ui.theme.Olive
import com.synq.app.ui.theme.OutlookBarGreen
import com.synq.app.ui.theme.OutlookBarOlive
import com.synq.app.ui.theme.OutlookCallout
import com.synq.app.ui.theme.OutlookCalloutIcon
import com.synq.app.ui.theme.OutlookCalloutText
import com.synq.app.ui.theme.OutlookScore
import com.synq.app.ui.theme.OutlookTimeLabel
import com.synq.app.ui.theme.PillBg
import com.synq.app.ui.theme.PillText
import com.synq.app.ui.theme.ProgressBarFillHydration
import com.synq.app.ui.theme.ProgressBarFillSteps
import com.synq.app.ui.theme.SectionCardBg
import com.synq.app.ui.theme.SectionCardBorder
import com.synq.app.ui.theme.SkyHydration
import com.synq.app.ui.theme.StatusError
import com.synq.app.ui.theme.StatusIdle
import com.synq.app.ui.theme.StatusLocating
import com.synq.app.ui.theme.TextMuted
import com.synq.app.ui.theme.TextPrimary
import com.synq.app.ui.theme.TextSecondary
import com.synq.app.ui.theme.Terracotta
import com.synq.app.ui.theme.TonalHero
import com.synq.app.ui.theme.TonalHeroEnd
import com.synq.app.ui.theme.TonalHeroMid
import com.synq.app.ui.theme.ToggleOff
import com.synq.app.ui.theme.ToggleOn
import com.synq.app.ui.theme.SynqBackground
import com.synq.app.ui.theme.WordmarkDot
import com.synq.app.ui.theme.SynqOnPrimary
import com.synq.app.ui.theme.SynqPrimary
import com.synq.app.ui.theme.SynqSurface
import com.synq.app.ui.theme.WearableCardBorder
import com.synq.app.ui.theme.WearableIconActive
import com.synq.app.ui.theme.WearableIconHydration
import com.synq.app.ui.theme.WearableIconRhr
import com.synq.app.ui.theme.WearableIconSleep
import com.synq.app.ui.theme.WearableIconSteps
import com.synq.app.ui.theme.WearableIconSync
import com.synq.app.ui.theme.WearableSubtitle
import com.synq.app.ui.theme.colorForScore
import com.synq.app.ui.theme.riskPillColors
import com.synq.app.viewmodel.EnvStatus
import com.synq.app.viewmodel.HealthConnectUiStatus
import com.synq.app.viewmodel.SynqUiState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

private val DemoCities = listOf("Hyderabad", "Delhi", "Chennai", "Mumbai")

@Composable
fun HomeScreen(
    state: SynqUiState,
    clockTick: Long,
    onLocate: () -> Unit,
    onRefresh: () -> Unit,
    onCityQueryChange: (String) -> Unit,
    onSearchCity: (String) -> Unit,
    onConnectHealthConnect: () -> Unit,
    onInstallHealthConnect: () -> Unit,
    onUseDemoData: () -> Unit,
    onSyncHealthConnect: () -> Unit,
    onDisconnectHealthConnect: () -> Unit,
    onLogHydration: (Int) -> Unit,
    onSaveManualSleep: (Double) -> Unit,
    onToggleBaseline: (Boolean) -> Unit,
    onBaselineSleepChange: (Double) -> Unit,
    onBaselineRhrChange: (Double) -> Unit,
) {
    val risk = remember(state) { computeRiskSnapshot(state) }
    val color = colorForScore(risk.riskScore)

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .background(SynqBackground)
            .safeDrawingPadding(),
        color = SynqBackground,
    ) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 430.dp),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 48.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            item { Header() }
            item { HeroRiskCard(state, risk, color) }
            item { LiveEnvironmentalMetrics(state) }
            item {
                SectionCard {
                    Column(Modifier.padding(20.dp)) {
                        ConditionsHeader(state = state, onRefresh = onRefresh)
                        Spacer(Modifier.height(14.dp))
                        ConditionsContent(
                            state = state,
                            onLocate = onLocate,
                            onCityQueryChange = onCityQueryChange,
                            onSearchCity = onSearchCity,
                        )
                    }
                }
            }
            item {
                SectionCard {
                    Column(Modifier.padding(20.dp)) {
                        WearableHeader(status = state.healthStatus)
                        Spacer(Modifier.height(16.dp))
                        WearableContent(
                            state = state,
                            onConnectHealthConnect = onConnectHealthConnect,
                            onInstallHealthConnect = onInstallHealthConnect,
                            onUseDemoData = onUseDemoData,
                            onSyncHealthConnect = onSyncHealthConnect,
                            onDisconnectHealthConnect = onDisconnectHealthConnect,
                            onLogHydration = onLogHydration,
                            onSaveManualSleep = onSaveManualSleep,
                            onToggleBaseline = onToggleBaseline,
                            onBaselineSleepChange = onBaselineSleepChange,
                            onBaselineRhrChange = onBaselineRhrChange,
                            clockTick = clockTick,
                        )
                    }
                }
            }
            item {
                SectionCard {
                    Column(Modifier.padding(20.dp)) {
                        CardHeading(
                            "What this means",
                            "Built from the actual factors driving your index — not guesswork.",
                        )
                        Spacer(Modifier.height(14.dp))
                        GuidanceContent(state, risk, color)
                    }
                }
            }
            item { Footer() }
        }
        }
    }
}

@Composable
private fun SectionCard(content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = SectionCardBg),
        border = BorderStroke(1.dp, SectionCardBorder),
    ) {
        content()
    }
}

@Composable
private fun CardHeading(title: String, subtitle: String) {
    Column {
        Text(
            title,
            style = MaterialTheme.typography.titleMedium,
            color = TextPrimary,
        )
        Spacer(Modifier.height(2.dp))
        Text(
            subtitle,
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
            color = TextSecondary,
        )
    }
}

// ──────────── Wearable header ────────────

@Composable
private fun WearableHeader(status: HealthConnectUiStatus) {
    val subtitle = when (status) {
        is HealthConnectUiStatus.Connected -> {
            if (status.isDemo) "Demo data (simulated) • On-device privacy store"
            else "Synced from Health Connect • On-device privacy store"
        }
        else -> "Health Connect on Android & iOS"
    }
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top,
    ) {
        Row(
            Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(IconCircleHeader),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Outlined.Schedule,
                    contentDescription = null,
                    tint = SynqPrimary,
                    modifier = Modifier.size(16.dp),
                )
            }
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    "Wearable & activity — Health Connect",
                    style = MaterialTheme.typography.bodyMedium.copy(
fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 17.5.sp,
                    ),
                    color = TextPrimary,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                    color = Color(0xFF8C583F),
                )
            }
        }
        if (status is HealthConnectUiStatus.Connected) {
            Spacer(Modifier.width(8.dp))
            Surface(
                shape = RoundedCornerShape(50.dp),
                color = BadgeConnected,
            ) {
                Row(
                    Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(BadgeConnectedDot),
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        "Connected",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.25.sp,
                        ),
                        color = BadgeConnectedText,
                    )
                }
            }
        }
    }
}

// ──────────── Header ────────────

@Composable
private fun Header() {
    Box(
        Modifier
            .fillMaxWidth()
            .padding(start = 4.dp, end = 4.dp, top = 12.dp, bottom = 4.dp),
        contentAlignment = Alignment.Center,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "SYNQ",
                modifier = Modifier.alignByBaseline(),
                style = MaterialTheme.typography.displaySmall.copy(
                    fontWeight = FontWeight.Black,
                    letterSpacing = (-0.9).sp,
                ),
                color = TextPrimary,
            )
            Spacer(Modifier.width(3.dp))
            WordmarkDot(
                color = WordmarkDot,
                size = 10.dp,
                modifier = Modifier.alignByBaseline(),
            )
        }
    }
}

@Composable
private fun WordmarkDot(
    color: Color,
    size: Dp,
    modifier: Modifier = Modifier,
    baselineSink: Dp = 0.4.dp,
) {
    Layout(
        modifier = modifier,
        content = {
            Box(
                Modifier
                    .size(size)
                    .clip(CircleShape)
                    .background(color),
            )
        },
        measurePolicy = MeasurePolicy { measurables, constraints ->
            val sizePx = size.roundToPx()
            val sinkPx = baselineSink.roundToPx()
            val baseline = sizePx - sinkPx
            val dot = measurables[0].measure(
                constraints.copy(
                    minWidth = sizePx,
                    maxWidth = sizePx,
                    minHeight = sizePx,
                    maxHeight = sizePx,
                )
            )
            layout(
                width = sizePx,
                height = sizePx,
                alignmentLines = mapOf(FirstBaseline to baseline, LastBaseline to baseline),
            ) {
                dot.place(0, 0)
            }
        },
    )
}

// ──────────── Hero Risk Card ────────────

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun HeroRiskCard(state: SynqUiState, risk: RiskSnapshot, color: Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(36.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = BorderStroke(1.dp, HeroBorder.copy(alpha = 0.6f)),
    ) {
        Box(
            Modifier.background(
                Brush.linearGradient(
                    colors = listOf(TonalHero, TonalHeroMid, TonalHeroEnd),
                )
            ),
        ) {
            Column(Modifier.padding(24.dp)) {
                // ── Top row: gauge + risk badge ──
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    RiskRing(
                        score = risk.riskScore,
                        unknown = risk.riskLevel == RiskLevel.UNKNOWN,
                        color = color,
                        size = 112.dp,
                    )
                    val pill = riskPillColors(risk.riskLevel)
                    Column(
                        Modifier.weight(1f),
                        horizontalAlignment = Alignment.Start,
                    ) {
                        Surface(
                            shape = RoundedCornerShape(50.dp),
                            color = pill.background,
                        ) {
                            Row(
                                Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Box(
                                    Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(color),
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    RiskEngine.RISK_LEVEL_TEXT.getValue(risk.riskLevel).uppercase(),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 0.5.sp,
                                    ),
                                    color = pill.foreground,
                                )
                            }
                        }
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "Based on current live outdoor conditions & weather metrics.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                lineHeight = 20.sp,
                            ),
                            color = Color(0xFF7B5341),
                        )
                    }
                }
                // ── Editorial narrative headline ──
                Spacer(Modifier.height(20.dp))
                Text(
                    risk.summary,
                    style = MaterialTheme.typography.titleLarge.copy(fontSize = 20.sp),
                    color = Color(0xFF3B1705),
                    fontWeight = FontWeight.Bold,
                    lineHeight = 26.sp,
                )
                // ── Contextual hydration advisory ──
                if (risk.hydrationTargetMl != null) {
                    Spacer(Modifier.height(14.dp))
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFFFFF7F2),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.6f)),
                    ) {
                        Row(
                            Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                imageVector = Icons.Filled.WaterDrop,
                                contentDescription = null,
                                tint = Terracotta,
                                modifier = Modifier.size(20.dp),
                            )
                            Spacer(Modifier.width(10.dp))
                            Text(
                                buildAnnotatedString {
                                    append("Suggested water intake today: ")
                                    withStyle(
                                        SpanStyle(
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF31170A),
                                        )
                                    ) {
                                        append("~${"%.1f".format(risk.hydrationTargetMl / 1000f)} L")
                                    }
                                    append(", given the current ambient heat and daytime load.")
                                },
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Medium,
                                    lineHeight = 18.sp,
                                ),
                                color = Color(0xFF4D2817),
                            )
                        }
                    }
                }
                // ── Contextual sleep advisory ──
                val sleepHours = if (state.isHealthConnected) state.effectiveSleepHours else null
                if (sleepHours != null && sleepHours < 7) {
                    Spacer(Modifier.height(10.dp))
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFFFFF7F2),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.6f)),
                    ) {
                        Row(
                            Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                imageVector = Icons.Filled.DarkMode,
                                contentDescription = null,
                                tint = Terracotta,
                                modifier = Modifier.size(20.dp),
                            )
                            Spacer(Modifier.width(10.dp))
                            Text(
                                buildAnnotatedString {
                                    append("You slept ~${"%.1f".format(sleepHours)}h. ")
                                    withStyle(
                                        SpanStyle(
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF31170A),
                                        )
                                    ) {
                                        append("Take it easier")
                                    }
                                    append(
                                        if (sleepHours < 4) {
                                            " today — your body can't cool itself as well on this little rest. Keep outdoors light, take frequent breaks, and skip intense activity."
                                        } else {
                                            " than usual outdoors today — add breaks and avoid intense activity in the heat."
                                        }
                                    )
                                    withStyle(
                                        SpanStyle(
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFB95D34),
                                        )
                                    ) {
                                        append(
                                            if (sleepHours < 5) " This counts heavily toward your risk score."
                                            else "."
                                        )
                                    }
                                },
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Medium,
                                    lineHeight = 18.sp,
                                ),
                                color = Color(0xFF4D2817),
                            )
                        }
                    }
                }
                // ── Contextual UV advisory ──
                if (state.envStatus == EnvStatus.READY && state.uvIndex != null) {
                    val uv = state.uvIndex
                    val bestTime = state.forecast
                        .filter { it.uvIndex != null && it.uvIndex <= 3 }
                        .firstOrNull()?.time
                    Spacer(Modifier.height(10.dp))
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFFFFF7F2),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.6f)),
                    ) {
                        Row(
                            Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                imageVector = Icons.Filled.WbSunny,
                                contentDescription = null,
                                tint = Terracotta,
                                modifier = Modifier.size(20.dp),
                            )
                            Spacer(Modifier.width(10.dp))
                            Text(
                                buildAnnotatedString {
                                    withStyle(
                                        SpanStyle(
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF31170A),
                                        )
                                    ) {
                                        append("UV is ${"%.1f".format(uv)} ")
                                    }
                                    append(
                                        when {
                                            uv >= 8 -> "(very high). "
                                            uv >= 6 -> "(high). "
                                            uv >= 3 -> "(moderate). "
                                            else -> "(low). "
                                        }
                                    )
                                    when {
                                        uv >= 6 && bestTime != null -> append(
                                            "Avoid direct sun now — go outside after $bestTime, when UV drops to a safer level."
                                        )
                                        uv >= 6 -> append(
                                            "Avoid direct sun between 10 AM and 4 PM — go out early morning or late afternoon."
                                        )
                                        uv >= 3 -> append(
                                            "Limit long direct sun exposure during midday and prefer shaded spots."
                                        )
                                        else -> append("Comfortable for outdoor sun exposure right now.")
                                    }
                                },
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Medium,
                                    lineHeight = 18.sp,
                                ),
                                color = Color(0xFF4D2817),
                            )
                        }
                    }
                }
                // ── Status assist chips ──
                if (risk.missingFactors.isNotEmpty()) {
                    Spacer(Modifier.height(16.dp))
                    HorizontalDivider(color = HeroDivider.copy(alpha = 0.5f))
                    Spacer(Modifier.height(8.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.Start),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        risk.missingFactors.take(3).forEach { f ->
                            MissingChip(
                                "No ${RiskEngine.MISSING_LABELS.getValue(f).lowercase()} data",
                                f,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MissingChip(text: String, factor: Factor) {
    Surface(
        shape = RoundedCornerShape(50.dp),
        color = Color.White.copy(alpha = 0.6f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.8f)),
    ) {
        Row(
            Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = factor.missingIcon(),
                contentDescription = null,
                tint = factor.missingTint(),
                modifier = Modifier.size(12.dp),
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text,
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF6B3A22),
            )
        }
    }
}

private fun Factor.missingIcon(): ImageVector = when (this) {
    Factor.TEMPERATURE -> Icons.Filled.Thermostat
    Factor.HUMIDITY -> Icons.Filled.WaterDrop
    Factor.UV -> Icons.Filled.WbSunny
    Factor.AIR_QUALITY -> Icons.Filled.Air
    Factor.SLEEP -> Icons.Outlined.DarkMode
    Factor.RHR -> Icons.Outlined.FavoriteBorder
    Factor.ACTIVITY -> Icons.Outlined.Bolt
}

private fun Factor.missingTint(): Color = when (this) {
    Factor.TEMPERATURE -> IconTemperature
    Factor.HUMIDITY -> IconHumidity
    Factor.UV -> IconUv
    Factor.AIR_QUALITY -> IconAirQuality
    Factor.SLEEP -> WearableIconSleep
    Factor.RHR -> WearableIconRhr
    Factor.ACTIVITY -> WearableIconSteps
}

// ──────────── Live Environmental Metrics ────────────

@Composable
private fun LiveEnvironmentalMetrics(state: SynqUiState) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.LocationOn,
                    contentDescription = null,
                    tint = Terracotta,
                    modifier = Modifier.size(16.dp),
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    state.locationLabel ?: "No location",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.Bold,
                    ),
                    color = Color(0xFF3B251B),
                )
            }
            if (state.updatedAtMillis != null) {
                Text(
                    "Updated ${formatClock(state.updatedAtMillis)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary,
                )
            }
        }
        val loading = state.envStatus == EnvStatus.LOADING || state.envStatus == EnvStatus.LOCATING
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(Modifier.weight(1f)) {
                MetricTile(
                    label = "Temperature",
                    value = state.temperatureC?.let { "${it.roundToInt()}\u00B0" } ?: "\u2014",
                    unit = "C",
                    icon = Icons.Filled.WbSunny,
                    iconTint = IconTemperature,
                    subtitle = state.temperatureC?.let { "Feels like ${it.roundToInt() + 2}\u00B0" },
                    loading = loading,
                )
            }
            Box(Modifier.weight(1f)) {
                val isHighHumidity = (state.humidityPct ?: 0.0) > 65
                MetricTile(
                    label = "Humidity",
                    value = state.humidityPct?.let { "${it.roundToInt()}%" } ?: "\u2014",
                    icon = Icons.Filled.WaterDrop,
                    iconTint = IconHumidity,
                    subtitle = state.humidityPct?.let {
                        if (isHighHumidity) "Elevated dampness" else "Comfortable"
                    },
                    loading = loading,
                )
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(Modifier.weight(1f)) {
                MetricTile(
                    label = "UV Index",
                    value = state.uvIndex?.let { "%.1f".format(it) } ?: "\u2014",
                    icon = Icons.Filled.WbSunny,
                    iconTint = IconUv,
                    subtitle = state.uvIndex?.let {
                        if (it < 3) "Low protection needed" else if (it < 6) "Moderate" else "High \u2014 cover up"
                    },
                    loading = loading,
                )
            }
            Box(Modifier.weight(1f)) {
                MetricTile(
                    label = "Air Quality",
                    value = state.aqi?.let { "${it.roundToInt()}" } ?: "\u2014",
                    unit = "AQI",
                    icon = Icons.Filled.Air,
                    iconTint = IconAirQuality,
                    subtitle = state.aqi?.let {
                        when {
                            it < 50 -> "Good"
                            it < 100 -> "Moderate"
                            else -> "Poor"
                        }
                    },
                    loading = loading,
                )
            }
        }
    }
}

@Composable
private fun ConditionsHeader(state: SynqUiState, onRefresh: () -> Unit) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column {
            Text(
                "Conditions near you",
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                "Auto-synced with weather station",
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                color = TextSecondary,
            )
        }
        Spacer(Modifier.width(8.dp))
        RefreshButton(
            onClick = onRefresh,
            enabled = state.envStatus == EnvStatus.READY,
        )
    }
}

// ──────────── Conditions Card ────────────

@Composable
private fun ConditionsContent(
    state: SynqUiState,
    onLocate: () -> Unit,
    onCityQueryChange: (String) -> Unit,
    onSearchCity: (String) -> Unit,
) {
    Button(
        onClick = onLocate,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(50.dp),
        colors = ButtonDefaults.buttonColors(containerColor = ButtonPrimary),
        contentPadding = PaddingValues(vertical = 12.dp, horizontal = 16.dp),
    ) {
        Icon(Icons.Filled.MyLocation, contentDescription = null, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(8.dp))
        Text(
            "Use my current location",
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = FontWeight.SemiBold,
            ),
        )
    }
    Spacer(Modifier.height(10.dp))

    var cityText by remember { mutableStateOf(state.citySearchQuery) }
    var searchFocused by remember { mutableStateOf(false) }
    val searchActive = searchFocused || cityText.isNotBlank()
    Surface(
        modifier = Modifier.onFocusChanged { searchFocused = it.isFocused },
        shape = RoundedCornerShape(if (searchActive) 18.dp else 16.dp),
        color = Color.White,
        border = BorderStroke(
            width = if (searchActive) 2.dp else 1.dp,
            color = if (searchActive) SynqPrimary.copy(alpha = 0.35f) else InputBorder,
        ),
    ) {
        Row(
            Modifier.padding(start = 14.dp, end = 10.dp, top = 6.dp, bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            androidx.compose.foundation.text.BasicTextField(
                value = cityText,
                onValueChange = { cityText = it; onCityQueryChange(it) },
                modifier = Modifier.weight(1f),
                singleLine = true,
                textStyle = MaterialTheme.typography.bodySmall.copy(
                    color = TextPrimary,
                ),
                cursorBrush = androidx.compose.ui.graphics.SolidColor(SynqPrimary),
                decorationBox = { innerTextField ->
                    Box(contentAlignment = Alignment.CenterStart) {
                        if (cityText.isEmpty()) {
                            Text(
                                "Search city (e.g. Hyderabad)",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF9E8A7F),
                            )
                        }
                        innerTextField()
                    }
                },
            )
            Spacer(Modifier.width(8.dp))
            Box(
                Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .clickable { onSearchCity(cityText) },
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Filled.Search,
                    contentDescription = "Search city",
                    tint = if (searchActive) SynqPrimary else Color(0xFF8C766C),
                    modifier = Modifier.size(18.dp),
                )
            }
        }
    }
    Spacer(Modifier.height(8.dp))

    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            "Quick:",
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
            ),
            color = Color(0xFF8F776C),
        )
        Spacer(Modifier.width(8.dp))
        DemoCities.forEach { city ->
            Surface(
                onClick = { onSearchCity(city) },
                shape = RoundedCornerShape(50.dp),
                color = PillBg,
            ) {
                Text(
                    city,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = PillText,
                )
            }
            Spacer(Modifier.width(6.dp))
        }
    }
}

// ──────────── Wearable Card ────────────

@Composable
private fun WearableContent(
    state: SynqUiState,
    onConnectHealthConnect: () -> Unit,
    onInstallHealthConnect: () -> Unit,
    onUseDemoData: () -> Unit,
    onSyncHealthConnect: () -> Unit,
    onDisconnectHealthConnect: () -> Unit,
    onLogHydration: (Int) -> Unit,
    onSaveManualSleep: (Double) -> Unit,
    onToggleBaseline: (Boolean) -> Unit,
    onBaselineSleepChange: (Double) -> Unit,
    onBaselineRhrChange: (Double) -> Unit,
    clockTick: Long,
) {
    when (val status = state.healthStatus) {
        HealthConnectUiStatus.Checking -> {
            Text(
                "Checking Health Connect\u2026",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
            )
        }
        HealthConnectUiStatus.NotInstalled -> {
            Text(
                "Health Connect isn't installed on this device. Install it from the Play Store, then come back and connect Synq so your fitness data can sync.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
            )
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = onInstallHealthConnect,
                    shape = RoundedCornerShape(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ButtonPrimary),
                ) { Text("Install Health Connect") }
                TextButton(onClick = onUseDemoData) { Text("Use demo data") }
            }
        }
        HealthConnectUiStatus.UpdateRequired -> {
            Text(
                "Health Connect needs an update from the Play Store before Synq can read your data. Update it, then tap Connect Health Connect.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
            )
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = onInstallHealthConnect,
                    shape = RoundedCornerShape(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ButtonPrimary),
                ) { Text("Update Health Connect") }
                TextButton(onClick = onUseDemoData) { Text("Use demo data") }
            }
        }
        HealthConnectUiStatus.NotSupported -> {
            Text(
                "Health Connect isn't available on this device.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
            )
            Spacer(Modifier.height(12.dp))
            FilledTonalButton(onClick = onUseDemoData) { Text("Use demo data instead") }
        }
        HealthConnectUiStatus.NeedsPermission -> {
            Text(
                "Steps, sleep, resting heart rate, exercise, and hydration are read straight from Health Connect \u2014 the same privacy store your fitness tracker writes to. Nothing leaves your device.",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
            )
            Spacer(Modifier.height(14.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = onConnectHealthConnect,
                    shape = RoundedCornerShape(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ButtonPrimary),
                ) { Text("Connect Health Connect") }
                TextButton(onClick = onUseDemoData) { Text("Skip (Demo)") }
            }
        }
        is HealthConnectUiStatus.Error -> {
            Text(status.message, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error)
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                FilledTonalButton(onClick = onSyncHealthConnect) { Text("Retry") }
                TextButton(onClick = onUseDemoData) { Text("Use demo data") }
            }
        }
        is HealthConnectUiStatus.Connected -> {
            val w = state.wearable
            val steps = w?.steps ?: 0L
            val activeMin = w?.activityMinutes ?: 0
            val sleepH = state.effectiveSleepHours
            val rhr = w?.restingHeartRate
            val hydL = state.hydrationLoggedMl / 1000f

            // ── 2×3 wearable metric cards ──
            Row(
                Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Box(Modifier.weight(1f).fillMaxHeight()) {
                    WearableMetricCard(
                        label = "Steps",
                        value = "%,d".format(steps),
                        icon = Icons.Filled.Bolt,
                        iconTint = WearableIconSteps,
                        subtitle = "Goal: 10,000 steps",
                        subtitleColor = WearableSubtitle,
                        progressFraction = (steps / 10_000f).coerceIn(0f, 1f),
                        progressColor = ProgressBarFillSteps,
                    )
                }
                Box(Modifier.weight(1f).fillMaxHeight()) {
                    WearableMetricCard(
                        label = "Active minutes",
                        value = "$activeMin",
                        unit = "min",
                        icon = Icons.Filled.Schedule,
                        iconTint = WearableIconActive,
                        badge = {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = BadgeLightExertion,
                            ) {
                                Text(
                                    "Light exertion",
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                    ),
                                    color = BadgeLightExertionText,
                                )
                            }
                        },
                    )
                }
            }
            Spacer(Modifier.height(10.dp))
            Row(
                Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Box(Modifier.weight(1f).fillMaxHeight()) {
                    val sleepLabel: String
                    val sleepBadgeColor: Color
                    val sleepBadgeTextColor: Color
                    when {
                        sleepH == null || sleepH <= 0.0 -> {
                            sleepLabel = "No data"
                            sleepBadgeColor = BadgeBaseline
                            sleepBadgeTextColor = BadgeBaselineText
                        }
                        sleepH < 5.0 -> {
                            sleepLabel = "Insufficient"
                            sleepBadgeColor = Color(0xFFFFEBEE)
                            sleepBadgeTextColor = Color(0xFFC62828)
                        }
                        sleepH < 7.0 -> {
                            sleepLabel = "Fair"
                            sleepBadgeColor = BadgeLightExertion
                            sleepBadgeTextColor = BadgeLightExertionText
                        }
                        else -> {
                            sleepLabel = "Restful"
                            sleepBadgeColor = BadgeRestful
                            sleepBadgeTextColor = BadgeRestfulText
                        }
                    }
                    WearableMetricCard(
                        label = "Sleep last night",
                        value = sleepH?.let { "%.2f".format(it) } ?: "\u2014",
                        unit = "hr",
                        icon = Icons.Filled.DarkMode,
                        iconTint = WearableIconSleep,
                        badge = {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = sleepBadgeColor,
                            ) {
                                Text(
                                    sleepLabel,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                    ),
                                    color = sleepBadgeTextColor,
                                )
                            }
                        },
                    )
                }
                Box(Modifier.weight(1f).fillMaxHeight()) {
                    WearableMetricCard(
                        label = "Resting HR",
                        value = rhr?.let { "${it.roundToInt()}" } ?: "\u2014",
                        unit = "bpm",
                        icon = Icons.Filled.Favorite,
                        iconTint = WearableIconRhr,
                        badge = {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = BadgeBaseline,
                            ) {
                                Text(
                                    "Baseline typical",
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                    ),
                                    color = BadgeBaselineText,
                                )
                            }
                        },
                    )
                }
            }
            Spacer(Modifier.height(10.dp))
            Row(
                Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Box(Modifier.weight(1f).fillMaxHeight()) {
                    WearableMetricCard(
                        label = "Hydration logged",
                        value = "%.2f".format(hydL),
                        unit = "L",
                        icon = Icons.Filled.WaterDrop,
                        iconTint = WearableIconHydration,
                        progressFraction = (hydL / 2.3f).coerceIn(0f, 1f),
                        progressColor = ProgressBarFillHydration,
                        subtitle = "Target ~2.3 L",
                        subtitleColor = WearableSubtitle,
                    )
                }
                Box(Modifier.weight(1f).fillMaxHeight()) {
                    val lastSync = w?.let { relativeTime(it.syncedAtMillis, clockTick) }
                    WearableMetricCard(
                        label = "Last sync",
                        value = lastSync?.removeSuffix(" ago") ?: "\u2014",
                        unit = if (lastSync != null && lastSync.endsWith(" ago")) "ago" else null,
                        icon = Icons.Filled.VerifiedUser,
                        iconTint = WearableIconSync,
                        subtitle = "Encrypted local store",
                        subtitleColor = WearableIconSync,
                    )
                }
            }

            // ── Quick action controls ──
            Spacer(Modifier.height(16.dp))
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = ControlsBg,
                border = BorderStroke(1.dp, ControlsBorder),
            ) {
                Column(Modifier.padding(14.dp)) {
                    // Log hydration row
                    Column(Modifier.fillMaxWidth()) {
                        Text(
                            "Log hydration:",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFF4B3428),
                        )
                        Spacer(Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            HydrationActionButton("+250 ml") { onLogHydration(250) }
                            HydrationActionButton("+500 ml") { onLogHydration(500) }
                            SyncNowButton(enabled = !state.loggingHydration) { onSyncHealthConnect() }
                        }
                    }
                    ControlSectionDivider()
                    // Sleep override
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            "Override sleep (hr):",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                            color = Color(0xFF5A453A),
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            var sleepText by remember { mutableStateOf(state.manualSleepHours?.toString() ?: "") }
                            Box(
                                Modifier
                                    .width(64.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color.White)
                                    .border(1.dp, InputBorderDark, RoundedCornerShape(12.dp))
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                BasicTextField(
                                    value = sleepText,
                                    onValueChange = { raw ->
                                        val filtered = raw.filter { c -> c.isDigit() || c == '.' }
                                            .let { s ->
                                                val parts = s.split('.')
                                                if (parts.size > 2) parts[0] + "." + parts.drop(1).joinToString("")
                                                else s
                                            }
                                        if (filtered.length <= 4) sleepText = filtered
                                    },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    textStyle = MaterialTheme.typography.bodySmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary,
                                        textAlign = TextAlign.Center,
                                    ),
                                    cursorBrush = SolidColor(SynqPrimary),
                                )
                            }
                            Spacer(Modifier.width(6.dp))
                            Surface(
                                onClick = {
                                    sleepText.toDoubleOrNull()?.let { v ->
                                        if (v in 0.0..12.0) onSaveManualSleep(v)
                                    }
                                },
                                shape = RoundedCornerShape(12.dp),
                                color = ButtonSave,
                            ) {
                                Text(
                                    "Save",
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                    color = ButtonSaveText,
                                )
                            }
                        }
                    }
                    ControlSectionDivider()
                    // Baseline toggle
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            "Compare against personal baseline",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                            ),
                            color = Color(0xFF4D392E),
                        )
                        CompactSwitch(checked = state.baselineOn, onCheckedChange = onToggleBaseline)
                    }
                }
            }

            // ── Baseline sliders ──
            if (state.baselineOn) {
                Spacer(Modifier.height(12.dp))
                BaselineSlider(
                    label = "My usual sleep",
                    valueLabel = "%.2f hr".format(state.baselineSleepHours),
                    value = state.baselineSleepHours.toFloat(),
                    range = 5f..9f,
                    onChange = { onBaselineSleepChange(it.toDouble()) },
                )
                Spacer(Modifier.height(10.dp))
                BaselineSlider(
                    label = "My usual resting heart rate",
                    valueLabel = "${state.baselineRestingHeartRate.roundToInt()} bpm",
                    value = state.baselineRestingHeartRate.toFloat(),
                    range = 45f..100f,
                    onChange = { onBaselineRhrChange(it.toDouble()) },
                )
            }

            // ── Disconnect link ──
            Spacer(Modifier.height(16.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                Surface(
                    onClick = onDisconnectHealthConnect,
                    shape = RoundedCornerShape(8.dp),
                    color = Color.Transparent,
                ) {
                    Text(
                        "Disconnect Health Connect",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            textDecoration = TextDecoration.Underline,
                        ),
                        color = Color(0xFF8C583F),
                    )
                }
            }
        }
    }
}

@Composable
private fun HydrationActionButton(text: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(50.dp),
        color = Color.White,
        border = BorderStroke(1.dp, InputBorder),
    ) {
        Text(
            text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
            color = Color(0xFF4A2616),
        )
    }
}

@Composable
private fun SyncNowButton(enabled: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = { if (enabled) onClick() },
        shape = RoundedCornerShape(50.dp),
        color = if (enabled) ButtonPrimary else ButtonPrimary.copy(alpha = 0.5f),
    ) {
        Row(
            Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                Icons.Filled.Sync,
                contentDescription = null,
                tint = SynqOnPrimary,
                modifier = Modifier.size(12.dp),
            )
            Spacer(Modifier.width(4.dp))
            Text(
                "Sync now",
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                color = SynqOnPrimary,
                maxLines = 1,
                softWrap = false,
            )
        }
    }
}

@Composable
private fun ControlSectionDivider() {
    Spacer(Modifier.height(12.dp))
    HorizontalDivider(color = ControlsDivider, thickness = 1.dp)
    Spacer(Modifier.height(4.dp))
}

@Composable
private fun CompactSwitch(checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    val thumbX by animateDpAsState(if (checked) 18.dp else 2.dp, label = "switchThumb")
    Surface(
        onClick = { onCheckedChange(!checked) },
        shape = RoundedCornerShape(50.dp),
        color = if (checked) ToggleOn else ToggleOff,
        modifier = Modifier
            .width(36.dp)
            .height(20.dp),
    ) {
        Box {
            Box(
                Modifier
                    .offset(x = thumbX, y = 2.dp)
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(Color.White),
            )
        }
    }
}

@Composable
private fun BaselineSlider(label: String, valueLabel: String, value: Float, range: ClosedFloatingPointRange<Float>, onChange: (Float) -> Unit) {
    Column {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            Text(valueLabel, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = TextPrimary)
        }
        androidx.compose.material3.Slider(
            value = value,
            onValueChange = onChange,
            valueRange = range,
            colors = androidx.compose.material3.SliderDefaults.colors(
                thumbColor = TextPrimary,
                activeTrackColor = SynqPrimary,
            ),
        )
    }
}

// ──────────── Guidance Card ────────────

@Composable
private fun GuidanceContent(state: SynqUiState, risk: RiskSnapshot, color: Color) {
    // ── Main contributor card ──
    if (risk.contributors.isNotEmpty()) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.7f)),
            border = BorderStroke(1.dp, Color(0xFFE9DFD6)),
        ) {
            Column(Modifier.padding(14.dp)) {
                Text(
                    "What's driving your score",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                    color = Color(0xFF3E2417),
                )
                Spacer(Modifier.height(10.dp))
                risk.contributors.take(4).forEachIndexed { index, contributor ->
                    if (index > 0) Spacer(Modifier.height(10.dp))
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            contributor.label,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFF3E2417),
                        )
                        Text(
                            "${contributor.score.toInt()} / 100",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold),
                            color = Color(0xFFB95D34),
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFFEADCCF)),
                    ) {
                        Box(
                            Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(fraction = (contributor.score / 100.0).coerceIn(0.0, 1.0).toFloat())
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFFB95D34)),
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
                val top = risk.contributors.first()
                Row(verticalAlignment = Alignment.Top) {
                    Box(
                        Modifier
                            .padding(top = 4.dp)
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFB95D34)),
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        risk.summary,
                        style = MaterialTheme.typography.bodySmall.copy(lineHeight = 18.sp),
                        color = Color(0xFF534036),
                    )
                }
            }
        }
    } else {
        Text(
            "No single factor is driving the risk right now.",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
        )
    }

    // ── Later today outlook ──
    Spacer(Modifier.height(16.dp))
    Text(
        "Later today outlook",
        style = MaterialTheme.typography.bodySmall.copy(
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
        ),
        color = Color(0xFF4B3428),
        letterSpacing = 0.5.sp,
    )
    Spacer(Modifier.height(10.dp))

    val forecastPoints = state.forecast.take(3).map { it.toForecastPoint() }
    if (forecastPoints.isNotEmpty()) {
        val scored = forecastPoints.map { fc ->
            val envScore = RiskEngine.environmentalScore(fc)
            val scorePct = envScore?.let { it.roundToInt().coerceIn(0, 100) }
            val barColor = when {
                scorePct == null -> OutlookBarGreen
                scorePct < 30 -> OutlookBarGreen
                scorePct < 50 -> OutlookBarOlive
                else -> Color(0xFFB95D34)
            }
            Triple(fc.time, scorePct, barColor)
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            scored.forEach { (time, scorePct, barColor) ->
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.6f)),
                    border = BorderStroke(1.dp, WearableCardBorder),
                ) {
                    Column(
                        Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            time,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = OutlookTimeLabel,
                        )
                        Spacer(Modifier.height(6.dp))
                        Box(
                            Modifier
                                .width(28.dp)
                                .height(12.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(barColor),
                        )
                        Spacer(Modifier.height(6.dp))
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.Bottom,
                        ) {
                            Text(
                                "${scorePct ?: "\u2014"}",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Black),
                                color = OutlookScore,
                            )
                            if (scorePct != null) {
                                Text(
                                    "/100",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextMuted,
                                    modifier = Modifier.padding(bottom = 1.dp),
                                )
                            }
                        }
                    }
                }
            }
        }
    } else {
        // Fallback: no forecast data
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("Now", "Later", "Evening").forEach { time ->
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.6f)),
                    border = BorderStroke(1.dp, WearableCardBorder),
                ) {
                    Column(
                        Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(time, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = OutlookTimeLabel)
                        Spacer(Modifier.height(6.dp))
                        Box(
                            Modifier
                                .width(28.dp)
                                .height(12.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(OutlookBarGreen),
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "\u2014",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Black),
                            color = OutlookScore,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }
        }
    }

    // ── Callout banner ──
    val advice = RiskEngine.timeAdvice(forecastPoints)
    if (advice != null) {
        Spacer(Modifier.height(12.dp))
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = OutlookCallout,
        ) {
            Row(
                Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    Icons.Filled.Info,
                    contentDescription = null,
                    tint = OutlookCalloutIcon,
                    modifier = Modifier.size(16.dp),
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    advice,
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                    color = OutlookCalloutText,
                )
            }
        }
    }
}

// ──────────── Footer ────────────

@Composable
private fun Footer() {
    Column(
        Modifier
            .padding(top = 8.dp, bottom = 24.dp)
            .padding(horizontal = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        HorizontalDivider(color = FooterDivider)
        Spacer(Modifier.height(16.dp))
        Text(
            "Synq estimates heat and outdoor sun risk by combining live micro-weather data with optional Health Connect vitals. It does not provide medical diagnosis or replace clinical judgment. If you experience dizziness, cramps, or distress, rest immediately in a cool area and seek medical care.",
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 11.sp,
                lineHeight = 18.sp,
            ),
            color = FooterText,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(6.dp))
        Text(
            "Weather & UV metrics sourced transparently via Open-Meteo. Wearable data runs privately and locally on-device.",
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
            color = FooterTextFaint,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("Privacy First", "Device Encrypted", "Open Standards").forEachIndexed { i, label ->
                if (i > 0) Text("\u2022", color = FooterBadge, style = MaterialTheme.typography.bodySmall)
                Text(
                    label,
                    color = FooterBadge,
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                )
            }
        }
    }
}

// ──────────── Shared Helpers ────────────

private data class RiskSnapshot(
    val riskScore: Int,
    val riskLevel: RiskLevel,
    val dataQuality: DataQuality,
    val summary: String,
    val hydrationTargetMl: Int?,
    val contributors: List<com.synq.app.data.risk.Contributor>,
    val recommendations: List<String>,
    val missingFactors: List<Factor>,
)

private fun ForecastCard.toForecastPoint() = ForecastPoint(
    time = time,
    temperatureC = temperatureC,
    humidityPct = humidityPct,
    uvIndex = uvIndex,
    airQualityAqi = airQualityAqi,
)

private fun computeRiskSnapshot(state: SynqUiState): RiskSnapshot {
    val inputs = com.synq.app.data.risk.RiskInputs(
        temperatureC = state.temperatureC,
        humidityPct = state.humidityPct,
        uvIndex = state.uvIndex,
        airQualityAqi = state.aqi,
        sleepHours = if (state.isHealthConnected) state.effectiveSleepHours else null,
        restingHeartRate = if (state.isHealthConnected) state.wearable?.restingHeartRate else null,
        activityMinutes = if (state.isHealthConnected) state.wearable?.activityMinutes?.toDouble() else null,
        baselineSleepHours = if (state.isHealthConnected && state.baselineOn) state.baselineSleepHours else null,
        baselineRestingHeartRate = if (state.isHealthConnected && state.baselineOn) state.baselineRestingHeartRate else null,
    )
    val result = RiskEngine.calculate(inputs, state.mlRiskScore)
    val top = result.contributors.take(4)
    val summary = RiskEngine.buildSummary(result, top)
    val target = if (state.envStatus == EnvStatus.READY) {
        RiskEngine.hydrationTargetMl(
            state.temperatureC, state.humidityPct,
            if (state.isHealthConnected) state.wearable?.activityMinutes?.toDouble() else null
        )
    } else null
    return RiskSnapshot(
        riskScore = result.riskScore,
        riskLevel = result.riskLevel,
        dataQuality = result.dataQuality,
        summary = summary,
        hydrationTargetMl = target,
        contributors = result.contributors,
        recommendations = if (top.isNotEmpty()) top.map { RiskEngine.FACTOR_RECOMMENDATIONS.getValue(it.factor) } else result.recommendations,
        missingFactors = result.missingFactors,
    )
}

private fun formatClock(millis: Long): String {
    val sdf = SimpleDateFormat("h:mm a", Locale.getDefault())
    return sdf.format(Date(millis))
}

private fun relativeTime(millis: Long, @Suppress("UNUSED_PARAMETER") tick: Long): String {
    val secs = (System.currentTimeMillis() - millis) / 1000
    return when {
        secs < 5 -> "Just now"
        secs < 60 -> "${secs}s ago"
        else -> "${secs / 60}m ago"
    }
}
