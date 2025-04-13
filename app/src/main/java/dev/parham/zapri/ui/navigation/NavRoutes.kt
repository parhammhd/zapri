package dev.parham.zapri.ui.navigation

sealed class NavRoutes(val route: String) {
    object Browser : NavRoutes("browser")
    object Settings : NavRoutes("settings")
    object About : NavRoutes("about")
}
