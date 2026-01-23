package com.mekki.taco.data.service

import android.util.Log
import com.mekki.taco.data.db.dao.FoodDao
import com.mekki.taco.data.db.entity.DietItem
import com.mekki.taco.data.db.entity.Food
import com.mekki.taco.data.db.entity.Lipidios
import com.mekki.taco.data.model.DietItemWithFood
import com.mekki.taco.utils.unaccent
import kotlinx.coroutines.flow.first
import java.util.UUID

class SmartFoodMatcher(private val foodDao: FoodDao) {

    private val matchCache = mutableMapOf<String, Food>()

    suspend fun matchItems(scannedItems: List<ScannedItem>): List<DietItemWithFood> {
        val results = mutableListOf<DietItemWithFood>()

        for (item in scannedItems) {
            var food: Food? = null

            // Try to use AI matched ID
            if (item.matchedId != null) {
                val matched = foodDao.getFoodById(item.matchedId).first()
                if (matched != null) {
                    food = matched
                    Log.d("SmartFoodMatcher", "Used AI matched ID: ${matched.name}")
                }
            }

            // If no ID or ID not valid, try to find in TACO by name
            if (food == null) {
                food = findBestMatch(item.rawName)
            }

            // If still not found, create a "Custom AI Food"
            if (food == null) {
                Log.i(
                    "SmartFoodMatcher",
                    "Food '${item.rawName}' not found in TACO. Creating custom entry."
                )
                food = createCustomFood(item)
            }

            // Create the Diet Item
            if (food != null) {
                val dietItem = DietItem(
                    id = UUID.randomUUID().hashCode(),
                    dietId = 0,
                    foodId = food.id,
                    quantityGrams = item.estimatedGrams,
                    mealType = normalizeMealType(item.mealType),
                    consumptionTime = getDefaultTimeForMeal(item.mealType)
                )
                results.add(DietItemWithFood(dietItem, food))
            }
        }
        return results
    }

    private suspend fun findBestMatch(rawTerm: String): Food? {
        if (matchCache.containsKey(rawTerm)) return matchCache[rawTerm]

        val cleanTerm = rawTerm.unaccent().lowercase()
            .replace(
                Regex("(cozido|grelhado|frito|assado|cru|fatia|unidade|colher|sopa|cha|scoop)"),
                ""
            )
            .trim()

        if (cleanTerm.length < 2) return null

        val candidates = foodDao.getFoodsByName(cleanTerm).first()

        // Prioritize exact match, then shortest name
        val bestMatch = candidates.find { it.name.equals(cleanTerm, true) }
            ?: candidates.minByOrNull { it.name.length }

        if (bestMatch != null) {
            matchCache[rawTerm] = bestMatch
        }
        return bestMatch
    }

    private suspend fun createCustomFood(item: ScannedItem): Food {
        // Create a new food entity based on AI estimates
        val newFood = Food(
            tacoID = "AI-${UUID.randomUUID().toString().take(8)}", // Unique AI ID
            name = item.rawName.replaceFirstChar { it.uppercase() } + " (IA)",
            category = "Personalizado",
            energiaKcal = item.backupCalories,
            energiaKj = item.backupCalories * 4.184,
            proteina = item.backupProtein,
            carboidratos = item.backupCarbs,
            lipidios = Lipidios(item.backupFat, 0.0, 0.0, 0.0),
            // Default others to 0 or null
            fibraAlimentar = 0.0, colesterol = 0.0, cinzas = 0.0, calcio = 0.0,
            magnesio = 0.0, manganes = 0.0, fosforo = 0.0, ferro = 0.0,
            sodio = 0.0, potassio = 0.0, cobre = 0.0, zinco = 0.0,
            retinol = 0.0, RE = 0.0, RAE = 0.0, tiamina = 0.0,
            riboflavina = 0.0, piridoxina = 0.0, niacina = 0.0,
            vitaminaC = 0.0, umidade = 0.0, aminoacidos = null
        )

        // Insert into DB and get the new auto-generated ID
        val newId = foodDao.insertFood(newFood)
        return newFood.copy(id = newId.toInt())
    }

    private fun normalizeMealType(raw: String): String {
        return when {
            raw.contains("Café", true) || raw.contains("Desjejum", true) -> "Café da Manhã"
            raw.contains("Almoço", true) -> "Almoço"
            raw.contains("Jantar", true) -> "Jantar"
            raw.contains("Lanche", true) || raw.contains("Pré", true) || raw.contains(
                "Pós",
                true
            ) -> "Lanche"

            raw.contains("Ceia", true) -> "Lanche"
            else -> "Lanche"
        }
    }

    private fun getDefaultTimeForMeal(meal: String): String {
        return when {
            meal.contains("Café", true) -> "08:00"
            meal.contains("Almoço", true) -> "12:30"
            meal.contains("Jantar", true) -> "19:30"
            else -> "16:00"
        }
    }
}