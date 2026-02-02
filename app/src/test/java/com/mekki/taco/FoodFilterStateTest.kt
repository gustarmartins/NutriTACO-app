package com.mekki.taco

import com.mekki.taco.data.db.entity.Food
import com.mekki.taco.presentation.ui.search.FoodFilterState
import com.mekki.taco.presentation.ui.search.FoodSource
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FoodFilterStateTest {

    private fun createTestFood(
        id: Int = 1,
        name: String = "Test Food",
        category: String = "Cereais",
        isCustom: Boolean = false,
        proteina: Double? = 10.0,
        carboidratos: Double? = 20.0,
        energiaKcal: Double? = 150.0,
        sodio: Double? = 100.0,
        fibraAlimentar: Double? = 5.0
    ) = Food(
        id = id,
        tacoID = "TEST-$id",
        name = name,
        category = category,
        isCustom = isCustom,
        proteina = proteina,
        carboidratos = carboidratos,
        energiaKcal = energiaKcal,
        energiaKj = null,
        colesterol = null,
        cinzas = null,
        calcio = null,
        magnesio = null,
        manganes = null,
        fosforo = null,
        ferro = null,
        sodio = sodio,
        potassio = null,
        cobre = null,
        zinco = null,
        retinol = null,
        RE = null,
        RAE = null,
        tiamina = null,
        riboflavina = null,
        piridoxina = null,
        niacina = null,
        vitaminaC = null,
        umidade = null,
        fibraAlimentar = fibraAlimentar,
        lipidios = null,
        aminoacidos = null
    )

    private fun applyFilters(foods: List<Food>, filters: FoodFilterState): List<Food> {
        return foods.filter { food ->
            val sourceMatch = when (filters.source) {
                FoodSource.ALL -> true
                FoodSource.TACO -> !food.isCustom
                FoodSource.CUSTOM -> food.isCustom
            }
            if (!sourceMatch) return@filter false

            if (filters.selectedCategories.isNotEmpty() && food.category !in filters.selectedCategories) {
                return@filter false
            }

            filters.minProtein?.let { if ((food.proteina ?: 0.0) < it) return@filter false }
            filters.maxProtein?.let { if ((food.proteina ?: 0.0) > it) return@filter false }
            filters.minCarbs?.let { if ((food.carboidratos ?: 0.0) < it) return@filter false }
            filters.maxCarbs?.let { if ((food.carboidratos ?: 0.0) > it) return@filter false }
            filters.minCalories?.let { if ((food.energiaKcal ?: 0.0) < it) return@filter false }
            filters.maxCalories?.let { if ((food.energiaKcal ?: 0.0) > it) return@filter false }
            filters.minSodio?.let { if ((food.sodio ?: 0.0) < it) return@filter false }
            filters.maxSodio?.let { if ((food.sodio ?: 0.0) > it) return@filter false }
            filters.minFibra?.let { if ((food.fibraAlimentar ?: 0.0) < it) return@filter false }

            true
        }
    }

    // --- Source Filter Tests ---

    @Test
    fun sourceFilter_All_ReturnsAllFoods() {
        val foods = listOf(
            createTestFood(id = 1, isCustom = false),
            createTestFood(id = 2, isCustom = true)
        )
        val filters = FoodFilterState.DEFAULT

        val result = applyFilters(foods, filters)

        assertEquals(2, result.size)
    }

    @Test
    fun sourceFilter_TacoOnly_ExcludesCustomFoods() {
        val foods = listOf(
            createTestFood(id = 1, name = "Arroz", isCustom = false),
            createTestFood(id = 2, name = "Meu Arroz", isCustom = true)
        )
        val filters = FoodFilterState.DEFAULT.copy(source = FoodSource.TACO)

        val result = applyFilters(foods, filters)

        assertEquals(1, result.size)
        assertEquals("Arroz", result.first().name)
    }

    @Test
    fun sourceFilter_CustomOnly_ExcludesTacoFoods() {
        val foods = listOf(
            createTestFood(id = 1, name = "Arroz", isCustom = false),
            createTestFood(id = 2, name = "Meu Arroz", isCustom = true)
        )
        val filters = FoodFilterState.DEFAULT.copy(source = FoodSource.CUSTOM)

        val result = applyFilters(foods, filters)

        assertEquals(1, result.size)
        assertEquals("Meu Arroz", result.first().name)
    }

    // --- Category Filter Tests ---

    @Test
    fun categoryFilter_EmptyCategories_ReturnsAllFoods() {
        val foods = listOf(
            createTestFood(id = 1, category = "Cereais"),
            createTestFood(id = 2, category = "Frutas")
        )
        val filters = FoodFilterState.DEFAULT

        val result = applyFilters(foods, filters)

        assertEquals(2, result.size)
    }

    @Test
    fun categoryFilter_SingleCategory_FiltersCorrectly() {
        val foods = listOf(
            createTestFood(id = 1, name = "Arroz", category = "Cereais"),
            createTestFood(id = 2, name = "Maçã", category = "Frutas"),
            createTestFood(id = 3, name = "Aveia", category = "Cereais")
        )
        val filters = FoodFilterState.DEFAULT.copy(selectedCategories = setOf("Cereais"))

        val result = applyFilters(foods, filters)

        assertEquals(2, result.size)
        assertTrue(result.all { it.category == "Cereais" })
    }

    @Test
    fun categoryFilter_MultipleCategories_FiltersCorrectly() {
        val foods = listOf(
            createTestFood(id = 1, category = "Cereais"),
            createTestFood(id = 2, category = "Frutas"),
            createTestFood(id = 3, category = "Carnes")
        )
        val filters = FoodFilterState.DEFAULT.copy(selectedCategories = setOf("Cereais", "Frutas"))

        val result = applyFilters(foods, filters)

        assertEquals(2, result.size)
        assertTrue(result.none { it.category == "Carnes" })
    }

    // --- Macro Filter Tests ---

    @Test
    fun proteinFilter_MinProtein_FiltersCorrectly() {
        val foods = listOf(
            createTestFood(id = 1, name = "Low Protein", proteina = 5.0),
            createTestFood(id = 2, name = "High Protein", proteina = 25.0)
        )
        val filters = FoodFilterState.DEFAULT.copy(minProtein = 10.0)

        val result = applyFilters(foods, filters)

        assertEquals(1, result.size)
        assertEquals("High Protein", result.first().name)
    }

    @Test
    fun calorieFilter_MaxCalories_FiltersCorrectly() {
        val foods = listOf(
            createTestFood(id = 1, name = "Low Cal", energiaKcal = 50.0),
            createTestFood(id = 2, name = "High Cal", energiaKcal = 500.0)
        )
        val filters = FoodFilterState.DEFAULT.copy(maxCalories = 100.0)

        val result = applyFilters(foods, filters)

        assertEquals(1, result.size)
        assertEquals("Low Cal", result.first().name)
    }

    @Test
    fun sodiumFilter_MaxSodium_FiltersCorrectly() {
        val foods = listOf(
            createTestFood(id = 1, name = "Low Sodium", sodio = 50.0),
            createTestFood(id = 2, name = "High Sodium", sodio = 500.0)
        )
        val filters = FoodFilterState.DEFAULT.copy(maxSodio = 100.0)

        val result = applyFilters(foods, filters)

        assertEquals(1, result.size)
        assertEquals("Low Sodium", result.first().name)
    }

    // --- Combined Filter Tests ---

    @Test
    fun combinedFilters_SourceAndCategory_BothApply() {
        val foods = listOf(
            createTestFood(id = 1, name = "TACO Cereal", category = "Cereais", isCustom = false),
            createTestFood(id = 2, name = "Custom Cereal", category = "Cereais", isCustom = true),
            createTestFood(id = 3, name = "TACO Fruit", category = "Frutas", isCustom = false)
        )
        val filters = FoodFilterState.DEFAULT.copy(
            source = FoodSource.TACO,
            selectedCategories = setOf("Cereais")
        )

        val result = applyFilters(foods, filters)

        assertEquals(1, result.size)
        assertEquals("TACO Cereal", result.first().name)
    }

    @Test
    fun combinedFilters_CategoryAndMacro_BothApply() {
        val foods = listOf(
            createTestFood(id = 1, name = "Low Prot Cereal", category = "Cereais", proteina = 5.0),
            createTestFood(id = 2, name = "High Prot Cereal", category = "Cereais", proteina = 20.0),
            createTestFood(id = 3, name = "High Prot Meat", category = "Carnes", proteina = 25.0)
        )
        val filters = FoodFilterState.DEFAULT.copy(
            selectedCategories = setOf("Cereais"),
            minProtein = 10.0
        )

        val result = applyFilters(foods, filters)

        assertEquals(1, result.size)
        assertEquals("High Prot Cereal", result.first().name)
    }

    // --- FoodFilterState Property Tests ---

    @Test
    fun hasActiveFilters_DefaultState_ReturnsFalse() {
        assertFalse(FoodFilterState.DEFAULT.hasActiveFilters)
    }

    @Test
    fun hasActiveFilters_WithSource_ReturnsTrue() {
        val filters = FoodFilterState.DEFAULT.copy(source = FoodSource.TACO)
        assertTrue(filters.hasActiveFilters)
    }

    @Test
    fun hasActiveFilters_WithCategories_ReturnsTrue() {
        val filters = FoodFilterState.DEFAULT.copy(selectedCategories = setOf("Cereais"))
        assertTrue(filters.hasActiveFilters)
    }

    @Test
    fun hasActiveFilters_WithMacroFilter_ReturnsTrue() {
        val filters = FoodFilterState.DEFAULT.copy(minProtein = 10.0)
        assertTrue(filters.hasActiveFilters)
    }

    @Test
    fun activeAdvancedFilterCount_NoFilters_ReturnsZero() {
        assertEquals(0, FoodFilterState.DEFAULT.activeAdvancedFilterCount)
    }

    @Test
    fun activeAdvancedFilterCount_MultipleMacros_CountsCorrectly() {
        val filters = FoodFilterState.DEFAULT.copy(
            minProtein = 10.0,
            maxCalories = 200.0,
            minFibra = 5.0
        )
        assertEquals(3, filters.activeAdvancedFilterCount)
    }
}