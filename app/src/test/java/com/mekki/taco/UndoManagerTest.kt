package com.mekki.taco

import com.mekki.taco.data.db.entity.DietItem
import com.mekki.taco.data.db.entity.Food
import com.mekki.taco.data.model.DietItemWithFood
import com.mekki.taco.presentation.undo.UndoManager
import com.mekki.taco.presentation.undo.UndoableAction
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Comprehensive unit tests for UndoManager.
 * 
 * Tests cover:
 * - Basic undo/redo operations
 * - Stack behavior (LIFO)
 * - Multiple sequential operations
 * - Max stack size enforcement
 * - Clear functionality
 * - State flows (canUndo, canRedo)
 * - Edge cases
 */
class UndoManagerTest {

    private lateinit var undoManager: UndoManager

    // --- Test Data Helpers ---

    private fun createDietItem(id: Int, mealType: String, sortOrder: Int) = DietItem(
        id = id,
        dietId = 1,
        foodId = id,
        quantityGrams = 100.0,
        mealType = mealType,
        sortOrder = sortOrder
    )

    private fun createFood(id: Int, name: String) = Food(
        id = id,
        tacoID = "TEST-$id",
        name = name,
        category = "Test",
        isCustom = false,
        proteina = 10.0,
        carboidratos = 20.0,
        energiaKcal = 100.0,
        energiaKj = null,
        colesterol = null,
        cinzas = null,
        calcio = null,
        magnesio = null,
        manganes = null,
        fosforo = null,
        ferro = null,
        sodio = null,
        potassio = null,
        cobre = null,
        zinco = null,
        retinol = null,
        RE = null,
        RAE = null,
        tiamina = null,
        riboflavina = null,
        piridoxina = null,
        niacina = null,
        vitaminaC = null,
        umidade = null,
        fibraAlimentar = null,
        lipidios = null,
        aminoacidos = null
    )

    private fun createItemWithFood(id: Int, mealType: String, sortOrder: Int, name: String) =
        DietItemWithFood(
            dietItem = createDietItem(id, mealType, sortOrder),
            food = createFood(id, name)
        )

    @Before
    fun setup() {
        undoManager = UndoManager(maxStackSize = 10)
    }

    // ============================================
    // BASIC UNDO/REDO TESTS
    // ============================================

    @Test
    fun `initial state - canUndo is false`() {
        assertFalse(undoManager.canUndo.value)
    }

    @Test
    fun `initial state - canRedo is false`() {
        assertFalse(undoManager.canRedo.value)
    }

    @Test
    fun `recordAction - canUndo becomes true`() {
        val item = createItemWithFood(1, "Café da Manhã", 0, "Pão")
        val action = UndoableAction.DeleteDietItem(item, "Café da Manhã", 0)
        
        undoManager.recordAction(action)
        
        assertTrue(undoManager.canUndo.value)
    }

    @Test
    fun `recordAction - undoStackSize increases`() {
        val item = createItemWithFood(1, "Café da Manhã", 0, "Pão")
        val action = UndoableAction.DeleteDietItem(item, "Café da Manhã", 0)
        
        assertEquals(0, undoManager.undoStackSize())
        undoManager.recordAction(action)
        assertEquals(1, undoManager.undoStackSize())
    }

    @Test
    fun `peekUndo - returns last action without removing`() {
        val item = createItemWithFood(1, "Café da Manhã", 0, "Pão")
        val action = UndoableAction.DeleteDietItem(item, "Café da Manhã", 0)
        
        undoManager.recordAction(action)
        
        val peeked = undoManager.peekUndo()
        assertEquals(action, peeked)
        assertEquals(1, undoManager.undoStackSize()) // Still there
    }

    @Test
    fun `popUndo - returns and removes last action`() {
        val item = createItemWithFood(1, "Café da Manhã", 0, "Pão")
        val action = UndoableAction.DeleteDietItem(item, "Café da Manhã", 0)
        
        undoManager.recordAction(action)
        
        val popped = undoManager.popUndo()
        assertEquals(action, popped)
        assertEquals(0, undoManager.undoStackSize())
        assertFalse(undoManager.canUndo.value)
    }

