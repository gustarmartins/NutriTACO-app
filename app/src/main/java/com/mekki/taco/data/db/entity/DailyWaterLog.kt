package com.mekki.taco.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_water")
data class DailyWaterLog(
    @PrimaryKey
    val date: String, // YYYY-MM-DD
    val quantityMl: Int = 0
)
