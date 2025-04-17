package com.ics.skillsync.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ics.skillsync.data.database.AppDatabase
import com.ics.skillsync.data.database.entity.User
import com.ics.skillsync.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.userProfileChangeRequest
import android.net.Uri
import com.google.android.gms.tasks.Tasks
import kotlinx.coroutines.tasks.await
import android.util.Log
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.firestore.FirebaseFirestore
import java.util.UUID

class ProfileViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: UserRepository
    private val _uiState = MutableStateFlow<UiState>(UiState.Initial)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val _isAuthenticated = MutableStateFlow(false)
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated.asStateFlow()

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val storage = FirebaseStorage.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    init {
        val database = AppDatabase.getDatabase(application)
        repository = UserRepository(
            userDao = database.userDao(),
            application = application
        )
        checkSession()
    }

    fun checkSession() {
        viewModelScope.launch {
            try {
                val firebaseUser = FirebaseAuth.getInstance().currentUser
                if (firebaseUser != null) {
                    // Obtener datos del usuario de Firestore
                    val userDoc = firestore.collection("users")
                        .document(firebaseUser.uid)
                        .get()
                        .await()

                    if (userDoc.exists()) {
                        val userData = userDoc.data
                        val user = User(
                            id = firebaseUser.uid,
                            firstName = userData?.get("firstName") as? String ?: "",
                            lastName = userData?.get("lastName") as? String ?: "",
                            username = userData?.get("username") as? String ?: "",
                            email = userData?.get("email") as? String ?: "",
                            password = "", // Password vacío ya que no lo necesitamos para mostrar datos
                            role = userData?.get("role") as? String ?: "Ambos roles",
                            photoUrl = firebaseUser.photoUrl?.toString() ?: ""
                        )
                        _currentUser.value = user
                        _isAuthenticated.value = true
                        
                        // Actualizar la base de datos local
                        repository.updateUser(user)
                    }
                } else {
                    _currentUser.value = null
                    _isAuthenticated.value = false
                }
                _uiState.value = UiState.Initial
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Error in checkSession", e)
                _currentUser.value = null
                _isAuthenticated.value = false
                _uiState.value = UiState.Initial
            }
        }
    }

    fun clearUiState() {
        _uiState.value = UiState.Initial
    }

    fun register(
        firstName: String,
        lastName: String,
        username: String,
        email: String,
        password: String,
        role: String
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _uiState.value = UiState.Loading
            try {
                val result = repository.registerUser(firstName, lastName, username, email, password, role)
                result.fold(
                    onSuccess = {
                        _uiState.value = UiState.Loading
                        loginUser(email, password, isFirstLogin = true)
                    },
                    onFailure = { 
                        _uiState.value = UiState.Error(it.message ?: "Error al registrar la cuenta")
                    }
                )
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Error al registrar la cuenta")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loginUser(email: String, password: String, isFirstLogin: Boolean = false) {
        viewModelScope.launch {
            _isLoading.value = true
            _uiState.value = UiState.Loading
            try {
                val result = repository.loginUser(email, password)
                result.fold(
                    onSuccess = { user ->
                        _currentUser.value = user
                        _isAuthenticated.value = true
                        _uiState.value = if (isFirstLogin) {
                            UiState.Success("¡Bienvenido a SkillSync!")
                        } else {
                            UiState.Success("¡Bienvenido de vuelta!")
                        }
                        delay(2000) // Esperar 2 segundos antes de limpiar el estado
                        clearUiState()
                    },
                    onFailure = {
                        _currentUser.value = null
                        _isAuthenticated.value = false
                        _uiState.value = UiState.Error(it.message ?: "Error al iniciar sesión")
                    }
                )
            } catch (e: Exception) {
                _currentUser.value = null
                _isAuthenticated.value = false
                _uiState.value = UiState.Error(e.message ?: "Error al iniciar sesión")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            _isLoading.value = true
            _uiState.value = UiState.Loading
            try {
                repository.logout()
                _currentUser.value = null
                _isAuthenticated.value = false
                _uiState.value = UiState.Success("¡Hasta pronto!")
                delay(2000) // Esperar 2 segundos antes de limpiar el estado
                clearUiState()
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Error al cerrar sesión")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateProfile(
        firstName: String,
        lastName: String,
        username: String,
        email: String,
        role: String
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _uiState.value = UiState.Loading
            try {
                val firebaseUser = FirebaseAuth.getInstance().currentUser ?: throw Exception("No hay usuario autenticado")
                
                // Crear mapa de datos para Firestore
                val userData = hashMapOf(
                    "firstName" to firstName,
                    "lastName" to lastName,
                    "username" to username,
                    "email" to email,
                    "role" to role,
                    "photoUrl" to (firebaseUser.photoUrl?.toString() ?: "")
                )

                // Actualizar en Firestore
                firestore.collection("users")
                    .document(firebaseUser.uid)
                    .set(userData)
                    .await()

                // Actualizar el objeto User local
                val updatedUser = User(
                    id = firebaseUser.uid,
                    firstName = firstName,
                    lastName = lastName,
                    username = username,
                    email = email,
                    password = "", // Password vacío ya que no lo necesitamos para actualizar
                    role = role,
                    photoUrl = firebaseUser.photoUrl?.toString() ?: ""
                )

                // Actualizar en la base de datos local
                repository.updateUser(updatedUser)
                
                // Actualizar el estado
                _currentUser.value = updatedUser
                _uiState.value = UiState.Success("Perfil actualizado correctamente")
                delay(1000)
                clearUiState()
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Error al actualizar el perfil")
            } finally {
                _isLoading.value = false
            }
        }
    }

    private suspend fun deleteOldProfilePhotos(userId: String) {
        try {
            // Obtener referencia a la carpeta de fotos del usuario
            val userPhotosRef = storage.reference
                .child("profile_images")
                .child(userId)

            // Listar todos los archivos en la carpeta
            val result = userPhotosRef.listAll().await()
            
            // Eliminar cada archivo
            result.items.forEach { photoRef ->
                try {
                    photoRef.delete().await()
                } catch (e: Exception) {
                    Log.e("ProfileViewModel", "Error deleting old photo: ${photoRef.path}", e)
                }
            }
        } catch (e: Exception) {
            Log.e("ProfileViewModel", "Error listing old photos", e)
        }
    }

    fun updateProfilePhoto(localImageUri: String) {
        viewModelScope.launch {
            try {
                val firebaseUser = FirebaseAuth.getInstance().currentUser
                if (firebaseUser == null) {
                    _uiState.value = UiState.Error("Usuario no autenticado")
                    return@launch
                }

                // Primero eliminar las fotos anteriores
                deleteOldProfilePhotos(firebaseUser.uid)

                if (localImageUri.isBlank()) {
                    // Si no hay imagen, eliminar la foto de perfil
                    firebaseUser.updateProfile(
                        userProfileChangeRequest { photoUri = null }
                    ).await()
                    _currentUser.value = _currentUser.value?.copy(photoUrl = "")
                } else {
                    // 1. Subir nueva imagen a Storage
                    val imageRef = storage.reference
                        .child("profile_images")
                        .child(firebaseUser.uid)
                        .child("profile_${UUID.randomUUID()}.jpg")

                    // Subir archivo
                    val uploadTask = imageRef.putFile(Uri.parse(localImageUri))
                    uploadTask.await()

                    // 2. Obtener URL de descarga
                    val downloadUrl = imageRef.downloadUrl.await().toString()

                    // 3. Actualizar perfil de Firebase Auth
                    firebaseUser.updateProfile(
                        userProfileChangeRequest {
                            photoUri = Uri.parse(downloadUrl)
                        }
                    ).await()

                    // 4. Actualizar estado local
                    _currentUser.value = _currentUser.value?.copy(photoUrl = downloadUrl)
                }
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Error updating profile photo", e)
                _uiState.value = UiState.Error("Error al actualizar la foto de perfil")
            }
        }
    }

    fun resetPassword(email: String) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                FirebaseAuth.getInstance().sendPasswordResetEmail(email).await()
                _uiState.value = UiState.Success("Se ha enviado un correo para restablecer tu contraseña")
                delay(1000)
                clearUiState()
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Error al enviar el correo de restablecimiento")
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