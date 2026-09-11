package com.synq.app.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.synq.app.ui.theme.IconCircleBg
import com.synq.app.ui.theme.MetricTileBg
import com.synq.app.ui.theme.MetricTileBorder
import com.synq.app.ui.theme.ProgressBarTrack
import com.synq.app.ui.theme.TextMuted
import com.synq.app.ui.theme.TextPrimary
import com.synq.app.ui.theme.TextSecondary
import com.synq.app.ui.theme.WearableCardBg
import com.synq.app.ui.theme.WearableCardBorder
import com.synq.app.ui.theme.WearableLabel
import com.synq.app.ui.theme.WearableSubtitle
import com.synq.app.ui.theme.WearableUnit
import com.synq.app.ui.theme.WearableValue

@Composable
fun StatTile(
    label: String,
    value: String,
    unit: String? = null,
    modifier: Modifier = Modifier,
    note: String? = null,
    loading: Boolean = false,
    accent: Color? = null,
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MetricTileBg),
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                label,
                style = MaterialTheme.typography.labelMedium,
                color = TextSecondary,
            )
            Spacer(Modifier.height(4.dp))
            if (loading) {
                Column(Modifier.fillMaxWidth()) {
                    ShimmerBar(width = 56.dp, height = 22.dp)
                    Spacer(Modifier.height(4.dp))
                    ShimmerBar(width = 44.dp, height = 11.dp)
                }
            } else {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        value,
                        style = MaterialTheme.typography.headlineSmall,
                        color = TextPrimary,
                    )
                    if (unit != null) {
                        Spacer(Modifier.width(4.dp))
                        Text(
                            unit,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                            ),
                            color = TextSecondary,
                            modifier = Modifier.padding(bottom = 2.dp),
                        )
                    }
                }
            }
            if (!note.isNullOrBlank() && !loading) {
                Spacer(Modifier.height(2.dp))
                Text(
                    note,
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted,
                )
            }
        }
    }
}

@Composable
fun MetricTile(
    label: String,
    value: String,
    unit: String? = null,
    icon: ImageVector,
    iconTint: Color,
    subtitle: String? = null,
    progressFraction: Float? = null,
    progressColor: Color = MaterialTheme.colorScheme.primary,
    modifier: Modifier = Modifier,
    loading: Boolean = false,
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MetricTileBg),
        border = BorderStroke(1.dp, MetricTileBorder),
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    label,
                    style = MaterialTheme.typography.labelMedium,
                    color = WearableLabel,
                    modifier = Modifier.weight(1f, fill = false),
                    softWrap = false,
                    maxLines = 1,
                )
                Box(
                    Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(IconCircleBg),
                    contentAlignment = Alignment.Center,
                ) {
                    androidx.compose.material3.Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(16.dp),
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            if (loading) {
                Column(Modifier.fillMaxWidth()) {
                    ShimmerBar(width = 68.dp, height = 24.dp)
                    Spacer(Modifier.height(4.dp))
                    ShimmerBar(width = 52.dp, height = 12.dp)
                }
            } else {
                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Bottom,
                ) {
                    Text(
                        value,
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Black,
                        ),
                        color = WearableValue,
                        modifier = Modifier.weight(1f, fill = false),
                        softWrap = false,
                        maxLines = 1,
                    )
                    if (unit != null) {
                        Spacer(Modifier.width(4.dp))
                        Text(
                            unit,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                            ),
                            color = WearableUnit,
                            modifier = Modifier.padding(bottom = 2.dp),
                            softWrap = false,
                            maxLines = 1,
                        )
                    }
                }
            }
            if (!subtitle.isNullOrBlank() && !loading) {
                Spacer(Modifier.height(4.dp))
                Text(
                    subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = iconTint,
                    maxLines = 1,
                    softWrap = false,
                )
            }
            if (progressFraction != null && !loading) {
                Spacer(Modifier.height(8.dp))
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(ProgressBarTrack),
                ) {
                    Box(
                        Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(fraction = progressFraction.coerceIn(0f, 1f))
                            .clip(RoundedCornerShape(3.dp))
                            .background(progressColor),
                    )
                }
            }
        }
    }
}

