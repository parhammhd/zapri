package dev.parham.zapri

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import dev.parham.zapri.ui.navigation.NavRoutes
import dev.parham.zapri.ui.screen.AboutScreen
import dev.parham.zapri.ui.screen.BrowserScreen
import dev.parham.zapri.ui.screen.SettingsScreen

@Composable
fun NavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = NavRoutes.Browser.route,
        modifier = modifier
    ) {
        composable(NavRoutes.Browser.route) {
            BrowserScreen(navController = navController)
        }
        composable(NavRoutes.Settings.route) {
            SettingsScreen(navController = navController)
        }
        composable(NavRoutes.About.route) {
            AboutScreen(navController = navController)
        }
    }
}
