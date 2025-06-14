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
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.FirebaseFirestoreSettings

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

    private val _temporaryPhotoUrl = MutableStateFlow<String?>(null)
    val temporaryPhotoUrl: StateFlow<String?> = _temporaryPhotoUrl.asStateFlow()

    private val storage = FirebaseStorage.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private var skillCountListener: ListenerRegistration? = null
    private var isSkillCountInitialized = false

    private val _verificationState = MutableStateFlow<VerificationState>(VerificationState.NotVerified)
    val verificationState: StateFlow<VerificationState> = _verificationState.asStateFlow()

    private val _skillCount = MutableStateFlow(0)
    val skillCount: StateFlow<Int> = _skillCount.asStateFlow()

    private val _sessionCount = MutableStateFlow(0)
    val sessionCount: StateFlow<Int> = _sessionCount.asStateFlow()

    private var sessionCountListener: ListenerRegistration? = null
    private var isSessionCountInitialized = false

    init {
        val database = AppDatabase.getDatabase(application)
        repository = UserRepository(
            userDao = database.userDao(),
            application = application
        )
        checkSession()
        
        // Agregar listener para cambios en la verificación del correo
        FirebaseAuth.getInstance().addAuthStateListener { firebaseAuth ->
            viewModelScope.launch {
                try {
                val user = firebaseAuth.currentUser
                if (user != null) {
                        try {
                    // Forzar recarga del usuario para obtener el estado más reciente
                    user.reload().await()
                    
                            // Actualizar el conteo de habilidades
                            refreshSkillCount()
                    
                    if (user.isEmailVerified) {
                        // Obtener el nivel de verificación actual de Firestore
                        val userDoc = firestore.collection("users")
                            .document(user.uid)
                            .get()
                            .await()

                        if (userDoc.exists()) {
                            val verificationLevel = userDoc.getLong("verificationLevel")?.toInt() ?: 0
                            
                            // Si el nivel es 0, actualizar a 1
                            if (verificationLevel == 0) {
                                firestore.collection("users")
                                    .document(user.uid)
                                    .update("verificationLevel", 1)
                                    .await()
                                
                                _verificationState.value = VerificationState.Verified(1)
                            }
                        }
                            }
                        } catch (e: com.google.firebase.FirebaseNetworkException) {
                            // Si hay error de red, mantener el estado actual
                            Log.w("ProfileViewModel", "Error de red en AuthStateListener", e)
                            _isAuthenticated.value = false
                            _verificationState.value = VerificationState.NotVerified
                    }
                } else {
                    // Resetear el estado cuando el usuario cierra sesión
                    isSkillCountInitialized = false
                    skillCountListener?.remove()
                    skillCountListener = null
                    _skillCount.value = 0
                    _currentUser.value = null
                    _isAuthenticated.value = false
                    _verificationState.value = VerificationState.NotVerified
                    _uiState.value = UiState.Initial
                    }
                } catch (e: Exception) {
                    Log.e("ProfileViewModel", "Error en AuthStateListener", e)
                    _isAuthenticated.value = false
                    _verificationState.value = VerificationState.NotVerified
                }
            }
        }

        viewModelScope.launch {
            try {
                val auth = FirebaseAuth.getInstance()
                auth.addAuthStateListener { firebaseAuth ->
                    val user = firebaseAuth.currentUser
                    if (user != null) {
                        Log.d("ProfileViewModel", "Usuario autenticado: ${user.uid}")
                        checkSession()
                        updateSkillCount(user.uid)
                        updateSessionCount(user.uid)
                    } else {
                        Log.d("ProfileViewModel", "No hay usuario autenticado")
                        _currentUser.value = null
                        _skillCount.value = 0
                        _sessionCount.value = 0
                    }
                }
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Error en init", e)
            }
        }
    }

    fun checkSession() {
        viewModelScope.launch {
            try {
                val firebaseUser = FirebaseAuth.getInstance().currentUser
                if (firebaseUser != null) {
                    try {
                        firebaseUser.reload().await()
                        
                        val userDoc = firestore.collection("users")
                            .document(firebaseUser.uid)
                            .get()
                            .await()

                        val userData = userDoc.data
                        val user = User(
                            id = firebaseUser.uid,
                            firstName = userData?.get("firstName") as? String ?: "",
                            lastName = userData?.get("lastName") as? String ?: "",
                            username = userData?.get("username") as? String ?: "",
                            email = userData?.get("email") as? String ?: "",
                            password = "",
                            role = userData?.get("role") as? String ?: "Ambos roles",
                            photoUrl = firebaseUser.photoUrl?.toString() ?: "",
                            biography = userData?.get("biography") as? String ?: "",
                            availability = userData?.get("availability") as? String ?: ""
                        )
                        _currentUser.value = user
                        _isAuthenticated.value = true
                        
                        repository.updateUser(user)
                        checkVerificationStatus()
                        
                        // Actualizar contadores
                        Log.d("ProfileViewModel", "Actualizando contadores para usuario: ${user.id}")
                        refreshSkillCount()
                        refreshSessionCount()
                    } catch (e: com.google.firebase.FirebaseNetworkException) {
                        Log.w("ProfileViewModel", "Error de red al verificar sesión", e)
                        _isAuthenticated.value = false
                        _verificationState.value = VerificationState.NotVerified
                    }
                } else {
                    _currentUser.value = null
                    _isAuthenticated.value = false
                    _verificationState.value = VerificationState.NotVerified
                }
                _uiState.value = UiState.Initial
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Error in checkSession", e)
                _currentUser.value = null
                _isAuthenticated.value = false
                _verificationState.value = VerificationState.NotVerified
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
                        // Verificar el estado de verificación después del login exitoso
                        checkVerificationStatus()
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
                        _verificationState.value = VerificationState.NotVerified
                        _uiState.value = UiState.Error(it.message ?: "Error al iniciar sesión")
                    }
                )
            } catch (e: Exception) {
                _currentUser.value = null
                _isAuthenticated.value = false
                _verificationState.value = VerificationState.NotVerified
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
                _verificationState.value = VerificationState.NotVerified
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
        role: String,
        biography: String = "",
        availability: String = ""
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
                    "photoUrl" to (firebaseUser.photoUrl?.toString() ?: ""),
                    "biography" to biography,
                    "availability" to availability
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
                    photoUrl = firebaseUser.photoUrl?.toString() ?: "",
                    biography = biography,
                    availability = availability
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
                // Si la URI está vacía, limpiar la foto temporal
                if (localImageUri.isBlank()) {
                    _temporaryPhotoUrl.value = null
                } else {
                    // Actualizar la foto temporal
                    _temporaryPhotoUrl.value = localImageUri
                }
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Error updating profile photo", e)
                _uiState.value = UiState.Error("Error al actualizar la foto de perfil")
            }
        }
    }

    // Nuevo metodo para guardar la foto de perfil en Firebase
    fun saveProfilePhoto(localImageUri: String) {
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
                
                // Limpiar la foto temporal
                _temporaryPhotoUrl.value = null
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Error saving profile photo", e)
                _uiState.value = UiState.Error("Error al guardar la foto de perfil")
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

    fun checkVerificationStatus() {
        viewModelScope.launch {
            try {
                val firebaseUser = FirebaseAuth.getInstance().currentUser
                if (firebaseUser != null) {
                    try {
                    // Forzar recarga del usuario para obtener el estado más reciente
                    firebaseUser.reload().await()
                    
                    // Verificar si el correo está verificado
                    if (firebaseUser.isEmailVerified) {
                        // Obtener el nivel de verificación actual de Firestore
                        val userDoc = firestore.collection("users")
                            .document(firebaseUser.uid)
                            .get()
                            .await()

                        if (userDoc.exists()) {
                            val verificationLevel = userDoc.getLong("verificationLevel")?.toInt() ?: 0
                            
                            // Si el nivel es 0, actualizar a 1
                            if (verificationLevel == 0) {
                                firestore.collection("users")
                                    .document(firebaseUser.uid)
                                    .update("verificationLevel", 1)
                                    .await()
                                
                                _verificationState.value = VerificationState.Verified(1)
                            } else {
                                _verificationState.value = VerificationState.Verified(verificationLevel)
                            }
                        }
                    } else {
                        _verificationState.value = VerificationState.NotVerified
                        }
                    } catch (e: com.google.firebase.FirebaseNetworkException) {
                        // Si hay error de red, mantener el estado actual
                        Log.w("ProfileViewModel", "Error de red al verificar estado", e)
                    }
                }
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Error checking verification status", e)
            }
        }
    }

    fun verifyEmail() {
        viewModelScope.launch {
            try {
                val firebaseUser = FirebaseAuth.getInstance().currentUser
                if (firebaseUser != null) {
                    firebaseUser.sendEmailVerification().await()
                    _uiState.value = UiState.Success("Se ha enviado un correo de verificación")
                }
            } catch (e: Exception) {
                _uiState.value = UiState.Error("Error al enviar el correo de verificación")
            }
        }
    }

    fun uploadVerificationImage(imageUri: Uri) {
        viewModelScope.launch {
            try {
                val firebaseUser = FirebaseAuth.getInstance().currentUser
                if (firebaseUser != null) {
                    val storageRef = storage.reference
                    val imageRef = storageRef.child("verification_images/${firebaseUser.uid}/${UUID.randomUUID()}")
                    
                    imageRef.putFile(imageUri).await()
                    val imageUrl = imageRef.downloadUrl.await().toString()

                    // Actualizar en Firestore
                    firestore.collection("users")
                        .document(firebaseUser.uid)
                        .update("verificationLevel", 2)
                        .await()

                    _uiState.value = UiState.Success("Imagen de verificación subida correctamente")
                    checkVerificationStatus()
                }
            } catch (e: Exception) {
                _uiState.value = UiState.Error("Error al subir la imagen de verificación")
            }
        }
    }

    fun completeVerificationExam() {
        viewModelScope.launch {
            try {
                val firebaseUser = FirebaseAuth.getInstance().currentUser
                if (firebaseUser != null) {
                    // Actualizar en Firestore
                    firestore.collection("users")
                        .document(firebaseUser.uid)
                        .update("verificationLevel", 3)
                        .await()

                    _uiState.value = UiState.Success("¡Felicidades! Has completado la verificación")
                    checkVerificationStatus()
                }
            } catch (e: Exception) {
                _uiState.value = UiState.Error("Error al completar la verificación")
            }
        }
    }

    private fun updateSkillCount(userId: String) {
        viewModelScope.launch {
            try {
                // Remover el listener anterior si existe
                skillCountListener?.remove()
                skillCountListener = null

                // Limpiar el valor actual
                _skillCount.value = 0

                // Configurar persistencia para Firestore
                firestore.firestoreSettings = FirebaseFirestoreSettings.Builder()
                    .setPersistenceEnabled(true)
                    .build()

                // Obtener el conteo inicial directamente de Firestore con persistencia
                val skillsSnapshot = firestore.collection("users")
                    .document(userId)
                    .collection("skills")
                    .get()
                    .await()
                
                // Establecer el conteo inicial
                _skillCount.value = skillsSnapshot.size()

                // Agregar el nuevo listener con persistencia
                skillCountListener = firestore.collection("users")
                    .document(userId)
                    .collection("skills")
                    .addSnapshotListener { snapshot, e ->
                        if (e != null) {
                            Log.e("ProfileViewModel", "Error getting skill count", e)
                            // En caso de error, intentar obtener el conteo directamente
                            viewModelScope.launch {
                                try {
                                    val directSnapshot = firestore.collection("users")
                                        .document(userId)
                                        .collection("skills")
                                        .get()
                                        .await()
                                    _skillCount.value = directSnapshot.size()
                                } catch (e2: Exception) {
                                    Log.e("ProfileViewModel", "Error getting direct skill count", e2)
                                }
                            }
                            return@addSnapshotListener
                        }

                        if (snapshot != null) {
                            // Actualizar el conteo con el tamaño real de la colección
                            _skillCount.value = snapshot.size()
                        }
                    }
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Error updating skill count", e)
                // En caso de error, intentar reconectar después de un breve retraso
                delay(1000)
                updateSkillCount(userId)
            }
        }
    }

    // Agregar metodo para actualizar el conteo cuando se restablece la conexión
    fun refreshSkillCount() {
        viewModelScope.launch {
            try {
                val firebaseUser = FirebaseAuth.getInstance().currentUser
                if (firebaseUser != null) {
                    // Forzar una reconexión del listener
                    skillCountListener?.remove()
                    skillCountListener = null
                    
                    // Obtener el conteo directamente primero
                    val directSnapshot = firestore.collection("users")
                        .document(firebaseUser.uid)
                        .collection("skills")
                        .get()
                        .await()
                    _skillCount.value = directSnapshot.size()
                    
                    // Luego actualizar el listener
                    updateSkillCount(firebaseUser.uid)
                }
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Error refreshing skill count", e)
            }
        }
    }

    private fun updateSessionCount(userId: String) {
        viewModelScope.launch {
            try {
                Log.d("ProfileViewModel", "Iniciando actualización de contador de sesiones para usuario: $userId")
                
                // Remover listener anterior
                sessionCountListener?.remove()
                sessionCountListener = null
                _sessionCount.value = 0

                // Obtener el rol del usuario actual
                val userDoc = firestore.collection("users")
                    .document(userId)
                    .get()
                    .await()
                val userRole = userDoc.getString("role") ?: "Ambos roles"
                
                Log.d("ProfileViewModel", "Rol del usuario: $userRole")

                when (userRole) {
                    "Mentor" -> {
                        // Solo contar sesiones como mentor
                        val mentorSessionsSnapshot = firestore.collection("sessions")
                            .whereEqualTo("mentorId", userId)
                            .get()
                            .await()
                        _sessionCount.value = mentorSessionsSnapshot.size()
                        Log.d("ProfileViewModel", "Sesiones como mentor: ${mentorSessionsSnapshot.size()}")

                        // Listener para sesiones como mentor
                        sessionCountListener = firestore.collection("sessions")
                            .whereEqualTo("mentorId", userId)
                            .addSnapshotListener { snapshot, e ->
                                if (e != null) {
                                    Log.e("ProfileViewModel", "Error getting mentor sessions count", e)
                                    return@addSnapshotListener
                                }
                                val count = snapshot?.size() ?: 0
                                Log.d("ProfileViewModel", "Actualizando sesiones como mentor: $count")
                                _sessionCount.value = count
                            }
                    }
                    "Aprendiz" -> {
                        // Solo contar sesiones como aprendiz
                        val learnerSessionsSnapshot = firestore.collection("sessions")
                            .whereEqualTo("learnerId", userId)
                            .get()
                            .await()
                        _sessionCount.value = learnerSessionsSnapshot.size()
                        Log.d("ProfileViewModel", "Sesiones como aprendiz: ${learnerSessionsSnapshot.size()}")

                        // Listener para sesiones como aprendiz
                        sessionCountListener = firestore.collection("sessions")
                            .whereEqualTo("learnerId", userId)
                            .addSnapshotListener { snapshot, e ->
                                if (e != null) {
                                    Log.e("ProfileViewModel", "Error getting learner sessions count", e)
                                    return@addSnapshotListener
                                }
                                val count = snapshot?.size() ?: 0
                                Log.d("ProfileViewModel", "Actualizando sesiones como aprendiz: $count")
                                _sessionCount.value = count
                            }
                    }
                    else -> {
                        // Contar sesiones como mentor y aprendiz
                        val mentorSessionsSnapshot = firestore.collection("sessions")
                            .whereEqualTo("mentorId", userId)
                            .get()
                            .await()
                        val learnerSessionsSnapshot = firestore.collection("sessions")
                            .whereEqualTo("learnerId", userId)
                            .get()
                            .await()
                        val totalCount = mentorSessionsSnapshot.size() + learnerSessionsSnapshot.size()
                        _sessionCount.value = totalCount
                        Log.d("ProfileViewModel", "Sesiones totales (mentor + aprendiz): $totalCount")

                        // Listener para sesiones como mentor
                        val mentorListener = firestore.collection("sessions")
                            .whereEqualTo("mentorId", userId)
                            .addSnapshotListener { snapshot, e ->
                                if (e != null) {
                                    Log.e("ProfileViewModel", "Error getting mentor sessions count", e)
                                    return@addSnapshotListener
                                }
                                updateTotalSessionCount(snapshot?.size() ?: 0, true)
                            }

                        // Listener para sesiones como aprendiz
                        val learnerListener = firestore.collection("sessions")
                            .whereEqualTo("learnerId", userId)
                            .addSnapshotListener { snapshot, e ->
                                if (e != null) {
                                    Log.e("ProfileViewModel", "Error getting learner sessions count", e)
                                    return@addSnapshotListener
                                }
                                updateTotalSessionCount(snapshot?.size() ?: 0, false)
                            }

                        // Guardar ambos listeners
                        sessionCountListener = object : ListenerRegistration {
                            override fun remove() {
                                mentorListener.remove()
                                learnerListener.remove()
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Error updating session count", e)
                delay(1000)
                updateSessionCount(userId)
            }
        }
    }

    private fun updateTotalSessionCount(newCount: Int, isMentor: Boolean) {
        viewModelScope.launch {
            try {
                val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return@launch
                val otherCount = if (isMentor) {
                    // Si es mentor, obtener conteo de aprendiz
                    firestore.collection("sessions")
                        .whereEqualTo("learnerId", userId)
                        .get()
                        .await()
                        .size()
                } else {
                    // Si es aprendiz, obtener conteo de mentor
                    firestore.collection("sessions")
                        .whereEqualTo("mentorId", userId)
                        .get()
                        .await()
                        .size()
                }
                val totalCount = newCount + otherCount
                Log.d("ProfileViewModel", "Actualizando total de sesiones: $totalCount (nuevo: $newCount, otro: $otherCount)")
                _sessionCount.value = totalCount
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Error updating total session count", e)
            }
        }
    }

    fun refreshSessionCount() {
        viewModelScope.launch {
            try {
                val firebaseUser = FirebaseAuth.getInstance().currentUser
                if (firebaseUser != null) {
                    sessionCountListener?.remove()
                    sessionCountListener = null
                    updateSessionCount(firebaseUser.uid)
                }
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Error refreshing session count", e)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        skillCountListener?.remove()
        skillCountListener = null
        sessionCountListener?.remove()
        sessionCountListener = null
        isSkillCountInitialized = false
        isSessionCountInitialized = false
        _skillCount.value = 0
        _sessionCount.value = 0
    }

    sealed class UiState {
        object Initial : UiState()
        object Loading : UiState()
        data class Success(val message: String) : UiState()
        data class Error(val message: String) : UiState()
    }

    sealed class VerificationState {
        object NotVerified : VerificationState()
        data class Verified(val level: Int) : VerificationState()
    }
} 