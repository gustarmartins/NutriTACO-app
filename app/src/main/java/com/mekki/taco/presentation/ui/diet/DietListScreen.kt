@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)

package com.mekki.taco.presentation.ui.diet

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mekki.taco.data.db.entity.Diet
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DietListScreen(
    viewModel: DietListViewModel,
    onNavigateToCreateDiet: () -> Unit,
    onNavigateToDietDetail: (dietId: Int) -> Unit,
    onEditDiet: (dietId: Int) -> Unit,
    onFabChange: (@Composable (() -> Unit)?) -> Unit
) {
    val dietas by viewModel.dietas.collectAsState()
    var dietToSetMain by remember { mutableStateOf<Diet?>(null) }

    LaunchedEffect(Unit) {
        onFabChange {
            FloatingActionButton(onClick = onNavigateToCreateDiet) {
                Icon(Icons.Filled.Add, contentDescription = "Criar Nova Dieta")
            }
        }
    }

    if (dietas.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "Nenhuma dieta criada ainda.",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = onNavigateToCreateDiet) {
                    Icon(Icons.Filled.Add, contentDescription = "Criar Nova Dieta")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Criar Dieta")
                }
            }
        }
    } else {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(16.dp)
        ) {
            item {
                Text(
                    "Toque e segure para definir uma dieta como Principal.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
            items(items = dietas, key = { dieta -> dieta.id }) { dieta ->
                DietListItem(
                    diet = dieta,
                    onClick = { onNavigateToDietDetail(dieta.id) },
                    onLongClick = { dietToSetMain = dieta },
                    onEditClick = { onEditDiet(dieta.id) },
                    onDeleteClick = { viewModel.deletarDieta(dieta) }
                )
            }
        }
    }

    if (dietToSetMain != null) {
        AlertDialog(
            onDismissRequest = { dietToSetMain = null },
            title = { Text("Definir como Principal?") },
            text = { Text("Deseja definir '${dietToSetMain?.name}' como sua dieta principal? Ela aparecerá primeiro na tela inicial.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        dietToSetMain?.let { viewModel.setMainDiet(it) }
                        dietToSetMain = null
                    }
                ) {
                    Text("Sim, definir")
                }
            },
            dismissButton = {
                TextButton(onClick = { dietToSetMain = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun DietListItem(
    diet: Diet,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val containerColor = if (diet.isMain)
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
    else
        MaterialTheme.colorScheme.surface

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        colors = CardDefaults.elevatedCardColors(containerColor = containerColor)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        diet.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    if (diet.isMain) {
                        Spacer(Modifier.width(8.dp))
                        Icon(
                            Icons.Filled.Star,
                            contentDescription = "Principal",
                            tint = Color(0xFFE6B905), // Gold star
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                Text(
                    "Criada em: ${formatarDataTimestamp(diet.creationDate)}",
                    style = MaterialTheme.typography.bodySmall
                )
                diet.calorieGoals?.let {
                    Text("Meta: ${it.toInt()} kcal", style = MaterialTheme.typography.bodySmall)
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onEditClick) {
                    Icon(
                        imageVector = Icons.Filled.Edit,
                        contentDescription = "Editar Dieta"
                    )
                }
                IconButton(onClick = onDeleteClick) {
                    Icon(
                        Icons.Filled.Delete,
                        contentDescription = "Deletar Dieta",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

private fun formatarDataTimestamp(timestamp: Long): String {
    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    return sdf.format(Date(timestamp))
}