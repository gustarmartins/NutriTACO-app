package com.mekki.taco.presentation.ui

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.hilt.navigation.compose.hiltViewModel
import com.mekki.taco.presentation.navigation.AppNavigation
import com.mekki.taco.presentation.navigation.FOOD_DATABASE_ROUTE
import com.mekki.taco.presentation.navigation.SETTINGS_ROUTE
import com.mekki.taco.presentation.ui.profile.ProfileSheetContent
import com.mekki.taco.presentation.ui.profile.ProfileViewModel
import com.mekki.taco.presentation.ui.theme.NutriTACOTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    companion object {
        private const val TAG = "MainActivity_TACO"
    }

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate: Iniciando MainActivity.")

        enableEdgeToEdge()

        setContent {
            val context = LocalContext.current
            val profileViewModel: ProfileViewModel = hiltViewModel()
            val uiState by profileViewModel.uiState.collectAsState()

            NutriTACOTheme(darkTheme = uiState.userProfile.isDarkMode) {
                val navController = rememberNavController()
                var screenTitle by rememberSaveable { mutableStateOf("NutriTACO") }
                var fab: @Composable (() -> Unit)? by remember { mutableStateOf(null) }
                var extraActions: @Composable (() -> Unit) by remember { mutableStateOf({}) }

                val sheetState = rememberModalBottomSheetState()
                val scope = rememberCoroutineScope()
                var showBottomSheet by remember { mutableStateOf(false) }

                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                val shouldShowGlobalTopBar = when {
                    currentRoute?.startsWith("food_detail") == true -> false
                    currentRoute?.startsWith("create_diet") == true -> false
                    currentRoute?.startsWith("diet_detail") == true -> false
                    currentRoute == "home" -> false
                    currentRoute == "diary" -> false
                    currentRoute == FOOD_DATABASE_ROUTE -> false
                    else -> true
                }

                val canNavigateBack = navController.previousBackStackEntry != null

                DisposableEffect(navBackStackEntry) {
                    val route = navBackStackEntry?.destination?.route
                    val defaultTitle = when {
                        route?.startsWith("create_diet") == true -> "Editar Dieta"
                        route == "diet_list" -> "Minhas Dietas"
                        route == "diary" -> "Diário Alimentar"
                        route == SETTINGS_ROUTE -> "Configurações"
                        else -> "NutriTACO"
                    }
                    if (route != "diet_detail/{dietId}") {
                        screenTitle = defaultTitle
                    }
                    onDispose {}
                }

                Scaffold(
                    topBar = {
                        if (shouldShowGlobalTopBar) {
                            TopAppBar(
                                title = { Text(text = screenTitle) },
                                colors = TopAppBarDefaults.topAppBarColors(
                                    containerColor = MaterialTheme.colorScheme.background,
                                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                                    actionIconContentColor = MaterialTheme.colorScheme.onBackground
                                ),
                                navigationIcon = {
                                    if (canNavigateBack && currentRoute != "home") {
                                        IconButton(onClick = { navController.navigateUp() }) {
                                            Icon(
                                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                                contentDescription = "Voltar"
                                            )
                                        }
                                    }
                                },
                                actions = {
                                    extraActions()
                                    IconButton(onClick = { showBottomSheet = true }) {
                                        Icon(
                                            imageVector = Icons.Default.AccountCircle,
                                            contentDescription = "Abrir Perfil"
                                        )
                                    }
                                }
                            )
                        }
                    },
                    floatingActionButton = {
                        fab?.invoke()
                    }
                ) { innerPadding ->
                    val contentPadding =
                        if (shouldShowGlobalTopBar) innerPadding else PaddingValues(bottom = innerPadding.calculateBottomPadding())

                    AppNavigation(
                        navController = navController,
                        modifier = Modifier.padding(contentPadding),
                        context = LocalContext.current,
                        onFabChange = { newFab -> fab = newFab },
                        onActionsChange = { newActions -> extraActions = newActions ?: {} },
                        onTitleChange = { newTitle -> screenTitle = newTitle }
                    )
                }

                if (showBottomSheet) {
                    ModalBottomSheet(
                        onDismissRequest = { showBottomSheet = false },
                        sheetState = sheetState
                    ) {
                        ProfileSheetContent(
                            viewModel = profileViewModel,
                            onDismiss = {
                                scope.launch { sheetState.hide() }.invokeOnCompletion {
                                    if (!sheetState.isVisible) {
                                        showBottomSheet = false
                                    }
                                }
                            },
                            onNavigateToSettings = {
                                scope.launch { sheetState.hide() }.invokeOnCompletion {
                                    showBottomSheet = false
                                    navController.navigate(SETTINGS_ROUTE)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}