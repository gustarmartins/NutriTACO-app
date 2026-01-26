package com.mekki.taco.data.db.entity

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "foods",
    indices = [
        Index(value = ["name"]),
        Index(value = ["tacoID"], unique = true),
        Index(value = ["category"]),
        Index(value = ["uuid"], unique = true)
    ]
)

data class Food(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val tacoID: String,
    val name: String,
    val category: String,

    @ColumnInfo(defaultValue = "0")
    val isCustom: Boolean = false,

    // collision-free sharing between users
    @ColumnInfo(defaultValue = "NULL")
    val uuid: String? = null,

    @ColumnInfo(defaultValue = "0")
    val usageCount: Int = 0,

    val energiaKcal: Double?,
    val energiaKj: Double?,
    val proteina: Double?, // g
    val colesterol: Double?, // mg
    val carboidratos: Double?, // g
    val fibraAlimentar: Double?, // g
    val cinzas: Double?, // g
    val calcio: Double?, // g
    val magnesio: Double?, // mg
    val manganes: Double?, // mg
    val fosforo: Double?, // mg
    val ferro: Double?, // mg
    val sodio: Double?, // mg
    val potassio: Double?, // mg
    val cobre: Double?, // mg
    val zinco: Double?, // mg
    val retinol: Double?, // µg
    val RE: Double?, // µg
    val RAE: Double?, // µg
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
)

data class Lipidios(
    val total: Double?,
    val saturados: Double?,
    val monoinsaturados: Double?,
    val poliinsaturados: Double?
)

data class Aminoacidos(
    val triptofano: Double?,
    val treonina: Double?,
    val isoleucina: Double?,
    val leucina: Double?,
    val lisina: Double?,
    val metionina: Double?,
    val cistina: Double?,
    val fenilalanina: Double?,
    val tirosina: Double?,
    val valina: Double?,
    val arginina: Double?,
    val histidina: Double?,
    val alanina: Double?,
    val acidoAspartico: Double?,
    val acidoGlutamico: Double?,
    val glicina: Double?,
    val prolina: Double?,
    val serina: Double?
)


