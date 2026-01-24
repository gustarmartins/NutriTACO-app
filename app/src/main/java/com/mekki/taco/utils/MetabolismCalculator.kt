package com.mekki.taco.utils

import com.mekki.taco.data.model.UserProfile

object BMRCalculator {

    /**
     * Calcula a Taxa Metabólica Basal (TMB) usando a fórmula de Mifflin-St Jeor.
     * Esta é a quantidade de calorias que o corpo queima em repouso.
     */
    fun calculateBMR(profile: UserProfile): Double {
        val weight = profile.weight ?: return 0.0
        val height = profile.height ?: return 0.0
        val age = profile.age ?: return 0.0
        val sex = profile.sex ?: return 0.0

        return if (sex.equals("Masculino", ignoreCase = true)) {
            (10 * weight) + (6.25 * height) - (5 * age) + 5
        } else { // Feminino
            (10 * weight) + (6.25 * height) - (5 * age) - 161
        }
    }
}
