package com.ics.skillsync.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.ics.skillsync.model.LearningSession
import com.ics.skillsync.model.SessionStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Date

class SessionViewModel(application: Application) : AndroidViewModel(application) {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _uiState = MutableStateFlow<UiState>(UiState.Initial)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val _sessions = MutableStateFlow<List<LearningSession>>(emptyList())
    val sessions: StateFlow<List<LearningSession>> = _sessions.asStateFlow()

    private val _mentorNames = MutableStateFlow<Map<String, String>>(emptyMap())
    val mentorNames: StateFlow<Map<String, String>> = _mentorNames.asStateFlow()

    fun loadUserSessions() {
        viewModelScope.launch {
            try {
                _uiState.value = UiState.Loading
                val userId = auth.currentUser?.uid ?: throw Exception("Usuario no autenticado")

                // Obtener el rol del usuario actual
                val userDoc = db.collection("users")
                    .document(userId)
                    .get()
                    .await()
                
                val userRole = userDoc.getString("role") ?: "Aprendiz"

                // Consulta base de sesiones
                val sessionsQuery = when (userRole) {
                    "Mentor" -> db.collection("sessions").whereEqualTo("mentorId", userId)
                    else -> db.collection("sessions").whereEqualTo("learnerId", userId)
                }

                val sessionsSnapshot = sessionsQuery.get().await()

                val sessionsList = sessionsSnapshot.documents.mapNotNull { doc ->
                    try {
                        val data = doc.data ?: return@mapNotNull null
                        LearningSession(
                            id = doc.id,
                            title = data["title"] as? String ?: "",
                            date = Date((data["date"] as? Number)?.toLong() ?: 0),
                            duration = (data["duration"] as? Number)?.toInt() ?: 30,
                            mentorId = data["mentorId"] as? String ?: "",
                            learnerId = data["learnerId"] as? String ?: "",
                            learnerName = data["learnerName"] as? String ?: "",
                            status = try {
                                SessionStatus.valueOf(data["status"] as? String ?: "PENDING")
                            } catch (e: Exception) {
                                SessionStatus.PENDING
                            }
                        )
                    } catch (e: Exception) {
                        null
                    }
                }

                // Filtrar las sesiones propias si el usuario tiene ambos roles
                val filteredSessions = if (userRole == "Ambos roles") {
                    sessionsList.filter { it.mentorId != userId }
                } else {
                    sessionsList
                }

                // Obtener los nombres de los mentores/aprendices según el rol
                val namesMap = mutableMapOf<String, String>()
                
                // Si es mentor, usar el nombre del aprendiz guardado en la sesión
                if (userRole == "Mentor") {
                    filteredSessions.forEach { session ->
                        if (session.learnerName.isNotEmpty()) {
                            namesMap[session.learnerId] = session.learnerName
                        }
                    }
                } else {
                    // Si es aprendiz, cargar los nombres de los mentores
                    val mentorIds = filteredSessions.map { it.mentorId }.distinct()
                    for (id in mentorIds) {
                        try {
                            val userDoc = db.collection("users")
                                .document(id)
                                .get()
                                .await()
                            
                            val firstName = userDoc.getString("firstName") ?: ""
                            val lastName = userDoc.getString("lastName") ?: ""
                            namesMap[id] = "$firstName $lastName"
                        } catch (e: Exception) {
                            namesMap[id] = "Mentor desconocido"
                        }
                    }
                }

                _mentorNames.value = namesMap
                _sessions.value = filteredSessions
            } catch (e: Exception) {

            }
        }
    }

    fun getMentorName(mentorId: String): String {
        return _mentorNames.value[mentorId] ?: "Mentor desconocido"
    }

    fun updateMentorNames(newMentorNames: Map<String, String>) {
        _mentorNames.value = newMentorNames
    }

    fun clearUiState() {
        _uiState.value = UiState.Initial
    }

    sealed class UiState {
        object Initial : UiState()
        object Loading : UiState()
        data class Success(val message: String) : UiState()
        data class Error(val message: String) : UiState()
    }
} 