package com.mekki.taco.data.model

import java.time.LocalDate

data class DiarySummary(
    val totalKcal: Double = 0.0,
    val avgKcal: Double = 0.0,
    val totalProtein: Double = 0.0,
    val totalCarbs: Double = 0.0,
    val totalFat: Double = 0.0,
    val totalFiber: Double = 0.0,
    val totalCholesterol: Double = 0.0,
    val totalSodium: Double = 0.0,
    val totalCalcium: Double = 0.0,
    val totalIron: Double = 0.0,
    val totalMagnesium: Double = 0.0,
    val totalPhosphorus: Double = 0.0,
    val totalPotassium: Double = 0.0,
    val totalZinc: Double = 0.0,
    val totalVitaminC: Double = 0.0,
    val totalRetinol: Double = 0.0,
    val totalThiamine: Double = 0.0,
    val totalRiboflavin: Double = 0.0,
    val totalNiacin: Double = 0.0,
    val totalPyridoxine: Double = 0.0,
    val avgProteinPerKg: Double = 0.0,
    val dailyCalories: List<DailyCalorieEntry> = emptyList(),
    val daysLogged: Int = 0,
    val daysOnTarget: Int = 0
)

data class DailyCalorieEntry(
    val date: LocalDate,
    val kcal: Double,
    val goalKcal: Double,
    val isOnTarget: Boolean = kcal <= goalKcal * 1.1 && kcal >= goalKcal * 0.9
)
