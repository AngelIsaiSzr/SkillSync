package com.ics.skillsync.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.ics.skillsync.model.TeachingCard
import com.ics.skillsync.ui.components.SharedTopBar
import com.ics.skillsync.ui.viewmodel.TeachingCardViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ics.skillsync.ui.components.SharedBottomBar
import com.ics.skillsync.ui.components.SharedNavigationDrawer
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeachingCardScreen(
    navController: NavController,
    viewModel: TeachingCardViewModel = viewModel(),
    cardId: String? = null
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var availability by remember { mutableStateOf("") }
    var experienceLevel by remember { mutableStateOf(TeachingCard.ExperienceLevel.PRINCIPIANTE) }
    var imageUrl by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    
    var titleError by remember { mutableStateOf<String?>(null) }
    var descriptionError by remember { mutableStateOf<String?>(null) }
    var categoryError by remember { mutableStateOf<String?>(null) }
    var availabilityError by remember { mutableStateOf<String?>(null) }
    
    var showExperienceLevelMenu by remember { mutableStateOf(false) }
    var showCategoryMenu by remember { mutableStateOf(false) }
    var isProcessing by remember { mutableStateOf(false) }

    // Lista de niveles de experiencia
    val experienceLevels = listOf(
        TeachingCard.ExperienceLevel.PRINCIPIANTE,
        TeachingCard.ExperienceLevel.BASICO,
        TeachingCard.ExperienceLevel.INTERMEDIO,
        TeachingCard.ExperienceLevel.AVANZADO,
        TeachingCard.ExperienceLevel.EXPERTO
    )

    // Lista de categorías disponibles
    val categories = listOf(
        "Tecnología",
        "Diseño",
        "Marketing",
        "Idiomas",
        "Arte",
        "Música",
        "Gastronomía",
        "Deportes",
        "Ciencias",
        "Humanidades",
        "Finanzas",
        "Derecho",
        "Salud",
        "Educación",
        "Otros"
    )

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            imageUrl = it.toString()
        }
    }
    
    // Cargar datos si estamos editando
    LaunchedEffect(cardId) {
        if (cardId != null) {
            viewModel.loadTeachingCard(cardId)?.let { card ->
                title = card.title
                description = card.description
                category = card.category
                availability = card.availability
                experienceLevel = card.experienceLevel
                imageUrl = card.imageUrl
            }
        }
    }
    
    // Validación de campos
    fun validateFields(): Boolean {
        var isValid = true
        
        if (title.isBlank()) {
            titleError = "El título es requerido"
            isValid = false
        }
        if (description.isBlank()) {
            descriptionError = "La descripción es requerida"
            isValid = false
        }
        if (category.isBlank()) {
            categoryError = "La categoría es requerida"
            isValid = false
        }
        if (availability.isBlank()) {
            availabilityError = "La disponibilidad es requerida"
            isValid = false
        }
        
        return isValid
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(
                                    color = Color(0xFF5B4DBC),
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.SwapHoriz,
                                contentDescription = "Logo",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Text(
                            text = "SkillSync",
                            style = MaterialTheme.typography.titleLarge,
                            color = Color(0xFF5B4DBC)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Volver",
                            tint = Color(0xFF5B4DBC)
                        )
                    }
                },
                actions = {
                    if (cardId != null) {
                        IconButton(
                            onClick = {
                                scope.launch {
                                    viewModel.deleteTeachingCard(cardId)
                                    snackbarHostState.showSnackbar("Tarjeta eliminada")
                                    navController.popBackStack()
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Eliminar",
                                tint = Color(0xFF5B4DBC)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color(0xFF5B4DBC)
                )
            )
        },
        bottomBar = {
            SharedBottomBar(navController = navController)
        },
        containerColor = Color.White,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(scrollState)
        ) {
            // Título de la pantalla
            Text(
                text = if (cardId == null) "Crear Tarjeta de Enseñanza" else "Editar Tarjeta de Enseñanza",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // Imagen de la tarjeta
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(Color(0xFFF3F4F6), RoundedCornerShape(8.dp))
                    .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (imageUrl.isNotEmpty()) {
                    AsyncImage(
                        model = if (selectedImageUri != null) selectedImageUri else imageUrl,
                        contentDescription = "Imagen de la tarjeta",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Image,
                        contentDescription = "Agregar imagen",
                        tint = Color(0xFF9CA3AF),
                        modifier = Modifier.size(48.dp)
                    )
                }
                
                Button(
                    onClick = { imagePicker.launch("image/*") },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5B4DBC))
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Agregar imagen",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (imageUrl.isEmpty()) "Agregar imagen" else "Cambiar imagen")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Campos del formulario
            OutlinedTextField(
                value = title,
                onValueChange = { 
                    title = it
                    titleError = null
                },
                label = { Text("Título") },
                modifier = Modifier.fillMaxWidth(),
                isError = titleError != null,
                supportingText = titleError?.let { { Text(it) } },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF5B4DBC),
                    unfocusedBorderColor = Color.LightGray
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = description,
                onValueChange = { 
                    description = it
                    descriptionError = null
                },
                label = { Text("Descripción") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 5,
                isError = descriptionError != null,
                supportingText = descriptionError?.let { { Text(it) } },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF5B4DBC),
                    unfocusedBorderColor = Color.LightGray
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Selector de categoría
            ExposedDropdownMenuBox(
                expanded = showCategoryMenu,
                onExpandedChange = { showCategoryMenu = !showCategoryMenu }
            ) {
                OutlinedTextField(
                    value = category,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Categoría") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showCategoryMenu) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    isError = categoryError != null,
                    supportingText = categoryError?.let { { Text(it) } },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF5B4DBC),
                        unfocusedBorderColor = Color.LightGray
                    )
                )
                ExposedDropdownMenu(
                    expanded = showCategoryMenu,
                    onDismissRequest = { showCategoryMenu = false }
                ) {
                    categories.forEach { cat ->
                        DropdownMenuItem(
                            text = { Text(cat) },
                            onClick = {
                                category = cat
                                categoryError = null
                                showCategoryMenu = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Selector de nivel de experiencia
            ExposedDropdownMenuBox(
                expanded = showExperienceLevelMenu,
                onExpandedChange = { showExperienceLevelMenu = !showExperienceLevelMenu }
            ) {
                OutlinedTextField(
                    value = experienceLevel.getDisplayName(),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Nivel de experiencia") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showExperienceLevelMenu) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF5B4DBC),
                        unfocusedBorderColor = Color.LightGray
                    )
                )
                ExposedDropdownMenu(
                    expanded = showExperienceLevelMenu,
                    onDismissRequest = { showExperienceLevelMenu = false }
                ) {
                    experienceLevels.forEach { level ->
                        DropdownMenuItem(
                            text = { Text(level.getDisplayName()) },
                            onClick = {
                                experienceLevel = level
                                showExperienceLevelMenu = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = availability,
                onValueChange = { 
                    availability = it
                    availabilityError = null
                },
                label = { Text("Horarios") },
                modifier = Modifier.fillMaxWidth(),
                isError = availabilityError != null,
                supportingText = availabilityError?.let { { Text(it) } },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF5B4DBC),
                    unfocusedBorderColor = Color.LightGray
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (validateFields()) {
                        isProcessing = true
                        if (cardId == null) {
                            viewModel.createTeachingCard(
                                title = title,
                                description = description,
                                category = category,
                                experienceLevel = experienceLevel,
                                availability = availability,
                                imageUri = selectedImageUri
                            )
                        } else {
                            viewModel.updateTeachingCard(
                                cardId = cardId,
                                title = title,
                                description = description,
                                category = category,
                                experienceLevel = experienceLevel,
                                availability = availability,
                                imageUri = selectedImageUri
                            )
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF5B4DBC),
                    disabledContainerColor = Color(0xFF5B4DBC).copy(alpha = 0.6f)
                ),
                enabled = !isProcessing
            ) {
                if (isProcessing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(if (cardId == null) "Crear Tarjeta" else "Actualizar Tarjeta")
                }
            }
        }
    }

    // Manejo de estados UI
    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is TeachingCardViewModel.UiState.Success -> {
                viewModel.clearUiState()
                navController.navigate("my_teaching_cards") {
                    popUpTo("my_teaching_cards") { inclusive = true }
                }
                isProcessing = false
            }
            is TeachingCardViewModel.UiState.Error -> {
                snackbarHostState.showSnackbar(state.message)
                viewModel.clearUiState()
                isProcessing = false
            }
            else -> {}
        }
    }

    // Limpiar estado al salir
    DisposableEffect(Unit) {
        onDispose {
            viewModel.clearUiState()
        }
    }
} 