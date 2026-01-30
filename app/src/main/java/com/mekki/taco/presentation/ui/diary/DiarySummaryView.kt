package com.mekki.taco.presentation.ui.diary

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Grain
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.Waves
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
    goalMode: DiaryGoalMode = DiaryGoalMode.DEFICIT,
    onDayClick: ((LocalDate) -> Unit)? = null,
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
            enter = fadeIn(tween(600, delayMillis = 150)) + slideInVertically(
                tween(
                    600,
                    delayMillis = 150
                )
            ) { 40 }
        ) {
            DietInsightsCard(
                summary = summary,
                isMonthlyMode = isMonthlyMode,
                goalMode = goalMode
            )
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
                isMonthlyMode = isMonthlyMode,
                goalMode = goalMode,
                onDayClick = onDayClick
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

            if (summary.currentStreak > 0) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.LocalFireDepartment,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = "${summary.currentStreak}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    Text(
                        text = "Dias seguidos",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
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

@Composable
private fun DietInsightsCard(
    summary: DiarySummary,
    isMonthlyMode: Boolean,
    goalMode: DiaryGoalMode = DiaryGoalMode.DEFICIT,
    modifier: Modifier = Modifier
) {
    val periodDays = if (isMonthlyMode) summary.dailyCalories.size.coerceAtLeast(1) else 7
    val dailyGoal by remember(summary) {
        derivedStateOf {
            summary.dailyCalories.firstOrNull()?.goalKcal ?: 2000.0
        }
    }
    val periodGoal = dailyGoal * periodDays
    val deficit = periodGoal - summary.totalKcal
    val deficitAbs = kotlin.math.abs(deficit)
    val percentage =
        if (periodGoal > 0) (summary.totalKcal / periodGoal * 100).coerceIn(0.0, 150.0) else 0.0

    val estimatedKgChange = deficit / 7700.0
    val projectedWeightIn5Weeks = estimatedKgChange * 5

    val tolerance = dailyGoal * 0.1 * periodDays
    val isOnTrack = deficitAbs < tolerance
    val isUnder = deficit > 0
    val isOver = deficit < 0

    val expectedDays = if (isMonthlyMode) {
        LocalDate.now().lengthOfMonth()
    } else 7
    val isIncomplete = summary.daysLogged < expectedDays

    val (statusColor, statusIcon, statusText) = when (goalMode) {
        DiaryGoalMode.DEFICIT -> when {
            isOnTrack -> Triple(
                MaterialTheme.colorScheme.primary,
                Icons.Default.LocalFireDepartment,
                "Você está no caminho certo!"
            )

            isUnder -> Triple(
                MaterialTheme.colorScheme.tertiary,
                Icons.AutoMirrored.Filled.TrendingDown,
                "Você ficou ${deficitAbs.toInt()} kcal abaixo da meta"
            )

            else -> Triple(
                MaterialTheme.colorScheme.error,
                Icons.AutoMirrored.Filled.TrendingUp,
                "Você excedeu ${deficitAbs.toInt()} kcal da meta"
            )
        }

        DiaryGoalMode.SURPLUS -> when {
            isOnTrack -> Triple(
                MaterialTheme.colorScheme.primary,
                Icons.Default.LocalFireDepartment,
                "Você está no caminho certo!"
            )

            isOver -> Triple(
                MaterialTheme.colorScheme.tertiary,
                Icons.AutoMirrored.Filled.TrendingUp,
                "Bom! Você consumiu ${deficitAbs.toInt()} kcal acima da meta"
            )

            else -> Triple(
                MaterialTheme.colorScheme.error,
                Icons.AutoMirrored.Filled.TrendingDown,
                "Atenção: ${deficitAbs.toInt()} kcal abaixo da meta para ganho"
            )
        }

        DiaryGoalMode.MAINTAIN -> when {
            isOnTrack -> Triple(
                MaterialTheme.colorScheme.primary,
                Icons.Default.LocalFireDepartment,
                "Você está mantendo o equilíbrio!"
            )

            isUnder -> Triple(
                MaterialTheme.colorScheme.secondary,
                Icons.AutoMirrored.Filled.TrendingDown,
                "Você ficou ${deficitAbs.toInt()} kcal abaixo da meta"
            )

            else -> Triple(
                MaterialTheme.colorScheme.secondary,
                Icons.AutoMirrored.Filled.TrendingUp,
                "Você excedeu ${deficitAbs.toInt()} kcal da meta"
            )
        }
    }

    // TODO Should probably get a few professional opinions on what to write here.
    val motivationalMessage = when (goalMode) {
        DiaryGoalMode.DEFICIT -> when {
            isOnTrack -> "Continue assim! Consistência é a chave."
            isUnder && deficitAbs > dailyGoal -> "Lembre-se de se nutrir adequadamente."
            isUnder -> "Bom progresso! Cuide para não restringir demais."
            deficitAbs < dailyGoal * 2 -> "Uma refeição livre por semana ajuda à adesão ao plano."
            else -> "Considere revisar suas refeições com calma."
        }

        DiaryGoalMode.SURPLUS -> when {
            isOnTrack -> "Continue assim! Consistência leva ao ganho."
            isOver && deficitAbs > dailyGoal -> "Ótimo superávit! Foco nos treinos."
            isOver -> "Bom progresso para ganho de massa!"
            deficitAbs < dailyGoal -> "Tente consumir um pouco mais amanhã."
            else -> "Lembre-se de comer o suficiente para seus objetivos."
        }

        DiaryGoalMode.MAINTAIN -> when {
            isOnTrack -> "Perfeito equilíbrio! Continue assim."
            else -> "Pequeno desvio, mas nada preocupante."
        }
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Insights do Período",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            val minDaysRequired = if (isMonthlyMode) 5 else 2
            val hasEnoughData = summary.daysLogged >= minDaysRequired

            if (!hasEnoughData) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.CalendarToday,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.size(40.dp)
                    )
                    Text(
                        text = "Registre suas refeições para ver insights precisos",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (summary.daysLogged > 0) {
                        Text(
                            text = "${summary.daysLogged} de $minDaysRequired dias mínimos registrados",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
            } else {
                if (isIncomplete) {
                    val loggedDates = summary.dailyCalories.map { it.date }.toSet()
                    val today = LocalDate.now()
                    val periodStart =
                        if (isMonthlyMode) today.withDayOfMonth(1) else today.with(java.time.DayOfWeek.MONDAY)
                    val allDatesInPeriod =
                        (0 until expectedDays).map { periodStart.plusDays(it.toLong()) }
                            .filter { it <= today }
                    val missingDates = allDatesInPeriod.filter { it !in loggedDates }
                    val missingDaysText = if (missingDates.size <= 5) {
                        missingDates.joinToString(", ") { it.dayOfMonth.toString() }
                    } else {
                        missingDates.take(4).joinToString(", ") { it.dayOfMonth.toString() } +
                                " +${missingDates.size - 4}"
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                                RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Dados parciais: Apenas ${summary.daysLogged} de ${allDatesInPeriod.size} dias registrados",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                        // TODO improve so it can be clicked
                        if (missingDates.isNotEmpty()) {
                            Text(
                                text = "Dias sem registro: $missingDaysText",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${periodGoal.toInt()}",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Meta (kcal)",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${summary.totalKcal.toInt()}",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = statusColor
                        )
                        Text(
                            text = "Real (kcal)",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }

                androidx.compose.material3.LinearProgressIndicator(
                    progress = { (percentage / 100).toFloat().coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp),
                    color = statusColor,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = statusIcon,
                        contentDescription = null,
                        tint = statusColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = statusText,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = statusColor
                    )
                }

                if (kotlin.math.abs(projectedWeightIn5Weeks) >= 0.1) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "📈",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        val weightText = when (goalMode) {
                            DiaryGoalMode.DEFICIT -> if (projectedWeightIn5Weeks > 0) {
                                "Projeção 5 semanas: ~%.1f kg a menos".format(
                                    projectedWeightIn5Weeks
                                )
                            } else {
                                "Projeção 5 semanas: ~%.1f kg a mais".format(-projectedWeightIn5Weeks)
                            }

                            DiaryGoalMode.SURPLUS -> if (projectedWeightIn5Weeks < 0) {
                                "Projeção 5 semanas: ~%.1f kg de ganho".format(-projectedWeightIn5Weeks)
                            } else {
                                "Projeção 5 semanas: ~%.1f kg a menos (déficit)".format(
                                    projectedWeightIn5Weeks
                                )
                            }

                            DiaryGoalMode.MAINTAIN -> "Variação projetada: ~%.1f kg".format(
                                kotlin.math.abs(projectedWeightIn5Weeks)
                            )
                        }
                        Text(
                            text = weightText,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.LocalFireDepartment,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "${summary.daysOnTarget} de ${summary.daysLogged} dias na meta",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "💡",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = motivationalMessage,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
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