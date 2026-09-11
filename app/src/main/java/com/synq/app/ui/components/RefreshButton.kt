package com.synq.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.synq.app.ui.theme.ButtonRefresh
import com.synq.app.ui.theme.ButtonRefreshText

private val RefreshIconOutline: ImageVector by lazy {
    ImageVector.Builder(
        name = "RefreshHeroiconsOutline",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).apply {
        path(
            pathFillType = PathFillType.NonZero,
            fill = null,
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round,
        ) {
            moveTo(4f, 4f)
            verticalLineToRelative(5f)
            horizontalLineToRelative(0.582f)
            moveToRelative(15.356f, 2f)
            arcTo(8.001f, 8.001f, 0f, false, false, 4.582f, 9f)
            moveToRelative(0f, 0f)
            horizontalLineTo(9f)
            moveToRelative(11f, 11f)
            verticalLineToRelative(-5f)
            horizontalLineToRelative(-0.581f)
            moveToRelative(0f, 0f)
            arcToRelative(8.003f, 8.003f, 0f, false, true, -15.357f, -2f)
            moveToRelative(15.357f, 2f)
            horizontalLineTo(15f)
        }
    }.build()
}

/**
 * Compact flat refresh pill matching the HTML reference:
 * `rounded-full` `bg-[#EADCCF]` `px-3.5 py-1.5` `text-xs font-medium`
 * with a 14sp stroke-style refresh icon.
 */
@Composable
fun RefreshButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Surface(
        onClick = { if (enabled) onClick() },
        modifier = modifier,
        shape = RoundedCornerShape(50.dp),
        color = if (enabled) ButtonRefresh else ButtonRefresh.copy(alpha = 0.5f),
        contentColor = ButtonRefreshText,
    ) {
        Row(
            Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Icon(
                imageVector = RefreshIconOutline,
                contentDescription = "Refresh conditions",
                modifier = Modifier.size(14.dp),
            )
            Spacer(Modifier.width(6.dp))
            Text(
                "Refresh",
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 0.sp,
                ),
            )
        }
    }
}