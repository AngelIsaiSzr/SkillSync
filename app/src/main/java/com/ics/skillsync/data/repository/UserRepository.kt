package com.ics.skillsync.data.repository

import com.ics.skillsync.data.database.dao.UserDao
import com.ics.skillsync.data.database.entity.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UserRepository(private val userDao: UserDao) {
    
    suspend fun registerUser(
        firstName: String,
        lastName: String,
        username: String,
        email: String,
        password: String,
        role: String
    ): Result<Long> = withContext(Dispatchers.IO) {
        try {
            if (userDao.isUsernameExists(username)) {
                return@withContext Result.failure(Exception("El nombre de usuario ya existe"))
            }
            if (userDao.isEmailExists(email)) {
                return@withContext Result.failure(Exception("El correo electrónico ya está registrado"))
            }
            
            val user = User(
                firstName = firstName,
                lastName = lastName,
                username = username,
                email = email,
                password = password,
                role = role
            )
            
            val userId = userDao.insertUser(user)
            Result.success(userId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun loginUser(username: String, password: String): Result<User> = withContext(Dispatchers.IO) {
        try {
            val user = userDao.getUser(username, password)
            if (user != null) {
                Result.success(user)
            } else {
                Result.failure(Exception("Usuario o contraseña incorrectos"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
} 