package com.mekki.taco.presentation.ui.profile

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mekki.taco.data.model.ActivityLevel
import java.text.DecimalFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileSheetContent(
    viewModel: ProfileViewModel,
    onDismiss: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val focusManager = LocalFocusManager.current
    val uiState by viewModel.uiState.collectAsState()
    val df = remember { DecimalFormat("#") }

    val inputTextStyle = TextStyle(fontSize = 14.sp)
    val labelTextStyle = TextStyle(fontSize = 12.sp)

    LazyColumn(
        modifier = Modifier
            .padding(16.dp)
            .pointerInput(Unit) {
                detectTapGestures(onTap = { focusManager.clearFocus() })
            },
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Meu Perfil", style = MaterialTheme.typography.headlineSmall)
                IconButton(onClick = onNavigateToSettings) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Configurações"
                    )
                }
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = uiState.weightInput,
                    onValueChange = { input ->
                        if (input.all { it.isDigit() || it == '.' || it == ',' } && input.count { it == '.' || it == ',' } <= 1) {
                            viewModel.onWeightChange(input)
                        }
                    },
                    label = { Text("Peso (kg)", style = labelTextStyle) },
                    textStyle = inputTextStyle,
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Next) }
                    )
                )
                OutlinedTextField(
                    value = uiState.heightInput,
                    onValueChange = { input ->
                        if (input.all { it.isDigit() || it == '.' || it == ',' } && input.count { it == '.' || it == ',' } <= 1) {
                            viewModel.onHeightChange(input)
                        }
                    },
                    label = { Text("Altura (cm)", style = labelTextStyle) },
                    textStyle = inputTextStyle,
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Next) }
                    )
                )
                OutlinedTextField(
                    value = uiState.ageInput,
                    onValueChange = { input ->
                        if (input.all { it.isDigit() }) {
                            viewModel.onAgeChange(input)
                        }
                    },
                    label = { Text("Idade", style = labelTextStyle) },
                    textStyle = inputTextStyle,
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { focusManager.clearFocus() }
                    )
                )
            }
        }

        item {
            SexSelector(
                selectedSex = uiState.userProfile.sex,
                onSexSelected = viewModel::onSexChange
            )
        }

        item {
            ActivityLevelSelector(
                selectedLevel = uiState.activityLevel,
                onLevelSelected = viewModel::onActivityLevelChange
            )
        }

        item {
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                thickness = DividerDefaults.Thickness,
                color = DividerDefaults.color
            )
        }
        item {
            Text("Estimativas Diárias", style = MaterialTheme.typography.titleLarge)
            Text("Taxa Metabólica Basal (TMB): ${df.format(uiState.tmb)} kcal")
            Text(
                "Gasto Calórico Total (GET): ${df.format(uiState.tdee)} kcal",
                style = MaterialTheme.typography.titleMedium
            )
        }

        item {
            Button(
                onClick = {
                    viewModel.saveProfile()
                    onDismiss()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Salvar Perfil")
            }
        }
    }
}

@Composable
fun SexSelector(selectedSex: String?, onSexSelected: (String) -> Unit) {
    val options = listOf("Masculino", "Feminino")
    Column {
        Text("Sexo", style = MaterialTheme.typography.bodyLarge)
        Row(Modifier.selectableGroup()) {
            options.forEach { text ->
                Row(
                    Modifier
                        .weight(1f)
                        .selectable(
                            selected = (text == selectedSex),
                            onClick = { onSexSelected(text) },
                            role = Role.RadioButton
                        )
                        .padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = (text == selectedSex),
                        onClick = null
                    )
                    Text(
                        text = text,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityLevelSelector(
    selectedLevel: ActivityLevel?,
    onLevelSelected: (ActivityLevel) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
        OutlinedTextField(
            value = selectedLevel?.displayName ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text("Nível de Atividade Física") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(
                    type = ExposedDropdownMenuAnchorType.PrimaryNotEditable,
                    enabled = true
                )
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            ActivityLevel.entries.forEach { level ->
                DropdownMenuItem(
                    text = { Text(level.displayName) },
                    onClick = {
                        onLevelSelected(level)
                        expanded = false
                    }
                )
            }
        }
    }
}
