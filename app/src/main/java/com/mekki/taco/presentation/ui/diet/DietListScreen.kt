@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)

package com.mekki.taco.presentation.ui.diet

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.SaveAlt
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mekki.taco.data.db.entity.Diet
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DietListScreen(
    viewModel: DietListViewModel,
    onNavigateToCreateDiet: () -> Unit,
    onNavigateToDietDetail: (dietId: Int) -> Unit,
    onEditDiet: (dietId: Int) -> Unit,
    onFabChange: (@Composable (() -> Unit)?) -> Unit,
    onActionsChange: (@Composable (() -> Unit)?) -> Unit
) {
    val dietas by viewModel.dietas.collectAsState()
    val sharingStatus by viewModel.sharingStatus.collectAsState()
    val shareUri by viewModel.shareUri.collectAsState()
    var dietToSetMain by remember { mutableStateOf<Diet?>(null) }
    var dietToDelete by remember { mutableStateOf<Diet?>(null) }
    var showShareInstructions by remember { mutableStateOf(false) }
    var dietToShareId by remember { mutableIntStateOf(-1) }
    var dietToShareName by remember { mutableStateOf("") }
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { viewModel.importDiet(it) }
    }

    val saveToDeviceLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/octet-stream")
    ) { uri ->
        uri?.let {
            if (dietToShareId != -1) {
                viewModel.exportDiet(dietToShareId, it)
                dietToShareId = -1
                dietToShareName = ""
            }
        }
    }

    LaunchedEffect(shareUri) {
        shareUri?.let { uri ->
            val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                type = "application/octet-stream"
                putExtra(android.content.Intent.EXTRA_STREAM, uri)
                addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(
                android.content.Intent.createChooser(
                    intent,
                    "Compartilhar Dieta"
                )
            )
            viewModel.clearShareUri()
        }
    }

    LaunchedEffect(sharingStatus) {
        sharingStatus?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearSharingStatus()
        }
    }

    DisposableEffect(Unit) {
        onFabChange {
            FloatingActionButton(onClick = onNavigateToCreateDiet) {
                Icon(Icons.Filled.Add, contentDescription = "Criar Nova Dieta")
            }
        }
        onActionsChange {
            TextButton(onClick = { importLauncher.launch("*/*") }) {
                Icon(Icons.Filled.ArrowDownward, contentDescription = null)
                Spacer(Modifier.width(4.dp))
                Text("Importar", fontWeight = FontWeight.Medium)
            }
        }
        onDispose {
            onFabChange(null)
            onActionsChange(null)
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
                    onDeleteClick = { dietToDelete = dieta },
                    onExportClick = {
                        dietToShareId = dieta.id
                        dietToShareName = dieta.name
                        showShareInstructions = true
                    }
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

    if (showShareInstructions) {
        AlertDialog(
            onDismissRequest = {
                showShareInstructions = false
                dietToShareId = -1
                dietToShareName = ""
            },
            icon = { Icon(Icons.Filled.Share, contentDescription = null) },
            title = { Text("Exportar Dieta") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Escolha como deseja exportar:")

                    FilledTonalButton(
                        onClick = {
                            showShareInstructions = false
                            if (dietToShareId != -1) {
                                val fileName = dietToShareName
                                    .replace(Regex("[^a-zA-ZÀ-ú0-9._\\- ]"), "")
                                    .trim()
                                    .ifEmpty { "dieta" }
                                saveToDeviceLauncher.launch("$fileName.dieta")
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Filled.SaveAlt, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Salvar no Dispositivo")
                    }

                    OutlinedButton(
                        onClick = {
                            showShareInstructions = false
                            if (dietToShareId != -1) {
                                viewModel.shareDiet(dietToShareId)
                                dietToShareId = -1
                                dietToShareName = ""
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Filled.Share, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Enviar para Alguém")
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(
                    onClick = {
                        showShareInstructions = false
                        dietToShareId = -1
                        dietToShareName = ""
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                ) {
                    Text("Cancelar")
                }
            }
        )
    }

    if (dietToDelete != null) {
        AlertDialog(
            onDismissRequest = { dietToDelete = null },
            title = { Text("Excluir Dieta?") },
            text = { Text("A dieta '${dietToDelete?.name}' será excluída.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        val diet = dietToDelete ?: return@TextButton
                        dietToDelete = null
                        coroutineScope.launch {
                            val snapshot = viewModel.deleteDietWithSnapshot(diet)
                            val result = snackbarHostState.showSnackbar(
                                message = "Dieta '${diet.name}' excluída",
                                actionLabel = "Desfazer",
                                withDismissAction = true,
                                duration = SnackbarDuration.Indefinite
                            )
                            if (result == SnackbarResult.ActionPerformed) {
                                viewModel.restoreDiet(snapshot)
                            }
                        }
                    }
                ) {
                    Text("Excluir", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { dietToDelete = null }) {
                    Text("Cancelar")
                }
            }
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
fun DietListItem(
    diet: Diet,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onExportClick: () -> Unit
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
                IconButton(onClick = onExportClick) {
                    Icon(
                        imageVector = Icons.Filled.Share,
                        contentDescription = "Exportar Dieta"
                    )
                }
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