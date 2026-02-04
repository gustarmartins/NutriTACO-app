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
 * Integration tests for UndoManager with Diet operations.
 * 
 * These tests simulate real-world scenarios of diet item manipulation
 * with undo/redo operations to ensure the system is bulletproof.
 */
class UndoManagerIntegrationTest {

    private lateinit var undoManager: UndoManager
    private lateinit var dietItems: MutableMap<String, MutableList<DietItemWithFood>>

    // --- Test Data Helpers ---

    private fun createDietItem(id: Int, mealType: String, sortOrder: Int, quantity: Double = 100.0) = DietItem(
        id = id,
        dietId = 1,
        foodId = id,
        quantityGrams = quantity,
        mealType = mealType,
        sortOrder = sortOrder,
        consumptionTime = "12:00"
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
        energiaKj = null, colesterol = null, cinzas = null, calcio = null,
        magnesio = null, manganes = null, fosforo = null, ferro = null,
        sodio = null, potassio = null, cobre = null, zinco = null,
        retinol = null, RE = null, RAE = null, tiamina = null,
        riboflavina = null, piridoxina = null, niacina = null,
        vitaminaC = null, umidade = null, fibraAlimentar = null,
        lipidios = null, aminoacidos = null
    )

    private fun createItemWithFood(id: Int, mealType: String, sortOrder: Int, name: String, qty: Double = 100.0) =
        DietItemWithFood(
            dietItem = createDietItem(id, mealType, sortOrder, qty),
            food = createFood(id, name)
        )

    @Before
    fun setup() {
        undoManager = UndoManager()
        
        // Setup a typical diet structure
        dietItems = mutableMapOf(
            "Café da Manhã" to mutableListOf(
                createItemWithFood(1, "Café da Manhã", 0, "Pão"),
                createItemWithFood(2, "Café da Manhã", 1, "Manteiga"),
                createItemWithFood(3, "Café da Manhã", 2, "Café")
            ),
            "Almoço" to mutableListOf(
                createItemWithFood(4, "Almoço", 0, "Arroz"),
                createItemWithFood(5, "Almoço", 1, "Feijão"),
                createItemWithFood(6, "Almoço", 2, "Frango")
            ),
            "Jantar" to mutableListOf(
                createItemWithFood(7, "Jantar", 0, "Salada"),
                createItemWithFood(8, "Jantar", 1, "Peixe")
            )
        )
    }

    // Helper to simulate delete operation
    private fun deleteItem(mealType: String, index: Int): DietItemWithFood {
        val items = dietItems[mealType]!!
        val item = items[index]
        
        undoManager.recordAction(UndoableAction.DeleteDietItem(item, mealType, index))
        items.removeAt(index)
        
        // Recalculate sortOrder
        items.forEachIndexed { i, it ->
            val newItem = it.copy(dietItem = it.dietItem.copy(sortOrder = i))
            items[i] = newItem
        }
        
        return item
    }

    // Helper to restore item (undo delete)
    private fun restoreItem(item: DietItemWithFood, mealType: String, index: Int) {
        val items = dietItems.getOrPut(mealType) { mutableListOf() }
        val insertIndex = minOf(index, items.size)
        items.add(insertIndex, item)
        
        // Recalculate sortOrder
        items.forEachIndexed { i, it ->
            val newItem = it.copy(dietItem = it.dietItem.copy(sortOrder = i))
            items[i] = newItem
        }
    }

