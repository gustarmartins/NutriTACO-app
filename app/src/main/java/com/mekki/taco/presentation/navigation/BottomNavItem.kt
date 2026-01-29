package com.mekki.taco.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector
) {
    data object Home : BottomNavItem(HOME_ROUTE, "Início", Icons.Default.Home)
    data object Diets : BottomNavItem(DIET_LIST_ROUTE, "Dietas", Icons.Default.Book)
    data object Diary : BottomNavItem(DIARY_ROUTE, "Diário", Icons.Default.EditCalendar)
    data object FoodDatabase :
        BottomNavItem(FOOD_DATABASE_ROUTE, "Alimentos", Icons.Default.Restaurant)

    companion object {
        val items = listOf(Home, Diets, Diary, FoodDatabase)
    }
}
