package com.mekki.taco.data.db.entity

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Entity(
    tableName = "foods",
    indices = [
        Index(value = ["name"]),
        Index(value = ["tacoID"], unique = true),
        Index(value = ["category"]),
        Index(value = ["uuid"], unique = true),
        Index(value = ["source"])
    ]
)
@Parcelize
data class Food(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val tacoID: String,
    val name: String,
    val category: String,

    @Deprecated("Value is deprecated and source should be used instead. Kept for UUID/sharing logic compatibility.")
    @ColumnInfo(defaultValue = "0")
    val isCustom: Boolean = false,

    // data source for future additions "TACO", "CUSTOM", "TBCA", "IBGE" etc.
    @ColumnInfo(defaultValue = "NULL")
    val source: String? = null,

    // collision-free sharing between users
    @ColumnInfo(defaultValue = "NULL")
    val uuid: String? = null,

    @ColumnInfo(defaultValue = "0")
    val usageCount: Int = 0,

    val energiaKcal: Double?, // kcal
    val energiaKj: Double?,
    val proteina: Double?, // g
    val colesterol: Double?, // mg
    val carboidratos: Double?, // g
    val fibraAlimentar: Double?, // g
    val cinzas: Double?, // g
    val calcio: Double?, // mg
    val magnesio: Double?, // mg
    val manganes: Double?, // mg
    val fosforo: Double?, // mg
    val ferro: Double?, // mg
    val sodio: Double?, // mg
    val potassio: Double?, // mg
    val cobre: Double?, // mg
    val zinco: Double?, // mg
    val retinol: Double?, // µg (mcg)
    val RE: Double?, // µg (mcg)
    val RAE: Double?, // µg (mcg)
    val tiamina: Double?, // mg
    val riboflavina: Double?, // mg
    val piridoxina: Double?, // mg
    val niacina: Double?, // mg
    val vitaminaC: Double?, // mg
    val umidade: Double?, // %

    @Embedded(prefix = "lipidios_")
    val lipidios: Lipidios?,

    @Embedded(prefix = "aminoacidos_")
    val aminoacidos: Aminoacidos?
) : Parcelable

@Parcelize
data class Lipidios(
    val total: Double?, // sum of three below in g
    val saturados: Double?, // g
    val monoinsaturados: Double?, // g
    val poliinsaturados: Double? // g
) : Parcelable

@Parcelize
data class Aminoacidos(
    val triptofano: Double?, // g
    val treonina: Double?, // g
    val isoleucina: Double?, // g
    val leucina: Double?, // g
    val lisina: Double?, // g
    val metionina: Double?, // g
    val cistina: Double?, // g
    val fenilalanina: Double?, // g
    val tirosina: Double?, // g
    val valina: Double?, // g
    val arginina: Double?, // g
    val histidina: Double?, // g
    val alanina: Double?, // g
    val acidoAspartico: Double?, // g
    val acidoGlutamico: Double?, // g
    val glicina: Double?, // g
    val prolina: Double?, // g
    val serina: Double? // g
) : Parcelable