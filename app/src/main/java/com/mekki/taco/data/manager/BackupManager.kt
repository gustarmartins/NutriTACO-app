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
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import javax.inject.Inject

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

// Internal IDs change across installs/devices, tacoID is stable across backups
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

data class RevertPoint(
    val file: File,
    val timestamp: Long
)


class BackupManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val db: AppDatabase,
    private val userProfileRepository: UserProfileRepository
) {
    private val gson = Gson()
    private val TAG = "BackupManager"
    private val revertDir = File(context.filesDir, "revert_backups")

    suspend fun exportData(uri: Uri): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Starting export...")

            val profile = userProfileRepository.userProfileFlow.firstOrNull()
            val customFoods = db.foodDao().getAllCustomFoods()
            val allFoods = db.foodDao().getAllFoods().first()
            val diets = db.dietDao().getAllDietsList()
            val rawDietItems = db.dietItemDao().getAllDietItems()
            val rawLogs = db.dailyLogDao().getAllLogs()
            val waterLogs = db.dailyWaterLogDao().getAllWaterLogs()

            // We store tacoID in the backup because internal IDs won't match on restore
            val foodIdToTacoId = allFoods.associate { it.id to it.tacoID }

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

    private val MAX_BACKUP_SIZE = 50 * 1024 * 1024L

    suspend fun importData(uri: Uri, merge: Boolean = false): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Starting import... Merge=$merge")

                // Size-checked + streamed parse (never loads raw string into memory)
                val backup = parseSecurely(uri)
                    ?: throw Exception("Falha ao ler o backup ou arquivo inválido")

                // External file = untrusted input, reject anything suspicious before touching DB
                try {
                    validateBackupIntegrity(backup)
                } catch (e: Exception) {
                    Log.e(TAG, "Validation failed", e)
                    return@withContext Result.failure(Exception("Arquivo de backup corrompido ou inseguro: ${e.message}"))
                }

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

                    Log.d(TAG, "Restoring custom foods...")
                    val customFoodMapping = mutableMapOf<String, Int>()

                    backup.customFoods.forEach { food ->
                        // Older backups didn't have uuid field, try to recover it from tacoID
                        var targetUuid = food.uuid
                        if (targetUuid == null && food.tacoID.startsWith("CUSTOM-")) {
                            targetUuid = food.tacoID.removePrefix("CUSTOM-")
                            if (targetUuid.length < 32) targetUuid = null
                        }

                        if (targetUuid == null) {
                            targetUuid = java.util.UUID.randomUUID().toString()
                        }

                        val foodWithUuid = food.copy(uuid = targetUuid)

                        val existingByUuid = db.foodDao().getFoodByUuid(targetUuid)

                        if (existingByUuid != null) {
                            customFoodMapping[food.tacoID] = existingByUuid.id
                        } else {
                            val existingByTacoId = db.foodDao().getFoodByTacoIDSuspend(food.tacoID)

                            val finalFoodToInsert = if (existingByTacoId != null) {
                                // Same tacoID but different UUID = different food, rename to avoid collision
                                val newTacoID = "CUSTOM-$targetUuid"
                                foodWithUuid.copy(tacoID = newTacoID, id = 0, isCustom = true)
                            } else {
                                foodWithUuid.copy(id = 0, isCustom = true)
                            }

                            val newId = db.foodDao().insertFood(finalFoodToInsert)
                            customFoodMapping[food.tacoID] = newId.toInt()
                        }
                    }

                    // Rebuild after custom food inserts so we can resolve both custom + official tacoIDs
                    val allFoods = db.foodDao().getAllFoods().first()
                    val standardFoodMap = allFoods.associate { it.tacoID to it.id }

                    // customFoodMapping overrides standardFoodMap for renamed tacoIDs
                    val tacoIdToNewId = standardFoodMap + customFoodMapping

                    Log.d(TAG, "Restoring diets...")
                    val oldDietIdToNewId = mutableMapOf<Int, Int>()
                    // Only one diet can be main — don't override the user's existing main on merge
                    val hasExistingMainDiet = if (merge) {
                        db.dietDao().getAllDietsList().any { it.isMain }
                    } else false

                    backup.diets.forEach { diet ->
                        val oldId = diet.id
                        val newDiet = diet.copy(
                            id = 0,
                            isMain = if (merge && hasExistingMainDiet) false else diet.isMain
                        )
                        val newId = db.dietDao().insertOrReplaceDiet(newDiet).toInt()
                        oldDietIdToNewId[oldId] = newId
                    }

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
                            null
                        }
                    }
                    if (newDietItems.isNotEmpty()) {
                        db.dietItemDao().insertDietItems(newDietItems)
                    }

                    Log.d(TAG, "Restoring daily logs...")
                    val newLogs = backup.dailyLogs.mapNotNull { log ->
                        val newFoodId = tacoIdToNewId[log.foodTacoId]
                        newFoodId?.let {
                            DailyLog(
                                id = 0,
                                foodId = it,
                                date = log.date,
                                quantityGrams = log.quantityGrams,
                                mealType = log.mealType,
                                isConsumed = log.isConsumed,
                                originalQuantityGrams = log.originalQuantityGrams
                            )
                        }
                    }.let { if (it.isNotEmpty()) db.dailyLogDao().insertAll(it) }

                    Log.d(TAG, "Restoring water logs...")
                    if (merge) {
                        val existingWaterDates =
                            db.dailyWaterLogDao().getAllWaterLogs().map { it.date }.toSet()
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

                // Profile is saved via DataStore, not Room — can't be inside the transaction
                if (!merge && backup.userProfile != null) {
                    userProfileRepository.saveProfile(backup.userProfile)
                } else if (merge) {
                    Log.d(TAG, "Merge mode: Skipping UserProfile import.")
                }

                Log.d(TAG, "Import completed successfully.")
                Result.success(Unit)
            } catch (e: Exception) {
                Log.e(TAG, "Import failed", e)
                Result.failure(e)
            }
        }

    private fun parseSecurely(uri: Uri): BackupData? {
        // Size check throws (not caught here) so the caller gets the specific error message
        context.contentResolver.openFileDescriptor(uri, "r")?.use { pfd ->
            if (pfd.statSize > MAX_BACKUP_SIZE) {
                throw Exception("Arquivo de backup muito grande (Lim: 50MB)")
            }
        }
        // Parse errors return null — the caller shows a generic "invalid file" message
        return try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                InputStreamReader(inputStream, Charsets.UTF_8).use { reader ->
                    gson.fromJson(reader, BackupData::class.java)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Secure parse failed", e)
            null
        }
    }

    private fun validateBackupIntegrity(backup: BackupData) {
        if (backup.dailyLogs.size > 50000) throw Exception("Excesso de registros diários no backup (>50k)")
        if (backup.diets.size > 500) throw Exception("Excesso de dietas no backup (>500)")
        if (backup.dietItems.size > 10000) throw Exception("Excesso de itens de dieta no backup (>10k)")
        if (backup.customFoods.size > 2000) throw Exception("Excesso de alimentos customizados (>2k)")
        if (backup.waterLogs.size > 50000) throw Exception("Excesso de registros de água (>50k)")

        backup.diets.forEach { diet ->
            if (diet.name.length > 100) throw Exception("Nome de dieta muito longo")
        }

        backup.dailyLogs.forEach { log ->
            if (log.quantityGrams < 0 || log.quantityGrams > 50000)
                throw Exception("Quantidade inválida em registro diário")
            if (log.mealType.length > 50) throw Exception("Tipo de refeição inválido")
            if (log.date.length > 20) throw Exception("Data inválida em registro")
            if (log.foodTacoId.length > 100) throw Exception("Referência de alimento inválida")
        }

        backup.dietItems.forEach { item ->
            if (item.quantityGrams < 0 || item.quantityGrams > 50000)
                throw Exception("Quantidade inválida em item de dieta")
            if (item.foodTacoId.length > 100) throw Exception("Referência de alimento inválida")
            if ((item.mealType?.length ?: 0) > 50) throw Exception("Tipo de refeição inválido")
            if ((item.consumptionTime?.length ?: 0) > 20) throw Exception("Horário inválido")
        }

        backup.customFoods.forEach { food ->
            if (food.name.length > 200) throw Exception("Nome de alimento muito longo: ${food.name.take(30)}")
            if (food.category.length > 100) throw Exception("Categoria muito longa")
            if ((food.uuid?.length ?: 0) > 60) throw Exception("UUID inválido")
            if (food.tacoID.length > 100) throw Exception("TacoID inválido")
            validateNutrientValue(food.energiaKcal, "energiaKcal", 20000.0)
            validateNutrientValue(food.proteina, "proteína", 1000.0)
            validateNutrientValue(food.carboidratos, "carboidratos", 1000.0)
            validateNutrientValue(food.colesterol, "colesterol", 5000.0)
        }
    }

    private fun validateNutrientValue(value: Double?, name: String, max: Double) {
        if (value == null) return
        if (value.isNaN() || value.isInfinite()) throw Exception("Valor numérico inválido em $name")
        if (value < 0 || value > max) throw Exception("Valor fora do intervalo para $name")
    }

    suspend fun createRevertPoint(): File? = withContext(Dispatchers.IO) {
        try {
            revertDir.mkdirs()
            cleanOldRevertPoints()

            val profile = userProfileRepository.userProfileFlow.firstOrNull()
            val customFoods = db.foodDao().getAllCustomFoods()
            val allFoods = db.foodDao().getAllFoods().first()
            val diets = db.dietDao().getAllDietsList()
            val rawDietItems = db.dietItemDao().getAllDietItems()
            val rawLogs = db.dailyLogDao().getAllLogs()
            val waterLogs = db.dailyWaterLogDao().getAllWaterLogs()

            val foodIdToTacoId = allFoods.associate { it.id to it.tacoID }

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
                } else null
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
                } else null
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

            val timestamp = System.currentTimeMillis()
            val file = File(revertDir, "revert_$timestamp.json")
            FileOutputStream(file).use { fos ->
                OutputStreamWriter(fos).use { writer ->
                    writer.write(gson.toJson(backup))
                }
            }

            Log.d(TAG, "Revert point created: ${file.name}")
            file
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create revert point", e)
            null
        }
    }

    fun getRevertPoints(): List<RevertPoint> {
        if (!revertDir.exists()) return emptyList()
        return revertDir.listFiles()
            ?.filter { it.name.startsWith("revert_") && it.name.endsWith(".json") }
            ?.mapNotNull { file ->
                val timestamp = file.name
                    .removePrefix("revert_")
                    .removeSuffix(".json")
                    .toLongOrNull()
                timestamp?.let { RevertPoint(file, it) }
            }
            ?.sortedByDescending { it.timestamp }
            ?: emptyList()
    }

    suspend fun restoreFromRevertPoint(file: File): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val json = file.readText()
            val backup = gson.fromJson(json, BackupData::class.java)

            db.withTransaction {
                db.dailyLogDao().deleteAllLogs()
                db.dailyWaterLogDao().deleteAllWaterLogs()
                db.dietItemDao().deleteAllDietItems()
                db.dietDao().deleteAllDiets()
                db.foodDao().deleteCustomFoods()

                backup.customFoods.forEach { food ->
                    db.foodDao().insertFood(food.copy(id = 0, isCustom = true))
                }

                val allFoods = db.foodDao().getAllFoods().first()
                val tacoIdToNewId = allFoods.associate { it.tacoID to it.id }

                val oldDietIdToNewId = mutableMapOf<Int, Int>()
                backup.diets.forEach { diet ->
                    val oldId = diet.id
                    val newId = db.dietDao().insertOrReplaceDiet(diet.copy(id = 0)).toInt()
                    oldDietIdToNewId[oldId] = newId
                }

                backup.dietItems.mapNotNull { item ->
                    val newDietId = oldDietIdToNewId[item.dietId]
                    val newFoodId = tacoIdToNewId[item.foodTacoId]
                    if (newDietId != null && newFoodId != null) {
                        DietItem(
                            id = 0, dietId = newDietId, foodId = newFoodId,
                            quantityGrams = item.quantityGrams, mealType = item.mealType,
                            consumptionTime = item.consumptionTime
                        )
                    } else null
                }.let { if (it.isNotEmpty()) db.dietItemDao().insertDietItems(it) }

                backup.dailyLogs.mapNotNull { log ->
                    val newFoodId = tacoIdToNewId[log.foodTacoId]
                    newFoodId?.let {
                        DailyLog(
                            id = 0,
                            foodId = it,
                            date = log.date,
                            quantityGrams = log.quantityGrams,
                            mealType = log.mealType,
                            isConsumed = log.isConsumed,
                            originalQuantityGrams = log.originalQuantityGrams
                        )
                    }
                }.let { if (it.isNotEmpty()) db.dailyLogDao().insertAll(it) }

                backup.waterLogs.forEach { db.dailyWaterLogDao().insertOrUpdate(it) }
            }

            if (backup.userProfile != null) {
                userProfileRepository.saveProfile(backup.userProfile)
            }

            Log.d(TAG, "Restored from revert point: ${file.name}")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to restore from revert point", e)
            Result.failure(e)
        }
    }

    private fun cleanOldRevertPoints() {
        val existing = getRevertPoints() // already sorted newest-first
        existing.drop(2).forEach { revertPoint ->
            revertPoint.file.delete()
            Log.d(TAG, "Deleted old revert point: ${revertPoint.file.name}")
        }
    }
}