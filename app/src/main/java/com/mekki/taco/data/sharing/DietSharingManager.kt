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

            val dietWithItems = db.dietDao().getDietWithItemsById(dietId).first()
                ?: return@withContext ExportResult.Error("Dieta não encontrada")

            val foodIds = dietWithItems.items.map { it.food.id }.distinct()
            val foods = foodIds.mapNotNull { id ->
                db.foodDao().getFoodByIdSync(id)
            }

            // Only custom foods are embedded — official sources will exist on every device
            // If it does not, we might evaluate whether we want future beta / stable versions
            // To have backwards compatibility with diet sharing (I don't see why it should during beta)
            val customFoods = foods.filter { it.isCustom }
            val officialFoods = foods.filter { !it.isCustom }

            Log.d(
                TAG,
                "Diet has ${foods.size} foods: ${customFoods.size} custom, ${officialFoods.size} official"
            )

            val entries = dietWithItems.items.map { item ->
                SharedDietEntry(
                    foodRef = FoodReference.fromFood(item.food),
                    quantityGrams = item.dietItem.quantityGrams,
                    mealType = item.dietItem.mealType,
                    consumptionTime = item.dietItem.consumptionTime
                )
            }

            val sharedDiet = SharedDiet(
                diet = SharedDietInfo(
                    name = dietWithItems.diet.name,
                    calorieGoal = dietWithItems.diet.calorieGoals,
                    creationDate = dietWithItems.diet.creationDate
                ),
                entries = entries,
                customFoods = customFoods.map { SharedFood.fromFood(it) }
            )

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
    
    private val MAX_FILE_SIZE = 5 * 1024 * 1024L

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

            // Size-checked + streamed parse (never loads raw string into memory)
            val sharedDiet = parseSecurely(uri)
                ?: return@withContext ImportResult.Error("Falha ao ler o arquivo ou arquivo inválido")

            // External file = untrusted input
            try {
                validateDietIntegrity(sharedDiet)
            } catch (e: Exception) {
                Log.e(TAG, "Validation failed", e)
                return@withContext ImportResult.Error("Arquivo corrompido ou inseguro: ${e.message}")
            }

            Log.d(
                TAG,
                "Parsed diet: ${sharedDiet.diet.name} with ${sharedDiet.entries.size} entries"
            )

            var importedDiet: Diet? = null
            var itemsImported = 0
            var customFoodsImported = 0
            var customFoodsSkipped = 0

            db.withTransaction {
                // Custom foods must be imported before diet items so we can resolve their IDs
                val uuidToLocalId = mutableMapOf<String, Int>()

                for (sharedFood in sharedDiet.customFoods) {
                    val existingFood = db.foodDao().getFoodByUuid(sharedFood.uuid)

                    if (existingFood != null) {
                        when (conflictResolution) {
                            ConflictResolution.KEEP_LOCAL -> {
                                uuidToLocalId[sharedFood.uuid] = existingFood.id
                                customFoodsSkipped++
                            }

                            ConflictResolution.REPLACE_WITH_INCOMING -> {
                                val updatedFood = sharedFood.toFood().copy(id = existingFood.id)
                                db.foodDao().updateFood(updatedFood)
                                uuidToLocalId[sharedFood.uuid] = existingFood.id
                                customFoodsImported++
                            }

                            ConflictResolution.KEEP_BOTH -> {
                                // New UUID so both copies can coexist without collisions
                                val newUuid = UUID.randomUUID().toString()
                                val newFood = sharedFood.toFood().copy(
                                    uuid = newUuid,
                                    tacoID = "CUSTOM-$newUuid",
                                    name = "${sharedFood.name} (Importado)"
                                )
                                val newId = db.foodDao().insertFood(newFood).toInt()
                                uuidToLocalId[sharedFood.uuid] = newId
                                customFoodsImported++
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
                    }
                }
                val officialTacoIds = sharedDiet.entries
                    .mapNotNull { it.foodRef.tacoId }
                    .filter { !it.startsWith("CUSTOM-") }
                    .distinct()

                val officialFoods = db.foodDao().getFoodsByTacoIds(officialTacoIds)
                val tacoIdToLocalId = officialFoods.associate { it.tacoID to it.id }

                // Imported diets are never main — user decides which one to set
                val dietToInsert = Diet(
                    id = 0,
                    name = newDietName ?: generateUniqueDietName(sharedDiet.diet.name),
                    creationDate = System.currentTimeMillis(),
                    calorieGoals = sharedDiet.diet.calorieGoal,
                    isMain = false
                )
                val newDietId = db.dietDao().insertOrReplaceDiet(dietToInsert).toInt()
                importedDiet = dietToInsert.copy(id = newDietId)

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
                        null
                    }
                }

                if (dietItems.isNotEmpty()) {
                    db.dietItemDao().insertDietItems(dietItems)
                    itemsImported = dietItems.size
                }
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

    private fun parseSecurely(uri: Uri): SharedDiet? {
        // Size check throws (not caught here) so the caller gets the specific error message
        context.contentResolver.openFileDescriptor(uri, "r")?.use { pfd ->
            if (pfd.statSize > MAX_FILE_SIZE) {
                throw Exception("Arquivo muito grande (Lim: 5MB)")
            }
        }
        // Parse errors return null — the caller shows a generic "invalid file" message
        return try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                InputStreamReader(inputStream, Charsets.UTF_8).use { reader ->
                    gson.fromJson(reader, SharedDiet::class.java)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Secure parse failed", e)
            null
        }
    }

    private fun validateDietIntegrity(sharedDiet: SharedDiet) {
        if (sharedDiet.diet.name.length > 100) throw Exception("Nome da dieta muito longo")

        if (sharedDiet.entries.size > 500) throw Exception("Muitos itens na dieta (Max: 500)")
        if (sharedDiet.customFoods.size > 200) throw Exception("Muitos alimentos customizados (Max: 200)")

        sharedDiet.entries.forEach { entry ->
            if (entry.quantityGrams < 0 || entry.quantityGrams > 5000) 
                 throw Exception("Quantidade inválida para item")
            if ((entry.mealType?.length ?: 0) > 50) throw Exception("Tipo de refeição inválido")
            if ((entry.consumptionTime?.length ?: 0) > 20) throw Exception("Horário inválido")
        }

        sharedDiet.customFoods.forEach { food ->
            if (food.name.length > 200) throw Exception("Nome de alimento muito longo: ${food.name.take(30)}")
            if (food.category.length > 100) throw Exception("Categoria muito longa")
            if (food.uuid.length > 60) throw Exception("UUID inválido")
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
            val path = uri.path ?: uri.lastPathSegment ?: ""
            if (path.endsWith(".dieta", ignoreCase = true)) {
                return NutriTacoFileType.DIET
            }

            // No extension match — peek at JSON keys to figure out the format
            val json = context.contentResolver.openInputStream(uri)?.use { inputStream ->
                InputStreamReader(inputStream, Charsets.UTF_8).use { it.readText() }
            } ?: return NutriTacoFileType.UNKNOWN

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