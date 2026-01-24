package com.mekki.taco.presentation.ui.search

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

/**
 * Reusable food search manager with FTS support, synonyms, and sorting.
 */
@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
class FoodSearchManager(
    private val foodDao: FoodDao,
    private val scope: CoroutineScope
) {
    private val _state = MutableStateFlow(FoodSearchState())
    val state: StateFlow<FoodSearchState> = _state.asStateFlow()

    // Values before sorting
    private val _rawResults = MutableStateFlow<List<Food>>(emptyList())

    private val stopWords = setOf(
        "de", "com", "da", "do", "para", "em", "um", "uma", "a", "o", "as", "os"
    )

    // Don't know about a better way to handle this - so let's add whenever necessary lmao
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
        observeSorting()
    }

    // TODO - Move all search logic in-app to a single place
    private fun observeSearchTerm() {
        _state.map { it.searchTerm }
            .debounce(300)
            .distinctUntilChanged()
            .flatMapLatest { term ->
                if (term.length < 2) {
                    _state.update { it.copy(isLoading = false) }
                    flowOf(emptyList())
                } else {
                    _state.update { it.copy(isLoading = true) }
                    val ftsQuery = buildFtsString(term)
                    if (ftsQuery == null) {
                        _state.update { it.copy(isLoading = false) }
                        flowOf(emptyList())
                    } else {
                        val normalized = term.normalizeForSearch()
                        foodDao.searchFoodsInternal(normalized, ftsQuery)
                    }
                }
            }
            .onEach { results ->
                _rawResults.value = results
                _state.update { it.copy(isLoading = false) }
            }
            .launchIn(scope)
    }

    private fun observeSorting() {
        combine(
            _rawResults,
            _state.map { it.sortOption }.distinctUntilChanged()
        ) { results, sortOption ->
            when (sortOption) {
                FoodSortOption.RELEVANCE -> results
                FoodSortOption.PROTEIN -> results.sortedByDescending { it.proteina ?: 0.0 }
                FoodSortOption.CARBS -> results.sortedByDescending { it.carboidratos ?: 0.0 }
                FoodSortOption.FAT -> results.sortedByDescending { it.lipidios?.total ?: 0.0 }
                FoodSortOption.CALORIES -> results.sortedByDescending { it.energiaKcal ?: 0.0 }
            }
        }
            .onEach { sortedResults ->
                _state.update { it.copy(results = sortedResults) }
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
        _state.update { it.copy(searchTerm = term, expandedFoodId = null) }
    }

    fun onSortOptionChange(option: FoodSortOption) {
        _state.update { it.copy(sortOption = option) }
    }

    fun onFoodToggled(foodId: Int) {
        _state.update {
            if (it.expandedFoodId == foodId) {
                it.copy(expandedFoodId = null)
            } else {
                it.copy(expandedFoodId = foodId, quickAddAmount = "100")
            }
        }
    }

    fun onQuickAddAmountChange(amount: String) {
        val filtered = amount.filter { it.isDigit() || it == '.' }
        _state.update { it.copy(quickAddAmount = filtered) }
    }

    fun clear() {
        _state.update { FoodSearchState() }
        _rawResults.value = emptyList()
    }
}