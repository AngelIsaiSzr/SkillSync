package com.ics.skillsync.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.ics.skillsync.model.User
import com.ics.skillsync.model.UserType
import com.ics.skillsync.model.Skill
import com.ics.skillsync.model.SkillCategory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SkillDetailScreen(skillId: String, navController: NavController) {
    val skill = getSkillById(skillId)
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(skill.name) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Descripción de la habilidad
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Sobre esta habilidad",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = skill.description,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            // Lista de mentores
            Text(
                text = "Mentores disponibles",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(16.dp)
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(getMentorsForSkill(skill.name)) { mentor ->
                    MentorCard(mentor = mentor, navController = navController)
                }
            }
        }
    }
}

@Composable
fun MentorCard(mentor: User, navController: NavController) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { navController.navigate("mentor/${mentor.id}") },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.size(56.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.padding(16.dp),
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = mentor.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Star,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${mentor.rating} (${mentor.reviews} reseñas)",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = { navController.navigate("chat/${mentor.id}") },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text("Contactar")
            }
        }
    }
}

private fun getMentorsForSkill(skillName: String): List<User> {
    // Simulación de datos
    return listOf(
        User(
            id = "1",
            name = "Ana García",
            userType = UserType.MENTOR,
            skills = listOf(skillName, "Diseño UI/UX"),
            rating = 4.8f,
            reviews = 15
        ),
        User(
            id = "2",
            name = "Carlos Ruiz",
            userType = UserType.MENTOR,
            skills = listOf(skillName, "Francés"),
            rating = 4.9f,
            reviews = 8
        ),
        User(
            id = "3",
            name = "María López",
            userType = UserType.MENTOR,
            skills = listOf(skillName, "Teoría Musical"),
            rating = 4.7f,
            reviews = 12
        )
    )
}

private fun getSkillById(skillId: String): Skill {
    // Simulación de datos - En una aplicación real, esto vendría de una base de datos
    return Skill(
        id = skillId,
        name = when (skillId) {
            "1" -> "Inglés Conversacional"
            "2" -> "Español para Principiantes"
            "3" -> "Francés Básico"
            "4" -> "Desarrollo Web Frontend"
            "5" -> "Python para Principiantes"
            "6" -> "JavaScript Avanzado"
            "7" -> "Guitarra Acústica"
            "8" -> "Piano Básico"
            "9" -> "Canto: Técnica Vocal"
            "10" -> "Dibujo Artístico"
            "11" -> "Edición de Video Básica"
            "12" -> "Marketing Digital"
            "13" -> "Ventas en Línea"
            else -> "Habilidad no encontrada"
        },
        description = "Encuentra mentores expertos que te ayudarán a desarrollar tus habilidades y alcanzar tus objetivos de aprendizaje.",
        category = SkillCategory.OTROS,
        imageUrl = "",
        mentorsCount = 5,
        learnersCount = 12
    )
} 