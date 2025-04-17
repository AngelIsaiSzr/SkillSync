package com.ics.skillsync.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.ics.skillsync.ui.viewmodel.ProfileViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.system.exitProcess
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SharedTopBar(
    navController: NavController,
    viewModel: ProfileViewModel,
    title: String,
    onDrawerOpen: () -> Unit
) {
    TopAppBar(
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(
                            color = Color(0xFF5B4DBC),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.SwapHoriz,
                        contentDescription = "Logo",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    color = Color(0xFF5B4DBC)
                )
            }
        },
        actions = {
            IconButton(onClick = onDrawerOpen) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Menú",
                    tint = Color(0xFF5B4DBC)
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.White,
            titleContentColor = Color(0xFF5B4DBC)
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SharedDrawerContent(
    navController: NavController,
    viewModel: ProfileViewModel,
    scope: CoroutineScope,
    onDrawerClose: suspend () -> Unit
) {
    val isAuthenticated by viewModel.isAuthenticated.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    
    ModalDrawerSheet {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .background(Color(0xFF5B4DBC)),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .border(2.dp, Color.White, CircleShape)
                        .padding(2.dp),
                    contentAlignment = Alignment.Center
                ) {
                    when {
                        currentUser?.photoUrl.isNullOrEmpty() -> {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Foto de perfil por defecto",
                                modifier = Modifier.size(40.dp),
                                tint = Color(0xFF5B4DBC)
                            )
                        }
                        else -> {
                            var isError by remember { mutableStateOf(false) }
                            
                            if (isError) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "Error al cargar la foto",
                                    modifier = Modifier.size(40.dp),
                                    tint = Color(0xFF5B4DBC)
                                )
                            } else {
                                AsyncImage(
                                    model = currentUser?.photoUrl,
                                    contentDescription = "Foto de perfil",
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop,
                                    onError = { isError = true }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = if (isAuthenticated) currentUser?.username ?: "Usuario" else "Anónimo",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        NavigationDrawerItem(
            icon = { Icon(Icons.Default.Search, contentDescription = "Buscar") },
            label = { Text("Buscar") },
            selected = false,
            onClick = {
                scope.launch { onDrawerClose() }
                navController.navigate("search")
            }
        )
        
        NavigationDrawerItem(
            icon = { Icon(Icons.Default.DateRange, contentDescription = "Sesiones") },
            label = { Text("Sesiones") },
            selected = false,
            onClick = {
                scope.launch { onDrawerClose() }
                navController.navigate("sessions")
            }
        )
        
        NavigationDrawerItem(
            icon = { Icon(Icons.Default.Settings, contentDescription = "Configuración") },
            label = { Text("Configuración") },
            selected = false,
            onClick = {
                scope.launch { onDrawerClose() }
                navController.navigate("settings")
            }
        )

        Spacer(modifier = Modifier.weight(1f))

        NavigationDrawerItem(
            icon = { Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Salir") },
            label = { Text("Salir") },
            selected = false,
            onClick = {
                exitProcess(0)
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SharedNavigationDrawer(
    navController: NavController,
    viewModel: ProfileViewModel,
    drawerState: DrawerState,
    content: @Composable () -> Unit
) {
    val scope = rememberCoroutineScope()
    
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            SharedDrawerContent(
                navController = navController,
                viewModel = viewModel,
                scope = scope,
                onDrawerClose = {
                    drawerState.close()
                }
            )
        }
    ) {
        content()
    }
} 