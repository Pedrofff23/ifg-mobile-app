package com.example.gymapp.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gymapp.ui.theme.Spacing
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.math.ceil
import kotlin.math.floor

// ============================================================
// DATA MODELS FOR CHARTS
// ============================================================

/** A single data point for a progress chart. */
data class ChartDataPoint(
    val date: LocalDate,
    val value: Float,
    val label: String? = null
)

/** Date range filter for charts. */
enum class DateRangeFilter(val label: String, val days: Int?) {
    LAST_30("30 dias", 30),
    LAST_90("90 dias", 90),
    LAST_180("6 meses", 180),
    ALL("Tudo", null)
}

// ============================================================
// PROGRESS LINE CHART — Canvas-based, theme-aware
// ============================================================

@Composable
fun ProgressLineChart(
    data: List<ChartDataPoint>,
    modifier: Modifier = Modifier,
    title: String? = null,
    valueLabel: String = "",
    lineColor: Color = MaterialTheme.colorScheme.primary,
    showDots: Boolean = true,
    showGradient: Boolean = true,
    minPoints: Int = 2
) {
    val textMeasurer = rememberTextMeasurer()
    val surfaceColor = MaterialTheme.colorScheme.surface
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    val primaryContainer = MaterialTheme.colorScheme.primaryContainer

    val filteredData = data.sortedBy { it.date }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(Spacing.md),
        colors = CardDefaults.cardColors(containerColor = surfaceColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(Spacing.lg)) {
            if (title != null) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(Spacing.sm))
            }

            if (filteredData.size < minPoints) {
                // Not enough data — show placeholder
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Dados insuficientes",
                            style = MaterialTheme.typography.bodyMedium,
                            color = onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(Spacing.xs))
                        Text(
                            text = "Registre mais medições para ver o gráfico",
                            style = MaterialTheme.typography.bodySmall,
                            color = onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
            } else {
                // Chart area
                val chartTopPadding = 16f
                val chartBottomPadding = 36f // space for x-axis labels
                val chartStartPadding = 48f // space for y-axis labels
                val chartEndPadding = 16f

                val minValue = filteredData.minOf { it.value }
                val maxValue = filteredData.maxOf { it.value }
                val valueRange = if (maxValue == minValue) 1f else maxValue - minValue
                // Add 10% padding on top/bottom
                val paddedMin = minValue - valueRange * 0.1f
                val paddedMax = maxValue + valueRange * 0.1f
                val paddedRange = paddedMax - paddedMin

                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                ) {
                    val canvasWidth = size.width
                    val canvasHeight = size.height
                    val chartWidth = canvasWidth - chartStartPadding - chartEndPadding
                    val chartHeight = canvasHeight - chartTopPadding - chartBottomPadding

                    // Draw horizontal grid lines + y-axis labels
                    val gridLineCount = 4
                    for (i in 0..gridLineCount) {
                        val fraction = i.toFloat() / gridLineCount
                        val y = chartTopPadding + chartHeight * (1f - fraction)
                        val value = paddedMin + paddedRange * fraction

                        // Grid line (dashed)
                        drawLine(
                            color = onSurfaceVariant.copy(alpha = 0.15f),
                            start = Offset(chartStartPadding, y),
                            end = Offset(canvasWidth - chartEndPadding, y),
                            strokeWidth = 1f,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f))
                        )

                        // Y-axis label
                        val textResult = textMeasurer.measure(
                            text = String.format("%.1f", value),
                            style = androidx.compose.ui.text.TextStyle(
                                fontSize = 10.sp,
                                color = onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        )
                        drawText(
                            textResult,
                            topLeft = Offset(
                                x = chartStartPadding - textResult.size.width - 6f,
                                y = y - textResult.size.height / 2f
                            )
                        )
                    }

                    // Map data points to canvas coordinates
                    val points = filteredData.mapIndexed { index, point ->
                        val xFraction = if (filteredData.size > 1) {
                            index.toFloat() / (filteredData.size - 1)
                        } else 0.5f
                        val yFraction = if (paddedRange != 0f) {
                            ((point.value - paddedMin) / paddedRange).coerceIn(0f, 1f)
                        } else 0.5f
                        Offset(
                            x = chartStartPadding + xFraction * chartWidth,
                            y = chartTopPadding + chartHeight * (1f - yFraction)
                        )
                    }

                    // Draw gradient fill under line
                    if (showGradient && points.size >= 2) {
                        val fillPath = Path().apply {
                            moveTo(points.first().x, chartTopPadding + chartHeight)
                            points.forEach { point ->
                                lineTo(point.x, point.y)
                            }
                            lineTo(points.last().x, chartTopPadding + chartHeight)
                            close()
                        }
                        clipPath(fillPath) {
                            drawRect(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        lineColor.copy(alpha = 0.25f),
                                        lineColor.copy(alpha = 0.02f)
                                    ),
                                    startY = chartTopPadding,
                                    endY = chartTopPadding + chartHeight
                                ),
                                topLeft = Offset(chartStartPadding, chartTopPadding),
                                size = Size(chartWidth, chartHeight)
                            )
                        }
                    }

                    // Draw line
                    if (points.size >= 2) {
                        val linePath = Path().apply {
                            moveTo(points.first().x, points.first().y)
                            for (i in 1 until points.size) {
                                // Smooth curve using quadratic bezier
                                val prev = points[i - 1]
                                val curr = points[i]
                                val midX = (prev.x + curr.x) / 2f
                                quadraticTo(prev.x, prev.y, midX, (prev.y + curr.y) / 2f)
                            }
                            val last = points.last()
                            lineTo(last.x, last.y)
                        }
                        drawPath(
                            path = linePath,
                            color = lineColor,
                            style = Stroke(width = 3f)
                        )
                    }

                    // Draw dots
                    if (showDots) {
                        points.forEach { point ->
                            drawCircle(
                                color = lineColor,
                                radius = 4f,
                                center = point
                            )
                            drawCircle(
                                color = surfaceColor,
                                radius = 2f,
                                center = point
                            )
                        }
                    }

                    // X-axis labels (show up to 6 dates to avoid overlap)
                    val maxLabels = 6
                    val step = if (filteredData.size <= maxLabels) 1 else {
                        ceil(filteredData.size.toFloat() / maxLabels).toInt()
                    }
                    for (i in filteredData.indices step step) {
                        val point = points[i]
                        val dateStr = filteredData[i].date.format(
                            DateTimeFormatter.ofPattern("dd/MM")
                        )
                        val textResult = textMeasurer.measure(
                            text = dateStr,
                            style = androidx.compose.ui.text.TextStyle(
                                fontSize = 9.sp,
                                color = onSurfaceVariant.copy(alpha = 0.6f)
                            )
                        )
                        drawText(
                            textResult,
                            topLeft = Offset(
                                x = point.x - textResult.size.width / 2f,
                                y = chartTopPadding + chartHeight + 8f
                            )
                        )
                    }
                }

                // Value label
                if (valueLabel.isNotBlank()) {
                    Spacer(modifier = Modifier.height(Spacing.xs))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Text(
                            text = valueLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        }
    }
}

