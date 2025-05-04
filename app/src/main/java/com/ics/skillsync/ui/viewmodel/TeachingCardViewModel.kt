package com.ics.skillsync.ui.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.ics.skillsync.model.TeachingCard
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import android.util.Log
import java.util.UUID
import kotlinx.coroutines.delay

class TeachingCardViewModel(application: Application) : AndroidViewModel(application) {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val storage = FirebaseStorage.getInstance()

    private val _uiState = MutableStateFlow<UiState>(UiState.Initial)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val _teachingCards = MutableStateFlow<List<TeachingCard>>(emptyList())
    val teachingCards: StateFlow<List<TeachingCard>> = _teachingCards.asStateFlow()

    private val _myTeachingCards = MutableStateFlow<List<TeachingCard>>(emptyList())
    val myTeachingCards: StateFlow<List<TeachingCard>> = _myTeachingCards.asStateFlow()

    init {
        loadTeachingCards()
        loadMyTeachingCards()
    }

    fun clearUiState() {
        _uiState.value = UiState.Initial
    }

    fun createTeachingCard(
        title: String,
        description: String,
        category: String,
        experienceLevel: TeachingCard.ExperienceLevel,
        availability: String,
        imageUri: Uri?
    ) {
        viewModelScope.launch {
            try {
                _uiState.value = UiState.Loading
                val currentUser = auth.currentUser ?: throw Exception("Usuario no autenticado")

                // Obtener información del usuario primero
                val userDoc = firestore.collection("users")
                    .document(currentUser.uid)
                    .get()
                    .await()

                if (!userDoc.exists()) {
                    throw Exception("No se encontró la información del usuario")
                }

                // Subir imagen si existe
                var imageUrl = ""
                if (imageUri != null) {
                    try {
                        imageUrl = uploadImage(imageUri)
                    } catch (e: Exception) {
                        Log.e("TeachingCardViewModel", "Error al subir la imagen", e)
                        throw Exception("Error al subir la imagen: ${e.message}")
                    }
                }

                val teachingCard = TeachingCard(
                    id = "",
                    mentorId = currentUser.uid,
                    mentorName = "${userDoc.getString("firstName")} ${userDoc.getString("lastName")}",
                    mentorPhotoUrl = userDoc.getString("photoUrl") ?: "",
                    title = title,
                    description = description,
                    category = category,
                    experienceLevel = experienceLevel,
                    availability = availability,
                    imageUrl = imageUrl,
                    isActive = true,
                    learnerCount = 0,
                    createdAt = System.currentTimeMillis()
                )

                // Crear documento en Firestore
                val docRef = firestore.collection("teaching_cards").document()
                val cardWithId = teachingCard.copy(id = docRef.id)
                
                Log.d("TeachingCardViewModel", "Creando tarjeta: ${cardWithId.toMap()}")
                
                docRef.set(cardWithId.toMap()).await()
                Log.d("TeachingCardViewModel", "Tarjeta creada con ID: ${docRef.id}")

                // Actualizar listas inmediatamente
                delay(1000) // Esperar a que Firestore se actualice
                loadMyTeachingCards()

                _uiState.value = UiState.Success("Tarjeta de enseñanza creada exitosamente")
            } catch (e: Exception) {
                Log.e("TeachingCardViewModel", "Error creating teaching card", e)
                _uiState.value = UiState.Error(e.message ?: "Error al crear la tarjeta de enseñanza")
            }
        }
    }

    private suspend fun uploadImage(imageUri: Uri): String {
        return try {
            val currentUser = auth.currentUser ?: throw Exception("Usuario no autenticado")
            val storageRef = storage.reference
            val imageFileName = "teaching_cards/${currentUser.uid}/${UUID.randomUUID()}"
            val imageRef = storageRef.child(imageFileName)
            
            imageRef.putFile(imageUri).await()
            imageRef.downloadUrl.await().toString()
        } catch (e: Exception) {
            Log.e("TeachingCardViewModel", "Error al subir la imagen: ${e.message}")
            throw Exception("Error al subir la imagen: ${e.message}")
        }
    }

    private fun loadTeachingCards() {
        viewModelScope.launch {
            try {
                val snapshot = firestore.collection("teaching_cards")
                    .whereEqualTo("isActive", true)
                    .get()
                    .await()

                val cards = snapshot.documents.mapNotNull { doc ->
                    try {
                        TeachingCard.fromMap(doc.data?.plus(mapOf("id" to doc.id)) ?: emptyMap())
                    } catch (e: Exception) {
                        Log.e("TeachingCardViewModel", "Error parsing teaching card", e)
                        null
                    }
                }

                _teachingCards.value = cards
            } catch (e: Exception) {
                Log.e("TeachingCardViewModel", "Error loading teaching cards", e)
                _uiState.value = UiState.Error("Error al cargar las tarjetas de enseñanza")
            }
        }
    }

