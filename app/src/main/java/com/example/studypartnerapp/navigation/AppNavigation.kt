package com.example.studypartnerapp.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.studypartnerapp.ui.screens.*

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        // ... (existing routes)
        composable(Screen.Splash.route) { SplashScreen(navController) }
        composable(Screen.Login.route) { LoginScreen(navController) }
        composable(Screen.Register.route) { RegisterScreen(navController) }
        composable(Screen.Dashboard.route) { DashboardScreen(navController) }
        composable(Screen.Profile.route) { ProfileScreen(navController) }
        composable(Screen.FindPartners.route) { FindPartnersScreen(navController) }
        composable(Screen.Matches.route) { MatchesScreen(navController) }
        composable(Screen.Groups.route) { StudyGroupsScreen(navController) }
        composable(Screen.Notifications.route) { NotificationsScreen(navController) }
        composable(Screen.About.route) { AboutScreen(navController) }

        composable(Screen.ChatList.route) {
            ChatListScreen(
                navController = navController,
                onChatClick = { chatId, otherUserId, otherUserName ->
                    navController.navigate(Screen.Chat.createRoute(chatId, otherUserId, otherUserName))
                }
            )
        }

        composable(
            route = Screen.Chat.route,
            arguments = listOf(
                navArgument("chatId") { type = NavType.StringType },
                navArgument("otherUserId") { type = NavType.StringType },
                navArgument("otherUserName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val chatId = backStackEntry.arguments?.getString("chatId") ?: ""
            val otherUserId = backStackEntry.arguments?.getString("otherUserId") ?: ""
            val otherUserName = backStackEntry.arguments?.getString("otherUserName") ?: ""
            ChatScreen(
                chatId = chatId,
                otherUserId = otherUserId,
                otherUserName = otherUserName,
                onBackClick = { navController.popBackStack() },
                onProfileClick = { navController.navigate(Screen.PartnerProfile.createRoute(it)) }
            )
        }

        composable(
            route = Screen.PartnerProfile.route,
            arguments = listOf(navArgument("userId") { type = NavType.StringType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            PartnerProfileScreen(userId = userId, navController = navController)
        }

        composable(
            route = Screen.UnitResources.route,
            arguments = listOf(navArgument("unitName") { type = NavType.StringType })
        ) { backStackEntry ->
            val unitName = backStackEntry.arguments?.getString("unitName") ?: ""
            UnitResourcesScreen(unitName = unitName, navController = navController)
        }
    }
}
