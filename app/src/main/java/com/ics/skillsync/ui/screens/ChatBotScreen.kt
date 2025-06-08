package com.ics.skillsync.ui.screens

import android.util.Log
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
import androidx.compose.material.icons.filled.*
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
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.GenerateContentResponse
import com.ics.skillsync.ui.components.SharedTopBar
import com.ics.skillsync.ui.viewmodel.ProfileViewModel
import com.ics.skillsync.utils.ApiKeys
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import androidx.lifecycle.viewmodel.compose.viewModel

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val content: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatBotScreen(
    navController: NavController,
    viewModel: ProfileViewModel = viewModel()
) {
    val currentUser = viewModel.currentUser.collectAsState().value
    val userName = currentUser?.firstName ?: "Usuario"
    var messages by remember { mutableStateOf(listOf<ChatMessage>()) }
    var inputText by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    
    // Inicializar Gemini
    val generativeModel = remember {
        GenerativeModel(
            modelName = "gemini-2.0-flash",
            apiKey = ApiKeys.GEMINI_API_KEY
        )
    }

    // Mensaje de bienvenida inicial
    LaunchedEffect(Unit) {
        messages = listOf(
            ChatMessage(
                content = "¡Hola $userName! Soy tu asistente virtual de SkillSync. ¿En qué puedo ayudarte hoy? Puedo responder preguntas sobre las siguientes categorías:\n\n" +
                        "• Programación\n" +
                        "• Diseño\n" +
                        "• Marketing\n" +
                        "• Negocios\n" +
                        "• Idiomas\n" +
                        "• Música\n" +
                        "• Arte\n" +
                        "• Deportes\n" +
                        "• Cocina\n" +
                        "• Salud\n\n" +
                        "¿Qué te gustaría saber?",
                isUser = false
            )
        )
    }

    // Función para construir el contexto de la conversación
    fun buildConversationContext(): String {
        val lastMessages = messages.takeLast(5).joinToString("\n") { 
            "${if (it.isUser) "Usuario" else "Asistente"}: ${it.content}"
        }
        return "Contexto de la conversación anterior:\n$lastMessages\n\n"
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
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF5B4DBC))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SmartToy,
                                    contentDescription = "Asistente IA",
                                    tint = Color.White,
                                    modifier = Modifier
                                        .size(24.dp)
                                        .align(Alignment.Center)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                "Asistente IA",
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { navController.navigateUp() }, modifier = Modifier.size(44.dp)) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Volver",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        titleContentColor = MaterialTheme.colorScheme.onSurface
                    )
                )
            }
        },
        containerColor = Color.White
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Lista de mensajes
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                state = listState,
                contentPadding = PaddingValues(top = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                reverseLayout = true
            ) {
                items(messages.reversed()) { message ->
                    ChatMessageItem(message = message, viewModel = viewModel)
                }
            }

            // Campo de entrada
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
                        value = inputText,
                        onValueChange = { inputText = it },
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
                                if (inputText.isNotBlank()) {
                                    val userMessage = ChatMessage(
                                        content = inputText,
                                        isUser = true
                                    )
                                    val currentMessage = inputText
                                    messages = messages + userMessage
                                    inputText = ""
                                    isLoading = true
                                    
                                    scope.launch {
                                        try {
                                            val categories = "Programación, Diseño, Marketing, Negocios, Idiomas, Música, Arte, Deportes, Cocina, Salud"
                                            val context = buildConversationContext()
                                            val prompt = """
                                                $context
                                                Instrucciones: 
                                                - Responde de forma amigable, alegre y entusiasta, como si estuvieras teniendo una conversación casual con un amigo.
                                                - Mantén un tono positivo y motivador y responde de forma breve y concisa, sin usar asteriscos ni formato de markdown.
                                                - Si te preguntan "¿quién eres?", "¿qué eres?" o similar, responde: "Soy el Asistente Virtual de SkillSync. ¡Tu compañero de aprendizaje! Estoy aquí para ayudarte a explorar y aprender sobre diferentes temas. ¿En qué puedo ayudarte hoy?"
                                                - Mantén el contexto de la conversación y responde de manera coherente, recuerda que el usuario se llama $userName y no lo menciones seguido.
                                                - Si la pregunta no está relacionada con estas categorías ($categories), 
                                                responde de forma amable: '¡Hmm! No estoy seguro sobre ese tema, pero podemos hablar de cosas interesantes como: $categories. ¿Cuál te gustaría explorar?'
                                                
                                                Pregunta actual: $currentMessage
                                            """.trimIndent()
                                            
                                            val response = generativeModel.generateContent(prompt)
                                            val botMessage = ChatMessage(
                                                content = response.text?.trim()?.replace("***", "") ?: "Lo siento, no pude procesar tu mensaje.",
                                                isUser = false
                                            )
                                            messages = messages + botMessage
                                        } catch (e: Exception) {
                                            Log.e("ChatBotScreen", "Error al generar contenido con Gemini", e)
                                            val errorMessage = ChatMessage(
                                                content = "Lo siento, hubo un error al procesar tu mensaje. Por favor, intenta de nuevo.",
                                                isUser = false
                                            )
                                            messages = messages + errorMessage
                                        } finally {
                                            isLoading = false
                                        }
                                    }
                                }
                            },
                            enabled = !isLoading && inputText.isNotBlank(),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = Color.White
                                )
                            } else {
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
        }
    }
}

@Composable
fun ChatMessageItem(message: ChatMessage, viewModel: ProfileViewModel) {
    val dateFormat = remember { SimpleDateFormat("hh:mm a", Locale.getDefault()) }
    val currentUser = viewModel.currentUser.collectAsState().value
    val isCurrentUser = message.isUser
    
    // Función para obtener las iniciales
    fun getInitials(name: String): String {
        return if (name.isBlank()) {
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
        horizontalArrangement = if (message.isUser) Arrangement.End else Arrangement.Start
    ) {
        if (!message.isUser) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF5B4DBC)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.SmartToy,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
        }
        
        Column(
            horizontalAlignment = if (message.isUser) Alignment.End else Alignment.Start
        ) {
            Surface(
                modifier = Modifier
                    .widthIn(min = 108.dp, max = 280.dp)
                    .padding(vertical = 4.dp),
                shape = RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 16.dp,
                    bottomStart = if (message.isUser) 16.dp else 4.dp,
                    bottomEnd = if (message.isUser) 4.dp else 16.dp
                ),
                color = if (message.isUser) Color(0xFF5B4DBC) else Color(0xFFEEEEEE)
            ) {
                Text(
                    text = message.content,
                    modifier = Modifier.padding(12.dp),
                    color = if (message.isUser) Color.White else Color(0xFF111827),
                    fontSize = 16.sp
                )
            }
            
            Text(
                text = dateFormat.format(message.timestamp),
                fontSize = 12.sp,
                color = Color.Gray,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }
        
        if (message.isUser) {
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF5B4DBC))
            ) {
                if (!currentUser?.photoUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = currentUser?.photoUrl,
                        contentDescription = "Foto de perfil",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    val name = listOfNotNull(currentUser?.firstName, currentUser?.lastName).joinToString(" ")
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