package com.mekki.taco.data.manager

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.room.withTransaction
import com.google.gson.Gson
import com.mekki.taco.BuildConfig
import com.mekki.taco.data.db.database.AppDatabase
import com.mekki.taco.data.db.entity.DailyLog
import com.mekki.taco.data.db.entity.DailyWaterLog
import com.mekki.taco.data.db.entity.Diet
import com.mekki.taco.data.db.entity.DietItem
import com.mekki.taco.data.db.entity.Food
import com.mekki.taco.data.model.UserProfile
import com.mekki.taco.data.repository.UserProfileRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import java.io.InputStreamReader
import java.io.OutputStreamWriter

data class BackupData(
    val version: Int = 1, // Schema Version of Backup
    val timestamp: Long = System.currentTimeMillis(),
    val appVersionCode: Int = BuildConfig.VERSION_CODE,
    val appVersionName: String = BuildConfig.VERSION_NAME,
    val userProfile: UserProfile?,
    val customFoods: List<Food>,
    val diets: List<Diet>,
    val dietItems: List<DietItemBackup>,
    val dailyLogs: List<DailyLogBackup>,
    val waterLogs: List<DailyWaterLog>
)

// Backup versions of entities using tacoID instead of internal ID for stability
data class DietItemBackup(
    val dietId: Int,
    val foodTacoId: String,
    val quantityGrams: Double,
    val mealType: String?,
    val consumptionTime: String?
)

data class DailyLogBackup(
    val foodTacoId: String,
    val date: String,
    val quantityGrams: Double,
    val mealType: String,
    val isConsumed: Boolean,
    val originalQuantityGrams: Double?
)

