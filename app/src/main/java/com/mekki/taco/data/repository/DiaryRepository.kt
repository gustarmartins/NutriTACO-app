package com.mekki.taco.data.repository

import com.mekki.taco.data.db.dao.DailyLogDao
import com.mekki.taco.data.db.dao.DailyWaterLogDao
import com.mekki.taco.data.db.dao.DietItemDao
import com.mekki.taco.data.db.entity.DailyLog
import com.mekki.taco.data.db.entity.DailyWaterLog
import com.mekki.taco.data.model.DailyLogWithFood
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

class DiaryRepository(
    private val dailyLogDao: DailyLogDao,
    private val dietItemDao: DietItemDao,
    private val dailyWaterLogDao: DailyWaterLogDao,
    private val foodDao: com.mekki.taco.data.db.dao.FoodDao
) {
    fun getDailyLogs(date: String): Flow<List<DailyLogWithFood>> {
        return dailyLogDao.getLogsForDate(date)
    }

    fun getWaterLog(date: String): Flow<DailyWaterLog?> {
        return dailyWaterLogDao.getWaterLog(date)
    }

    suspend fun updateWater(date: String, quantity: Int) {
        dailyWaterLogDao.insertOrUpdate(DailyWaterLog(date, quantity))
    }

    suspend fun addLog(log: DailyLog) {
        dailyLogDao.insertLog(log)
        foodDao.incrementUsageCount(log.foodId)
    }

    suspend fun importDietPlanToDate(dietId: Int, dateStr: String) {
        val planItems = dietItemDao.getDietItemsList(dietId)
        val date = try {
            LocalDate.parse(dateStr)
        } catch (e: Exception) {
            LocalDate.now()
        }

        val newLogs = planItems.map { planItem ->
            val timeStr = planItem.consumptionTime ?: "08:00"
            val time = try {
                LocalTime.parse(timeStr)
            } catch (e: Exception) {
                LocalTime.of(8, 0)
            }
            val dateTime = LocalDateTime.of(date, time)
            val timestamp = dateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

            DailyLog(
                foodId = planItem.foodId,
                date = dateStr,
                quantityGrams = planItem.quantityGrams,
                mealType = planItem.mealType ?: "Outros",
                entryTimestamp = timestamp,
                isConsumed = false,
                originalQuantityGrams = planItem.quantityGrams
            )
        }

        dailyLogDao.insertAll(newLogs)
        newLogs.forEach { log ->
            foodDao.incrementUsageCount(log.foodId)
        }
    }

    suspend fun updateTimestamp(log: DailyLog, newTimestamp: Long) {
        dailyLogDao.updateLog(log.copy(entryTimestamp = newTimestamp))
    }

    suspend fun toggleConsumed(log: DailyLog) {
        dailyLogDao.updateLog(log.copy(isConsumed = !log.isConsumed))
    }

    suspend fun updateQuantity(log: DailyLog, newQuantity: Double) {
        dailyLogDao.updateLog(log.copy(quantityGrams = newQuantity))
    }

    suspend fun updateNotes(log: DailyLog, newNotes: String) {
        dailyLogDao.updateLog(log.copy(notes = newNotes))
    }

    suspend fun deleteLog(log: DailyLog) {
        dailyLogDao.deleteLog(log)
    }
}
