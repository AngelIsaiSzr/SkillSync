package com.ics.skillsync.data.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.ics.skillsync.model.Chat
import com.ics.skillsync.model.Message
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.Date
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.RemoteMessage
import java.util.UUID
import org.json.JSONObject

class ChatRepository {
    private val db = FirebaseFirestore.getInstance()
    private val chatsCollection = db.collection("chats")
    private val messagesCollection = db.collection("messages")
    private val usersCollection = db.collection("users")

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
            // Verificar si el chat existe antes de enviar el mensaje
            val chatDoc = chatsCollection.document(chatId).get().await()
            if (!chatDoc.exists()) {
                throw Exception("El chat no existe")
            }

            // Crear el mensaje
            val message = Message(
                id = UUID.randomUUID().toString(),
                chatId = chatId,
                senderId = senderId,
                content = content,
                timestamp = Date(),
                isRead = false
            )

            // Guardar el mensaje
            messagesCollection.document(message.id).set(message).await()
            Log.d("ChatRepository", "Mensaje guardado en Firestore: ${message.id}")

            // Obtener los participantes del chat
            val chat = chatDoc.toObject(Chat::class.java)
            val participants = chat?.participants ?: emptyList()
            val recipientId = participants.firstOrNull { it != senderId }

            // Actualizar contadores de mensajes no leídos
            val unreadCounts = (chatDoc.get("unreadCounts") as? Map<String, Long>)?.mapValues { it.value.toInt() }?.toMutableMap() ?: mutableMapOf()
            participants.forEach { participantId ->
                if (participantId != senderId) {
                    unreadCounts[participantId] = (unreadCounts[participantId] ?: 0) + 1
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
            Log.d("ChatRepository", "Chat actualizado en Firestore: $chatId")

            // Enviar notificación al destinatario
            if (recipientId != null) {
                val recipientDoc = usersCollection.document(recipientId).get().await()
                Log.d("ChatRepository", "Documento del destinatario: ${recipientDoc.data}")
                
                var recipientToken = recipientDoc.getString("fcmToken")
                val senderDoc = usersCollection.document(senderId).get().await()
                val senderName = "${senderDoc.getString("firstName")} ${senderDoc.getString("lastName")}"

                if (recipientToken.isNullOrBlank()) {
                    Log.w("ChatRepository", "Token FCM no encontrado para el destinatario: $recipientId, intentando obtener uno nuevo...")
                    try {
                        recipientToken = FirebaseMessaging.getInstance().token.await()
                        usersCollection.document(recipientId).update("fcmToken", recipientToken).await()
                        Log.d("ChatRepository", "Token FCM actualizado para el destinatario: $recipientToken")
                    } catch (e: Exception) {
                        Log.e("ChatRepository", "Error al obtener nuevo token FCM", e)
                        return
                    }
                }

                if (recipientToken.isNotBlank()) {
                    Log.d("ChatRepository", "Enviando notificación a: $recipientId con token: $recipientToken")
                    
                    try {
                        val message = RemoteMessage.Builder(recipientToken)
                            .setMessageId(UUID.randomUUID().toString())
                            .addData("title", senderName)
                            .addData("body", content)
                            .addData("chatId", chatId)
                            .addData("messageId", message.id)
                            .addData("senderId", senderId)
                            .addData("type", "message")
                            .build()

                        FirebaseMessaging.getInstance().send(message)
                        Log.d("ChatRepository", "Notificación enviada exitosamente")
                    } catch (e: Exception) {
                        Log.e("ChatRepository", "Error al enviar notificación", e)
                    }
                } else {
                    Log.e("ChatRepository", "No se pudo obtener un token FCM válido para el destinatario")
                }
            } else {
                Log.w("ChatRepository", "No se encontró destinatario para el chat: $chatId")
            }
        } catch (e: Exception) {
            Log.e("ChatRepository", "Error al enviar mensaje", e)
            throw Exception("Error al enviar el mensaje: ${e.message}")
        }
    }

    suspend fun markMessagesAsRead(chatId: String, userId: String) {
        try {
            // Verificar si el chat existe antes de marcar los mensajes como leídos
            val chatRef = chatsCollection.document(chatId)
            val chatDoc = chatRef.get().await()
            if (!chatDoc.exists()) {
                return // El chat ya no existe, no hay nada que marcar como leído
            }

            val chatData = chatDoc.data
            val unreadCounts = (chatData?.get("unreadCounts") as? Map<String, Long>)?.mapValues { it.value.toInt() }?.toMutableMap() ?: mutableMapOf()
            unreadCounts[userId] = 0
            chatRef.update("unreadCounts", unreadCounts).await()

            // Marcar los mensajes como leídos en segundo plano
            val messages = messagesCollection
                .whereEqualTo("chatId", chatId)
                .whereNotEqualTo("senderId", userId)
                .whereEqualTo("isRead", false)
                .get()
                .await()
            
            if (!messages.isEmpty) {
                val batch = db.batch()
            messages.documents.forEach { doc ->
                    batch.update(doc.reference, "isRead", true)
                }
                batch.commit().await()
            }
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