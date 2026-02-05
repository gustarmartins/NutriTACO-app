package com.mekki.taco.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MoveDown
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mekki.taco.data.db.entity.Food
import com.mekki.taco.utils.NutrientCalculator
import java.text.DecimalFormat

/**
 * Actions to food items, in the format:
 *
 * 1. Header: Food name + quick action icons
 * 2. Portion & Time controls components
 * 3. Calories with delta indicator + macro icons
 * 4. Portion presets (extra one for original if available)
 * 6. Meal selector + Clone/Move options
 * 7. Action itemsM
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnifiedFoodItemActionsSheet(
    foodName: String,
    food: Food,
    currentQuantity: Double,
    currentTime: String,
    currentMealType: String,
    originalQuantity: Double? = null,
    isEditMode: Boolean,
    mealTypes: List<String>,
    onDismiss: () -> Unit,
    onViewDetails: () -> Unit,
    onUpdateItem: (quantity: Double, time: String, mealType: String) -> Unit,
    onEditNutrients: (() -> Unit)? = null,
    onReplaceFood: (() -> Unit)? = null,
    onCloneToMeal: () -> Unit,
    onMoveToMeal: ((String) -> Unit)? = null,
    onAddToLog: (() -> Unit)? = null,
    onDelete: () -> Unit
) {
    val df = DecimalFormat("#.#")

    // state to handle editable fields
    var quantity by remember { mutableStateOf(df.format(currentQuantity)) }
    var time by remember { mutableStateOf(currentTime.ifBlank { "12:00" }) }
    var selectedMeal by remember {
        mutableStateOf(currentMealType.ifBlank {
            mealTypes.firstOrNull() ?: ""
        })
    }
    var showTimePicker by remember { mutableStateOf(false) }
    var showMealDropdown by remember { mutableStateOf(false) }
    var showMoveMenu by remember { mutableStateOf(false) }

    val currentQty = quantity.replace(",", ".").toDoubleOrNull() ?: currentQuantity
    val calc = remember(food, currentQty) {
        NutrientCalculator.calcularNutrientesParaPorcao(food, currentQty)
    }
    val originalCalc = remember(food, originalQuantity) {
        originalQuantity?.let { NutrientCalculator.calcularNutrientesParaPorcao(food, it) }
    }

    val hasChanges = remember(quantity, time, selectedMeal) {
        val newQty = quantity.replace(",", ".").toDoubleOrNull() ?: currentQuantity
        newQty != currentQuantity || time != currentTime || selectedMeal != currentMealType
    }

    ModalBottomSheet(onDismissRequest = {
        // Auto-save on dismiss if there are changes
        if (hasChanges && isEditMode) {
            val newQty = quantity.replace(",", ".").toDoubleOrNull() ?: currentQuantity
            onUpdateItem(newQty, time, selectedMeal)
        }
        onDismiss()
    }) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp)
        ) {
            // Header: Food name + Quick action icons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = foodName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                // Quick action icons
                if (onReplaceFood != null && isEditMode) {
                    IconButton(
                        onClick = { onDismiss(); onReplaceFood() },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            Icons.Default.SwapHoriz,
                            contentDescription = "Trocar",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                IconButton(
                    onClick = { onDismiss(); onViewDetails() },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = "Ver Detalhes",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            HorizontalDivider()

            // Portion & Time Row (only in edit mode)
            if (isEditMode) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    PortionControlInput(
                        portion = quantity,
                        onPortionChange = { quantity = it },
                        step = 5.0
                    )
                    TimeControlInput(
                        time = time,
                        onClick = { showTimePicker = true }
                    )
                }

                // Calories + Macros unified row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.LocalFireDepartment,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "${df.format(calc.energiaKcal ?: 0.0)}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    originalCalc?.let { orig ->
                        val delta = (calc.energiaKcal ?: 0.0) - (orig.energiaKcal ?: 0.0)
                        if (delta != 0.0) {
                            Text(
                                text = "(${if (delta > 0) "+" else ""}${df.format(delta)})",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (delta > 0) MaterialTheme.colorScheme.error
                                else MaterialTheme.colorScheme.tertiary
                            )
                        }
                    }

                    Spacer(Modifier.weight(1f))

                    DietTotalsMacroRow(
                        protein = calc.proteina ?: 0.0,
                        carbs = calc.carboidratos ?: 0.0,
                        fat = calc.lipidios?.total ?: 0.0
                    )
                }

                Spacer(Modifier.height(12.dp))

                // Portion presets
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val presets = buildList {
                        add(50.0)
                        originalQuantity?.let { orig ->
                            if (orig !in listOf(50.0, 100.0, 150.0, 200.0)) add(orig)
                        }
                        add(100.0)
                        add(150.0)
                        add(200.0)
                    }.distinct().sorted()

                    presets.forEach { preset ->
                        val isOriginal = preset == originalQuantity
                        val isSelected = currentQty.toInt() == preset.toInt()
                        FilterChip(
                            selected = isSelected,
                            onClick = { quantity = preset.toInt().toString() },
                            label = {
                                Text(
                                    "${preset.toInt()}g${if (isOriginal) "*" else ""}",
                                    fontWeight = if (isOriginal) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = if (isOriginal) {
                                FilterChipDefaults.filterChipColors(
                                    containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(
                                        alpha = 0.5f
                                    )
                                )
                            } else {
                                FilterChipDefaults.filterChipColors()
                            }
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))

                // Meal type selector
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    OutlinedTextField(
                        value = selectedMeal,
                        onValueChange = { },
                        label = { Text("Refeição") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = false,
                        readOnly = true,
                        singleLine = true,
                        trailingIcon = {
                            Icon(
                                Icons.Default.ArrowDropDown,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledTextColor = MaterialTheme.colorScheme.onSurface,
                            disabledBorderColor = MaterialTheme.colorScheme.outline,
                            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clickable { showMealDropdown = true }
                    )
                    DropdownMenu(
                        expanded = showMealDropdown,
                        onDismissRequest = { showMealDropdown = false }
                    ) {
                        mealTypes.forEach { meal ->
                            DropdownMenuItem(
                                text = { Text(meal) },
                                onClick = {
                                    selectedMeal = meal
                                    showMealDropdown = false
                                },
                                leadingIcon = {
                                    if (meal == selectedMeal) {
                                        Icon(
                                            Icons.Default.Check,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            )
                        }
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
            }

            SectionHeader("Copiar / Mover")

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ActionChip(
                    icon = Icons.Default.ContentCopy,
                    label = "Clonar",
                    onClick = { onDismiss(); onCloneToMeal() },
                    modifier = Modifier.weight(1f)
                )
                if (onMoveToMeal != null) {
                    Box(modifier = Modifier.weight(1f)) {
                        ActionChip(
                            icon = Icons.Default.MoveDown,
                            label = "Mover para...",
                            onClick = { showMoveMenu = true },
                            modifier = Modifier.fillMaxWidth()
                        )
                        DropdownMenu(
                            expanded = showMoveMenu,
                            onDismissRequest = { showMoveMenu = false }
                        ) {
                            mealTypes.filter { it != selectedMeal }.forEach { meal ->
                                DropdownMenuItem(
                                    text = { Text(meal) },
                                    onClick = {
                                        showMoveMenu = false
                                        onDismiss()
                                        onMoveToMeal(meal)
                                    }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            SheetActionItem(
                icon = Icons.Default.Info,
                title = "Ver Detalhes",
                subtitle = "Informações nutricionais completas",
                onClick = { onDismiss(); onViewDetails() }
            )

            if (isEditMode) {
                if (onEditNutrients != null) {
                    SheetActionItem(
                        icon = Icons.Default.Edit,
                        title = "Personalizar Nutrientes",
                        subtitle = "Criar cópia editável deste alimento",
                        onClick = { onDismiss(); onEditNutrients() }
                    )
                }

                if (onReplaceFood != null) {
                    SheetActionItem(
                        icon = Icons.Default.SwapHoriz,
                        title = "Trocar Alimento",
                        subtitle = "Substituir por outro alimento",
                        onClick = { onDismiss(); onReplaceFood() }
                    )
                }
            }

            if (onAddToLog != null) {
                SheetActionItem(
                    icon = Icons.Default.AddCircleOutline,
                    title = "Adicionar ao Diário",
                    subtitle = "Registrar consumo de hoje",
                    onClick = { onDismiss(); onAddToLog() }
                )
            }

            if (isEditMode) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                SheetActionItem(
                    icon = Icons.Default.Delete,
                    title = "Remover",
                    subtitle = null,
                    onClick = { onDismiss(); onDelete() },
                    isDestructive = true
                )
            }
        }
    }

    if (showTimePicker) {
        SheetTimePickerDialog(
            initialTime = time,
            onDismiss = { showTimePicker = false },
            onConfirm = { newTime ->
                time = newTime
                showTimePicker = false
            }
        )
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(start = 16.dp, top = 12.dp, bottom = 4.dp)
    )
}

@Composable
private fun SheetActionItem(
    icon: ImageVector,
    title: String,
    subtitle: String?,
    onClick: () -> Unit,
    isDestructive: Boolean = false
) {
    val contentColor = if (isDestructive) MaterialTheme.colorScheme.error
    else MaterialTheme.colorScheme.onSurface

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = contentColor,
            modifier = Modifier.size(24.dp)
        )
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = contentColor
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}

@Composable
private fun ActionChip(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(18.dp)
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SheetTimePickerDialog(
    initialTime: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    val parts = initialTime.split(":")
    val initialHour = parts.getOrNull(0)?.toIntOrNull() ?: 12
    val initialMinute = parts.getOrNull(1)?.toIntOrNull() ?: 0

    val timePickerState = rememberTimePickerState(
        initialHour = initialHour,
        initialMinute = initialMinute,
        is24Hour = true
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                val hour = timePickerState.hour.toString().padStart(2, '0')
                val minute = timePickerState.minute.toString().padStart(2, '0')
                onConfirm("$hour:$minute")
            }) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        },
        text = {
            TimePicker(state = timePickerState)
        }
    )
}