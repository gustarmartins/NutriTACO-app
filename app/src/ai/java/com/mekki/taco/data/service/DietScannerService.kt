package com.mekki.taco.data.service

import android.graphics.Bitmap
import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.vertexai.vertexAI
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.mekki.taco.data.db.dao.FoodDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.json.JSONArray

class DietScannerService(private val foodDao: FoodDao) {

    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    private val generativeModel = Firebase.vertexAI.generativeModel("gemini-2.5-flash")

    suspend fun scanAndParseDiet(bitmap: Bitmap): List<ScannedItem> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d("DietScanner", "Starting OCR...")
                val image = InputImage.fromBitmap(bitmap, 0)
                val visionText = recognizer.process(image).await()
                val rawText = visionText.text

                Log.d("DietScanner", "OCR Raw Text:\n$rawText")

                if (rawText.isBlank()) {
                    Log.w("DietScanner", "OCR returned empty text")
                    return@withContext emptyList()
                }

                // This is very expensive and not practical at all for production
                val allFoods = foodDao.getAllFoods().first()
                val dbContext = allFoods.joinToString("\n") {
                    "${it.id}|${it.name}|${it.category}"
                }

                val prompt = """
                    You are an expert nutritionist assistant specialized in digitizing diet plans from OCR text in Portuguese.
                    
                    ### OBJECTIVE
                    Extract a structured list of food items from the 'SCANNED DOCUMENT TEXT'.
                    
                    ### CRITICAL RULES (Follow strictly)
                    1. **NO HALLUCINATIONS**: Only extract foods that are clearly visible in the text. Do not invent items. If the text is garbage or noise, ignore it.
                    2. **IGNORE SUBSTITUTIONS**: Diet plans often list alternatives.
                       - **ALWAYS** pick only the **PRIMARY/FIRST** option listed for a meal.
                       - **IGNORE** text starting with "ou", "ou então", "opção", "substituir por", "trocar por".
                       - Example: "Arroz 100g ou Batata 200g" -> Extract ONLY "Arroz" (100g).
                       - Example: "Patinho moído. Você pode substituir por frango..." -> Extract ONLY "Patinho moído".
                    3. **IGNORE INSTRUCTIONS**: Ignore preparation instructions (e.g., "beber muita água", "mastigar bem") and general text.
                    4. **QUANTITIES**: Extract precise grams. Convert "colher de sopa" (~15g), "fatia" (~30g) if specific grams aren't listed.
                    
                    ### LOCAL DATABASE CONTEXT (For ID matching)
                    The user has a local database. Use this to find the best `db_id` for the extracted food.
                    Format: ID|Name|Category
                    $dbContext
                    
                    ### SCANNED DOCUMENT TEXT
                    "$rawText"
                    
                    ### OUTPUT FORMAT (JSON ARRAY ONLY)
                    [
                      {
                        "food": "Exact name found in text (cleaned)",
                        "grams": 150.0,
                        "meal": "Almoço",  // Infer from headers like 'Café', 'Almoço', 'Lanche', 'Jantar'
                        "db_id": 123,      // Best match ID from DB or null
                        "est_kcal_100g": 0, // Fallback values if ID is null
                        "est_prot_100g": 0,
                        "est_carb_100g": 0,
                        "est_fat_100g": 0
                      }
                    ]
                """.trimIndent()

                Log.d(
                    "DietScanner",
                    "Sending prompt to Gemini (Context Length: ${dbContext.length} chars)..."
                )
                val response = generativeModel.generateContent(prompt)

                val jsonString = response.text
                    ?.replace("```json", "")
                    ?.replace("```", "")
                    ?.trim() ?: "[]"

                Log.d("DietScanner", "Gemini Response JSON:\n$jsonString")

                parseJsonResult(jsonString)

            } catch (e: Exception) {
                Log.e("DietScanner", "Error processing image", e)
                emptyList()
            }
        }
    }

    private fun parseJsonResult(json: String): List<ScannedItem> {
        val result = mutableListOf<ScannedItem>()
        try {
            val array = JSONArray(json)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val dbId =
                    if (obj.has("db_id") && !obj.isNull("db_id")) obj.getInt("db_id") else null

                result.add(
                    ScannedItem(
                        rawName = obj.optString("food"),
                        estimatedGrams = obj.optDouble("grams", 100.0),
                        mealType = obj.optString("meal", "Outros"),
                        matchedId = dbId,
                        backupCalories = obj.optDouble("est_kcal_100g", 0.0),
                        backupProtein = obj.optDouble("est_prot_100g", 0.0),
                        backupCarbs = obj.optDouble("est_carb_100g", 0.0),
                        backupFat = obj.optDouble("est_fat_100g", 0.0)
                    )
                )
            }
        } catch (e: Exception) {
            Log.e("DietScanner", "JSON Parsing Error", e)
        }
        return result
    }
}
