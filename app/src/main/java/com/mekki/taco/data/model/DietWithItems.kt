package com.mekki.taco.data.model

import androidx.room.Embedded
import androidx.room.Relation
import com.mekki.taco.data.db.entity.Diet
import com.mekki.taco.data.db.entity.DietItem

data class DietWithItems(
    @Embedded
    val diet: Diet,

    @Relation(
        parentColumn = "id",
        entityColumn = "dietId",
        entity = DietItem::class
    )
    val items: List<DietItemWithFood>
)