    // Helper to move item
    private fun moveItem(fromMeal: String, fromIndex: Int, toMeal: String, toIndex: Int) {
        val fromItems = dietItems[fromMeal]!!
        val item = fromItems[fromIndex]
        
        undoManager.recordAction(
            UndoableAction.MoveDietItem(item, fromMeal, fromIndex, toMeal, toIndex)
        )
        
        fromItems.removeAt(fromIndex)
        val toItems = dietItems.getOrPut(toMeal) { mutableListOf() }
        val movedItem = item.copy(dietItem = item.dietItem.copy(mealType = toMeal))
        toItems.add(toIndex, movedItem)
        
        // Recalculate sortOrder for both meals
        fromItems.forEachIndexed { i, it ->
            fromItems[i] = it.copy(dietItem = it.dietItem.copy(sortOrder = i))
        }
        toItems.forEachIndexed { i, it ->
            toItems[i] = it.copy(dietItem = it.dietItem.copy(sortOrder = i))
        }
    }

    // ============================================
    // SCENARIO 1: Delete and Undo Single Item
    // ============================================

    @Test
    fun `delete single item - undo restores item at correct position`() {
        assertEquals(3, dietItems["Café da Manhã"]!!.size)
        assertEquals("Manteiga", dietItems["Café da Manhã"]!![1].food.name)
        
        // Delete "Manteiga" (index 1)
        val deleted = deleteItem("Café da Manhã", 1)
        
        assertEquals(2, dietItems["Café da Manhã"]!!.size)
        assertEquals("Pão", dietItems["Café da Manhã"]!![0].food.name)
        assertEquals("Café", dietItems["Café da Manhã"]!![1].food.name)
        
        // Undo
        val action = undoManager.popUndo() as UndoableAction.DeleteDietItem
        restoreItem(action.item, action.mealType, action.index)
        undoManager.confirmUndo(action)
        
        // Verify restored at position 1
        assertEquals(3, dietItems["Café da Manhã"]!!.size)
        assertEquals("Pão", dietItems["Café da Manhã"]!![0].food.name)
        assertEquals("Manteiga", dietItems["Café da Manhã"]!![1].food.name)
        assertEquals("Café", dietItems["Café da Manhã"]!![2].food.name)
    }

    @Test
    fun `delete first item - undo restores at position 0`() {
        deleteItem("Café da Manhã", 0)
        
        assertEquals(2, dietItems["Café da Manhã"]!!.size)
        assertEquals("Manteiga", dietItems["Café da Manhã"]!![0].food.name)
        
        val action = undoManager.popUndo() as UndoableAction.DeleteDietItem
        restoreItem(action.item, action.mealType, action.index)
        
        assertEquals(3, dietItems["Café da Manhã"]!!.size)
        assertEquals("Pão", dietItems["Café da Manhã"]!![0].food.name)
    }

    @Test
    fun `delete last item - undo restores at end`() {
        deleteItem("Café da Manhã", 2)
        
        assertEquals(2, dietItems["Café da Manhã"]!!.size)
        
        val action = undoManager.popUndo() as UndoableAction.DeleteDietItem
        restoreItem(action.item, action.mealType, action.index)
        
        assertEquals(3, dietItems["Café da Manhã"]!!.size)
        assertEquals("Café", dietItems["Café da Manhã"]!![2].food.name)
    }

    // ============================================
    // SCENARIO 2: Multiple Deletes Then Undos
    // ============================================

