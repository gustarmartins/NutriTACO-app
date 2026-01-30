package com.mekki.taco.presentation.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mekki.taco.data.model.DailyCalorieEntry
import com.mekki.taco.presentation.ui.diary.DiaryGoalMode
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun WeeklyCalorieChart(
    dailyCalories: List<DailyCalorieEntry>,
    modifier: Modifier = Modifier,
    isMonthlyMode: Boolean = false,
    goalMode: DiaryGoalMode = DiaryGoalMode.DEFICIT,
    onDayClick: ((LocalDate) -> Unit)? = null
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

    val goalLineColor = MaterialTheme.colorScheme.outline
    val avgGoal = dailyCalories.map { it.goalKcal }.average()
    val goalRatioForLine = (avgGoal / maxKcal).toFloat().coerceIn(0f, 1f)

    // Semantic colors with clear progression
    val colorOnTarget = MaterialTheme.colorScheme.primary
    val colorBelow = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.6f)
    val colorAbove = MaterialTheme.colorScheme.error

    Column(modifier = modifier.fillMaxWidth()) {
        val scrollState = rememberScrollState()

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .then(if (isMonthlyMode) Modifier.horizontalScroll(scrollState) else Modifier)
                .height(chartHeight + 40.dp)
                .padding(horizontal = 8.dp)
                .drawBehind {
                    val goalY = size.height - 40.dp.toPx() - (chartHeight.toPx() * goalRatioForLine)
                    drawLine(
                        color = goalLineColor,
                        start = Offset(0f, goalY),
                        end = Offset(size.width, goalY),
                        strokeWidth = 2.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f))
                    )
                }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(chartHeight + 40.dp),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = if (isMonthlyMode) Arrangement.Start else Arrangement.SpaceEvenly
            ) {
                dailyCalories.forEachIndexed { index, entry ->
                    val heightRatio = (entry.kcal / maxKcal).toFloat().coerceIn(0f, 1f)
                    val animatedHeight =
                        heightRatio * (animatedProgress.getOrNull(index)?.value ?: 0f)

                    val deficit = entry.kcal - entry.goalKcal
                    val barColor = when (goalMode) {
                        DiaryGoalMode.DEFICIT -> when {
                            deficit > 100 -> colorAbove
                            deficit < -100 -> colorBelow
                            else -> colorOnTarget
                        }

                        DiaryGoalMode.SURPLUS -> when {
                            deficit > 100 -> colorBelow
                            deficit < -100 -> colorAbove
                            else -> colorOnTarget
                        }

                        DiaryGoalMode.MAINTAIN -> when {
                            kotlin.math.abs(deficit) > 100 -> colorAbove.copy(alpha = 0.7f)
                            else -> colorOnTarget
                        }
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = (if (isMonthlyMode) {
                            Modifier.padding(horizontal = barSpacing / 2)
                        } else {
                            Modifier.weight(1f)
                        }).then(
                            if (onDayClick != null) {
                                Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { onDayClick(entry.date) }
                            } else Modifier
                        )
                    ) {
                        if (!isMonthlyMode) {
                            Text(
                                text = "${entry.kcal.toInt()}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Visible
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
                                    .height((chartHeight * animatedHeight))
                                    .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                    .background(barColor)
                                    .then(
                                        if (entry.isOnTarget) {
                                            Modifier.border(
                                                1.dp,
                                                MaterialTheme.colorScheme.primary,
                                                RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
                                            )
                                        } else Modifier
                                    )
                            )
                        }

                        Spacer(Modifier.height(4.dp))

                        Text(
                            text = if (isMonthlyMode) {
                                entry.date.dayOfMonth.toString()
                            } else {
                                entry.date.dayOfWeek.getDisplayName(
                                    TextStyle.NARROW,
                                    Locale("pt", "BR")
                                )
                                    .replaceFirstChar { it.uppercase() }
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val (_, _) = when (goalMode) {
                DiaryGoalMode.DEFICIT -> "Acima" to "Abaixo"
                DiaryGoalMode.SURPLUS -> "Abaixo" to "Acima"
                DiaryGoalMode.MAINTAIN -> "Desvio" to "Desvio"
            }
            if (goalMode != DiaryGoalMode.MAINTAIN) {
                LegendDot(
                    color = colorBelow,
                    label = if (goalMode == DiaryGoalMode.DEFICIT) "Abaixo" else "Atenção"
                )
                Spacer(Modifier.width(12.dp))
            }
            LegendDot(color = colorOnTarget, label = "No alvo")
            Spacer(Modifier.width(12.dp))
            LegendDot(
                color = colorAbove,
                label = if (goalMode == DiaryGoalMode.SURPLUS) "Bom" else "Acima"
            )
            Spacer(Modifier.width(12.dp))
            LegendLine(color = goalLineColor, label = "Meta")
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
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            softWrap = false
        )
    }
}

@Composable
private fun LegendLine(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .width(12.dp)
                .height(2.dp)
                .background(color)
        )
        Spacer(Modifier.width(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            softWrap = false
        )
    }
}