    @Test
    fun `confirmUndo - moves action to redo stack`() {
        val item = createItemWithFood(1, "Café da Manhã", 0, "Pão")
        val action = UndoableAction.DeleteDietItem(item, "Café da Manhã", 0)
        
        undoManager.recordAction(action)
        val popped = undoManager.popUndo()!!
        undoManager.confirmUndo(popped)
        
        assertTrue(undoManager.canRedo.value)
        assertEquals(1, undoManager.redoStackSize())
    }

    @Test
    fun `popRedo - returns action from redo stack`() {
        val item = createItemWithFood(1, "Café da Manhã", 0, "Pão")
        val action = UndoableAction.DeleteDietItem(item, "Café da Manhã", 0)
        
        undoManager.recordAction(action)
        undoManager.confirmUndo(undoManager.popUndo()!!)
        
        val redoAction = undoManager.popRedo()
        assertEquals(action, redoAction)
        assertFalse(undoManager.canRedo.value)
    }

    @Test
    fun `confirmRedo - moves action back to undo stack`() {
        val item = createItemWithFood(1, "Café da Manhã", 0, "Pão")
        val action = UndoableAction.DeleteDietItem(item, "Café da Manhã", 0)
        
        undoManager.recordAction(action)
        undoManager.confirmUndo(undoManager.popUndo()!!)
        undoManager.confirmRedo(undoManager.popRedo()!!)
        
        assertTrue(undoManager.canUndo.value)
        assertEquals(1, undoManager.undoStackSize())
    }

    // ============================================
    // STACK BEHAVIOR (LIFO) TESTS
    // ============================================

    @Test
    fun `multiple actions - undo in reverse order (LIFO)`() {
        val item1 = createItemWithFood(1, "Café da Manhã", 0, "Pão")
        val item2 = createItemWithFood(2, "Café da Manhã", 1, "Manteiga")
        val item3 = createItemWithFood(3, "Almoço", 0, "Arroz")
        
        val action1 = UndoableAction.DeleteDietItem(item1, "Café da Manhã", 0)
        val action2 = UndoableAction.DeleteDietItem(item2, "Café da Manhã", 1)
        val action3 = UndoableAction.DeleteDietItem(item3, "Almoço", 0)
        
        undoManager.recordAction(action1)
        undoManager.recordAction(action2)
        undoManager.recordAction(action3)
        
        assertEquals(3, undoManager.undoStackSize())
        
        // Undo should return in reverse order
        assertEquals(action3, undoManager.popUndo())
        assertEquals(action2, undoManager.popUndo())
        assertEquals(action1, undoManager.popUndo())
        assertNull(undoManager.popUndo()) // Stack empty
    }

    @Test
    fun `new action clears redo stack`() {
        val item1 = createItemWithFood(1, "Café da Manhã", 0, "Pão")
        val item2 = createItemWithFood(2, "Café da Manhã", 1, "Manteiga")
        
        val action1 = UndoableAction.DeleteDietItem(item1, "Café da Manhã", 0)
        val action2 = UndoableAction.DeleteDietItem(item2, "Café da Manhã", 1)
        
        // Record and undo action1
        undoManager.recordAction(action1)
        undoManager.confirmUndo(undoManager.popUndo()!!)
        assertTrue(undoManager.canRedo.value)
        
        // Record new action - should clear redo
        undoManager.recordAction(action2)
        assertFalse(undoManager.canRedo.value)
        assertEquals(0, undoManager.redoStackSize())
    }

    // ============================================
    // COMPLEX SEQUENCES TESTS
    // ============================================

