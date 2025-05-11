package com.ics.skillsync.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.ics.skillsync.model.Chat
import com.ics.skillsync.model.Message
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.Date

class ChatRepository {
    private val db = FirebaseFirestore.getInstance()
    private val chatsCollection = db.collection("chats")
    private val messagesCollection = db.collection("messages")

    suspend fun createChat(chat: Chat) {
        try {
            chatsCollection.document(chat.id).set(chat)
        } catch (e: Exception) {
            throw Exception("Error al crear el chat: ${e.message}")
        }
    }

    fun getChatsForUser(userId: String): Flow<List<Chat>> = callbackFlow {
        val listener = chatsCollection
            .whereArrayContains("participants", userId)
            .orderBy("lastMessageTimestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    if (error.message?.contains("cancelled") == true || error.message?.contains("StandaloneCoroutine was cancelled") == true) {
                        // Ignorar errores de cancelación de corutina
                        return@addSnapshotListener
                    }
                    close(error)
                    return@addSnapshotListener
                }
                val chats = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Chat::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                trySend(chats)
            }
        awaitClose { listener.remove() }
    }

    fun getMessagesForChat(chatId: String): Flow<List<Message>> = callbackFlow {
        val listener = messagesCollection
            .whereEqualTo("chatId", chatId)
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    if (error.message?.contains("cancelled") == true || error.message?.contains("StandaloneCoroutine was cancelled") == true) {
                        // Ignorar errores de cancelación de corutina
                        return@addSnapshotListener
                    }
                    close(error)
                    return@addSnapshotListener
                }
                val messages = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Message::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                trySend(messages)
            }
        awaitClose { listener.remove() }
    }

    suspend fun sendMessage(chatId: String, senderId: String, content: String) {
        try {
            val message = Message(
                id = "",
                chatId = chatId,
                senderId = senderId,
                content = content,
                timestamp = Date(),
                isRead = false
            )
            
            val messageRef = messagesCollection.document()
            
            messageRef.set(message.copy(id = messageRef.id)).await()
            
            // Actualizar último mensaje en el chat
            chatsCollection.document(chatId).update(
                mapOf(
                    "lastMessage" to content,
                    "lastMessageTimestamp" to Date()
                )
            ).await()
        } catch (e: Exception) {
            throw Exception("Error al enviar el mensaje: ${e.message}")
        }
    }

    suspend fun markMessagesAsRead(chatId: String, userId: String) {
        try {
            val messages = messagesCollection
                .whereEqualTo("chatId", chatId)
                .whereNotEqualTo("senderId", userId)
                .whereEqualTo("isRead", false)
                .get()
                .await()
            
            messages.documents.forEach { doc ->
                doc.reference.update("isRead", true).await()
            }

            // Actualizar contador de mensajes no leídos
            chatsCollection.document(chatId).update("unreadCount", 0).await()
        } catch (e: Exception) {
            throw Exception("Error al marcar mensajes como leídos: ${e.message}")
        }
    }
} 