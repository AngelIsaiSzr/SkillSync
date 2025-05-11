package com.ics.skillsync.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.ics.skillsync.ui.screens.*
import com.ics.skillsync.ui.viewmodel.ProfileViewModel
import com.ics.skillsync.ui.viewmodel.SkillViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ics.skillsync.ui.viewmodel.TeachingCardViewModel
import com.ics.skillsync.ui.viewmodel.NetworkViewModel
import com.ics.skillsync.ui.viewmodel.ChatViewModel

@Composable
fun Navigation(
    navController: NavHostController,
    startDestination: String = "splash"
) {
    val profileViewModel: ProfileViewModel = viewModel()
    val skillViewModel: SkillViewModel = viewModel()
    val teachingCardViewModel: TeachingCardViewModel = viewModel()
    val networkViewModel: NetworkViewModel = viewModel()
    val chatViewModel: ChatViewModel = viewModel()
    
    val isConnected by networkViewModel.isConnected.collectAsState()
    
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable("splash") {
            SplashScreen(navController = navController)
        }
        
        composable("no_internet") {
            NoInternetScreen()
        }
        
        composable("home") {
            if (isConnected) {
                HomeScreen(
                    navController = navController,
                    viewModel = profileViewModel,
                    teachingCardViewModel = teachingCardViewModel
                )
            } else {
                NoInternetScreen()
            }
        }
        
        composable("explore") {
            if (isConnected) {
                ExploreScreen(
                    navController = navController,
                    viewModel = profileViewModel,
                    teachingCardViewModel = teachingCardViewModel
                )
            } else {
                NoInternetScreen()
            }
        }
        
        composable("chats") {
            ChatsScreen(
                navController = navController,
                profileViewModel = profileViewModel,
                chatViewModel = chatViewModel
            )
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
                onBackClick = { navController.popBackStack() },
                viewModel = chatViewModel
            )
        }
        
        composable("skill/{cardId}") { backStackEntry ->
            if (isConnected) {
                SkillDetailScreen(
                    cardId = backStackEntry.arguments?.getString("cardId") ?: "",
                    navController = navController,
                    teachingCardViewModel = teachingCardViewModel
                )
            } else {
                NoInternetScreen()
            }
        }
        
        composable("skills") {
            if (isConnected) {
                SkillsScreen(
                    navController = navController,
                    skillViewModel = skillViewModel,
                    profileViewModel = profileViewModel
                )
            } else {
                NoInternetScreen()
            }
        }
        
        composable("my_teaching_cards") {
            if (isConnected) {
                MyTeachingCardsScreen(
                    navController = navController,
                    viewModel = teachingCardViewModel,
                    profileViewModel = profileViewModel
                )
            } else {
                NoInternetScreen()
            }
        }
        
        composable("teaching_card/{cardId}") { backStackEntry ->
            if (isConnected) {
                TeachingCardScreen(
                    navController = navController,
                    viewModel = teachingCardViewModel,
                    cardId = backStackEntry.arguments?.getString("cardId")
                )
            } else {
                NoInternetScreen()
            }
        }
        
        composable("create_teaching_card") {
            if (isConnected) {
                TeachingCardScreen(
                    navController = navController,
                    viewModel = teachingCardViewModel
                )
            } else {
                NoInternetScreen()
            }
        }
    }
} 