package com.mekki.taco.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.mekki.taco.data.model.ActivityLevel
import com.mekki.taco.data.model.UserProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_profile")

class UserProfileRepository(context: Context) {

    private val dataStore = context.dataStore

    private object PreferencesKeys {
        val WEIGHT = doublePreferencesKey("weight")
        val HEIGHT = doublePreferencesKey("height")
        val AGE = intPreferencesKey("age")
        val SEX = stringPreferencesKey("sex")
        val ACTIVITY_LEVEL = stringPreferencesKey("activity_level")
        val PROTEIN_GOAL = doublePreferencesKey("protein_goal")
        val CARBS_GOAL = doublePreferencesKey("carbs_goal")
        val FAT_GOAL = doublePreferencesKey("fat_goal")
        val WATER_GOAL = doublePreferencesKey("water_goal")
        val CALORIE_GOAL = doublePreferencesKey("calorie_goal")
        val IS_DARK_MODE = androidx.datastore.preferences.core.booleanPreferencesKey("is_dark_mode")
    }

    val userProfileFlow: Flow<UserProfile> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            val weight = preferences[PreferencesKeys.WEIGHT]
            val height = preferences[PreferencesKeys.HEIGHT]
            val age = preferences[PreferencesKeys.AGE]
            val sex = preferences[PreferencesKeys.SEX]
            val calorieGoal = preferences[PreferencesKeys.CALORIE_GOAL]
            val isDarkMode = preferences[PreferencesKeys.IS_DARK_MODE] ?: true

            val activityLevelString = preferences[PreferencesKeys.ACTIVITY_LEVEL]
            val activityLevel = try {
                if (activityLevelString != null) ActivityLevel.valueOf(activityLevelString) else null
            } catch (e: IllegalArgumentException) {
                null
            }

            val proteinGoal =
                preferences[PreferencesKeys.PROTEIN_GOAL] ?: UserProfile().proteinGoalPerKg
            val carbsGoal = preferences[PreferencesKeys.CARBS_GOAL] ?: UserProfile().carbsGoalPerKg
            val fatGoal = preferences[PreferencesKeys.FAT_GOAL] ?: UserProfile().fatGoalPerKg
            val waterGoal =
                preferences[PreferencesKeys.WATER_GOAL] ?: UserProfile().waterGoalPerMlPerKg

            UserProfile(
                weight = weight,
                height = height,
                age = age,
                sex = sex,
                activityLevel = activityLevel,
                proteinGoalPerKg = proteinGoal,
                carbsGoalPerKg = carbsGoal,
                fatGoalPerKg = fatGoal,
                waterGoalPerMlPerKg = waterGoal,
                calorieGoal = calorieGoal,
                isDarkMode = isDarkMode
            )
        }

    suspend fun saveProfile(profile: UserProfile) {
        dataStore.edit { preferences ->
            profile.weight?.let { preferences[PreferencesKeys.WEIGHT] = it }
            profile.height?.let { preferences[PreferencesKeys.HEIGHT] = it }
            profile.age?.let { preferences[PreferencesKeys.AGE] = it }
            profile.sex?.let { preferences[PreferencesKeys.SEX] = it }
            profile.activityLevel?.let { preferences[PreferencesKeys.ACTIVITY_LEVEL] = it.name }

            profile.calorieGoal?.let { preferences[PreferencesKeys.CALORIE_GOAL] = it }

            preferences[PreferencesKeys.PROTEIN_GOAL] = profile.proteinGoalPerKg
            preferences[PreferencesKeys.CARBS_GOAL] = profile.carbsGoalPerKg
            preferences[PreferencesKeys.FAT_GOAL] = profile.fatGoalPerKg
            preferences[PreferencesKeys.WATER_GOAL] = profile.waterGoalPerMlPerKg
            preferences[PreferencesKeys.IS_DARK_MODE] = profile.isDarkMode
        }
    }

    suspend fun saveWeight(weight: Double) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.WEIGHT] = weight
        }
    }

    suspend fun saveDietChartPreference(dietId: Int, chartType: String) {
        dataStore.edit { preferences ->
            preferences[stringPreferencesKey("chart_type_diet_$dietId")] = chartType
        }
    }

    val dietChartPreferencesFlow: Flow<Map<Int, String>> = dataStore.data
        .map { preferences ->
            preferences.asMap().entries
                .filter { it.key.name.startsWith("chart_type_diet_") }
                .mapNotNull { entry ->
                    val id = entry.key.name.removePrefix("chart_type_diet_").toIntOrNull()
                    val type = entry.value as? String
                    if (id != null && type != null) id to type else null
                }
                .toMap()
        }
}
