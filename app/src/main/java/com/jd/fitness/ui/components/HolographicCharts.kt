package com.jd.fitness.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jd.fitness.ui.theme.DeepBlackNavy
import com.jd.fitness.ui.theme.NeonCyan
import com.jd.fitness.ui.theme.NeonElectricBlue
import com.jd.fitness.ui.theme.NeonGreen
import com.jd.fitness.ui.theme.NeonPurple
import com.jd.fitness.ui.theme.TextMuted
import com.jd.fitness.ui.theme.TextSubtle
import com.jd.fitness.ui.theme.TextWhite
import java.util.Locale

@Composable
fun WeightLineChart(
    dataPoints: List<Pair<String, Double>>, // Date string, weightKg
    modifier: Modifier = Modifier,
    lineColor: Color = NeonElectricBlue,
    fillColor: Color = NeonElectricBlue.copy(alpha = 0.15f)
) {
    if (dataPoints.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(180.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No weight logs recorded yet.",
                color = TextMuted,
                fontSize = 13.sp
            )
        }
        return
    }

    var selectedIndex by remember { mutableStateOf<Int?>(null) }
    val weights = dataPoints.map { it.second }
    val minWeight = (weights.minOrNull() ?: 50.0) - 1.0
    val maxWeight = (weights.maxOrNull() ?: 80.0) + 1.0
    val weightRange = (maxWeight - minWeight).coerceAtLeast(1.0)

    Column(modifier = modifier.fillMaxWidth()) {
        // Selected data point tooltip
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val selected = selectedIndex?.let { dataPoints.getOrNull(it) } ?: dataPoints.lastOrNull()
            if (selected != null) {
                Text(
                    text = "${String.format(Locale.US, "%.1f", selected.second)} kg",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = NeonCyan
                )
                Text(
                    text = selected.first,
                    fontSize = 12.sp,
                    color = TextMuted
                )
            }
        }

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .pointerInput(dataPoints) {
                    detectTapGestures { offset ->
                        val spacing = size.width / (dataPoints.size - 1).coerceAtLeast(1)
                        val index = (offset.x / spacing).toInt().coerceIn(0, dataPoints.lastIndex)
                        selectedIndex = index
                    }
                }
        ) {
            val width = size.width
            val height = size.height
            val spacing = if (dataPoints.size > 1) width / (dataPoints.size - 1) else width / 2

            // Draw horizontal reference grid lines
            val gridLines = 3
            for (i in 0..gridLines) {
                val y = height * (i.toFloat() / gridLines)
                drawLine(
                    color = Color(0x1A8E9EB8),
                    start = Offset(0f, y),
                    end = Offset(width, y),
                    strokeWidth = 1.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                )
            }

            // Build path
            val strokePath = Path()
            val fillPath = Path()

            val points = dataPoints.mapIndexed { index, pair ->
                val x = if (dataPoints.size == 1) width / 2 else index * spacing
                val normY = ((pair.second - minWeight) / weightRange).toFloat().coerceIn(0f, 1f)
                val y = height - (normY * (height * 0.85f)) - (height * 0.08f)
                Offset(x, y)
            }

            if (points.isNotEmpty()) {
                strokePath.moveTo(points.first().x, points.first().y)
                fillPath.moveTo(points.first().x, height)
                fillPath.lineTo(points.first().x, points.first().y)

                for (i in 1 until points.size) {
                    val prev = points[i - 1]
                    val curr = points[i]
                    val controlX = (prev.x + curr.x) / 2
                    strokePath.cubicTo(controlX, prev.y, controlX, curr.y, curr.x, curr.y)
                    fillPath.cubicTo(controlX, prev.y, controlX, curr.y, curr.x, curr.y)
                }

                fillPath.lineTo(points.last().x, height)
                fillPath.close()

                // Draw gradient fill
                drawPath(
                    path = fillPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(fillColor, Color.Transparent)
                    )
                )

                // Draw stroke line
                drawPath(
                    path = strokePath,
                    color = lineColor,
                    style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                )

                // Draw point markers
                points.forEachIndexed { idx, point ->
                    val isSelected = selectedIndex == idx || (selectedIndex == null && idx == points.lastIndex)
                    drawCircle(
                        color = if (isSelected) NeonCyan else lineColor,
                        radius = if (isSelected) 6.dp.toPx() else 3.5.dp.toPx(),
                        center = point
                    )
                    if (isSelected) {
                        drawCircle(
                            color = Color(0x5500E5FF),
                            radius = 11.dp.toPx(),
                            center = point
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StepBarChart(
    stepDays: List<Pair<String, Int>>, // Date (e.g. "Mon"), stepCount
    modifier: Modifier = Modifier,
    goal: Int = 10000
) {
    if (stepDays.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(160.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "No step data available", color = TextMuted, fontSize = 13.sp)
        }
        return
    }

    val maxSteps = ((stepDays.maxOfOrNull { it.second } ?: 10000).coerceAtLeast(goal) * 1.15f).toInt()

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Target: ${goal / 1000}k steps",
                fontSize = 12.sp,
                color = NeonCyan,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "Health Connect Sync",
                fontSize = 11.sp,
                color = TextSubtle
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
        ) {
            val width = size.width
            val height = size.height
            val barCount = stepDays.size
            val barWidth = (width / (barCount * 1.8f)).coerceIn(12.dp.toPx(), 28.dp.toPx())
            val slotWidth = width / barCount

            // Goal reference dashed line
            val goalY = height - ((goal.toFloat() / maxSteps.toFloat()) * height)
            drawLine(
                color = Color(0x6600E5FF),
                start = Offset(0f, goalY),
                end = Offset(width, goalY),
                strokeWidth = 1.5.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
            )

            // Draw bars
            stepDays.forEachIndexed { index, (dayLabel, count) ->
                val centerX = (index * slotWidth) + (slotWidth / 2)
                val barHeight = ((count.toFloat() / maxSteps.toFloat()) * height).coerceAtLeast(4.dp.toPx())
                val isGoalReached = count >= goal

                val barBrush = if (isGoalReached) {
                    Brush.verticalGradient(listOf(NeonCyan, NeonGreen))
                } else {
                    Brush.verticalGradient(listOf(NeonPurple, Color(0xFF331E6D)))
                }

                drawRoundRect(
                    brush = barBrush,
                    topLeft = Offset(centerX - (barWidth / 2), height - barHeight),
                    size = Size(barWidth, barHeight),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(6.dp.toPx(), 6.dp.toPx())
                )
            }
        }

        // X-Axis labels
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 6.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            stepDays.forEach { (label, _) ->
                Text(
                    text = label.takeLast(5),
                    fontSize = 10.sp,
                    color = TextMuted,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun WeeklyConsistencyHeatmap(
    weeklyScores: List<Pair<String, Float>>, // Day name (M, T, W, T, F, S, S) and completion % (0f to 100f)
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        weeklyScores.forEach { (day, score) ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                val boxColor = when {
                    score >= 100f -> NeonGreen
                    score >= 70f -> NeonElectricBlue
                    score >= 40f -> NeonPurple
                    score > 0f -> Color(0xFF263254)
                    else -> Color(0xFF131A2F)
                }

                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(boxColor),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (score > 0f) "${score.toInt()}%" else "—",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (score > 0f) DeepBlackNavy else TextSubtle
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = day,
                    fontSize = 11.sp,
                    color = TextMuted,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