class BackupManager(
    private val context: Context,
    private val db: AppDatabase,
    private val userProfileRepository: UserProfileRepository
) {
    private val gson = Gson()
    private val TAG = "BackupManager"

    suspend fun exportData(uri: Uri): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Starting export...")

            // Fetch all data
            val profile = userProfileRepository.userProfileFlow.firstOrNull()
            val customFoods = db.foodDao().getAllCustomFoods()
            val allFoods = db.foodDao().getAllFoods().first()
            val diets = db.dietDao().getAllDietsList()
            val rawDietItems = db.dietItemDao().getAllDietItems()
            val rawLogs = db.dailyLogDao().getAllLogs()
            val waterLogs = db.dailyWaterLogDao().getAllWaterLogs()

            // Build Maps for ID resolution
            val foodIdToTacoId = allFoods.associate { it.id to it.tacoID }

            // transform Data
            val dietItemsBackup = rawDietItems.mapNotNull { item ->
                val tacoId = foodIdToTacoId[item.foodId]
                if (tacoId != null) {
                    DietItemBackup(
                        dietId = item.dietId,
                        foodTacoId = tacoId,
                        quantityGrams = item.quantityGrams,
                        mealType = item.mealType,
                        consumptionTime = item.consumptionTime
                    )
                } else {
                    Log.w(TAG, "Skipping DietItem with invalid foodId: ${item.foodId}")
                    null
                }
            }

            val dailyLogsBackup = rawLogs.mapNotNull { log ->
                val tacoId = foodIdToTacoId[log.foodId]
                if (tacoId != null) {
                    DailyLogBackup(
                        foodTacoId = tacoId,
                        date = log.date,
                        quantityGrams = log.quantityGrams,
                        mealType = log.mealType,
                        isConsumed = log.isConsumed,
                        originalQuantityGrams = log.originalQuantityGrams
                    )
                } else {
                    Log.w(TAG, "Skipping DailyLog with invalid foodId: ${log.foodId}")
                    null
                }
            }

            val backup = BackupData(
                userProfile = profile,
                customFoods = customFoods,
                diets = diets,
                dietItems = dietItemsBackup,
                dailyLogs = dailyLogsBackup,
                waterLogs = waterLogs
            )

            // 4. Write to file
            val json = gson.toJson(backup)

            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                OutputStreamWriter(outputStream).use { writer ->
                    writer.write(json)
                }
            } ?: throw Exception("Could not open output stream for URI: $uri")

            Log.d(TAG, "Export completed successfully.")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Export failed", e)
            Result.failure(e)
        }
    }

    suspend fun importData(uri: Uri): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Starting import...")

            // 1. Read JSON
            val json = context.contentResolver.openInputStream(uri)?.use { inputStream ->
                InputStreamReader(inputStream).use { reader ->
                    reader.readText()
                }
            } ?: throw Exception("Could not open input stream for URI: $uri")

            val backup = gson.fromJson(json, BackupData::class.java)

            // 2. Transactional Restore
            db.withTransaction {
                // Wipe user data
                Log.d(TAG, "Wiping existing user data...")
                db.dailyLogDao().deleteAllLogs()
                db.dailyWaterLogDao().deleteAllWaterLogs()
                db.dietItemDao().deleteAllDietItems()
                db.dietDao().deleteAllDiets()
                db.foodDao().deleteCustomFoods()

                // Restore Custom Foods & Build Map (TacoID -> New ID)
                Log.d(TAG, "Restoring custom foods...")
                backup.customFoods.forEach { food ->
                    // Ensure isCustom is true just in case
                    val newFood = food.copy(id = 0, isCustom = true)
                    db.foodDao().insertFood(newFood)
                }

                // Refresh Food Map (All foods now in DB)
                // We need this to resolve tacoID -> ID for both custom and official foods
                // Since we are inside a transaction, we can query.
                val allFoods = db.foodDao().getAllFoods().first()
                val tacoIdToNewId = allFoods.associate { it.tacoID to it.id }

                // Restore Diets
                Log.d(TAG, "Restoring diets...")
                val oldDietIdToNewId = mutableMapOf<Int, Int>()
                backup.diets.forEach { diet ->
                    val oldId = diet.id
                    val newDiet = diet.copy(id = 0)
                    val newId = db.dietDao().insertOrReplaceDiet(newDiet).toInt()
                    oldDietIdToNewId[oldId] = newId
                }

                // Restore Diet Items
                Log.d(TAG, "Restoring diet items...")
                val newDietItems = backup.dietItems.mapNotNull { item ->
                    val newDietId = oldDietIdToNewId[item.dietId]
                    val newFoodId = tacoIdToNewId[item.foodTacoId]

                    if (newDietId != null && newFoodId != null) {
                        DietItem(
                            id = 0,
                            dietId = newDietId,
                            foodId = newFoodId,
                            quantityGrams = item.quantityGrams,
                            mealType = item.mealType,
                            consumptionTime = item.consumptionTime
                        )
                    } else {
                        Log.w(
                            TAG,
                            "Skipping DietItem. DietFound=${newDietId != null}, FoodFound=${newFoodId != null} (TacoID: ${item.foodTacoId})"
                        )
                        null
                    }
                }
                if (newDietItems.isNotEmpty()) {
                    db.dietItemDao().insertDietItems(newDietItems)
                }

                // Restore Logs
                Log.d(TAG, "Restoring daily logs...")
                val newLogs = backup.dailyLogs.mapNotNull { log ->
                    val newFoodId = tacoIdToNewId[log.foodTacoId]
                    if (newFoodId != null) {
                        DailyLog(
                            id = 0,
                            foodId = newFoodId,
                            date = log.date,
                            quantityGrams = log.quantityGrams,
                            mealType = log.mealType,
                            isConsumed = log.isConsumed,
                            originalQuantityGrams = log.originalQuantityGrams
                        )
                    } else {
                        Log.w(TAG, "Skipping DailyLog. Food not found: ${log.foodTacoId}")
                        null
                    }
                }
                if (newLogs.isNotEmpty()) {
                    db.dailyLogDao().insertAll(newLogs)
                }

                // Restore Water
                Log.d(TAG, "Restoring water logs...")
                backup.waterLogs.forEach {
                    db.dailyWaterLogDao().insertOrUpdate(it)
                }
            }

            // Restore Profile (outside transaction)
            if (backup.userProfile != null) {
                userProfileRepository.saveProfile(backup.userProfile)
            }

            Log.d(TAG, "Import completed successfully.")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Import failed", e)
            Result.failure(e)
        }
    }
}
