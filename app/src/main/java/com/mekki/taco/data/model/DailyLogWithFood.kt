package com.mekki.taco.data.model

import androidx.room.Embedded
import androidx.room.Relation
import com.mekki.taco.data.db.entity.DailyLog
import com.mekki.taco.data.db.entity.Food

data class DailyLogWithFood(
    @Embedded val log: DailyLog,

    @Relation(
        parentColumn = "foodId",
        entityColumn = "id"
    )
    val food: Food
)