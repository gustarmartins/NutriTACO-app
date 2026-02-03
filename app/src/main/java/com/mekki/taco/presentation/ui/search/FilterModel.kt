package com.mekki.taco.presentation.ui.search

import androidx.compose.ui.graphics.Color
import com.mekki.taco.utils.NutrientesPorPorcao

data class NutrientDisplayInfo(
    val label: String,
    val unit: String,
    val color: Color,
    val getValue: (NutrientesPorPorcao) -> Double?
)

private val DEFAULT_DISPLAYED_SORTS = setOf(
    FoodSortOption.RELEVANCE,
    FoodSortOption.NAME,
    FoodSortOption.CALORIES,
    FoodSortOption.PROTEIN,
    FoodSortOption.CARBS,
    FoodSortOption.FAT
)

fun FoodSortOption.getNutrientDisplayInfo(): NutrientDisplayInfo? {
    if (this in DEFAULT_DISPLAYED_SORTS) return null

    return when (this) {
        FoodSortOption.FIBER -> NutrientDisplayInfo(
            label = "Fibra", unit = "g", color = Color(0xFF8BC34A)
        ) { it.fibraAlimentar }

        FoodSortOption.CHOLESTEROL -> NutrientDisplayInfo(
            label = "Colesterol", unit = "mg", color = Color(0xFFE57373)
        ) { it.colesterol }

        FoodSortOption.SODIUM -> NutrientDisplayInfo(
            label = "Sódio", unit = "mg", color = Color(0xFF26A69A)
        ) { it.sodio }

        FoodSortOption.POTASSIUM -> NutrientDisplayInfo(
            label = "Potássio", unit = "mg", color = Color(0xFF42A5F5)
        ) { it.potassio }

        FoodSortOption.CALCIUM -> NutrientDisplayInfo(
            label = "Cálcio", unit = "mg", color = Color(0xFFECEFF1)
        ) { it.calcio }

        FoodSortOption.MAGNESIUM -> NutrientDisplayInfo(
            label = "Magnésio", unit = "mg", color = Color(0xFF78909C)
        ) { it.magnesio }

        FoodSortOption.PHOSPHORUS -> NutrientDisplayInfo(
            label = "Fósforo", unit = "mg", color = Color(0xFFFFB74D)
        ) { it.fosforo }

        FoodSortOption.IRON -> NutrientDisplayInfo(
            label = "Ferro", unit = "mg", color = Color(0xFFBF360C)
        ) { it.ferro }

        FoodSortOption.ZINC -> NutrientDisplayInfo(
            label = "Zinco", unit = "mg", color = Color(0xFF7E57C2)
        ) { it.zinco }

        FoodSortOption.COPPER -> NutrientDisplayInfo(
            label = "Cobre", unit = "mg", color = Color(0xFFD4A574)
        ) { it.cobre }

        FoodSortOption.MANGANESE -> NutrientDisplayInfo(
            label = "Manganês", unit = "mg", color = Color(0xFF9E9E9E)
        ) { it.manganes }

        FoodSortOption.VITAMIN_C -> NutrientDisplayInfo(
            label = "C", unit = "mg", color = Color(0xFFFFA726)
        ) { it.vitaminaC }

        FoodSortOption.RETINOL -> NutrientDisplayInfo(
            label = "A", unit = "mcg", color = Color(0xFFAB47BC)
        ) { it.retinol }

        FoodSortOption.THIAMINE -> NutrientDisplayInfo(
            label = "B1", unit = "mg", color = Color(0xFF66BB6A)
        ) { it.tiamina }

        FoodSortOption.RIBOFLAVIN -> NutrientDisplayInfo(
            label = "B2", unit = "mg", color = Color(0xFF29B6F6)
        ) { it.riboflavina }

        FoodSortOption.PYRIDOXINE -> NutrientDisplayInfo(
            label = "B6", unit = "mg", color = Color(0xFFEC407A)
        ) { it.piridoxina }

        FoodSortOption.NIACIN -> NutrientDisplayInfo(
            label = "B3", unit = "mg", color = Color(0xFF5C6BC0)
        ) { it.niacina }

        else -> null
    }
}

enum class FoodSource(val displayName: String) {
    ALL(displayName = "Todos"),
    TACO(displayName = "TACO"),
    CUSTOM(displayName = "Personalizados")
}

enum class FoodSortOption(val label: String, val category: SortCategory = SortCategory.GENERAL) {
    // General
    RELEVANCE("Relevância", SortCategory.GENERAL),
    NAME("Nome", SortCategory.GENERAL),

