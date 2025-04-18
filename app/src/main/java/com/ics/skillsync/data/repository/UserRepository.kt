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

            try {
                // Primero intentar registrar en Firebase Auth
                val authResult = auth.createUserWithEmailAndPassword(email, password).await()
                val userId = authResult.user?.uid ?: throw Exception("Error al crear la cuenta")

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
            } catch (e: com.google.firebase.auth.FirebaseAuthWeakPasswordException) {
                Result.failure(Exception("La contraseña es muy débil"))
            } catch (e: com.google.firebase.auth.FirebaseAuthInvalidCredentialsException) {
                Result.failure(Exception("El correo electrónico no es válido"))
            } catch (e: com.google.firebase.auth.FirebaseAuthUserCollisionException) {
                Result.failure(Exception("Ya existe una cuenta con este correo electrónico"))
            } catch (e: com.google.firebase.FirebaseNetworkException) {
                Result.failure(Exception("Error de conexión. Verifica tu conexión a internet"))
            } catch (e: com.google.firebase.auth.FirebaseAuthException) {
                Result.failure(Exception("Error al registrarte. Por favor, intenta de nuevo"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Error al registrarte. Por favor, intenta de nuevo"))
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

            try {
                // Iniciar sesión en Firebase Auth
                val authResult = auth.signInWithEmailAndPassword(email, password).await()
                val firebaseUser = authResult.user ?: throw Exception("Error al iniciar sesión")

                // Obtener datos del usuario de Firestore
                val userDoc = firestore.collection("users")
                    .document(firebaseUser.uid)
                    .get()
                    .await()

                if (!userDoc.exists()) {
                    throw Exception("No se encontraron los datos del usuario")
                }

                val userData = userDoc.data ?: throw Exception("Error al cargar los datos del usuario")
                
                val user = User(
                    id = firebaseUser.uid,
                    firstName = userData["firstName"] as? String ?: "",
                    lastName = userData["lastName"] as? String ?: "",
                    username = userData["username"] as? String ?: "",
                    email = userData["email"] as? String ?: "",
                    password = "", // No guardamos la contraseña en la base de datos local
                    role = userData["role"] as? String ?: "Ambos roles",
                    photoUrl = firebaseUser.photoUrl?.toString() ?: ""
                )

                // Guardar en Room para acceso local
                userDao.insertUser(user)
                userDao.setCurrentUser(CurrentUser(value = user.id))

                Result.success(user)
            } catch (e: com.google.firebase.auth.FirebaseAuthInvalidCredentialsException) {
                Result.failure(Exception("Correo o contraseña incorrectos"))
            } catch (e: com.google.firebase.auth.FirebaseAuthInvalidUserException) {
                Result.failure(Exception("No se encontró la cuenta o fue deshabilitada"))
            } catch (e: com.google.firebase.FirebaseNetworkException) {
                Result.failure(Exception("Error de conexión. Verifica tu conexión a internet"))
            } catch (e: com.google.firebase.auth.FirebaseAuthException) {
                Result.failure(Exception("Error al iniciar sesión. Por favor, intenta de nuevo"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Error al iniciar sesión. Por favor, intenta de nuevo"))
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
            // Verificar la conexión a internet primero
            try {
                val response = firestore.collection("users").document("test").get().await()
                if (!response.exists()) {
                    // Esta bien que no exista, solo verificamos que podamos hacer la petición
                }
            } catch (e: Exception) {
                return@withContext Result.failure(Exception("Error de conexión. Verifica tu conexión a internet"))
            }

            // Si llegamos aquí, hay conexión, procedemos con el cierre de sesión
            auth.signOut()
            userDao.clearCurrentUser()
            
            Result.success(Unit)
        } catch (e: com.google.firebase.FirebaseNetworkException) {
            Result.failure(Exception("Error de conexión. Verifica tu conexión a internet"))
        } catch (e: Exception) {
            Result.failure(Exception("Error al cerrar sesión"))
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

    suspend fun updateVerificationLevel(userId: String, level: Int): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // Actualizar en Firestore
            firestore.collection("users")
                .document(userId)
                .update("verificationLevel", level)
                .await()

            // Actualizar en Room
            userDao.updateUserVerificationLevel(userId, level)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getVerificationLevel(userId: String): Result<Int> = withContext(Dispatchers.IO) {
        try {
            // Obtener de Firestore
            val userDoc = firestore.collection("users")
                .document(userId)
                .get()
                .await()

            val level = userDoc.getLong("verificationLevel")?.toInt() ?: 0
            Result.success(level)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
} 