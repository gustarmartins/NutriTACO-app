package com.mekki.taco.data.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "daily_log",
    foreignKeys = [
        ForeignKey(
            entity = Food::class,
            parentColumns = ["id"],
            childColumns = ["foodId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["foodId"]), Index(value = ["date"])]
)
data class DailyLog(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val foodId: Int,

    val date: String,

    val quantityGrams: Double,
    val mealType: String,

    val entryTimestamp: Long = 0L,

    val isConsumed: Boolean = false,

    val originalQuantityGrams: Double? = null,

    val notes: String? = null,

    val sortOrder: Int = 0
)