    @Test
    fun `complex sequence - delete, undo, redo, undo`() {
        val item = createItemWithFood(1, "Café da Manhã", 0, "Pão")
        val action = UndoableAction.DeleteDietItem(item, "Café da Manhã", 0)
        
        // 1. Delete
        undoManager.recordAction(action)
        assertEquals(1, undoManager.undoStackSize())
        assertEquals(0, undoManager.redoStackSize())
        
        // 2. Undo
        undoManager.confirmUndo(undoManager.popUndo()!!)
        assertEquals(0, undoManager.undoStackSize())
        assertEquals(1, undoManager.redoStackSize())
        
        // 3. Redo
        undoManager.confirmRedo(undoManager.popRedo()!!)
        assertEquals(1, undoManager.undoStackSize())
        assertEquals(0, undoManager.redoStackSize())
        
        // 4. Undo again
        undoManager.confirmUndo(undoManager.popUndo()!!)
        assertEquals(0, undoManager.undoStackSize())
        assertEquals(1, undoManager.redoStackSize())
    }

    @Test
    fun `multiple undos then redos preserve order`() {
        val item1 = createItemWithFood(1, "Café da Manhã", 0, "Pão")
        val item2 = createItemWithFood(2, "Café da Manhã", 1, "Manteiga")
        val item3 = createItemWithFood(3, "Almoço", 0, "Arroz")
        
        val action1 = UndoableAction.DeleteDietItem(item1, "Café da Manhã", 0)
        val action2 = UndoableAction.DeleteDietItem(item2, "Café da Manhã", 1)
        val action3 = UndoableAction.DeleteDietItem(item3, "Almoço", 0)
        
        undoManager.recordAction(action1)
        undoManager.recordAction(action2)
        undoManager.recordAction(action3)
        
        // Undo all 3
        undoManager.confirmUndo(undoManager.popUndo()!!) // action3
        undoManager.confirmUndo(undoManager.popUndo()!!) // action2
        undoManager.confirmUndo(undoManager.popUndo()!!) // action1
        
        assertEquals(0, undoManager.undoStackSize())
        assertEquals(3, undoManager.redoStackSize())
        
        // Redo should return action1 first, then action2, then action3
        assertEquals(action1, undoManager.popRedo())
        assertEquals(action2, undoManager.popRedo())
        assertEquals(action3, undoManager.popRedo())
    }

    // ============================================
    // MAX STACK SIZE TESTS
    // ============================================

    @Test
    fun `max stack size - old actions dropped when exceeded`() {
        val manager = UndoManager(maxStackSize = 5)
        
        // Add 7 actions
        for (i in 1..7) {
            val item = createItemWithFood(i, "Café da Manhã", i, "Food $i")
            manager.recordAction(UndoableAction.DeleteDietItem(item, "Café da Manhã", i))
        }
        
        // Should only have 5 (max size)
        assertEquals(5, manager.undoStackSize())
        
        // Oldest (1, 2) should be dropped, newest should be there
        val last = manager.peekUndo() as UndoableAction.DeleteDietItem
        assertEquals("Food 7", last.item.food.name)
    }

    @Test
    fun `max stack size - redo stack unbounded by record limit`() {
        val manager = UndoManager(maxStackSize = 3)
        
        // Add 3 actions
        for (i in 1..3) {
            val item = createItemWithFood(i, "Café da Manhã", i, "Food $i")
            manager.recordAction(UndoableAction.DeleteDietItem(item, "Café da Manhã", i))
        }
        
        // Undo all 3
        manager.confirmUndo(manager.popUndo()!!)
        manager.confirmUndo(manager.popUndo()!!)
        manager.confirmUndo(manager.popUndo()!!)
        
        // Redo stack has 3 items
        assertEquals(3, manager.redoStackSize())
    }

    // ============================================
    // CLEAR TESTS
    // ============================================

    @Test
    fun `clear - empties both stacks`() {
        val item = createItemWithFood(1, "Café da Manhã", 0, "Pão")
        val action = UndoableAction.DeleteDietItem(item, "Café da Manhã", 0)
        
        undoManager.recordAction(action)
        undoManager.confirmUndo(undoManager.popUndo()!!)
        undoManager.recordAction(action) // Add to undo again
        
        undoManager.clear()
        
        assertEquals(0, undoManager.undoStackSize())
        assertEquals(0, undoManager.redoStackSize())
        assertFalse(undoManager.canUndo.value)
        assertFalse(undoManager.canRedo.value)
    }

