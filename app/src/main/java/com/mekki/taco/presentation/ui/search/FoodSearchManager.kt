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
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

enum class FoodSortOption(val label: String) {
    RELEVANCE("Relevância"),
    PROTEIN("Proteínas"),
    CARBS("Carboidratos"),
    FAT("Gorduras"),
    CALORIES("Calorias")
}

data class FoodSearchState(
    val searchTerm: String = "",
    val isLoading: Boolean = false,
    val results: List<Food> = emptyList(),
    val expandedFoodId: Int? = null,
    val quickAddAmount: String = "100",
    val sortOption: FoodSortOption = FoodSortOption.RELEVANCE
)

private const val KEY_SM_TERM = "sm_search_term"
private const val KEY_SM_QUICK_ADD = "sm_quick_add"
private const val KEY_SM_SORT = "sm_sort_option"
private const val KEY_SM_EXPANDED = "sm_expanded_id"

/**
 * Reusable food search manager with FTS support, synonyms, and sorting.
 */
@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
class FoodSearchManager(
    private val foodDao: FoodDao,
    private val scope: CoroutineScope,
    private val savedStateHandle: SavedStateHandle? = null // Optional for flexibility
) {
    private val _localSearchTerm = MutableStateFlow("")
    private val _localQuickAdd = MutableStateFlow("100")
    private val _localSortOption = MutableStateFlow(FoodSortOption.RELEVANCE)
    private val _localExpandedId = MutableStateFlow<Int?>(null)

    private val searchTermFlow = savedStateHandle?.getStateFlow(KEY_SM_TERM, "") ?: _localSearchTerm.asStateFlow()
    private val quickAddFlow = savedStateHandle?.getStateFlow(KEY_SM_QUICK_ADD, "100") ?: _localQuickAdd.asStateFlow()
    private val sortOptionFlow = savedStateHandle?.getStateFlow(KEY_SM_SORT, FoodSortOption.RELEVANCE) ?: _localSortOption.asStateFlow()
    private val expandedIdFlow = savedStateHandle?.getStateFlow<Int?>(KEY_SM_EXPANDED, null) ?: _localExpandedId.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    private val _rawResults = MutableStateFlow<List<Food>>(emptyList())

    val state: StateFlow<FoodSearchState> = combine(
        listOf(
            searchTermFlow,
            _isLoading,
            _rawResults,
            expandedIdFlow,
            quickAddFlow,
            sortOptionFlow
        )
    ) { args ->
        val term = args[0] as String
        val loading = args[1] as Boolean
        val raw = args[2] as List<Food>
        val expanded = args[3] as Int?
        val quickAdd = args[4] as String
        val sort = args[5] as FoodSortOption

        val sorted = when (sort) {
            FoodSortOption.RELEVANCE -> raw
            FoodSortOption.PROTEIN -> raw.sortedByDescending { it.proteina ?: 0.0 }
            FoodSortOption.CARBS -> raw.sortedByDescending { it.carboidratos ?: 0.0 }
            FoodSortOption.FAT -> raw.sortedByDescending { it.lipidios?.total ?: 0.0 }
            FoodSortOption.CALORIES -> raw.sortedByDescending { it.energiaKcal ?: 0.0 }
        }
        FoodSearchState(
            searchTerm = term,
            isLoading = loading,
            results = sorted,
            expandedFoodId = expanded,
            quickAddAmount = quickAdd,
            sortOption = sort
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
        searchTermFlow
            .debounce(300)
            .distinctUntilChanged()
            .flatMapLatest { term ->
                if (term.length < 2) {
                    _isLoading.value = false
                    flowOf(emptyList())
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
        if (savedStateHandle != null) {
            savedStateHandle[KEY_SM_SORT] = option
        } else {
            _localSortOption.value = option
        }
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
            savedStateHandle[KEY_SM_SORT] = FoodSortOption.RELEVANCE
        } else {
            _localSearchTerm.value = ""
            _localExpandedId.value = null
            _localQuickAdd.value = "100"
            _localSortOption.value = FoodSortOption.RELEVANCE
        }
        _rawResults.value = emptyList()
    }
}