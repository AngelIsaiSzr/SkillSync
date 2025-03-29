package com.ics.skillsync.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.ics.skillsync.ui.screens.*

@Composable
fun Navigation(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = "splash"
    ) {
        composable("splash") {
            SplashScreen(navController)
        }
        
        composable("home") {
            HomeScreen(navController)
        }
        composable("explore") {
            ExploreScreen(navController)
        }
        composable("chats") {
            ChatsScreen(navController)
        }
        composable("chat/{chatId}") { backStackEntry ->
            ChatDetailScreen(
                chatId = backStackEntry.arguments?.getString("chatId") ?: "",
                navController = navController
            )
        }
        composable("profile") {
            ProfileScreen(navController)
        }
        composable("sessions") {
            SessionsScreen(navController)
        }
        composable("skill/{skillId}") { backStackEntry ->
            SkillDetailScreen(
                skillId = backStackEntry.arguments?.getString("skillId") ?: "",
                navController = navController
            )
        }
    }
} 