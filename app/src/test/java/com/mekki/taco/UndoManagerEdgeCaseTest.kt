package com.mekki.taco

import com.mekki.taco.data.db.entity.DailyLog
import com.mekki.taco.data.db.entity.DietItem
import com.mekki.taco.data.db.entity.Food
import com.mekki.taco.data.model.DietItemWithFood
import com.mekki.taco.presentation.undo.UndoManager
import com.mekki.taco.presentation.undo.UndoableAction
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Edge case and stress tests for UndoManager.
 * 
 * These tests cover potential failure scenarios and ensure
 * the system handles them gracefully.
 */
class UndoManagerEdgeCaseTest {

    private lateinit var undoManager: UndoManager

    private fun createDietItem(id: Int, mealType: String, sortOrder: Int, quantity: Double = 100.0) = DietItem(
        id = id, dietId = 1, foodId = id, quantityGrams = quantity, mealType = mealType, sortOrder = sortOrder
    )

    private fun createFood(id: Int, name: String) = Food(
        id = id, tacoID = "TEST-$id", name = name, category = "Test", isCustom = false,
        proteina = 10.0, carboidratos = 20.0, energiaKcal = 100.0,
        energiaKj = null, colesterol = null, cinzas = null, calcio = null,
        magnesio = null, manganes = null, fosforo = null, ferro = null,
        sodio = null, potassio = null, cobre = null, zinco = null,
        retinol = null, RE = null, RAE = null, tiamina = null,
        riboflavina = null, piridoxina = null, niacina = null,
        vitaminaC = null, umidade = null, fibraAlimentar = null,
        lipidios = null, aminoacidos = null
    )

    private fun createItemWithFood(id: Int, mealType: String, sortOrder: Int, name: String) =
        DietItemWithFood(createDietItem(id, mealType, sortOrder), createFood(id, name))

    @Before
    fun setup() {
        undoManager = UndoManager(maxStackSize = 5)
    }

    // ============================================
    // EDGE CASE: Concurrent-like Operations
    // ============================================

    @Test
    fun `rapid succession of operations maintains correct order`() {
        // Simulate rapid user actions
        for (i in 1..10) {
            val item = createItemWithFood(i, "Meal", 0, "Food $i")
            undoManager.recordAction(UndoableAction.DeleteDietItem(item, "Meal", 0))
        }
        
        // Only last 5 should remain (max stack size)
        assertEquals(5, undoManager.undoStackSize())
        
        // Most recent should be Food 10
        val last = undoManager.peekUndo() as UndoableAction.DeleteDietItem
        assertEquals("Food 10", last.item.food.name)
    }

    @Test
    fun `alternating undo and new actions sequence`() {
        val item1 = createItemWithFood(1, "Meal", 0, "Food1")
        val item2 = createItemWithFood(2, "Meal", 0, "Food2")
        val item3 = createItemWithFood(3, "Meal", 0, "Food3")
        
        // Record action 1
        undoManager.recordAction(UndoableAction.DeleteDietItem(item1, "Meal", 0))
        
        // Record action 2
        undoManager.recordAction(UndoableAction.DeleteDietItem(item2, "Meal", 0))
        
        // Undo action 2
        val undone = undoManager.popUndo()
        undoManager.confirmUndo(undone!!)
        
        // Record action 3 (this should clear redo)
        undoManager.recordAction(UndoableAction.DeleteDietItem(item3, "Meal", 0))
        
        assertFalse(undoManager.canRedo.value)
        assertEquals(2, undoManager.undoStackSize())
        
        // Last is Food3
        val last = undoManager.peekUndo() as UndoableAction.DeleteDietItem
        assertEquals("Food3", last.item.food.name)
    }

    // ============================================
    // EDGE CASE: Mixed Action Types
    // ============================================

    @Test
    fun `mixed diet and diary actions in same stack`() {
        val dietItem = createItemWithFood(1, "Meal", 0, "DietFood")
        val dailyLog = DailyLog(id = 1, foodId = 1, date = "2024-01-01", quantityGrams = 100.0, mealType = "Meal")
        val food = createFood(1, "DiaryFood")
        
        undoManager.recordAction(UndoableAction.DeleteDietItem(dietItem, "Meal", 0))
        undoManager.recordAction(UndoableAction.DeleteDailyLog(dailyLog, food, "Meal", 0))
        undoManager.recordAction(UndoableAction.UpdateDietItemPortion(1, 100.0, 150.0, "DietFood"))
        undoManager.recordAction(UndoableAction.ToggleDailyLogConsumed(1, true, "DiaryFood"))
        
        assertEquals(4, undoManager.undoStackSize())
        
        // Verify correct order and types
        assertTrue(undoManager.popUndo() is UndoableAction.ToggleDailyLogConsumed)
        assertTrue(undoManager.popUndo() is UndoableAction.UpdateDietItemPortion)
        assertTrue(undoManager.popUndo() is UndoableAction.DeleteDailyLog)
        assertTrue(undoManager.popUndo() is UndoableAction.DeleteDietItem)
    }

    // ============================================
    // EDGE CASE: Empty Batch Operations
    // ============================================

