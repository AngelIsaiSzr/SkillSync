package com.ics.skillsync.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.ics.skillsync.MainActivity
import com.ics.skillsync.R
import com.ics.skillsync.data.repository.ChatRepository
import com.ics.skillsync.data.repository.UserRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import org.json.JSONObject

class FCMService : FirebaseMessagingService() {
    private val TAG = "FCMService"
    private val CHANNEL_ID = "chat_notifications"
    private val CHANNEL_NAME = "Chat Notifications"
    private val CHANNEL_DESCRIPTION = "Notifications for new chat messages"
    private val DND_WARNING_CHANNEL_ID = "dnd_warning"
    private val DND_WARNING_CHANNEL_NAME = "Important Notifications"

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "Nuevo token FCM: $token")
        
        // Guardar el token en Firestore
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val auth = com.google.firebase.auth.FirebaseAuth.getInstance()
                val currentUser = auth.currentUser
                
                if (currentUser != null) {
                    val firestore = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                    firestore.collection("users")
                        .document(currentUser.uid)
                        .update("fcmToken", token)
                        .await()
                    Log.d(TAG, "Token FCM guardado en Firestore para usuario: ${currentUser.uid}")
                } else {
                    Log.w(TAG, "No hay usuario autenticado para guardar el token FCM")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error al guardar token FCM en Firestore", e)
            }
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        Log.d(TAG, "Mensaje recibido: ${message.data}")
        
        try {
            // Intentar obtener datos de la notificación
            val notification = message.notification
            val data = message.data
            
            Log.d(TAG, "Datos de la notificación: $notification")
            Log.d(TAG, "Datos del mensaje: $data")
            
            // Verificar si el modo No molestar está activo
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && notificationManager.isNotificationPolicyAccessGranted) {
                if (notificationManager.currentInterruptionFilter == NotificationManager.INTERRUPTION_FILTER_NONE) {
                    // Mostrar advertencia sobre No molestar
                    showDndWarningNotification()
                    return
                }
            }
            
            // Crear canales de notificación
            createNotificationChannels()
            
            // Construir la notificación
            val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(notification?.title ?: data["title"] ?: "Nuevo mensaje")
                .setContentText(notification?.body ?: data["body"] ?: "Tienes un nuevo mensaje")
                .setAutoCancel(true)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
            
            // Agregar intent para abrir la app
            val intent = Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                // Agregar datos extras si están disponibles
                data["chatId"]?.let { putExtra("chatId", it) }
                data["messageId"]?.let { putExtra("messageId", it) }
            }
            
            val pendingIntent = PendingIntent.getActivity(
                this,
                0,
                intent,
                PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
            )
            
            notificationBuilder.setContentIntent(pendingIntent)
            
            // Mostrar la notificación
            val notificationId = System.currentTimeMillis().toInt()
            notificationManager.notify(notificationId, notificationBuilder.build())
            
            Log.d(TAG, "Notificación mostrada con ID: $notificationId")
            
        } catch (e: Exception) {
            Log.e(TAG, "Error al procesar mensaje FCM", e)
        }
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            
            // Canal principal para mensajes
            val mainChannel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = CHANNEL_DESCRIPTION
                enableVibration(true)
                setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION), 
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                        .build()
                )
            }
            
            // Canal para advertencias de No molestar
            val dndChannel = NotificationChannel(
                DND_WARNING_CHANNEL_ID,
                DND_WARNING_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Important notifications about app settings"
                enableVibration(true)
                setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION), 
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                        .build()
                )
            }
            
            notificationManager.createNotificationChannel(mainChannel)
            notificationManager.createNotificationChannel(dndChannel)
            Log.d(TAG, "Canales de notificación creados")
        }
    }

    private fun showDndWarningNotification() {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        // Intent para abrir la configuración de No molestar
        val intent = Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(this, DND_WARNING_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Modo No molestar activo")
            .setContentText("Las notificaciones de chat están bloqueadas. Toca para configurar.")
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .build()
        
        notificationManager.notify(1, notification)
        Log.d(TAG, "Notificación de advertencia de No molestar mostrada")
    }
} 