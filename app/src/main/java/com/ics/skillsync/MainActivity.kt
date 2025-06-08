package com.ics.skillsync

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.rememberNavController
import com.ics.skillsync.navigation.Navigation
import com.ics.skillsync.ui.theme.SkillSyncTheme
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ics.skillsync.ui.viewmodel.NetworkViewModel
import com.ics.skillsync.ui.screens.NoInternetScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        setContent {
            SkillSyncTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    val networkViewModel: NetworkViewModel = viewModel()
                    val isConnected by networkViewModel.isConnected.collectAsState()
                    
                    networkViewModel.initialize(this)
                    
                    if (!isConnected) {
                        NoInternetScreen()
                    } else {
                    Navigation(navController = navController)
                    }
                }
            }
        }
    }
}