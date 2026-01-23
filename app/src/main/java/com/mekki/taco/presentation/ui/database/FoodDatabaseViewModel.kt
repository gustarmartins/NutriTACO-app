package com.mekki.taco.presentation.ui.database

import android.content.Context
import androidx.core.content.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.mekki.taco.data.db.dao.FoodDao
import com.mekki.taco.data.db.entity.Food
import com.mekki.taco.utils.normalizeForSearch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

enum class FoodSource(val displayName: String) {
    ALL(displayName = "TODOS"),
    TACO(displayName = "TACO"),
    CUSTOM(displayName = "PERSONALIZADOS")
}

enum class SortOption(val displayName: String) {
    NAME("Nome"),
    CALORIES("Calorias"),
    PROTEIN("Proteína"),
    CARBS("Carboidratos"),
    FAT("Gorduras")
}

data class FoodDatabaseState(
    val isLoading: Boolean = true,
    val foods: List<Food> = emptyList(),
    val categories: List<String> = emptyList(),

    // Filters
    val searchQuery: String = "",
    val selectedCategories: Set<String> = emptySet(),
    val selectedSource: FoodSource = FoodSource.ALL,
    val sortOption: SortOption = SortOption.NAME
) {
    // checks if any no default filters are active
    val hasActiveFilters: Boolean
        get() = searchQuery.isNotEmpty() ||
                selectedCategories.isNotEmpty() ||
                selectedSource != FoodSource.ALL ||
                sortOption != SortOption.NAME
}

class FoodDatabaseViewModel(
    private val foodDao: FoodDao,
    private val filterPreferences: FilterPreferences
) : ViewModel() {

    private val _searchQuery = MutableStateFlow(filterPreferences.searchQuery)
    private val _selectedCategories = MutableStateFlow(filterPreferences.selectedCategories)
    private val _selectedSource = MutableStateFlow(filterPreferences.selectedSource)
    private val _sortOption = MutableStateFlow(filterPreferences.sortOption)

    private val _allFoods = foodDao.getAllFoods()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val categories = foodDao.getAllCategories()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private data class FilterState(
        val query: String,
        val categories: Set<String>,
        val source: FoodSource,
        val sort: SortOption
    )

    val uiState: StateFlow<FoodDatabaseState> = combine(
        _allFoods,
        categories,
        combine(
            _searchQuery,
            _selectedCategories,
            _selectedSource,
            _sortOption
        ) { query, categories, source, sort ->
            FilterState(query, categories, source, sort)
        }
    ) { foods, cats, filters ->

        val filtered = foods.asSequence().filter { food ->
            // 1. Source Filter
            when (filters.source) {
                FoodSource.TACO -> !food.isCustom
                FoodSource.CUSTOM -> food.isCustom
                FoodSource.ALL -> true
            }
        }.filter { food ->
            // 2. Category Filter
            filters.categories.isEmpty() || food.category in filters.categories
        }.filter { food ->
            // 3. Search Query
            if (filters.query.isBlank()) true
            else {
                val normalizedQuery = filters.query.normalizeForSearch()
                val normalizedName = food.name.normalizeForSearch()
                normalizedName.contains(normalizedQuery)
            }
        }.sortedWith(
            // 4. Sorting
            when (filters.sort) {
                SortOption.NAME -> compareBy { it.name }
                SortOption.CALORIES -> compareByDescending { it.energiaKcal }
                SortOption.PROTEIN -> compareByDescending { it.proteina }
                SortOption.CARBS -> compareByDescending { it.carboidratos }
                SortOption.FAT -> compareByDescending { it.lipidios?.total }
            }
        ).toList()

        FoodDatabaseState(
            isLoading = false,
            foods = filtered,
            categories = cats,
            searchQuery = filters.query,
            selectedCategories = filters.categories,
            selectedSource = filters.source,
            sortOption = filters.sort
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

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
        filterPreferences.searchQuery = query
    }

    // Toggle category selection
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

    // Remove a specific category from selection
    fun onCategoryRemove(category: String) {
        val currentCategories = _selectedCategories.value.toMutableSet()
        currentCategories.remove(category)
        _selectedCategories.value = currentCategories
        filterPreferences.selectedCategories = currentCategories
    }

    // Clear all category selections
    fun onClearCategories() {
        _selectedCategories.value = emptySet()
        filterPreferences.selectedCategories = emptySet()
    }

    fun onSourceChange(source: FoodSource) {
        _selectedSource.value = source
        filterPreferences.selectedSource = source
    }

    fun onSortChange(sort: SortOption) {
        _sortOption.value = sort
        filterPreferences.sortOption = sort
    }

    // Reset all filters to default values
    fun onResetFilters() {
        _searchQuery.value = ""
        _selectedCategories.value = emptySet()
        _selectedSource.value = FoodSource.ALL
        _sortOption.value = SortOption.NAME

        filterPreferences.clear()
    }
}

/**
 * Handles persistence of filter preferences using SharedPreferences
 */
class FilterPreferences(context: Context) {
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

    var sortOption: SortOption
        get() {
            val name = prefs.getString(KEY_SORT, SortOption.NAME.name) ?: SortOption.NAME.name
            return try {
                SortOption.valueOf(name)
            } catch (e: IllegalArgumentException) {
                SortOption.NAME
            }
        }
        set(value) = prefs.edit { putString(KEY_SORT, value.name) }

    fun clear() {
        prefs.edit {
            remove(KEY_SEARCH_QUERY)
                .remove(KEY_CATEGORIES)
                .remove(KEY_SOURCE)
                .remove(KEY_SORT)
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

class FoodDatabaseViewModelFactory(
    private val foodDao: FoodDao,
    private val filterPreferences: FilterPreferences
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FoodDatabaseViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return FoodDatabaseViewModel(foodDao, filterPreferences) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}