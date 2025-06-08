package com.ics.skillsync.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.ics.skillsync.model.TeachingCard
import com.ics.skillsync.ui.viewmodel.TeachingCardViewModel
import com.ics.skillsync.ui.components.SharedTopBar
import com.ics.skillsync.ui.components.SharedBottomBar
import com.ics.skillsync.ui.components.SharedNavigationDrawer
import com.ics.skillsync.ui.viewmodel.ProfileViewModel
import kotlinx.coroutines.launch
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import com.ics.skillsync.ui.screens.TabButton
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.DrawerState
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ics.skillsync.model.Skill
import com.ics.skillsync.ui.viewmodel.SkillViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import androidx.compose.runtime.collectAsState
import com.ics.skillsync.ui.viewmodel.EnrollmentViewModel
import com.ics.skillsync.ui.viewmodel.ChatViewModel
import kotlinx.coroutines.CoroutineScope

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SkillDetailScreen(
    cardId: String,
    navController: NavController,
    teachingCardViewModel: TeachingCardViewModel,
    profileViewModel: ProfileViewModel = viewModel(),
    skillViewModel: SkillViewModel = viewModel(),
    enrollmentViewModel: EnrollmentViewModel = viewModel(),
    chatViewModel: ChatViewModel = viewModel()
) {
    var card by remember { mutableStateOf<TeachingCard?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    var topBarTitle by remember { mutableStateOf("Detalle de habilidad") }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    var mentorSkills by remember {
        mutableStateOf<List<com.ics.skillsync.data.database.entity.Skill>>(
            emptyList()
        )
    }
    var mentorBio by remember { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }

    // Obtener el rol del usuario actual
    val currentUser by profileViewModel.currentUser.collectAsState()
    val userRole = currentUser?.role ?: ""
    val canEnroll = userRole == "Aprendiz" || userRole == "Ambos roles"

    // Observar el estado de la inscripción
    val enrollmentState by enrollmentViewModel.uiState.collectAsState()
    val isEnrolled by enrollmentViewModel.isEnrolled.collectAsState()

    // Verificar el estado de inscripción al cargar
    LaunchedEffect(cardId) {
        enrollmentViewModel.checkEnrollmentStatus(cardId)
    }

    // Manejar el estado de la inscripción
    LaunchedEffect(enrollmentState) {
        when (enrollmentState) {
            is EnrollmentViewModel.UiState.Success -> {
                snackbarHostState.showSnackbar(
                    message = (enrollmentState as EnrollmentViewModel.UiState.Success).message,
                    duration = SnackbarDuration.Short
                )
                // Recargar la tarjeta para actualizar el contador de aprendices
                try {
                    val updatedCard = teachingCardViewModel.loadTeachingCard(cardId)
                    if (updatedCard != null) {
                        card = updatedCard
                    }
                } catch (e: Exception) {
                    snackbarHostState.showSnackbar(
                        message = "Error al actualizar la información: ${e.message}",
                        duration = SnackbarDuration.Short
                    )
                }
                enrollmentViewModel.clearUiState()
            }

            is EnrollmentViewModel.UiState.Error -> {
                snackbarHostState.showSnackbar(
                    message = (enrollmentState as EnrollmentViewModel.UiState.Error).message,
                    duration = SnackbarDuration.Short
                )
                enrollmentViewModel.clearUiState()
            }

            is EnrollmentViewModel.UiState.Loading -> {
                // El estado de carga se maneja en el botón
            }

            else -> {}
        }
    }

    // Tabs: 0 = Mentor, 1 = Aprendices
    var selectedTab by remember { mutableStateOf(0) }
    val tabTitles = listOf("Mentor", "Aprendices")

    LaunchedEffect(cardId) {
        isLoading = true
        error = null
        try {
            card = teachingCardViewModel.loadTeachingCard(cardId)
            // Cargar las habilidades del mentor
            if (card != null) {
                mentorSkills = skillViewModel.loadMentorSkills(card!!.mentorId)
                // Obtener la biografía actualizada del mentor desde Firebase
                val mentorDoc = FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(card!!.mentorId)
                    .get()
                    .await()

                mentorBio = mentorDoc.getString("biography") ?: ""
            }
        } catch (e: Exception) {
            error = "Error al cargar la tarjeta: ${e.message}"
        }
        isLoading = false
        topBarTitle = card?.title ?: "Detalle de habilidad"
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
                    title = "SkillSync",
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

                    error != null -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(error ?: "Error desconocido", color = Color.Red)
                        }
                    }

                    card != null -> {
                        val c = card!!
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .padding(bottom = 10.dp)
                        ) {
                            // Título y categoría
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 20.dp, vertical = 16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = c.title,
                                    fontSize = 26.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.weight(1f)
                                )
                                Surface(
                                    color = getCategoryColor(c.category),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = c.category,
                                        color = Color.White,
                                        fontSize = 13.sp,
                                        modifier = Modifier.padding(
                                            horizontal = 10.dp,
                                            vertical = 4.dp
                                        )
                                    )
                                }
                            }
                            // Descripción
                            Text(
                                text = c.description,
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color(0xFF444444),
                                modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
                            )
                            // Imagen
                            if (c.imageUrl.isNotEmpty()) {
                                AsyncImage(
                                    model = c.imageUrl,
                                    contentDescription = c.title,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(220.dp)
                                        .padding(horizontal = 20.dp, vertical = 16.dp)
                                        .clip(RoundedCornerShape(16.dp)),
                                    contentScale = ContentScale.Crop
                                )
                            }
                            // Estadísticas
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 20.dp, vertical = 4.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                StatCardWeb(
                                    icon = Icons.Default.Person,
                                    value = c.learnerCount.toString(),
                                    label = "Aprendices",
                                    colorBg = Color(0xFFD1FAE5),
                                    iconColor = Color(0xFF059669)
                                )
                                StatCardWeb(
                                    icon = Icons.Default.School,
                                    value = c.experienceLevel.getDisplayName().ifEmpty { "N/A" },
                                    label = "Dificultad promedio",
                                    colorBg = Color(0xFFFFF9C4),
                                    iconColor = Color(0xFFF59E42)
                                )

                                // Nueva StatCardWeb para inscripción, solo visible para aprendices
                                if (canEnroll) {
                                    StatCardWeb(
                                        icon = if (isEnrolled) Icons.Default.ExitToApp else Icons.Default.Add,
                                        value = "",  // Valor vacío porque mostraremos un botón
                                        label = if (isEnrolled) "Inscrito" else "Inscríbete",
                                        colorBg = if (isEnrolled) Color(0xFFFFEBEE) else Color(
                                            0xFFE0E7FF
                                        ),
                                        iconColor = if (isEnrolled) Color(0xFFE53935) else Color(
                                            0xFF5B4DBC
                                        ),
                                        customContent = {
                                            Button(
                                                onClick = {
                                                    scope.launch {
                                                        if (isEnrolled) {
                                                            enrollmentViewModel.unenrollFromTeachingCard(
                                                                cardId
                                                            )
                                                        } else {
                                                            enrollmentViewModel.enrollInTeachingCard(
                                                                cardId
                                                            )
                                                        }
                                                    }
                                                },
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = if (isEnrolled) Color(
                                                        0xFFE53935
                                                    ) else Color(0xFF5B4DBC)
                                                ),
                                                modifier = Modifier
                                                    .fillMaxWidth(),
                                                enabled = enrollmentState !is EnrollmentViewModel.UiState.Loading
                                            ) {
                                                if (enrollmentState is EnrollmentViewModel.UiState.Loading) {
                                                    CircularProgressIndicator(
                                                        modifier = Modifier.size(20.dp),
                                                        color = Color.White
                                                    )
                                                } else {
                                                    Text(
                                                        if (isEnrolled) "Dejar curso" else "Inscribirse",
                                                        fontSize = 14.sp
                                                    )
                                                }
                                            }
                                        }
                                    )
                                }
                            }
                            // Título Comunidad
                            Text(
                                text = "Comunidad",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = Color(0xFF111827),
                                modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)
                            )
                            // Tabs de comunidad (Mentores/Aprendices)
                            CommunityTabs(
                                selectedTab = selectedTab,
                                onTabSelected = { selectedTab = it }
                            )
                            // Contenido de tabs
                            when (selectedTab) {
                                0 -> MentorCommunityCard(
                                    card = c,
                                    mentorSkills = mentorSkills,
                                    currentUser = currentUser,
                                    snackbarHostState = snackbarHostState,
                                    scope = scope,
                                    chatViewModel = chatViewModel,
                                    navController = navController
                                )

                                1 -> LearnerCommunityCard(
                                    card = c,
                                    enrollmentViewModel = enrollmentViewModel
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatCardWeb(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: String,
    label: String,
    colorBg: Color,
    iconColor: Color,
    customContent: @Composable (() -> Unit)? = null
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(
                BorderStroke(1.dp, Color(0xFFE5E7EB)),
                RoundedCornerShape(16.dp)
            ),
        color = colorBg
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color(0xFF374151)
                )
            }
            if (customContent != null) {
                customContent()
            } else {
                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color(0xFF111827),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun CommunityTabs(selectedTab: Int, onTabSelected: (Int) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        TabWeb(
            text = "Mentor",
            selected = selectedTab == 0,
            onClick = { onTabSelected(0) },
            left = true,
            modifier = Modifier.weight(1f)
        )
        TabWeb(
            text = "Aprendices",
            selected = selectedTab == 1,
            onClick = { onTabSelected(1) },
            left = false,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun TabWeb(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    left: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .height(40.dp),
        color = if (selected) Color.White else Color(0xFFF3F4F6),
        border = BorderStroke(1.dp, if (selected) Color(0xFF5B4DBC) else Color(0xFFF3F4F6)),
        shape = if (left) RoundedCornerShape(
            topStart = 8.dp,
            bottomStart = 8.dp
        ) else RoundedCornerShape(topEnd = 8.dp, bottomEnd = 8.dp),
        shadowElevation = if (selected) 2.dp else 0.dp
    ) {
        TextButton(
            onClick = onClick,
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                text = text,
                fontSize = 15.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                color = if (selected) Color(0xFF5B4DBC) else Color(0xFF6B7280)
            )
        }
    }
}

@Composable
private fun MentorCommunityCard(
    card: TeachingCard,
    mentorSkills: List<com.ics.skillsync.data.database.entity.Skill>,
    currentUser: com.ics.skillsync.data.database.entity.User?,
    snackbarHostState: SnackbarHostState,
    scope: CoroutineScope,
    chatViewModel: ChatViewModel,
    navController: NavController
) {
    var mentorBio by remember { mutableStateOf("") }
    var mentorPhotoUrl by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }

    // Efecto para cargar la biografía y foto del mentor
    LaunchedEffect(card.mentorId) {
        isLoading = true
        try {
            val mentorDoc = FirebaseFirestore.getInstance()
                .collection("users")
                .document(card.mentorId)
                .get()
                .await()

            mentorBio = mentorDoc.getString("biography") ?: ""
            mentorPhotoUrl = mentorDoc.getString("photoUrl") ?: ""
        } catch (e: Exception) {
            // Manejar el error silenciosamente y usar los valores de la tarjeta como respaldo
            mentorBio = card.mentorBio
            mentorPhotoUrl = card.mentorPhotoUrl
        }
        isLoading = false
    }

    if (isLoading) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = Color(0xFF5B4DBC))
        }
    } else {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .wrapContentHeight()
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (mentorPhotoUrl.isNotEmpty()) {
                        AsyncImage(
                            model = mentorPhotoUrl,
                            contentDescription = "Foto del mentor",
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Surface(
                            modifier = Modifier.size(48.dp),
                            shape = CircleShape,
                            color = Color(0xFFE0E7FF)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                modifier = Modifier.padding(12.dp),
                                tint = Color(0xFF5B4DBC)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = card.mentorName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = Color(0xFF111827)
                        )
                        Row {
                            BadgeWeb("Mentor")
                            Spacer(modifier = Modifier.width(6.dp))
                            if (mentorSkills.isNotEmpty()) {
                                BadgeWeb(mentorSkills.first().name)
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = if (mentorBio.isNotBlank()) mentorBio else "Este mentor aún no ha agregado una biografía.",
                    color = Color(0xFF444444),
                    fontSize = 15.sp
                )
                Spacer(modifier = Modifier.height(10.dp))
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                if (currentUser?.id == card.mentorId) {
                                    scope.launch {
                                        snackbarHostState.showSnackbar(
                                            message = "No puedes enviarte mensajes a ti mismo",
                                            duration = SnackbarDuration.Short
                                        )
                                    }
                                } else {
                                    scope.launch {
                                        try {
                                            val chatId =
                                                chatViewModel.getOrCreateChat(card.mentorId)
                                            navController.navigate("chat/$chatId")
                                        } catch (e: Exception) {
                                            snackbarHostState.showSnackbar(
                                                message = "Error al iniciar el chat: ${e.message}",
                                                duration = SnackbarDuration.Short
                                            )
                                        }
                                    }
                                }
                            },
                            modifier = Modifier
                                .widthIn(max = 260.dp)
                                .padding(top = 8.dp),
                            border = BorderStroke(1.dp, Color(0xFF5B4DBC)),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color(
                                    0xFF5B4DBC
                                )
                            )
                        ) {
                            Icon(Icons.Default.Message, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Mensaje")
                        }

                        Button(
                            onClick = { navController.navigate("sessions") },
                            modifier = Modifier
                                .widthIn(max = 260.dp)
                                .padding(top = 8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5B4DBC))
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

@Composable
private fun LearnerCommunityCard(
    card: TeachingCard,
    enrollmentViewModel: EnrollmentViewModel
) {
    val learners by enrollmentViewModel.learners.collectAsState()
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(card.id) {
        isLoading = true
        enrollmentViewModel.loadLearners(card.id)
        isLoading = false
    }

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color(0xFF5B4DBC))
            }
        } else if (learners.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Aún no hay aprendices inscritos en este curso",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            learners.forEach { learner ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (learner.userPhotoUrl.isNotEmpty()) {
                                AsyncImage(
                                    model = learner.userPhotoUrl,
                                    contentDescription = "Foto del aprendiz",
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Surface(
                                    modifier = Modifier.size(48.dp),
                                    shape = CircleShape,
                                    color = Color(0xFFE0E7FF)
                                ) {
                                    val initials = learner.userName
                                        .split(" ")
                                        .filter { it.isNotBlank() }
                                        .take(2)
                                        .map { it.firstOrNull()?.toString() ?: "" }
                                        .joinToString("")
                                        .ifBlank { "?" }
                                    Text(
                                        text = initials,
                                        color = Color(0xFF5B4DBC),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp,
                                        modifier = Modifier.padding(12.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = learner.userName,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 17.sp,
                                    color = Color(0xFF111827)
                                )
                                Row {
                                    BadgeWeb("Aprendiz")
                                    Spacer(modifier = Modifier.width(6.dp))
                                    BadgeWeb(
                                        text = "Inscrito el ${formatDate(learner.enrollmentDate)}",
                                        backgroundColor = Color(0xFFE8F5E9),
                                        textColor = Color(0xFF2E7D32)
                                    )
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
private fun BadgeWeb(
    text: String,
    backgroundColor: Color = Color(0xFFF3F4F6),
    textColor: Color = Color(0xFF5B4DBC)
) {
    Surface(
        color = backgroundColor,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, Color(0xFFE5E7EB))
    ) {
        Text(
            text = text,
            color = textColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
        )
    }
}

private fun formatDate(timestamp: Long): String {
    val date = java.util.Date(timestamp)
    val format = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale("es", "ES"))
    return format.format(date)
}

private fun getCategoryColor(category: String): Color {
    return when (category) {
        "Tecnología" -> Color(0xFF2196F3)
        "Diseño" -> Color(0xFFC8419D)
        "Marketing" -> Color(0xFFFFCD00)
        "Idiomas" -> Color(0xFF4CAF50)
        "Arte" -> Color(0xFFE91E63)
        "Música" -> Color(0xFFF44336)
        "Gastronomía" -> Color(0xFFFF9800)
        "Deportes" -> Color(0xFFD8201D)
        "Ciencias" -> Color(0xFF48C21D)
        "Humanidades" -> Color(0xFF795548)
        "Finanzas" -> Color(0xFF009688)
        "Derecho" -> Color(0xFF6C3B2A)
        "Salud" -> Color(0xFF607D8B)
        "Educación" -> Color(0xFF1D4DD8)
        else -> Color(0xFF9C27B0)
    }
} 