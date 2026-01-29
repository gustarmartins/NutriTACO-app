package com.mekki.taco.utils

import android.content.Context
import com.mekki.taco.data.db.database.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


object MockDataLoader {

    suspend fun loadMockDiaryData(db: AppDatabase, context: Context) = withContext(Dispatchers.IO) {
        context.assets.open("mock_diary_data.sql").bufferedReader().use { reader ->
            val sqlContent = reader.readText()

            val statements = sqlContent
                .lines()
                .filter { line ->
                    val trimmed = line.trim()
                    trimmed.isNotEmpty() && !trimmed.startsWith("--")
                }
                .joinToString(" ")
                .split(";")
                .map { it.trim() }
                .filter {
                    it.isNotEmpty() &&
                            !it.equals("BEGIN TRANSACTION", ignoreCase = true) &&
                            !it.equals("COMMIT", ignoreCase = true)
                }

            db.openHelper.writableDatabase.apply {
                beginTransaction()
                try {
                    statements.forEach { statement ->
                        if (statement.isNotBlank()) {
                            execSQL(statement)
                        }
                    }
                    setTransactionSuccessful()
                } finally {
                    endTransaction()
                }
            }
        }
    }

    suspend fun clearDiaryData(db: AppDatabase) = withContext(Dispatchers.IO) {
        db.openHelper.writableDatabase.execSQL("DELETE FROM daily_log")
    }
}
