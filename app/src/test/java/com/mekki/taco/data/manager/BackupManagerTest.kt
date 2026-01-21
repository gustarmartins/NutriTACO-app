package com.mekki.taco.data.manager

import com.google.gson.Gson
import com.mekki.taco.data.db.entity.DailyWaterLog
import com.mekki.taco.data.db.entity.Diet
import com.mekki.taco.data.db.entity.Food
import com.mekki.taco.data.model.UserProfile
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class BackupManagerTest {

    private val gson = Gson()

    @Test
    fun testBackupDataSerialization() {
        val backupData = BackupData(
            version = 1,
            appVersionCode = 10,
            appVersionName = "1.0.0",
            userProfile = UserProfile(weight = 70.0, height = 175.0),
            customFoods = listOf(
                Food(
                    id = 1, tacoID = "custom_1", name = "My Custom Food", category = "Custom", isCustom = true,
                    energiaKcal = 100.0, energiaKj = 418.0, proteina = 10.0, colesterol = 0.0,
                    carboidratos = 20.0, fibraAlimentar = 2.0, cinzas = 1.0, calcio = 10.0,
                    magnesio = 5.0, manganes = 0.5, fosforo = 50.0, ferro = 1.0, sodio = 5.0,
                    potassio = 100.0, cobre = 0.1, zinco = 1.0, retinol = 0.0, RE = 0.0, RAE = 0.0,
                    tiamina = 0.0, riboflavina = 0.0, piridoxina = 0.0, niacina = 0.0, vitaminaC = 0.0,
                    umidade = 10.0, lipidios = null, aminoacidos = null
                )
            ),
            diets = listOf(
                Diet(id = 1, name = "My Diet", creationDate = System.currentTimeMillis(), isMain = true)
            ),
            dietItems = listOf(
                DietItemBackup(dietId = 1, foodTacoId = "1", quantityGrams = 100.0, mealType = "Breakfast", consumptionTime = "08:00")
            ),
            dailyLogs = listOf(
                DailyLogBackup(foodTacoId = "1", date = "2023-01-01", quantityGrams = 150.0, mealType = "Lunch", isConsumed = true, originalQuantityGrams = 150.0)
            ),
            waterLogs = listOf(
                DailyWaterLog(date = "2023-01-01", quantityMl = 2000)
            )
        )

        val json = gson.toJson(backupData)
        assertNotNull(json)

        val restoredBackup = gson.fromJson(json, BackupData::class.java)
        
        assertEquals(backupData.version, restoredBackup.version)
        assertEquals(backupData.appVersionCode, restoredBackup.appVersionCode)
        assertEquals(backupData.userProfile?.weight, restoredBackup.userProfile?.weight)
        assertEquals(backupData.customFoods.size, restoredBackup.customFoods.size)
        assertEquals(backupData.customFoods[0].name, restoredBackup.customFoods[0].name)
        assertEquals(backupData.dailyLogs.size, restoredBackup.dailyLogs.size)
        assertEquals(backupData.dailyLogs[0].foodTacoId, restoredBackup.dailyLogs[0].foodTacoId)
    }

    @Test
    fun testBackupDataSerialization_EmptyFields() {
         val backupData = BackupData(
            userProfile = null,
            customFoods = emptyList(),
            diets = emptyList(),
            dietItems = emptyList(),
            dailyLogs = emptyList(),
            waterLogs = emptyList()
        )
        
        val json = gson.toJson(backupData)
        val restoredBackup = gson.fromJson(json, BackupData::class.java)
        
        assertEquals(0, restoredBackup.customFoods.size)
        assertEquals(null, restoredBackup.userProfile)
    }
}
