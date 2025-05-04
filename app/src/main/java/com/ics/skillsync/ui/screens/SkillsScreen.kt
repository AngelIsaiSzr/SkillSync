package com.ics.skillsync.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ics.skillsync.data.database.entity.Skill
import com.ics.skillsync.ui.viewmodel.SkillViewModel
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Delete
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.ui.text.style.TextAlign
import androidx.navigation.NavController
import com.ics.skillsync.ui.components.SharedBottomBar
import com.ics.skillsync.ui.components.SharedNavigationDrawer
import com.ics.skillsync.ui.components.SharedTopBar
import com.ics.skillsync.ui.viewmodel.ProfileViewModel
import kotlinx.coroutines.launch
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SkillsScreen(
    navController: NavController,
    skillViewModel: SkillViewModel = viewModel(),
    profileViewModel: ProfileViewModel = viewModel()
) {
    val uiState by skillViewModel.uiState.collectAsState()
    val userSkills by skillViewModel.userSkills.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var showAddSkillDialog by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }
    var selectedSkill by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf<Skill.SkillType?>(null) }
    var selectedLevel by remember { mutableStateOf(1) }
    var levelExpanded by remember { mutableStateOf(false) }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    // Cargar habilidades al iniciar
    LaunchedEffect(Unit) {
        skillViewModel.loadUserSkills()
    }

    SharedNavigationDrawer(
        navController = navController,
        viewModel = profileViewModel,
        drawerState = drawerState
    ) {
        // Observar cambios en el rol del usuario desde el ProfileViewModel
        val currentUser by profileViewModel.currentUser.collectAsState()
        LaunchedEffect(currentUser?.role) {
            when (currentUser?.role) {
                "Mentor" -> skillViewModel.updateUserRole(SkillViewModel.UserRole.MENTOR)
                "Aprendiz" -> skillViewModel.updateUserRole(SkillViewModel.UserRole.LEARNER)
                else -> skillViewModel.updateUserRole(SkillViewModel.UserRole.BOTH)
            }
        }

        Scaffold(
            topBar = {
                SharedTopBar(
                    navController = navController,
                    viewModel = profileViewModel,
                    title = "SkillSync",
                    onDrawerOpen = {
                        scope.launch { drawerState.open() }
                    }
                )
            },
            bottomBar = {
                SharedBottomBar(navController = navController)
            },
            containerColor = Color.White,
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Título y descripción fijos
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Mis habilidades",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF111827),
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        Text(
                            text = "Habilidades que puedes enseñar o quieres aprender",
                            fontSize = 16.sp,
                            color = Color(0xFF6B7280),
                            modifier = Modifier.padding(bottom = 24.dp)
                        )
                    }

                    // Lista de habilidades con scroll
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 16.dp)
                            .padding(bottom = 120.dp) // Espacio para los botones
                    ) {
                        if (userSkills.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 32.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.School,
                                            contentDescription = null,
                                            tint = Color(0xFFE2E8F0),
                                            modifier = Modifier.size(64.dp)
                                        )
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Text(
                                            text = "No tienes habilidades",
                                            fontSize = 18.sp,
                                            color = Color(0xFF6B7280),
                                            textAlign = TextAlign.Center
                                        )
                                        Text(
                                            text = "Añade habilidades para enseñar o aprender",
                                            fontSize = 16.sp,
                                            color = Color(0xFF6B7280),
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }
                        } else {
                            items(userSkills) { skill ->
                                SkillItem(
                                    skill = skill,
                                    onDelete = { skillViewModel.deleteSkill(skill) }
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                    }
                }

                // Botones fijos en la parte inferior
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { showAddSkillDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF5B4DBC)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Añadir habilidad")
                    }

                    OutlinedButton(
                        onClick = { navController.navigate("profile") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFF5B4DBC)
                        ),
                        border = BorderStroke(1.dp, Color(0xFF5B4DBC)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Regresar")
                    }
                }
            }

            if (showAddSkillDialog) {
                Dialog(
                    onDismissRequest = {
                        showAddSkillDialog = false
                        selectedSkill = ""
                        selectedType = null
                        selectedLevel = 1
                    },
                    properties = DialogProperties(
                        dismissOnBackPress = true,
                        dismissOnClickOutside = true,
                        usePlatformDefaultWidth = false
                    )
                ) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth(0.95f)
                            .padding(16.dp),
                        shape = RoundedCornerShape(28.dp),
                        color = MaterialTheme.colorScheme.surface,
                        tonalElevation = 6.dp
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Añadir habilidad",
                                        style = MaterialTheme.typography.headlineSmall,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Agrega una nueva habilidad a tu perfil",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Divider(color = MaterialTheme.colorScheme.outlineVariant)

                            ExposedDropdownMenuBox(
                                expanded = expanded,
                                onExpandedChange = { expanded = it }
                            ) {
                                OutlinedTextField(
                                    value = selectedSkill,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Habilidad") },
                                    placeholder = { Text("Selecciona una habilidad") },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .menuAnchor(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                                        focusedBorderColor = MaterialTheme.colorScheme.primary
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                )

                                ExposedDropdownMenu(
                                    expanded = expanded,
                                    onDismissRequest = { expanded = false },
                                    modifier = Modifier.heightIn(max = 300.dp)
                                ) {
                                    skillViewModel.predefinedSkills.forEach { skill ->
                                        DropdownMenuItem(
                                            text = { Text(skill) },
                                            onClick = {
                                                selectedSkill = skill
                                                expanded = false
                                            }
                                        )
                                    }
                                }
                            }

                            val userRole by skillViewModel.userRole.collectAsState()
                            LaunchedEffect(userRole) {
                                when (userRole) {
                                    SkillViewModel.UserRole.MENTOR -> {
                                        selectedType = Skill.SkillType.TEACH
                                    }
                                    SkillViewModel.UserRole.LEARNER -> {
                                        selectedType = Skill.SkillType.LEARN
                                    }
                                    SkillViewModel.UserRole.BOTH -> {
                                        // No forzar ningún tipo
                                        if (selectedType == null) {
                                            selectedType = null
                                        }
                                    }
                                }
                            }

                            when (userRole) {
                                SkillViewModel.UserRole.MENTOR -> {
                                    Text(
                                        text = "Como mentor, solo puedes agregar habilidades para enseñar",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                SkillViewModel.UserRole.LEARNER -> {
                                    Text(
                                        text = "Como aprendiz, solo puedes agregar habilidades para aprender",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                SkillViewModel.UserRole.BOTH -> {
                                    Text(
                                        text = "Tipo de habilidad",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        FilterChip(
                                            selected = selectedType == Skill.SkillType.LEARN,
                                            onClick = { selectedType = Skill.SkillType.LEARN },
                                            label = { Text("Quiero aprender") },
                                            leadingIcon = {
                                                Icon(
                                                    imageVector = Icons.Default.School,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            },
                                            modifier = Modifier.weight(1f)
                                        )
                                        FilterChip(
                                            selected = selectedType == Skill.SkillType.TEACH,
                                            onClick = { selectedType = Skill.SkillType.TEACH },
                                            label = { Text("Puedo enseñar") },
                                            leadingIcon = {
                                                Icon(
                                                    imageVector = Icons.Default.Psychology,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            },
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                            }

                            Text(
                                text = "¿Qué tan avanzado eres en esta habilidad?",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            ExposedDropdownMenuBox(
                                expanded = levelExpanded,
                                onExpandedChange = { levelExpanded = it }
                            ) {
                                OutlinedTextField(
                                    value = skillViewModel.skillLevels.find { it.id == selectedLevel }?.name ?: "",
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Nivel de conocimiento") },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = levelExpanded) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .menuAnchor(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                                        focusedBorderColor = MaterialTheme.colorScheme.primary
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                )

                                ExposedDropdownMenu(
                                    expanded = levelExpanded,
                                    onDismissRequest = { levelExpanded = false }
                                ) {
                                    skillViewModel.skillLevels.forEach { level ->
                                        DropdownMenuItem(
                                            text = {
                                                Column {
                                                    Text(text = "${level.id} - ${level.name}")
                                                    if (level.description.isNotEmpty()) {
                                                        Text(
                                                            text = level.description,
                                                            style = MaterialTheme.typography.bodySmall,
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                                        )
                                                    }
                                                }
                                            },
                                            onClick = {
                                                selectedLevel = level.id
                                                levelExpanded = false
                                            }
                                        )
                                    }
                                }
                            }

                            Divider(color = MaterialTheme.colorScheme.outlineVariant)

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { showAddSkillDialog = false },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = MaterialTheme.colorScheme.onSurface
                                    ),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                                ) {
                                    Text("Cancelar")
                                }
                                Button(
                                    onClick = {
                                        if (selectedSkill.isNotBlank() && selectedType != null) {
                                            skillViewModel.addSkill(selectedSkill, selectedType!!, selectedLevel)
                                            selectedSkill = ""
                                            selectedType = null
                                            selectedLevel = 1
                                            showAddSkillDialog = false
                                        }
                                    },
                                    enabled = selectedSkill.isNotBlank() && selectedType != null,
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary
                                    )
                                ) {
                                    Text("Añadir")
                                }
                            }
                        }
                    }
                }
            }

            LaunchedEffect(uiState) {
                when (uiState) {
                    is SkillViewModel.UiState.Success -> {
                        snackbarHostState.showSnackbar(
                            message = (uiState as SkillViewModel.UiState.Success).message,
                            duration = SnackbarDuration.Short,
                            withDismissAction = true
                        )
                        skillViewModel.resetState()
                    }
                    is SkillViewModel.UiState.Error -> {
                        snackbarHostState.showSnackbar(
                            message = (uiState as SkillViewModel.UiState.Error).message,
                            duration = SnackbarDuration.Short,
                            withDismissAction = true
                        )
                        skillViewModel.resetState()
                    }
                    else -> {}
                }
            }
        }
    }
}

@Composable
fun SkillItem(
    skill: Skill,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = skill.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF111827)
                )
                Text(
                    text = when (skill.type) {
                        Skill.SkillType.TEACH -> "Quiero enseñar"
                        Skill.SkillType.LEARN -> "Quiero aprender"
                    },
                    fontSize = 14.sp,
                    color = Color(0xFF6B7280)
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
                    fontSize = 14.sp,
                    color = Color(0xFF6B7280)
                )
            }
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Eliminar habilidad",
                    tint = Color(0xFFEF4444)
                )
            }
        }
    }
} 