    // Macros
    CALORIES("Calorias", SortCategory.MACRO),
    PROTEIN("Proteínas", SortCategory.MACRO),
    CARBS("Carboidratos", SortCategory.MACRO),
    FAT("Gorduras", SortCategory.MACRO),
    FIBER("Fibra", SortCategory.MACRO),
    CHOLESTEROL("Colesterol", SortCategory.MACRO),

    // Minerals
    SODIUM("Sódio", SortCategory.MINERAL),
    POTASSIUM("Potássio", SortCategory.MINERAL),
    CALCIUM("Cálcio", SortCategory.MINERAL),
    MAGNESIUM("Magnésio", SortCategory.MINERAL),
    PHOSPHORUS("Fósforo", SortCategory.MINERAL),
    IRON("Ferro", SortCategory.MINERAL),
    ZINC("Zinco", SortCategory.MINERAL),
    COPPER("Cobre", SortCategory.MINERAL),
    MANGANESE("Manganês", SortCategory.MINERAL),

    // Vitamins
    VITAMIN_C("Vitamina C", SortCategory.VITAMIN),
    RETINOL("Vitamina A", SortCategory.VITAMIN),
    THIAMINE("Tiamina (B1)", SortCategory.VITAMIN),
    RIBOFLAVIN("Riboflavina (B2)", SortCategory.VITAMIN),
    PYRIDOXINE("Piridoxina (B6)", SortCategory.VITAMIN),
    NIACIN("Niacina (B3)", SortCategory.VITAMIN);

    val displayName: String get() = label
}

enum class SortCategory(val label: String) {
    GENERAL("Geral"),
    MACRO("Macronutrientes"),
    MINERAL("Minerais"),
    VITAMIN("Vitaminas")
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

    fun getFirstActiveAdvancedFilterInfo(): NutrientDisplayInfo? {
        return when {
            minFibra != null -> NutrientDisplayInfo(
                label = "Fibra", unit = "g", color = Color(0xFF8BC34A)
            ) { it.fibraAlimentar }

            minColesterol != null || maxColesterol != null -> NutrientDisplayInfo(
                label = "Colesterol", unit = "mg", color = Color(0xFFE57373)
            ) { it.colesterol }

            minSodio != null || maxSodio != null -> NutrientDisplayInfo(
                label = "Sódio", unit = "mg", color = Color(0xFF26A69A)
            ) { it.sodio }

            minPotassio != null -> NutrientDisplayInfo(
                label = "Potássio", unit = "mg", color = Color(0xFF42A5F5)
            ) { it.potassio }

            minCalcio != null -> NutrientDisplayInfo(
                label = "Cálcio", unit = "mg", color = Color(0xFFECEFF1)
            ) { it.calcio }

            minMagnesio != null -> NutrientDisplayInfo(
                label = "Magnésio", unit = "mg", color = Color(0xFF78909C)
            ) { it.magnesio }

            minFosforo != null -> NutrientDisplayInfo(
                label = "Fósforo", unit = "mg", color = Color(0xFFFFB74D)
            ) { it.fosforo }

            minFerro != null -> NutrientDisplayInfo(
                label = "Ferro", unit = "mg", color = Color(0xFFBF360C)
            ) { it.ferro }

            minZinco != null -> NutrientDisplayInfo(
                label = "Zinco", unit = "mg", color = Color(0xFF7E57C2)
            ) { it.zinco }

            minCobre != null -> NutrientDisplayInfo(
                label = "Cobre", unit = "mg", color = Color(0xFFD4A574)
            ) { it.cobre }

            minManganes != null -> NutrientDisplayInfo(
                label = "Manganês", unit = "mg", color = Color(0xFF9E9E9E)
            ) { it.manganes }

            minVitaminaC != null -> NutrientDisplayInfo(
                label = "Vit. C", unit = "mg", color = Color(0xFFFFA726)
            ) { it.vitaminaC }

            minRetinol != null -> NutrientDisplayInfo(
                label = "Retinol", unit = "µg", color = Color(0xFFAB47BC)
            ) { it.retinol }

            minTiamina != null -> NutrientDisplayInfo(
                label = "Tiamina", unit = "mg", color = Color(0xFF66BB6A)
            ) { it.tiamina }

            minRiboflavina != null -> NutrientDisplayInfo(
                label = "Riboflavina", unit = "mg", color = Color(0xFF29B6F6)
            ) { it.riboflavina }

            minPiridoxina != null -> NutrientDisplayInfo(
                label = "Piridoxina", unit = "mg", color = Color(0xFFEC407A)
            ) { it.piridoxina }

            minNiacina != null -> NutrientDisplayInfo(
                label = "Niacina", unit = "mg", color = Color(0xFF5C6BC0)
            ) { it.niacina }

            else -> null
        }
    }

    companion object {
        val DEFAULT = FoodFilterState()
    }
}