package com.ics.skillsync.ui.screens

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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
import java.text.SimpleDateFormat
import java.util.*
import com.ics.skillsync.ui.viewmodel.TeachingCardViewModel
import com.ics.skillsync.model.TeachingCard
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.DrawerState
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ics.skillsync.ui.viewmodel.SessionViewModel
import com.ics.skillsync.model.LearningSession
import com.ics.skillsync.model.SessionStatus
import kotlinx.coroutines.delay
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.ics.skillsync.model.Skill

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionsScreen(
    navController: NavController,
    viewModel: ProfileViewModel,
    teachingCardViewModel: TeachingCardViewModel = viewModel(),
    sessionViewModel: SessionViewModel = viewModel()
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val paddingValues = PaddingValues(16.dp)
    var showAddSessionDialog by remember { mutableStateOf(false) }
    var showSessionDetailsDialog by remember { mutableStateOf(false) }
    var selectedSession by remember { mutableStateOf<LearningSession?>(null) }
    var currentMonth by remember { mutableStateOf(Calendar.getInstance()) }
    var selectedDate by remember { mutableStateOf(Calendar.getInstance()) }
    val teachingCards by teachingCardViewModel.teachingCards.collectAsState()
    val sessions by sessionViewModel.sessions.collectAsState()
    val mentorNames by sessionViewModel.mentorNames.collectAsState()
    val uiState by sessionViewModel.uiState.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()

    // Cargar sesiones solo una vez al inicio
    LaunchedEffect(Unit) {
        sessionViewModel.loadUserSessions()
    }

    // Observar el estado de la UI
    LaunchedEffect(uiState) {
        when (val currentState = uiState) {
            is SessionViewModel.UiState.Success -> {
                snackbarHostState.showSnackbar(
                    message = currentState.message,
                    duration = SnackbarDuration.Short
                )
            }
            is SessionViewModel.UiState.Error -> {
                snackbarHostState.showSnackbar(
                    message = currentState.message,
                    duration = SnackbarDuration.Long
                )
            }
            else -> {}
        }
    }

    // Estados para el diálogo de nueva sesión
    var selectedSkill by remember { mutableStateOf<TeachingCard?>(null) }
    var selectedHour by remember { mutableStateOf("") }
    var selectedMinute by remember { mutableStateOf("") }
    var selectedDuration by remember { mutableStateOf("30") }

    // Estados para controlar los menús desplegables
    var expandedSkill by remember { mutableStateOf(false) }
    var expandedHour by remember { mutableStateOf(false) }
    var expandedMinute by remember { mutableStateOf(false) }
    var expandedDuration by remember { mutableStateOf(false) }

    // Listas para los desplegables
    val hours = (8..20).map { it.toString().padStart(2, '0') }
    val minutes = listOf("15", "30", "45", "00")
    val durations = listOf("30", "60", "120", "180")

    // Función para reiniciar los campos
    fun resetFields() {
        selectedSkill = null
        selectedHour = ""
        selectedMinute = ""
        selectedDuration = "30"
        expandedSkill = false
        expandedHour = false
        expandedMinute = false
        expandedDuration = false
    }

    // Función para guardar la sesión en Firebase
    fun saveSessionToFirebase(session: LearningSession) {
        val db = FirebaseFirestore.getInstance()
        val auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser

        if (currentUser != null) {
            // Primero obtener el nombre del aprendiz
            db.collection("users")
                .document(currentUser.uid)
                .get()
                .addOnSuccessListener { userDoc ->
                    if (userDoc != null && userDoc.exists()) {
                        val firstName = userDoc.getString("firstName") ?: ""
                        val lastName = userDoc.getString("lastName") ?: ""
                        val learnerName = "$firstName $lastName"

                        // Crear el documento de la sesión con el nombre del aprendiz
                        val sessionData = hashMapOf(
                            "title" to session.title,
                            "date" to session.date.time,
                            "duration" to session.duration,
                            "mentorId" to session.mentorId,
                            "learnerId" to currentUser.uid,
                            "learnerName" to learnerName,
                            "status" to session.status.name
                        )

                        db.collection("sessions")
                            .document(session.id)
                            .set(sessionData)
                            .addOnSuccessListener {
                                Toast.makeText(context, "Sesión creada correctamente", Toast.LENGTH_SHORT).show()
                                sessionViewModel.loadUserSessions()
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(context, "Error al crear la sesión: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(context, "Error al obtener datos del usuario: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    // Función para cancelar la sesión en Firebase
    fun cancelSession(session: LearningSession) {
        val sessionData = hashMapOf<String, Any>(
            "status" to SessionStatus.CANCELLED.name
        )

        db.collection("sessions")
            .document(session.id)
            .update(sessionData)
            .addOnSuccessListener {
                Toast.makeText(context, "Sesión cancelada correctamente", Toast.LENGTH_SHORT).show()
                sessionViewModel.loadUserSessions()
                showSessionDetailsDialog = false
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Error al cancelar la sesión: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // Función para borrar la sesión de Firebase
    fun deleteSession(session: LearningSession) {
        db.collection("sessions")
            .document(session.id)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(context, "Sesión eliminada correctamente", Toast.LENGTH_SHORT).show()
                sessionViewModel.loadUserSessions()
                showSessionDetailsDialog = false
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Error al eliminar la sesión: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // Modificar la función que muestra las sesiones del día
    val sessionsForSelectedDate = remember(sessions, selectedDate) {
        sessions.filter { session ->
            val sessionDate = Calendar.getInstance().apply {
                timeInMillis = session.date.time
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val selectedDateStart = Calendar.getInstance().apply {
                time = selectedDate.time
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            sessionDate.timeInMillis == selectedDateStart.timeInMillis
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
            floatingActionButton = {
                if (currentUser?.role != "Mentor") {
                    FloatingActionButton(
                        onClick = { showAddSessionDialog = true },
                        containerColor = Color(0xFF5B4DBC)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Agregar sesión",
                            tint = Color.White
                        )
                    }
                }
            },
            snackbarHost = { SnackbarHost(snackbarHostState) },
            containerColor = Color.White
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Calendario
                Text(
                    text = "Calendario",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color(0xFF5B4DBC),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                CalendarSection(
                    currentMonth = currentMonth,
                    selectedDate = selectedDate,
                    sessions = sessions,
                    onMonthChange = { currentMonth = it },
                    onDateSelected = { selectedDate = it }
                )

                // Sesiones del día seleccionado
                Text(
                    text = "Sesiones del ${SimpleDateFormat("d 'de' MMMM", Locale("es")).format(selectedDate.time)}",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color(0xFF5B4DBC),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                )
                SessionsSection(
                    date = selectedDate,
                    sessions = sessionsForSelectedDate,
                    mentorNames = mentorNames,
                    onSessionClick = { 
                        selectedSession = it
                        showSessionDetailsDialog = true
                    },
                    isMentor = currentUser?.role == "Mentor"
                )
            }
        }
    }

    // Diálogo para agregar nueva sesión
    if (showAddSessionDialog) {
        AlertDialog(
            onDismissRequest = { showAddSessionDialog = false },
            title = { Text("Nueva Sesión") },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Selector de habilidad
                    ExposedDropdownMenuBox(
                        expanded = expandedSkill,
                        onExpandedChange = { expandedSkill = it }
                    ) {
                        OutlinedTextField(
                            value = selectedSkill?.title ?: "",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Habilidad") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedSkill) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )

                        ExposedDropdownMenu(
                            expanded = expandedSkill,
                            onDismissRequest = { expandedSkill = false }
                        ) {
                            teachingCards.forEach { card ->
                                DropdownMenuItem(
                                    text = { Text(card.title) },
                                    onClick = {
                                        selectedSkill = card
                                        expandedSkill = false
                                        // Cargar el nombre del mentor cuando se selecciona una habilidad
                                        if (card.mentorId.isNotEmpty()) {
                                            db.collection("users")
                                                .document(card.mentorId)
                                                .get()
                                                .addOnSuccessListener { document ->
                                                    if (document != null && document.exists()) {
                                                        val firstName = document.getString("firstName") ?: ""
                                                        val lastName = document.getString("lastName") ?: ""
                                                        val mentorName = "$firstName $lastName"
                                                        val updatedMentorNames = mentorNames.toMutableMap()
                                                        updatedMentorNames[card.mentorId] = mentorName
                                                        sessionViewModel.updateMentorNames(updatedMentorNames)
                                                    }
                                                }
                                        }
                                    }
                                )
                            }
                        }
                    }

                    // Mostrar nombre del mentor si se ha seleccionado una habilidad
                    if (selectedSkill != null) {
                        val mentorName = mentorNames[selectedSkill?.mentorId] ?: "Cargando..."
                        Text(
                            text = "Mentor: $mentorName",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF5B4DBC),
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }

                    // Selector de hora
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ExposedDropdownMenuBox(
                            expanded = expandedHour,
                            onExpandedChange = { expandedHour = it },
                            modifier = Modifier.weight(1f)
                        ) {
                            OutlinedTextField(
                                value = selectedHour,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Hora") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedHour) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor()
                            )

                            ExposedDropdownMenu(
                                expanded = expandedHour,
                                onDismissRequest = { expandedHour = false }
                            ) {
                                hours.forEach { hour ->
                                    DropdownMenuItem(
                                        text = { Text(hour) },
                                        onClick = {
                                            selectedHour = hour
                                            expandedHour = false
                                        }
                                    )
                                }
                            }
                        }

                        ExposedDropdownMenuBox(
                            expanded = expandedMinute,
                            onExpandedChange = { expandedMinute = it },
                            modifier = Modifier.weight(1f)
                        ) {
                            OutlinedTextField(
                                value = selectedMinute,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Minutos", maxLines = 1) },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedMinute) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor()
                            )

                            ExposedDropdownMenu(
                                expanded = expandedMinute,
                                onDismissRequest = { expandedMinute = false }
                            ) {
                                minutes.forEach { minute ->
                                    DropdownMenuItem(
                                        text = { Text(minute) },
                                        onClick = {
                                            selectedMinute = minute
                                            expandedMinute = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Selector de duración
                    ExposedDropdownMenuBox(
                        expanded = expandedDuration,
                        onExpandedChange = { expandedDuration = it }
                    ) {
                        OutlinedTextField(
                            value = selectedDuration,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Duración (minutos)") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedDuration) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )

                        ExposedDropdownMenu(
                            expanded = expandedDuration,
                            onDismissRequest = { expandedDuration = false }
                        ) {
                            durations.forEach { duration ->
                                DropdownMenuItem(
                                    text = { Text(duration) },
                                    onClick = {
                                        selectedDuration = duration
                                        expandedDuration = false
                                    }
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (selectedSkill != null && selectedHour.isNotEmpty() && selectedMinute.isNotEmpty() && selectedDuration.isNotEmpty()) {
                            val calendar = Calendar.getInstance().apply {
                                time = selectedDate.time
                                set(Calendar.HOUR_OF_DAY, selectedHour.toInt())
                                set(Calendar.MINUTE, selectedMinute.toInt())
                            }
                            
                            val newSession = LearningSession(
                                id = UUID.randomUUID().toString(),
                                title = selectedSkill!!.title,
                                date = calendar.time,
                                duration = selectedDuration.toInt(),
                                mentorId = selectedSkill!!.mentorId,
                                status = SessionStatus.PENDING
                            )
                            saveSessionToFirebase(newSession)
                            showAddSessionDialog = false
                            resetFields()
                        }
                    },
                    enabled = selectedSkill != null && selectedHour.isNotEmpty() && selectedMinute.isNotEmpty() && selectedDuration.isNotEmpty()
                ) {
                    Text("Agregar")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { 
                        showAddSessionDialog = false
                        resetFields()
                    }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Diálogo de detalles de la sesión
    if (showSessionDetailsDialog && selectedSession != null) {
        val isMentor = currentUser?.role == "Mentor"
        SessionDetailsDialog(
            session = selectedSession!!,
            mentorName = mentorNames[selectedSession!!.mentorId] ?: "Mentor desconocido",
            isMentor = isMentor,
            onDismiss = { showSessionDetailsDialog = false },
            onCancel = {
                if (selectedSession!!.status == SessionStatus.CANCELLED) {
                    deleteSession(selectedSession!!)
                } else {
                    cancelSession(selectedSession!!)
                }
            },
            onAccept = {
                // Función para aceptar la sesión en Firebase
                val sessionData = hashMapOf<String, Any>(
                    "status" to SessionStatus.CONFIRMED.name
                )

                db.collection("sessions")
                    .document(selectedSession!!.id)
                    .update(sessionData)
                    .addOnSuccessListener {
                        Toast.makeText(context, "Sesión aceptada correctamente", Toast.LENGTH_SHORT).show()
                        sessionViewModel.loadUserSessions()
                        showSessionDetailsDialog = false
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(context, "Error al aceptar la sesión: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
        )
    }
}

@Composable
private fun CalendarSection(
    currentMonth: Calendar,
    selectedDate: Calendar,
    sessions: List<LearningSession>,
    onMonthChange: (Calendar) -> Unit,
    onDateSelected: (Calendar) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Encabezado del calendario
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { 
                        val newMonth = currentMonth.clone() as Calendar
                        newMonth.add(Calendar.MONTH, -1)
                        onMonthChange(newMonth)
                    },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Default.ChevronLeft,
                        contentDescription = "Mes anterior",
                        modifier = Modifier.size(20.dp)
                    )
                }
                
                Text(
                    text = "${currentMonth.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale("es"))} ${currentMonth.get(Calendar.YEAR)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                IconButton(
                    onClick = { 
                        val newMonth = currentMonth.clone() as Calendar
                        newMonth.add(Calendar.MONTH, 1)
                        onMonthChange(newMonth)
                    },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Default.ChevronRight,
                        contentDescription = "Mes siguiente",
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Días de la semana
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                listOf("Lun", "Mar", "Mié", "Jue", "Vie", "Sáb", "Dom").forEach { day ->
                    Text(
                        text = day,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Días del mes
            val firstDayOfMonth = currentMonth.clone() as Calendar
            firstDayOfMonth.set(Calendar.DAY_OF_MONTH, 1)
            val lastDayOfMonth = currentMonth.clone() as Calendar
            lastDayOfMonth.set(Calendar.DAY_OF_MONTH, lastDayOfMonth.getActualMaximum(Calendar.DAY_OF_MONTH))
            
            // Ajustar para que la semana comience en lunes (2) en lugar de domingo (1)
            val firstDayOfWeek = (firstDayOfMonth.get(Calendar.DAY_OF_WEEK) + 5) % 7 + 1
            val daysInMonth = lastDayOfMonth.get(Calendar.DAY_OF_MONTH)

            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                var currentDay = 1
                val weeks = (daysInMonth + firstDayOfWeek - 1) / 7 + 1

                repeat(weeks) { week ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        repeat(7) { dayOfWeek ->
                            if (week == 0 && dayOfWeek < firstDayOfWeek - 1) {
                                Box(modifier = Modifier.weight(1f))
                            } else if (currentDay <= daysInMonth) {
                                val date = currentMonth.clone() as Calendar
                                date.set(Calendar.DAY_OF_MONTH, currentDay)
                                val hasSession = sessions.any { 
                                    val sessionDate = Calendar.getInstance().apply { time = it.date }
                                    sessionDate.isSameDay(date)
                                }
                                
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(1f)
                                        .padding(2.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Surface(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(1.dp),
                                        shape = CircleShape,
                                        color = when {
                                            date.isSameDay(selectedDate) -> Color(0xFF5B4DBC)
                                            hasSession -> Color(0xFF5B4DBC).copy(alpha = 0.1f)
                                            else -> Color.Transparent
                                        },
                                        onClick = { onDateSelected(date) }
                                    ) {
                                        Box(
                                            modifier = Modifier.fillMaxSize(),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = currentDay.toString(),
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = when {
                                                    date.isSameDay(selectedDate) -> Color.White
                                                    hasSession -> Color(0xFF5B4DBC)
                                                    else -> Color.Black
                                                }
                                            )
                                        }
                                    }
                                }
                                currentDay++
                            } else {
                                Box(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SessionsSection(
    date: Calendar,
    sessions: List<LearningSession>,
    mentorNames: Map<String, String>,
    onSessionClick: (LearningSession) -> Unit,
    isMentor: Boolean
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (sessions.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No hay sesiones programadas para este día",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            sessions.forEach { session ->
                SessionCard(
                    session = session,
                    userName = if (isMentor) {
                        mentorNames[session.learnerId] ?: "Aprendiz desconocido"
                    } else {
                        mentorNames[session.mentorId] ?: "Mentor desconocido"
                    },
                    isMentor = isMentor,
                    onClick = { onSessionClick(session) }
                )
            }
        }
    }
}

@Composable
private fun SessionCard(
    session: LearningSession,
    userName: String,
    isMentor: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = session.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                StatusChip(status = session.status)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                SessionInfoItem(
                    icon = Icons.Default.Schedule,
                    text = "${SimpleDateFormat("HH:mm", Locale("es")).format(session.date)} (${session.duration} min)"
                )
                SessionInfoItem(
                    icon = Icons.Default.Person,
                    text = if (isMentor) "Aprendiz: $userName" else "Mentor: $userName"
                )
            }
        }
    }
}

@Composable
private fun SessionInfoItem(
    icon: ImageVector,
    text: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = Color.Gray
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )
    }
}

@Composable
private fun StatusChip(status: SessionStatus) {
    val (color, text) = when (status) {
        SessionStatus.PENDING -> Pair(Color(0xFF5B4DBC), "Pendiente")
        SessionStatus.CONFIRMED -> Pair(Color(0xFF2196F3), "Confirmada")
        SessionStatus.COMPLETED -> Pair(Color.Green, "Completada")
        SessionStatus.CANCELLED -> Pair(Color.Red, "Cancelada")
    }

    Surface(
        modifier = Modifier.padding(vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        color = color.copy(alpha = 0.1f)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            color = color,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun SessionDetailsDialog(
    session: LearningSession,
    mentorName: String,
    isMentor: Boolean,
    onDismiss: () -> Unit,
    onCancel: () -> Unit,
    onAccept: () -> Unit
) {
    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()
    var userName by remember { mutableStateOf("Cargando...") }
    var isLoading by remember { mutableStateOf(true) }

    // Cargar el nombre del usuario correspondiente
    LaunchedEffect(session.learnerId, session.mentorId) {
        try {
            // Si es mentor, cargar datos del aprendiz
            // Si es aprendiz, cargar datos del mentor
            val userId = if (isMentor) session.learnerId else session.mentorId
            
            if (userId.isNotEmpty()) {
                db.collection("users")
                    .document(userId)
                    .get()
                    .addOnSuccessListener { document ->
                        if (document != null && document.exists()) {
                            val firstName = document.getString("firstName") ?: ""
                            val lastName = document.getString("lastName") ?: ""
                            userName = "$firstName $lastName"
                        } else {
                            userName = if (isMentor) "Aprendiz desconocido" else "Mentor desconocido"
                        }
                        isLoading = false
                    }
                    .addOnFailureListener {
                        userName = if (isMentor) "Aprendiz desconocido" else "Mentor desconocido"
                        isLoading = false
                    }
            } else {
                userName = if (isMentor) "Aprendiz desconocido" else "Mentor desconocido"
                isLoading = false
            }
        } catch (e: Exception) {
            userName = if (isMentor) "Aprendiz desconocido" else "Mentor desconocido"
            isLoading = false
        }
    }

    if (isLoading) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Cargando...") },
            text = {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            },
            confirmButton = {
                TextButton(onClick = onDismiss) {
                    Text("Cerrar")
                }
            }
        )
    } else {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text(session.title) },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Fecha: ${SimpleDateFormat("d 'de' MMMM", Locale("es")).format(session.date)}")
                    Text("Hora: ${SimpleDateFormat("HH:mm", Locale("es")).format(session.date)}")
                    Text("Duración: ${session.duration} minutos")
                    Text(if (isMentor) "Aprendiz: $userName" else "Mentor: $userName")
                    StatusChip(status = session.status)
                }
            },
            confirmButton = {
                TextButton(onClick = onDismiss) {
                    Text("Cerrar")
                }
            },
            dismissButton = {
                if (isMentor) {
                    when (session.status) {
                        SessionStatus.PENDING -> {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                TextButton(
                                    onClick = onAccept,
                                    colors = ButtonDefaults.textButtonColors(
                                        contentColor = Color(0xFF2196F3)
                                    )
                                ) {
                                    Text("Aceptar")
                                }
                                TextButton(
                                    onClick = onCancel,
                                    colors = ButtonDefaults.textButtonColors(
                                        contentColor = Color.Red
                                    )
                                ) {
                                    Text("Rechazar")
                                }
                            }
                        }
                        SessionStatus.CONFIRMED -> {
                            TextButton(
                                onClick = onCancel,
                                colors = ButtonDefaults.textButtonColors(
                                    contentColor = Color.Red
                                )
                            ) {
                                Text("Cancelar sesión")
                            }
                        }
                        SessionStatus.CANCELLED -> {
                            TextButton(
                                onClick = onCancel,
                                colors = ButtonDefaults.textButtonColors(
                                    contentColor = Color.Red
                                )
                            ) {
                                Text("Borrar sesión")
                            }
                        }
                        else -> {}
                    }
                } else {
                    TextButton(
                        onClick = onCancel,
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = Color.Red
                        )
                    ) {
                        Text(if (session.status == SessionStatus.CANCELLED) "Borrar sesión" else "Cancelar sesión")
                    }
                }
            }
        )
    }
}

private fun Calendar.isSameDay(other: Calendar): Boolean {
    return this.get(Calendar.YEAR) == other.get(Calendar.YEAR) &&
            this.get(Calendar.MONTH) == other.get(Calendar.MONTH) &&
            this.get(Calendar.DAY_OF_MONTH) == other.get(Calendar.DAY_OF_MONTH)
} 