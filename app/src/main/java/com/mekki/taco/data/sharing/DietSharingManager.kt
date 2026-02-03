package com.mekki.taco.data.sharing

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.room.withTransaction
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.mekki.taco.data.db.database.AppDatabase
import com.mekki.taco.data.db.entity.Diet
import com.mekki.taco.data.db.entity.DietItem
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.util.UUID
import javax.inject.Inject

/**
 * exporting and importing diets for sharing between users.
 */
class DietSharingManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val db: AppDatabase
) {
    private val gson: Gson = GsonBuilder()
        .setPrettyPrinting()
        .serializeNulls()
        .create()

    private val TAG = "DietSharing"

    // ==================== EXPORT ====================

    /**
     * Export a diet to a shareable JSON format.
     *
     * @param dietId The ID of the diet to export
     * @return ExportResult.Success with JSON string, or ExportResult.Error
     */
    suspend fun exportDiet(dietId: Int): ExportResult = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Starting export for diet $dietId")

            // 1. Load diet with all items
            val dietWithItems = db.dietDao().getDietWithItemsById(dietId).first()
                ?: return@withContext ExportResult.Error("Dieta não encontrada")

            // 2. Collect all foods used in this diet
            val foodIds = dietWithItems.items.map { it.food.id }.distinct()
            val foods = foodIds.mapNotNull { id ->
                db.foodDao().getFoodByIdSync(id)
            }

            // 3. Separate custom foods (to be included) from official foods
            val customFoods = foods.filter { it.isCustom }
            val officialFoods = foods.filter { !it.isCustom }

            Log.d(
                TAG,
                "Diet has ${foods.size} foods: ${customFoods.size} custom, ${officialFoods.size} official"
            )

            // 4. Build shared diet entries
            val entries = dietWithItems.items.map { item ->
                SharedDietEntry(
                    foodRef = FoodReference.fromFood(item.food),
                    quantityGrams = item.dietItem.quantityGrams,
                    mealType = item.dietItem.mealType,
                    consumptionTime = item.dietItem.consumptionTime
                )
            }

            // 5. Build the SharedDiet object
            val sharedDiet = SharedDiet(
                diet = SharedDietInfo(
                    name = dietWithItems.diet.name,
                    calorieGoal = dietWithItems.diet.calorieGoals,
                    creationDate = dietWithItems.diet.creationDate
                ),
                entries = entries,
                customFoods = customFoods.map { SharedFood.fromFood(it) }
            )

            // 6. Serialize to JSON
            val json = gson.toJson(sharedDiet)
            val fileName =
                "${sanitizeFileName(dietWithItems.diet.name)}.${SharedDiet.FILE_EXTENSION}"

            Log.d(TAG, "Export successful: ${json.length} bytes")
            ExportResult.Success(json, fileName)

        } catch (e: Exception) {
            Log.e(TAG, "Export failed", e)
            ExportResult.Error("Erro ao exportar dieta: ${e.message}", e)
        }
    }

    /**
     * Export directly to a file URI
     */
    suspend fun exportDietToUri(dietId: Int, uri: Uri): ExportResult = withContext(Dispatchers.IO) {
        when (val result = exportDiet(dietId)) {
            is ExportResult.Success -> {
                try {
                    context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                        OutputStreamWriter(outputStream, Charsets.UTF_8).use { writer ->
                            writer.write(result.json)
                        }
                    } ?: return@withContext ExportResult.Error("Não foi possível abrir o arquivo")
                    result
                } catch (e: Exception) {
                    ExportResult.Error("Erro ao salvar arquivo: ${e.message}", e)
                }
            }

            is ExportResult.Error -> result
        }
    }

    // ==================== IMPORT ====================

    /**
     * @param uri File URI (from SAF or content provider)
     * @param conflictResolution How to handle existing custom foods
     * @param newDietName Optional name override for the imported diet
     */
    suspend fun importDiet(
        uri: Uri,
        conflictResolution: ConflictResolution = ConflictResolution.KEEP_LOCAL,
        newDietName: String? = null
    ): ImportResult = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Starting import from $uri")

            // 1. Read and parse JSON
            val json = readJsonFromUri(uri)
                ?: return@withContext ImportResult.Error("Não foi possível ler o arquivo")

            val sharedDiet = parseSharedDiet(json)
                ?: return@withContext ImportResult.Error("Formato de arquivo inválido")

            Log.d(
                TAG,
                "Parsed diet: ${sharedDiet.diet.name} with ${sharedDiet.entries.size} entries"
            )

            // 2. Import in a transaction
            var importedDiet: Diet? = null
            var itemsImported = 0
            var customFoodsImported = 0
            var customFoodsSkipped = 0

            db.withTransaction {
                // 2a. Import custom foods first
                val uuidToLocalId = mutableMapOf<String, Int>()

                for (sharedFood in sharedDiet.customFoods) {
                    val existingFood = db.foodDao().getFoodByUuid(sharedFood.uuid)

                    if (existingFood != null) {
                        // Food already exists locally
                        // (Highly unlikely in most cases, but possible if the user tries to import their own previously exported diet)
                        when (conflictResolution) {
                            ConflictResolution.KEEP_LOCAL -> {
                                uuidToLocalId[sharedFood.uuid] = existingFood.id
                                customFoodsSkipped++
                                Log.d(TAG, "Keeping local food: ${existingFood.name}")
                            }

                            ConflictResolution.REPLACE_WITH_INCOMING -> {
                                val updatedFood = sharedFood.toFood().copy(id = existingFood.id)
                                db.foodDao().updateFood(updatedFood)
                                uuidToLocalId[sharedFood.uuid] = existingFood.id
                                customFoodsImported++
                                Log.d(TAG, "Replaced food: ${sharedFood.name}")
                            }

                            ConflictResolution.KEEP_BOTH -> {
                                // Insert as new with different UUID
                                val newUuid = UUID.randomUUID().toString()
                                val newFood = sharedFood.toFood().copy(
                                    uuid = newUuid,
                                    tacoID = "CUSTOM-$newUuid",
                                    name = "${sharedFood.name} (Importado)"
                                )
                                val newId = db.foodDao().insertFood(newFood).toInt()
                                uuidToLocalId[sharedFood.uuid] = newId
                                customFoodsImported++
                                Log.d(TAG, "Created duplicate: ${newFood.name}")
                            }

                            ConflictResolution.ASK_USER -> {
                                uuidToLocalId[sharedFood.uuid] = existingFood.id
                                customFoodsSkipped++
                            }
                        }
                    } else {
                        val newFood = sharedFood.toFood()
                        val newId = db.foodDao().insertFood(newFood).toInt()
                        uuidToLocalId[sharedFood.uuid] = newId
                        customFoodsImported++
                        Log.d(TAG, "Imported new food: ${sharedFood.name} -> ID $newId")
                    }
                }
                val officialTacoIds = sharedDiet.entries
                    .mapNotNull { it.foodRef.tacoId }
                    .filter { !it.startsWith("CUSTOM-") }
                    .distinct()

                val officialFoods = db.foodDao().getFoodsByTacoIds(officialTacoIds)
                val tacoIdToLocalId = officialFoods.associate { it.tacoID to it.id }

                // 2b. Creates the diet
                val dietToInsert = Diet(
                    id = 0,
                    name = newDietName ?: generateUniqueDietName(sharedDiet.diet.name),
                    creationDate = System.currentTimeMillis(),
                    calorieGoals = sharedDiet.diet.calorieGoal,
                    isMain = false
                )
                val newDietId = db.dietDao().insertOrReplaceDiet(dietToInsert).toInt()
                importedDiet = dietToInsert.copy(id = newDietId)

                Log.d(TAG, "Created diet: ${importedDiet?.name} with ID $newDietId")

                // 2c. Create diet items
                val dietItems = sharedDiet.entries.mapNotNull { entry ->
                    val localFoodId = resolveFoodId(entry.foodRef, uuidToLocalId, tacoIdToLocalId)

                    if (localFoodId != null) {
                        DietItem(
                            id = 0,
                            dietId = newDietId,
                            foodId = localFoodId,
                            quantityGrams = entry.quantityGrams,
                            mealType = entry.mealType,
                            consumptionTime = entry.consumptionTime
                        )
                    } else {
                        Log.w(TAG, "Could not resolve food for entry: ${entry.foodRef}")
                        null
                    }
                }

                if (dietItems.isNotEmpty()) {
                    db.dietItemDao().insertDietItems(dietItems)
                    itemsImported = dietItems.size
                }

                Log.d(TAG, "Imported $itemsImported diet items")
            }

            ImportResult.Success(
                diet = importedDiet!!,
                itemsImported = itemsImported,
                customFoodsImported = customFoodsImported,
                customFoodsSkipped = customFoodsSkipped
            )

        } catch (e: Exception) {
            Log.e(TAG, "Import failed", e)
            ImportResult.Error("Erro ao importar dieta: ${e.message}", e)
        }
    }

    // ==================== HELPERS ====================

    private fun readJsonFromUri(uri: Uri): String? {
        return try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                InputStreamReader(inputStream, Charsets.UTF_8).use { reader ->
                    reader.readText()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to read file", e)
            null
        }
    }

    private fun parseSharedDiet(json: String): SharedDiet? {
        return try {
            gson.fromJson(json, SharedDiet::class.java)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse JSON", e)
            null
        }
    }

    private fun resolveFoodId(
        ref: FoodReference,
        uuidMap: Map<String, Int>,
        tacoIdMap: Map<String, Int>
    ): Int? {
        return when {
            ref.uuid != null -> uuidMap[ref.uuid]
            ref.tacoId != null -> tacoIdMap[ref.tacoId]
            else -> null
        }
    }

    private suspend fun generateUniqueDietName(baseName: String): String {
        val existingDiets = db.dietDao().getAllDietsList()
        val existingNames = existingDiets.map { it.name.lowercase() }.toSet()

        if (baseName.lowercase() !in existingNames) {
            return baseName
        }

        var counter = 2
        while ("${baseName.lowercase()} ($counter)" in existingNames) {
            counter++
        }
        return "$baseName ($counter)"
    }

    private fun sanitizeFileName(name: String): String {
        return name
            .replace(Regex("[^a-zA-Z0-9áàâãéèêíìîóòôõúùûçÁÀÂÃÉÈÊÍÌÎÓÒÔÕÚÙÛÇ\\s-]"), "")
            .replace(Regex("\\s+"), "_")
            .take(50)
            .ifBlank { "dieta" }
    }

    suspend fun shareDietToCache(dietId: Int): Uri? = withContext(Dispatchers.IO) {
        when (val result = exportDiet(dietId)) {
            is ExportResult.Success -> {
                try {
                    val cacheDir = java.io.File(context.cacheDir, "shared_diets")
                    cacheDir.mkdirs()

                    val file = java.io.File(cacheDir, result.fileName)
                    file.writeText(result.json, Charsets.UTF_8)

                    androidx.core.content.FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.fileprovider",
                        file
                    )
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to create shareable file", e)
                    null
                }
            }

            is ExportResult.Error -> null
        }
    }

    fun detectFileType(uri: Uri): NutriTacoFileType {
        return try {
            val json = readJsonFromUri(uri) ?: return NutriTacoFileType.UNKNOWN

            when {
                json.contains("\"diet\"") && json.contains("\"entries\"") -> NutriTacoFileType.DIET
                json.contains("\"diets\"") && json.contains("\"dailyLogs\"") -> NutriTacoFileType.BACKUP
                else -> NutriTacoFileType.UNKNOWN
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to detect file type", e)
            NutriTacoFileType.UNKNOWN
        }
    }
}