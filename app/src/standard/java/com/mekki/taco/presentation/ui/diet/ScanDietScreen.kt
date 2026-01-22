package com.mekki.taco.presentation.ui.diet

import android.graphics.Bitmap
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun ScanDietScreen(
    onPhotoCaptured: (Bitmap) -> Unit,
    onCancel: () -> Unit
) {
    BackHandler {
        onCancel()
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("As funcionalidades IA não estão presentes nessa versão.")
    }
}
