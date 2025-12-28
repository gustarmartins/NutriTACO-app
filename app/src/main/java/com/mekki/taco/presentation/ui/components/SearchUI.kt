package com.mekki.taco.presentation.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.mekki.taco.data.db.entity.Food
import com.mekki.taco.presentation.ui.home.MacroText
import com.mekki.taco.utils.NutrientCalculator
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SearchItem(
    food: Food,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    onNavigateToDetail: (Int) -> Unit,
    currentAmount: String,
    onAmountChange: (String) -> Unit,
    onAdd: (() -> Unit)? = null,
    actionButtonText: String? = null,
) {
    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(isExpanded) {
        if (isExpanded) {
            coroutineScope.launch {
                delay(200) // allow animation to start
                bringIntoViewRequester.bringIntoView()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle, role = Role.Button)
            .padding(vertical = 4.dp)
            .animateContentSize()
            .bringIntoViewRequester(bringIntoViewRequester)
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = food.nome,
                    modifier = Modifier.padding(vertical = 12.dp, horizontal = 8.dp),
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                )
                if (!isExpanded) {
                    val subtitle = food.subtitleShort()
                    if (subtitle.isNotEmpty()) {
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(start = 8.dp, bottom = 8.dp),
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }
            Icon(
                imageVector = if (isExpanded) Icons.Default.KeyboardArrowDown else Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = if (isExpanded) "Recolher" else "Expandir",
                tint = MaterialTheme.colorScheme.outline
            )
        }

        AnimatedVisibility(visible = isExpanded) {
            MacroInfoBubble(
                food = food,
                currentAmount = currentAmount,
                onAmountChange = onAmountChange,
                onNavigateToDetail = { onNavigateToDetail(food.id) },
                onAdd = onAdd,
                actionButtonText = actionButtonText
            )
        }

        HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outline)
    }
}

@Composable
fun MacroInfoBubble(
    food: Food,
    currentAmount: String,
    onAmountChange: (String) -> Unit,
    onNavigateToDetail: () -> Unit,
    modifier: Modifier = Modifier,
    onAdd: (() -> Unit)? = null,
    actionButtonText: String? = null,
) {
    var isInEditMode by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    val amountDouble = currentAmount.toDoubleOrNull() ?: 0.0

    val calculatedNutrients = remember(food, amountDouble) {
        NutrientCalculator.calcularNutrientesParaPorcao(food, amountDouble)
    }

    LaunchedEffect(isInEditMode) {
        if (isInEditMode) {
            focusRequester.requestFocus()
        }
    }

    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 8.dp, end = 8.dp, bottom = 12.dp)
            .clickable(onClick = onNavigateToDetail),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (isInEditMode) {
                    OutlinedTextField(
                        value = currentAmount,
                        onValueChange = onAmountChange,
                        modifier = Modifier
                            .weight(1f)
                            .focusRequester(focusRequester),
                        label = { Text("Quantidade (g)") },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(onDone = {
                            isInEditMode = false
                            keyboardController?.hide()
                            focusManager.clearFocus()
                        }),
                        singleLine = true
                    )
                } else {
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { isInEditMode = true },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Valores por ${currentAmount}g",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Editar quantidade",
                            modifier = Modifier.size(16.dp).padding(start = 4.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                Spacer(Modifier.width(8.dp))
                Text(food.categoria, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                thickness = DividerDefaults.Thickness,
                color = DividerDefaults.color
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                MacroText("Calorias", calculatedNutrients.energiaKcal, "kcal", Color(0xFFA83C3C))
                MacroText("Carbs", calculatedNutrients.carboidratos, "g", Color(0xFFDCC48E))
                MacroText("Proteínas", calculatedNutrients.proteina, "g", Color(0xFF2E7A7A))
                MacroText("Gorduras", calculatedNutrients.lipidios?.total, "g", Color(0xFFC97C4A))
            }

            Spacer(Modifier.height(12.dp))

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                if (onAdd != null) {
                    TextButton(onClick = onAdd) {
                        Icon(Icons.Default.AddCircle, contentDescription = "Adicionar à dieta")
                        Spacer(Modifier.width(8.dp))
                        Text(actionButtonText ?: "Adicionar")
                    }
                }

                TextButton(onClick = onNavigateToDetail) {
                    Icon(Icons.Default.Info, contentDescription = "Detalhes")
                    Spacer(Modifier.width(8.dp))
                    Text("Detalhes")
                }
            }
        }
    }
}

private fun Food.subtitleShort(): String {
    val df = java.text.DecimalFormat("#.#")
    return this.categoria.takeIf { it.isNotBlank() }
        ?: this.proteina?.let { "Proteína: ${df.format(it)} g" }
        ?: this.energiaKcal?.let { "${it.toInt()} kcal" }
        ?: this.codigoOriginal
}