package com.nhockool1002.costoftrips.ui.screens.common

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

data class DonutSlice(val label: String, val value: Double, val color: Color)

/**
 * Donut chart with the grand total in the middle, plus a color-keyed legend.
 * Slices smaller than a sliver are still given a hairline sweep so they stay visible.
 */
@Composable
fun SpendingDonutChart(
    slices: List<DonutSlice>,
    centerLabel: String,
    modifier: Modifier = Modifier
) {
    val total = slices.sumOf { it.value }
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(120.dp), contentAlignment = Alignment.Center) {
            Canvas(modifier = Modifier.size(120.dp)) {
                val strokeWidth = size.minDimension * 0.22f
                var startAngle = -90f
                slices.forEach { slice ->
                    val sweep = if (total > 0) (slice.value / total * 360.0).toFloat() else 0f
                    drawArc(
                        color = slice.color,
                        startAngle = startAngle,
                        sweepAngle = sweep.coerceAtLeast(if (slice.value > 0) 2f else 0f),
                        useCenter = false,
                        style = Stroke(width = strokeWidth)
                    )
                    startAngle += sweep
                }
            }
            Text(
                centerLabel,
                style = MaterialTheme.typography.labelSmall,
                textAlign = TextAlign.Center,
                modifier = Modifier.width(76.dp)
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            slices.forEach { slice ->
                val percent = if (total > 0) (slice.value / total * 100).toInt() else 0
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(slice.color, CircleShape)
                    )
                    Text(
                        "${slice.label} · $percent%",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        }
    }
}

data class BarDatum(val label: String, val value: Double)

/** Simple vertical bar chart, bars scaled relative to the tallest value. */
@Composable
fun TrendBarChart(bars: List<BarDatum>, modifier: Modifier = Modifier) {
    val maxValue = bars.maxOfOrNull { it.value } ?: 0.0
    val maxBarHeight = 100.dp
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Bottom
    ) {
        bars.forEach { bar ->
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                val heightFraction = if (maxValue > 0) (bar.value / maxValue).toFloat() else 0f
                Box(
                    modifier = Modifier
                        .width(20.dp)
                        .height((maxBarHeight.value * heightFraction).dp.coerceAtLeast(2.dp))
                        .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(6.dp))
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(bar.label, style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}
