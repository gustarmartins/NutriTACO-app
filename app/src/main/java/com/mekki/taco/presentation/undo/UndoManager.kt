package com.mekki.taco.presentation.undo

import com.mekki.taco.data.db.entity.DailyLog
import com.mekki.taco.data.db.entity.Food
import com.mekki.taco.data.model.DietItemWithFood
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Represents an action that can be undone/redone.
 * Each action stores everything needed to reverse or replay the operation.
 */
sealed class UndoableAction {
    abstract val description: String

    // Diet Item Actions
    data class DeleteDietItem(
        val item: DietItemWithFood,
        val mealType: String,
        val index: Int,
        override val description: String = "Remover ${item.food.name}"
    ) : UndoableAction()

    data class DeleteMultipleDietItems(
        val items: List<Triple<DietItemWithFood, String, Int>>, // item, mealType, index
        override val description: String = "Remover ${items.size} itens"
    ) : UndoableAction()

    data class MoveDietItem(
        val item: DietItemWithFood,
        val fromMeal: String,
        val fromIndex: Int,
        val toMeal: String,
        val toIndex: Int,
        override val description: String = "Mover ${item.food.name}"
    ) : UndoableAction()

    data class UpdateDietItemPortion(
        val itemId: Int,
        val oldQuantity: Double,
        val newQuantity: Double,
        val foodName: String,
        override val description: String = "Alterar porção de $foodName"
    ) : UndoableAction()

    data class UpdateDietItemTime(
        val itemId: Int,
        val oldTime: String,
        val newTime: String,
        val foodName: String,
        override val description: String = "Alterar horário de $foodName"
    ) : UndoableAction()

    data class AddDietItem(
        val item: DietItemWithFood,
        val mealType: String,
        override val description: String = "Adicionar ${item.food.name}"
    ) : UndoableAction()

    data class AddMultipleDietItems(
        val items: List<DietItemWithFood>,
        val mealType: String,
        override val description: String = "Adicionar ${items.size} itens"
    ) : UndoableAction()

    data class ReplaceDietItem(
        val itemId: Int,
        val oldFood: Food,
        val newFood: Food,
        override val description: String = "Trocar ${oldFood.name} por ${newFood.name}"
    ) : UndoableAction()

    // Daily Log Actions (Diary)
    data class DeleteDailyLog(
        val log: DailyLog,
        val food: Food,
        val mealType: String,
        val index: Int,
        override val description: String = "Remover ${food.name}"
    ) : UndoableAction()

    data class DeleteMultipleDailyLogs(
        val logs: List<Triple<DailyLog, Food, Int>>, // log, food, index
        override val description: String = "Remover ${logs.size} itens"
    ) : UndoableAction()

    data class UpdateDailyLogPortion(
        val logId: Int,
        val oldQuantity: Double,
        val newQuantity: Double,
        val foodName: String,
        override val description: String = "Alterar porção de $foodName"
    ) : UndoableAction()

    data class ToggleDailyLogConsumed(
        val logId: Int,
        val wasConsumed: Boolean,
        val foodName: String,
        override val description: String = if (wasConsumed) "Desmarcar $foodName" else "Marcar $foodName"
    ) : UndoableAction()

    data class AddDailyLog(
        val log: DailyLog,
        val food: Food,
        override val description: String = "Adicionar ${food.name}"
    ) : UndoableAction()
}

/**
 * Manages undo/redo operations with a stack-based approach.
 *
 * Design:
 * - Actions are pushed to undoStack when executed
 * - Undo pops from undoStack, reverses, pushes to redoStack
 * - Redo pops from redoStack, replays, pushes to undoStack
 * - New actions clear redoStack (standard undo behavior)
 * - Stack has max size to prevent memory issues
 */
class UndoManager(
    private val maxStackSize: Int = 50
) {
    private val _undoStack = mutableListOf<UndoableAction>()
    private val _redoStack = mutableListOf<UndoableAction>()

    private val _canUndo = MutableStateFlow(false)
    val canUndo: StateFlow<Boolean> = _canUndo.asStateFlow()

    private val _canRedo = MutableStateFlow(false)
    val canRedo: StateFlow<Boolean> = _canRedo.asStateFlow()

    private val _lastAction = MutableStateFlow<UndoableAction?>(null)
    val lastAction: StateFlow<UndoableAction?> = _lastAction.asStateFlow()

    /**
     * Records an action that was executed. Call this AFTER the action is performed.
     */
    fun recordAction(action: UndoableAction) {
        _undoStack.add(action)
        _redoStack.clear() // New action invalidates redo history

        // Enforce max size
        while (_undoStack.size > maxStackSize) {
            _undoStack.removeAt(0)
        }

        updateState()
        _lastAction.value = action
    }

    /**
     * Gets the action to undo without popping. Returns null if nothing to undo.
     */
    fun peekUndo(): UndoableAction? = _undoStack.lastOrNull()

    /**
     * Gets the action to redo without popping. Returns null if nothing to redo.
     */
    fun peekRedo(): UndoableAction? = _redoStack.lastOrNull()

    /**
     * Pops and returns the action to undo. The caller must perform the reverse operation,
     * then call confirmUndo() to move the action to redo stack.
     */
    fun popUndo(): UndoableAction? {
        if (_undoStack.isEmpty()) return null
        val action = _undoStack.removeAt(_undoStack.lastIndex)
        updateState()
        return action
    }

    /**
     * Confirms that undo was performed, moving the action to redo stack.
     */
    fun confirmUndo(action: UndoableAction) {
        _redoStack.add(action)
        updateState()
    }

    /**
     * Pops and returns the action to redo. The caller must perform the operation,
     * then call confirmRedo() to move the action back to undo stack.
     */
    fun popRedo(): UndoableAction? {
        if (_redoStack.isEmpty()) return null
        val action = _redoStack.removeAt(_redoStack.lastIndex)
        updateState()
        return action
    }

    /**
     * Confirms that redo was performed, moving the action to undo stack.
     */
    fun confirmRedo(action: UndoableAction) {
        _undoStack.add(action)
        updateState()
    }

    /**
     * Clears all undo/redo history. Call when navigating away or discarding changes.
     */
    fun clear() {
        _undoStack.clear()
        _redoStack.clear()
        updateState()
        _lastAction.value = null
    }

    /**
     * Returns the number of actions in undo stack (for testing).
     */
    fun undoStackSize(): Int = _undoStack.size

    /**
     * Returns the number of actions in redo stack (for testing).
     */
    fun redoStackSize(): Int = _redoStack.size

    private fun updateState() {
        _canUndo.value = _undoStack.isNotEmpty()
        _canRedo.value = _redoStack.isNotEmpty()
    }
}