package com.mekki.taco.data.db.entity

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Entity(
    tableName = "diet_items",
    foreignKeys = [
        ForeignKey(
            entity = Diet::class,
            parentColumns = ["id"],
            childColumns = ["dietId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Food::class,
            parentColumns = ["id"],
            childColumns = ["foodId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["dietId"]), Index(value = ["foodId"])]
)
@Parcelize
data class DietItem(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val dietId: Int,
    val foodId: Int,

    val quantityGrams: Double,
    val mealType: String? = null,
    val consumptionTime: String? = null,
    val sortOrder: Int = 0
) : Parcelable