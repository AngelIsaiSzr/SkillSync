package com.ics.skillsync.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ics.skillsync.data.database.AppDatabase
import com.ics.skillsync.data.database.entity.Skill
import com.ics.skillsync.data.repository.SkillRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class SkillViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: SkillRepository
    private val _uiState = MutableStateFlow<UiState>(UiState.Initial)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val _isAuthenticated = MutableStateFlow(false)
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated.asStateFlow()

    private val _userSkills = MutableStateFlow<List<Skill>>(emptyList())
    val userSkills: StateFlow<List<Skill>> = _userSkills.asStateFlow()

    private val _teachSkills = MutableStateFlow<List<Skill>>(emptyList())
    val teachSkills: StateFlow<List<Skill>> = _teachSkills.asStateFlow()

    private val _learnSkills = MutableStateFlow<List<Skill>>(emptyList())
    val learnSkills: StateFlow<List<Skill>> = _learnSkills.asStateFlow()

    private val _userRole = MutableStateFlow<UserRole>(UserRole.BOTH)
    val userRole: StateFlow<UserRole> = _userRole.asStateFlow()

    enum class UserRole {
        MENTOR, LEARNER, BOTH
    }

    data class SkillLevel(
        val id: Int,
        val name: String,
        val description: String = ""
    )

    val skillLevels = listOf(
        SkillLevel(1, "Principiante", "Comenzando a aprender"),
        SkillLevel(2, "Básico", "Conocimientos fundamentales"),
        SkillLevel(3, "Intermedio", "Buen dominio"),
        SkillLevel(4, "Avanzado", "Alto nivel de competencia"),
        SkillLevel(5, "Experto", "Dominio completo")
    )

    // Lista de habilidades predefinidas
    val predefinedSkills = listOf(
        "Programación",
        "Diseño gráfico",
        "Marketing digital",
        "Idiomas",
        "Música",
        "Fotografía",
        "Cocina",
        "Deportes",
        "Matemáticas",
        "Física",
        "Química",
        "Biología",
        "Historia",
        "Literatura",
        "Filosofía",
        "Psicología",
        "Economía",
        "Contabilidad",
        "Derecho",
        "Medicina"
    )

    init {
        val database = AppDatabase.getDatabase(application)
        repository = SkillRepository(
            skillDao = database.skillDao(),
            application = application
        )
        
        viewModelScope.launch {
            delay(500)
            checkAuthState()
            
            FirebaseAuth.getInstance().addAuthStateListener { firebaseAuth ->
                val user = firebaseAuth.currentUser
                _isAuthenticated.value = user != null
                if (user != null) {
                    loadUserRole(user.uid)
                    loadUserSkills()
                } else {
                    _userSkills.value = emptyList()
                    _teachSkills.value = emptyList()
                    _learnSkills.value = emptyList()
                    _userRole.value = UserRole.BOTH
                }
            }
        }
    }

    private fun checkAuthState() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        _isAuthenticated.value = currentUser != null
        if (currentUser != null) {
            loadUserRole(currentUser.uid)
            loadUserSkills()
        } else {
            _userRole.value = UserRole.BOTH
        }
    }

    private fun loadUserRole(userId: String) {
        viewModelScope.launch {
            try {
                val userDoc = FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(userId)
                    .get()
                    .await()

                if (userDoc.exists()) {
                    val role = userDoc.getString("role") ?: "Ambos roles"
                    Log.d("SkillViewModel", "Role loaded from Firestore: $role")
                    _userRole.value = when (role) {
                        "Mentor" -> UserRole.MENTOR
                        "Aprendiz" -> UserRole.LEARNER
                        else -> UserRole.BOTH
                    }
                    Log.d("SkillViewModel", "UserRole set to: ${_userRole.value}")
                } else {
                    Log.w("SkillViewModel", "User document does not exist")
                    _userRole.value = UserRole.BOTH
                }
            } catch (e: Exception) {
                Log.e("SkillViewModel", "Error loading user role", e)
                _userRole.value = UserRole.BOTH
            }
        }
    }

    fun loadUserSkills() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            _uiState.value = UiState.Error("Por favor inicia sesión para ver tus habilidades")
            return
        }
        
        viewModelScope.launch {
            try {
                // Primero sincronizamos con Firebase
                repository.syncUserSkills(userId)
                
                // Limpiar habilidades duplicadas o incorrectas
                cleanDuplicateSkills(userId)
                
                // Luego observamos los cambios en la base de datos local
                repository.getUserSkills(userId).collect { skills ->
                    _userSkills.value = skills
                    _teachSkills.value = skills.filter { it.type == Skill.SkillType.TEACH }
                    _learnSkills.value = skills.filter { it.type == Skill.SkillType.LEARN }
                }
            } catch (e: Exception) {
                Log.e("SkillViewModel", "Error loading skills", e)
                _uiState.value = UiState.Error("Error al cargar las habilidades: ${e.message}")
            }
        }
    }

    private suspend fun cleanDuplicateSkills(userId: String) {
        try {
            // Obtener todas las habilidades de Firestore
            val firestoreSkills = FirebaseFirestore.getInstance()
                .collection("users")
                .document(userId)
                .collection("skills")
                .get()
                .await()

            // Eliminar todas las habilidades existentes
            firestoreSkills.documents.forEach { doc ->
                doc.reference.delete().await()
            }

            // Volver a agregar las habilidades correctas desde la base de datos local
            val localSkills = repository.getUserSkills(userId).first()
            localSkills.forEach { skill ->
                FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(userId)
                    .collection("skills")
                    .document()
                    .set(mapOf(
                        "name" to skill.name,
                        "type" to skill.type,
                        "level" to skill.level
                    ))
                    .await()
            }
        } catch (e: Exception) {
            Log.e("SkillViewModel", "Error cleaning duplicate skills", e)
        }
    }

    fun addSkill(name: String, type: Skill.SkillType, level: Int) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            _uiState.value = UiState.Error("Por favor inicia sesión para agregar habilidades")
            return
        }
        
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                repository.addSkill(userId, name, type, level).fold(
                    onSuccess = {
                        // Actualizar la colección de skills del usuario en Firestore
                        FirebaseFirestore.getInstance()
                            .collection("users")
                            .document(userId)
                            .collection("skills")
                            .document()
                            .set(mapOf(
                                "name" to name,
                                "type" to type,
                                "level" to level
                            ))
                            .await()
                            
                        _uiState.value = UiState.Success("Habilidad agregada correctamente")
                        // Recargar las habilidades después de agregar
                        loadUserSkills()
                    },
                    onFailure = {
                        _uiState.value = UiState.Error(it.message ?: "Error al agregar la habilidad")
                    }
                )
            } catch (e: Exception) {
                Log.e("SkillViewModel", "Error adding skill", e)
                _uiState.value = UiState.Error("Error al agregar la habilidad: ${e.message}")
            }
        }
    }

    fun deleteSkill(skill: Skill) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            _uiState.value = UiState.Error("Por favor inicia sesión para eliminar habilidades")
            return
        }
        
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                repository.deleteSkill(skill).fold(
                    onSuccess = {
                        // Eliminar también de la colección de skills del usuario en Firestore
                        FirebaseFirestore.getInstance()
                            .collection("users")
                            .document(userId)
                            .collection("skills")
                            .whereEqualTo("name", skill.name)
                            .whereEqualTo("type", skill.type)
                            .whereEqualTo("level", skill.level)
                            .get()
                            .await()
                            .documents
                            .forEach { doc ->
                                doc.reference.delete().await()
                            }
                            
                        _uiState.value = UiState.Success("Habilidad eliminada correctamente")
                        // Recargar las habilidades después de eliminar
                        loadUserSkills()
                    },
                    onFailure = {
                        _uiState.value = UiState.Error(it.message ?: "Error al eliminar la habilidad")
                    }
                )
            } catch (e: Exception) {
                Log.e("SkillViewModel", "Error deleting skill", e)
                _uiState.value = UiState.Error("Error al eliminar la habilidad: ${e.message}")
            }
        }
    }

    fun resetState() {
        _uiState.value = UiState.Initial
    }

    fun updateUserRole(newRole: UserRole) {
        viewModelScope.launch {
            _userRole.value = newRole
            Log.d("SkillViewModel", "Role updated manually to: $newRole")
        }
    }

    sealed class UiState {
        object Initial : UiState()
        object Loading : UiState()
        data class Success(val message: String) : UiState()
        data class Error(val message: String) : UiState()
    }
} 