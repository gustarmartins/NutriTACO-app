package com.mekki.taco.presentation.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mekki.taco.data.db.entity.Food
import com.mekki.taco.presentation.ui.search.NutrientDisplayInfo
import com.mekki.taco.utils.NutrientCalculator
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.DecimalFormat

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SearchItem(
    food: Food,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    onNavigateToDetail: (Food) -> Unit,
    currentAmount: String,
    onAmountChange: (String) -> Unit,
    onLog: ((String) -> Unit)? = null,
    onAddToDiet: ((String) -> Unit)? = null,
    onViewDetails: ((Food) -> Unit)? = null,
    showLogTutorial: Boolean = false,
    onDismissLogTutorial: () -> Unit = {},
    onFastEdit: ((Food) -> Unit)? = null,
    isAddToDietPrimary: Boolean = false,
    actionButtonLabel: String = "Registrar",
    resultIndex: Int? = null,
    highlightedNutrient: NutrientDisplayInfo? = null
) {
    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(isExpanded) {
        if (isExpanded) {
            coroutineScope.launch {
                delay(50)
                try {
                    bringIntoViewRequester.bringIntoView()
                } catch (e: Exception) {
                }
            }
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .animateContentSize(animationSpec = tween(150))
            .bringIntoViewRequester(bringIntoViewRequester),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onToggle)
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (resultIndex != null) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .background(
                                MaterialTheme.colorScheme.primaryContainer,
                                RoundedCornerShape(6.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = resultIndex.toString(),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = food.name,
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = food.category,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowDown else Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.outline
                )
            }
            AnimatedVisibility(visible = isExpanded) {
                Column(Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    Spacer(Modifier.height(12.dp))

                    SearchResultDetailContent(
                        food = food,
                        currentAmount = currentAmount,
                        onAmountChange = onAmountChange,
                        onNavigateToDetail = { onNavigateToDetail(food) },
                        onLog = onLog,
                        onAddToDiet = onAddToDiet,
                        onViewDetails = onViewDetails,
                        showLogTutorial = showLogTutorial,
                        onDismissLogTutorial = onDismissLogTutorial,
                        onFastEdit = if (onFastEdit != null) {
                            { onFastEdit(food) }
                        } else null,
                        isAddToDietPrimary = isAddToDietPrimary,
                        actionButtonLabel = actionButtonLabel,
                        highlightedNutrient = highlightedNutrient
                    )

                    Spacer(Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun SearchResultDetailContent(
    food: Food,
    currentAmount: String,
    onAmountChange: (String) -> Unit,
    onNavigateToDetail: () -> Unit,
    onLog: ((String) -> Unit)?,
    onAddToDiet: ((String) -> Unit)?,
    onViewDetails: ((Food) -> Unit)?,
    showLogTutorial: Boolean,
    onDismissLogTutorial: () -> Unit,
    onFastEdit: (() -> Unit)?,
    isAddToDietPrimary: Boolean,
    actionButtonLabel: String,
    highlightedNutrient: NutrientDisplayInfo? = null
) {
    val amountDouble = currentAmount.toDoubleOrNull() ?: 0.0
    val calc = remember(food, amountDouble) {
        NutrientCalculator.calcularNutrientesParaPorcao(food, amountDouble)
    }

    Column {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            PortionControlInput(
                portion = currentAmount,
                onPortionChange = onAmountChange
            )
        }

        Spacer(Modifier.height(12.dp))

        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val hasExtraNutrient = highlightedNutrient != null
            val wideThreshold = if (hasExtraNutrient) 480.dp else 386.dp

            if (maxWidth >= wideThreshold) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    MacroMiniColumn("Energia", calc.energiaKcal, Color(0xFFA83C3C), unit = "kcal")
                    MacroMiniColumn(
                        "Carboidratos",
                        calc.carboidratos,
                        Color(0xFFDCC48E),
                        unit = "g"
                    )
                    MacroMiniColumn("Proteínas", calc.proteina, Color(0xFF2E7A7A), unit = "g")
                    MacroMiniColumn("Gorduras", calc.lipidios?.total, Color(0xFFC97C4A), unit = "g")
                    if (highlightedNutrient != null) {
                        MacroMiniColumn(
                            highlightedNutrient.label,
                            highlightedNutrient.getValue(calc),
                            highlightedNutrient.color,
                            unit = highlightedNutrient.unit
                        )
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        MacroMiniColumn(
                            "Energia", calc.energiaKcal, Color(0xFFA83C3C), "kcal",
                            modifier = Modifier.weight(1f)
                        )
                        MacroMiniColumn(
                            "Carboidratos", calc.carboidratos, Color(0xFFDCC48E), "g",
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row(modifier = Modifier.fillMaxWidth()) {
                        MacroMiniColumn(
                            "Proteínas", calc.proteina, Color(0xFF2E7A7A), "g",
                            modifier = Modifier.weight(1f)
                        )
                        MacroMiniColumn(
                            "Gorduras", calc.lipidios?.total, Color(0xFFC97C4A), "g",
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (highlightedNutrient != null) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            MacroMiniColumn(
                                highlightedNutrient.label,
                                highlightedNutrient.getValue(calc),
                                highlightedNutrient.color,
                                unit = highlightedNutrient.unit
                            )
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        Spacer(Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(
                onClick = onNavigateToDetail,
                modifier = Modifier.weight(1f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowForward, null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(8.dp))
                Text("Detalhes")
            }

            if (onFastEdit != null) {
                val isCustom = food.isCustom
                Button(
                    onClick = onFastEdit,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.tertiary,
                        contentColor = MaterialTheme.colorScheme.onTertiary
                    )
                ) {
                    Icon(
                        if (isCustom) Icons.Default.Edit else Icons.Default.ContentCopy,
                        null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(if (isCustom) "Editar" else "Clonar e Editar")
                }
            }
        }

        if (onLog != null || onAddToDiet != null) {
            Spacer(Modifier.height(8.dp))

            if (onLog != null && onAddToDiet != null) {
                var showDropdown by remember { mutableStateOf(false) }

                Box(modifier = Modifier.fillMaxWidth()) {
                    OnboardingTooltip(
                        isVisible = showLogTutorial,
                        text = "Registre o alimento rapidamente no seu diário ou adicione-o a uma dieta.",
                        onDismiss = onDismissLogTutorial
                    ) {
                        Button(
                            onClick = { showDropdown = true },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        ) {
                            Icon(Icons.Default.AddCircle, null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Registrar")
                        }
                    }

                    DropdownMenu(
                        expanded = showDropdown,
                        onDismissRequest = { showDropdown = false },
                        modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                    ) {
                        if (onLog != null) {
                            DropdownMenuItem(
                                text = { Text("Para o Diário (Hoje)") },
                                onClick = {
                                    onLog(currentAmount)
                                    showDropdown = false
                                },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Today,
                                        null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            )
                        }
                        if (onAddToDiet != null) {
                            DropdownMenuItem(
                                text = { Text("Para uma Dieta") },
                                onClick = {
                                    onAddToDiet(currentAmount)
                                    showDropdown = false
                                },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.ContentCopy,
                                        null,
                                        tint = MaterialTheme.colorScheme.secondary
                                    )
                                }
                            )
                        }
                    }
                }
            } else {
                // no Dropdown here
                val action = onLog ?: onAddToDiet!!
                Button(
                    onClick = { action(currentAmount) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Icon(Icons.Default.AddCircle, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(actionButtonLabel)
                }
            }
        }
    }
}

@Composable
private fun MacroMiniColumn(
    label: String,
    value: Double?,
    color: Color,
    unit: String,
    modifier: Modifier = Modifier
) {
    val df = DecimalFormat("#.#")
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Text(
            text = if (value == null) "--" else df.format(value) + unit,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1
        )
    }
}