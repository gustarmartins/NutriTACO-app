package com.mekki.taco.presentation.ui.search

enum class FoodSource(val displayName: String) {
    ALL(displayName = "Todos"),
    TACO(displayName = "TACO"),
    CUSTOM(displayName = "Personalizados")
}

enum class FoodSortOption(val label: String) {
    RELEVANCE("Relevância"),
    NAME("Nome"),
    CALORIES("Calorias"),
    PROTEIN("Proteínas"),
    CARBS("Carboidratos"),
    FAT("Gorduras");

    val displayName: String get() = label
}

data class FoodFilterState(
    val searchQuery: String = "",
    val source: FoodSource = FoodSource.ALL,
    val sortOption: FoodSortOption = FoodSortOption.RELEVANCE,
    val selectedCategories: Set<String> = emptySet(),

    val minProtein: Double? = null,
    val maxProtein: Double? = null,
    val minCarbs: Double? = null,
    val maxCarbs: Double? = null,
    val minFat: Double? = null,
    val maxFat: Double? = null,
    val minCalories: Double? = null,
    val maxCalories: Double? = null,

    val minVitaminaC: Double? = null,
    val minRetinol: Double? = null,
    val minTiamina: Double? = null,
    val minRiboflavina: Double? = null,
    val minPiridoxina: Double? = null,
    val minNiacina: Double? = null,

    val minCalcio: Double? = null,
    val minFerro: Double? = null,
    val minSodio: Double? = null,
    val maxSodio: Double? = null,
    val minPotassio: Double? = null,
    val minMagnesio: Double? = null,
    val minZinco: Double? = null,
    val minCobre: Double? = null,
    val minFosforo: Double? = null,
    val minManganes: Double? = null,

    val minColesterol: Double? = null,
    val maxColesterol: Double? = null,
    val minFibra: Double? = null,

    val minAminoAcidTotal: Double? = null
) {
    val hasActiveFilters: Boolean
        get() = searchQuery.isNotEmpty() ||
                source != FoodSource.ALL ||
                sortOption != FoodSortOption.RELEVANCE ||
                selectedCategories.isNotEmpty() ||
                hasAdvancedFilters

    val hasMacroFilters: Boolean
        get() = minProtein != null || maxProtein != null ||
                minCarbs != null || maxCarbs != null ||
                minFat != null || maxFat != null ||
                minCalories != null || maxCalories != null

    val hasMicroFilters: Boolean
        get() = minVitaminaC != null || minRetinol != null ||
                minTiamina != null || minRiboflavina != null ||
                minPiridoxina != null || minNiacina != null ||
                minCalcio != null || minFerro != null ||
                minSodio != null || maxSodio != null ||
                minPotassio != null || minMagnesio != null ||
                minZinco != null || minCobre != null ||
                minFosforo != null || minManganes != null ||
                minColesterol != null || maxColesterol != null ||
                minFibra != null || minAminoAcidTotal != null

    val hasAdvancedFilters: Boolean
        get() = hasMacroFilters || hasMicroFilters

    val activeAdvancedFilterCount: Int
        get() = listOfNotNull(
            minProtein, maxProtein, minCarbs, maxCarbs,
            minFat, maxFat, minCalories, maxCalories,
            minVitaminaC, minRetinol, minTiamina, minRiboflavina,
            minPiridoxina, minNiacina, minCalcio, minFerro,
            minSodio, maxSodio, minPotassio, minMagnesio,
            minZinco, minCobre, minFosforo, minManganes,
            minColesterol, maxColesterol, minFibra, minAminoAcidTotal
        ).size

    companion object {
        val DEFAULT = FoodFilterState()
    }
}