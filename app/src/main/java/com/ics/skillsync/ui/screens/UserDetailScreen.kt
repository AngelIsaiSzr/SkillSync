package com.ics.skillsync.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.ics.skillsync.R
import androidx.compose.foundation.BorderStroke
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ics.skillsync.ui.viewmodel.ProfileViewModel
import androidx.compose.foundation.border
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarDuration
import com.ics.skillsync.ui.components.SharedNavigationDrawer
import com.ics.skillsync.ui.components.SharedTopBar
import com.ics.skillsync.ui.components.SharedBottomBar
import com.ics.skillsync.ui.components.SkillCard
import com.ics.skillsync.model.Skill
import com.ics.skillsync.model.SkillCategory
import kotlinx.coroutines.launch
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.rememberDrawerState
import com.ics.skillsync.ui.viewmodel.ChatViewModel
import com.ics.skillsync.ui.viewmodel.SkillViewModel
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import com.ics.skillsync.model.TeachingCard
import com.ics.skillsync.ui.viewmodel.TeachingCardViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserDetailScreen(
    userId: String,
    navController: NavController,
    profileViewModel: ProfileViewModel = viewModel(),
    chatViewModel: ChatViewModel = viewModel(),
    skillViewModel: SkillViewModel = viewModel(),
    teachingCardViewModel: TeachingCardViewModel = viewModel()
) {
    val uiState by profileViewModel.uiState.collectAsState()
    val currentUser by profileViewModel.currentUser.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    
    // Estado para el usuario que estamos viendo
    var user by remember { mutableStateOf<com.ics.skillsync.data.database.entity.User?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var userSkills by remember { mutableStateOf<List<com.ics.skillsync.data.database.entity.Skill>>(emptyList()) }
    var teachingCards by remember { mutableStateOf<List<TeachingCard>>(emptyList()) }

    // Cargar datos del usuario
    LaunchedEffect(userId) {
        isLoading = true
        try {
            // Obtener usuario de Firestore
            val userDoc = FirebaseFirestore.getInstance()
                .collection("users")
                .document(userId)
                .get()
                .await()

            if (userDoc.exists()) {
                val userData = userDoc.data
                user = com.ics.skillsync.data.database.entity.User(
                    id = userId,
                    firstName = userData?.get("firstName") as? String ?: "",
                    lastName = userData?.get("lastName") as? String ?: "",
                    username = userData?.get("username") as? String ?: "",
                    email = userData?.get("email") as? String ?: "",
                    password = "", // No necesitamos la contraseña
                    role = userData?.get("role") as? String ?: "Ambos roles",
                    photoUrl = userData?.get("photoUrl") as? String ?: "",
                    biography = userData?.get("biography") as? String ?: "",
                    availability = userData?.get("availability") as? String ?: ""
                )
            }

            // Cargar habilidades del usuario
            val skillsSnapshot = FirebaseFirestore.getInstance()
                .collection("users")
                .document(userId)
                .collection("skills")
                .get()
                .await()

            userSkills = skillsSnapshot.documents.mapNotNull { doc ->
                try {
                    com.ics.skillsync.data.database.entity.Skill(
                        id = doc.id,
                        userId = userId,
                        name = doc.getString("name") ?: "",
                        type = com.ics.skillsync.data.database.entity.Skill.SkillType.valueOf(doc.getString("type") ?: "TEACH"),
                        level = doc.getLong("level")?.toInt() ?: 1
                    )
                } catch (e: Exception) {
                    null
                }
            }

            // Cargar tarjetas de enseñanza si el usuario es mentor
            if (user?.role == "Mentor" || user?.role == "Ambos roles") {
                val cardsSnapshot = FirebaseFirestore.getInstance()
                    .collection("teaching_cards")
                    .whereEqualTo("mentorId", userId)
                    .whereEqualTo("isActive", true)
                    .get()
                    .await()

                teachingCards = cardsSnapshot.documents.mapNotNull { doc ->
                    try {
                        TeachingCard.fromMap(doc.data?.plus(mapOf("id" to doc.id)) ?: emptyMap())
                    } catch (e: Exception) {
                        null
                    }
                }
            }
        } catch (e: Exception) {
            snackbarHostState.showSnackbar(
                message = "Error al cargar los datos del usuario: ${e.message}",
                duration = SnackbarDuration.Short
            )
        }
        isLoading = false
    }

    SharedNavigationDrawer(
        navController = navController,
        viewModel = profileViewModel,
        drawerState = drawerState
    ) {
        Scaffold(
            topBar = {
                SharedTopBar(
                    navController = navController,
                    viewModel = profileViewModel,
                    title = "Perfil de Usuario",
                    onDrawerOpen = { scope.launch { drawerState.open() } }
                )
            },
            bottomBar = {
                SharedBottomBar(navController = navController)
            },
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White)
                    .padding(paddingValues)
            ) {
                when {
                    isLoading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = Color(0xFF5B4DBC))
                        }
                    }
                    user != null -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(scrollState)
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Foto de perfil
                            Box(
                                modifier = Modifier
                                    .size(120.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFF3F4F6))
                                    .border(2.dp, Color(0xFF5B4DBC), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                when {
                                    user?.photoUrl.isNullOrEmpty() -> {
                                        Icon(
                                            imageVector = Icons.Default.Person,
                                            contentDescription = "Foto de perfil por defecto",
                                            modifier = Modifier.size(60.dp),
                                            tint = Color(0xFF5B4DBC)
                                        )
                                    }
                                    else -> {
                                        AsyncImage(
                                            model = user?.photoUrl,
                                            contentDescription = "Foto de perfil",
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Nombre y rol del usuario
                            Text(
                                text = "${user?.firstName} ${user?.lastName}",
                                style = MaterialTheme.typography.headlineMedium,
                                color = Color(0xFF111827),
                                textAlign = TextAlign.Center
                            )

                            Text(
                                text = user?.role ?: "Miembro",
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color(0xFF6B7280),
                                modifier = Modifier.padding(top = 4.dp)
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            // Tarjeta de información personal
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, Color(0xFFE5E7EB))
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp)
                                ) {
                                    Text(
                                        text = "Información Personal",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = Color(0xFF111827),
                                        fontWeight = FontWeight.Bold
                                    )

                                    Spacer(modifier = Modifier.height(16.dp))

                                    InfoField(
                                        icon = Icons.Default.Email,
                                        label = "Correo electrónico",
                                        value = user?.email ?: "No disponible"
                                    )

                                    Spacer(modifier = Modifier.height(12.dp))

                                    InfoField(
                                        icon = Icons.Default.Person,
                                        label = "Nombre de usuario",
                                        value = user?.username ?: "No disponible"
                                    )

                                    Spacer(modifier = Modifier.height(12.dp))

                                    InfoField(
                                        icon = Icons.Default.Description,
                                        label = "Biografía",
                                        value = user?.biography?.ifEmpty { "No especificada" } ?: "No especificada"
                                    )

                                    Spacer(modifier = Modifier.height(12.dp))

                                    InfoField(
                                        icon = Icons.Default.Schedule,
                                        label = "Disponibilidad",
                                        value = user?.availability?.ifEmpty { "No especificada" } ?: "No especificada"
                                    )
                                }
                            }

                            val hasSkillsOrTeachingCards = userSkills.isNotEmpty() ||
                                    ((user?.role == "Mentor" || user?.role == "Ambos roles") && teachingCards.isNotEmpty())

                            Spacer(modifier = Modifier.height(if (hasSkillsOrTeachingCards) 24.dp else 4.dp))

                            // Tarjeta de habilidades
                            if (userSkills.isNotEmpty()) {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, Color(0xFFE5E7EB))
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp)
                                    ) {
                                        Text(
                                            text = "Habilidades",
                                            style = MaterialTheme.typography.titleMedium,
                                            color = Color(0xFF111827),
                                            fontWeight = FontWeight.Bold
                                        )

                                        Spacer(modifier = Modifier.height(16.dp))

                                        userSkills.forEach { skill ->
                                            SkillItem(skill = skill)
                                            Spacer(modifier = Modifier.height(8.dp))
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            // Tarjeta de habilidades que enseña (solo para mentores)
                            if ((user?.role == "Mentor" || user?.role == "Ambos roles") && teachingCards.isNotEmpty()) {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, Color(0xFFE5E7EB))
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp)
                                    ) {
                                        Text(
                                            text = "Habilidades que enseña",
                                            style = MaterialTheme.typography.titleMedium,
                                            color = Color(0xFF111827),
                                            fontWeight = FontWeight.Bold
                                        )

                                        Spacer(modifier = Modifier.height(16.dp))

                                        teachingCards.forEach { card ->
                                            SkillCard(
                                                skill = Skill(
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
                                                ),
                                                navController = navController,
                                                modifier = Modifier.padding(bottom = 8.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            // Botones de acción
                            if (currentUser?.id != userId) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = {
                                            scope.launch {
                                                try {
                                                    val chatId = chatViewModel.getOrCreateChat(userId)
                                                    navController.navigate("chat/$chatId")
                                                } catch (e: Exception) {
                                                    snackbarHostState.showSnackbar(
                                                        message = "Error al iniciar el chat: ${e.message}",
                                                        duration = SnackbarDuration.Short
                                                    )
                                                }
                                            }
                                        },
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(48.dp),
                                        border = BorderStroke(1.dp, Color(0xFF5B4DBC)),
                                        colors = ButtonDefaults.outlinedButtonColors(
                                            contentColor = Color(0xFF5B4DBC)
                                        )
                                    ) {
                                        Icon(Icons.Default.Message, contentDescription = null)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Mensaje")
                                    }

                                    if (user?.role == "Mentor" || user?.role == "Ambos roles") {
                                        Button(
                                            onClick = { /* TODO: Programar */ },
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(48.dp),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = Color(0xFF5B4DBC)
                                            )
                                        ) {
                                            Icon(Icons.Default.CalendarMonth, contentDescription = null)
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Programar")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoField(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color(0xFF5B4DBC),
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF6B7280)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                color = Color(0xFF111827)
            )
        }
    }
}

@Composable
private fun SkillItem(skill: com.ics.skillsync.data.database.entity.Skill) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp)),
        color = Color(0xFFF3F4F6)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.School,
                contentDescription = null,
                tint = Color(0xFF5B4DBC),
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = skill.name,
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color(0xFF111827),
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = when (skill.level) {
                        1 -> "Nivel: Principiante"
                        2 -> "Nivel: Básico"
                        3 -> "Nivel: Intermedio"
                        4 -> "Nivel: Avanzado"
                        5 -> "Nivel: Experto"
                        else -> "Nivel: No especificado"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF6B7280)
                )
            }
        }
    }
}

@Composable
private fun SkillCard(
    skill: Skill,
    navController: NavController,
    modifier: Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp)),
        color = Color(0xFFF3F4F6)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.School,
                contentDescription = null,
                tint = Color(0xFF5B4DBC),
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
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