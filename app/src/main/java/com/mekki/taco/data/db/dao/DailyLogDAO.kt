package com.mekki.taco.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.mekki.taco.data.db.entity.DailyLog
import com.mekki.taco.data.model.DailyLogWithFood
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyLogDao {

    @Transaction
    @Query("SELECT * FROM daily_log WHERE date = :date ORDER BY entryTimestamp ASC")
    fun getLogsForDate(date: String): Flow<List<DailyLogWithFood>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: DailyLog)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(logs: List<DailyLog>)

    @Update
    suspend fun updateLog(log: DailyLog)

    @Delete
    suspend fun deleteLog(log: DailyLog)

    @Query("DELETE FROM daily_log WHERE date = :date")
    suspend fun clearDay(date: String)

    @Query("SELECT * FROM daily_log")
    suspend fun getAllLogs(): List<DailyLog>

    @Query("DELETE FROM daily_log")
    suspend fun deleteAllLogs()
}