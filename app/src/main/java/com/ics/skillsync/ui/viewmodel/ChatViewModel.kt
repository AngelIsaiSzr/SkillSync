package com.ics.skillsync.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.ics.skillsync.model.Chat
import com.ics.skillsync.model.Message
import com.ics.skillsync.data.repository.ChatRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.*
import com.google.firebase.firestore.FirebaseFirestore
import com.ics.skillsync.data.database.entity.User as FirestoreUser
import kotlinx.coroutines.Job

class ChatViewModel : ViewModel() {
    private val repository = ChatRepository()
    private val _chats = MutableStateFlow<List<Chat>>(emptyList())
    val chats: StateFlow<List<Chat>> = _chats.asStateFlow()

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val firestore = FirebaseFirestore.getInstance()
    private val _otherUsers = MutableStateFlow<Map<String, FirestoreUser>>(emptyMap())
    val otherUsers: StateFlow<Map<String, FirestoreUser>> = _otherUsers.asStateFlow()

    private var currentChatJob: Job? = null
    private var chatsListenerJob: Job? = null

    init {
        loadChats()
    }

    private fun loadChats() {
        viewModelScope.launch {
            try {
                val currentUser = FirebaseAuth.getInstance().currentUser
                if (currentUser != null) {
                    repository.getChatsForUser(currentUser.uid).collect { chatList ->
                        _chats.value = chatList
                    }
                }
            } catch (e: Exception) {
                if (e.message?.contains("cancelled") == true || e.message?.contains("StandaloneCoroutine was cancelled") == true) {
                    return@launch
                }
                _error.value = e.message
            }
        }
    }

    fun loadMessages(chatId: String) {
        currentChatJob?.cancel()
        currentChatJob = viewModelScope.launch {
            try {
                repository.getMessagesForChat(chatId).collect { messageList ->
                    _messages.value = messageList
                }
            } catch (e: Exception) {
                if (e.message?.contains("cancelled") == true || e.message?.contains("StandaloneCoroutine was cancelled") == true) {
                    // Ignorar cualquier error de cancelación de corutina
                    return@launch
                }
                // Solo mostrar errores reales
                _error.value = e.message
            }
        }
    }

    suspend fun getOrCreateChat(otherUserId: String): String {
        val currentUser = FirebaseAuth.getInstance().currentUser ?: throw Exception("Usuario no autenticado")
        
        // Buscar chat existente
        val existingChat = _chats.value.find { chat ->
            chat.participants.contains(currentUser.uid) && chat.participants.contains(otherUserId)
        }
        
        if (existingChat != null) {
            return existingChat.id
        }
        
        // Crear nuevo chat
        val newChat = Chat(
            id = UUID.randomUUID().toString(),
            participants = listOf(currentUser.uid, otherUserId),
            lastMessage = "",
            lastMessageTimestamp = Date()
        )
        
        repository.createChat(newChat)
        return newChat.id
    }

    fun sendMessage(chatId: String, content: String) {
        viewModelScope.launch {
            try {
                val currentUser = FirebaseAuth.getInstance().currentUser ?: throw Exception("Usuario no autenticado")
                repository.sendMessage(chatId, currentUser.uid, content)
                // Forzar recarga de chats después de enviar mensaje
                loadChats()
            } catch (e: Exception) {
                if (e.message?.contains("cancelled") == true || e.message?.contains("StandaloneCoroutine was cancelled") == true) {
                    return@launch
                }
                _error.value = e.message
            }
        }
    }

    fun clearError() {
        _error.value = null
    }

    fun fetchOtherUser(userId: String) {
        if (_otherUsers.value.containsKey(userId)) return // Ya está en caché
        firestore.collection("users").document(userId).get().addOnSuccessListener { doc ->
            doc.toObject(FirestoreUser::class.java)?.let { user ->
                _otherUsers.value = _otherUsers.value + (userId to user)
            }
        }
    }

    fun startChatsListener() {
        chatsListenerJob?.cancel()
        chatsListenerJob = viewModelScope.launch {
            try {
                val currentUser = FirebaseAuth.getInstance().currentUser
                if (currentUser != null) {
                    repository.getChatsForUser(currentUser.uid).collect { chatList ->
                        _chats.value = chatList
                    }
                }
            } catch (e: Exception) {
                if (e.message?.contains("cancelled") == true || e.message?.contains("StandaloneCoroutine was cancelled") == true) {
                    return@launch
                }
                _error.value = e.message
            }
        }
    }

    fun stopChatsListener() {
        chatsListenerJob?.cancel()
        chatsListenerJob = null
    }

    fun markMessagesAsRead(chatId: String, userId: String) {
        viewModelScope.launch {
            try {
                repository.markMessagesAsRead(chatId, userId)
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun deleteChat(chatId: String) {
        viewModelScope.launch {
            try {
                repository.deleteChat(chatId)
                // Limpiar los mensajes del chat actual
                _messages.value = emptyList()
                // Actualizar la lista de chats inmediatamente
                _chats.value = _chats.value.filter { it.id != chatId }
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }
} 