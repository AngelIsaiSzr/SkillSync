package com.ics.skillsync.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import kotlinx.coroutines.launch
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
import kotlinx.coroutines.delay
import androidx.compose.animation.core.tween

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    viewModel: ProfileViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val isAuthenticated by viewModel.isAuthenticated.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()
    val verificationState by viewModel.verificationState.collectAsState()
    var showImagePicker by remember { mutableStateOf(false) }
    var showVerificationDialog by remember { mutableStateOf(false) }
    
    // Estado para el diálogo de restablecimiento de contraseña
    var showResetPasswordDialog by remember { mutableStateOf(false) }
    var resetEmail by remember { mutableStateOf("") }
    var resetEmailError by remember { mutableStateOf<String?>(null) }

    // Efecto para cargar la foto al montar el componente
    LaunchedEffect(Unit) {
        viewModel.clearUiState()
        // Forzar una recarga del usuario actual
        viewModel.checkSession()
    }

    // Efecto para manejar la autenticación
    LaunchedEffect(isAuthenticated) {
        if (isAuthenticated) {
            delay(300)
            scope.launch {
                scrollState.animateScrollTo(0, animationSpec = tween(500))
            }
        }
    }

    // Efecto para cargar el estado de verificación
    LaunchedEffect(Unit) {
        viewModel.checkVerificationStatus()
    }

    // Estados de validación
    var usernameError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var confirmPasswordError by remember { mutableStateOf<String?>(null) }
    var firstNameError by remember { mutableStateOf<String?>(null) }
    var lastNameError by remember { mutableStateOf<String?>(null) }

    // Estado para mostrar/ocultar contraseña
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    var isLoginForm by remember { mutableStateOf(true) }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf("Ambos roles") }
    var expanded by remember { mutableStateOf(false) }
    val roles = listOf("Ambos roles", "Mentor", "Aprendiz")

    // Funciones de validación
    fun validateUsername(username: String): String? {
        return when {
            username.isBlank() -> "El nombre de usuario es requerido"
            else -> null
        }
    }

    fun validatePassword(password: String): String? {
        return when {
            password.isBlank() -> "La contraseña es requerida"
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

    fun validateName(name: String, field: String): String? {
        return when {
            name.isBlank() -> "$field es requerido"
            name.length < 2 -> "$field debe tener al menos 2 caracteres"
            else -> null
        }
    }

    fun validateConfirmPassword(password: String, confirmPassword: String): String? {
        return when {
            confirmPassword.isBlank() -> "Confirma tu contraseña"
            confirmPassword != password -> "Las contraseñas no coinciden"
            else -> null
        }
    }

    fun validateLoginForm(): Boolean {
        // Solo validar el campo que está vacío
        if (email.isBlank()) {
            emailError = "El correo electrónico es requerido"
            passwordError = null
            return false
        }
        if (password.isBlank()) {
            passwordError = "La contraseña es requerida"
            emailError = null
            return false
        }
        emailError = null
        passwordError = null
        return true
    }

    fun validateRegistrationForm(): Boolean {
        firstNameError = validateName(firstName, "El nombre")
        lastNameError = validateName(lastName, "El apellido")
        usernameError = validateUsername(username)
        emailError = validateEmail(email)
        passwordError = validatePassword(password)
        confirmPasswordError = validateConfirmPassword(password, confirmPassword)
        return firstNameError == null && lastNameError == null && 
               usernameError == null && emailError == null && 
               passwordError == null && confirmPasswordError == null
    }

    // Función para manejar el inicio de sesión
    fun handleLogin() {
        if (validateLoginForm()) {
            viewModel.loginUser(email, password)
        }
    }

    // Función para manejar el registro
    fun handleRegister() {
        if (validateRegistrationForm()) {
            viewModel.register(
                firstName = firstName,
                lastName = lastName,
                username = username,
                email = email,
                password = password,
                role = selectedRole
            )
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
                horizontalAlignment = Alignment.Start
            ) {
                if (!isAuthenticated) {
                    Spacer(modifier = Modifier.height(16.dp))

                    // Título principal y descripción
                    Text(
                        text = "Únete a SkillSync",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF111827),
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Conecta con personas dispuestas a compartir sus conocimientos y habilidades. Ya sea que quieras enseñar o aprender, SkillSync facilita el intercambio.",
                        fontSize = 16.sp,
                        color = Color(0xFF6B7280),
                        lineHeight = 24.sp,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Sección "¿Por qué unirte?"
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFF3F4F6)
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = "¿Por qué unirte?",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF111827)
                            )

                            BenefitItem(
                                text = "Aprende nuevas habilidades de manera gratuita"
                            )
                            BenefitItem(
                                text = "Comparte tus conocimientos y ayuda a otros a crecer"
                            )
                            BenefitItem(
                                text = "Construye una red de contactos profesionales"
                            )
                            BenefitItem(
                                text = "Programa sesiones en horarios que te convengan"
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Imagen de personas estudiando
                    AsyncImage(
                        model = "https://images.unsplash.com/photo-1522202176988-66273c2fd55f",
                        contentDescription = "Personas estudiando",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .padding(horizontal = 16.dp)
                            .clip(RoundedCornerShape(16.dp)),
                        contentScale = ContentScale.Crop,
                        onLoading = {
                            @Composable {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = Color(0xFF5B4DBC)
                                )
                            }
                        },
                        onError = {
                            @Composable {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "Error al cargar la foto",
                                    modifier = Modifier.size(60.dp),
                                    tint = Color(0xFF5B4DBC)
                                )
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Botones de navegación entre formularios
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .background(Color.White)
                            .clip(RoundedCornerShape(8.dp))
                            .border(
                                width = 1.dp,
                                color = Color(0xFFE5E7EB),
                                shape = RoundedCornerShape(8.dp)
                            )
                    ) {
                        TextButton(
                            onClick = { isLoginForm = true },
                            modifier = Modifier
                                .weight(1f)
                                .background(
                                    if (isLoginForm) Color.White else Color(0xFFF3F4F6),
                                    shape = RoundedCornerShape(topStart = 8.dp, bottomStart = 8.dp)
                                ),
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = if (isLoginForm) Color(0xFF5B4DBC) else Color(0xFF6B7280)
                            )
                        ) {
                            Text(
                                text = "Iniciar Sesión",
                                fontSize = 14.sp,
                                fontWeight = if (isLoginForm) FontWeight.Medium else FontWeight.Normal,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                        TextButton(
                            onClick = { isLoginForm = false },
                            modifier = Modifier
                                .weight(1f)
                                .background(
                                    if (!isLoginForm) Color.White else Color(0xFFF3F4F6),
                                    shape = RoundedCornerShape(topEnd = 8.dp, bottomEnd = 8.dp)
                                ),
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = if (!isLoginForm) Color(0xFF5B4DBC) else Color(0xFF6B7280)
                            )
                        ) {
                            Text(
                                text = "Registrarse",
                                fontSize = 14.sp,
                                fontWeight = if (!isLoginForm) FontWeight.Medium else FontWeight.Normal,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Card del formulario
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, Color(0xFFE5E7EB))
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // Contenido del formulario
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Text(
                                    text = if (isLoginForm) "Iniciar Sesión" else "Crear una cuenta",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                )
                                
                                Text(
                                    text = if (isLoginForm) 
                                        "Ingresa tus credenciales para acceder a tu cuenta" 
                                    else 
                                        "Regístrate para empezar a intercambiar habilidades",
                                    fontSize = 14.sp,
                                    color = Color.Gray,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )

                                if (isLoginForm) {
                                    // Formulario de inicio de sesión
                                    OutlinedTextField(
                                        value = email,
                                        onValueChange = { 
                                            email = it
                                            emailError = null
                                        },
                                        label = { Text("Correo electrónico") },
                                        modifier = Modifier.fillMaxWidth(),
                                        isError = emailError != null,
                                        supportingText = emailError?.let { { Text(it) } },
                                        colors = OutlinedTextFieldDefaults.colors(
                                            unfocusedBorderColor = Color.LightGray,
                                            focusedBorderColor = Color(0xFF5B4DBC)
                                        )
                                    )

                                    OutlinedTextField(
                                        value = password,
                                        onValueChange = { 
                                            password = it
                                            passwordError = null
                                        },
                                        label = { Text("Contraseña") },
                                        modifier = Modifier.fillMaxWidth(),
                                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                        trailingIcon = {
                                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                                Icon(
                                                    if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                                    contentDescription = if (passwordVisible) "Ocultar contraseña" else "Mostrar contraseña"
                                                )
                                            }
                                        },
                                        isError = passwordError != null,
                                        supportingText = passwordError?.let { { Text(it) } },
                                        colors = OutlinedTextFieldDefaults.colors(
                                            unfocusedBorderColor = Color.LightGray,
                                            focusedBorderColor = Color(0xFF5B4DBC)
                                        )
                                    )

                                    Box(
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        TextButton(
                                            onClick = { showResetPasswordDialog = true },
                                            modifier = Modifier
                                                .align(Alignment.CenterEnd)
                                        ) {
                                            Text(
                                                text = "¿Olvidaste tu contraseña?",
                                                color = Color(0xFF5B4DBC),
                                                fontSize = 14.sp
                                            )
                                        }
                                    }

                                    Button(
                                        onClick = { handleLogin() },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(48.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5B4DBC)),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text("Iniciar Sesión")
                                    }

                                    Column(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = "¿No tienes una cuenta?",
                                            color = Color(0xFF6B7280)
                                        )
                                        TextButton(
                                            onClick = { isLoginForm = false }
                                        ) {
                                            Text(
                                                text = "Crear una cuenta",
                                                color = Color(0xFF5B4DBC)
                                            )
                                        }
                                    }
                                } else {
                                    // Formulario de registro
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

                                    OutlinedTextField(
                                        value = password,
                                        onValueChange = { 
                                            password = it
                                            passwordError = null
                                            confirmPasswordError = null
                                        },
                                        label = { Text("Contraseña") },
                                        modifier = Modifier.fillMaxWidth(),
                                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                        trailingIcon = {
                                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                                Icon(
                                                    if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                                    contentDescription = if (passwordVisible) "Ocultar contraseña" else "Mostrar contraseña"
                                                )
                                            }
                                        },
                                        isError = passwordError != null,
                                        supportingText = passwordError?.let { { Text(it) } },
                                        colors = OutlinedTextFieldDefaults.colors(
                                            unfocusedBorderColor = Color.LightGray,
                                            focusedBorderColor = Color(0xFF5B4DBC)
                                        )
                                    )

                                    OutlinedTextField(
                                        value = confirmPassword,
                                        onValueChange = { 
                                            confirmPassword = it
                                            confirmPasswordError = null
                                        },
                                        label = { Text("Confirmar contraseña") },
                                        modifier = Modifier.fillMaxWidth(),
                                        visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                        trailingIcon = {
                                            IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                                                Icon(
                                                    if (confirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                                    contentDescription = if (confirmPasswordVisible) "Ocultar contraseña" else "Mostrar contraseña"
                                                )
                                            }
                                        },
                                        isError = confirmPasswordError != null,
                                        supportingText = confirmPasswordError?.let { { Text(it) } },
                                        colors = OutlinedTextFieldDefaults.colors(
                                            unfocusedBorderColor = Color.LightGray,
                                            focusedBorderColor = Color(0xFF5B4DBC)
                                        )
                                    )

                                    Button(
                                        onClick = { handleRegister() },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(48.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5B4DBC)),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text("Registrarse")
                                    }

                                    Column(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = "¿Ya tienes una cuenta?",
                                            color = Color(0xFF6B7280)
                                        )
                                        TextButton(
                                            onClick = { isLoginForm = true }
                                        ) {
                                            Text(
                                                text = "Iniciar sesión",
                                                color = Color(0xFF5B4DBC)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // Vista de perfil autenticado
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
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
                                currentUser?.photoUrl.isNullOrEmpty() -> {
                                    // Mostrar ícono por defecto cuando no hay foto
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = "Foto de perfil por defecto",
                                        modifier = Modifier.size(60.dp),
                                        tint = Color(0xFF5B4DBC)
                                    )
                                }
                                else -> {
                                    var isLoading by remember { mutableStateOf(true) }
                                    
                                    if (isLoading) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(24.dp),
                                            color = Color(0xFF5B4DBC)
                                        )
                                    }
                                    
                                    AsyncImage(
                                        model = currentUser?.photoUrl,
                                        contentDescription = "Foto de perfil",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop,
                                        onLoading = { isLoading = true },
                                        onSuccess = { isLoading = false },
                                        onError = { isLoading = false }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Nombre y rol del usuario
                        currentUser?.let { user ->
                            Text(
                                text = "${user.firstName} ${user.lastName}",
                                style = MaterialTheme.typography.headlineMedium,
                                color = Color(0xFF111827),
                                textAlign = TextAlign.Center
                            )

                            Text(
                                text = user.role ?: "Miembro",
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color(0xFF6B7280),
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Estado de verificación
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Surface(
                                modifier = Modifier
                                    .border(
                                        width = 1.dp,
                                        color = when (verificationState) {
                                            is ProfileViewModel.VerificationState.Verified -> Color(0xFF2E7D32)
                                            else -> Color(0xFF757575)
                                        },
                                        shape = RoundedCornerShape(16.dp)
                                    )
                                    .padding(horizontal = 12.dp, vertical = 6.dp),
                                color = Color.Transparent
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = when (verificationState) {
                                            is ProfileViewModel.VerificationState.Verified -> Icons.Default.Verified
                                            else -> Icons.Default.Close
                                        },
                                        contentDescription = "Estado de verificación",
                                        tint = when (verificationState) {
                                            is ProfileViewModel.VerificationState.Verified -> Color(0xFF2E7D32)
                                            else -> Color(0xFF757575)
                                        }
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = when (verificationState) {
                                            is ProfileViewModel.VerificationState.Verified -> 
                                                "Verificado: Nivel ${(verificationState as ProfileViewModel.VerificationState.Verified).level}"
                                            else -> "No verificado"
                                        },
                                        color = when (verificationState) {
                                            is ProfileViewModel.VerificationState.Verified -> Color(0xFF2E7D32)
                                            else -> Color(0xFF757575)
                                        },
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Tarjeta de información personal
                        Card(
                            modifier = Modifier
                                .fillMaxWidth(),
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

                                // Campos de información
                                currentUser?.let { user ->
                                    InfoField(
                                        icon = Icons.Default.Email,
                                        label = "Correo electrónico",
                                        value = user.email
                                    )

                                    Spacer(modifier = Modifier.height(12.dp))

                                    InfoField(
                                        icon = Icons.Default.Person,
                                        label = "Nombre de usuario",
                                        value = user.username
                                    )

                                    Spacer(modifier = Modifier.height(12.dp))

                                    InfoField(
                                        icon = Icons.Default.School,
                                        label = "Rol",
                                        value = user.role ?: "No especificado"
                                    )

                                    Spacer(modifier = Modifier.height(12.dp))

                                    InfoField(
                                        icon = Icons.Default.Description,
                                        label = "Biografía",
                                        value = user.biography.ifEmpty { "No especificada" }
                                    )

                                    Spacer(modifier = Modifier.height(12.dp))

                                    InfoField(
                                        icon = Icons.Default.Schedule,
                                        label = "Disponibilidad",
                                        value = user.availability.ifEmpty { "No especificada" }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Tarjeta de estadísticas
                        Card(
                            modifier = Modifier
                                .fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, Color(0xFFE5E7EB))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                StatisticItem(
                                    icon = Icons.Default.Star,
                                    value = "0.0",
                                    label = "Calificación"
                                )
                                StatisticItem(
                                    icon = Icons.Default.Groups,
                                    value = "0",
                                    label = "Sesiones"
                                )
                                val skillCount by viewModel.skillCount.collectAsState()
                                StatisticItem(
                                    icon = Icons.Default.WorkspacePremium,
                                    value = skillCount.toString(),
                                    label = "Habilidades"
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Botones de acción
                        if (verificationState !is ProfileViewModel.VerificationState.Verified || 
                            (verificationState as ProfileViewModel.VerificationState.Verified).level < 3) {
                            Button(
                                onClick = {
                                    when (verificationState) {
                                        is ProfileViewModel.VerificationState.Verified -> {
                                            val level = (verificationState as ProfileViewModel.VerificationState.Verified).level
                                            when (level) {
                                                1 -> showImagePicker = true
                                                2 -> showVerificationDialog = true
                                                else -> viewModel.verifyEmail()
                                            }
                                        }
                                        else -> viewModel.verifyEmail()
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF5B4DBC)
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Verified,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = when (verificationState) {
                                        is ProfileViewModel.VerificationState.Verified -> {
                                            val level = (verificationState as ProfileViewModel.VerificationState.Verified).level
                                            when (level) {
                                                1 -> "Subir imagen de verificación"
                                                2 -> "Tomar examen de verificación"
                                                else -> "Verificar correo electrónico"
                                            }
                                        }
                                        else -> "Verificar correo electrónico"
                                    }
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                        }

                        Button(
                            onClick = { navController.navigate("edit_profile") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF5B4DBC)
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Editar Perfil")
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = { navController.navigate("skills") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF5B4DBC)
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.School,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Mis Habilidades")
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedButton(
                            onClick = { viewModel.logout() },
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
                                imageVector = Icons.Default.Logout,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Cerrar Sesión")
                        }
                    }
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
                            // Scroll al inicio después del login exitoso
                            scope.launch {
                                delay(500) // Esperar a que se muestre el snackbar
                                scrollState.animateScrollTo(0, animationSpec = tween(500))
                                navController.navigate("profile") {
                                    popUpTo("profile") { inclusive = true }
                                }
                            }
                        }
                    }
                    else -> {}
                }

                // Diálogo de restablecimiento de contraseña
                if (showResetPasswordDialog) {
                    AlertDialog(
                        onDismissRequest = { 
                            showResetPasswordDialog = false
                            resetEmail = ""
                            resetEmailError = null
                        },
                        title = { Text("Restablecer contraseña") },
                        text = {
                            Column {
                                Text(
                                    "Ingresa tu correo electrónico y te enviaremos las instrucciones para restablecer tu contraseña.",
                                    modifier = Modifier.padding(bottom = 16.dp)
                                )
                                OutlinedTextField(
                                    value = resetEmail,
                                    onValueChange = { 
                                        resetEmail = it
                                        resetEmailError = null
                                    },
                                    label = { Text("Correo electrónico") },
                                    isError = resetEmailError != null,
                                    supportingText = resetEmailError?.let { { Text(it) } },
                                    modifier = Modifier.fillMaxWidth(),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        unfocusedBorderColor = Color.LightGray,
                                        focusedBorderColor = Color(0xFF5B4DBC)
                                    )
                                )
                            }
                        },
                        confirmButton = {
                            Button(
                                onClick = {
                                    if (resetEmail.isBlank()) {
                                        resetEmailError = "El correo electrónico es requerido"
                                    } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(resetEmail).matches()) {
                                        resetEmailError = "El correo electrónico no es válido"
                                    } else {
                                        viewModel.resetPassword(resetEmail)
                                        showResetPasswordDialog = false
                                        resetEmail = ""
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF5B4DBC)
                                )
                            ) {
                                Text("Enviar")
                            }
                        },
                        dismissButton = {
                            TextButton(
                                onClick = { 
                                    showResetPasswordDialog = false
                                    resetEmail = ""
                                    resetEmailError = null
                                }
                            ) {
                                Text("Cancelar")
                            }
                        }
                    )
                }

                // Diálogo de verificación
                if (showVerificationDialog) {
                    AlertDialog(
                        onDismissRequest = { showVerificationDialog = false },
                        title = { Text("Examen de verificación") },
                        text = { Text("¿Estás listo para tomar el examen de verificación?") },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    showVerificationDialog = false
                                    viewModel.completeVerificationExam()
                                }
                            ) {
                                Text("Comenzar")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showVerificationDialog = false }) {
                                Text("Cancelar")
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun BenefitItem(text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            tint = Color(0xFF5B4DBC)
        )
        Text(
            text = text,
            fontSize = 16.sp,
            color = Color(0xFF111827)
        )
    }
}

@Composable
fun TabButton(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth(),
        color = if (selected) Color.White else Color(0xFFF3F4F6)
    ) {
        TextButton(
            onClick = onClick,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
        ) {
            Text(
                text = text,
                fontSize = 16.sp,
                fontWeight = if (selected) FontWeight.Medium else FontWeight.Normal,
                color = if (selected) Color(0xFF5B4DBC) else Color.Gray
            )
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
private fun StatisticItem(
    icon: ImageVector,
    value: String,
    label: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color(0xFF5B4DBC),
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            color = Color(0xFF111827),
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF6B7280)
        )
    }
} 