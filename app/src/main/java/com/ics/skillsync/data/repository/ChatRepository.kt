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
            
            // Obtener participantes y contadores actuales
            val chatDoc = chatsCollection.document(chatId).get().await()
            val chatData = chatDoc.data
            val participants = chatData?.get("participants") as? List<String> ?: emptyList()
            val unreadCounts = (chatData?.get("unreadCounts") as? Map<String, Long>)?.mapValues { it.value.toInt() }?.toMutableMap() ?: mutableMapOf()
            // Incrementar el contador para todos los participantes excepto el remitente
            participants.forEach { userId ->
                if (userId != senderId) {
                    unreadCounts[userId] = (unreadCounts[userId] ?: 0) + 1
                }
            }
            // Actualizar último mensaje y contadores en el chat
            chatsCollection.document(chatId).update(
                mapOf(
                    "lastMessage" to content,
                    "lastMessageTimestamp" to Date(),
                    "unreadCounts" to unreadCounts
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

            // Actualizar solo el contador de este usuario
            val chatDoc = chatsCollection.document(chatId).get().await()
            val chatData = chatDoc.data
            val unreadCounts = (chatData?.get("unreadCounts") as? Map<String, Long>)?.mapValues { it.value.toInt() }?.toMutableMap() ?: mutableMapOf()
            unreadCounts[userId] = 0
            chatsCollection.document(chatId).update("unreadCounts", unreadCounts).await()
        } catch (e: Exception) {
            throw Exception("Error al marcar mensajes como leídos: ${e.message}")
        }
    }

    suspend fun deleteChat(chatId: String) {
        try {
            // Eliminar todos los mensajes del chat
            val messages = messagesCollection
                .whereEqualTo("chatId", chatId)
                .get()
                .await()
            
            // Crear un batch para eliminar todos los mensajes
            val batch = db.batch()
            messages.documents.forEach { doc ->
                batch.delete(doc.reference)
            }
            
            // Eliminar el chat
            batch.delete(chatsCollection.document(chatId))
            
            // Ejecutar todas las operaciones en una sola transacción
            batch.commit().await()
        } catch (e: Exception) {
            throw Exception("Error al eliminar el chat: ${e.message}")
        }
    }
} 