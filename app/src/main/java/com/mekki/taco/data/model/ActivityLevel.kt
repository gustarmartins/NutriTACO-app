package com.mekki.taco.data.model

enum class ActivityLevel(val multiplier: Double, val displayName: String) {
    SEDENTARY(1.2, "Sedentário"),
    LIGHT(1.375, "Leve (1-3 dias/semana)"),
    MODERATE(1.55, "Moderado (3-5 dias/semana)"),
    ACTIVE(1.725, "Ativo (6-7 dias/semana)"),
    VERY_ACTIVE(1.9, "Muito Ativo (trabalho físico)")
}