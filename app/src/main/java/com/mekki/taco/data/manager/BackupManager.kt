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
    val version: Int, // Schema Version of Backup
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
                version = db.openHelper.readableDatabase.version,
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

    suspend fun importData(uri: Uri, merge: Boolean = false): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Starting import... Merge=$merge")

            // 1. Read JSON
            val json = context.contentResolver.openInputStream(uri)?.use { inputStream ->
                InputStreamReader(inputStream).use { reader ->
                    reader.readText()
                }
            } ?: throw Exception("Could not open input stream for URI: $uri")

            val backup = gson.fromJson(json, BackupData::class.java)

            // 2. Transactional Restore
            db.withTransaction {
                if (!merge) {
                    // WIPES USER DATA
                    Log.d(TAG, "Wiping existing user data...")
                    db.dailyLogDao().deleteAllLogs()
                    db.dailyWaterLogDao().deleteAllWaterLogs()
                    db.dietItemDao().deleteAllDietItems()
                    db.dietDao().deleteAllDiets()
                    db.foodDao().deleteCustomFoods()
                }

                // Restore Custom Foods & Build Map (TacoID -> New ID)
                Log.d(TAG, "Restoring custom foods...")
                val customFoodMapping = mutableMapOf<String, Int>()

                backup.customFoods.forEach { food ->
                    // 1. Heal missing UUIDs from older backups
                    var targetUuid = food.uuid
                    if (targetUuid == null && food.tacoID.startsWith("CUSTOM-")) {
                        // Extract UUID from tacoID if possible (CUSTOM-uuid-string...)
                        targetUuid = food.tacoID.removePrefix("CUSTOM-")
                        // Validate it's a UUID, otherwise keep null to trigger generation
                        if (targetUuid.length < 32) targetUuid = null 
                    }
                    
                    if (targetUuid == null) {
                        targetUuid = java.util.UUID.randomUUID().toString()
                    }

                    val foodWithUuid = food.copy(uuid = targetUuid)

                    // 2. Check if we already have this food by UUID to prevent duplication
                    val existingByUuid = db.foodDao().getFoodByUuid(targetUuid)

                    if (existingByUuid != null) {
                        Log.d(TAG, "Skipping duplicate custom food (UUID match): ${food.name}")
                        customFoodMapping[food.tacoID] = existingByUuid.id
                    } else {
                        // 3. Check for tacoID collisions (same ID, different UUID)
                        val existingByTacoId = db.foodDao().getFoodByTacoIDSuspend(food.tacoID)
                        
                        val finalFoodToInsert = if (existingByTacoId != null) {
                            // Collision detected. Rename tacoID but keep our resolved UUID
                            val newTacoID = "CUSTOM-$targetUuid"
                            Log.d(TAG, "TacoID collision for ${food.name}. Renaming ${food.tacoID} -> $newTacoID")
                            foodWithUuid.copy(tacoID = newTacoID, id = 0, isCustom = true)
                        } else {
                            foodWithUuid.copy(id = 0, isCustom = true)
                        }

                        val newId = db.foodDao().insertFood(finalFoodToInsert)
                        customFoodMapping[food.tacoID] = newId.toInt()
                    }
                }

                // Refresh Food Map (All foods now in DB)
                // We need this to resolve tacoID -> ID for both custom and official foods
                val allFoods = db.foodDao().getAllFoods().first()
                val standardFoodMap = allFoods.associate { it.tacoID to it.id }

                // We want to look up in custom mapping first (resolves collisions), then standard map
                val tacoIdToNewId = standardFoodMap + customFoodMapping

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
                if (merge) {
                    // In merge mode, we don't overwrite existing days.
                    val existingWaterDates = db.dailyWaterLogDao().getAllWaterLogs().map { it.date }.toSet()
                    backup.waterLogs.forEach { log ->
                        if (!existingWaterDates.contains(log.date)) {
                            db.dailyWaterLogDao().insertOrUpdate(log)
                        }
                    }
                } else {
                    backup.waterLogs.forEach {
                        db.dailyWaterLogDao().insertOrUpdate(it)
                    }
                }
            }

            // Restore Profile (outside transaction)
            if (!merge && backup.userProfile != null) {
                userProfileRepository.saveProfile(backup.userProfile)
            } else if (merge) {
                // In merge, we ignore profile import to preserve user data.
                Log.d(TAG, "Merge mode: Skipping UserProfile import.")
            }

            Log.d(TAG, "Import completed successfully.")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Import failed", e)
            Result.failure(e)
        }
    }
}
