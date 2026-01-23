package com.mekki.taco.presentation.ui.search

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.mekki.taco.data.db.entity.Food
import com.mekki.taco.presentation.ui.components.SearchItem

@Composable
fun FoodSearchScreen(
    viewModel: FoodViewModel,
    onAlimentoClick: (alimentoId: Int) -> Unit,
    onNavigateBack: () -> Unit
) {
    val termoBusca by viewModel.termoBusca.collectAsState()
    val resultados by viewModel.resultadosBusca.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val expandedId by viewModel.expandedAlimentoId.collectAsState()
    val quickAddAmount by viewModel.quickAddAmount.collectAsState()
    val keyboardController = LocalSoftwareKeyboardController.current

    AlimentoSearchScreenContent(
        termoBusca = termoBusca,
        onTermoBuscaChange = { viewModel.onTermoBuscaChange(it) },
        resultados = resultados,
        isLoading = isLoading,
        expandedId = expandedId,
        quickAddAmount = quickAddAmount,
        onToggleItem = { id ->
            viewModel.onAlimentoToggled(id)
            keyboardController?.hide()
        },
        onAmountChange = { viewModel.onQuickAddAmountChange(it) },
        onAlimentoClick = { id ->
            val food = resultados.find { it.id == id }
            if (food != null) {
                viewModel.onFoodSelected(food)
            }
            onAlimentoClick(id)
        },
        onPerformSearch = { keyboardController?.hide() }
    )
}

@Composable
private fun AlimentoSearchScreenContent(
    modifier: Modifier = Modifier,
    termoBusca: String,
    onTermoBuscaChange: (String) -> Unit,
    resultados: List<Food>,
    isLoading: Boolean,
    expandedId: Int?,
    quickAddAmount: String,
    onToggleItem: (Int) -> Unit,
    onAmountChange: (String) -> Unit,
    onAlimentoClick: (alimentoId: Int) -> Unit,
    onPerformSearch: () -> Unit
) {
    val keyboardController = LocalSoftwareKeyboardController.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        OutlinedTextField(
            value = termoBusca,
            onValueChange = onTermoBuscaChange,
            label = { Text("Digite o nome do alimento") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Search
            ),
            keyboardActions = KeyboardActions(
                onSearch = {
                    onPerformSearch()
                    keyboardController?.hide()
                }
            ),
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Ícone de busca") }
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (isLoading) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Buscando...")
            }
        } else {
            if (termoBusca.length < 2 && resultados.isEmpty()) {
                Text(
                    "Digite ao menos 2 caracteres para iniciar a busca.",
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(top = 8.dp)
                )
            } else if (resultados.isEmpty() && termoBusca.length >= 2) {
                Text(
                    "Nenhum alimento encontrado para \"$termoBusca\".",
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(top = 8.dp)
                )
            } else if (resultados.isNotEmpty()) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(
                        items = resultados,
                        key = { alimento -> alimento.id }
                    ) { alimento ->
                        SearchItem(
                            food = alimento,
                            isExpanded = expandedId == alimento.id,
                            onToggle = { onToggleItem(alimento.id) },
                            onNavigateToDetail = { food -> onAlimentoClick(food.id) },
                            currentAmount = quickAddAmount,
                            onAmountChange = onAmountChange
                        )
                    }
                }
            }
        }
    }
}
