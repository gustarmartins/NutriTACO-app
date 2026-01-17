package com.mekki.taco.data.repository

import com.mekki.taco.data.db.dao.DailyLogDao
import com.mekki.taco.data.db.dao.DailyWaterLogDao
import com.mekki.taco.data.db.dao.DietItemDao
import com.mekki.taco.data.db.entity.DailyLog
import com.mekki.taco.data.db.entity.DailyWaterLog
import com.mekki.taco.data.model.DailyLogWithFood
import kotlinx.coroutines.flow.Flow

class DiaryRepository(
    private val dailyLogDao: DailyLogDao,
    private val dietItemDao: DietItemDao,
    private val dailyWaterLogDao: DailyWaterLogDao
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
    }

    suspend fun importDietPlanToDate(dietId: Int, date: String) {
        val planItems = dietItemDao.getDietItemsList(dietId)

        val newLogs = planItems.map { planItem ->
            DailyLog(
                foodId = planItem.foodId,
                date = date,
                quantityGrams = planItem.quantityGrams,
                mealType = planItem.mealType ?: "Outros",
                isConsumed = false,
                originalQuantityGrams = planItem.quantityGrams
            )
        }

        dailyLogDao.insertAll(newLogs)
    }

    suspend fun toggleConsumed(log: DailyLog) {
        dailyLogDao.updateLog(log.copy(isConsumed = !log.isConsumed))
    }

    suspend fun updateQuantity(log: DailyLog, newQuantity: Double) {
        dailyLogDao.updateLog(log.copy(quantityGrams = newQuantity))
    }

    suspend fun deleteLog(log: DailyLog) {
        dailyLogDao.deleteLog(log)
    }
}
