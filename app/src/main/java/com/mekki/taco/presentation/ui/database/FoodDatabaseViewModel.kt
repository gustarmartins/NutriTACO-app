package com.mekki.taco.presentation.ui.database

import android.content.Context
import androidx.core.content.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mekki.taco.data.db.dao.FoodDao
import com.mekki.taco.data.db.entity.Food
import com.mekki.taco.presentation.ui.search.FoodFilterState
import com.mekki.taco.presentation.ui.search.FoodSortOption
import com.mekki.taco.presentation.ui.search.FoodSource
import com.mekki.taco.utils.normalizeForSearch
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject


data class FoodDatabaseState(
    val isLoading: Boolean = true,
    val foods: List<Food> = emptyList(),
    val categories: List<String> = emptyList(),

    val searchQuery: String = "",
    val selectedCategories: Set<String> = emptySet(),
    val selectedSource: FoodSource = FoodSource.ALL,
    val sortOption: FoodSortOption = FoodSortOption.NAME,
    val filterState: FoodFilterState = FoodFilterState()
) {
    val hasActiveFilters: Boolean
        get() = searchQuery.isNotEmpty() ||
                selectedCategories.isNotEmpty() ||
                selectedSource != FoodSource.ALL ||
                sortOption != FoodSortOption.NAME ||
                filterState.hasAdvancedFilters
    val hasClearableFilters: Boolean
        get() = selectedCategories.isNotEmpty() ||
                selectedSource != FoodSource.ALL ||
                filterState.hasAdvancedFilters
}

