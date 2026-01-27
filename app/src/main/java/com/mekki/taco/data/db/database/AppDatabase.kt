package com.mekki.taco.data.db.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.mekki.taco.data.db.dao.DailyLogDao
import com.mekki.taco.data.db.dao.DailyWaterLogDao
import com.mekki.taco.data.db.dao.DietDao
import com.mekki.taco.data.db.dao.DietItemDao
import com.mekki.taco.data.db.dao.FoodDao
import com.mekki.taco.data.db.entity.DailyLog
import com.mekki.taco.data.db.entity.DailyWaterLog
import com.mekki.taco.data.db.entity.Diet
import com.mekki.taco.data.db.entity.DietItem
import com.mekki.taco.data.db.entity.Food
import com.mekki.taco.data.db.entity.FoodFts
import kotlinx.coroutines.CoroutineScope

@Database(
    entities = [
        Food::class,
        Diet::class,
        DietItem::class,
        DailyLog::class,
        FoodFts::class,
        DailyWaterLog::class
    ],
    version = 3,
    exportSchema = true
)

abstract class AppDatabase : RoomDatabase() {

    abstract fun foodDao(): FoodDao
    abstract fun dietDao(): DietDao
    abstract fun dietItemDao(): DietItemDao
    abstract fun dailyLogDao(): DailyLogDao
    abstract fun dailyWaterLogDao(): DailyWaterLogDao

    companion object {
        // Reads and writes to this field are atomic and writes are always made visible to other threads
        @Volatile
        private var INSTANCE: AppDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE daily_log ADD COLUMN notes TEXT")
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // 1. Add UUID column (nullable, with default NULL for official foods)
                safeAddColumn(db, "foods", "uuid", "TEXT DEFAULT NULL")

                // 1.1 Add usageCount column
                safeAddColumn(db, "foods", "usageCount", "INTEGER NOT NULL DEFAULT 0")

                // 1.2 Add potentially missing nutritional columns (RE, RAE, lipids, amino acids)
                val missingColumns = listOf(
                    "RE",
                    "RAE",
                    "lipidios_total",
                    "lipidios_saturados",
                    "lipidios_monoinsaturados",
                    "lipidios_poliinsaturados",
                    "aminoacidos_triptofano",
                    "aminoacidos_treonina",
                    "aminoacidos_isoleucina",
                    "aminoacidos_leucina",
                    "aminoacidos_lisina",
                    "aminoacidos_metionina",
                    "aminoacidos_cistina",
                    "aminoacidos_fenilalanina",
                    "aminoacidos_tirosina",
                    "aminoacidos_valina",
                    "aminoacidos_arginina",
                    "aminoacidos_histidina",
                    "aminoacidos_alanina",
                    "aminoacidos_acidoAspartico",
                    "aminoacidos_acidoGlutamico",
                    "aminoacidos_glicina",
                    "aminoacidos_prolina",
                    "aminoacidos_serina"
                )

                for (column in missingColumns) {
                    safeAddColumn(db, "foods", column, "REAL")
                }

                // 2. Create unique index for UUID lookups
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_foods_uuid ON foods(uuid)")

                // 3. Generate UUIDs for existing custom foods
                val cursor = db.query("SELECT id FROM foods WHERE isCustom = 1 AND uuid IS NULL")
                while (cursor.moveToNext()) {
                    val id = cursor.getInt(0)
                    val uuid = java.util.UUID.randomUUID().toString()
                    db.execSQL("UPDATE foods SET uuid = ? WHERE id = ?", arrayOf<Any?>(uuid, id))
                }
                cursor.close()
            }

            private fun safeAddColumn(
                db: SupportSQLiteDatabase,
                table: String,
                column: String,
                type: String
            ) {
                var exists = false
                val cursor = db.query("PRAGMA table_info($table)")
                try {
                    val nameColumnIndex = cursor.getColumnIndex("name")
                    if (nameColumnIndex != -1) {
                        while (cursor.moveToNext()) {
                            val name = cursor.getString(nameColumnIndex)
                            if (name.equals(column, ignoreCase = true)) {
                                exists = true
                                break
                            }
                        }
                    }
                } finally {
                    cursor.close()
                }

                if (!exists) {
                    db.execSQL("ALTER TABLE $table ADD COLUMN $column $type")
                }
            }
        }

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            // Retorna a instância existente se já foi criada (padrão Singleton).
            // Caso contrário, cria a instância do banco de dados de forma segura para threads.
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "taco_database"
                )
                    .addCallback(AppDatabaseCallback(context.applicationContext, scope))
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}