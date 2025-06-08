package com.ics.skillsync.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
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
import com.google.firebase.auth.FirebaseAuth
import com.ics.skillsync.model.Message
import com.ics.skillsync.ui.viewmodel.ChatViewModel
import java.text.SimpleDateFormat
import java.util.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarDuration
import kotlinx.coroutines.launch
import com.ics.skillsync.data.database.entity.User as FirestoreUser
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.foundation.border
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.ui.text.style.TextAlign

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatDetailScreen(
    chatId: String,
    onBackClick: () -> Unit,
    viewModel: ChatViewModel = viewModel(),
    navController: NavController
) {
    val messages by viewModel.messages.collectAsState()
    val error by viewModel.error.collectAsState()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    var messageText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val prevMessageCount = remember { mutableStateOf(messages.size) }
    val focusManager = LocalFocusManager.current
    var isLoading by remember { mutableStateOf(true) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }

    // Obtener el otro usuario
    val currentUser = FirebaseAuth.getInstance().currentUser
    val currentUserId = currentUser?.uid
    val chats by viewModel.chats.collectAsState()
    val chat = chats.find { it.id == chatId }
    val otherUserId = chat?.participants?.firstOrNull { it != currentUser?.uid } ?: ""
    val otherUsers by viewModel.otherUsers.collectAsState()
    LaunchedEffect(otherUserId) {
        if (otherUserId.isNotEmpty()) viewModel.fetchOtherUser(otherUserId)
    }
    val user: FirestoreUser? = otherUsers[otherUserId]
    val name = listOfNotNull(user?.firstName, user?.lastName).joinToString(" ").ifBlank { "Usuario" }
    val photoUrl = user?.photoUrl

    // Función para manejar el borrado del chat
    fun handleDeleteChat() {
        scope.launch {
            try {
                viewModel.deleteChat(chatId)
                onBackClick()
            } catch (e: Exception) {
                snackbarHostState.showSnackbar(
                    message = "Error al eliminar el chat: ${e.message}",
                    duration = SnackbarDuration.Short
                )
            }
        }
    }

    // Diálogo de confirmación
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Eliminar chat") },
            text = { Text("¿Estás seguro de que deseas eliminar este chat? Esta acción no se puede deshacer.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        handleDeleteChat()
                    }
                ) {
                    Text("Eliminar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    LaunchedEffect(chatId) {
        isLoading = true
        viewModel.loadMessages(chatId)
        // Simulamos un tiempo mínimo de carga para evitar parpadeos
        kotlinx.coroutines.delay(1000)
        isLoading = false
    }

    // Escuchar cambios en el chat para detectar si fue eliminado
    LaunchedEffect(chatId) {
        viewModel.chats.collect { chatList ->
            if (chatList.none { it.id == chatId }) {
                // El chat ya no existe, redirigir a la pantalla de chats
                onBackClick()
            }
        }
    }

    // Marcar mensajes como leídos al entrar al chat y cuando se reciben nuevos mensajes
    LaunchedEffect(chatId, currentUserId, messages) {
        if (!currentUserId.isNullOrBlank()) {
            viewModel.markMessagesAsRead(chatId, currentUserId)
        }
    }

    LaunchedEffect(messages.size) {
        if (messages.size > prevMessageCount.value) {
            listState.animateScrollToItem(0)
        }
        prevMessageCount.value = messages.size
    }

    Scaffold(
        topBar = {
            Surface(
                tonalElevation = 6.dp,
                shadowElevation = 12.dp,
                color = MaterialTheme.colorScheme.surface,
            ) {
                TopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable {
                                if (otherUserId.isNotEmpty()) {
                                    navController.navigate("user_detail/$otherUserId")
                                }
                            }
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
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
                                        color = Color(0xFFFFFFFF),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp,
                                        modifier = Modifier.align(Alignment.Center)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                name,
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onBackClick, modifier = Modifier.size(44.dp)) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Volver",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    },
                    actions = {
                        Box {
                            IconButton(
                                onClick = { showMenu = true },
                                modifier = Modifier.size(44.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.MoreVert,
                                    contentDescription = "Más opciones",
                                    tint = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                            DropdownMenu(
                                expanded = showMenu,
                                onDismissRequest = { showMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { 
                                        Text(
                                            "Eliminar chat",
                                            color = MaterialTheme.colorScheme.error
                                        )
                                    },
                                    onClick = {
                                        showMenu = false
                                        showDeleteDialog = true
                                    },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                    }
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        titleContentColor = MaterialTheme.colorScheme.onSurface
                    )
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) { focusManager.clearFocus() }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    reverseLayout = true,
                    contentPadding = PaddingValues(top = 16.dp)
                ) {
                    items(messages.reversed()) { message ->
                        MessageItem(message = message)
                    }
                }

                // Área de escritura y envío de mensajes mejorada visualmente
                Surface(
                    tonalElevation = 4.dp,
                    shadowElevation = 8.dp,
                    shape = RoundedCornerShape(32.dp),
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                        .imePadding()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = messageText,
                            onValueChange = { messageText = it },
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 8.dp),
                            placeholder = { Text("Escribe un mensaje...", color = Color.Gray) },
                            maxLines = 4,
                            shape = RoundedCornerShape(24.dp),
                            colors = TextFieldDefaults.colors(
                                unfocusedContainerColor = Color.Transparent,
                                focusedContainerColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                focusedIndicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                            ),
                            textStyle = LocalTextStyle.current.copy(fontSize = 16.sp)
                        )

                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary,
                            shadowElevation = 6.dp,
                            modifier = Modifier.size(44.dp)
                        ) {
                            IconButton(
                                onClick = {
                                    if (messageText.isNotBlank()) {
                                        viewModel.sendMessage(chatId, messageText)
                                        messageText = ""
                                    }
                                },
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Send,
                                    contentDescription = "Enviar mensaje",
                                    tint = Color.White,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Loader que cubre toda la pantalla
            AnimatedVisibility(
                visible = isLoading,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 1f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(48.dp)
                    )
                }
            }
        }

        error?.let { errorMessage ->
            LaunchedEffect(errorMessage) {
                snackbarHostState.showSnackbar(
                    message = errorMessage,
                    duration = SnackbarDuration.Short
                )
                viewModel.clearError()
            }
        }
    }
}

@Composable
private fun MessageItem(message: Message) {
    val currentUser = FirebaseAuth.getInstance().currentUser
    val isCurrentUser = message.senderId == currentUser?.uid
    val dateFormat = remember { SimpleDateFormat("hh:mm a", Locale.getDefault()) }
    val viewModel: ChatViewModel = viewModel()
    val otherUsers by viewModel.otherUsers.collectAsState()
    
    // Cargar la información del usuario si no está disponible
    LaunchedEffect(message.senderId) {
        if (!otherUsers.containsKey(message.senderId)) {
            viewModel.fetchOtherUser(message.senderId)
        }
    }
    
    // Obtener información del usuario
    val user = otherUsers[message.senderId]
    val name = listOfNotNull(user?.firstName, user?.lastName).joinToString(" ")
    val photoUrl = user?.photoUrl

    // Función para obtener las iniciales
    fun getInitials(name: String): String {
        return if (name.isBlank()) {
            // Si no hay nombre, intentar obtener el email del usuario actual
            if (isCurrentUser) {
                currentUser?.email?.split("@")?.firstOrNull()?.take(2)?.uppercase() ?: "?"
            } else {
                "?"
            }
        } else {
            name.split(" ")
                .filter { it.isNotBlank() }
                .take(2)
                .map { it.firstOrNull()?.toString() ?: "" }
                .joinToString("")
                .uppercase()
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp, horizontal = 8.dp),
        horizontalArrangement = if (isCurrentUser) Arrangement.End else Arrangement.Start
    ) {
        if (!isCurrentUser) {
            Box(
                modifier = Modifier
                    .size(32.dp)
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
                    Text(
                        text = getInitials(name),
                        color = Color(0xFFFFFFFF),
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        Column(
            horizontalAlignment = if (isCurrentUser) Alignment.End else Alignment.Start
        ) {
            Surface(
                modifier = Modifier
                    .widthIn(min = 108.dp, max = 280.dp)
                    .padding(vertical = 4.dp),
                shape = RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 16.dp,
                    bottomStart = if (isCurrentUser) 16.dp else 4.dp,
                    bottomEnd = if (isCurrentUser) 4.dp else 16.dp
                ),
                color = if (isCurrentUser) Color(0xFF5B4DBC) else Color(0xFFEEEEEE)
            ) {
                Text(
                    text = message.content,
                    modifier = Modifier.padding(12.dp),
                    color = if (isCurrentUser) Color.White else Color(0xFF111827),
                    fontSize = 16.sp
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.align(if (isCurrentUser) Alignment.End else Alignment.Start)
            ) {
                Text(
                    text = dateFormat.format(message.timestamp),
                    fontSize = 12.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
                if (isCurrentUser) {
                    Icon(
                        imageVector = if (message.isRead) Icons.Filled.DoneAll else Icons.Filled.Done,
                        contentDescription = if (message.isRead) "Leído" else "Enviado",
                        tint = if (message.isRead) Color(0xFF4FC3F7) else Color.Gray,
                        modifier = Modifier
                            .padding(start = 1.dp)
                            .size(15.dp)
                    )
                }
            }
        }

        if (isCurrentUser) {
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .size(32.dp)
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
                    Text(
                        text = getInitials(name),
                        color = Color(0xFFE0E0E0),
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        }
    }
}