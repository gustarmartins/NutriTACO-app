package com.mekki.taco.presentation.ui.search

enum class FoodSource(val displayName: String) {
    ALL(displayName = "Todos"),
    TACO(displayName = "TACO"),
    CUSTOM(displayName = "Personalizados")
    // TODO we plan on the future adding USP("TBCA")
}

/**
 * Sort options for food lists.
 */
enum class FoodSortOption(val label: String) {
    RELEVANCE("Relevância"),
    NAME("Nome"),
    CALORIES("Calorias"),
    PROTEIN("Proteínas"),
    CARBS("Carboidratos"),
    FAT("Gorduras");

    // Backward compatibility
    val displayName: String get() = label
}

data class FoodFilterState(
    val searchQuery: String = "",
    val source: FoodSource = FoodSource.ALL,
    val sortOption: FoodSortOption = FoodSortOption.RELEVANCE,
    val selectedCategories: Set<String> = emptySet(),
    
    // Macro filters advanced filtering
    val minProtein: Double? = null,
    val maxProtein: Double? = null,
    val minCarbs: Double? = null,
    val maxCarbs: Double? = null,
    val minFat: Double? = null,
    val maxFat: Double? = null,
    val minCalories: Double? = null,
    val maxCalories: Double? = null
) {
    val hasActiveFilters: Boolean
        get() = searchQuery.isNotEmpty() ||
                source != FoodSource.ALL ||
                sortOption != FoodSortOption.RELEVANCE ||
                selectedCategories.isNotEmpty() ||
                hasMacroFilters

    val hasMacroFilters: Boolean
        get() = minProtein != null || maxProtein != null ||
                minCarbs != null || maxCarbs != null ||
                minFat != null || maxFat != null ||
                minCalories != null || maxCalories != null

    companion object {
        val DEFAULT = FoodFilterState()
    }
}