package com.mekki.taco.data.model

import androidx.room.Embedded
import androidx.room.Relation
import com.mekki.taco.data.db.entity.DietItem
import com.mekki.taco.data.db.entity.Food

data class DietItemWithFood(
    @Embedded
    val dietItem: DietItem,

    @Relation(
        parentColumn = "foodId",
        entityColumn = "id"
    )
    val food: Food
)