package com.ics.skillsync.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.ics.skillsync.data.database.dao.UserDao
import com.ics.skillsync.data.database.entity.User
import com.ics.skillsync.data.database.entity.CurrentUser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import android.app.Application

class UserRepository(
    private val userDao: UserDao,
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val application: Application
) {
    
    suspend fun registerUser(
        firstName: String,
        lastName: String,
        username: String,
        email: String,
        password: String,
        role: String
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            // Validaciones básicas
            if (firstName.isBlank() || lastName.isBlank() || username.isBlank() || email.isBlank() || password.isBlank()) {
                return@withContext Result.failure(Exception("Todos los campos son obligatorios"))
            }

            if (password.length < 6) {
                return@withContext Result.failure(Exception("La contraseña debe tener al menos 6 caracteres"))
            }

            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                return@withContext Result.failure(Exception("El correo electrónico no es válido"))
            }

            // Primero intentar registrar en Firebase Auth
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            if (authResult.user == null) {
                return@withContext Result.failure(Exception("Error al crear la cuenta"))
            }

            val userId = authResult.user?.uid ?: throw Exception("Error al obtener ID de usuario")

            // Crear el objeto de usuario
            val user = User(
                id = userId,
                firstName = firstName,
                lastName = lastName,
                username = username,
                email = email,
                password = password,
                role = role
            )

            // Guardar en Firestore
            firestore.collection("users").document(userId).set(user).await()

            // Guardar en Room para acceso local
            userDao.insertUser(user)
            userDao.setCurrentUser(CurrentUser(value = user.id))

            Result.success(userId)
        } catch (e: Exception) {
            println("Error de registro: ${e.message}")
            val errorMessage = when {
                e.message?.contains("The email address is already in use by another account") == true -> "Este correo electrónico ya está registrado"
                e.message?.contains("The email address is badly formatted") == true -> "El correo electrónico no es válido"
                e.message?.contains("The password should be at least 6 characters") == true -> "La contraseña debe tener al menos 6 caracteres"
                e.message?.contains("A network error has occurred") == true -> "Error de conexión. Verifica tu conexión a internet"
                e.message?.contains("Operation not allowed") == true -> "El registro está temporalmente deshabilitado"
                e.message?.contains("Todos los campos son obligatorios") == true -> "Todos los campos son obligatorios"
                else -> "Error al registrar la cuenta. Por favor, intenta de nuevo"
            }
            Result.failure(Exception(errorMessage))
        }
    }

    suspend fun loginUser(email: String, password: String): Result<User> = withContext(Dispatchers.IO) {
        try {
            // Validaciones básicas
            if (email.isBlank() || password.isBlank()) {
                return@withContext Result.failure(Exception("El correo electrónico y la contraseña son obligatorios"))
            }

            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                return@withContext Result.failure(Exception("El correo electrónico no es válido"))
            }

            // Iniciar sesión en Firebase Auth
            val authResult = auth.signInWithEmailAndPassword(email, password).await()
            if (authResult.user == null) {
                return@withContext Result.failure(Exception("Credenciales incorrectas"))
            }

            val userId = authResult.user?.uid ?: throw Exception("Error al obtener ID de usuario")
            
            // Obtener datos del usuario de Firestore
            val userDoc = firestore.collection("users").document(userId).get().await()
            if (!userDoc.exists()) {
                return@withContext Result.failure(Exception("No se encontraron los datos del usuario"))
            }
            
            val user = userDoc.toObject(User::class.java)
            if (user == null) {
                return@withContext Result.failure(Exception("Error al cargar los datos del usuario"))
            }

            // Guardar en Room para acceso local
            userDao.insertUser(user)
            userDao.setCurrentUser(CurrentUser(value = user.id))

            Result.success(user)
        } catch (e: Exception) {
            println("Error de inicio de sesión: ${e.message}")
            val errorMessage = when {
                e.message?.contains("The email address is badly formatted") == true -> "El correo electrónico no es válido"
                e.message?.contains("The password is invalid") == true -> "Contraseña incorrecta"
                e.message?.contains("The supplied auth credential is incorrect") == true -> "Contraseña incorrecta"
                e.message?.contains("There is no user record") == true -> "No existe una cuenta con este correo electrónico"
                e.message?.contains("A network error has occurred") == true -> "Error de conexión. Verifica tu conexión a internet"
                e.message?.contains("Too many attempts") == true -> "Demasiados intentos. Por favor, espera un momento"
                e.message?.contains("This user has been disabled") == true -> "Esta cuenta ha sido deshabilitada"
                e.message?.contains("No se encontraron los datos del usuario") == true -> "No se encontraron los datos del usuario"
                e.message?.contains("Error al cargar los datos del usuario") == true -> "Error al cargar los datos del usuario"
                e.message?.contains("El correo electrónico y la contraseña son obligatorios") == true -> "El correo electrónico y la contraseña son obligatorios"
                else -> "Error al iniciar sesión. Por favor, intenta de nuevo"
            }
            Result.failure(Exception(errorMessage))
        }
    }

    suspend fun getCurrentUser(): Result<User> = withContext(Dispatchers.IO) {
        try {
            // Primero intentar obtener de Room
            val currentUser = userDao.getCurrentUser()
            if (currentUser != null) {
                val user = userDao.getUserById(currentUser.value)
                if (user != null) {
                    return@withContext Result.success(user)
                }
            }

            // Si no está en Room, obtener de Firebase Auth
            val firebaseUser = auth.currentUser
            if (firebaseUser == null) {
                return@withContext Result.failure(Exception("No hay sesión activa"))
            }

            // Obtener datos completos de Firestore
            val userDoc = firestore.collection("users").document(firebaseUser.uid).get().await()
            val user = userDoc.toObject(User::class.java) ?: throw Exception("Error al obtener datos del usuario")

            // Guardar en Room para acceso local
            userDao.insertUser(user)
            userDao.setCurrentUser(CurrentUser(value = user.id))

            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun logout() = withContext(Dispatchers.IO) {
        try {
            // Cerrar sesión en Firebase Auth
            auth.signOut()
            
            // Limpiar datos locales
            userDao.clearCurrentUser()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateUserPhoto(userId: String, photoUrl: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // Actualizar en Firestore
            firestore.collection("users").document(userId)
                .update("photoUrl", photoUrl)
                .await()

            // Actualizar en Room
            userDao.updateUserPhoto(userId, photoUrl)
            
            // Guardar la foto en SharedPreferences para persistencia
            application.getSharedPreferences("user_prefs", android.content.Context.MODE_PRIVATE)
                .edit()
                .putString("profile_photo", photoUrl)
                .apply()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateUser(user: User): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // Actualizar en Firestore
            firestore.collection("users").document(user.id)
                .set(user)
                .await()

            // Actualizar en Room
            userDao.updateUser(user)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
} 