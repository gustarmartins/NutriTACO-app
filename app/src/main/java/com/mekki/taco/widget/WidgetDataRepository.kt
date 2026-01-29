package com.mekki.taco.widget

import android.content.Context
import com.mekki.taco.data.db.database.AppDatabase
import com.mekki.taco.data.db.entity.Food
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

object WidgetDataRepository {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    suspend fun getTopFoods(context: Context, limit: Int = 10): List<Food> {
        return withContext(Dispatchers.IO) {
            val db = AppDatabase.getDatabase(context, scope)
            db.foodDao().getTopFoods(limit).first()
        }
    }
}
