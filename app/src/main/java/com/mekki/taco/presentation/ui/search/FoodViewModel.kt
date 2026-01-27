package com.mekki.taco.presentation.ui.search

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mekki.taco.data.db.dao.FoodDao
import com.mekki.taco.data.db.entity.Food
import com.mekki.taco.utils.normalizeForSearch
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
@HiltViewModel
class FoodViewModel @Inject constructor(private val foodDao: FoodDao) : ViewModel() {

    companion object {
        private const val TAG = "Search Food module"
    }

    private val _termoBusca = MutableStateFlow("")
    val termoBusca: StateFlow<String> = _termoBusca.asStateFlow()
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _expandedAlimentoId = MutableStateFlow<Int?>(null)
    val expandedAlimentoId: StateFlow<Int?> = _expandedAlimentoId.asStateFlow()

    private val _quickAddAmount = MutableStateFlow("100")
    val quickAddAmount: StateFlow<String> = _quickAddAmount.asStateFlow()

    val resultadosBusca: StateFlow<List<Food>> = _termoBusca
        .debounce(300)
        .flatMapLatest { termo ->
            if (termo.length < 2) {
                flowOf(emptyList())
            } else {
                _isLoading.value = true
                val ftsQuery = buildFtsString(termo)
                if (ftsQuery == null) {
                    _isLoading.value = false
                    flowOf(emptyList())
                } else {
                    val normalized = termo.normalizeForSearch()
                    foodDao.searchFoodsInternal(normalized, ftsQuery)
                        .onEach { _isLoading.value = false }
                }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = emptyList()
        )

    fun onTermoBuscaChange(novoTermo: String) {
        _termoBusca.value = novoTermo
    }

    fun onAlimentoToggled(id: Int) {
        if (_expandedAlimentoId.value == id) {
            _expandedAlimentoId.value = null
        } else {
            _expandedAlimentoId.value = id
            _quickAddAmount.value = "100"
        }
    }

    fun onQuickAddAmountChange(newAmount: String) {
        _quickAddAmount.value = newAmount
    }

    fun onFoodSelected(food: Food) {
        viewModelScope.launch {
            foodDao.incrementUsageCount(food.id)
        }
    }

    private val stopWords =
        setOf("de", "com", "da", "do", "para", "em", "um", "uma", "a", "o", "as", "os")

    private val rawSynonyms = mapOf(
        "frango" to "galinha",
        "boi" to "bovino",
        "carne" to "bovino",
        "pao" to "padaria",
        "mandioca" to "aipim",
        "porco" to "suino",
        "batata" to "inglesa"
    )

    // Generated bidirectional map
    private val synonyms: Map<String, Set<String>> by lazy {
        val map = mutableMapOf<String, MutableSet<String>>()
        rawSynonyms.forEach { (key, value) ->
            map.getOrPut(key) { mutableSetOf() }.add(value)
            map.getOrPut(value) { mutableSetOf() }.add(key)
        }
        map
    }

    private fun buildFtsString(input: String): String? {
        Log.d(TAG, "Raw query: $input")

        // 1. Normalize Input
        val normalized = input.normalizeForSearch()

        Log.d(TAG, "Normalized: $normalized")

        // 2. Tokenize
        val tokens = normalized.split(" ").filter { it.isNotBlank() && it !in stopWords }

        if (tokens.isEmpty()) return null

        // 3. Build FTS Query String
        // Format: (token1* OR synonym1*) AND (token2* OR synonym2*)
        // SQLite FTS uses space for AND.

        val queryParts = tokens.map { token ->
            val forms = mutableSetOf<String>()

            // Stemming
            val stem = if (token.length > 3 && token.endsWith("s")) token.dropLast(1) else token

            forms.add(token)
            forms.add(stem)

            // Synonyms
            synonyms[token]?.let { forms.addAll(it) }
            synonyms[stem]?.let { forms.addAll(it) }

            // Create clause
            if (forms.size > 1) {
                "(" + forms.joinToString(" OR ") { "$it*" } + ")"
            } else {
                "${forms.first()}*"
            }
        }

        val finalQuery = queryParts.joinToString(" ")
        Log.d(TAG, "Generated FTS Query: $finalQuery")

        return finalQuery
    }
}