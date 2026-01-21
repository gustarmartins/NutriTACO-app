package com.mekki.taco.data.model

data class UserProfile(
    val weight: Double? = null,
    val height: Double? = null,
    val age: Int? = null,
    val sex: String? = null,
    val activityLevel: ActivityLevel? = null,
    val proteinGoalPerKg: Double = 2.0,
    val carbsGoalPerKg: Double = 2.0,
    val fatGoalPerKg: Double = 0.8,
    val waterGoalPerMlPerKg: Double = 40.0,
    val calorieGoal: Double? = null,
    val isDarkMode: Boolean = true
)
