package com.ics.skillsync.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import java.text.SimpleDateFormat
import java.util.*

data class Message(
    val id: String,
    val content: String,
    val timestamp: Date,
    val isFromMe: Boolean
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatDetailScreen(
    chatId: String,
    navController: NavController
) {
    var messageText by remember { mutableStateOf("") }
    
    // Datos de ejemplo
    val messages = remember {
        listOf(
            Message(
                "1",
                "¡Hola! ¿Te gustaría unirte a mi grupo de estudio?",
                Date(System.currentTimeMillis() - 3600000),
                false
            ),
            Message(
                "2",
                "¡Claro! Me encantaría. ¿Qué temas están estudiando?",
                Date(System.currentTimeMillis() - 3500000),
                true
            ),
            Message(
                "3",
                "Estamos viendo programación en Kotlin y desarrollo de aplicaciones Android.",
                Date(System.currentTimeMillis() - 3400000),
                false
            ),
            Message(
                "4",
                "¡Perfecto! Justo lo que necesito. ¿Cuándo es la próxima sesión?",
                Date(System.currentTimeMillis() - 3300000),
                true
            )
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("María García") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    IconButton(onClick = { /* TODO: Implementar menú de opciones */ }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Más opciones")
                    }
                }
            )
        }
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
                contentPadding = PaddingValues(vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(messages) { message ->
                    MessageItem(message = message)
                }
            }

            // Campo de entrada de mensaje
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = messageText,
                    onValueChange = { messageText = it },
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp),
                    placeholder = { Text("Escribe un mensaje...") },
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF5B4DBC),
                        unfocusedBorderColor = Color(0xFFE5E7EB)
                    )
                )
                
                IconButton(
                    onClick = { /* TODO: Implementar envío de mensaje */ },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF5B4DBC))
                ) {
                    Icon(
                        Icons.Default.Send,
                        contentDescription = "Enviar",
                        tint = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun MessageItem(message: Message) {
    val dateFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isFromMe) Arrangement.End else Arrangement.Start
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .padding(horizontal = 8.dp)
        ) {
            Box(
                modifier = Modifier
                    .clip(
                        RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 16.dp,
                            bottomStart = if (message.isFromMe) 16.dp else 4.dp,
                            bottomEnd = if (message.isFromMe) 4.dp else 16.dp
                        )
                    )
                    .background(
                        if (message.isFromMe) Color(0xFF5B4DBC)
                        else Color(0xFFF3F4F6)
                    )
                    .padding(12.dp)
            ) {
                Text(
                    text = message.content,
                    color = if (message.isFromMe) Color.White else Color(0xFF111827),
                    fontSize = 16.sp
                )
            }
            
            Text(
                text = dateFormat.format(message.timestamp),
                fontSize = 12.sp,
                color = Color(0xFF6B7280),
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
    }
} 