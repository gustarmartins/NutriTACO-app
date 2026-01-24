package com.mekki.taco.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.mekki.taco.data.db.entity.DietItem
import com.mekki.taco.data.model.DietItemWithFood
import kotlinx.coroutines.flow.Flow

@Dao
interface DietItemDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDietItem(dietItem: DietItem): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDietItems(itensDieta: List<DietItem>)

    @Update
    suspend fun updateDietItem(dietItem: DietItem)

    @Delete
    suspend fun deleteDietItem(dietItem: DietItem)

    @Query("SELECT * FROM diet_items WHERE dietId = :idDieta ORDER BY mealType ASC, id ASC")
    fun getDietItemsByDietId(idDieta: Int): Flow<List<DietItem>>

    @Query("SELECT * FROM diet_items WHERE id = :itemId")
    fun getDietItemById(itemId: Int): Flow<DietItem?>

    @Query("DELETE FROM diet_items WHERE dietId = :idDieta")
    suspend fun deleteAllItemsByDietId(idDieta: Int)

    @Transaction
    @Query("SELECT * FROM diet_items WHERE dietId = :idDieta")
    fun getDietItemsWithFoodByDietId(idDieta: Int): Flow<List<DietItemWithFood>>

    @Query("UPDATE diet_items SET quantityGrams = :novaQuantidade WHERE id = :itemId")
    suspend fun updateItemQuantity(itemId: Int, novaQuantidade: Double)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<DietItem>)

    @Query("SELECT * FROM diet_items WHERE dietId = :dietId")
    suspend fun getDietItemsList(dietId: Int): List<DietItem>

    @Query("SELECT * FROM diet_items")
    suspend fun getAllDietItems(): List<DietItem>

    @Query("DELETE FROM diet_items")
    suspend fun deleteAllDietItems()
}