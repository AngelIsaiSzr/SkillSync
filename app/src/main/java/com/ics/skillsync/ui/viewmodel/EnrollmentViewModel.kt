package com.ics.skillsync.ui.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class Learner(
    val userId: String,
    val userName: String,
    val userPhotoUrl: String,
    val enrollmentDate: Long,
    val status: String
)

class EnrollmentViewModel(application: Application) : AndroidViewModel(application) {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _uiState = MutableStateFlow<UiState>(UiState.Initial)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val _isEnrolled = MutableStateFlow(false)
    val isEnrolled: StateFlow<Boolean> = _isEnrolled.asStateFlow()

    private val _learners = MutableStateFlow<List<Learner>>(emptyList())
    val learners: StateFlow<List<Learner>> = _learners.asStateFlow()

    suspend fun checkEnrollmentStatus(cardId: String) {
        try {
            val currentUser = auth.currentUser ?: return
            val enrollmentDoc = firestore.collection("teaching_cards")
                .document(cardId)
                .collection("enrollments")
                .document(currentUser.uid)
                .get()
                .await()

            _isEnrolled.value = enrollmentDoc.exists()
        } catch (e: Exception) {
            Log.e("EnrollmentViewModel", "Error al verificar inscripción", e)
            _isEnrolled.value = false
        }
    }

    fun enrollInTeachingCard(cardId: String) {
        viewModelScope.launch {
            try {
                _uiState.value = UiState.Loading
                val currentUser = auth.currentUser ?: throw Exception("Usuario no autenticado")

                // Verificar si el usuario ya está inscrito
                val enrollmentDoc = firestore.collection("teaching_cards")
                    .document(cardId)
                    .collection("enrollments")
                    .document(currentUser.uid)
                    .get()
                    .await()

                if (enrollmentDoc.exists()) {
                    throw Exception("Ya estás inscrito en esta tarjeta")
                }

                // Obtener información del usuario
                val userDoc = firestore.collection("users")
                    .document(currentUser.uid)
                    .get()
                    .await()

                if (!userDoc.exists()) {
                    throw Exception("No se encontró la información del usuario")
                }

                // Crear la inscripción
                val enrollment = hashMapOf(
                    "userId" to currentUser.uid,
                    "userName" to "${userDoc.getString("firstName")} ${userDoc.getString("lastName")}",
                    "userPhotoUrl" to (userDoc.getString("photoUrl") ?: ""),
                    "enrollmentDate" to System.currentTimeMillis(),
                    "status" to "active"
                )

                // Realizar la inscripción en una transacción
                firestore.runTransaction { transaction ->
                    // Obtener la tarjeta actual
                    val cardDoc = transaction.get(firestore.collection("teaching_cards").document(cardId))
                    
                    // Verificar que la tarjeta existe y está activa
                    if (!cardDoc.exists()) {
                        throw Exception("La tarjeta no existe")
                    }
                    if (cardDoc.getBoolean("isActive") != true) {
                        throw Exception("La tarjeta no está activa")
                    }

                    // Incrementar el contador de aprendices
                    val currentCount = cardDoc.getLong("learnerCount") ?: 0
                    
                    // Actualizar la tarjeta
                    transaction.update(
                        firestore.collection("teaching_cards").document(cardId),
                        "learnerCount", currentCount + 1
                    )

                    // Crear la inscripción
                    transaction.set(
                        firestore.collection("teaching_cards")
                            .document(cardId)
                            .collection("enrollments")
                            .document(currentUser.uid),
                        enrollment
                    )
                }.await()

                _isEnrolled.value = true
                _uiState.value = UiState.Success("¡Te has inscrito exitosamente!")
            } catch (e: Exception) {
                Log.e("EnrollmentViewModel", "Error en la inscripción", e)
                _uiState.value = UiState.Error(e.message ?: "Error al inscribirse en la tarjeta")
            }
        }
    }

    fun unenrollFromTeachingCard(cardId: String) {
        viewModelScope.launch {
            try {
                _uiState.value = UiState.Loading
                val currentUser = auth.currentUser ?: throw Exception("Usuario no autenticado")

                // Realizar la desinscripción en una transacción
                firestore.runTransaction { transaction ->
                    // Obtener la tarjeta actual
                    val cardDoc = transaction.get(firestore.collection("teaching_cards").document(cardId))
                    
                    // Verificar que la tarjeta existe
                    if (!cardDoc.exists()) {
                        throw Exception("La tarjeta no existe")
                    }

                    // Verificar que el usuario está inscrito
                    val enrollmentDoc = transaction.get(
                        firestore.collection("teaching_cards")
                            .document(cardId)
                            .collection("enrollments")
                            .document(currentUser.uid)
                    )

                    if (!enrollmentDoc.exists()) {
                        throw Exception("No estás inscrito en esta tarjeta")
                    }

                    // Decrementar el contador de aprendices
                    val currentCount = cardDoc.getLong("learnerCount") ?: 0
                    if (currentCount > 0) {
                        transaction.update(
                            firestore.collection("teaching_cards").document(cardId),
                            "learnerCount", currentCount - 1
                        )
                    }

                    // Eliminar la inscripción
                    transaction.delete(
                        firestore.collection("teaching_cards")
                            .document(cardId)
                            .collection("enrollments")
                            .document(currentUser.uid)
                    )
                }.await()

                _isEnrolled.value = false
                _uiState.value = UiState.Success("Te has desinscrito exitosamente")
            } catch (e: Exception) {
                Log.e("EnrollmentViewModel", "Error en la desinscripción", e)
                _uiState.value = UiState.Error(e.message ?: "Error al desinscribirse de la tarjeta")
            }
        }
    }

    fun clearUiState() {
        _uiState.value = UiState.Initial
    }

    suspend fun loadLearners(cardId: String) {
        try {
            val enrollmentsSnapshot = firestore.collection("teaching_cards")
                .document(cardId)
                .collection("enrollments")
                .get()
                .await()

            val learnersList = enrollmentsSnapshot.documents.mapNotNull { doc ->
                try {
                    Learner(
                        userId = doc.getString("userId") ?: return@mapNotNull null,
                        userName = doc.getString("userName") ?: return@mapNotNull null,
                        userPhotoUrl = doc.getString("userPhotoUrl") ?: "",
                        enrollmentDate = doc.getLong("enrollmentDate") ?: System.currentTimeMillis(),
                        status = doc.getString("status") ?: "active"
                    )
                } catch (e: Exception) {
                    Log.e("EnrollmentViewModel", "Error al parsear aprendiz", e)
                    null
                }
            }.sortedByDescending { it.enrollmentDate }

            _learners.value = learnersList
        } catch (e: Exception) {
            Log.e("EnrollmentViewModel", "Error al cargar aprendices", e)
            _learners.value = emptyList()
        }
    }

    sealed class UiState {
        object Initial : UiState()
        object Loading : UiState()
        data class Success(val message: String) : UiState()
        data class Error(val message: String) : UiState()
    }
} 