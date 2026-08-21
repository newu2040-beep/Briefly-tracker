package com.example.ui.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Checklist
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.ui.components.CreateEditGoalBottomSheet
import com.example.ui.screens.GoalDetailsScreen
import com.example.ui.screens.GoalsScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.ReportsScreen
import com.example.ui.screens.WelcomeScreen
import com.example.ui.theme.IndigoPrimary
import com.example.ui.viewmodel.BrieflyViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrieflyApp(
    viewModel: BrieflyViewModel,
    navController: NavHostController = rememberNavController()
) {
    val isOnboardingCompleted by viewModel.isOnboardingCompleted.collectAsStateWithLifecycle()
    val startDestination = if (isOnboardingCompleted) Screen.Home.route else Screen.Welcome.route

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    var showCreateGoalSheet by remember { mutableStateOf(false) }

    val shouldShowBottomBar = currentRoute in listOf(
        Screen.Home.route,
        Screen.Goals.route,
        Screen.Reports.route,
        Screen.Profile.route
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (shouldShowBottomBar) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(16.dp, shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)),
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    NavigationBar(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .height(68.dp),
                        containerColor = Color.Transparent,
                        tonalElevation = 0.dp
                    ) {
                        val navItems = listOf(
                            NavigationItemData(
                                route = Screen.Home.route,
                                label = "Home",
                                selectedIcon = Icons.Filled.Home,
                                unselectedIcon = Icons.Outlined.Home,
                                tag = "nav_home"
                            ),
                            NavigationItemData(
                                route = Screen.Goals.route,
                                label = "Goals",
                                selectedIcon = Icons.Filled.Checklist,
                                unselectedIcon = Icons.Outlined.Checklist,
                                tag = "nav_goals"
                            ),
                            NavigationItemData(
                                route = Screen.Reports.route,
                                label = "Reports",
                                selectedIcon = Icons.Filled.BarChart,
                                unselectedIcon = Icons.Outlined.BarChart,
                                tag = "nav_reports"
                            ),
                            NavigationItemData(
                                route = Screen.Profile.route,
                                label = "Profile",
                                selectedIcon = Icons.Filled.Person,
                                unselectedIcon = Icons.Outlined.Person,
                                tag = "nav_profile"
                            )
                        )

                        navItems.forEach { item ->
                            val isSelected = currentRoute == item.route
                            NavigationBarItem(
                                selected = isSelected,
                                onClick = {
                                    if (currentRoute != item.route) {
                                        navController.navigate(item.route) {
                                            popUpTo(navController.graph.findStartDestination().id) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                },
                                icon = {
                                    Icon(
                                        imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                                        contentDescription = item.label,
                                        modifier = Modifier.size(24.dp)
                                    )
                                },
                                label = {
                                    Text(
                                        text = item.label,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            fontSize = 11.sp
                                        )
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = IndigoPrimary,
                                    selectedTextColor = IndigoPrimary,
                                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                    indicatorColor = Color(0xFFEEF2FF)
                                ),
                                modifier = Modifier.testTag(item.tag)
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Welcome.route) {
                WelcomeScreen(
                    onGetStarted = {
                        viewModel.completeOnboarding()
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Welcome.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.Home.route) {
                HomeScreen(
                    viewModel = viewModel,
                    onNavigateToGoalDetails = { goalId ->
                        navController.navigate(Screen.GoalDetails.createRoute(goalId))
                    },
                    onNavigateToGoalsList = {
                        navController.navigate(Screen.Goals.route)
                    },
                    onNavigateToReports = {
                        navController.navigate(Screen.Reports.route)
                    },
                    onOpenCreateGoalSheet = {
                        showCreateGoalSheet = true
                    },
                    onOpenNotificationsOrMenu = {
                        navController.navigate(Screen.Profile.route)
                    }
                )
            }

            composable(Screen.Goals.route) {
                GoalsScreen(
                    viewModel = viewModel,
                    onNavigateToGoalDetails = { goalId ->
                        navController.navigate(Screen.GoalDetails.createRoute(goalId))
                    },
                    onOpenCreateGoalSheet = {
                        showCreateGoalSheet = true
                    }
                )
            }

            composable(
                route = Screen.GoalDetails.route,
                arguments = listOf(navArgument("goalId") { type = NavType.LongType })
            ) { backStackEntry ->
                val goalId = backStackEntry.arguments?.getLong("goalId") ?: 0L
                GoalDetailsScreen(
                    goalId = goalId,
                    viewModel = viewModel,
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }

            composable(Screen.Reports.route) {
                ReportsScreen(
                    viewModel = viewModel
                )
            }

            composable(Screen.Profile.route) {
                ProfileScreen(
                    viewModel = viewModel
                )
            }
        }
    }

    // Global ModalBottomSheet for Creating Goal
    if (showCreateGoalSheet) {
        CreateEditGoalBottomSheet(
            onDismiss = { showCreateGoalSheet = false },
            onSaveGoal = { title, desc, cat, color, icon, target, unit, reminder, subtasks ->
                viewModel.createGoal(
                    title = title,
                    description = desc,
                    category = cat,
                    categoryColorHex = color,
                    categoryIconName = icon,
                    dailyTarget = target,
                    targetUnit = unit,
                    reminderTime = reminder,
                    subtasks = subtasks
                )
            }
        )
    }
}

private data class NavigationItemData(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val tag: String
)
