package com.ics.skillsync.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.ics.skillsync.model.User

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(userId: String, navController: NavController) {
    var message by remember { mutableStateOf("") }
    val messages = remember { mutableStateListOf<ChatMessage>() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chat") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Lista de mensajes
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(messages) { chatMessage ->
                    ChatMessageItem(chatMessage)
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
                    value = message,
                    onValueChange = { message = it },
                    label = { Text("Escribe un mensaje...") },
                    modifier = Modifier.weight(1f),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                        unfocusedIndicatorColor = MaterialTheme.colorScheme.outline
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = {
                        if (message.isNotBlank()) {
                            messages.add(ChatMessage(message, true))
                            message = ""
                            // Simular respuesta
                            // TODO: Implementar lógica real de chat
                        }
                    }
                ) {
                    Icon(Icons.Default.Send, contentDescription = "Enviar")
                }
            }
        }
    }
}

@Composable
fun ChatMessageItem(chatMessage: ChatMessage) {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = if (chatMessage.isFromMe) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Surface(
            modifier = Modifier.widthIn(max = 300.dp),
            shape = MaterialTheme.shapes.medium,
            color = if (chatMessage.isFromMe) 
                MaterialTheme.colorScheme.primary 
            else 
                MaterialTheme.colorScheme.surfaceVariant
        ) {
            Text(
                text = chatMessage.content,
                modifier = Modifier.padding(12.dp),
                color = if (chatMessage.isFromMe) 
                    MaterialTheme.colorScheme.onPrimary 
                else 
                    MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

data class ChatMessage(
    val content: String,
    val isFromMe: Boolean
) 