package com.mekki.taco.utils

import com.mekki.taco.data.db.entity.Food

enum class NutrientWarning {
    HIGH_SODIUM,
    HIGH_SATURATED_FAT
}

object NutrientWarnings {

    private const val SODIUM_THRESHOLD_MG_PER_100G = 600.0
    private const val SATURATED_FAT_THRESHOLD_G_PER_100G = 6.0

    @Suppress("DEPRECATION")
    fun getWarningsForFood(food: Food): Set<NutrientWarning> {
        val warnings = mutableSetOf<NutrientWarning>()

        food.sodio?.let { sodio ->
            if (sodio >= SODIUM_THRESHOLD_MG_PER_100G) {
                warnings.add(NutrientWarning.HIGH_SODIUM)
            }
        }

        food.lipidios?.saturados?.let { saturados ->
            if (saturados >= SATURATED_FAT_THRESHOLD_G_PER_100G) {
                warnings.add(NutrientWarning.HIGH_SATURATED_FAT)
            }
        }

        return warnings
    }

    fun getWarningsForPortion(food: Food, portionGrams: Double): Set<NutrientWarning> {
        if (portionGrams <= 0) return emptySet()
        
        val factor = portionGrams / 100.0
        val warnings = mutableSetOf<NutrientWarning>()

        food.sodio?.let { sodio ->
            val scaled = sodio * factor
            if (scaled >= SODIUM_THRESHOLD_MG_PER_100G * factor) {
                warnings.add(NutrientWarning.HIGH_SODIUM)
            }
        }

        food.lipidios?.saturados?.let { saturados ->
            val scaled = saturados * factor
            if (scaled >= SATURATED_FAT_THRESHOLD_G_PER_100G * factor) {
                warnings.add(NutrientWarning.HIGH_SATURATED_FAT)
            }
        }

        return warnings
    }
}