@Composable
fun WearableMetricCard(
    label: String,
    value: String,
    unit: String? = null,
    icon: ImageVector,
    iconTint: Color,
    modifier: Modifier = Modifier,
    badge: @Composable (() -> Unit)? = null,
    subtitle: String? = null,
    subtitleColor: Color = WearableSubtitle,
    progressFraction: Float? = null,
    progressColor: Color = MaterialTheme.colorScheme.primary,
    loading: Boolean = false,
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = WearableCardBg),
        border = BorderStroke(1.dp, WearableCardBorder),
    ) {
        Column(
            Modifier
                .fillMaxHeight()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    label,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.SemiBold,
                    ),
                    color = WearableLabel,
                )
                androidx.compose.material3.Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(16.dp),
                )
            }
            Spacer(Modifier.height(8.dp))
            Column {
                if (loading) {
                    ShimmerBar(width = 60.dp, height = 22.dp)
                    Spacer(Modifier.height(4.dp))
                    ShimmerBar(width = 44.dp, height = 11.dp)
                } else {
                    Row(
                        Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Bottom,
                    ) {
                        Text(
                            value,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = (-0.5).sp,
                            ),
                            color = WearableValue,
                        )
                        if (unit != null) {
                            Spacer(Modifier.width(4.dp))
                            Text(
                                unit,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.SemiBold,
                                ),
                                color = WearableUnit,
                                modifier = Modifier.padding(bottom = 2.dp),
                            )
                        }
                    }
                }
                if (badge != null && !loading) {
                    Spacer(Modifier.height(8.dp))
                    badge()
                }
                if (progressFraction != null && !loading) {
                    Spacer(Modifier.height(8.dp))
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(ProgressBarTrack),
                    ) {
                        Box(
                            Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(fraction = progressFraction.coerceIn(0f, 1f))
                                .clip(RoundedCornerShape(3.dp))
                                .background(progressColor),
                        )
                    }
                }
                if (!subtitle.isNullOrBlank() && !loading) {
                    Spacer(
                        Modifier.height(
                            if (progressFraction != null) 6.dp else 8.dp,
                        ),
                    )
                    Text(
                        subtitle,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.sp,
                            fontWeight = if (subtitle == "Encrypted local store") FontWeight.SemiBold else null,
                        ),
                        color = subtitleColor,
                    )
                }
            }
        }
    }
}

@Composable
fun ShimmerBar(width: Dp, height: Dp) {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val alpha by transition.animateFloat(
        initialValue = 0.06f,
        targetValue = 0.16f,
        animationSpec = infiniteRepeatable(
            tween(900, easing = FastOutSlowInEasing),
            RepeatMode.Reverse,
        ),
        label = "shimmerAlpha",
    )
    Box(
        Modifier
            .width(width)
            .height(height)
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = alpha))
    )
}

@Composable
fun StatGrid(modifier: Modifier = Modifier, columns: Int = 2, content: List<@Composable () -> Unit>) {
    Column(modifier) {
        content.chunked(columns).forEach { rowItems ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                rowItems.forEach { item ->
                    Box(Modifier.weight(1f)) { item() }
                }
                repeat(columns - rowItems.size) { Spacer(Modifier.weight(1f)) }
            }
            Spacer(Modifier.height(10.dp))
        }
    }
}

@Composable
fun ContributorRow(label: String, score: Double, color: Color, modifier: Modifier = Modifier) {
    val animatedScore by animateFloatAsState(
        targetValue = score.toFloat(),
        animationSpec = tween(650),
        label = "contribScore",
    )
    Row(modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(
            label,
            modifier = Modifier.width(118.dp),
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary,
        )
        Box(
            Modifier
                .weight(1f)
                .height(10.dp)
                .clip(RoundedCornerShape(5.dp))
                .background(ProgressBarTrack)
        ) {
            Box(
                Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(fraction = (animatedScore / 100f).coerceIn(0f, 1f))
                    .clip(RoundedCornerShape(5.dp))
                    .background(Brush.horizontalGradient(listOf(color.copy(alpha = 0.7f), color)))
            )
        }
        Spacer(Modifier.width(10.dp))
        Text(
            "${animatedScore.toInt()}",
            style = MaterialTheme.typography.labelSmall,
            color = color,
            modifier = Modifier.width(26.dp),
        )
    }
}

@Composable
fun RecommendationRow(text: String, accent: Color, modifier: Modifier = Modifier) {
    Row(modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
        Box(
            Modifier
                .padding(top = 6.dp)
                .size(6.dp)
                .clip(CircleShape)
                .background(accent)
        )
        Spacer(Modifier.width(12.dp))
        Text(text, style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
    }
}

data class TimelinePointUi(val time: String, val scorePct: Int?, val color: Color)

@Composable
fun TimelineRow(points: List<TimelinePointUi>, modifier: Modifier = Modifier) {
    Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
        points.forEach { p ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f),
            ) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.6f)),
                    border = BorderStroke(1.dp, WearableCardBorder),
                ) {
                    Column(
                        Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            p.time,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                            ),
                            color = TextSecondary,
                        )
                        Spacer(Modifier.height(6.dp))
                        Box(
                            Modifier
                                .width(28.dp)
                                .height(12.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(p.color),
                        )
                        Spacer(Modifier.height(6.dp))
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                "${p.scorePct ?: "—"}",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Black,
                                ),
                                color = TextPrimary,
                            )
                            if (p.scorePct != null) {
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
    }
}
