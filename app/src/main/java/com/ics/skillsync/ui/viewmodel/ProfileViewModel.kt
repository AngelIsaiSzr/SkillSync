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

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

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

    sealed class UiState {
        object Initial : UiState()
        object Loading : UiState()
        data class Success(val message: String) : UiState()
        data class Error(val message: String) : UiState()
    }
} 