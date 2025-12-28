package com.mekki.taco.data.model

import androidx.room.Embedded
import androidx.room.Relation
import com.mekki.taco.data.db.entity.Alimento
import com.mekki.taco.data.db.entity.ItemDieta

/**
 * Esta data class junta um [ItemDieta] com o [Alimento] correspondente.
 */
data class ItemDietaComAlimento(
    @Embedded
    val itemDieta: ItemDieta,

    @Relation(
        parentColumn = "alimentoId", // Coluna em ItemDieta
        entityColumn = "id"         // Coluna em Alimento que corresponde
    )
    val alimento: Alimento
)