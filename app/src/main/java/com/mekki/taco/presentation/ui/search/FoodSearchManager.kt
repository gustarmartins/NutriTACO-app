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
private const val KEY_SM_FILTER_STATE = "sm_filter_state"

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

    private val _localFilterState = MutableStateFlow(FoodFilterState.DEFAULT)
    private val filterStateFlow =
        savedStateHandle?.getStateFlow(KEY_SM_FILTER_STATE, FoodFilterState.DEFAULT)
            ?: _localFilterState.asStateFlow()

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
            filterStateFlow
        )
    ) { args ->
        @Suppress("UNCHECKED_CAST")
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
            // Macros
            FoodSortOption.CALORIES -> filtered.sortedByDescending { it.energiaKcal ?: 0.0 }
            FoodSortOption.PROTEIN -> filtered.sortedByDescending { it.proteina ?: 0.0 }
            FoodSortOption.CARBS -> filtered.sortedByDescending { it.carboidratos ?: 0.0 }
            FoodSortOption.FAT -> filtered.sortedByDescending { it.lipidios?.total ?: 0.0 }
            FoodSortOption.FIBER -> filtered.sortedByDescending { it.fibraAlimentar ?: 0.0 }
            FoodSortOption.CHOLESTEROL -> filtered.sortedByDescending { it.colesterol ?: 0.0 }
            // Minerals
            FoodSortOption.SODIUM -> filtered.sortedByDescending { it.sodio ?: 0.0 }
            FoodSortOption.POTASSIUM -> filtered.sortedByDescending { it.potassio ?: 0.0 }
            FoodSortOption.CALCIUM -> filtered.sortedByDescending { it.calcio ?: 0.0 }
            FoodSortOption.MAGNESIUM -> filtered.sortedByDescending { it.magnesio ?: 0.0 }
            FoodSortOption.PHOSPHORUS -> filtered.sortedByDescending { it.fosforo ?: 0.0 }
            FoodSortOption.IRON -> filtered.sortedByDescending { it.ferro ?: 0.0 }
            FoodSortOption.ZINC -> filtered.sortedByDescending { it.zinco ?: 0.0 }
            FoodSortOption.COPPER -> filtered.sortedByDescending { it.cobre ?: 0.0 }
            FoodSortOption.MANGANESE -> filtered.sortedByDescending { it.manganes ?: 0.0 }
            // Vitamins
            FoodSortOption.VITAMIN_C -> filtered.sortedByDescending { it.vitaminaC ?: 0.0 }
            FoodSortOption.RETINOL -> filtered.sortedByDescending { it.retinol ?: 0.0 }
            FoodSortOption.THIAMINE -> filtered.sortedByDescending { it.tiamina ?: 0.0 }
            FoodSortOption.RIBOFLAVIN -> filtered.sortedByDescending { it.riboflavina ?: 0.0 }
            FoodSortOption.PYRIDOXINE -> filtered.sortedByDescending { it.piridoxina ?: 0.0 }
            FoodSortOption.NIACIN -> filtered.sortedByDescending { it.niacina ?: 0.0 }
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
        combine(searchTermFlow, filterStateFlow) { term, filters -> Pair(term, filters) }
            .debounce(300)
            .distinctUntilChanged()
            .flatMapLatest { (term, filters) ->
                val hasCategories = filters.selectedCategories.isNotEmpty()
                val hasAdvanced = filters.hasAdvancedFilters
                val hasSourceFilter = filters.source != FoodSource.ALL
                val hasAnyFilter = hasCategories || hasAdvanced || hasSourceFilter

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
        updateFilterState(filterStateFlow.value.copy(sortOption = option))
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
            savedStateHandle[KEY_SM_FILTER_STATE] = FoodFilterState.DEFAULT
        } else {
            _localSearchTerm.value = ""
            _localExpandedId.value = null
            _localQuickAdd.value = "100"
            _localFilterState.value = FoodFilterState.DEFAULT
        }
        _rawResults.value = emptyList()
    }

    fun restoreState(
        searchTerm: String,
        filterState: FoodFilterState,
        expandedId: Int?,
        quickAddAmount: String
    ) {
        if (savedStateHandle != null) {
            savedStateHandle[KEY_SM_TERM] = searchTerm
            savedStateHandle[KEY_SM_EXPANDED] = expandedId
            savedStateHandle[KEY_SM_QUICK_ADD] = quickAddAmount
            savedStateHandle[KEY_SM_FILTER_STATE] = filterState
        } else {
            _localSearchTerm.value = searchTerm
            _localExpandedId.value = expandedId
            _localQuickAdd.value = quickAddAmount
            _localFilterState.value = filterState
        }
    }

    fun onFilterStateChange(newState: FoodFilterState) {
        updateFilterState(newState)
    }

    fun onSourceFilterChange(source: FoodSource) {
        updateFilterState(filterStateFlow.value.copy(source = source))
    }

    fun onCategoryToggle(category: String) {
        val current = filterStateFlow.value.selectedCategories
        val updated = if (category in current) current - category else current + category
        updateFilterState(filterStateFlow.value.copy(selectedCategories = updated))
    }

    fun onClearCategories() {
        updateFilterState(filterStateFlow.value.copy(selectedCategories = emptySet()))
    }

    private fun updateFilterState(newState: FoodFilterState) {
        if (savedStateHandle != null) {
            savedStateHandle[KEY_SM_FILTER_STATE] = newState
        } else {
            _localFilterState.value = newState
        }
    }

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
            filters.minFibra?.let { if ((food.fibraAlimentar ?: 0.0) < it) return@filter false }
            filters.maxFibra?.let { if ((food.fibraAlimentar ?: 0.0) > it) return@filter false }
            filters.minColesterol?.let { if ((food.colesterol ?: 0.0) < it) return@filter false }
            filters.maxColesterol?.let { if ((food.colesterol ?: 0.0) > it) return@filter false }

            filters.minSaturados?.let {
                if ((food.lipidios?.saturados ?: 0.0) < it) return@filter false
            }
            filters.maxSaturados?.let {
                if ((food.lipidios?.saturados ?: 0.0) > it) return@filter false
            }
            filters.minMonoinsaturados?.let {
                if ((food.lipidios?.monoinsaturados ?: 0.0) < it) return@filter false
            }
            filters.maxMonoinsaturados?.let {
                if ((food.lipidios?.monoinsaturados ?: 0.0) > it) return@filter false
            }
            filters.minPoliinsaturados?.let {
                if ((food.lipidios?.poliinsaturados ?: 0.0) < it) return@filter false
            }
            filters.maxPoliinsaturados?.let {
                if ((food.lipidios?.poliinsaturados ?: 0.0) > it) return@filter false
            }

            filters.minVitaminaC?.let { if ((food.vitaminaC ?: 0.0) < it) return@filter false }
            filters.maxVitaminaC?.let { if ((food.vitaminaC ?: 0.0) > it) return@filter false }
            filters.minRetinol?.let { if ((food.retinol ?: 0.0) < it) return@filter false }
            filters.maxRetinol?.let { if ((food.retinol ?: 0.0) > it) return@filter false }
            filters.minTiamina?.let { if ((food.tiamina ?: 0.0) < it) return@filter false }
            filters.maxTiamina?.let { if ((food.tiamina ?: 0.0) > it) return@filter false }
            filters.minRiboflavina?.let { if ((food.riboflavina ?: 0.0) < it) return@filter false }
            filters.maxRiboflavina?.let { if ((food.riboflavina ?: 0.0) > it) return@filter false }
            filters.minPiridoxina?.let { if ((food.piridoxina ?: 0.0) < it) return@filter false }
            filters.maxPiridoxina?.let { if ((food.piridoxina ?: 0.0) > it) return@filter false }
            filters.minNiacina?.let { if ((food.niacina ?: 0.0) < it) return@filter false }
            filters.maxNiacina?.let { if ((food.niacina ?: 0.0) > it) return@filter false }

            filters.minCalcio?.let { if ((food.calcio ?: 0.0) < it) return@filter false }
            filters.maxCalcio?.let { if ((food.calcio ?: 0.0) > it) return@filter false }
            filters.minFerro?.let { if ((food.ferro ?: 0.0) < it) return@filter false }
            filters.maxFerro?.let { if ((food.ferro ?: 0.0) > it) return@filter false }
            filters.minSodio?.let { if ((food.sodio ?: 0.0) < it) return@filter false }
            filters.maxSodio?.let { if ((food.sodio ?: 0.0) > it) return@filter false }
            filters.minPotassio?.let { if ((food.potassio ?: 0.0) < it) return@filter false }
            filters.maxPotassio?.let { if ((food.potassio ?: 0.0) > it) return@filter false }
            filters.minMagnesio?.let { if ((food.magnesio ?: 0.0) < it) return@filter false }
            filters.maxMagnesio?.let { if ((food.magnesio ?: 0.0) > it) return@filter false }
            filters.minZinco?.let { if ((food.zinco ?: 0.0) < it) return@filter false }
            filters.maxZinco?.let { if ((food.zinco ?: 0.0) > it) return@filter false }
            filters.minCobre?.let { if ((food.cobre ?: 0.0) < it) return@filter false }
            filters.maxCobre?.let { if ((food.cobre ?: 0.0) > it) return@filter false }
            filters.minFosforo?.let { if ((food.fosforo ?: 0.0) < it) return@filter false }
            filters.maxFosforo?.let { if ((food.fosforo ?: 0.0) > it) return@filter false }
            filters.minManganes?.let { if ((food.manganes ?: 0.0) < it) return@filter false }
            filters.maxManganes?.let { if ((food.manganes ?: 0.0) > it) return@filter false }

            filters.minUmidade?.let { if ((food.umidade ?: 0.0) < it) return@filter false }
            filters.maxUmidade?.let { if ((food.umidade ?: 0.0) > it) return@filter false }
            filters.minCinzas?.let { if ((food.cinzas ?: 0.0) < it) return@filter false }
            filters.maxCinzas?.let { if ((food.cinzas ?: 0.0) > it) return@filter false }

            // TODO: Amino acid filters - add UI implementation (most foods don't have this data)
            filters.minTriptofano?.let {
                if ((food.aminoacidos?.triptofano ?: 0.0) < it) return@filter false
            }
            filters.maxTriptofano?.let {
                if ((food.aminoacidos?.triptofano ?: 0.0) > it) return@filter false
            }
            filters.minTreonina?.let {
                if ((food.aminoacidos?.treonina ?: 0.0) < it) return@filter false
            }
            filters.maxTreonina?.let {
                if ((food.aminoacidos?.treonina ?: 0.0) > it) return@filter false
            }
            filters.minIsoleucina?.let {
                if ((food.aminoacidos?.isoleucina ?: 0.0) < it) return@filter false
            }
            filters.maxIsoleucina?.let {
                if ((food.aminoacidos?.isoleucina ?: 0.0) > it) return@filter false
            }
            filters.minLeucina?.let {
                if ((food.aminoacidos?.leucina ?: 0.0) < it) return@filter false
            }
            filters.maxLeucina?.let {
                if ((food.aminoacidos?.leucina ?: 0.0) > it) return@filter false
            }
            filters.minLisina?.let {
                if ((food.aminoacidos?.lisina ?: 0.0) < it) return@filter false
            }
            filters.maxLisina?.let {
                if ((food.aminoacidos?.lisina ?: 0.0) > it) return@filter false
            }
            filters.minMetionina?.let {
                if ((food.aminoacidos?.metionina ?: 0.0) < it) return@filter false
            }
            filters.maxMetionina?.let {
                if ((food.aminoacidos?.metionina ?: 0.0) > it) return@filter false
            }
            filters.minCistina?.let {
                if ((food.aminoacidos?.cistina ?: 0.0) < it) return@filter false
            }
            filters.maxCistina?.let {
                if ((food.aminoacidos?.cistina ?: 0.0) > it) return@filter false
            }
            filters.minFenilalanina?.let {
                if ((food.aminoacidos?.fenilalanina ?: 0.0) < it) return@filter false
            }
            filters.maxFenilalanina?.let {
                if ((food.aminoacidos?.fenilalanina ?: 0.0) > it) return@filter false
            }
            filters.minTirosina?.let {
                if ((food.aminoacidos?.tirosina ?: 0.0) < it) return@filter false
            }
            filters.maxTirosina?.let {
                if ((food.aminoacidos?.tirosina ?: 0.0) > it) return@filter false
            }
            filters.minValina?.let {
                if ((food.aminoacidos?.valina ?: 0.0) < it) return@filter false
            }
            filters.maxValina?.let {
                if ((food.aminoacidos?.valina ?: 0.0) > it) return@filter false
            }
            filters.minArginina?.let {
                if ((food.aminoacidos?.arginina ?: 0.0) < it) return@filter false
            }
            filters.maxArginina?.let {
                if ((food.aminoacidos?.arginina ?: 0.0) > it) return@filter false
            }
            filters.minHistidina?.let {
                if ((food.aminoacidos?.histidina ?: 0.0) < it) return@filter false
            }
            filters.maxHistidina?.let {
                if ((food.aminoacidos?.histidina ?: 0.0) > it) return@filter false
            }
            filters.minAlanina?.let {
                if ((food.aminoacidos?.alanina ?: 0.0) < it) return@filter false
            }
            filters.maxAlanina?.let {
                if ((food.aminoacidos?.alanina ?: 0.0) > it) return@filter false
            }
            filters.minAcidoAspartico?.let {
                if ((food.aminoacidos?.acidoAspartico ?: 0.0) < it) return@filter false
            }
            filters.maxAcidoAspartico?.let {
                if ((food.aminoacidos?.acidoAspartico ?: 0.0) > it) return@filter false
            }
            filters.minAcidoGlutamico?.let {
                if ((food.aminoacidos?.acidoGlutamico ?: 0.0) < it) return@filter false
            }
            filters.maxAcidoGlutamico?.let {
                if ((food.aminoacidos?.acidoGlutamico ?: 0.0) > it) return@filter false
            }
            filters.minGlicina?.let {
                if ((food.aminoacidos?.glicina ?: 0.0) < it) return@filter false
            }
            filters.maxGlicina?.let {
                if ((food.aminoacidos?.glicina ?: 0.0) > it) return@filter false
            }
            filters.minProlina?.let {
                if ((food.aminoacidos?.prolina ?: 0.0) < it) return@filter false
            }
            filters.maxProlina?.let {
                if ((food.aminoacidos?.prolina ?: 0.0) > it) return@filter false
            }
            filters.minSerina?.let {
                if ((food.aminoacidos?.serina ?: 0.0) < it) return@filter false
            }
            filters.maxSerina?.let {
                if ((food.aminoacidos?.serina ?: 0.0) > it) return@filter false
            }

            true
        }
    }
}