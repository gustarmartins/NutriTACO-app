package com.mekki.taco.data.db.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.mekki.taco.data.db.entity.Alimento
import com.mekki.taco.data.db.entity.Dieta
import com.mekki.taco.data.db.entity.ItemDieta
import com.mekki.taco.data.db.dao.AlimentoDao
import com.mekki.taco.data.db.dao.DietaDao
import com.mekki.taco.data.db.dao.ItemDietaDao
import kotlinx.coroutines.CoroutineScope

// para produção o exportSchema deve ser true

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

    abstract fun alimentoDao(): AlimentoDao
    abstract fun dietaDao(): DietaDao
    abstract fun itemDietaDao(): ItemDietaDao

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