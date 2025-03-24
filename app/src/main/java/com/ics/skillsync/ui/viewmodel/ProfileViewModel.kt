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
                        _uiState.value = UiState.Success("Registro exitoso")
                        loginUser(username, password)
                    },
                    onFailure = { 
                        _uiState.value = UiState.Error(it.message ?: "Error desconocido")
                    }
                )
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Error desconocido")
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
                        _uiState.value = UiState.Success("Inicio de sesión exitoso")
                    },
                    onFailure = {
                        _uiState.value = UiState.Error(it.message ?: "Error desconocido")
                    }
                )
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Error desconocido")
            }
        }
    }

    fun logout() {
        _currentUser.value = null
        _isAuthenticated.value = false
        _uiState.value = UiState.Initial
    }

    sealed class UiState {
        object Initial : UiState()
        object Loading : UiState()
        data class Success(val message: String) : UiState()
        data class Error(val message: String) : UiState()
    }
} 