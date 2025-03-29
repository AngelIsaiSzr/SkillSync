package com.ics.skillsync.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState

@Composable
fun SharedBottomBar(
    navController: NavController,
    items: List<BottomNavItem> = listOf(
        BottomNavItem(
            name = "Inicio",
            route = "home",
            icon = Icons.Default.Home
        ),
        BottomNavItem(
            name = "Explorar",
            route = "explore",
            icon = Icons.Default.Explore
        ),
        BottomNavItem(
            name = "Chats",
            route = "chats",
            icon = Icons.Default.Message
        ),
        BottomNavItem(
            name = "Perfil",
            route = "profile",
            icon = Icons.Default.Person
        )
    )
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar(
        containerColor = Color.White
    ) {
        items.forEach { item ->
            NavigationBarItem(
                icon = { Icon(item.icon, contentDescription = item.name) },
                label = { Text(item.name) },
                selected = currentRoute == item.route,
                onClick = {
                    if (currentRoute != item.route) {
                        navController.navigate(item.route) {
                            popUpTo(navController.graph.startDestinationId)
                            launchSingleTop = true
                        }
                    }
                }
            )
        }
    }
}

data class BottomNavItem(
    val name: String,
    val route: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
) 