package com.ics.skillsync.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.ics.skillsync.ui.components.SharedTopBar
import com.ics.skillsync.ui.components.SharedBottomBar
import com.ics.skillsync.ui.components.SharedNavigationDrawer
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.rememberDrawerState
import kotlinx.coroutines.launch
import com.ics.skillsync.ui.viewmodel.ProfileViewModel
import com.ics.skillsync.ui.viewmodel.TeachingCardViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.ics.skillsync.model.Skill
import com.ics.skillsync.model.SkillCategory
import com.ics.skillsync.model.TeachingCard
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

sealed class SearchResult {
    data class SkillResult(val skill: Skill) : SearchResult()
    data class UserResult(
        val id: String,
        val name: String,
        val role: String
    ) : SearchResult()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    navController: NavController,
    viewModel: ProfileViewModel,
    teachingCardViewModel: TeachingCardViewModel
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var searchQuery by remember { mutableStateOf("") }
    var searchResults by remember { mutableStateOf<List<SearchResult>>(emptyList()) }
    var isSearching by remember { mutableStateOf(false) }
    val db = FirebaseFirestore.getInstance()
    val teachingCards by teachingCardViewModel.teachingCards.collectAsState()

    // Función para realizar la búsqueda
    fun performSearch(query: String) {
        if (query.isBlank()) {
            searchResults = emptyList()
            isSearching = false
            return
        }

        isSearching = true
        scope.launch {
            try {
                // Buscar habilidades desde TeachingCards
                val skills = teachingCards.map { card ->
                    Skill(
                        id = card.id,
                        name = card.title,
                        description = card.description,
                        category = SkillCategory.fromString(card.category),
                        imageUrl = card.imageUrl,
                        mentorsCount = 1,
                        learnersCount = card.learnerCount,
                        mentorName = card.mentorName,
                        level = when (card.experienceLevel) {
                            TeachingCard.ExperienceLevel.PRINCIPIANTE -> 1
                            TeachingCard.ExperienceLevel.BASICO -> 2
                            TeachingCard.ExperienceLevel.INTERMEDIO -> 3
                            TeachingCard.ExperienceLevel.AVANZADO -> 4
                            TeachingCard.ExperienceLevel.EXPERTO -> 5
                        }
                    )
                }.filter { skill ->
                    skill.name.contains(query, ignoreCase = true) ||
                    skill.description.contains(query, ignoreCase = true) ||
                    skill.mentorName.contains(query, ignoreCase = true)
                }

                // Buscar usuarios
                val usersSnapshot = db.collection("users")
                    .get()
                    .await()

                val users = usersSnapshot.documents.mapNotNull { doc ->
                    try {
                        val firstName = doc.getString("firstName") ?: ""
                        val lastName = doc.getString("lastName") ?: ""
                        val role = doc.getString("role") ?: ""
                        val fullName = "$firstName $lastName"
                        
                        if (firstName.isNotBlank() && lastName.isNotBlank() &&
                            (fullName.contains(query, ignoreCase = true) ||
                             role.contains(query, ignoreCase = true))) {
                            SearchResult.UserResult(
                                id = doc.id,
                                name = fullName,
                                role = role
                            )
                        } else null
                    } catch (e: Exception) {
                        null
                    }
                }

                // Combinar resultados
                searchResults = users + skills.map { skill -> SearchResult.SkillResult(skill) }
                isSearching = false
            } catch (e: Exception) {
                searchResults = emptyList()
                isSearching = false
            }
        }
    }

    SharedNavigationDrawer(
        navController = navController,
        viewModel = viewModel,
        drawerState = drawerState
    ) {
        Scaffold(
            topBar = {
                SharedTopBar(
                    navController = navController,
                    viewModel = viewModel,
                    title = "SkillSync",
                    onDrawerOpen = {
                        scope.launch { drawerState.open() }
                    }
                )
            },
            bottomBar = {
                SharedBottomBar(navController = navController)
            },
            containerColor = Color.White
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Barra de búsqueda
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { 
                        searchQuery = it
                        if (it.isBlank()) {
                            searchResults = emptyList()
                        } else {
                            performSearch(it)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Buscar habilidades o usuarios...") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Buscar",
                            tint = Color(0xFF5B4DBC)
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { 
                                searchQuery = ""
                                searchResults = emptyList()
                            }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Limpiar",
                                    tint = Color(0xFF5B4DBC)
                                )
                            }
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF5B4DBC),
                        unfocusedBorderColor = Color(0xFF5B4DBC).copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                // Resultados de búsqueda
                if (isSearching) {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = Color(0xFF5B4DBC)
                        )
                    }
                } else if (searchResults.isEmpty() && searchQuery.isNotEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No se encontraron resultados",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.Gray
                        )
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(searchResults) { result ->
                            when (result) {
                                is SearchResult.SkillResult -> SkillResultItem(result.skill)
                                is SearchResult.UserResult -> UserResultItem(result)
                            }
                        }
                    }
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        teachingCardViewModel.refreshTeachingCards()
    }
}

@Composable
private fun SkillResultItem(skill: Skill) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.School,
                contentDescription = null,
                tint = Color(0xFF5B4DBC),
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = skill.name,
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color(0xFF111827),
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = skill.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF6B7280)
                )
            }
        }
    }
}

@Composable
private fun UserResultItem(user: SearchResult.UserResult) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                tint = Color(0xFF5B4DBC),
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = user.name,
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color(0xFF111827),
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = user.role,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF6B7280)
                )
            }
        }
    }
} 