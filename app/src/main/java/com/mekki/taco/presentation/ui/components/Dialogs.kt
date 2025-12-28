package com.mekki.taco.presentation.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.mekki.taco.data.model.DietItemWithFood

@Composable
fun EditFoodItemDialog(
    item: DietItemWithFood,
    onDismiss: () -> Unit,
    onConfirm: (newQuantity: Double, newTime: String) -> Unit
) {
    var quantity by remember { mutableStateOf(item.itemDieta.quantidadeGramas.toString()) }
    var time by remember { mutableStateOf(item.itemDieta.horaConsumo ?: "12:00") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar ${item.food.nome}") },
        text = {
            Column {
                OutlinedTextField(
                    value = quantity,
                    onValueChange = { quantity = it },
                    label = { Text("Quantidade (g)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = time,
                    onValueChange = { time = it },
                    label = { Text("Hora (HH:mm)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii)
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                val newQuantity = quantity.toDoubleOrNull() ?: item.itemDieta.quantidadeGramas
                onConfirm(newQuantity, time)
            }) {
                Text("Salvar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}