    @Test
    fun `delete multiple items - undo in reverse order restores correctly`() {
        // Delete items in sequence: Manteiga, Pão, Café
        deleteItem("Café da Manhã", 1) // Manteiga
        deleteItem("Café da Manhã", 0) // Pão (now at 0)
        deleteItem("Café da Manhã", 0) // Café (now at 0)
        
        assertEquals(0, dietItems["Café da Manhã"]!!.size)
        assertEquals(3, undoManager.undoStackSize())
        
        // Undo all in reverse order
        // First undo: restore Café
        var action = undoManager.popUndo() as UndoableAction.DeleteDietItem
        assertEquals("Café", action.item.food.name)
        restoreItem(action.item, action.mealType, action.index)
        undoManager.confirmUndo(action)
        
        assertEquals(1, dietItems["Café da Manhã"]!!.size)
        assertEquals("Café", dietItems["Café da Manhã"]!![0].food.name)
        
        // Second undo: restore Pão
        action = undoManager.popUndo() as UndoableAction.DeleteDietItem
        assertEquals("Pão", action.item.food.name)
        restoreItem(action.item, action.mealType, action.index)
        undoManager.confirmUndo(action)
        
        assertEquals(2, dietItems["Café da Manhã"]!!.size)
        
        // Third undo: restore Manteiga
        action = undoManager.popUndo() as UndoableAction.DeleteDietItem
        assertEquals("Manteiga", action.item.food.name)
        restoreItem(action.item, action.mealType, action.index)
        undoManager.confirmUndo(action)
        
        // All items restored in original order
        assertEquals(3, dietItems["Café da Manhã"]!!.size)
        assertEquals("Pão", dietItems["Café da Manhã"]!![0].food.name)
        assertEquals("Manteiga", dietItems["Café da Manhã"]!![1].food.name)
        assertEquals("Café", dietItems["Café da Manhã"]!![2].food.name)
    }

    // ============================================
    // SCENARIO 3: Cross-Meal Move Operations
    // ============================================

    @Test
    fun `move item between meals - undo restores original meal`() {
        assertEquals(3, dietItems["Café da Manhã"]!!.size)
        assertEquals(3, dietItems["Almoço"]!!.size)
        
        // Move "Manteiga" from Breakfast to Lunch at position 1
        moveItem("Café da Manhã", 1, "Almoço", 1)
        
        assertEquals(2, dietItems["Café da Manhã"]!!.size)
        assertEquals(4, dietItems["Almoço"]!!.size)
        assertEquals("Manteiga", dietItems["Almoço"]!![1].food.name)
        
        // Undo the move
        val action = undoManager.popUndo() as UndoableAction.MoveDietItem
        
        // Reverse the move
        val toItems = dietItems[action.toMeal]!!
        val fromItems = dietItems[action.fromMeal]!!
        val item = toItems.removeAt(action.toIndex)
        val restoredItem = item.copy(dietItem = item.dietItem.copy(mealType = action.fromMeal))
        fromItems.add(action.fromIndex, restoredItem)
        
        // Recalculate sortOrder
        toItems.forEachIndexed { i, it -> toItems[i] = it.copy(dietItem = it.dietItem.copy(sortOrder = i)) }
        fromItems.forEachIndexed { i, it -> fromItems[i] = it.copy(dietItem = it.dietItem.copy(sortOrder = i)) }
        
        undoManager.confirmUndo(action)
        
        // Verify restored
        assertEquals(3, dietItems["Café da Manhã"]!!.size)
        assertEquals(3, dietItems["Almoço"]!!.size)
        assertEquals("Manteiga", dietItems["Café da Manhã"]!![1].food.name)
    }

    // ============================================
    // SCENARIO 4: Portion Change Operations
    // ============================================

    @Test
    fun `portion change - undo restores original quantity`() {
        val item = dietItems["Café da Manhã"]!![0]
        val oldQty = item.dietItem.quantityGrams
        val newQty = 150.0
        
        // Record portion change
        undoManager.recordAction(
            UndoableAction.UpdateDietItemPortion(
                itemId = item.dietItem.id,
                oldQuantity = oldQty,
                newQuantity = newQty,
                foodName = item.food.name
            )
        )
        
        // Simulate update
        val updatedItem = item.copy(dietItem = item.dietItem.copy(quantityGrams = newQty))
        dietItems["Café da Manhã"]!![0] = updatedItem
        
        assertEquals(150.0, dietItems["Café da Manhã"]!![0].dietItem.quantityGrams, 0.01)
        
        // Undo
        val action = undoManager.popUndo() as UndoableAction.UpdateDietItemPortion
        val restoredItem = dietItems["Café da Manhã"]!![0].copy(
            dietItem = dietItems["Café da Manhã"]!![0].dietItem.copy(quantityGrams = action.oldQuantity)
        )
        dietItems["Café da Manhã"]!![0] = restoredItem
        undoManager.confirmUndo(action)
        
        assertEquals(100.0, dietItems["Café da Manhã"]!![0].dietItem.quantityGrams, 0.01)
    }

