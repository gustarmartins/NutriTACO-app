package com.mekki.taco.presentation.ui.diary

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Grain
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.Waves
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mekki.taco.data.model.DiarySummary
import com.mekki.taco.presentation.ui.components.VerticalNutrientCard
import com.mekki.taco.presentation.ui.components.WeeklyCalorieChart
import com.mekki.taco.presentation.ui.theme.LocalNutrientColors
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun DiarySummaryView(
    summary: DiarySummary,
    isMonthlyMode: Boolean = false,
    modifier: Modifier = Modifier
) {
    var showMacros by rememberSaveable { mutableStateOf(true) }
    var showSecondary by rememberSaveable { mutableStateOf(true) }
    var showMinerals by rememberSaveable { mutableStateOf(false) }
    var showVitamins by rememberSaveable { mutableStateOf(false) }

    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isVisible = true
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn(tween(600, delayMillis = 100)) + slideInVertically(
                tween(
                    600,
                    delayMillis = 100
                )
            ) { 40 }
        ) {
            SummaryStatsCard(summary)
        }

        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn(tween(600, delayMillis = 200)) + slideInVertically(
                tween(
                    600,
                    delayMillis = 200
                )
            ) { 40 }
        ) {
            WeeklyCalorieChart(
                dailyCalories = summary.dailyCalories,
                modifier = Modifier.fillMaxWidth(),
                isMonthlyMode = isMonthlyMode
            )
        }

        AnimatedTransitionCard(visible = isVisible, delay = 300) {
            CollapsibleSection(
                title = "Macronutrientes",
                expanded = showMacros,
                onToggle = { showMacros = !showMacros }
            ) {
                val nutrientColors = LocalNutrientColors.current
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    VerticalNutrientCard(
                        label = "Proteínas",
                        value = summary.totalProtein,
                        unit = "g",
                        color = nutrientColors.protein,
                        icon = Icons.Default.FitnessCenter,
                        modifier = Modifier.weight(1f)
                    )
                    VerticalNutrientCard(
                        label = "Carbs",
                        value = summary.totalCarbs,
                        unit = "g",
                        color = nutrientColors.carbs,
                        icon = Icons.Default.Grain,
                        modifier = Modifier.weight(1f)
                    )
                    VerticalNutrientCard(
                        label = "Gorduras",
                        value = summary.totalFat,
                        unit = "g",
                        color = nutrientColors.fat,
                        icon = Icons.Default.WaterDrop,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        AnimatedTransitionCard(visible = isVisible, delay = 400) {
            CollapsibleSection(
                title = "Outros Nutrientes",
                expanded = showSecondary,
                onToggle = { showSecondary = !showSecondary }
            ) {
                val nutrientColors = LocalNutrientColors.current
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    VerticalNutrientCard(
                        label = "Fibra",
                        value = summary.totalFiber,
                        unit = "g",
                        color = nutrientColors.fiber,
                        icon = Icons.Default.Spa,
                        modifier = Modifier.weight(1f)
                    )
                    VerticalNutrientCard(
                        label = "Colesterol",
                        value = summary.totalCholesterol,
                        unit = "mg",
                        color = nutrientColors.cholesterol,
                        icon = Icons.Default.Favorite,
                        modifier = Modifier.weight(1f)
                    )
                    VerticalNutrientCard(
                        label = "Sódio",
                        value = summary.totalSodium,
                        unit = "mg",
                        color = nutrientColors.sodium,
                        icon = Icons.Default.Waves,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        AnimatedTransitionCard(visible = isVisible, delay = 500) {
            CollapsibleSection(
                title = "Minerais",
                expanded = showMinerals,
                onToggle = { showMinerals = !showMinerals }
            ) {
                MineralsGrid(summary)
            }
        }

        AnimatedTransitionCard(visible = isVisible, delay = 600) {
            CollapsibleSection(
                title = "Vitaminas",
                expanded = showVitamins,
                onToggle = { showVitamins = !showVitamins }
            ) {
                VitaminsGrid(summary)
            }
        }

        Spacer(Modifier.height(100.dp))
    }
}

@Composable
fun AnimatedTransitionCard(visible: Boolean, delay: Int, content: @Composable () -> Unit) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(600, delayMillis = delay)) + slideInVertically(
            tween(
                600,
                delayMillis = delay
            )
        ) { 40 }
    ) {
        content()
    }
}

@Composable
private fun SummaryStatsCard(summary: DiarySummary) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            StatItem(
                label = "Total",
                value = summary.totalKcal,
                unit = "kcal"
            )
            StatItem(
                label = "Média/dia",
                value = summary.avgKcal,
                unit = "kcal"
            )
            // Days logged doesn't need float animation, just distinct display
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "${summary.daysLogged}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = "Dias registrados",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
            }

            if (summary.daysOnTarget > 0) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.LocalFireDepartment,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = "${summary.daysOnTarget}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
    }
}

@Composable
private fun StatItem(label: String, value: Double, unit: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        // Animated Counter
        val counter = remember(value) { Animatable(0f) }
        LaunchedEffect(value) {
            counter.animateTo(
                targetValue = value.toFloat(),
                animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing)
            )
        }

        Text(
            text = "${counter.value.toInt()} $unit",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
        )
    }
}

@Composable
private fun CollapsibleSection(
    title: String,
    expanded: Boolean,
    onToggle: () -> Unit,
    content: @Composable () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggle() },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (expanded) "Recolher" else "Expandir",
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    content()
                }
            }
        }
    }
}

@Composable
private fun MineralsGrid(summary: DiarySummary) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            NutrientRow("Cálcio", summary.totalCalcium, "mg", Modifier.weight(1f))
            NutrientRow("Ferro", summary.totalIron, "mg", Modifier.weight(1f))
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            NutrientRow("Magnésio", summary.totalMagnesium, "mg", Modifier.weight(1f))
            NutrientRow("Fósforo", summary.totalPhosphorus, "mg", Modifier.weight(1f))
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            NutrientRow("Potássio", summary.totalPotassium, "mg", Modifier.weight(1f))
            NutrientRow("Zinco", summary.totalZinc, "mg", Modifier.weight(1f))
        }
    }
}

@Composable
private fun VitaminsGrid(summary: DiarySummary) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            NutrientRow("Vitamina C", summary.totalVitaminC, "mg", Modifier.weight(1f))
            NutrientRow("Retinol (A)", summary.totalRetinol, "mcg", Modifier.weight(1f))
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            NutrientRow("Tiamina (B1)", summary.totalThiamine, "mg", Modifier.weight(1f))
            NutrientRow("Riboflavina (B2)", summary.totalRiboflavin, "mg", Modifier.weight(1f))
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            NutrientRow("Niacina (B3)", summary.totalNiacin, "mg", Modifier.weight(1f))
            NutrientRow("Piridoxina (B6)", summary.totalPyridoxine, "mg", Modifier.weight(1f))
        }
    }
}

@Composable
private fun NutrientRow(label: String, value: Double, unit: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.padding(horizontal = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = "%.1f %s".format(value, unit).replace(".0 ", " "),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

fun formatWeekLabel(weekStart: LocalDate): String {
    val weekEnd = weekStart.plusDays(6)
    val formatter = DateTimeFormatter.ofPattern("dd MMM", Locale("pt", "BR"))
    return "${weekStart.format(formatter)} - ${weekEnd.format(formatter)}"
}

fun formatMonthLabel(monthStart: LocalDate): String {
    val formatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale("pt", "BR"))
    return monthStart.format(formatter).replaceFirstChar { it.uppercase() }
}
