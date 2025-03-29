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

class ProfileViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: UserRepository
    private val _uiState = MutableStateFlow<UiState>(UiState.Initial)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val _isAuthenticated = MutableStateFlow(false)
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated.asStateFlow()

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    init {
        val database = AppDatabase.getDatabase(application)
        repository = UserRepository(database.userDao())
        checkSession()
    }

    private fun checkSession() {
        viewModelScope.launch {
            try {
                val result = repository.getCurrentUser()
                result.fold(
                    onSuccess = { user ->
                        _currentUser.value = user
                        _isAuthenticated.value = true
                        _uiState.value = UiState.Initial
                    },
                    onFailure = {
                        _currentUser.value = null
                        _isAuthenticated.value = false
                        _uiState.value = UiState.Initial
                    }
                )
            } catch (e: Exception) {
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
            _uiState.value = UiState.Loading
            try {
                val result = repository.registerUser(firstName, lastName, username, email, password, role)
                result.fold(
                    onSuccess = { 
                        _uiState.value = UiState.Loading
                        loginUser(username, password)
                    },
                    onFailure = { 
                        _uiState.value = UiState.Error(when (it.message) {
                            "Ya existe" -> "Esta cuenta ya existe"
                            "El correo electrónico ya está registrado" -> "Este correo electrónico ya está registrado"
                            else -> "Error al registrar la cuenta"
                        })
                    }
                )
            } catch (e: Exception) {
                _uiState.value = UiState.Error("Error al registrar la cuenta")
            }
        }
    }

    fun loginUser(username: String, password: String) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                val result = repository.loginUser(username, password)
                result.fold(
                    onSuccess = { user ->
                        _currentUser.value = user
                        _isAuthenticated.value = true
                        _uiState.value = UiState.Success("¡Bienvenido de vuelta!")
                        delay(2000) // Esperar 2 segundos antes de limpiar el estado
                        clearUiState()
                    },
                    onFailure = {
                        _currentUser.value = null
                        _isAuthenticated.value = false
                        _uiState.value = UiState.Error(when (it.message) {
                            "No existe" -> "El usuario no existe"
                            "Contraseña incorrecta" -> "Contraseña incorrecta"
                            else -> "Error al iniciar sesión"
                        })
                    }
                )
            } catch (e: Exception) {
                _currentUser.value = null
                _isAuthenticated.value = false
                _uiState.value = UiState.Error("Error al iniciar sesión")
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                repository.logout()
                _currentUser.value = null
                _isAuthenticated.value = false
                _uiState.value = UiState.Success("¡Hasta pronto!")
                delay(2000) // Esperar 2 segundos antes de limpiar el estado
                clearUiState()
            } catch (e: Exception) {
                _uiState.value = UiState.Error("Error al cerrar sesión")
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