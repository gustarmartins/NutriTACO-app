package com.mekki.taco.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun PortionControlInput(
    portion: String,
    onPortionChange: (String) -> Unit,
    step: Double = 10.0
) {
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }
    val effectiveScale = rememberEffectiveScale()
    // in case the space is tight we attempt to shrink that chip
    val inputWidth = if (effectiveScale > 1.15f) 140.dp else 160.dp

    var textFieldValue by remember(portion) {
        mutableStateOf(
            TextFieldValue(
                text = portion,
                selection = TextRange(portion.length)
            )
        )
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .width(160.dp)
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                shape = RoundedCornerShape(24.dp)
            )
            .padding(4.dp)
    ) {
        IconButton(
            onClick = {
                focusManager.clearFocus()
                val current = portion.toDoubleOrNull() ?: 0.0
                val new = (current - step).coerceAtLeast(0.0)
                onPortionChange(if (new % 1.0 == 0.0) new.toInt().toString() else new.toString())
            },
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                Icons.Default.Remove,
                contentDescription = "Diminuir",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    focusRequester.requestFocus()
                },
            contentAlignment = Alignment.Center
        ) {
            BasicTextField(
                value = textFieldValue,
                onValueChange = { newValue ->
                    if (newValue.text.length <= 6 && newValue.text.all { it.isDigit() || it == '.' }) {
                        textFieldValue = newValue
                        onPortionChange(newValue.text)
                    }
                },
                modifier = Modifier
                    .focusRequester(focusRequester),
                textStyle = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                ),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                maxLines = 1,
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                decorationBox = { innerTextField ->
                    Row(
                        modifier = Modifier.width(IntrinsicSize.Max),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Box(modifier = Modifier.weight(1f, fill = false)) {
                            innerTextField()
                        }
                        Text(
                            text = " g",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            )
        }

        IconButton(
            onClick = {
                focusManager.clearFocus()
                val current = portion.toDoubleOrNull() ?: 0.0
                val new = current + step
                onPortionChange(if (new % 1.0 == 0.0) new.toInt().toString() else new.toString())
            },
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                Icons.Default.Add,
                contentDescription = "Aumentar",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun TimeControlInput(
    time: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .width(160.dp)
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                shape = RoundedCornerShape(24.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 4.dp, vertical = 4.dp)
            .height(40.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.AccessTime,
            contentDescription = "Selecionar horário",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = time.ifBlank { "--:--" },
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            ),
            textAlign = TextAlign.Center
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerDialog(
    onDismissRequest: () -> Unit,
    onConfirm: (Int, Int) -> Unit,
    initialHour: Int,
    initialMinute: Int
) {
    val timePickerState = rememberTimePickerState(
        initialHour = initialHour,
        initialMinute = initialMinute,
        is24Hour = true
    )

    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            TextButton(onClick = { onConfirm(timePickerState.hour, timePickerState.minute) }) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text("Cancelar")
            }
        },
        text = {
            TimePicker(state = timePickerState)
        }
    )
}

/**
 * Drop zone placeholder for drag-and-drop reordering.
 * Shows a dashed border with a circle icon indicating where items can be dropped.
 *
 * @param mealType The meal type this drop zone belongs to (used as key for reorderable)
 * @param isEmpty Whether the meal section is empty (shows different text)
 * @param isHighlighted Whether a dragged item is hovering over this zone
 * @param modifier Modifier for the composable
 */
@Composable
fun DropZonePlaceholder(
    mealType: String,
    isEmpty: Boolean = false,
    isHighlighted: Boolean = false,
    modifier: Modifier = Modifier
) {
    val borderColor = if (isHighlighted) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.outlineVariant
    }
    val backgroundColor = if (isHighlighted) {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
    } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
    }

    Box(
        modifier = modifier
            .height(48.dp)
            .background(
                color = backgroundColor,
                shape = RoundedCornerShape(12.dp)
            )
            .dashedBorder(
                color = borderColor,
                shape = RoundedCornerShape(12.dp),
                strokeWidth = 1.5.dp,
                dashLength = 8.dp,
                gapLength = 4.dp
            )
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .background(
                        color = borderColor.copy(alpha = 0.3f),
                        shape = androidx.compose.foundation.shape.CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(
                            color = borderColor,
                            shape = androidx.compose.foundation.shape.CircleShape
                        )
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (isEmpty) "Arraste alimentos aqui" else "Soltar aqui",
                style = MaterialTheme.typography.bodySmall,
                color = if (isHighlighted) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
        }
    }
}

/**
 * Extension function to draw a dashed border around a composable.
 */
fun Modifier.dashedBorder(
    color: androidx.compose.ui.graphics.Color,
    shape: androidx.compose.ui.graphics.Shape,
    strokeWidth: androidx.compose.ui.unit.Dp,
    dashLength: androidx.compose.ui.unit.Dp,
    gapLength: androidx.compose.ui.unit.Dp
): Modifier = this.then(
    Modifier.drawWithContent {
        drawContent()
        val strokeWidthPx = strokeWidth.toPx()
        val dashLengthPx = dashLength.toPx()
        val gapLengthPx = gapLength.toPx()

        when (val outline = shape.createOutline(size, layoutDirection, this)) {
            is androidx.compose.ui.graphics.Outline.Rectangle -> {
                drawRoundRect(
                    color = color,
                    style = androidx.compose.ui.graphics.drawscope.Stroke(
                        width = strokeWidthPx,
                        pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(
                            floatArrayOf(dashLengthPx, gapLengthPx),
                            0f
                        )
                    )
                )
            }

            is androidx.compose.ui.graphics.Outline.Rounded -> {
                drawRoundRect(
                    color = color,
                    cornerRadius = outline.roundRect.topLeftCornerRadius,
                    style = androidx.compose.ui.graphics.drawscope.Stroke(
                        width = strokeWidthPx,
                        pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(
                            floatArrayOf(dashLengthPx, gapLengthPx),
                            0f
                        )
                    )
                )
            }

            else -> {}
        }
    }
)