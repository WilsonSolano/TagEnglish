package com.example.tagenglish.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.tagenglish.ui.screens.home.HomeScreen
import com.example.tagenglish.ui.screens.test.WeeklyTestScreen
import com.example.tagenglish.ui.viewmodels.HomeViewModel
import com.example.tagenglish.ui.viewmodels.TestViewModel
import com.example.tagenglish.ui.viewmodels.ViewModelFactory

// ─── Rutas ────────────────────────────────────────────────────────────────────

sealed class Screen(val route: String) {
    object Home       : Screen("home")
    object WeeklyTest : Screen("weekly_test/{weekId}") {
        fun createRoute(weekId: Int) = "weekly_test/$weekId"
    }
}

// ─── NavGraph ─────────────────────────────────────────────────────────────────

@Composable
fun AppNavGraph(
    navController: NavHostController = rememberNavController()
) {
    val context = LocalContext.current
    val factory = ViewModelFactory(context)

    NavHost(
        navController    = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            val viewModel: HomeViewModel = viewModel(factory = factory)
            HomeScreen(
                viewModel    = viewModel,
                onStartTest  = { weekId ->
                    navController.navigate(Screen.WeeklyTest.createRoute(weekId))
                }
            )
        }

        composable(Screen.WeeklyTest.route) { backStackEntry ->
            val weekId    = backStackEntry.arguments?.getString("weekId")?.toIntOrNull() ?: 1
            val viewModel: TestViewModel = viewModel(factory = factory)
            WeeklyTestScreen(
                viewModel = viewModel,
                weekId    = weekId,
                onFinish  = { navController.popBackStack() }
            )
        }
    }
}
