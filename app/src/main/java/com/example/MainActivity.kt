package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.MilkHisabViewModel
import androidx.compose.material.icons.filled.ShowChart
import com.example.ui.screens.CustomersScreen
import com.example.ui.screens.DailyEntryScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.MonthlyAnalyticsScreen
import com.example.ui.screens.MonthlyBillScreen
import com.example.ui.screens.PaymentsScreen
import com.example.ui.screens.ToolsScreen
import com.example.ui.theme.DairyBluePrimary
import com.example.ui.theme.MilkHisabTheme

enum class AppScreen(val label: String, val icon: ImageVector) {
    HOME("Home", Icons.Default.Home),
    DAILY_ENTRY("Entry", Icons.Default.AddCircle),
    CUSTOMERS("Customers", Icons.Default.People),
    BILLS("Bills", Icons.Default.PictureAsPdf),
    ANALYTICS("Analytics", Icons.Default.ShowChart),
    TOOLS("Tools", Icons.Default.Build),
    PAYMENTS("Payments", Icons.Default.Payments)
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MilkHisabTheme {
                MilkHisabApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MilkHisabApp(viewModel: MilkHisabViewModel = viewModel()) {
    var currentScreen by remember { mutableStateOf(AppScreen.HOME) }
    val strings by viewModel.strings.collectAsState()
    val snackbarMessage by viewModel.snackbarMessage.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearSnackbarMessage()
        }
    }

    // Android Hardware Back button handling
    if (currentScreen != AppScreen.HOME) {
        BackHandler {
            currentScreen = AppScreen.HOME
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = when (currentScreen) {
                            AppScreen.HOME -> strings.appName
                            AppScreen.DAILY_ENTRY -> strings.dailyEntry
                            AppScreen.CUSTOMERS -> strings.customers
                            AppScreen.PAYMENTS -> strings.payments
                            AppScreen.BILLS -> strings.bills
                            AppScreen.ANALYTICS -> strings.monthlyAnalytics
                            AppScreen.TOOLS -> strings.tools
                        },
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                actions = {
                    // Quick language toggle
                    FilledTonalIconButton(
                        onClick = { viewModel.toggleLanguage() },
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = Color.White.copy(alpha = 0.2f),
                            contentColor = Color.White
                        ),
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .size(38.dp)
                            .testTag("topbar_lang_button")
                    ) {
                        Text(
                            text = strings.switchLanguage,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White
                )
            )
        },
        bottomBar = {
            val mainNavScreens = listOf(
                AppScreen.HOME,
                AppScreen.DAILY_ENTRY,
                AppScreen.CUSTOMERS,
                AppScreen.BILLS,
                AppScreen.ANALYTICS,
                AppScreen.TOOLS
            )
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp
            ) {
                mainNavScreens.forEach { screen ->
                    val screenLabel = when (screen) {
                        AppScreen.HOME -> strings.dashboard
                        AppScreen.DAILY_ENTRY -> strings.dailyEntry
                        AppScreen.CUSTOMERS -> strings.customers
                        AppScreen.PAYMENTS -> strings.payments
                        AppScreen.BILLS -> strings.bills
                        AppScreen.ANALYTICS -> strings.analytics
                        AppScreen.TOOLS -> strings.tools
                    }
                    NavigationBarItem(
                        selected = currentScreen == screen,
                        onClick = { currentScreen = screen },
                        icon = {
                            Icon(
                                imageVector = screen.icon,
                                contentDescription = screenLabel
                            )
                        },
                        label = {
                            Text(
                                text = screenLabel,
                                maxLines = 1,
                                style = MaterialTheme.typography.labelSmall
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = DairyBluePrimary,
                            selectedTextColor = DairyBluePrimary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        modifier = Modifier.testTag("nav_${screen.name.lowercase()}")
                    )
                }
            }
        }
    ) { innerPadding ->
        androidx.compose.foundation.layout.Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentScreen) {
                AppScreen.HOME -> HomeScreen(
                    viewModel = viewModel,
                    onNavigateToDailyEntry = { currentScreen = AppScreen.DAILY_ENTRY },
                    onNavigateToCustomers = { currentScreen = AppScreen.CUSTOMERS },
                    onNavigateToPayments = { currentScreen = AppScreen.PAYMENTS },
                    onNavigateToBills = { currentScreen = AppScreen.BILLS },
                    onNavigateToTools = { currentScreen = AppScreen.TOOLS },
                    onNavigateToAnalytics = { currentScreen = AppScreen.ANALYTICS }
                )
                AppScreen.DAILY_ENTRY -> DailyEntryScreen(
                    viewModel = viewModel,
                    onNavigateBack = { currentScreen = AppScreen.HOME }
                )
                AppScreen.CUSTOMERS -> CustomersScreen(
                    viewModel = viewModel,
                    onNavigateToBills = { currentScreen = AppScreen.BILLS }
                )
                AppScreen.PAYMENTS -> PaymentsScreen(
                    viewModel = viewModel
                )
                AppScreen.BILLS -> MonthlyBillScreen(
                    viewModel = viewModel
                )
                AppScreen.ANALYTICS -> MonthlyAnalyticsScreen(
                    viewModel = viewModel
                )
                AppScreen.TOOLS -> ToolsScreen(
                    viewModel = viewModel
                )
            }
        }
    }
}
