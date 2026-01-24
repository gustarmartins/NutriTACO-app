package com.mekki.taco.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.mekki.taco.data.db.entity.Diet
import com.mekki.taco.data.model.DietWithItems
import kotlinx.coroutines.flow.Flow

@Dao
interface DietDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrReplaceDiet(diet: Diet): Long

    @Update
    suspend fun updateDiet(diet: Diet)

    @Query("UPDATE diets SET name = :novoNome WHERE id = :dietId")
    suspend fun updateDietName(dietId: Int, novoNome: String)

    @Delete
    suspend fun deleteDiet(diet: Diet)

    @Query("SELECT * FROM diets WHERE id = :id")
    fun getDietById(id: Int): Flow<Diet?>

    @Query("SELECT * FROM diets ORDER BY isMain DESC, creationDate DESC")
    fun getAllDiets(): Flow<List<Diet>>

    @Query("SELECT * FROM diets WHERE name LIKE '%' || :nomeBusca || '%' ORDER BY name ASC")
    fun searchDietsByName(nomeBusca: String): Flow<List<Diet>>

    @Query("DELETE FROM diets")
    suspend fun deleteAllDiets()

    @Transaction
    @Query("SELECT * FROM diets WHERE id = :dietId")
    fun getDietWithItemsById(dietId: Int): Flow<DietWithItems?>

    @Transaction
    @Query("SELECT * FROM diets ORDER BY isMain DESC, creationDate DESC LIMIT 1")
    fun getLatestDietWithItems(): Flow<DietWithItems?>

    @Query("UPDATE diets SET isMain = 0")
    suspend fun clearAllMainDiets()

    @Query("UPDATE diets SET isMain = 1 WHERE id = :dietId")
    suspend fun setDietMain(dietId: Int)

    @Transaction
    suspend fun setAsMainDiet(dietId: Int) {
        clearAllMainDiets()
        setDietMain(dietId)
    }

    @Transaction
    @Query("SELECT * FROM diets ORDER BY isMain DESC, creationDate DESC")
    fun getAllDietsWithItems(): Flow<List<DietWithItems>>

    @Query("SELECT * FROM diets")
    suspend fun getAllDietsList(): List<Diet>
}