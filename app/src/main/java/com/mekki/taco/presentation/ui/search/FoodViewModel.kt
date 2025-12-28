package com.mekki.taco.presentation.ui.search // Ou o pacote que você preferir

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mekki.taco.data.db.entity.Food
import com.mekki.taco.data.db.dao.FoodDao
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn

// Removi o import de kotlinx.coroutines.launch pois não está sendo usado diretamente aqui
// mas viewModelScope.launch é usado internamente por stateIn se necessário.

@OptIn(FlowPreview::class)
class AlimentoViewModel(private val foodDao: FoodDao) : ViewModel() {

    companion object {
        private const val TAG = "AlimentoViewModel"
    }

    private val _termoBusca = MutableStateFlow("")
    val termoBusca: StateFlow<String> = _termoBusca.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    val resultadosBusca: StateFlow<List<Food>> = _termoBusca
        .debounce(300L) // Adiciona um 'L' para indicar Long, boa prática
        .distinctUntilChanged()
        .flatMapLatest { termo -> // Executado quando 'termo' muda (após debounce e distinctUntilChanged)
            Log.d(TAG, "flatMapLatest: Processando termoBusca: '$termo'")
            if (termo.isBlank() || termo.length < 2) {
                _isLoading.value = false // Garante que isLoading seja false se não houver busca
                flowOf(emptyList<Food>()) // Retorna um fluxo com lista vazia
            } else {
                // O DAO retorna um Flow. Usamos operadores de Flow para gerenciar o estado.
                foodDao.buscarAlimentosPorNome(termo)
                    .onStart {
                        // Chamado quando a coleta deste Flow (do DAO) começa
                        Log.d(TAG, "Busca no DAO iniciada para o termo: '$termo'")
                        _isLoading.value = true
                    }
                    .onEach { resultados ->
                        // Chamado sempre que o Flow do DAO emite uma nova lista de resultados
                        Log.d(TAG, "Resultados recebidos do DAO para '$termo': ${resultados.size} itens.")
                        _isLoading.value = false // Desativa o loading quando os resultados chegam
                    }
                    .catch { e ->
                        // Chamado se houver um erro na coleta do Flow do DAO
                        Log.e(TAG, "Erro ao buscar alimentos para o termo '$termo'", e)
                        _isLoading.value = false // Desativa o loading em caso de erro
                        emit(emptyList<Food>()) // Emite uma lista vazia em caso de erro para não travar a UI
                    }
            }
        }
        .stateIn(
            scope = viewModelScope, // Escopo do ViewModel para a coroutine
            started = SharingStarted.WhileSubscribed(5000L), // Mantém o flow ativo por 5s
            initialValue = emptyList() // Valor inicial para resultadosBusca
        )

    fun onTermoBuscaChange(novoTermo: String) {
        Log.d(TAG, "onTermoBuscaChange: novoTermo = '$novoTermo'")
        _termoBusca.value = novoTermo
        // O estado de _isLoading agora é primariamente controlado pelo fluxo de 'resultadosBusca'
        // (onStart, onEach, catch). Não precisamos mais definir _isLoading aqui diretamente,
        // pois isso poderia causar piscadas ou estados inconsistentes com o debounce.
    }
}