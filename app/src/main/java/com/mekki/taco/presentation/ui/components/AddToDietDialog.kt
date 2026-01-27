package com.mekki.taco.presentation.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mekki.taco.data.db.entity.Diet
import com.mekki.taco.data.db.entity.Food
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddToDietDialog(
    food: Food,
    diets: List<Diet>,
    initialQuantity: String = "100",
    onDismiss: () -> Unit,
    onConfirm: (dietId: Int, quantity: Double, mealType: String, time: String) -> Unit
) {
    var selectedDiet by remember { mutableStateOf(diets.firstOrNull()) }
    var isDietExpanded by remember { mutableStateOf(false) }

    val mealTypes =
        listOf("Café da Manhã", "Almoço", "Jantar", "Lanche", "Pré-treino", "Pós-treino")
    var selectedMealType by remember { mutableStateOf(mealTypes[0]) }
    var isMealExpanded by remember { mutableStateOf(false) }

    var quantity by remember(initialQuantity) { mutableStateOf(initialQuantity) }
    var time by remember { mutableStateOf("12:00") }

    // we use the launchedeffect so time updates as user picks the meal
    LaunchedEffect(selectedMealType) {
        time = when (selectedMealType) {
            "Café da Manhã" -> "08:00"
            "Almoço" -> "12:00"
            "Jantar" -> "20:00"
            "Lanche" -> "16:00"
            "Pré-treino" -> "17:00"
            "Pós-treino" -> "18:00"
            else -> "12:00"
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text("Adicionar à Dieta")
                Text(
                    food.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                if (diets.isEmpty()) {
                    Text("Você não possui dietas criadas.")
                } else {
                    // Diet Selector
                    ExposedDropdownMenuBox(
                        expanded = isDietExpanded,
                        onExpandedChange = { isDietExpanded = !isDietExpanded }
                    ) {
                        OutlinedTextField(
                            value = selectedDiet?.name ?: "",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Dieta") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isDietExpanded) },
                            modifier = Modifier
                                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                                .fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = isDietExpanded,
                            onDismissRequest = { isDietExpanded = false }
                        ) {
                            diets.forEach { diet ->
                                DropdownMenuItem(
                                    text = { Text(diet.name) },
                                    onClick = {
                                        selectedDiet = diet
                                        isDietExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Meal Type Selector
                    ExposedDropdownMenuBox(
                        expanded = isMealExpanded,
                        onExpandedChange = { isMealExpanded = !isMealExpanded }
                    ) {
                        OutlinedTextField(
                            value = selectedMealType,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Refeição") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isMealExpanded) },
                            modifier = Modifier
                                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                                .fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = isMealExpanded,
                            onDismissRequest = { isMealExpanded = false }
                        ) {
                            mealTypes.forEach { meal ->
                                DropdownMenuItem(
                                    text = { Text(meal) },
                                    onClick = {
                                        selectedMealType = meal
                                        isMealExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Portion Control
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
                    ) {
                        Text(
                            "Quantidade",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        PortionControlInput(
                            portion = quantity,
                            onPortionChange = { quantity = it }
                        )
                    }

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
                    ) {
                        Text(
                            "Horário",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        var showTimePicker by remember { mutableStateOf(false) }
                        TimeControlInput(
                            time = time,
                            onClick = { showTimePicker = true }
                        )

                        if (showTimePicker) {
                            val parts = time.split(":")
                            val initialHour = parts.getOrNull(0)?.toIntOrNull() ?: 12
                            val initialMinute = parts.getOrNull(1)?.toIntOrNull() ?: 0

                            TimePickerDialog(
                                onDismissRequest = { showTimePicker = false },
                                onConfirm = { h, m ->
                                    time = String.format(Locale.US, "%02d:%02d", h, m)
                                    showTimePicker = false
                                },
                                initialHour = initialHour,
                                initialMinute = initialMinute
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    selectedDiet?.let { diet ->
                        val qty = quantity.toDoubleOrNull() ?: 100.0
                        onConfirm(diet.id, qty, selectedMealType, time)
                    }
                },
                enabled = selectedDiet != null && diets.isNotEmpty()
            ) {
                Text("Adicionar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}