package com.mekki.taco.presentation.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.mekki.taco.data.model.DietItemWithFood

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanResultDialog(
    candidates: List<DietItemWithFood>,
    mealTypes: List<String>,
    onConfirm: (List<DietItemWithFood>) -> Unit,
    onDismiss: () -> Unit
) {
    var items by remember { mutableStateOf(candidates) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Revisar Itens Escaneados",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                if (items.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Nenhum item encontrado.")
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(items) { item ->
                            ScanResultItemRow(
                                item = item,
                                mealTypes = mealTypes,
                                onUpdate = { updated ->
                                    items =
                                        items.map { if (it.dietItem.id == item.dietItem.id) updated else it }
                                },
                                onDelete = {
                                    items = items.filterNot { it.dietItem.id == item.dietItem.id }
                                }
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Descartar")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { onConfirm(items) },
                        enabled = items.isNotEmpty()
                    ) {
                        Text("Importar (${items.size})")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanResultItemRow(
    item: DietItemWithFood,
    mealTypes: List<String>,
    onUpdate: (DietItemWithFood) -> Unit,
    onDelete: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var qtyText by remember { mutableStateOf(item.dietItem.quantityGrams.toString()) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.food.name,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = if (item.food.isCustom) "Gerado por IA" else "Banco de Dados",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (item.food.isCustom) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, "Remover", tint = MaterialTheme.colorScheme.error)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Quantity Input
                OutlinedTextField(
                    value = qtyText,
                    onValueChange = {
                        qtyText = it
                        val newQty = it.toDoubleOrNull()
                        if (newQty != null) {
                            onUpdate(item.copy(dietItem = item.dietItem.copy(quantityGrams = newQty)))
                        }
                    },
                    label = { Text("Gramas") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .width(100.dp)
                        .height(60.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Meal Dropdown (Simplified as a Box with Click)
                Box(modifier = Modifier.weight(1f)) {
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded }
                    ) {
                        OutlinedTextField(
                            value = item.dietItem.mealType ?: "",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Refeição") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                            modifier = Modifier.menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            mealTypes.forEach { meal ->
                                DropdownMenuItem(
                                    text = { Text(meal) },
                                    onClick = {
                                        onUpdate(item.copy(dietItem = item.dietItem.copy(mealType = meal)))
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
