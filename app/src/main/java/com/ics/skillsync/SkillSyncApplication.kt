package com.ics.skillsync

import android.app.Application
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.FirebaseException
import android.util.Log
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.firestore.FirebaseFirestore

class SkillSyncApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        try {
            // Intentar inicializar Firebase
            FirebaseApp.initializeApp(this)
            
            // Obtener el token FCM
            FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.w("SkillSyncApplication", "Error al obtener el token FCM", task.exception)
                    return@addOnCompleteListener
                }
                
                val token = task.result
                Log.d("SkillSyncApplication", "Token FCM obtenido: $token")
                
                // Guardar el token en Firestore para el usuario actual
                val currentUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
                if (currentUser != null) {
                    val userRef = FirebaseFirestore.getInstance()
                        .collection("users")
                        .document(currentUser.uid)

                    // Primero verificar si el documento existe
                    userRef.get().addOnSuccessListener { document ->
                        if (document.exists()) {
                            // Actualizar el token
                            userRef.update("fcmToken", token)
                                .addOnSuccessListener {
                                    Log.d("SkillSyncApplication", "Token FCM actualizado en Firestore")
                                }
                                .addOnFailureListener { e ->
                                    Log.e("SkillSyncApplication", "Error al actualizar token FCM en Firestore", e)
                                }
                        } else {
                            // Crear el documento con el token
                            userRef.set(mapOf("fcmToken" to token))
                                .addOnSuccessListener {
                                    Log.d("SkillSyncApplication", "Documento de usuario creado con token FCM")
                                }
                                .addOnFailureListener { e ->
                                    Log.e("SkillSyncApplication", "Error al crear documento de usuario con token FCM", e)
                                }
                        }
                    }.addOnFailureListener { e ->
                        Log.e("SkillSyncApplication", "Error al verificar documento de usuario", e)
                    }
                } else {
                    Log.w("SkillSyncApplication", "No hay usuario autenticado para guardar el token FCM")
                }
            }
        } catch (e: FirebaseException) {
            // Si hay un error de inicialización, intentar con opciones por defecto
            try {
                val options = FirebaseOptions.Builder()
                    .setProjectId("skillsync-e4933")
                    .setApplicationId("1:880110870036:android:8ec43885e87cadb4bb9101")
                    .setApiKey("AIzaSyDxXxXxXxXxXxXxXxXxXxXxXxXxXxXxXx")
                    .build()
                FirebaseApp.initializeApp(this, options)
            } catch (e2: Exception) {
                Log.e("SkillSyncApplication", "Error al inicializar Firebase", e2)
            }
        } catch (e: Exception) {
            Log.e("SkillSyncApplication", "Error inesperado al inicializar Firebase", e)
        }
    }
} 