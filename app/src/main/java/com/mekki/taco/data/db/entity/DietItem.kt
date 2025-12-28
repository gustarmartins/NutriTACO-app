package com.mekki.taco.data.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "itens_dieta",
    foreignKeys = [
        ForeignKey(
            entity = Dieta::class,
            parentColumns = ["id"],
            childColumns = ["dietaId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Food::class,
            parentColumns = ["id"],
            childColumns = ["alimentoId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["dietaId"]), Index(value = ["alimentoId"])]
)
data class ItemDieta(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val dietaId: Int,
    val alimentoId: Int,

    val quantidadeGramas: Double,
    val tipoRefeicao: String? = null,
    val horaConsumo: String? = null
)