package com.mekki.taco.data.db.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
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
    // aumentar conforme atualizações da Tabela TACO oficial
    version = 1,
    exportSchema = true
)

abstract class AppDatabase : RoomDatabase() {

    abstract fun foodDao(): FoodDao
    abstract fun dietDao(): DietDao
    abstract fun dietItemDao(): DietItemDao
    abstract fun dailyLogDao(): DailyLogDao
    abstract fun dailyWaterLogDao(): DailyWaterLogDao

    companion object {
        // A anotação @Volatile garante que a variável INSTANCE seja sempre atualizada
        // e visível para todas as threads, prevenindo problemas de concorrência.
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            // Retorna a instância existente se já foi criada (padrão Singleton).
            // Caso contrário, cria a instância do banco de dados de forma segura para threads.
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "taco_database"
                )
                    // popula o banco na primeira chamada da DB - apenas caso não exista.
                    .addCallback(AppDatabaseCallback(context.applicationContext, scope))

                    // destrói o banco ao mudar versões
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}