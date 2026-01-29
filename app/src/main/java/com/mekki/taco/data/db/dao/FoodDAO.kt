package com.mekki.taco.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RawQuery
import androidx.room.Transaction
import androidx.room.Update
import androidx.sqlite.db.SupportSQLiteQuery
import com.mekki.taco.data.db.entity.Food
import com.mekki.taco.utils.normalizeForSearch
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

@Dao
abstract class FoodDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    protected abstract suspend fun insertFoodInternal(food: Food): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    protected abstract suspend fun insertAllFoodsInternal(foods: List<Food>): List<Long>

    @Update
    protected abstract suspend fun updateFoodInternal(food: Food): Int

    @Delete
    protected abstract suspend fun deleteFoodInternal(food: Food)

    @Query("DELETE FROM foods")
    protected abstract suspend fun deleteAllFoodsInternal()

    // --- FTS Maintenance ---
    @Query("INSERT INTO foods_fts (rowid, normalized_data) VALUES (:rowid, :normalized)")
    abstract suspend fun insertFts(rowid: Long, normalized: String)

    @Query("DELETE FROM foods_fts WHERE rowid = :rowid")
    abstract suspend fun deleteFts(rowid: Long)

    @Query("DELETE FROM foods_fts")
    abstract suspend fun deleteAllFts()

    // --- Public Operations (With FTS Maintenance) ---

    @Transaction
    open suspend fun insertFood(food: Food): Long {
        val id = insertFoodInternal(food)
        val normalized = food.name.normalizeForSearch()
        insertFts(id, normalized)
        return id
    }

    @Transaction
    open suspend fun insertAllFoods(foods: List<Food>) {
        val ids = insertAllFoodsInternal(foods)
        foods.forEachIndexed { index, food ->
            val id = ids[index]
            val normalized = food.name.normalizeForSearch()
            insertFts(id, normalized)
        }
    }

    @Transaction
    open suspend fun updateFood(food: Food): Int {
        val count = updateFoodInternal(food)
        if (count > 0) {
            deleteFts(food.id.toLong())
            insertFts(food.id.toLong(), food.name.normalizeForSearch())
        }
        return count
    }

    @Transaction
    open suspend fun deleteFood(food: Food) {
        deleteFoodInternal(food)
        deleteFts(food.id.toLong())
    }

    @Transaction
    open suspend fun deleteAllFoods() {
        deleteAllFoodsInternal()
        deleteAllFts()
    }

    // --- Search ---

    @Query(
        """
        SELECT foods.* FROM foods 
        JOIN foods_fts ON foods.id = foods_fts.rowid 
        WHERE foods_fts MATCH :ftsQuery
        ORDER BY 
          CASE WHEN foods.isCustom = 1 THEN 1 ELSE 0 END DESC,
          CASE 
            WHEN foods_fts.normalized_data = :normalizedQuery THEN 3
            WHEN foods_fts.normalized_data LIKE :normalizedQuery || ' %' THEN 2
            WHEN foods_fts.normalized_data LIKE :normalizedQuery || '%' THEN 1
            ELSE 0 
          END DESC,
          length(foods.name) ASC, 
          foods.usageCount DESC
    """
    )
    abstract fun searchFoodsInternal(normalizedQuery: String, ftsQuery: String): Flow<List<Food>>

    /**
     * Standard entry point for basic search.
     * Uses FTS with default prefix matching.
     */
    open fun getFoodsByName(termoBusca: String): Flow<List<Food>> {
        val normalized = termoBusca.normalizeForSearch()
        if (normalized.isBlank()) return flowOf(emptyList())

        val ftsQuery = normalized.split(" ")
            .filter { it.isNotBlank() }
            .joinToString(" ") { "$it*" }

        return searchFoodsInternal(normalized, ftsQuery)
    }

    @Query("SELECT * FROM foods WHERE name LIKE :pattern")
    abstract suspend fun findFoodsByNameLike(pattern: String): List<Food>

    @Query("SELECT * FROM foods WHERE id = :id")
    abstract fun getFoodById(id: Int): Flow<Food?>

    @Query("SELECT * FROM foods WHERE id = :id")
    abstract suspend fun getFoodByIdSync(id: Int): Food?

    @Query("SELECT * FROM foods WHERE tacoID = :tacoID")
    abstract fun getFoodByTacoID(tacoID: String): Flow<Food?>

    @Query("SELECT * FROM foods WHERE tacoID = :tacoID LIMIT 1")
    abstract suspend fun getFoodByTacoIDSuspend(tacoID: String): Food?

    @Query("SELECT * FROM foods WHERE uuid = :uuid LIMIT 1")
    abstract suspend fun getFoodByUuid(uuid: String): Food?

    /**
     * Handles the Efficient batch lookup for import operations.
     */
    @Query("SELECT * FROM foods WHERE uuid IN (:uuids)")
    abstract suspend fun getFoodsByUuids(uuids: List<String>): List<Food>

    /**
     * Used for resolving official TACO foods during import.
     */
    @Query("SELECT * FROM foods WHERE tacoID IN (:tacoIds)")
    abstract suspend fun getFoodsByTacoIds(tacoIds: List<String>): List<Food>

    /**
     * Check if a UUID already exists in the database.
     */
    @Query("SELECT EXISTS(SELECT 1 FROM foods WHERE uuid = :uuid LIMIT 1)")
    abstract suspend fun uuidExists(uuid: String): Boolean

    @Query("SELECT * FROM foods ORDER BY name ASC")
    abstract fun getAllFoods(): Flow<List<Food>>

    @RawQuery(observedEntities = [Food::class])
    abstract fun searchFoodsRaw(query: SupportSQLiteQuery): Flow<List<Food>>

    @Query("UPDATE foods SET usageCount = usageCount + 1 WHERE id = :id")
    abstract suspend fun incrementUsageCount(id: Int)

    @Query("SELECT * FROM foods WHERE category = :categoria ORDER BY name ASC")
    abstract fun getFoodsByCategory(categoria: String): Flow<List<Food>>

    @Query("SELECT DISTINCT category FROM foods ORDER BY category ASC")
    abstract fun getAllCategories(): Flow<List<String>>

    @Query("SELECT COUNT(*) FROM foods")
    abstract suspend fun countAllFoods(): Int

    @Query("SELECT * FROM foods WHERE isCustom = 1")
    abstract suspend fun getAllCustomFoods(): List<Food>

    @Query("DELETE FROM foods WHERE isCustom = 1")
    abstract suspend fun deleteCustomFoods()

    @Query(
        """
        SELECT * FROM foods 
        ORDER BY 
            usageCount DESC,
            CASE WHEN isCustom = 1 THEN 1 ELSE 0 END DESC,
            name ASC
        LIMIT :limit
        """
    )
    abstract fun getTopFoods(limit: Int = 10): Flow<List<Food>>
}