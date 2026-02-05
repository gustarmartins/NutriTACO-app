package com.mekki.taco.presentation.ui.search

import android.os.Parcelable
import androidx.compose.ui.graphics.Color
import com.mekki.taco.utils.NutrientesPorPorcao
import kotlinx.parcelize.Parcelize

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

@Parcelize
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
    val minFibra: Double? = null,
    val maxFibra: Double? = null,
    val minColesterol: Double? = null,
    val maxColesterol: Double? = null,

    val minSaturados: Double? = null,
    val maxSaturados: Double? = null,
    val minMonoinsaturados: Double? = null,
    val maxMonoinsaturados: Double? = null,
    val minPoliinsaturados: Double? = null,
    val maxPoliinsaturados: Double? = null,

    val minCalcio: Double? = null,
    val maxCalcio: Double? = null,
    val minFerro: Double? = null,
    val maxFerro: Double? = null,
    val minSodio: Double? = null,
    val maxSodio: Double? = null,
    val minPotassio: Double? = null,
    val maxPotassio: Double? = null,
    val minMagnesio: Double? = null,
    val maxMagnesio: Double? = null,
    val minZinco: Double? = null,
    val maxZinco: Double? = null,
    val minCobre: Double? = null,
    val maxCobre: Double? = null,
    val minFosforo: Double? = null,
    val maxFosforo: Double? = null,
    val minManganes: Double? = null,
    val maxManganes: Double? = null,

    val minVitaminaC: Double? = null,
    val maxVitaminaC: Double? = null,
    val minRetinol: Double? = null,
    val maxRetinol: Double? = null,
    val minTiamina: Double? = null,
    val maxTiamina: Double? = null,
    val minRiboflavina: Double? = null,
    val maxRiboflavina: Double? = null,
    val minPiridoxina: Double? = null,
    val maxPiridoxina: Double? = null,
    val minNiacina: Double? = null,
    val maxNiacina: Double? = null,

    val minUmidade: Double? = null,
    val maxUmidade: Double? = null,
    val minCinzas: Double? = null,
    val maxCinzas: Double? = null,

    val minTriptofano: Double? = null,
    val maxTriptofano: Double? = null,
    val minTreonina: Double? = null,
    val maxTreonina: Double? = null,
    val minIsoleucina: Double? = null,
    val maxIsoleucina: Double? = null,
    val minLeucina: Double? = null,
    val maxLeucina: Double? = null,
    val minLisina: Double? = null,
    val maxLisina: Double? = null,
    val minMetionina: Double? = null,
    val maxMetionina: Double? = null,
    val minCistina: Double? = null,
    val maxCistina: Double? = null,
    val minFenilalanina: Double? = null,
    val maxFenilalanina: Double? = null,
    val minTirosina: Double? = null,
    val maxTirosina: Double? = null,
    val minValina: Double? = null,
    val maxValina: Double? = null,
    val minArginina: Double? = null,
    val maxArginina: Double? = null,
    val minHistidina: Double? = null,
    val maxHistidina: Double? = null,
    val minAlanina: Double? = null,
    val maxAlanina: Double? = null,
    val minAcidoAspartico: Double? = null,
    val maxAcidoAspartico: Double? = null,
    val minAcidoGlutamico: Double? = null,
    val maxAcidoGlutamico: Double? = null,
    val minGlicina: Double? = null,
    val maxGlicina: Double? = null,
    val minProlina: Double? = null,
    val maxProlina: Double? = null,
    val minSerina: Double? = null,
    val maxSerina: Double? = null,

    // compatibility
    val minAminoAcidTotal: Double? = null
) : Parcelable {
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
                minCalories != null || maxCalories != null ||
                minFibra != null || maxFibra != null ||
                minColesterol != null || maxColesterol != null ||
                minSaturados != null || maxSaturados != null ||
                minMonoinsaturados != null || maxMonoinsaturados != null ||
                minPoliinsaturados != null || maxPoliinsaturados != null

    val hasMineralFilters: Boolean
        get() = minCalcio != null || maxCalcio != null ||
                minFerro != null || maxFerro != null ||
                minSodio != null || maxSodio != null ||
                minPotassio != null || maxPotassio != null ||
                minMagnesio != null || maxMagnesio != null ||
                minZinco != null || maxZinco != null ||
                minCobre != null || maxCobre != null ||
                minFosforo != null || maxFosforo != null ||
                minManganes != null || maxManganes != null

    val hasVitaminFilters: Boolean
        get() = minVitaminaC != null || maxVitaminaC != null ||
                minRetinol != null || maxRetinol != null ||
                minTiamina != null || maxTiamina != null ||
                minRiboflavina != null || maxRiboflavina != null ||
                minPiridoxina != null || maxPiridoxina != null ||
                minNiacina != null || maxNiacina != null

    val hasOtherFilters: Boolean
        get() = minUmidade != null || maxUmidade != null ||
                minCinzas != null || maxCinzas != null

    val hasAminoAcidFilters: Boolean
        get() = minTriptofano != null || maxTriptofano != null ||
                minTreonina != null || maxTreonina != null ||
                minIsoleucina != null || maxIsoleucina != null ||
                minLeucina != null || maxLeucina != null ||
                minLisina != null || maxLisina != null ||
                minMetionina != null || maxMetionina != null ||
                minCistina != null || maxCistina != null ||
                minFenilalanina != null || maxFenilalanina != null ||
                minTirosina != null || maxTirosina != null ||
                minValina != null || maxValina != null ||
                minArginina != null || maxArginina != null ||
                minHistidina != null || maxHistidina != null ||
                minAlanina != null || maxAlanina != null ||
                minAcidoAspartico != null || maxAcidoAspartico != null ||
                minAcidoGlutamico != null || maxAcidoGlutamico != null ||
                minGlicina != null || maxGlicina != null ||
                minProlina != null || maxProlina != null ||
                minSerina != null || maxSerina != null ||
                minAminoAcidTotal != null

    @Deprecated("Use hasMineralFilters instead", ReplaceWith("hasMineralFilters"))
    val hasMicroFilters: Boolean
        get() = hasMineralFilters || hasVitaminFilters

    val hasAdvancedFilters: Boolean
        get() = hasMacroFilters || hasMineralFilters || hasVitaminFilters ||
                hasOtherFilters || hasAminoAcidFilters

    val activeAdvancedFilterCount: Int
        get() = listOfNotNull(
            // Macro
            minProtein, maxProtein, minCarbs, maxCarbs,
            minFat, maxFat, minCalories, maxCalories,
            minFibra, maxFibra, minColesterol, maxColesterol,
            minSaturados, maxSaturados, minMonoinsaturados, maxMonoinsaturados,
            minPoliinsaturados, maxPoliinsaturados,
            // Mineral
            minCalcio, maxCalcio, minFerro, maxFerro,
            minSodio, maxSodio, minPotassio, maxPotassio,
            minMagnesio, maxMagnesio, minZinco, maxZinco,
            minCobre, maxCobre, minFosforo, maxFosforo,
            minManganes, maxManganes,
            // Vitamina
            minVitaminaC, maxVitaminaC, minRetinol, maxRetinol,
            minTiamina, maxTiamina, minRiboflavina, maxRiboflavina,
            minPiridoxina, maxPiridoxina, minNiacina, maxNiacina,
            // misc
            minUmidade, maxUmidade, minCinzas, maxCinzas,
            // Amino acids
            minTriptofano, maxTriptofano, minTreonina, maxTreonina,
            minIsoleucina, maxIsoleucina, minLeucina, maxLeucina,
            minLisina, maxLisina, minMetionina, maxMetionina,
            minCistina, maxCistina, minFenilalanina, maxFenilalanina,
            minTirosina, maxTirosina, minValina, maxValina,
            minArginina, maxArginina, minHistidina, maxHistidina,
            minAlanina, maxAlanina, minAcidoAspartico, maxAcidoAspartico,
            minAcidoGlutamico, maxAcidoGlutamico, minGlicina, maxGlicina,
            minProlina, maxProlina, minSerina, maxSerina,
            minAminoAcidTotal
        ).size

    fun getFirstActiveAdvancedFilterInfo(): NutrientDisplayInfo? {
        return when {
            minFibra != null || maxFibra != null -> NutrientDisplayInfo(
                label = "Fibra", unit = "g", color = Color(0xFF8BC34A)
            ) { it.fibraAlimentar }

            minColesterol != null || maxColesterol != null -> NutrientDisplayInfo(
                label = "Colesterol", unit = "mg", color = Color(0xFFE57373)
            ) { it.colesterol }

            minSaturados != null || maxSaturados != null -> NutrientDisplayInfo(
                label = "G. Saturadas", unit = "g", color = Color(0xFFFF8A65)
            ) { it.lipidios?.saturados }

            minMonoinsaturados != null || maxMonoinsaturados != null -> NutrientDisplayInfo(
                label = "G. Mono", unit = "g", color = Color(0xFFFFCC80)
            ) { it.lipidios?.monoinsaturados }

            minPoliinsaturados != null || maxPoliinsaturados != null -> NutrientDisplayInfo(
                label = "G. Poli", unit = "g", color = Color(0xFFFFE082)
            ) { it.lipidios?.poliinsaturados }

            minSodio != null || maxSodio != null -> NutrientDisplayInfo(
                label = "Sódio", unit = "mg", color = Color(0xFF26A69A)
            ) { it.sodio }

            minPotassio != null || maxPotassio != null -> NutrientDisplayInfo(
                label = "Potássio", unit = "mg", color = Color(0xFF42A5F5)
            ) { it.potassio }

            minCalcio != null || maxCalcio != null -> NutrientDisplayInfo(
                label = "Cálcio", unit = "mg", color = Color(0xFFECEFF1)
            ) { it.calcio }

            minMagnesio != null || maxMagnesio != null -> NutrientDisplayInfo(
                label = "Magnésio", unit = "mg", color = Color(0xFF78909C)
            ) { it.magnesio }

            minFosforo != null || maxFosforo != null -> NutrientDisplayInfo(
                label = "Fósforo", unit = "mg", color = Color(0xFFFFB74D)
            ) { it.fosforo }

            minFerro != null || maxFerro != null -> NutrientDisplayInfo(
                label = "Ferro", unit = "mg", color = Color(0xFFBF360C)
            ) { it.ferro }

            minZinco != null || maxZinco != null -> NutrientDisplayInfo(
                label = "Zinco", unit = "mg", color = Color(0xFF7E57C2)
            ) { it.zinco }

            minCobre != null || maxCobre != null -> NutrientDisplayInfo(
                label = "Cobre", unit = "mg", color = Color(0xFFD4A574)
            ) { it.cobre }

            minManganes != null || maxManganes != null -> NutrientDisplayInfo(
                label = "Manganês", unit = "mg", color = Color(0xFF9E9E9E)
            ) { it.manganes }

            minVitaminaC != null || maxVitaminaC != null -> NutrientDisplayInfo(
                label = "Vit. C", unit = "mg", color = Color(0xFFFFA726)
            ) { it.vitaminaC }

            minRetinol != null || maxRetinol != null -> NutrientDisplayInfo(
                label = "Retinol", unit = "µg", color = Color(0xFFAB47BC)
            ) { it.retinol }

            minTiamina != null || maxTiamina != null -> NutrientDisplayInfo(
                label = "Tiamina", unit = "mg", color = Color(0xFF66BB6A)
            ) { it.tiamina }

            minRiboflavina != null || maxRiboflavina != null -> NutrientDisplayInfo(
                label = "Riboflavina", unit = "mg", color = Color(0xFF29B6F6)
            ) { it.riboflavina }

            minPiridoxina != null || maxPiridoxina != null -> NutrientDisplayInfo(
                label = "Piridoxina", unit = "mg", color = Color(0xFFEC407A)
            ) { it.piridoxina }

            minNiacina != null || maxNiacina != null -> NutrientDisplayInfo(
                label = "Niacina", unit = "mg", color = Color(0xFF5C6BC0)
            ) { it.niacina }

            minUmidade != null || maxUmidade != null -> NutrientDisplayInfo(
                label = "Umidade", unit = "%", color = Color(0xFF4FC3F7)
            ) { it.umidade }

            minCinzas != null || maxCinzas != null -> NutrientDisplayInfo(
                label = "Cinzas", unit = "g", color = Color(0xFF90A4AE)
            ) { it.cinzas }

            minTriptofano != null || maxTriptofano != null -> NutrientDisplayInfo(
                label = "Triptofano", unit = "g", color = Color(0xFF81C784)
            ) { it.aminoacidos?.triptofano }

            minLeucina != null || maxLeucina != null -> NutrientDisplayInfo(
                label = "Leucina", unit = "g", color = Color(0xFF4DB6AC)
            ) { it.aminoacidos?.leucina }

            hasAminoAcidFilters -> NutrientDisplayInfo(
                label = "Aminoácidos", unit = "g", color = Color(0xFF81C784)
            ) { null }

            else -> null
        }
    }

    companion object {
        val DEFAULT = FoodFilterState()
    }
}