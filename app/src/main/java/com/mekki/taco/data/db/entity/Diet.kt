package com.mekki.taco.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "diets")
data class Diet(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val name: String,
    val creationDate: Long,
    val calorieGoals: Double? = null,

    val isMain: Boolean = false
)