    @Test
    fun `empty batch delete creates valid action`() {
        val batchAction = UndoableAction.DeleteMultipleDietItems(emptyList())
        undoManager.recordAction(batchAction)
        
        assertEquals(1, undoManager.undoStackSize())
        val popped = undoManager.popUndo() as UndoableAction.DeleteMultipleDietItems
        assertEquals(0, popped.items.size)
    }

    // ============================================
    // EDGE CASE: State Consistency
    // ============================================

    @Test
    fun `state flows remain consistent after many operations`() {
        // Initially both should be false
        assertFalse(undoManager.canUndo.value)
        assertFalse(undoManager.canRedo.value)
        
        val item = createItemWithFood(1, "Meal", 0, "Food")
        
        // Add action - canUndo true
        undoManager.recordAction(UndoableAction.DeleteDietItem(item, "Meal", 0))
        assertTrue(undoManager.canUndo.value)
        assertFalse(undoManager.canRedo.value)
        
        // Undo - canUndo false, canRedo true
        undoManager.confirmUndo(undoManager.popUndo()!!)
        assertFalse(undoManager.canUndo.value)
        assertTrue(undoManager.canRedo.value)
        
        // Redo - canUndo true, canRedo false
        undoManager.confirmRedo(undoManager.popRedo()!!)
        assertTrue(undoManager.canUndo.value)
        assertFalse(undoManager.canRedo.value)
        
        // Clear - both false
        undoManager.clear()
        assertFalse(undoManager.canUndo.value)
        assertFalse(undoManager.canRedo.value)
    }

    @Test
    fun `lastAction is always the most recently recorded`() {
        val item1 = createItemWithFood(1, "Meal", 0, "First")
        val item2 = createItemWithFood(2, "Meal", 0, "Second")
        
        assertNull(undoManager.lastAction.value)
        
        val action1 = UndoableAction.DeleteDietItem(item1, "Meal", 0)
        undoManager.recordAction(action1)
        assertEquals(action1, undoManager.lastAction.value)
        
        val action2 = UndoableAction.DeleteDietItem(item2, "Meal", 0)
        undoManager.recordAction(action2)
        assertEquals(action2, undoManager.lastAction.value)
        
        // Undo doesn't change lastAction
        undoManager.popUndo()
        assertEquals(action2, undoManager.lastAction.value)
    }

    // ============================================
    // EDGE CASE: Extreme Portion Values
    // ============================================

    @Test
    fun `portion changes with extreme values`() {
        val zeroAction = UndoableAction.UpdateDietItemPortion(1, 0.0, 0.001, "Food")
        undoManager.recordAction(zeroAction)
        
        val largeAction = UndoableAction.UpdateDietItemPortion(2, 1.0, 999999.99, "Food")
        undoManager.recordAction(largeAction)
        
        val negativeAction = UndoableAction.UpdateDietItemPortion(3, 100.0, -50.0, "Food")
        undoManager.recordAction(negativeAction)
        
        assertEquals(3, undoManager.undoStackSize())
        
        val popped = undoManager.popUndo() as UndoableAction.UpdateDietItemPortion
        assertEquals(-50.0, popped.newQuantity, 0.001)
    }

    // ============================================
    // EDGE CASE: Special Characters in Names
    // ============================================

    @Test
    fun `action descriptions handle special characters`() {
        val item = createItemWithFood(1, "Café da Manhã", 0, "Pão 100% Integral (Açúcar)")
        val action = UndoableAction.DeleteDietItem(item, "Café da Manhã", 0)
        
        assertEquals("Remover Pão 100% Integral (Açúcar)", action.description)
    }

    @Test
    fun `action descriptions handle empty names`() {
        val item = DietItemWithFood(
            dietItem = createDietItem(1, "Meal", 0),
            food = createFood(1, "")
        )
        val action = UndoableAction.DeleteDietItem(item, "Meal", 0)
        
        assertEquals("Remover ", action.description)
    }

    // ============================================
    // STRESS TEST: Undo All Then Redo All
    // ============================================

    @Test
    fun `undo all then redo all preserves full history`() {
        // Record 5 actions (max stack size)
        for (i in 1..5) {
            val item = createItemWithFood(i, "Meal", 0, "Food$i")
            undoManager.recordAction(UndoableAction.DeleteDietItem(item, "Meal", 0))
        }
        
        // Undo all
        val undoneActions = mutableListOf<UndoableAction>()
        while (undoManager.canUndo.value) {
            val action = undoManager.popUndo()!!
            undoneActions.add(action)
            undoManager.confirmUndo(action)
        }
        
        assertEquals(5, undoneActions.size)
        assertEquals(0, undoManager.undoStackSize())
        assertEquals(5, undoManager.redoStackSize())
        
        // Redo all
        val redoneActions = mutableListOf<UndoableAction>()
        while (undoManager.canRedo.value) {
            val action = undoManager.popRedo()!!
            redoneActions.add(action)
            undoManager.confirmRedo(action)
        }
        
        assertEquals(5, redoneActions.size)
        assertEquals(5, undoManager.undoStackSize())
        assertEquals(0, undoManager.redoStackSize())
        
        // Redo should be in reverse order of undo
        assertEquals("Food1", (redoneActions[0] as UndoableAction.DeleteDietItem).item.food.name)
        assertEquals("Food5", (redoneActions[4] as UndoableAction.DeleteDietItem).item.food.name)
    }
}
