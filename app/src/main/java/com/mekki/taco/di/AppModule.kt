package com.mekki.taco.di

import android.content.Context
import com.mekki.taco.data.db.dao.DailyLogDao
import com.mekki.taco.data.db.dao.DailyWaterLogDao
import com.mekki.taco.data.db.dao.DietDao
import com.mekki.taco.data.db.dao.DietItemDao
import com.mekki.taco.data.db.dao.FoodDao
import com.mekki.taco.data.db.database.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideApplicationScope(): CoroutineScope {
        return CoroutineScope(SupervisorJob() + Dispatchers.IO)
    }

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context,
        scope: CoroutineScope
    ): AppDatabase {
        return AppDatabase.getDatabase(context, scope)
    }

    @Provides
    @Singleton
    fun provideFoodDao(database: AppDatabase): FoodDao {
        return database.foodDao()
    }

    @Provides
    @Singleton
    fun provideDietDao(database: AppDatabase): DietDao {
        return database.dietDao()
    }

    @Provides
    @Singleton
    fun provideDietItemDao(database: AppDatabase): DietItemDao {
        return database.dietItemDao()
    }

    @Provides
    @Singleton
    fun provideDailyLogDao(database: AppDatabase): DailyLogDao {
        return database.dailyLogDao()
    }

    @Provides
    @Singleton
    fun provideDailyWaterLogDao(database: AppDatabase): DailyWaterLogDao {
        return database.dailyWaterLogDao()
    }
}