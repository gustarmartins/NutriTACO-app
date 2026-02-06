package com.mekki.taco.presentation.ui.fooddetail

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mekki.taco.data.db.entity.Diet
import com.mekki.taco.presentation.ui.components.AddToDietDialog
import com.mekki.taco.presentation.ui.components.CompactMacroGrid
import com.mekki.taco.presentation.ui.components.DiscardChangesDialog
import com.mekki.taco.presentation.ui.components.DynamicMacroGrid
import com.mekki.taco.presentation.ui.components.MicronutrientsPanel
import com.mekki.taco.presentation.ui.components.NutrientWarningBadges
import com.mekki.taco.presentation.ui.components.PortionControlInput
import com.mekki.taco.presentation.ui.components.SecondaryStatsGrid
import com.mekki.taco.presentation.ui.components.VerticalNutrientCard
import com.mekki.taco.presentation.ui.components.rememberEffectiveScale
import com.mekki.taco.utils.NutrientWarnings
import kotlinx.coroutines.launch

@OptIn(
    ExperimentalMaterial3Api::class,
    androidx.compose.foundation.layout.ExperimentalLayoutApi::class
)
@Composable
fun FoodDetailScreen(
    uiState: FoodDetailState,
    availableDiets: List<Diet> = emptyList(),
    onPortionChange: (String) -> Unit,
    onNavigateBack: () -> Unit,
    onTitleChange: (String) -> Unit,
    onEditToggle: () -> Unit = {},
    onSave: () -> Unit = {},
    onClone: suspend () -> Int = { 0 },
    onDelete: (() -> Unit) -> Unit = {},
    onEditFieldChange: (String, String) -> Unit = { _, _ -> },
    onNavigateToNewFood: (Int) -> Unit = {},
    onAddToDiet: (Int, Double, String, String) -> Unit = { _, _, _, _ -> },
    onFastAdd: ((String) -> Unit)? = null,
    targetDietName: String? = null,
    restrictDelete: Boolean = false
) {
    val scope = rememberCoroutineScope()
    var showMenu by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showDiscardDialog by remember { mutableStateOf(false) }
    var showAddToDietDialog by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    val onBack = {
        if (uiState.hasUnsavedChanges) {
            showDiscardDialog = true
        } else {
            onNavigateBack()
        }
    }

    BackHandler(enabled = true, onBack = onBack)

    LaunchedEffect(uiState.displayFood) {
        uiState.displayFood?.let {
            onTitleChange(it.name)
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(
            0,
            0,
            0,
            0
        ), // Handle insets manually or via safeDrawing if needed
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (uiState.isEditMode) "Editar Alimento" else "Detalhes do Alimento",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Voltar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                actions = {
                    if (uiState.isEditMode) {
                        IconButton(
                            onClick = onSave,
                            modifier = Modifier.defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
                        ) {
                            Icon(Icons.Default.Save, "Salvar")
                        }
                    } else {
                        if (onFastAdd != null) {
                            TextButton(
                                onClick = { onFastAdd(uiState.portion) },
                                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.onPrimary),
                                modifier = Modifier.defaultMinSize(
                                    minWidth = 48.dp,
                                    minHeight = 48.dp
                                )
                            ) {
                                Icon(Icons.Default.Add, null)
                                Spacer(Modifier.width(4.dp))
                                Text("Adicionar à Dieta: '${targetDietName ?: "Dieta"}'")
                            }
                        } else {
                            IconButton(
                                onClick = { showAddToDietDialog = true },
                                modifier = Modifier.defaultMinSize(
                                    minWidth = 48.dp,
                                    minHeight = 48.dp
                                )
                            ) {
                                Icon(Icons.Default.Add, "Adicionar a uma dieta")
                            }
                        }

                        Box {
                            IconButton(
                                onClick = { showMenu = true },
                                modifier = Modifier.defaultMinSize(
                                    minWidth = 48.dp,
                                    minHeight = 48.dp
                                )
                            ) {
                                Icon(Icons.Default.MoreVert, "Opções")
                            }
                            DropdownMenu(
                                expanded = showMenu,
                                onDismissRequest = { showMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Clonar e Editar") },
                                    leadingIcon = { Icon(Icons.Default.ContentCopy, null) },
                                    onClick = {
                                        showMenu = false
                                        scope.launch {
                                            val newId = onClone()
                                            if (newId > 0) onNavigateToNewFood(newId)
                                        }
                                    }
                                )

                                if (uiState.displayFood?.isCustom == true) {
                                    DropdownMenuItem(
                                        text = { Text("Editar") },
                                        leadingIcon = { Icon(Icons.Default.Edit, null) },
                                        onClick = {
                                            showMenu = false
                                            onEditToggle()
                                        }
                                    )
                                    if (!restrictDelete) {
                                        DropdownMenuItem(
                                            text = {
                                                Text(
                                                    "Deletar",
                                                    color = MaterialTheme.colorScheme.error
                                                )
                                            },
                                            leadingIcon = {
                                                Icon(
                                                    Icons.Default.Delete,
                                                    null,
                                                    tint = MaterialTheme.colorScheme.error
                                                )
                                            },
                                            onClick = {
                                                showMenu = false
                                                showDeleteConfirm = true
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
                .imePadding()
                .pointerInput(Unit) {
                    detectTapGestures(onTap = { focusManager.clearFocus() })
                }
        ) {

            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                uiState.displayFood?.let { alimento ->
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (uiState.isEditMode) {
                            item {
                                EditFoodForm(
                                    state = uiState,
                                    onFieldChange = onEditFieldChange
                                )
                            }
                        } else {
                            item {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 16.dp)
                                ) {
                                    Text(
                                        text = alimento.name,
                                        style = MaterialTheme.typography.headlineSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onBackground,
                                        modifier = Modifier.padding(bottom = 8.dp)
                                    )

                                    FlowRow(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Surface(
                                            color = if (alimento.isCustom) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.secondaryContainer,
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.align(Alignment.CenterVertically)
                                        ) {
                                            Text(
                                                text = if (alimento.isCustom) "PERSONALIZADO" else alimento.category.uppercase(),
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = if (alimento.isCustom) MaterialTheme.colorScheme.onTertiaryContainer else MaterialTheme.colorScheme.onSecondaryContainer,
                                                modifier = Modifier.padding(
                                                    horizontal = 12.dp,
                                                    vertical = 6.dp
                                                )
                                            )
                                        }

                                        PortionControlInput(
                                            portion = uiState.portion,
                                            onPortionChange = onPortionChange
                                        )
                                    }

                                    val warnings = NutrientWarnings.getWarningsForFood(alimento)
                                    if (warnings.isNotEmpty()) {
                                        NutrientWarningBadges(
                                            warnings = warnings,
                                            modifier = Modifier.padding(top = 8.dp)
                                        )
                                    }
                                }
                            }

                            item {
                                val effectiveScale = rememberEffectiveScale()
                                // Switch to compact 2×2 grid when things get too large
                                // At 540 dpi + fontScale 1.0 it breaks, so we trigger at effectiveScale = 1.227
                                // based on 440 dpi + fontScale 1.0 (common on most devices)
                                val isCompact = effectiveScale > 1.2f

                                if (isCompact) {
                                    CompactMacroGrid(
                                        energiaKcal = alimento.energiaKcal,
                                        proteinas = alimento.proteina,
                                        carboidratos = alimento.carboidratos,
                                        lipidios = alimento.lipidios?.total
                                    )
                                } else {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        VerticalNutrientCard(
                                            label = "Calorias",
                                            value = alimento.energiaKcal,
                                            unit = "kcal",
                                            color = Color(0xFFA83C3C),
                                            icon = Icons.Default.Bolt,
                                            modifier = Modifier.weight(1f)
                                        )
                                        DynamicMacroGrid(
                                            proteinas = alimento.proteina,
                                            carboidratos = alimento.carboidratos,
                                            lipidios = alimento.lipidios?.total,
                                            modifier = Modifier.weight(3f)
                                        )
                                    }
                                }
                            }

                            item {
                                SecondaryStatsGrid(
                                    fibra = alimento.fibraAlimentar,
                                    colesterol = alimento.colesterol,
                                    sodio = alimento.sodio
                                )
                            }

                            item {
                                MicronutrientsPanel(food = alimento)
                            }

                            item {
                                Text(
                                    text = if (alimento.isCustom) "Alimento criado pelo usuário." else "Dados: Tabela TACO - NEPA/UNICAMP\nOs valores são calculados proporcionalmente com base na porção.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.outline,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 24.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Deletar Alimento") },
            text = { Text("Tem certeza? Esta ação não pode ser desfeita.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete {
                            showDeleteConfirm = false
                            onNavigateBack()
                        }
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
                ) {
                    Text("Deletar")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteConfirm = false },
                    modifier = Modifier.defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
                ) {
                    Text("Cancelar")
                }
            }
        )
    }

    if (showAddToDietDialog && uiState.displayFood != null) {
        AddToDietDialog(
            food = uiState.displayFood!!,
            diets = availableDiets,
            initialQuantity = uiState.portion,
            onDismiss = { showAddToDietDialog = false },
            onConfirm = { dietId, qty, meal, time ->
                onAddToDiet(dietId, qty, meal, time)
                showAddToDietDialog = false
            }
        )
    }

    if (showDiscardDialog) {
        DiscardChangesDialog(
            onDismissRequest = { showDiscardDialog = false },
            onConfirmDiscard = {
                showDiscardDialog = false
                onNavigateBack()
            }
        )
    }
}

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun EditFoodForm(
    state: FoodDetailState,
    onFieldChange: (String, String) -> Unit
) {
    val fields = state.editFields
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        OutlinedTextField(
            value = state.editName,
            onValueChange = { onFieldChange("name", it) },
            label = { Text("Nome do Alimento") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
            singleLine = true
        )

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            maxItemsInEachRow = 2
        ) {
            OutlinedTextField(
                value = fields["kcal"].orEmpty(),
                onValueChange = { onFieldChange("kcal", it) },
                label = { Text("Calorias") },
                suffix = { Text("kcal") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier
                    .weight(1f)
                    .defaultMinSize(minWidth = 140.dp),
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyMedium.copy(
                    color = Color(0xFFA83C3C),
                    fontWeight = FontWeight.Bold
                )
            )
            OutlinedTextField(
                value = state.editPortionBase,
                onValueChange = { onFieldChange("portionBase", it) },
                label = { Text("Base") },
                suffix = { Text("g") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier
                    .weight(1f)
                    .defaultMinSize(minWidth = 140.dp),
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyMedium
            )
        }

        com.mekki.taco.presentation.ui.components.EditableMacroGrid(
            protein = fields["protein"].orEmpty(),
            onProteinChange = { onFieldChange("protein", it) },
            carbs = fields["carbs"].orEmpty(),
            onCarbsChange = { onFieldChange("carbs", it) },
            fat = fields["fat"].orEmpty(),
            onFatChange = { onFieldChange("fat", it) }
        )

        com.mekki.taco.presentation.ui.components.EditableSecondaryStatsGrid(
            fiber = fields["fiber"].orEmpty(),
            onFiberChange = { onFieldChange("fiber", it) },
            cholest = fields["colest"].orEmpty(),
            onCholestChange = { onFieldChange("colest", it) },
            sodium = fields["sodio"].orEmpty(),
            onSodiumChange = { onFieldChange("sodio", it) }
        )

        com.mekki.taco.presentation.ui.components.EditableMicronutrientsPanel(
            fields = fields,
            onFieldChange = onFieldChange
        )

        Spacer(Modifier.height(24.dp))
    }
}

@Composable
fun RowScope.EditNutrientField(
    label: String,
    value: String,
    unit: String,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text("$label ($unit)") },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = Modifier.weight(1f),
        singleLine = true
    )
}
