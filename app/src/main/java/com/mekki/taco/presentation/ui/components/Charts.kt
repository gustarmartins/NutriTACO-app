package com.mekki.taco.presentation.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import java.text.DecimalFormat

data class PieChartData(val value: Float, val color: Color, val label: String)

enum class ChartType(val label: String) {
    PIE("Gráfico de Pizza"),
    BAR("Gráfico de Barras")
}

@Composable
fun MacroPieChart(
    modifier: Modifier = Modifier,
    data: List<PieChartData>,
    totalValue: Double,
    totalUnit: String,
    showCenterText: Boolean = true
) {
    val totalMacros = data.sumOf { it.value.toDouble() }.toFloat()
    if (totalMacros <= 0f) return

    val angles = data.map { 360f * it.value / totalMacros }
    val animatedProgress = remember(data) { data.map { Animatable(0f) } }

    LaunchedEffect(data) {
        animatedProgress.forEachIndexed { index, animatable ->
            launch {
                animatable.animateTo(
                    targetValue = angles.getOrElse(index) { 0f },
                    animationSpec = tween(durationMillis = 700, delayMillis = index * 120)
                )
            }
        }
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            val canvasSize = 160.dp
            val backgroundCircleColor = MaterialTheme.colorScheme.surfaceVariant

            Canvas(modifier = Modifier.size(canvasSize)) {
                val strokeWidth = size.minDimension * 0.15f
                drawCircle(
                    color = backgroundCircleColor,
                    style = Stroke(width = strokeWidth)
                )

                var startAngle = -90f
                animatedProgress.forEachIndexed { index, animatable ->
                    drawArc(
                        color = data[index].color,
                        startAngle = startAngle,
                        sweepAngle = animatable.value,
                        useCenter = false,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                    startAngle += animatable.value
                }
            }
            if (showCenterText) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    val df = DecimalFormat("#")
                    Text(
                        text = df.format(totalValue),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = totalUnit,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        Legend(data, totalMacros)
    }
}

@Composable
fun MacroBarChart(
    modifier: Modifier = Modifier,
    data: List<PieChartData>,
    totalValue: Double
) {
    val totalMacros = data.sumOf { it.value.toDouble() }.toFloat()
    val maxVal = data.maxOfOrNull { it.value } ?: 1f
    val animatedProgress = remember(data) { data.map { Animatable(0f) } }

    LaunchedEffect(data) {
        animatedProgress.forEachIndexed { index, animatable ->
            launch {
                animatable.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(durationMillis = 800, delayMillis = index * 100)
                )
            }
        }
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(150.dp)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        val dfPercent = DecimalFormat("#")

        data.forEachIndexed { index, item ->
            val heightRatio = if (maxVal > 0) item.value / maxVal else 0f
            val animatedRatio = heightRatio * animatedProgress[index].value
            val percent = if (totalMacros > 0) 100f * item.value / totalMacros else 0f

            val label = when (item.label.lowercase()) {
                "carboidratos" -> "Carbs"
                "proteínas" -> "Prot"
                "gorduras" -> "Gord"
                else -> item.label.take(4)
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "${item.value.toInt()}g",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${dfPercent.format(percent)}%",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 10.sp
                )

                Spacer(Modifier.height(4.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.5f)
                        .height(80.dp * animatedRatio)
                        .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                        .background(item.color)
                )

                Spacer(Modifier.height(4.dp))

                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
private fun Legend(data: List<PieChartData>, totalMacros: Float) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        val dfValue = DecimalFormat("#.#")
        val dfPercent = DecimalFormat("#")

        data.forEach { slice ->
            val percent = if (totalMacros > 0) 100f * slice.value / totalMacros else 0f
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(color = slice.color, shape = CircleShape)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = slice.label,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "${dfValue.format(slice.value)}g (${dfPercent.format(percent)}%)",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.End
                )
            }
        }
    }
}