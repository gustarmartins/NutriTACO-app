package com.mekki.taco.data.db.entity

import androidx.room.Entity
import androidx.room.Fts4

@Entity(tableName = "foods_fts")
@Fts4
data class FoodFts(
    val normalized_data: String
)