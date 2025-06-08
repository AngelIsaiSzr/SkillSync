package com.ics.skillsync.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.ics.skillsync.ui.components.SharedTopBar
import com.ics.skillsync.ui.components.SharedBottomBar
import com.ics.skillsync.ui.components.SharedNavigationDrawer
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.rememberDrawerState
import kotlinx.coroutines.launch
import com.ics.skillsync.ui.viewmodel.ProfileViewModel
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: ProfileViewModel
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showDeleteAccountDialog by remember { mutableStateOf(false) }
    var darkMode by remember { mutableStateOf(false) }
    var notificationsEnabled by remember { mutableStateOf(true) }
    var emailNotifications by remember { mutableStateOf(true) }
    var pushNotifications by remember { mutableStateOf(true) }
    var soundEnabled by remember { mutableStateOf(true) }
    var vibrationEnabled by remember { mutableStateOf(true) }
    val isAuthenticated by viewModel.isAuthenticated.collectAsState()

    // Función para cerrar sesión
    fun handleLogout() {
        FirebaseAuth.getInstance().signOut()
        navController.navigate("profile") {
            popUpTo("home") { inclusive = true }
        }
    }

    // Función para eliminar cuenta
    fun handleDeleteAccount() {
        val user = FirebaseAuth.getInstance().currentUser
        user?.delete()?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                navController.navigate("profile") {
                    popUpTo("home") { inclusive = true }
                }
            }
        }
    }

    SharedNavigationDrawer(
        navController = navController,
        viewModel = viewModel,
        drawerState = drawerState
    ) {
        Scaffold(
            topBar = {
                SharedTopBar(
                    navController = navController,
                    viewModel = viewModel,
                    title = "SkillSync",
                    onDrawerOpen = {
                        scope.launch { drawerState.open() }
                    }
                )
            },
            bottomBar = {
                SharedBottomBar(navController = navController)
            },
            containerColor = Color.White
        ) { paddingValues ->
            if (!isAuthenticated) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFFF9FAFB))
                        .padding(paddingValues)
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = null,
                        modifier = Modifier.size(120.dp),
                        tint = Color(0xFF5B4DBC)
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Text(
                        text = "¡Inicia sesión para acceder a todas las configuraciones de tu cuenta!",
                        fontSize = 18.sp,
                        textAlign = TextAlign.Center,
                        color = Color(0xFF111827),
                        fontWeight = FontWeight.Medium
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Button(
                        onClick = { navController.navigate("profile") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF5B4DBC)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Login,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Iniciar Sesión")
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Sección de Apariencia
                    SettingsSection(title = "Apariencia") {
                        SettingsSwitch(
                            icon = Icons.Default.DarkMode,
                            title = "Modo Oscuro",
                            description = "Cambiar entre tema claro y oscuro",
                            checked = darkMode,
                            onCheckedChange = { darkMode = it }
                        )
                    }

                    // Sección de Notificaciones
                    SettingsSection(title = "Notificaciones") {
                        SettingsSwitch(
                            icon = Icons.Default.Notifications,
                            title = "Notificaciones",
                            description = "Activar o desactivar todas las notificaciones",
                            checked = notificationsEnabled,
                            onCheckedChange = { notificationsEnabled = it }
                        )
                        
                        if (notificationsEnabled) {
                            SettingsSwitch(
                                icon = Icons.Default.Email,
                                title = "Notificaciones por correo",
                                description = "Recibir notificaciones por correo electrónico",
                                checked = emailNotifications,
                                onCheckedChange = { emailNotifications = it }
                            )
                            
                            SettingsSwitch(
                                icon = Icons.Default.NotificationsActive,
                                title = "Notificaciones push",
                                description = "Recibir notificaciones en el dispositivo",
                                checked = pushNotifications,
                                onCheckedChange = { pushNotifications = it }
                            )
                            
                            SettingsSwitch(
                                icon = Icons.Default.VolumeUp,
                                title = "Sonido",
                                description = "Activar sonido en las notificaciones",
                                checked = soundEnabled,
                                onCheckedChange = { soundEnabled = it }
                            )
                            
                            SettingsSwitch(
                                icon = Icons.Default.Vibration,
                                title = "Vibración",
                                description = "Activar vibración en las notificaciones",
                                checked = vibrationEnabled,
                                onCheckedChange = { vibrationEnabled = it }
                            )
                        }
                    }

                    // Sección de Privacidad
                    SettingsSection(title = "Privacidad") {
                        SettingsItem(
                            icon = Icons.Default.Lock,
                            title = "Cambiar contraseña",
                            description = "Actualizar tu contraseña de acceso"
                        )
                        
                        SettingsItem(
                            icon = Icons.Default.Visibility,
                            title = "Visibilidad del perfil",
                            description = "Controlar quién puede ver tu perfil"
                        )
                        
                        SettingsItem(
                            icon = Icons.Default.Shield,
                            title = "Datos personales",
                            description = "Gestionar tus datos personales"
                        )
                    }

                    // Sección de Cuenta
                    SettingsSection(title = "Cuenta") {
                        SettingsItem(
                            icon = Icons.Default.Language,
                            title = "Idioma",
                            description = "Cambiar el idioma de la aplicación"
                        )
                        
                        SettingsItem(
                            icon = Icons.Default.Help,
                            title = "Ayuda y soporte",
                            description = "Obtener ayuda y contactar soporte"
                        )
                        
                        SettingsItem(
                            icon = Icons.Default.Info,
                            title = "Acerca de",
                            description = "Información sobre la aplicación"
                        )
                        
                        SettingsItem(
                            icon = Icons.Default.Delete,
                            title = "Eliminar cuenta",
                            description = "Eliminar permanentemente tu cuenta",
                            onClick = { showDeleteAccountDialog = true }
                        )
                    }

                    // Botón de cerrar sesión
                    Button(
                        onClick = { showLogoutDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF5B4DBC)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Logout,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Cerrar Sesión")
                    }
                }
            }
        }
    }

    // Diálogo de confirmación para cerrar sesión
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Cerrar sesión") },
            text = { Text("¿Estás seguro de que quieres cerrar sesión?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogoutDialog = false
                        handleLogout()
                    }
                ) {
                    Text("Cerrar sesión")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Diálogo de confirmación para eliminar cuenta
    if (showDeleteAccountDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteAccountDialog = false },
            title = { Text("Eliminar cuenta") },
            text = { Text("¿Estás seguro de que quieres eliminar tu cuenta? Esta acción no se puede deshacer.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteAccountDialog = false
                        handleDeleteAccount()
                    }
                ) {
                    Text("Eliminar", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteAccountDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = Color(0xFF5B4DBC),
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                content()
            }
        }
    }
}

@Composable
private fun SettingsItem(
    icon: ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit = {}
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        onClick = onClick,
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color(0xFF5B4DBC),
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color(0xFF111827)
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF6B7280)
                )
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = Color(0xFF6B7280),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun SettingsSwitch(
    icon: ImageVector,
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color(0xFF5B4DBC),
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = Color(0xFF111827)
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF6B7280)
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color(0xFF5B4DBC),
                checkedTrackColor = Color(0xFF5B4DBC).copy(alpha = 0.5f)
            )
        )
    }
} 