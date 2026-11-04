package za.co.sprinta.app.navigation

sealed class Screen(val route: String) {
    data object Splash : Screen("splash")
    data object Auth : Screen("auth")
    data object Dashboard : Screen("dashboard")
    data object History : Screen("history")
    data object Profile : Screen("profile")
    data object EditProfile : Screen("edit_profile")
    data object Performance : Screen("performance")
    data object Feed : Screen("feed")
    data object Leaderboard : Screen("leaderboard")
    data object About : Screen("about")
    data object Settings : Screen("settings")
    data object Tracking : Screen("tracking")
    data object Summary : Screen("summary")
    data object ProUpgrade : Screen("pro_upgrade")
    data object Gear : Screen("gear")
    data object SmartWatch : Screen("smartwatch")
}