    fun loadMyTeachingCards() {
        viewModelScope.launch {
            try {
                val currentUser = auth.currentUser ?: return@launch
                Log.d("TeachingCardViewModel", "Loading teaching cards for user: ${currentUser.uid}")
                
                val snapshot = firestore.collection("teaching_cards")
                    .whereEqualTo("mentorId", currentUser.uid)
                    .whereEqualTo("isActive", true)
                    .get()
                    .await()

                Log.d("TeachingCardViewModel", "Found ${snapshot.documents.size} documents")

                val cards = snapshot.documents.mapNotNull { doc ->
                    try {
                        val data = doc.data?.plus(mapOf("id" to doc.id)) ?: emptyMap()
                        Log.d("TeachingCardViewModel", "Parsing card data: $data")
                        TeachingCard.fromMap(data).also { card ->
                            Log.d("TeachingCardViewModel", "Successfully parsed card: ${card.title} with image: ${card.imageUrl}")
                        }
                    } catch (e: Exception) {
                        Log.e("TeachingCardViewModel", "Error parsing teaching card: ${e.message}", e)
                        null
                    }
                }.sortedByDescending { it.createdAt }

                Log.d("TeachingCardViewModel", "Successfully loaded ${cards.size} cards")
                _myTeachingCards.value = cards
            } catch (e: Exception) {
                Log.e("TeachingCardViewModel", "Error loading my teaching cards: ${e.message}", e)
                _uiState.value = UiState.Error("Error al cargar las tarjetas de enseñanza")
            }
        }
    }

    fun refreshTeachingCards() {
        loadTeachingCards()
        loadMyTeachingCards()
    }

    suspend fun loadTeachingCard(cardId: String): TeachingCard? {
        return try {
            val doc = firestore.collection("teaching_cards")
                .document(cardId)
                .get()
                .await()

            if (doc.exists()) {
                TeachingCard.fromMap(doc.data?.plus(mapOf("id" to doc.id)) ?: emptyMap())
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e("TeachingCardViewModel", "Error loading teaching card", e)
            null
        }
    }

    fun updateTeachingCard(
        cardId: String,
        title: String,
        description: String,
        category: String,
        experienceLevel: TeachingCard.ExperienceLevel,
        availability: String,
        imageUri: Uri? = null
    ) {
        viewModelScope.launch {
            try {
                _uiState.value = UiState.Loading

                // Obtener la tarjeta actual
                val currentCard = loadTeachingCard(cardId) ?: throw Exception("Tarjeta no encontrada")

                // Subir nueva imagen si existe
                var imageUrl = currentCard.imageUrl
                if (imageUri != null) {
                    // Eliminar la imagen anterior si existe
                    if (currentCard.imageUrl.isNotEmpty()) {
                        deleteImage(currentCard.imageUrl)
                    }
                    // Subir la nueva imagen
                    imageUrl = uploadImage(imageUri)
                }

                val updatedCard = currentCard.copy(
                    title = title,
                    description = description,
                    category = category,
                    experienceLevel = experienceLevel,
                    availability = availability,
                    imageUrl = imageUrl
                )

                firestore.collection("teaching_cards")
                    .document(cardId)
                    .set(updatedCard.toMap())
                    .await()

                _uiState.value = UiState.Success("Tarjeta actualizada correctamente")
                loadTeachingCards()
                loadMyTeachingCards()
            } catch (e: Exception) {
                Log.e("TeachingCardViewModel", "Error updating teaching card", e)
                _uiState.value = UiState.Error(e.message ?: "Error al actualizar la tarjeta")
            }
        }
    }

    private suspend fun deleteImage(imageUrl: String) {
        try {
            if (imageUrl.isNotEmpty()) {
                Log.d("TeachingCardViewModel", "Intentando eliminar imagen anterior: $imageUrl")
                val imageRef = storage.getReferenceFromUrl(imageUrl)
                imageRef.delete().await()
                Log.d("TeachingCardViewModel", "Imagen anterior eliminada exitosamente: $imageUrl")
            }
        } catch (e: Exception) {
            Log.e("TeachingCardViewModel", "Error al eliminar la imagen anterior: ${e.message}", e)
            // No lanzamos la excepción para que no interrumpa el flujo de actualización
        }
    }

    fun deleteTeachingCard(cardId: String) {
        viewModelScope.launch {
            try {
                _uiState.value = UiState.Loading
                
                // Obtener la tarjeta para conseguir la URL de la imagen
                val card = loadTeachingCard(cardId) ?: throw Exception("Tarjeta no encontrada")
                
                // Eliminar la imagen si existe
                if (card.imageUrl.isNotEmpty()) {
                    try {
                        Log.d("TeachingCardViewModel", "Intentando eliminar imagen: ${card.imageUrl}")
                        deleteImage(card.imageUrl)
                        Log.d("TeachingCardViewModel", "Imagen eliminada exitosamente")
                    } catch (e: Exception) {
                        Log.e("TeachingCardViewModel", "Error al eliminar la imagen: ${e.message}")
                        // Continuamos con la eliminación de la tarjeta aunque falle la eliminación de la imagen
                    }
                }

                // Eliminar la tarjeta de Firestore
                firestore.collection("teaching_cards")
                    .document(cardId)
                    .delete()
                    .await()

                _uiState.value = UiState.Success("Tarjeta eliminada correctamente")
                loadTeachingCards()
                loadMyTeachingCards()
            } catch (e: Exception) {
                Log.e("TeachingCardViewModel", "Error deleting teaching card", e)
                _uiState.value = UiState.Error(e.message ?: "Error al eliminar la tarjeta")
            }
        }
    }

    sealed class UiState {
        object Initial : UiState()
        object Loading : UiState()
        data class Success(val message: String) : UiState()
        data class Error(val message: String) : UiState()
    }
} 