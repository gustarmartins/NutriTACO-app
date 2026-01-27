package com.mekki.taco.presentation.navigation

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.mekki.taco.presentation.ui.database.FoodDatabaseScreen
import com.mekki.taco.presentation.ui.database.FoodDatabaseViewModel
import com.mekki.taco.presentation.ui.diary.DiaryScreen
import com.mekki.taco.presentation.ui.diary.DiaryViewModel
import com.mekki.taco.presentation.ui.diet.DietDetailScreen
import com.mekki.taco.presentation.ui.diet.DietDetailViewModel
import com.mekki.taco.presentation.ui.diet.DietListScreen
import com.mekki.taco.presentation.ui.diet.DietListViewModel
import com.mekki.taco.presentation.ui.fooddetail.FoodDetailScreen
import com.mekki.taco.presentation.ui.fooddetail.FoodDetailViewModel
import com.mekki.taco.presentation.ui.home.HomeScreen
import com.mekki.taco.presentation.ui.home.HomeViewModel
import com.mekki.taco.presentation.ui.profile.ProfileViewModel
import com.mekki.taco.presentation.ui.search.FoodSearchScreen
import com.mekki.taco.presentation.ui.search.FoodViewModel
import com.mekki.taco.presentation.ui.settings.SettingsScreen
import com.mekki.taco.presentation.ui.settings.SettingsViewModel

const val HOME_ROUTE = "home"
const val DIET_LIST_ROUTE = "diet_list"
const val DIET_DETAIL_ROUTE = "diet_detail"
const val FOOD_DETAIL_ROUTE = "food_detail"
const val DIARY_ROUTE = "diary"
const val FOOD_SEARCH_ROUTE = "food_search"
const val FOOD_DATABASE_ROUTE = "food_database"
const val SETTINGS_ROUTE = "settings"