    // ============================================
    // DIFFERENT ACTION TYPES TESTS
    // ============================================

    @Test
    fun `different action types - all work with stack`() {
        val item = createItemWithFood(1, "Café da Manhã", 0, "Pão")
        
        val deleteAction = UndoableAction.DeleteDietItem(item, "Café da Manhã", 0)
        val moveAction = UndoableAction.MoveDietItem(item, "Café da Manhã", 0, "Almoço", 0)
        val portionAction = UndoableAction.UpdateDietItemPortion(1, 100.0, 150.0, "Pão")
        val timeAction = UndoableAction.UpdateDietItemTime(1, "08:00", "09:00", "Pão")
        
        undoManager.recordAction(deleteAction)
        undoManager.recordAction(moveAction)
        undoManager.recordAction(portionAction)
        undoManager.recordAction(timeAction)
        
        assertEquals(4, undoManager.undoStackSize())
        
        // Verify correct order
        assertTrue(undoManager.popUndo() is UndoableAction.UpdateDietItemTime)
        assertTrue(undoManager.popUndo() is UndoableAction.UpdateDietItemPortion)
        assertTrue(undoManager.popUndo() is UndoableAction.MoveDietItem)
        assertTrue(undoManager.popUndo() is UndoableAction.DeleteDietItem)
    }

    @Test
    fun `batch delete action - stores all items`() {
        val item1 = createItemWithFood(1, "Café da Manhã", 0, "Pão")
        val item2 = createItemWithFood(2, "Café da Manhã", 1, "Manteiga")
        val item3 = createItemWithFood(3, "Almoço", 0, "Arroz")
        
        val items = listOf(
            Triple(item1, "Café da Manhã", 0),
            Triple(item2, "Café da Manhã", 1),
            Triple(item3, "Almoço", 0)
        )
        
        val batchAction = UndoableAction.DeleteMultipleDietItems(items)
        undoManager.recordAction(batchAction)
        
        val popped = undoManager.popUndo() as UndoableAction.DeleteMultipleDietItems
        assertEquals(3, popped.items.size)
        assertEquals("Pão", popped.items[0].first.food.name)
        assertEquals("Manteiga", popped.items[1].first.food.name)
        assertEquals("Arroz", popped.items[2].first.food.name)
    }

    // ============================================
    // EDGE CASES
    // ============================================

    @Test
    fun `popUndo on empty stack returns null`() {
        assertNull(undoManager.popUndo())
    }

    @Test
    fun `popRedo on empty stack returns null`() {
        assertNull(undoManager.popRedo())
    }

    @Test
    fun `peekUndo on empty stack returns null`() {
        assertNull(undoManager.peekUndo())
    }

    @Test
    fun `peekRedo on empty stack returns null`() {
        assertNull(undoManager.peekRedo())
    }

    @Test
    fun `lastAction updates on record`() {
        val item = createItemWithFood(1, "Café da Manhã", 0, "Pão")
        val action = UndoableAction.DeleteDietItem(item, "Café da Manhã", 0)
        
        assertNull(undoManager.lastAction.value)
        undoManager.recordAction(action)
        assertEquals(action, undoManager.lastAction.value)
    }

    @Test
    fun `action descriptions are correct`() {
        val item = createItemWithFood(1, "Café da Manhã", 0, "Pão")
        
        val delete = UndoableAction.DeleteDietItem(item, "Café da Manhã", 0)
        assertEquals("Remover Pão", delete.description)
        
        val move = UndoableAction.MoveDietItem(item, "Café da Manhã", 0, "Almoço", 0)
        assertEquals("Mover Pão", move.description)
        
        val portion = UndoableAction.UpdateDietItemPortion(1, 100.0, 150.0, "Pão")
        assertEquals("Alterar porção de Pão", portion.description)
        
        val batch = UndoableAction.DeleteMultipleDietItems(
            listOf(Triple(item, "Café da Manhã", 0))
        )
        assertEquals("Remover 1 itens", batch.description)
    }
}
