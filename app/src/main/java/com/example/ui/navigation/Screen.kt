package com.example.ui.navigation

sealed class Screen(val route: String) {
    object Welcome : Screen("welcome")
    object Home : Screen("home")
    object Goals : Screen("goals")
    object GoalDetails : Screen("goal_details/{goalId}") {
        fun createRoute(goalId: Long) = "goal_details/$goalId"
    }
    object Reports : Screen("reports")
    object Profile : Screen("profile")
}

sealed class BottomNavItem(
    val route: String,
    val title: String,
    val iconName: String
) {
    object Home : BottomNavItem(Screen.Home.route, "Home", "home")
    object Goals : BottomNavItem(Screen.Goals.route, "Goals", "track_changes")
    object Reports : BottomNavItem(Screen.Reports.route, "Reports", "bar_chart")
    object Profile : BottomNavItem(Screen.Profile.route, "Profile", "person")
}
