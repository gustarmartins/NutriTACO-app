package com.mekki.taco.data.model

import android.os.Parcelable
import androidx.room.Embedded
import androidx.room.Relation
import com.mekki.taco.data.db.entity.DietItem
import com.mekki.taco.data.db.entity.Food
import kotlinx.parcelize.Parcelize

@Parcelize
data class DietItemWithFood(
    @Embedded
    val dietItem: DietItem,

    @Relation(
        parentColumn = "foodId",
        entityColumn = "id"
    )
    val food: Food
) : Parcelable