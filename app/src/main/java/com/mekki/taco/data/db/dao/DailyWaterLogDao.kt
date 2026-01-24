package com.mekki.taco.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mekki.taco.data.db.entity.DailyWaterLog
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyWaterLogDao {
    @Query("SELECT * FROM daily_water WHERE date = :date")
    fun getWaterLog(date: String): Flow<DailyWaterLog?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(log: DailyWaterLog)

    @Query("SELECT * FROM daily_water")
    suspend fun getAllWaterLogs(): List<DailyWaterLog>

    @Query("DELETE FROM daily_water")
    suspend fun deleteAllWaterLogs()
}
