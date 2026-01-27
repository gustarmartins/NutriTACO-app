package com.mekki.taco.data.db.entity

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Entity(tableName = "diets")
@Parcelize
data class Diet(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val name: String,
    val creationDate: Long,
    val calorieGoals: Double? = null,

    val isMain: Boolean = false
) : Parcelable