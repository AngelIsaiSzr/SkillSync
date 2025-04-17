package com.ics.skillsync.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.ics.skillsync.ui.screens.*
import com.ics.skillsync.ui.viewmodel.ProfileViewModel
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun Navigation(
    navController: NavHostController,
    startDestination: String = "splash"
) {
    val viewModel: ProfileViewModel = viewModel()
    
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable("splash") {
            SplashScreen(navController = navController)
        }
        
        composable("home") {
            HomeScreen(navController = navController, viewModel = viewModel)
        }
        composable("explore") {
            ExploreScreen(navController = navController, viewModel = viewModel)
        }
        composable("chats") {
            ChatsScreen(navController = navController, viewModel = viewModel)
        }
        composable("profile") {
            ProfileScreen(navController = navController, viewModel = viewModel)
        }
        composable("edit_profile") {
            EditProfileScreen(navController = navController, viewModel = viewModel)
        }
        composable("search") {
            SearchScreen(navController = navController)
        }
        composable("sessions") {
            SessionsScreen(navController = navController)
        }
        composable("settings") {
            SettingsScreen(navController = navController)
        }
        composable("chat/{chatId}") { backStackEntry ->
            ChatDetailScreen(
                chatId = backStackEntry.arguments?.getString("chatId") ?: "",
                navController = navController
            )
        }
        composable("skill/{skillId}") { backStackEntry ->
            SkillDetailScreen(
                skillId = backStackEntry.arguments?.getString("skillId") ?: "",
                navController = navController
            )
        }
    }
} 