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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.ics.skillsync.ui.viewmodel.ProfileViewModel
import com.ics.skillsync.ui.components.SharedNavigationDrawer
import com.ics.skillsync.ui.components.SharedTopBar
import com.ics.skillsync.ui.components.SharedBottomBar
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.foundation.layout.Arrangement.Absolute.Center
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.graphicsLayer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    navController: NavController,
    viewModel: ProfileViewModel = viewModel()
) {
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var showPhotoOptions by remember { mutableStateOf(false) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    var isPhotoDeleted by remember { mutableStateOf(false) }
    
    // Launcher para seleccionar imagen de la galería
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri -> 
        uri?.let { 
            selectedImageUri = it
            viewModel.updateProfilePhoto(it.toString())
            showPhotoOptions = false
        }
    }
    
    // Launcher para recortar la imagen
    val cropLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            selectedImageUri = it
            viewModel.updateProfilePhoto(it.toString())
            showPhotoOptions = false
        }
    }

    val uiState by viewModel.uiState.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val temporaryPhotoUrl by viewModel.temporaryPhotoUrl.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    val context = LocalContext.current

    // Desplazar hacia arriba cuando se carga la pantalla
    LaunchedEffect(Unit) {
        delay(300) // Pequeño retraso para asegurar que la UI se haya renderizado
        scope.launch {
            scrollState.animateScrollTo(0, animationSpec = tween(500))
        }
    }

    // Estados para los campos del formulario
    var firstName by remember { mutableStateOf(currentUser?.firstName ?: "") }
    var lastName by remember { mutableStateOf(currentUser?.lastName ?: "") }
    var username by remember { mutableStateOf(currentUser?.username ?: "") }
    var email by remember { mutableStateOf(currentUser?.email ?: "") }
    var selectedRole by remember { mutableStateOf(currentUser?.role ?: "Ambos roles") }
    var biography by remember { mutableStateOf(currentUser?.biography ?: "") }
    var availability by remember { mutableStateOf(currentUser?.availability ?: "") }
    var expanded by remember { mutableStateOf(false) }
    val roles = listOf("Ambos roles", "Mentor", "Aprendiz")

    // Actualizar los campos cuando cambie el usuario actual
    LaunchedEffect(currentUser) {
        currentUser?.let { user ->
            firstName = user.firstName
            lastName = user.lastName
            username = user.username
            email = user.email
            selectedRole = user.role
            biography = user.biography
            availability = user.availability
        }
    }

    // Estados de validación
    var firstNameError by remember { mutableStateOf<String?>(null) }
    var lastNameError by remember { mutableStateOf<String?>(null) }
    var usernameError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }

    // Funciones de validación
    fun validateName(name: String, field: String): String? {
        return when {
            name.isBlank() -> "$field es requerido"
            name.length < 2 -> "$field debe tener al menos 2 caracteres"
            else -> null
        }
    }

    fun validateUsername(username: String): String? {
        return when {
            username.isBlank() -> "El nombre de usuario es requerido"
            else -> null
        }
    }

    fun validateEmail(email: String): String? {
        return when {
            email.isBlank() -> "El correo electrónico es requerido"
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> "El correo electrónico no es válido"
            else -> null
        }
    }

    fun validateForm(): Boolean {
        firstNameError = validateName(firstName, "El nombre")
        lastNameError = validateName(lastName, "El apellido")
        usernameError = validateUsername(username)
        emailError = validateEmail(email)
        return firstNameError == null && lastNameError == null && 
               usernameError == null && emailError == null
    }

    // Función para manejar la actualización del perfil
    fun handleUpdateProfile() {
        if (validateForm()) {
            // Si hay una nueva foto seleccionada o se eliminó la foto, actualizar la foto primero
            if (selectedImageUri != null || isPhotoDeleted) {
                viewModel.saveProfilePhoto(if (isPhotoDeleted) "" else selectedImageUri.toString())
            }
            
            viewModel.updateProfile(
                firstName = firstName,
                lastName = lastName,
                username = username,
                email = email,
                role = selectedRole,
                biography = biography,
                availability = availability
            )
            
            // Navegar a la pantalla de perfil después de actualizar
            scope.launch {
                delay(1000) // Esperar a que se complete la actualización
                navController.navigate("profile") {
                    // Limpiar el back stack para evitar que el usuario pueda volver a la pantalla de edición
                    popUpTo("profile") { inclusive = true }
                }
            }
        }
    }
    
    // Función para confirmar la actualización de la foto
    fun confirmPhotoUpdate() {
        selectedImageUri?.let { uri ->
            viewModel.updateProfilePhoto(uri.toString())
            showPhotoOptions = false
        }
    }
    
    // Función para eliminar la foto de perfil
    fun deleteProfilePhoto() {
        isPhotoDeleted = true
        selectedImageUri = null
        viewModel.updateProfilePhoto("")
        showDeleteConfirmation = false
    }

    // Efecto para cargar la foto al montar el componente y cuando se actualiza
    LaunchedEffect(Unit) {
        viewModel.clearUiState()
        viewModel.checkSession()
    }

    // Mantener el estado del formulario cuando se vuelve de la galería
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                // Restaurar el estado del formulario
                currentUser?.let { user ->
                    firstName = user.firstName
                    lastName = user.lastName
                    username = user.username
                    email = user.email
                    selectedRole = user.role
                    biography = user.biography
                    availability = user.availability
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
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
            containerColor = Color.White,
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) { paddingValues ->
            Column(
            modifier = Modifier
                .fillMaxSize()
                    .background(Color.White)
                    .padding(paddingValues)
                    .verticalScroll(scrollState),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Foto de perfil con opciones
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surface)
                        .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                        .clickable { showPhotoOptions = true }
                ) {
                    when {
                        isPhotoDeleted -> {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Foto de perfil por defecto",
                                modifier = Modifier
                                    .size(60.dp)
                                    .align(Alignment.Center),
                                tint = Color(0xFF5B4DBC)
                            )
                        }
                        temporaryPhotoUrl != null -> {
                            AsyncImage(
                                model = temporaryPhotoUrl,
                                contentDescription = "Foto de perfil seleccionada",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                        currentUser?.photoUrl?.isNotEmpty() == true -> {
                            AsyncImage(
                                model = currentUser?.photoUrl,
                                contentDescription = "Foto de perfil actual",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                        else -> {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Foto de perfil por defecto",
                                modifier = Modifier
                                    .size(60.dp)
                                    .align(Alignment.Center),
                                tint = Color(0xFF5B4DBC)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Toca la foto para cambiarla",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF5B4DBC)
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Formulario de edición
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color(0xFFE5E7EB))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Información Personal",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color(0xFF111827),
                            fontWeight = FontWeight.Bold
                        )

                        // Nombre y apellido
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            OutlinedTextField(
                                value = firstName,
                                onValueChange = { 
                                    firstName = it
                                    firstNameError = null
                                },
                                label = { Text("Nombre") },
                                modifier = Modifier.weight(1f),
                                isError = firstNameError != null,
                                supportingText = firstNameError?.let { { Text(it) } },
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedBorderColor = Color.LightGray,
                                    focusedBorderColor = Color(0xFF5B4DBC)
                                )
                            )
                            OutlinedTextField(
                                value = lastName,
                                onValueChange = { 
                                    lastName = it
                                    lastNameError = null
                                },
                                label = { Text("Apellido") },
                                modifier = Modifier.weight(1f),
                                isError = lastNameError != null,
                                supportingText = lastNameError?.let { { Text(it) } },
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedBorderColor = Color.LightGray,
                                    focusedBorderColor = Color(0xFF5B4DBC)
                                )
                            )
                        }

                        // Nombre de usuario
                        OutlinedTextField(
                            value = username,
                            onValueChange = { 
                                username = it
                                usernameError = null
                            },
                            label = { Text("Nombre de usuario") },
                            modifier = Modifier.fillMaxWidth(),
                            isError = usernameError != null,
                            supportingText = usernameError?.let { { Text(it) } },
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedBorderColor = Color.LightGray,
                                focusedBorderColor = Color(0xFF5B4DBC)
                            )
                        )

                        // Correo electrónico
                        OutlinedTextField(
                            value = email,
                            onValueChange = { 
                                email = it
                                emailError = null
                            },
                            label = { Text("Correo electrónico") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            modifier = Modifier.fillMaxWidth(),
                            isError = emailError != null,
                            supportingText = emailError?.let { { Text(it) } },
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedBorderColor = Color.LightGray,
                                focusedBorderColor = Color(0xFF5B4DBC)
                            )
                        )

                        // Rol
                        ExposedDropdownMenuBox(
                            expanded = expanded,
                            onExpandedChange = { expanded = !expanded }
                        ) {
                            OutlinedTextField(
                                value = selectedRole,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("¿Cómo quieres participar?") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedBorderColor = Color.LightGray,
                                    focusedBorderColor = Color(0xFF5B4DBC)
                                )
                            )
                            ExposedDropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }
                            ) {
                                roles.forEach { role ->
                                    DropdownMenuItem(
                                        text = { Text(role) },
                                        onClick = {
                                            selectedRole = role
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }

                        // Biografía
                        OutlinedTextField(
                            value = biography,
                            onValueChange = { biography = it },
                            label = { Text("Biografía") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedBorderColor = Color.LightGray,
                                focusedBorderColor = Color(0xFF5B4DBC)
                            ),
                            placeholder = { Text("Cuéntanos sobre ti, tus intereses y experiencia...") },
                            minLines = 3,
                            maxLines = 5
                        )

                        // Disponibilidad
                        OutlinedTextField(
                            value = availability,
                            onValueChange = { availability = it },
                            label = { Text("Disponibilidad") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedBorderColor = Color.LightGray,
                                focusedBorderColor = Color(0xFF5B4DBC)
                            ),
                            placeholder = { Text("Ej: Lunes y miércoles por la tarde, fines de semana...") }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Botones de acción
                Button(
                    onClick = { handleUpdateProfile() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF5B4DBC)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Guardar Cambios")
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(
                    onClick = { 
                        // Limpiar la foto temporal antes de cancelar
                        viewModel.updateProfilePhoto("")
                        navController.navigateUp() 
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .height(48.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFF5B4DBC)
                    ),
                    border = BorderStroke(1.dp, Color(0xFF5B4DBC)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Cancelar")
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // Diálogo de opciones para la foto de perfil
    if (showPhotoOptions) {
        Dialog(
            onDismissRequest = { showPhotoOptions = false },
            properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = true)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Opciones de foto de perfil",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF111827)
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Botón para seleccionar de la galería
                    OutlinedButton(
                        onClick = {
                            showPhotoOptions = false
                            galleryLauncher.launch("image/*")
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFF5B4DBC)
                        ),
                        border = BorderStroke(1.dp, Color(0xFF5B4DBC))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PhotoLibrary,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Seleccionar")
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Botón para recortar la imagen
                    OutlinedButton(
                        onClick = { 
                            showPhotoOptions = false
                            cropLauncher.launch("image/*")
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFF5B4DBC)
                        ),
                        border = BorderStroke(1.dp, Color(0xFF5B4DBC))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Crop,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Recortar imagen")
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Botón para eliminar la foto
                    OutlinedButton(
                        onClick = { 
                            showPhotoOptions = false
                            showDeleteConfirmation = true
                        },
                    modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color.Red
                        ),
                        border = BorderStroke(1.dp, Color.Red)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                                Icon(
                                imageVector = Icons.Outlined.Delete,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Eliminar foto")
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Botón para cancelar
                    TextButton(
                        onClick = { showPhotoOptions = false }
                    ) {
                        Text("Cancelar")
                    }
                }
            }
        }
    }
    
    // Diálogo de confirmación para eliminar la foto
    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text("Eliminar foto de perfil") },
            text = { Text("¿Estás seguro de que deseas eliminar tu foto de perfil?") },
            confirmButton = {
                TextButton(
                    onClick = { deleteProfilePhoto() }
                ) {
                    Text("Eliminar", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteConfirmation = false }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Snackbar para mostrar mensajes de error o éxito
    when (val state = uiState) {
        is ProfileViewModel.UiState.Error -> {
            LaunchedEffect(state) {
                snackbarHostState.showSnackbar(
                    message = state.message,
                    duration = SnackbarDuration.Short
                )
            }
        }
        is ProfileViewModel.UiState.Success -> {
            LaunchedEffect(state) {
                snackbarHostState.showSnackbar(
                    message = state.message,
                    duration = SnackbarDuration.Short
                )
                // Navegar de vuelta a la pantalla de perfil después de mostrar el mensaje
                delay(1000)
                navController.navigate("profile") {
                    // Limpiar el back stack para evitar que el usuario pueda volver a la pantalla de edición
                    popUpTo("profile") { inclusive = true }
                }
            }
        }
        else -> {}
    }
} 