@Composable
fun AppNavigation(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    context: Context,
    onFabChange: (@Composable (() -> Unit)?) -> Unit,
    onActionsChange: (@Composable (() -> Unit)?) -> Unit,
    onTitleChange: (String) -> Unit
) {
    val homeViewModel: HomeViewModel = hiltViewModel()
    val profileViewModel: ProfileViewModel = hiltViewModel()
    val dietListViewModel: DietListViewModel = hiltViewModel()
    val settingsViewModel: SettingsViewModel = hiltViewModel()

    NavHost(
        modifier = modifier,
        navController = navController,
        startDestination = HOME_ROUTE
    ) {
        composable(HOME_ROUTE) {
            onFabChange(null)
            onActionsChange(null)
            onTitleChange("NutriTACO")
            HomeScreen(
                homeViewModel = homeViewModel,
                profileViewModel = profileViewModel,
                onNavigateToDietList = { navController.navigate(DIET_LIST_ROUTE) },
                onNavigateToCreateDiet = { navController.navigate("$DIET_DETAIL_ROUTE/-1") },
                onNavigateToDiary = { navController.navigate(DIARY_ROUTE) },
                onNavigateToDetail = { foodId ->
                    navController.navigate("$FOOD_DETAIL_ROUTE/$foodId")
                },
                onNavigateToEdit = { foodId ->
                    navController.navigate("$FOOD_DETAIL_ROUTE/$foodId?edit=true")
                },
                onNavigateToDietDetail = { dietId ->
                    navController.navigate("$DIET_DETAIL_ROUTE/$dietId")
                },
                onNavigateToSearch = { searchTerm ->
                    navController.navigate("$FOOD_SEARCH_ROUTE?term=$searchTerm")
                },
                onNavigateToDatabase = { navController.navigate(FOOD_DATABASE_ROUTE) },
                onNavigateToSettings = { navController.navigate(SETTINGS_ROUTE) }
            )
        }

        composable(DIARY_ROUTE) {
            onFabChange(null)
            val diaryViewModel: DiaryViewModel = hiltViewModel()
            DiaryScreen(
                viewModel = diaryViewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToDetail = { foodId ->
                    navController.navigate("$FOOD_DETAIL_ROUTE/$foodId")
                },
                onActionsChange = onActionsChange
            )
        }

        composable(DIET_LIST_ROUTE) {
            DietListScreen(
                viewModel = dietListViewModel,
                onNavigateToCreateDiet = { navController.navigate("$DIET_DETAIL_ROUTE/-1") },
                onNavigateToDietDetail = { dietId ->
                    navController.navigate("$DIET_DETAIL_ROUTE/$dietId")
                },
                onEditDiet = { dietId ->
                    navController.navigate("$DIET_DETAIL_ROUTE/$dietId")
                },
                onFabChange = onFabChange,
                onActionsChange = onActionsChange
            )
        }

        composable(
            route = "$DIET_DETAIL_ROUTE/{dietId}",
            arguments = listOf(navArgument("dietId") { type = NavType.IntType })
        ) { backStackEntry ->
            // Hilt ViewModel - dietId is automatically injected via SavedStateHandle
            val dietDetailViewModel: DietDetailViewModel = hiltViewModel()

            // Observe updates from FoodDetailScreen
            val savedStateHandle = backStackEntry.savedStateHandle
            val modifiedFoodId by savedStateHandle.getLiveData<Int>("modified_food_id")
                .observeAsState()

            androidx.compose.runtime.LaunchedEffect(modifiedFoodId) {
                modifiedFoodId?.let { id ->
                    dietDetailViewModel.onFoodUpdated(id)
                    savedStateHandle.remove<Int>("modified_food_id")
                }
            }

            DietDetailScreen(
                viewModel = dietDetailViewModel,
                profileViewModel = profileViewModel,
                onEditDiet = { /* Unused */ },
                onEditFood = { foodId ->
                    navController.navigate("$FOOD_DETAIL_ROUTE/$foodId?edit=true")
                },
                onViewFood = { foodId ->
                    navController.navigate("$FOOD_DETAIL_ROUTE/$foodId")
                },
                onTitleChange = onTitleChange,
                onFabChange = onFabChange,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToSettings = { navController.navigate(SETTINGS_ROUTE) }
            )
        }

        composable(
            route = "$FOOD_DETAIL_ROUTE/{foodId}?edit={edit}&addToDietContext={addToDietContext}&dietName={dietName}",
            arguments = listOf(
                navArgument("foodId") { type = NavType.IntType },
                navArgument("edit") { type = NavType.BoolType; defaultValue = false },
                navArgument("addToDietContext") { type = NavType.BoolType; defaultValue = false },
                navArgument("dietName") {
                    type = NavType.StringType; nullable = true; defaultValue = null
                }
            )
        ) { backStackEntry ->
            onFabChange(null)
            val foodId = backStackEntry.arguments?.getInt("foodId") ?: 0
            val isEdit = backStackEntry.arguments?.getBoolean("edit") ?: false
            val isAddToDietContext =
                backStackEntry.arguments?.getBoolean("addToDietContext") ?: false
            val dietName = backStackEntry.arguments?.getString("dietName")

            // Hilt ViewModel - foodId and edit are injected via SSH
            val foodDetailViewModel: FoodDetailViewModel = hiltViewModel()
            
            val uiState by foodDetailViewModel.uiState.collectAsState()
            val availableDiets by homeViewModel.availableDiets.collectAsState()

            FoodDetailScreen(
                uiState = uiState,
                availableDiets = availableDiets,
                onPortionChange = foodDetailViewModel::updatePortion,
                onNavigateBack = { navController.popBackStack() },
                onTitleChange = onTitleChange,
                onEditToggle = foodDetailViewModel::onEditToggle,
                onSave = {
                    foodDetailViewModel.saveChanges { newId ->
                        navController.previousBackStackEntry?.savedStateHandle?.set(
                            "modified_food_id",
                            newId
                        )

                        val prevRoute = navController.previousBackStackEntry?.destination?.route
                        if (prevRoute?.startsWith(FOOD_DETAIL_ROUTE) == true ||
                            prevRoute?.startsWith(DIET_DETAIL_ROUTE) == true
                        ) {
                            navController.popBackStack()
                        } else {
                            // If we came directly to edit (e.g. from Home), replace Edit screen with View screen
                            navController.navigate("$FOOD_DETAIL_ROUTE/$newId") {
                                popUpTo(
                                    navController.currentBackStackEntry?.destination?.id ?: 0
                                ) { inclusive = true }
                            }
                        }
                    }
                },
                onClone = foodDetailViewModel::cloneAndGetId,
                onDelete = foodDetailViewModel::deleteFood,
                onEditFieldChange = foodDetailViewModel::onEditFieldChange,
                onNavigateToNewFood = { newId ->
                    navController.popBackStack()
                    navController.navigate("$FOOD_DETAIL_ROUTE/$newId?edit=true&addToDietContext=$isAddToDietContext&dietName=$dietName")
                },
                onAddToDiet = { dietId, qty, meal, time ->
                    homeViewModel.addFoodToDiet(dietId, foodId, qty, meal, time)
                },
                onFastAdd = if (isAddToDietContext) {
                    { portion ->
                        navController.popBackStack()
                    }
                } else null,
                targetDietName = dietName
            )
        }

        composable(
            route = "$FOOD_SEARCH_ROUTE?term={term}",
            arguments = listOf(navArgument("term") {
                type = NavType.StringType
                defaultValue = ""
                nullable = true
            })
        ) { backStackEntry ->
            onFabChange(null)
            onTitleChange("Buscar Alimentos")
            val initialTerm = backStackEntry.arguments?.getString("term") ?: ""

            val foodViewModel: FoodViewModel = hiltViewModel()

            // Initializes search if term provided
            androidx.compose.runtime.LaunchedEffect(initialTerm) {
                if (initialTerm.isNotBlank()) {
                    foodViewModel.onTermoBuscaChange(initialTerm)
                }
            }

            FoodSearchScreen(
                viewModel = foodViewModel,
                onAlimentoClick = { foodId ->
                    navController.navigate("$FOOD_DETAIL_ROUTE/$foodId")
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(FOOD_DATABASE_ROUTE) {
            onFabChange(null)
            val viewModel: FoodDatabaseViewModel = hiltViewModel()
            FoodDatabaseScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onFoodClick = { foodId ->
                    navController.navigate("$FOOD_DETAIL_ROUTE/$foodId")
                }
            )
        }

        composable(SETTINGS_ROUTE) {
            onFabChange(null)
            onActionsChange(null)
            onTitleChange("Configurações")
            SettingsScreen(
                viewModel = settingsViewModel
            )
        }
    }
}