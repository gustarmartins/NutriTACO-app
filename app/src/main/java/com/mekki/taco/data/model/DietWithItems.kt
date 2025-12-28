package com.mekki.taco.data.model

import androidx.room.Embedded
import androidx.room.Relation
import com.mekki.taco.data.db.entity.Diet
import com.mekki.taco.data.db.entity.DietItem

data class DietaComItens(
    @Embedded
    val diet: Diet,

    @Relation(
        parentColumn = "id",
        entityColumn = "dietaId",
        entity = DietItem::class
    )
    val itens: List<DietItemWithFood>
)
