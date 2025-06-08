package com.ics.skillsync.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.Search
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
import com.ics.skillsync.model.Chat
import com.ics.skillsync.ui.components.SharedNavigationDrawer
import com.ics.skillsync.ui.components.SharedTopBar
import com.ics.skillsync.ui.components.SharedBottomBar
import com.ics.skillsync.ui.viewmodel.ChatViewModel
import com.ics.skillsync.ui.viewmodel.ProfileViewModel
import kotlin.system.exitProcess
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SmartToy
import com.ics.skillsync.data.database.entity.User as FirestoreUser
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatsScreen(
    navController: NavController,
    profileViewModel: ProfileViewModel,
    chatViewModel: ChatViewModel = viewModel()
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var searchQuery by remember { mutableStateOf("") }
    val isAuthenticated by profileViewModel.isAuthenticated.collectAsState()
    val chats by chatViewModel.chats.collectAsState()
    val isLoading by chatViewModel.isLoading.collectAsState()
    
    LaunchedEffect(Unit) {
        chatViewModel.startChatsListener()
    }
    DisposableEffect(Unit) {
        onDispose {
            chatViewModel.stopChatsListener()
        }
    }
    
    SharedNavigationDrawer(
        navController = navController,
        viewModel = profileViewModel,
        drawerState = drawerState
    ) {
        Scaffold(
            topBar = {
                SharedTopBar(
                    navController = navController,
                    viewModel = profileViewModel,
                    title = "SkillSync",
                    onDrawerOpen = {
                        scope.launch { drawerState.open() }
                    }
                )
            },
            bottomBar = {
                SharedBottomBar(navController = navController)
            },
            floatingActionButton = {
                if (isAuthenticated) {
                    FloatingActionButton(
                        onClick = { navController.navigate("chatbot") },
                        containerColor = Color(0xFF5B4DBC),
                        contentColor = Color.White
                    ) {
                        Icon(
                            imageVector = Icons.Default.SmartToy,
                            contentDescription = "Chat con IA"
                        )
                    }
                }
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
                        imageVector = Icons.AutoMirrored.Filled.Message,
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
                                imageVector = Icons.Filled.Search,
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

                    if (isLoading) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                color = Color(0xFF5B4DBC)
                            )
                        }
                    } else {
                    // Lista de chats
                        if (chats.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.Message,
                                        contentDescription = null,
                                        modifier = Modifier.size(80.dp),
                                        tint = Color(0xFF5B4DBC)
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        text = "No hay chats para mostrar",
                                        fontSize = 18.sp,
                                        textAlign = TextAlign.Center,
                                        color = Color(0xFF111827),
                                        fontWeight = FontWeight.Medium
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Explora habilidades y comienza a chatear con mentores y aprendices",
                                        fontSize = 14.sp,
                                        textAlign = TextAlign.Center,
                                        color = Color(0xFF6B7280)
                                    )
                                }
                            }
                        } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        items(chats) { chat ->
                            ChatItem(
                                chat = chat,
                                        onClick = { navController.navigate("chat/${chat.id}") },
                                        chatViewModel = chatViewModel
                            )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ChatItem(
    chat: Chat,
    onClick: () -> Unit,
    chatViewModel: ChatViewModel = viewModel()
) {
    val currentUser = FirebaseAuth.getInstance().currentUser
    val currentUserId = currentUser?.uid
    val otherUserId = chat.participants.firstOrNull { it != currentUserId } ?: ""
    val otherUsers by chatViewModel.otherUsers.collectAsState()
    LaunchedEffect(otherUserId) {
        if (otherUserId.isNotEmpty()) chatViewModel.fetchOtherUser(otherUserId)
    }
    val user: FirestoreUser? = otherUsers[otherUserId]
    val name = listOfNotNull(user?.firstName, user?.lastName).joinToString(" ").ifBlank { "Usuario" }
    val photoUrl = user?.photoUrl
    val dateFormat = remember { SimpleDateFormat("hh:mm a", Locale.getDefault()) }
    val unreadCount = chat.unreadCounts[currentUserId] ?: 0
    
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
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF5B4DBC))
            ) {
                if (!photoUrl.isNullOrBlank()) {
                AsyncImage(
                        model = photoUrl,
                        contentDescription = "Foto de perfil",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                } else {
                    val initials = name
                        .split(" ")
                        .filter { it.isNotBlank() }
                        .take(2)
                        .map { it.firstOrNull()?.toString() ?: "" }
                        .joinToString("")
                        .ifBlank { "?" }
                    Text(
                        text = initials,
                        color = Color(0xFFFFFFFFF),
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = name,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF111827)
                    )
                    Text(
                        text = dateFormat.format(chat.lastMessageTimestamp),
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
                    if (unreadCount > 0) {
                        Box(
                            modifier = Modifier
                                .size(26.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF5B4DBC)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = unreadCount.toString(),
                                fontSize = 10.sp,
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