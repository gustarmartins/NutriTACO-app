package com.mekki.taco.presentation.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mekki.taco.data.model.DailyCalorieEntry
import kotlinx.coroutines.launch
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun WeeklyCalorieChart(
    dailyCalories: List<DailyCalorieEntry>,
    modifier: Modifier = Modifier,
    isMonthlyMode: Boolean = false
) {
    if (dailyCalories.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(180.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "Nenhum registro encontrado",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    val maxKcal =
        (dailyCalories.maxOfOrNull { maxOf(it.kcal, it.goalKcal) } ?: 2000.0).coerceAtLeast(100.0)
    val animatedProgress = remember(dailyCalories) { dailyCalories.map { Animatable(0f) } }

    LaunchedEffect(dailyCalories) {
        animatedProgress.forEachIndexed { index, animatable ->
            launch {
                animatable.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(durationMillis = 400, delayMillis = index * 30)
                )
            }
        }
    }

    val barWidth = if (isMonthlyMode) 18.dp else 28.dp
    val barSpacing = if (isMonthlyMode) 4.dp else 8.dp
    val chartHeight = if (isMonthlyMode) 100.dp else 120.dp

    Column(modifier = modifier.fillMaxWidth()) {
        val scrollState = rememberScrollState()

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .then(if (isMonthlyMode) Modifier.horizontalScroll(scrollState) else Modifier)
                .height(chartHeight + 40.dp)
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = if (isMonthlyMode) Arrangement.Start else Arrangement.SpaceEvenly
        ) {
            dailyCalories.forEachIndexed { index, entry ->
                val heightRatio = (entry.kcal / maxKcal).toFloat().coerceIn(0f, 1f)
                val goalRatio = (entry.goalKcal / maxKcal).toFloat().coerceIn(0f, 1f)
                val animatedHeight = heightRatio * (animatedProgress.getOrNull(index)?.value ?: 0f)

                val barColor = when {
                    entry.kcal > entry.goalKcal * 1.1 -> MaterialTheme.colorScheme.tertiary
                    entry.kcal < entry.goalKcal * 0.9 -> MaterialTheme.colorScheme.surfaceVariant
                    else -> MaterialTheme.colorScheme.primary
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = if (isMonthlyMode) {
                        Modifier.padding(horizontal = barSpacing / 2)
                    } else {
                        Modifier.weight(1f)
                    }
                ) {
                    if (!isMonthlyMode) {
                        Text(
                            text = "${entry.kcal.toInt()}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(Modifier.height(4.dp))
                    }

                    Box(
                        modifier = Modifier
                            .width(barWidth)
                            .height(chartHeight),
                        contentAlignment = Alignment.BottomCenter
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height((chartHeight * goalRatio))
                                .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height((chartHeight * animatedHeight))
                                .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                .background(barColor)
                        )
                    }

                    Spacer(Modifier.height(4.dp))

                    Text(
                        text = if (isMonthlyMode) {
                            entry.date.dayOfMonth.toString()
                        } else {
                            entry.date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale("pt", "BR"))
                                .take(3).replaceFirstChar { it.uppercase() }
                        },
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = if (isMonthlyMode) 9.sp else 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            LegendDot(color = MaterialTheme.colorScheme.primary, label = "No alvo")
            Spacer(Modifier.width(16.dp))
            LegendDot(color = MaterialTheme.colorScheme.surfaceVariant, label = "Abaixo")
            Spacer(Modifier.width(16.dp))
            LegendDot(color = MaterialTheme.colorScheme.tertiary, label = "Acima")
        }
    }
}

@Composable
private fun LegendDot(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .width(8.dp)
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(color)
        )
        Spacer(Modifier.width(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
