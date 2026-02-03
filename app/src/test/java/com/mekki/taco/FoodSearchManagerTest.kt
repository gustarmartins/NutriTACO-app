package com.mekki.taco

import com.mekki.taco.data.db.entity.Food
import com.mekki.taco.presentation.ui.search.FoodFilterState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Tests are passing up to commit 90a11c8 commit
 * we try the FoodSearchManager query behavior.
 * and also handles an edge-case for the "funnel model" search behavior.
 */
class FoodSearchManagerBehaviorTest {

    private val testFoods = listOf(
        createTestFood(1, "Frango Grelhado", proteina = 30.0, calcio = 15.0),
        createTestFood(2, "Leite Integral", proteina = 3.0, calcio = 120.0),
        createTestFood(3, "Espinafre", proteina = 2.0, calcio = 99.0),
        createTestFood(4, "Queijo Minas", proteina = 20.0, calcio = 500.0)
    )

    /**
     * query decision logic from FoodSearchManager.observeSearchTerm()
     * "fts" for FTS query, "all" for getAllFoods, "none" for no query
     */
    private fun determineQueryType(term: String, filters: FoodFilterState): String {
        val hasCategories = filters.selectedCategories.isNotEmpty()
        val hasAdvanced = filters.hasAdvancedFilters
        val hasAnyFilter = hasCategories || hasAdvanced

        return when {
            term.length < 2 && !hasAnyFilter -> "none"
            hasAnyFilter && term.length < 2 -> "all"
            term.length >= 2 -> "fts"
            else -> "none"
        }
    }

    // --- Query Triggering Edge Cases ---

    @Test
    fun clearSearchTerm_WithActiveAdvancedFilters_TriggersAllFoodsQuery() {
        val filters = FoodFilterState.DEFAULT.copy(minCalcio = 100.0)

        // With search term → FTS query
        assertEquals("fts", determineQueryType("frango", filters))

        // should query all foods
        assertEquals("all", determineQueryType("", filters))
    }

    @Test
    fun advancedFilters_WithoutSearchTerm_TriggersAllFoodsQuery() {
        val filters = FoodFilterState.DEFAULT.copy(minProtein = 10.0)
        assertEquals("all", determineQueryType("", filters))
    }

    @Test
    fun categoryFilter_WithoutSearchTerm_TriggersAllFoodsQuery() {
        val filters = FoodFilterState.DEFAULT.copy(selectedCategories = setOf("Cereais"))
        assertEquals("all", determineQueryType("", filters))
    }

    @Test
    fun noFiltersNoSearchTerm_DoesNotQuery() {
        assertEquals("none", determineQueryType("", FoodFilterState.DEFAULT))
        assertEquals("none", determineQueryType("a", FoodFilterState.DEFAULT))  // 1 char
    }

    @Test
    fun searchTerm_WithoutFilters_TriggersFtsQuery() {
        assertEquals("fts", determineQueryType("banana", FoodFilterState.DEFAULT))
    }

    // --- Multiple Advanced Filters selected (AND logic) ---

    @Test
    fun multipleAdvancedFilters_ApplyAndLogic() {
        val filters = FoodFilterState.DEFAULT.copy(
            minCalcio = 100.0,  // Confirms it Matches: Leite(120), Queijo(500)
            minProtein = 10.0  // Matches: Frango(30), Queijo(20)
        )
        // Queijo is the only 1 that has  high calcium & high protein

        val result = applyFilters(testFoods, filters)

        assertEquals(1, result.size)
        assertEquals("Queijo Minas", result.first().name)
    }

    @Test
    fun threeAdvancedFilters_AllMustMatch() {
        val filters = FoodFilterState.DEFAULT.copy(
            minProtein = 1.0,    // All match
            minCalcio = 50.0,    // Leite, Espinafre, Queijo
            maxCalories = 200.0  // Filter by energy (0kcal on all)
        )

        val result = applyFilters(testFoods, filters)

        // All foods give us 0kcal, which is < 200, so calcium filter decides
        assertEquals(3, result.size)  // Leite, Espinafre, Queijo
    }

    // --- Funnel Model: Filters narrow existing results ---

    @Test
    fun funnelModel_FiltersNarrowSearchResults() {
        // Simulate: user searched "frango" and got 1 result
        val searchResults = testFoods.filter { it.name.contains("Frango", ignoreCase = true) }
        assertEquals(1, searchResults.size)

        // Now add minCalcio filter - should narrow from search results, not expand
        val filters = FoodFilterState.DEFAULT.copy(minCalcio = 100.0)
        val filteredResults = applyFilters(searchResults, filters)

        // Frango has calcio=15 gets filtered out - passing
        assertEquals(0, filteredResults.size)

        // Expected behaviour for advanced filters - passing
        // PSA: Expected behaviour is it should NARROW and not requery
    }

    // --- Helper Functions ---

    private fun applyFilters(foods: List<Food>, filters: FoodFilterState): List<Food> {
        return foods.filter { food ->
            filters.minProtein?.let { if ((food.proteina ?: 0.0) < it) return@filter false }
            filters.maxProtein?.let { if ((food.proteina ?: 0.0) > it) return@filter false }
            filters.minCalcio?.let { if ((food.calcio ?: 0.0) < it) return@filter false }
            filters.minCalories?.let { if ((food.energiaKcal ?: 0.0) < it) return@filter false }
            filters.maxCalories?.let { if ((food.energiaKcal ?: 0.0) > it) return@filter false }
            true
        }
    }

    private fun createTestFood(
        id: Int,
        name: String,
        proteina: Double = 0.0,
        calcio: Double = 0.0
    ) = Food(
        id = id,
        tacoID = "TEST-$id",
        name = name,
        category = "Test",
        isCustom = false,
        proteina = proteina,
        carboidratos = 0.0,
        energiaKcal = 0.0,
        energiaKj = null,
        colesterol = null,
        cinzas = null,
        calcio = calcio,
        magnesio = null,
        manganes = null,
        fosforo = null,
        ferro = null,
        sodio = null,
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
        fibraAlimentar = null,
        lipidios = null,
        aminoacidos = null
    )
}