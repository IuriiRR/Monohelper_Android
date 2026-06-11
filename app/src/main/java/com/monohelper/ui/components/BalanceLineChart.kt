package com.monohelper.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.monohelper.domain.model.BalancePoint

private val ChartHeight = 160.dp

/** Plain-Canvas balance-over-time line chart (no chart dependency). */
@Composable
fun BalanceLineChart(
    points: List<BalancePoint>,
    modifier: Modifier = Modifier,
) {
    if (points.size < 2) {
        Box(
            modifier = modifier.fillMaxWidth().height(ChartHeight),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                "Not enough data for chart",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        return
    }

    val lineColor = MaterialTheme.colorScheme.primary
    val sorted = points.sortedBy { it.time }

    Canvas(modifier = modifier.fillMaxWidth().height(ChartHeight)) {
        val minTime = sorted.first().time
        val maxTime = sorted.last().time
        val minBalance = sorted.minOf { it.balance }
        val maxBalance = sorted.maxOf { it.balance }
        val timeSpan = (maxTime - minTime).coerceAtLeast(1).toFloat()
        val balanceSpan = (maxBalance - minBalance).coerceAtLeast(1).toFloat()

        // 5% vertical padding so the line never touches the edges.
        val topPad = size.height * 0.05f
        val usableHeight = size.height - 2 * topPad

        fun xOf(time: Long): Float = (time - minTime) / timeSpan * size.width
        fun yOf(balance: Long): Float =
            topPad + (1f - (balance - minBalance) / balanceSpan) * usableHeight

        val line = Path().apply {
            moveTo(xOf(sorted.first().time), yOf(sorted.first().balance))
            for (point in sorted.drop(1)) {
                lineTo(xOf(point.time), yOf(point.balance))
            }
        }
        val fill = Path().apply {
            addPath(line)
            lineTo(size.width, size.height)
            lineTo(0f, size.height)
            close()
        }

        drawPath(
            path = fill,
            brush = Brush.verticalGradient(listOf(lineColor.copy(alpha = 0.25f), Color.Transparent)),
        )
        drawPath(
            path = line,
            color = lineColor,
            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round),
        )
    }
}
