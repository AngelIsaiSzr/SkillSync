package com.ics.skillsync.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ics.skillsync.R
import com.ics.skillsync.ui.viewmodel.ProfileViewModel
import com.ics.skillsync.ui.components.SharedNavigationDrawer
import com.ics.skillsync.ui.components.SharedTopBar
import com.ics.skillsync.ui.components.SharedBottomBar
import kotlin.system.exitProcess

data class ChatMessage(
    val id: String,
    val senderName: String,
    val lastMessage: String,
    val timestamp: Date,
    val unreadCount: Int = 0,
    val isOnline: Boolean = false,
    val profileImage: String = "https://i.pravatar.cc/150?img="
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatsScreen(
    navController: NavController,
    viewModel: ProfileViewModel
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var searchQuery by remember { mutableStateOf("") }
    val isAuthenticated by viewModel.isAuthenticated.collectAsState()
    
    // Datos de ejemplo
    val chats = remember {
        listOf(
            ChatMessage(
                "1",
                "María García",
                "¡Hola! ¿Te gustaría unirte a mi grupo de estudio?",
                Date(System.currentTimeMillis() - 3600000),
                0,
                true,
                "https://i.pravatar.cc/150?img=1"
            ),
            ChatMessage(
                "2",
                "Carlos Rodríguez",
                "¿Podrías ayudarme con la tarea de matemáticas?",
                Date(System.currentTimeMillis() - 7200000),
                0,
                false,
                "https://i.pravatar.cc/150?img=2"
            ),
            ChatMessage(
                "3",
                "Ana Martínez",
                "¡Excelente trabajo en el proyecto!",
                Date(System.currentTimeMillis() - 86400000),
                0,
                true,
                "https://i.pravatar.cc/150?img=3"
            ),
            ChatMessage(
                "4",
                "Pedro Sánchez",
                "¿Cuándo es la próxima reunión?",
                Date(System.currentTimeMillis() - 172800000),
                0,
                false,
                "https://i.pravatar.cc/150?img=4"
            )
        )
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
                        imageVector = Icons.Default.Message,
                        contentDescription = null,
                        modifier = Modifier.size(120.dp),
                        tint = Color(0xFF5B4DBC)
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Text(
                        text = "¡Inicia sesión para comenzar a chatear con mentores y aprendices de todo el mundo!",
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
                        Text("Iniciar sesión")
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFFF9FAFB))
                        .padding(paddingValues)
                        .padding(horizontal = 16.dp)
                ) {
                    // Barra de búsqueda
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        placeholder = { Text("Buscar chats...") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Buscar",
                                tint = Color.Gray
                            )
                        },
                        colors = TextFieldDefaults.colors(
                            unfocusedContainerColor = Color(0xFFF5F5F5),
                            focusedContainerColor = Color(0xFFF5F5F5),
                            unfocusedIndicatorColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent
                        ),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true
                    )

                    // Lista de chats
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        items(chats) { chat ->
                            ChatItem(
                                chat = chat,
                                onClick = { navController.navigate("chat/${chat.id}") }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ChatItem(
    chat: ChatMessage,
    onClick: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar con indicador de estado
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
            ) {
                AsyncImage(
                    model = chat.profileImage,
                    contentDescription = "Foto de perfil de ${chat.senderName}",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                
                // Indicador de estado online
                if (chat.isOnline) {
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF22C55E))
                            .align(Alignment.BottomEnd)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Contenido del chat
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = chat.senderName,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF111827)
                    )
                    Text(
                        text = dateFormat.format(chat.timestamp),
                        fontSize = 12.sp,
                        color = Color(0xFF6B7280)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = chat.lastMessage,
                        fontSize = 14.sp,
                        color = Color(0xFF6B7280),
                        maxLines = 1
                    )
                    
                    if (chat.unreadCount > 0) {
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(Color(0xFF5B4DBC))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = chat.unreadCount.toString(),
                                fontSize = 12.sp,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
} 