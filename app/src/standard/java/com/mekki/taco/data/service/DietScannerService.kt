package com.mekki.taco.data.service

import android.graphics.Bitmap
import com.mekki.taco.data.db.dao.FoodDao

class DietScannerService(private val foodDao: FoodDao) {
    suspend fun scanAndParseDiet(bitmap: Bitmap): List<ScannedItem> {
        return emptyList()
    }
}
