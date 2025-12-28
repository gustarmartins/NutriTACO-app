package com.mekki.taco.presentation.ui.search

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mekki.taco.data.db.dao.FoodDao
import com.mekki.taco.data.db.entity.Food
import com.mekki.taco.data.db.entity.Lipidios
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlimentoSearchScreen(
    viewModel: AlimentoViewModel,
    onAlimentoClick: (alimentoId: Int) -> Unit,
    onNavigateBack: () -> Unit
) {
    // Collect state from the ViewModel
    val termoBusca by viewModel.termoBusca.collectAsState()
    val resultados by viewModel.resultadosBusca.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val keyboardController = LocalSoftwareKeyboardController.current


        AlimentoSearchScreenContent(
            termoBusca = termoBusca,
            onTermoBuscaChange = { viewModel.onTermoBuscaChange(it) },
            resultados = resultados,
            isLoading = isLoading,
            onAlimentoClick = onAlimentoClick, // Pass the navigation lambda through
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
    onAlimentoClick: (alimentoId: Int) -> Unit,
    onPerformSearch: () -> Unit
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    Log.d("AlimentoSearchScreen", "Recompondo Content: termo='$termoBusca', loading=$isLoading, resultados=${resultados.size}")

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
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Buscando...")
            }
        } else {
            if (termoBusca.length < 2 && resultados.isEmpty()) {
                Text("Digite ao menos 2 caracteres para iniciar a busca.",
                    modifier = Modifier.align(Alignment.CenterHorizontally).padding(top=8.dp))
            } else if (resultados.isEmpty() && termoBusca.length >= 2) {
                Text("Nenhum alimento encontrado para \"$termoBusca\".",
                    modifier = Modifier.align(Alignment.CenterHorizontally).padding(top=8.dp))
            } else if (resultados.isNotEmpty()){
                LazyColumn(
                    modifier = Modifier.fillMaxSize(), // Allow list to take available space
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(
                        items = resultados,
                        key = { alimento -> alimento.id }
                    ) { alimento ->
                        AlimentoListItem(food = alimento) {
                            onAlimentoClick(alimento.id)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AlimentoListItem(food: Food, onClick: () -> Unit) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = food.nome, style = MaterialTheme.typography.titleMedium)
            Text(text = "Categoria: ${food.categoria}", style = MaterialTheme.typography.bodySmall)
        }
    }
}
