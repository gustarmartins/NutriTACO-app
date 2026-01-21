package com.mekki.taco.data.service

data class ScannedItem(
    val rawName: String,
    val estimatedGrams: Double,
    val mealType: String,
    val matchedId: Int?,
    val backupCalories: Double,
    val backupProtein: Double,
    val backupCarbs: Double,
    val backupFat: Double
)
