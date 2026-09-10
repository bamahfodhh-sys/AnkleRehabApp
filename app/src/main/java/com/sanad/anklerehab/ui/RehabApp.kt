package com.sanad.anklerehab.ui

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.weight
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

private data class RootDestination(val route: String, val emoji: String, val label: String)
private val roots = listOf(
    RootDestination("today", "●", "اليوم"),
    RootDestination("plan", "▦", "الخطة"),
    RootDestination("history", "✓", "السجل"),
    RootDestination("settings", "⚙", "الإعدادات")
)

@Composable
fun RehabApp(viewModel: MainViewModel) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        val state by viewModel.state.collectAsStateWithLifecycle()
        val navController = rememberNavController()
        BoxWithConstraints(Modifier.fillMaxSize()) {
            val wide = maxWidth >= 600.dp
            if (wide) {
                Scaffold(containerColor = MaterialTheme.colorScheme.background) { inner ->
                    Row(Modifier.fillMaxSize().padding(inner)) {
                        RootNavigationRail(navController)
                        AppNavHost(navController, state, viewModel, Modifier.weight(1f))
                    }
                }
            } else {
                val entry by navController.currentBackStackEntryAsState()
                val route = entry?.destination?.route
                val showBottom = roots.any { it.route == route }
                Scaffold(
                    containerColor = MaterialTheme.colorScheme.background,
                    bottomBar = { if (showBottom) RootNavigationBar(navController) }
                ) { inner ->
                    AppNavHost(navController, state, viewModel, Modifier.padding(inner))
                }
            }
        }
    }
}

@Composable
private fun RootNavigationBar(navController: NavHostController) {
    val entry by navController.currentBackStackEntryAsState()
    val current = entry?.destination?.route
    NavigationBar {
        roots.forEach { item ->
            NavigationBarItem(
                selected = current == item.route,
                onClick = { navigateRoot(navController, item.route) },
                icon = { Text(item.emoji) },
                label = { Text(item.label) }
            )
        }
    }
}

@Composable
private fun RootNavigationRail(navController: NavHostController) {
    val entry by navController.currentBackStackEntryAsState()
    val current = entry?.destination?.route
    NavigationRail {
        roots.forEach { item ->
            NavigationRailItem(
                selected = current == item.route,
                onClick = { navigateRoot(navController, item.route) },
                icon = { Text(item.emoji) },
                label = { Text(item.label) }
            )
        }
    }
}

private fun navigateRoot(navController: NavHostController, route: String) {
    navController.navigate(route) {
        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

@Composable
private fun AppNavHost(
    navController: NavHostController,
    state: AppUiState,
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    NavHost(navController = navController, startDestination = "today", modifier = modifier.fillMaxSize()) {
        composable("today") {
            TodayScreen(state, viewModel, onOpenSession = { navController.navigate("session/$it") })
        }
        composable("plan") {
            PlanScreen(state, onOpenSession = { navController.navigate("session/$it") })
        }
        composable("history") {
            HistoryScreen(state, onOpenSession = { navController.navigate("session/$it") })
        }
        composable("settings") {
            SettingsScreen(state, viewModel)
        }
        composable(
            route = "session/{day}",
            arguments = listOf(navArgument("day") { type = NavType.IntType })
        ) { backStackEntry ->
            val day = backStackEntry.arguments?.getInt("day") ?: return@composable
            SessionScreen(
                programDay = day,
                state = state,
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
