package com.mekki.taco.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "dietas")
data class Dieta(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val nome: String,
    val dataCriacao: Long,
    val objetivoCalorias: Double? = null
)