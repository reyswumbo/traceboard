package com.traceboard.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.lifecycle.viewmodel.compose.viewModel
import com.traceboard.app.ui.navigation.Screen
import com.traceboard.app.ui.screens.clipboard.ClipboardScreen
import com.traceboard.app.ui.screens.dashboard.DashboardScreen
import com.traceboard.app.ui.screens.usage.UsageScreen
import com.traceboard.app.ui.screens.writing.WritingScreen
import com.traceboard.app.ui.theme.TraceboardTheme
import com.traceboard.app.viewmodel.ClipboardViewModel
import com.traceboard.app.viewmodel.DashboardViewModel
import com.traceboard.app.viewmodel.UsageViewModel
import com.traceboard.app.viewmodel.ViewModelFactory
import com.traceboard.app.viewmodel.WritingViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            TraceboardTheme {
                val app = application as TraceboardApplication
                val settingsRepo = remember { com.traceboard.app.data.repository.SettingsRepository(app) }
                val themeMode by settingsRepo.themeMode.collectAsStateWithLifecycle(initialValue = com.traceboard.app.ui.theme.ThemeMode.DEFAULT)
                val factory = factory(app)
                RequestNotificationPermissionIfNeeded()
                com.traceboard.app.ui.theme.TraceboardTheme(themeMode = themeMode) {
                    TraceboardApp(factory)
                }
            }
        }
    }
}

@Composable
private fun RequestNotificationPermissionIfNeeded() {
    val context = LocalContext.current
    val launcher = androidx.activity.compose.rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }
    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}

private fun factory(app: TraceboardApplication): ViewModelFactory {
    return ViewModelFactory(
        app,
        clipboardRepository = com.traceboard.app.data.repository.ClipboardRepository(app),
        trackedWordRepository = com.traceboard.app.data.repository.TrackedWordRepository(app),
        settingsRepository = com.traceboard.app.data.repository.SettingsRepository(app),
        usageRepository = com.traceboard.app.data.repository.UsageRepository(app)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TraceboardApp(factory: ViewModelFactory) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val bottomScreens = listOf(Screen.Dashboard, Screen.Clipboard, Screen.Writing, Screen.Usage)

    Scaffold(
        bottomBar = {
            NavigationBar {
                bottomScreens.forEach { screen ->
                    NavigationBarItem(
                        selected = currentRoute == screen.route,
                        onClick = {
                            if (currentRoute != screen.route) {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        icon = { Icon(screen.icon, contentDescription = screen.title) },
                        label = { Text(screen.title) }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Dashboard.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Dashboard.route) {
                val vm: DashboardViewModel = viewModel(factory = factory)
                DashboardScreen(vm)
            }
            composable(Screen.Clipboard.route) {
                val vm: ClipboardViewModel = viewModel(factory = factory)
                ClipboardScreen(vm)
            }
            composable(Screen.Writing.route) {
                val vm: WritingViewModel = viewModel(factory = factory)
                WritingScreen(vm)
            }
            composable(Screen.Usage.route) {
                val vm: UsageViewModel = viewModel(factory = factory)
                UsageScreen(vm)
            }
        }
    }
}