@HiltViewModel
class FoodDatabaseViewModel @Inject constructor(
    private val foodDao: FoodDao,
    private val filterPreferences: FilterPreferences
) : ViewModel() {

    private val _searchQuery = MutableStateFlow(filterPreferences.searchQuery)
    private val _selectedCategories = MutableStateFlow(filterPreferences.selectedCategories)
    private val _selectedSource = MutableStateFlow(filterPreferences.selectedSource)
    private val _sortOption = MutableStateFlow(filterPreferences.sortOption)
    private val _advancedFilters = MutableStateFlow(FoodFilterState())

    private val _allFoods = foodDao.getAllFoods()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val categories = foodDao.getAllCategories()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private data class CombinedFilters(
        val query: String,
        val categories: Set<String>,
        val source: FoodSource,
        val sort: FoodSortOption,
        val advanced: FoodFilterState
    )

    val uiState: StateFlow<FoodDatabaseState> = combine(
        _allFoods,
        categories,
        combine(
            _searchQuery,
            _selectedCategories,
            _selectedSource,
            _sortOption,
            _advancedFilters
        ) { query, cats, source, sort, advanced ->
            CombinedFilters(query, cats, source, sort, advanced)
        }
    ) { foods, cats, filters ->
        val filtered = applyFilters(foods, filters)

        FoodDatabaseState(
            isLoading = false,
            foods = filtered,
            categories = cats,
            searchQuery = filters.query,
            selectedCategories = filters.categories,
            selectedSource = filters.source,
            sortOption = filters.sort,
            filterState = filters.advanced
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = FoodDatabaseState(
            searchQuery = filterPreferences.searchQuery,
            selectedCategories = filterPreferences.selectedCategories,
            selectedSource = filterPreferences.selectedSource,
            sortOption = filterPreferences.sortOption
        )
    )

    private fun applyFilters(foods: List<Food>, filters: CombinedFilters): List<Food> {
        val advanced = filters.advanced
        return foods.asSequence().filter { food ->
            when (filters.source) {
                FoodSource.TACO -> !food.isCustom
                FoodSource.CUSTOM -> food.isCustom
                FoodSource.ALL -> true
            }
        }.filter { food ->
            filters.categories.isEmpty() || food.category in filters.categories
        }.filter { food ->
            if (filters.query.isBlank()) true
            else {
                val normalizedQuery = filters.query.normalizeForSearch()
                val normalizedName = food.name.normalizeForSearch()
                normalizedName.contains(normalizedQuery)
            }
        }.filter { food ->
            advanced.minProtein?.let { if ((food.proteina ?: 0.0) < it) return@filter false }
            advanced.maxProtein?.let { if ((food.proteina ?: 0.0) > it) return@filter false }
            advanced.minCarbs?.let { if ((food.carboidratos ?: 0.0) < it) return@filter false }
            advanced.maxCarbs?.let { if ((food.carboidratos ?: 0.0) > it) return@filter false }
            advanced.minFat?.let { if ((food.lipidios?.total ?: 0.0) < it) return@filter false }
            advanced.maxFat?.let { if ((food.lipidios?.total ?: 0.0) > it) return@filter false }
            advanced.minCalories?.let { if ((food.energiaKcal ?: 0.0) < it) return@filter false }
            advanced.maxCalories?.let { if ((food.energiaKcal ?: 0.0) > it) return@filter false }
            advanced.minFibra?.let { if ((food.fibraAlimentar ?: 0.0) < it) return@filter false }
            advanced.maxFibra?.let { if ((food.fibraAlimentar ?: 0.0) > it) return@filter false }
            advanced.minColesterol?.let { if ((food.colesterol ?: 0.0) < it) return@filter false }
            advanced.maxColesterol?.let { if ((food.colesterol ?: 0.0) > it) return@filter false }

            // TODO: SUBTYPES of fat - add UI for these filters
            advanced.minSaturados?.let {
                if ((food.lipidios?.saturados ?: 0.0) < it) return@filter false
            }
            advanced.maxSaturados?.let {
                if ((food.lipidios?.saturados ?: 0.0) > it) return@filter false
            }
            advanced.minMonoinsaturados?.let {
                if ((food.lipidios?.monoinsaturados ?: 0.0) < it) return@filter false
            }
            advanced.maxMonoinsaturados?.let {
                if ((food.lipidios?.monoinsaturados ?: 0.0) > it) return@filter false
            }
            advanced.minPoliinsaturados?.let {
                if ((food.lipidios?.poliinsaturados ?: 0.0) < it) return@filter false
            }
            advanced.maxPoliinsaturados?.let {
                if ((food.lipidios?.poliinsaturados ?: 0.0) > it) return@filter false
            }

            advanced.minVitaminaC?.let { if ((food.vitaminaC ?: 0.0) < it) return@filter false }
            advanced.maxVitaminaC?.let { if ((food.vitaminaC ?: 0.0) > it) return@filter false }
            advanced.minRetinol?.let { if ((food.retinol ?: 0.0) < it) return@filter false }
            advanced.maxRetinol?.let { if ((food.retinol ?: 0.0) > it) return@filter false }
            advanced.minTiamina?.let { if ((food.tiamina ?: 0.0) < it) return@filter false }
            advanced.maxTiamina?.let { if ((food.tiamina ?: 0.0) > it) return@filter false }
            advanced.minRiboflavina?.let { if ((food.riboflavina ?: 0.0) < it) return@filter false }
            advanced.maxRiboflavina?.let { if ((food.riboflavina ?: 0.0) > it) return@filter false }
            advanced.minPiridoxina?.let { if ((food.piridoxina ?: 0.0) < it) return@filter false }
            advanced.maxPiridoxina?.let { if ((food.piridoxina ?: 0.0) > it) return@filter false }
            advanced.minNiacina?.let { if ((food.niacina ?: 0.0) < it) return@filter false }
            advanced.maxNiacina?.let { if ((food.niacina ?: 0.0) > it) return@filter false }

            advanced.minCalcio?.let { if ((food.calcio ?: 0.0) < it) return@filter false }
            advanced.maxCalcio?.let { if ((food.calcio ?: 0.0) > it) return@filter false }
            advanced.minFerro?.let { if ((food.ferro ?: 0.0) < it) return@filter false }
            advanced.maxFerro?.let { if ((food.ferro ?: 0.0) > it) return@filter false }
            advanced.minSodio?.let { if ((food.sodio ?: 0.0) < it) return@filter false }
            advanced.maxSodio?.let { if ((food.sodio ?: 0.0) > it) return@filter false }
            advanced.minPotassio?.let { if ((food.potassio ?: 0.0) < it) return@filter false }
            advanced.maxPotassio?.let { if ((food.potassio ?: 0.0) > it) return@filter false }
            advanced.minMagnesio?.let { if ((food.magnesio ?: 0.0) < it) return@filter false }
            advanced.maxMagnesio?.let { if ((food.magnesio ?: 0.0) > it) return@filter false }
            advanced.minZinco?.let { if ((food.zinco ?: 0.0) < it) return@filter false }
            advanced.maxZinco?.let { if ((food.zinco ?: 0.0) > it) return@filter false }
            advanced.minCobre?.let { if ((food.cobre ?: 0.0) < it) return@filter false }
            advanced.maxCobre?.let { if ((food.cobre ?: 0.0) > it) return@filter false }
            advanced.minFosforo?.let { if ((food.fosforo ?: 0.0) < it) return@filter false }
            advanced.maxFosforo?.let { if ((food.fosforo ?: 0.0) > it) return@filter false }
            advanced.minManganes?.let { if ((food.manganes ?: 0.0) < it) return@filter false }
            advanced.maxManganes?.let { if ((food.manganes ?: 0.0) > it) return@filter false }

            advanced.minUmidade?.let { if ((food.umidade ?: 0.0) < it) return@filter false }
            advanced.maxUmidade?.let { if ((food.umidade ?: 0.0) > it) return@filter false }
            advanced.minCinzas?.let { if ((food.cinzas ?: 0.0) < it) return@filter false }
            advanced.maxCinzas?.let { if ((food.cinzas ?: 0.0) > it) return@filter false }

            // TODO: Amino acid filters - add UI implementation (most foods don't have this data)
            advanced.minTriptofano?.let {
                if ((food.aminoacidos?.triptofano ?: 0.0) < it) return@filter false
            }
            advanced.maxTriptofano?.let {
                if ((food.aminoacidos?.triptofano ?: 0.0) > it) return@filter false
            }
            advanced.minTreonina?.let {
                if ((food.aminoacidos?.treonina ?: 0.0) < it) return@filter false
            }
            advanced.maxTreonina?.let {
                if ((food.aminoacidos?.treonina ?: 0.0) > it) return@filter false
            }
            advanced.minIsoleucina?.let {
                if ((food.aminoacidos?.isoleucina ?: 0.0) < it) return@filter false
            }
            advanced.maxIsoleucina?.let {
                if ((food.aminoacidos?.isoleucina ?: 0.0) > it) return@filter false
            }
            advanced.minLeucina?.let {
                if ((food.aminoacidos?.leucina ?: 0.0) < it) return@filter false
            }
            advanced.maxLeucina?.let {
                if ((food.aminoacidos?.leucina ?: 0.0) > it) return@filter false
            }
            advanced.minLisina?.let {
                if ((food.aminoacidos?.lisina ?: 0.0) < it) return@filter false
            }
            advanced.maxLisina?.let {
                if ((food.aminoacidos?.lisina ?: 0.0) > it) return@filter false
            }
            advanced.minMetionina?.let {
                if ((food.aminoacidos?.metionina ?: 0.0) < it) return@filter false
            }
            advanced.maxMetionina?.let {
                if ((food.aminoacidos?.metionina ?: 0.0) > it) return@filter false
            }
            advanced.minCistina?.let {
                if ((food.aminoacidos?.cistina ?: 0.0) < it) return@filter false
            }
            advanced.maxCistina?.let {
                if ((food.aminoacidos?.cistina ?: 0.0) > it) return@filter false
            }
            advanced.minFenilalanina?.let {
                if ((food.aminoacidos?.fenilalanina ?: 0.0) < it) return@filter false
            }
            advanced.maxFenilalanina?.let {
                if ((food.aminoacidos?.fenilalanina ?: 0.0) > it) return@filter false
            }
            advanced.minTirosina?.let {
                if ((food.aminoacidos?.tirosina ?: 0.0) < it) return@filter false
            }
            advanced.maxTirosina?.let {
                if ((food.aminoacidos?.tirosina ?: 0.0) > it) return@filter false
            }
            advanced.minValina?.let {
                if ((food.aminoacidos?.valina ?: 0.0) < it) return@filter false
            }
            advanced.maxValina?.let {
                if ((food.aminoacidos?.valina ?: 0.0) > it) return@filter false
            }
            advanced.minArginina?.let {
                if ((food.aminoacidos?.arginina ?: 0.0) < it) return@filter false
            }
            advanced.maxArginina?.let {
                if ((food.aminoacidos?.arginina ?: 0.0) > it) return@filter false
            }
            advanced.minHistidina?.let {
                if ((food.aminoacidos?.histidina ?: 0.0) < it) return@filter false
            }
            advanced.maxHistidina?.let {
                if ((food.aminoacidos?.histidina ?: 0.0) > it) return@filter false
            }
            advanced.minAlanina?.let {
                if ((food.aminoacidos?.alanina ?: 0.0) < it) return@filter false
            }
            advanced.maxAlanina?.let {
                if ((food.aminoacidos?.alanina ?: 0.0) > it) return@filter false
            }
            advanced.minAcidoAspartico?.let {
                if ((food.aminoacidos?.acidoAspartico ?: 0.0) < it) return@filter false
            }
            advanced.maxAcidoAspartico?.let {
                if ((food.aminoacidos?.acidoAspartico ?: 0.0) > it) return@filter false
            }
            advanced.minAcidoGlutamico?.let {
                if ((food.aminoacidos?.acidoGlutamico ?: 0.0) < it) return@filter false
            }
            advanced.maxAcidoGlutamico?.let {
                if ((food.aminoacidos?.acidoGlutamico ?: 0.0) > it) return@filter false
            }
            advanced.minGlicina?.let {
                if ((food.aminoacidos?.glicina ?: 0.0) < it) return@filter false
            }
            advanced.maxGlicina?.let {
                if ((food.aminoacidos?.glicina ?: 0.0) > it) return@filter false
            }
            advanced.minProlina?.let {
                if ((food.aminoacidos?.prolina ?: 0.0) < it) return@filter false
            }
            advanced.maxProlina?.let {
                if ((food.aminoacidos?.prolina ?: 0.0) > it) return@filter false
            }
            advanced.minSerina?.let {
                if ((food.aminoacidos?.serina ?: 0.0) < it) return@filter false
            }
            advanced.maxSerina?.let {
                if ((food.aminoacidos?.serina ?: 0.0) > it) return@filter false
            }

            true
        }.sortedWith(
            when (filters.sort) {
                FoodSortOption.RELEVANCE -> compareByDescending<Food> {
                    it.usageCount * 2 + if (it.source == "CUSTOM" || (it.source == null && it.isCustom)) 5 else 0
                }

                FoodSortOption.NAME -> compareBy { it.name }
                FoodSortOption.CALORIES -> compareByDescending { it.energiaKcal ?: 0.0 }
                FoodSortOption.PROTEIN -> compareByDescending { it.proteina ?: 0.0 }
                FoodSortOption.CARBS -> compareByDescending { it.carboidratos ?: 0.0 }
                FoodSortOption.FAT -> compareByDescending { it.lipidios?.total ?: 0.0 }
                FoodSortOption.FIBER -> compareByDescending { it.fibraAlimentar ?: 0.0 }
                FoodSortOption.CHOLESTEROL -> compareByDescending { it.colesterol ?: 0.0 }
                FoodSortOption.SODIUM -> compareByDescending { it.sodio ?: 0.0 }
                FoodSortOption.POTASSIUM -> compareByDescending { it.potassio ?: 0.0 }
                FoodSortOption.CALCIUM -> compareByDescending { it.calcio ?: 0.0 }
                FoodSortOption.MAGNESIUM -> compareByDescending { it.magnesio ?: 0.0 }
                FoodSortOption.PHOSPHORUS -> compareByDescending { it.fosforo ?: 0.0 }
                FoodSortOption.IRON -> compareByDescending { it.ferro ?: 0.0 }
                FoodSortOption.ZINC -> compareByDescending { it.zinco ?: 0.0 }
                FoodSortOption.COPPER -> compareByDescending { it.cobre ?: 0.0 }
                FoodSortOption.MANGANESE -> compareByDescending { it.manganes ?: 0.0 }
                FoodSortOption.VITAMIN_C -> compareByDescending { it.vitaminaC ?: 0.0 }
                FoodSortOption.RETINOL -> compareByDescending { it.retinol ?: 0.0 }
                FoodSortOption.THIAMINE -> compareByDescending { it.tiamina ?: 0.0 }
                FoodSortOption.RIBOFLAVIN -> compareByDescending { it.riboflavina ?: 0.0 }
                FoodSortOption.PYRIDOXINE -> compareByDescending { it.piridoxina ?: 0.0 }
                FoodSortOption.NIACIN -> compareByDescending { it.niacina ?: 0.0 }
            }
        ).toList()
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
        filterPreferences.searchQuery = query
    }

    fun onCategoryToggle(category: String) {
        val currentCategories = _selectedCategories.value.toMutableSet()
        if (category in currentCategories) {
            currentCategories.remove(category)
        } else {
            currentCategories.add(category)
        }
        _selectedCategories.value = currentCategories
        filterPreferences.selectedCategories = currentCategories
    }

    fun onCategoryRemove(category: String) {
        val currentCategories = _selectedCategories.value.toMutableSet()
        currentCategories.remove(category)
        _selectedCategories.value = currentCategories
        filterPreferences.selectedCategories = currentCategories
    }

    fun onClearCategories() {
        _selectedCategories.value = emptySet()
        filterPreferences.selectedCategories = emptySet()
    }

    fun onSourceChange(source: FoodSource) {
        _selectedSource.value = source
        filterPreferences.selectedSource = source
    }

    fun onSortChange(sort: FoodSortOption) {
        _sortOption.value = sort
        filterPreferences.sortOption = sort
    }

    fun onFilterStateChange(newState: FoodFilterState) {
        _advancedFilters.value = newState
    }

    /**
     * Clears only the applied filters (source, categories, advanced)
     * Preserves: search query, sort option
     */
    fun onClearFilters() {
        _selectedCategories.value = emptySet()
        _selectedSource.value = FoodSource.ALL
        _advancedFilters.value = FoodFilterState()

        filterPreferences.clearFiltersOnly()
    }

    /**
     * Reset ALL filters to default values (including search and sort)
     */
    fun onResetFilters() {
        _searchQuery.value = ""
        _selectedCategories.value = emptySet()
        _selectedSource.value = FoodSource.ALL
        _sortOption.value = FoodSortOption.NAME
        _advancedFilters.value = FoodFilterState()

        filterPreferences.clear()
    }
}

/**
 * Handles persistence of filter preferences using SharedPreferences
 */
class FilterPreferences @Inject constructor(@ApplicationContext context: Context) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    var searchQuery: String
        get() = prefs.getString(KEY_SEARCH_QUERY, "") ?: ""
        set(value) = prefs.edit { putString(KEY_SEARCH_QUERY, value) }

    var selectedCategories: Set<String>
        get() = prefs.getStringSet(KEY_CATEGORIES, emptySet()) ?: emptySet()
        set(value) = prefs.edit { putStringSet(KEY_CATEGORIES, value) }

    var selectedSource: FoodSource
        get() {
            val name = prefs.getString(KEY_SOURCE, FoodSource.ALL.name) ?: FoodSource.ALL.name
            return try {
                FoodSource.valueOf(name)
            } catch (e: IllegalArgumentException) {
                FoodSource.ALL
            }
        }
        set(value) = prefs.edit { putString(KEY_SOURCE, value.name) }

    var sortOption: FoodSortOption
        get() {
            val name =
                prefs.getString(KEY_SORT, FoodSortOption.NAME.name) ?: FoodSortOption.NAME.name
            return try {
                FoodSortOption.valueOf(name)
            } catch (e: IllegalArgumentException) {
                FoodSortOption.NAME
            }
        }
        set(value) = prefs.edit { putString(KEY_SORT, value.name) }

    /**
     * Clears only filter preferences (source, categories)
     * Preserves: search query, sort option
     */
    fun clearFiltersOnly() {
        prefs.edit {
            remove(KEY_CATEGORIES)
            remove(KEY_SOURCE)
        }
    }

    /**
     * Clears all preferences
     */
    fun clear() {
        prefs.edit {
            remove(KEY_SEARCH_QUERY)
            remove(KEY_CATEGORIES)
            remove(KEY_SOURCE)
            remove(KEY_SORT)
        }
    }

    companion object {
        private const val PREFS_NAME = "food_database_filters"
        private const val KEY_SEARCH_QUERY = "search_query"
        private const val KEY_CATEGORIES = "selected_categories"
        private const val KEY_SOURCE = "selected_source"
        private const val KEY_SORT = "sort_option"
    }
}