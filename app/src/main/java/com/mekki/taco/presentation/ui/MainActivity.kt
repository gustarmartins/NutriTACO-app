package com.mekki.taco.presentation.ui

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
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
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.mekki.taco.data.sharing.DietSharingManager
import com.mekki.taco.data.sharing.NutriTacoFileType
import com.mekki.taco.presentation.navigation.AppNavigation
import com.mekki.taco.presentation.navigation.BottomNavItem
import com.mekki.taco.presentation.navigation.FOOD_DATABASE_ROUTE
import com.mekki.taco.presentation.navigation.HOME_ROUTE
import com.mekki.taco.presentation.navigation.SETTINGS_ROUTE
import com.mekki.taco.presentation.ui.profile.ProfileSheetContent
import com.mekki.taco.presentation.ui.profile.ProfileViewModel
import com.mekki.taco.presentation.ui.theme.NutriTACOTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var dietSharingManager: DietSharingManager

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    companion object {
        private const val TAG = "MainActivity_TACO"
        const val EXTRA_OPEN_SEARCH = "extra_open_search"
        const val EXTRA_OPEN_FOOD_DETAIL = "extra_open_food_detail"
        const val EXTRA_IMPORT_FILE_URI = "extra_import_file_uri"
        const val EXTRA_IMPORT_FILE_TYPE = "extra_import_file_type"
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

                // We want to handle widget intents on fresh start, not restoration
                // To avoid restoring to widget last state when kill
                val isFromWidget = savedInstanceState == null && (
                        intent?.getBooleanExtra(EXTRA_OPEN_SEARCH, false) == true ||
                                (intent?.getIntExtra(EXTRA_OPEN_FOOD_DETAIL, -1) ?: -1) > 0
                        )

                androidx.compose.runtime.LaunchedEffect(Unit) {
                    if (!isFromWidget) return@LaunchedEffect

                    val openSearch = intent?.getBooleanExtra(EXTRA_OPEN_SEARCH, false) == true
                    val openFoodId = intent?.getIntExtra(EXTRA_OPEN_FOOD_DETAIL, -1) ?: -1

                    intent?.removeExtra(EXTRA_OPEN_SEARCH)
                    intent?.removeExtra(EXTRA_OPEN_FOOD_DETAIL)

                    when {
                        openFoodId > 0 -> {
                            navController.navigate("food_detail/$openFoodId") {
                                popUpTo("home") { saveState = true }
                                launchSingleTop = true
                            }
                        }

                        openSearch -> {
                            navController.navigate("food_database") {
                                popUpTo("home") { saveState = true }
                                launchSingleTop = true
                            }
                        }
                    }
                }

                // Handle .json file intents
                val incomingUri = remember(savedInstanceState) {
                    if (savedInstanceState == null && intent?.action == android.content.Intent.ACTION_VIEW) {
                        intent?.data
                    } else null
                }
                var pendingImportUri by remember { mutableStateOf(incomingUri) }
                var detectedFileType by remember { mutableStateOf<NutriTacoFileType?>(null) }

                androidx.compose.runtime.LaunchedEffect(pendingImportUri) {
                    pendingImportUri?.let { uri ->
                        kotlinx.coroutines.withContext(Dispatchers.IO) {
                            detectedFileType = dietSharingManager.detectFileType(uri)
                        }
                        when (detectedFileType) {
                            NutriTacoFileType.DIET -> {
                                val encodedUri = java.net.URLEncoder.encode(uri.toString(), "UTF-8")
                                navController.navigate("diet_list?importUri=$encodedUri") {
                                    popUpTo("home") { saveState = true }
                                    launchSingleTop = true
                                }
                            }

                            NutriTacoFileType.BACKUP -> {
                                val encodedUri = java.net.URLEncoder.encode(uri.toString(), "UTF-8")
                                navController.navigate("$SETTINGS_ROUTE?importUri=$encodedUri") {
                                    popUpTo("home") { saveState = true }
                                    launchSingleTop = true
                                }
                            }

                            NutriTacoFileType.UNKNOWN, null -> {
                                android.widget.Toast.makeText(
                                    context,
                                    "Arquivo não reconhecido. Certifique-se de que seja um backup ou dieta.",
                                    android.widget.Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                        pendingImportUri = null
                    }
                }

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

                val shouldShowBottomBar = when {
                    currentRoute == "home" -> true
                    currentRoute?.startsWith("diet_list") == true -> true
                    currentRoute == "diary" -> true
                    currentRoute == FOOD_DATABASE_ROUTE -> true
                    else -> false
                }
                var isBottomBarVisible by remember { mutableStateOf(true) }

                val canNavigateBack = navController.previousBackStackEntry != null

                DisposableEffect(navBackStackEntry) {
                    val route = navBackStackEntry?.destination?.route
                    val defaultTitle = when {
                        route?.startsWith("create_diet") == true -> "Editar Dieta"
                        route == "diet_list" -> "Dietas"
                        route?.startsWith("diet_list") == true -> "Dietas"
                        route == "diary" -> "Diário"
                        route == SETTINGS_ROUTE -> "Configurações"
                        else -> "NutriTACO"
                    }
                    if (route != "diet_detail/{dietId}") {
                        screenTitle = defaultTitle
                    }
                    isBottomBarVisible = true
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
                                    navigationIconContentColor = MaterialTheme.colorScheme.onBackground,
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
                    },
                    bottomBar = {
                        AnimatedVisibility(
                            visible = shouldShowBottomBar && isBottomBarVisible,
                            enter = slideInVertically { it },
                            exit = slideOutVertically { it }
                        ) {
                            NavigationBar {
                                BottomNavItem.items.forEach { item ->
                                    val isSelected = currentRoute?.startsWith(item.route) == true
                                    NavigationBarItem(
                                        selected = isSelected,
                                        onClick = {
                                            if (!isSelected) {
                                                navController.navigate(item.route) {
                                                    popUpTo(HOME_ROUTE) { saveState = true }
                                                    launchSingleTop = true
                                                    restoreState = true
                                                }
                                            }
                                        },
                                        icon = {
                                            Icon(
                                                imageVector = item.icon,
                                                contentDescription = item.label
                                            )
                                        },
                                        label = { Text(item.label) }
                                    )
                                }
                            }
                        }
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
                        onTitleChange = { newTitle -> screenTitle = newTitle },
                        onBottomBarVisibilityChange = { isBottomBarVisible = it }
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