    // ============================================
    // SCENARIO 5: Complex Multi-Operation Sequence
    // ============================================

    @Test
    fun `complex sequence - delete, move, change portion, undo all`() {
        // 1. Delete Café from Breakfast
        deleteItem("Café da Manhã", 2)
        assertEquals(2, dietItems["Café da Manhã"]!!.size)
        
        // 2. Move Manteiga to Lunch
        moveItem("Café da Manhã", 1, "Almoço", 0)
        assertEquals(1, dietItems["Café da Manhã"]!!.size)
        assertEquals(4, dietItems["Almoço"]!!.size)
        
        // 3. Change Arroz portion
        val arroz = dietItems["Almoço"]!![1] // Arroz now at index 1
        undoManager.recordAction(
            UndoableAction.UpdateDietItemPortion(
                itemId = arroz.dietItem.id,
                oldQuantity = 100.0,
                newQuantity = 200.0,
                foodName = arroz.food.name
            )
        )
        dietItems["Almoço"]!![1] = arroz.copy(dietItem = arroz.dietItem.copy(quantityGrams = 200.0))
        
        assertEquals(3, undoManager.undoStackSize())
        
        // Now undo all 3 operations in reverse order
        
        // Undo portion change
        var action = undoManager.popUndo()
        assertTrue(action is UndoableAction.UpdateDietItemPortion)
        val portionAction = action as UndoableAction.UpdateDietItemPortion
        dietItems["Almoço"]!![1] = dietItems["Almoço"]!![1].copy(
            dietItem = dietItems["Almoço"]!![1].dietItem.copy(quantityGrams = portionAction.oldQuantity)
        )
        undoManager.confirmUndo(action)
        assertEquals(100.0, dietItems["Almoço"]!![1].dietItem.quantityGrams, 0.01)
        
        // Undo move
        action = undoManager.popUndo()
        assertTrue(action is UndoableAction.MoveDietItem)
        val moveAction = action as UndoableAction.MoveDietItem
        val toItems = dietItems[moveAction.toMeal]!!
        val fromItems = dietItems[moveAction.fromMeal]!!
        val moved = toItems.removeAt(moveAction.toIndex)
        fromItems.add(moveAction.fromIndex, moved.copy(dietItem = moved.dietItem.copy(mealType = moveAction.fromMeal)))
        toItems.forEachIndexed { i, it -> toItems[i] = it.copy(dietItem = it.dietItem.copy(sortOrder = i)) }
        fromItems.forEachIndexed { i, it -> fromItems[i] = it.copy(dietItem = it.dietItem.copy(sortOrder = i)) }
        undoManager.confirmUndo(action)
        
        assertEquals(2, dietItems["Café da Manhã"]!!.size)
        assertEquals(3, dietItems["Almoço"]!!.size)
        
        // Undo delete
        action = undoManager.popUndo()
        assertTrue(action is UndoableAction.DeleteDietItem)
        val deleteAction = action as UndoableAction.DeleteDietItem
        restoreItem(deleteAction.item, deleteAction.mealType, deleteAction.index)
        undoManager.confirmUndo(action)
        
        // Verify fully restored
        assertEquals(3, dietItems["Café da Manhã"]!!.size)
        assertEquals("Pão", dietItems["Café da Manhã"]!![0].food.name)
        assertEquals("Manteiga", dietItems["Café da Manhã"]!![1].food.name)
        assertEquals("Café", dietItems["Café da Manhã"]!![2].food.name)
    }

    // ============================================
    // SCENARIO 6: Redo After Undo
    // ============================================

