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
    version = 2,
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
                    .addMigrations(MIGRATION_1_2)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}