// ============================================================
// DATE RANGE SELECTOR — chip row for filtering chart time range
// ============================================================

@Composable
fun DateRangeSelector(
    selectedRange: DateRangeFilter,
    onRangeSelected: (DateRangeFilter) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Spacing.xs)
    ) {
        DateRangeFilter.entries.forEach { range ->
            FilterChip(
                selected = selectedRange == range,
                onClick = { onRangeSelected(range) },
                label = {
                    Text(
                        text = range.label,
                        style = MaterialTheme.typography.labelSmall
                    )
                },
                shape = RoundedCornerShape(Spacing.sm)
            )
        }
    }
}

// ============================================================
// EXERCISE PROGRESS CHART — shows weight/reps/duration over time
// ============================================================

@Composable
fun ExerciseProgressChart(
    exerciseName: String,
    progressPoints: List<ChartDataPoint>,
    modifier: Modifier = Modifier,
    selectedMetric: String,
    onMetricChanged: (String) -> Unit,
    lineColor: Color = MaterialTheme.colorScheme.primary
) {
    // Determine metric label based on selectedMetric
    val metricLabel = when (selectedMetric) {
        "weight" -> "Peso (kg)"
        "reps" -> "Repetições"
        "duration" -> "Duração (min)"
        "distance" -> "Distância (m)"
        else -> "Valor"
    }
    // Metric selector chips
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Spacing.xs)
        ) {
            val metrics = listOf(
                "weight" to "Peso",
                "reps" to "Reps",
                "duration" to "Duração",
                "distance" to "Distância"
            )
            metrics.forEach { (key, label) ->
                FilterChip(
                    selected = selectedMetric == key,
                    onClick = { onMetricChanged(key) },
                    label = { Text(text = label, style = MaterialTheme.typography.labelSmall) },
                    shape = RoundedCornerShape(Spacing.sm)
                )
            }
        }
        Spacer(modifier = Modifier.height(Spacing.sm))
        ProgressLineChart(
            data = progressPoints,
            modifier = Modifier.fillMaxWidth(),
            title = exerciseName,
            valueLabel = metricLabel,
            lineColor = lineColor,
            showDots = true,
            showGradient = true,
            minPoints = 1
        )
    }
}

// ============================================================
// HELPER: Convert BodyMeasurement list to ChartDataPoint list
// ============================================================

fun List<com.example.gymapp.domain.model.BodyMeasurement>.toWeightChartData(): List<ChartDataPoint> {
    return this.mapNotNull { m ->
        val weight = m.weightKg ?: return@mapNotNull null
        val date = try {
            val raw = m.measuredAt ?: return@mapNotNull null
            // Handle ISO date strings (take first 10 chars for yyyy-MM-dd)
            LocalDate.parse(raw.substring(0, 10))
        } catch (_: Exception) {
            return@mapNotNull null
        }
        ChartDataPoint(date = date, value = weight.toFloat())
    }.sortedBy { it.date }
}

// ============================================================
// HELPER: Convert ExerciseProgressPoint list to ChartDataPoint list
// ============================================================

fun List<com.example.gymapp.domain.model.ExerciseProgressPoint>.toExerciseChartData(
    metricSelector: (com.example.gymapp.domain.model.ExerciseProgressPoint) -> Float?
): List<ChartDataPoint> {
    return this.mapNotNull { point ->
        val value = metricSelector(point) ?: return@mapNotNull null
        val date = try {
            val raw = point.sessionDate ?: return@mapNotNull null
            LocalDate.parse(raw.substring(0, 10))
        } catch (_: Exception) {
            return@mapNotNull null
        }
        ChartDataPoint(date = date, value = value)
    }.sortedBy { it.date }
}

fun List<ChartDataPoint>.filterByDateRange(range: DateRangeFilter): List<ChartDataPoint> {
    if (range.days == null) return this
    val cutoff = LocalDate.now().minusDays(range.days.toLong())
    return this.filter { it.date >= cutoff }
}
