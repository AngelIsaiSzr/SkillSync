package com.ics.skillsync.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.ics.skillsync.ui.screens.*
import com.ics.skillsync.ui.viewmodel.ProfileViewModel
import com.ics.skillsync.ui.viewmodel.SkillViewModel
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun Navigation(
    navController: NavHostController,
    startDestination: String = "splash"
) {
    val profileViewModel: ProfileViewModel = viewModel()
    val skillViewModel: SkillViewModel = viewModel()
    
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable("splash") {
            SplashScreen(navController = navController)
        }
        
        composable("home") {
            HomeScreen(navController = navController, viewModel = profileViewModel)
        }
        composable("explore") {
            ExploreScreen(navController = navController, viewModel = profileViewModel)
        }
        composable("chats") {
            ChatsScreen(navController = navController, viewModel = profileViewModel)
        }
        composable("profile") {
            ProfileScreen(navController = navController, viewModel = profileViewModel)
        }
        composable("edit_profile") {
            EditProfileScreen(navController = navController, viewModel = profileViewModel)
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
        composable("skills") {
            SkillsScreen(
                navController = navController,
                skillViewModel = skillViewModel,
                profileViewModel = profileViewModel
            )
        }
    }
} 