package com.synq.app.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.synq.app.ui.theme.RingAccent
import com.synq.app.ui.theme.RingTrack
import com.synq.app.ui.theme.TextPrimary
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun RiskRing(
    score: Int,
    unknown: Boolean,
    modifier: Modifier = Modifier,
    size: Dp = 112.dp,
    color: Color = RingAccent,
) {
    val target = if (unknown) 0f else score.toFloat()
    val animatedScore by animateFloatAsState(
        targetValue = target,
        animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessVeryLow),
        label = "riskScore"
    )

    Box(modifier.size(size), contentAlignment = Alignment.Center) {
        Canvas(Modifier.size(size)) {
            val strokeWidth = size.toPx() * 0.089f
            drawArc(
                color = RingTrack,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                size = Size(this.size.width - strokeWidth, this.size.height - strokeWidth),
                topLeft = Offset(strokeWidth / 2, strokeWidth / 2),
            )
            // Fraction of the ring filled. The arc is flat-capped and a rounded dot sits
            // exactly at its tip, so low and high values all keep a consistent rounded
            // look, stay proportional, and never get overlapping caps near 100.
            val fraction = (animatedScore / 100f).coerceIn(0f, 1f)
            val arcRadius = (this.size.width - strokeWidth) / 2f
            val cx = this.size.width / 2f
            val cy = this.size.height / 2f
            drawArc(
                color = color,
                startAngle = -90f,
                sweepAngle = -360f * fraction,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Butt),
                size = Size(this.size.width - strokeWidth, this.size.height - strokeWidth),
                topLeft = Offset(strokeWidth / 2, strokeWidth / 2),
            )
            // Rounded start cap at the top of the arc
            if (fraction > 0f) {
                drawCircle(
                    color = color,
                    radius = strokeWidth / 2f,
                    center = Offset(cx, cy - arcRadius),
                )
            }
            if (fraction > 0.015f && fraction < 0.995f) {
                val endRad = ((-90f - 360f * fraction) * Math.PI.toFloat()) / 180f
                drawCircle(
                    color = color,
                    radius = strokeWidth / 2f,
                    center = Offset(
                        cx + arcRadius * cos(endRad),
                        cy + arcRadius * sin(endRad),
                    ),
                )
            } else if (fraction >= 0.995f) {
                drawCircle(
                    color = color,
                    radius = strokeWidth / 2f,
                    center = Offset(cx, cy - arcRadius),
                )
            }
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = if (unknown) "--" else animatedScore.toInt().toString(),
                fontSize = 30.sp,
                fontWeight = FontWeight.ExtraBold,
                color = TextPrimary,
                letterSpacing = (-0.5).sp,
            )
            Text(
                text = "out of 100",
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF7A4F39),
                letterSpacing = 0.sp,
            )
        }
    }
}