    @Test
    fun `redo after undo - reapplies operation`() {
        // Delete an item
        val deleted = deleteItem("Café da Manhã", 1)
        assertEquals(2, dietItems["Café da Manhã"]!!.size)
        
        // Undo
        val action = undoManager.popUndo() as UndoableAction.DeleteDietItem
        restoreItem(action.item, action.mealType, action.index)
        undoManager.confirmUndo(action)
        assertEquals(3, dietItems["Café da Manhã"]!!.size)
        
        // Redo
        val redoAction = undoManager.popRedo() as UndoableAction.DeleteDietItem
        val items = dietItems[redoAction.mealType]!!
        items.removeAt(redoAction.index)
        items.forEachIndexed { i, it -> items[i] = it.copy(dietItem = it.dietItem.copy(sortOrder = i)) }
        undoManager.confirmRedo(redoAction)
        
        assertEquals(2, dietItems["Café da Manhã"]!!.size)
        assertEquals("Pão", dietItems["Café da Manhã"]!![0].food.name)
        assertEquals("Café", dietItems["Café da Manhã"]!![1].food.name)
    }

    // ============================================
    // SCENARIO 7: Batch Delete Operations
    // ============================================

    @Test
    fun `batch delete multiple items - single undo restores all`() {
        val items = listOf(
            Triple(dietItems["Café da Manhã"]!![0], "Café da Manhã", 0),
            Triple(dietItems["Café da Manhã"]!![1], "Café da Manhã", 1),
            Triple(dietItems["Almoço"]!![0], "Almoço", 0)
        )
        
        undoManager.recordAction(UndoableAction.DeleteMultipleDietItems(items))
        
        // Perform batch delete
        dietItems["Café da Manhã"]!!.removeAt(1)
        dietItems["Café da Manhã"]!!.removeAt(0)
        dietItems["Almoço"]!!.removeAt(0)
        
        assertEquals(1, dietItems["Café da Manhã"]!!.size)
        assertEquals(2, dietItems["Almoço"]!!.size)
        
        // Single undo restores all
        val action = undoManager.popUndo() as UndoableAction.DeleteMultipleDietItems
        
        // Restore in reverse order
        action.items.reversed().forEach { (item, mealType, index) ->
            restoreItem(item, mealType, index)
        }
        undoManager.confirmUndo(action)
        
        assertEquals(3, dietItems["Café da Manhã"]!!.size)
        assertEquals(3, dietItems["Almoço"]!!.size)
    }

    // ============================================
    // SCENARIO 8: Edge Cases
    // ============================================

    @Test
    fun `undo empty meal then re-add - meal recreated correctly`() {
        // Delete all from Jantar
        deleteItem("Jantar", 1)
        deleteItem("Jantar", 0)
        
        assertEquals(0, dietItems["Jantar"]!!.size)
        
        // Undo first delete
        var action = undoManager.popUndo() as UndoableAction.DeleteDietItem
        restoreItem(action.item, action.mealType, action.index)
        
        assertEquals(1, dietItems["Jantar"]!!.size)
        
        // Undo second delete  
        action = undoManager.popUndo() as UndoableAction.DeleteDietItem
        restoreItem(action.item, action.mealType, action.index)
        
        assertEquals(2, dietItems["Jantar"]!!.size)
    }

    @Test
    fun `action descriptions are localized correctly`() {
        val item = dietItems["Café da Manhã"]!![0]
        
        val deleteAction = UndoableAction.DeleteDietItem(item, "Café da Manhã", 0)
        assertEquals("Remover Pão", deleteAction.description)
        
        val moveAction = UndoableAction.MoveDietItem(item, "Café da Manhã", 0, "Almoço", 0)
        assertEquals("Mover Pão", moveAction.description)
        
        val portionAction = UndoableAction.UpdateDietItemPortion(1, 100.0, 150.0, "Pão")
        assertEquals("Alterar porção de Pão", portionAction.description)
    }
}
