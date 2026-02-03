package com.mekki.taco.presentation.ui.search

import androidx.lifecycle.SavedStateHandle
import com.mekki.taco.data.db.dao.FoodDao
import com.mekki.taco.data.db.entity.Food
import com.mekki.taco.utils.normalizeForSearch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn


data class FoodSearchState(
    val searchTerm: String = "",
    val isLoading: Boolean = false,
    val results: List<Food> = emptyList(),
    val expandedFoodId: Int? = null,
    val quickAddAmount: String = "100",
    val sortOption: FoodSortOption = FoodSortOption.RELEVANCE,
    val filterState: FoodFilterState = FoodFilterState.DEFAULT
)

private const val KEY_SM_TERM = "sm_search_term"
private const val KEY_SM_QUICK_ADD = "sm_quick_add"
private const val KEY_SM_SORT = "sm_sort_option"
private const val KEY_SM_EXPANDED = "sm_expanded_id"
private const val KEY_SM_SOURCE = "sm_source"

/**
 * Reusable food search manager with FTS support, synonyms, and sorting.
 */
@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
class FoodSearchManager(
    private val foodDao: FoodDao,
    private val scope: CoroutineScope,
    private val savedStateHandle: SavedStateHandle? = null
) {
    private val _localSearchTerm = MutableStateFlow("")
    private val _localQuickAdd = MutableStateFlow("100")
    private val _localExpandedId = MutableStateFlow<Int?>(null)

    private val searchTermFlow =
        savedStateHandle?.getStateFlow(KEY_SM_TERM, "") ?: _localSearchTerm.asStateFlow()
    private val quickAddFlow =
        savedStateHandle?.getStateFlow(KEY_SM_QUICK_ADD, "100") ?: _localQuickAdd.asStateFlow()
    private val expandedIdFlow = savedStateHandle?.getStateFlow<Int?>(KEY_SM_EXPANDED, null)
        ?: _localExpandedId.asStateFlow()

    private val _localSourceFilter = MutableStateFlow(FoodSource.ALL)
    private val sourceFilterFlow = savedStateHandle?.getStateFlow(KEY_SM_SOURCE, FoodSource.ALL)
        ?: _localSourceFilter.asStateFlow()

    private val _filterState = MutableStateFlow(FoodFilterState.DEFAULT)

    val categories: StateFlow<List<String>> = foodDao.getAllCategories()
        .stateIn(scope, kotlinx.coroutines.flow.SharingStarted.Lazily, emptyList())

    private val _isLoading = MutableStateFlow(false)
    private val _rawResults = MutableStateFlow<List<Food>>(emptyList())

    val state: StateFlow<FoodSearchState> = combine(
        listOf(
            searchTermFlow,
            _isLoading,
            _rawResults,
            expandedIdFlow,
            quickAddFlow,
            _filterState
        )
    ) { args ->
        val term = args[0] as String
        val loading = args[1] as Boolean
        val raw = args[2] as List<Food>
        val expanded = args[3] as Int?
        val quickAdd = args[4] as String
        val filters = args[5] as FoodFilterState

        val filtered = applyFilters(raw, filters)

        val sorted = when (filters.sortOption) {
            FoodSortOption.RELEVANCE -> filtered
            FoodSortOption.NAME -> filtered.sortedBy { it.name }
            FoodSortOption.PROTEIN -> filtered.sortedByDescending { it.proteina ?: 0.0 }
            FoodSortOption.CARBS -> filtered.sortedByDescending { it.carboidratos ?: 0.0 }
            FoodSortOption.FAT -> filtered.sortedByDescending { it.lipidios?.total ?: 0.0 }
            FoodSortOption.CALORIES -> filtered.sortedByDescending { it.energiaKcal ?: 0.0 }
        }
        FoodSearchState(
            searchTerm = term,
            isLoading = loading,
            results = sorted,
            expandedFoodId = expanded,
            quickAddAmount = quickAdd,
            sortOption = filters.sortOption,
            filterState = filters
        )
    }.stateIn(scope, kotlinx.coroutines.flow.SharingStarted.Lazily, FoodSearchState())

    private val stopWords = setOf(
        "de", "com", "da", "do", "para", "em", "um", "uma", "a", "o", "as", "os"
    )

    private val rawSynonyms = mapOf(
        "frango" to "galinha",
        "boi" to "bovino",
        "carne" to "bovino",
        "pao" to "padaria",
        "mandioca" to "aipim",
        "porco" to "suino",
        "batata" to "inglesa"
    )

    private val synonyms: Map<String, Set<String>> by lazy {
        val map = mutableMapOf<String, MutableSet<String>>()
        rawSynonyms.forEach { (key, value) ->
            map.getOrPut(key) { mutableSetOf() }.add(value)
            map.getOrPut(value) { mutableSetOf() }.add(key)
        }
        map
    }

    init {
        observeSearchTerm()
    }

    private fun observeSearchTerm() {
        combine(searchTermFlow, _filterState) { term, filters -> Pair(term, filters) }
            .debounce(300)
            .distinctUntilChanged()
            .flatMapLatest { (term, filters) ->
                val hasCategories = filters.selectedCategories.isNotEmpty()
                val hasAdvanced = filters.hasAdvancedFilters
                val hasAnyFilter = hasCategories || hasAdvanced

                if (term.length < 2 && !hasAnyFilter) {
                    _isLoading.value = false
                    flowOf(emptyList())
                } else if (hasAnyFilter && term.length < 2) {
                    _isLoading.value = true
                    if (hasCategories) {
                        foodDao.getFoodsByCategories(filters.selectedCategories.toList())
                    } else {
                        foodDao.getAllFoods()
                    }
                } else {
                    _isLoading.value = true
                    val ftsQuery = buildFtsString(term)
                    if (ftsQuery == null) {
                        _isLoading.value = false
                        flowOf(emptyList())
                    } else {
                        val normalized = term.normalizeForSearch()
                        foodDao.searchFoodsInternal(normalized, ftsQuery)
                    }
                }
            }
            .onEach { results ->
                _rawResults.value = results
                _isLoading.value = false
            }
            .launchIn(scope)
    }

    private fun buildFtsString(input: String): String? {
        val normalized = input.normalizeForSearch()
        val tokens = normalized.split(" ").filter { it.isNotBlank() && it !in stopWords }

        if (tokens.isEmpty()) return null

        val queryParts = tokens.map { token ->
            val forms = mutableSetOf<String>()

            val stem = if (token.length > 3 && token.endsWith("s")) token.dropLast(1) else token

            forms.add(token)
            forms.add(stem)

            synonyms[token]?.let { forms.addAll(it) }
            synonyms[stem]?.let { forms.addAll(it) }

            if (forms.size > 1) {
                "(" + forms.joinToString(" OR ") { "$it*" } + ")"
            } else {
                "${forms.first()}*"
            }
        }

        return queryParts.joinToString(" ")
    }

    fun onSearchTermChange(term: String) {
        if (savedStateHandle != null) {
            savedStateHandle[KEY_SM_TERM] = term
            savedStateHandle[KEY_SM_EXPANDED] = null
        } else {
            _localSearchTerm.value = term
            _localExpandedId.value = null
        }
    }

    fun onSortOptionChange(option: FoodSortOption) {
        _filterState.value = _filterState.value.copy(sortOption = option)
    }

    fun onFoodToggled(foodId: Int) {
        if (savedStateHandle != null) {
            val current = savedStateHandle.get<Int?>(KEY_SM_EXPANDED)
            if (current == foodId) {
                savedStateHandle[KEY_SM_EXPANDED] = null
            } else {
                savedStateHandle[KEY_SM_EXPANDED] = foodId
                savedStateHandle[KEY_SM_QUICK_ADD] = "100"
            }
        } else {
            val current = _localExpandedId.value
            if (current == foodId) {
                _localExpandedId.value = null
            } else {
                _localExpandedId.value = foodId
                _localQuickAdd.value = "100"
            }
        }
    }

    fun onQuickAddAmountChange(amount: String) {
        val filtered = amount.filter { it.isDigit() || it == '.' }
        if (savedStateHandle != null) {
            savedStateHandle[KEY_SM_QUICK_ADD] = filtered
        } else {
            _localQuickAdd.value = filtered
        }
    }

    fun clear() {
        if (savedStateHandle != null) {
            savedStateHandle[KEY_SM_TERM] = ""
            savedStateHandle[KEY_SM_EXPANDED] = null
            savedStateHandle[KEY_SM_QUICK_ADD] = "100"
        } else {
            _localSearchTerm.value = ""
            _localExpandedId.value = null
            _localQuickAdd.value = "100"
        }
        _rawResults.value = emptyList()
        _filterState.value = FoodFilterState.DEFAULT
    }

    fun onFilterStateChange(newState: FoodFilterState) {
        _filterState.value = newState
    }

    fun onSourceFilterChange(source: FoodSource) {
        _filterState.value = _filterState.value.copy(source = source)
    }

    fun onCategoryToggle(category: String) {
        val current = _filterState.value.selectedCategories
        val updated = if (category in current) current - category else current + category
        _filterState.value = _filterState.value.copy(selectedCategories = updated)
    }

    fun onClearCategories() {
        _filterState.value = _filterState.value.copy(selectedCategories = emptySet())
    }

    @Suppress("DEPRECATION")
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
            filters.minFat?.let { if ((food.lipidios?.total ?: 0.0) < it) return@filter false }
            filters.maxFat?.let { if ((food.lipidios?.total ?: 0.0) > it) return@filter false }
            filters.minCalories?.let { if ((food.energiaKcal ?: 0.0) < it) return@filter false }
            filters.maxCalories?.let { if ((food.energiaKcal ?: 0.0) > it) return@filter false }

            filters.minVitaminaC?.let { if ((food.vitaminaC ?: 0.0) < it) return@filter false }
            filters.minRetinol?.let { if ((food.retinol ?: 0.0) < it) return@filter false }
            filters.minTiamina?.let { if ((food.tiamina ?: 0.0) < it) return@filter false }
            filters.minRiboflavina?.let { if ((food.riboflavina ?: 0.0) < it) return@filter false }
            filters.minPiridoxina?.let { if ((food.piridoxina ?: 0.0) < it) return@filter false }
            filters.minNiacina?.let { if ((food.niacina ?: 0.0) < it) return@filter false }

            filters.minCalcio?.let { if ((food.calcio ?: 0.0) < it) return@filter false }
            filters.minFerro?.let { if ((food.ferro ?: 0.0) < it) return@filter false }
            filters.minSodio?.let { if ((food.sodio ?: 0.0) < it) return@filter false }
            filters.maxSodio?.let { if ((food.sodio ?: 0.0) > it) return@filter false }
            filters.minPotassio?.let { if ((food.potassio ?: 0.0) < it) return@filter false }
            filters.minMagnesio?.let { if ((food.magnesio ?: 0.0) < it) return@filter false }
            filters.minZinco?.let { if ((food.zinco ?: 0.0) < it) return@filter false }
            filters.minCobre?.let { if ((food.cobre ?: 0.0) < it) return@filter false }
            filters.minFosforo?.let { if ((food.fosforo ?: 0.0) < it) return@filter false }
            filters.minManganes?.let { if ((food.manganes ?: 0.0) < it) return@filter false }

            filters.minColesterol?.let { if ((food.colesterol ?: 0.0) < it) return@filter false }
            filters.maxColesterol?.let { if ((food.colesterol ?: 0.0) > it) return@filter false }
            filters.minFibra?.let { if ((food.fibraAlimentar ?: 0.0) < it) return@filter false }

            true
        }
    }
}