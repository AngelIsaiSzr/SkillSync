package com.ics.skillsync.ui.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.ics.skillsync.ui.components.SharedBottomBar
import com.ics.skillsync.ui.components.SharedTopBar
import com.ics.skillsync.ui.components.SharedDrawerContent
import com.ics.skillsync.ui.viewmodel.TeachingCardViewModel
import com.ics.skillsync.ui.viewmodel.ProfileViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyTeachingCardsScreen(
    navController: NavController,
    viewModel: TeachingCardViewModel = viewModel(),
    profileViewModel: ProfileViewModel = viewModel()
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val myTeachingCards by viewModel.myTeachingCards.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val currentUser by profileViewModel.currentUser.collectAsState()
    var isLoading by remember { mutableStateOf(true) }

    // Verificar rol del usuario y cargar tarjetas
    LaunchedEffect(currentUser) {
        if (currentUser?.role != "Mentor" && currentUser?.role != "Ambos roles") {
            navController.navigate("profile") {
                popUpTo("profile") { inclusive = true }
            }
        } else {
            isLoading = true
            viewModel.loadMyTeachingCards()
            delay(1000) // Dar más tiempo para que se carguen las tarjetas
            isLoading = false
        }
    }

    // Recargar tarjetas cuando se vuelve a la pantalla
    LaunchedEffect(Unit) {
        isLoading = true
        viewModel.loadMyTeachingCards()
        delay(1000) // Dar más tiempo para que se carguen las tarjetas
        isLoading = false
    }

    // Mostrar mensaje de éxito si viene de crear/editar una tarjeta
    LaunchedEffect(viewModel.uiState.collectAsState().value) {
        val state = viewModel.uiState.value
        if (state is TeachingCardViewModel.UiState.Success) {
            delay(500) // Esperar a que se complete la navegación
            snackbarHostState.showSnackbar(state.message)
            viewModel.clearUiState()
        } else if (state is TeachingCardViewModel.UiState.Error) {
            snackbarHostState.showSnackbar(state.message)
            viewModel.clearUiState()
        }
    }

    // Recargar tarjetas periódicamente
    LaunchedEffect(Unit) {
        while(true) {
            delay(5000) // Recargar cada 5 segundos
            viewModel.loadMyTeachingCards()
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            SharedDrawerContent(
                navController = navController,
                viewModel = profileViewModel,
                scope = scope,
                onDrawerClose = { scope.launch { drawerState.close() } }
            )
        }
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
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { navController.navigate("create_teaching_card") },
                    containerColor = Color(0xFF5B4DBC)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Crear tarjeta",
                        tint = Color.White
                    )
                }
            },
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                if (isLoading) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(48.dp),
                            color = Color(0xFF5B4DBC)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Cargando tus tarjetas...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF5B4DBC)
                        )
                    }
                } else if (myTeachingCards.isEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Assignment,
                            contentDescription = null,
                            modifier = Modifier.size(80.dp),
                            tint = Color(0xFF5B4DBC)
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = "No tienes tarjetas de enseñanza",
                            style = MaterialTheme.typography.titleLarge,
                            color = Color(0xFF1F2937),
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Crea una tarjeta para compartir tus conocimientos y ayudar a otros a aprender",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color(0xFF6B7280),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 32.dp)
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(vertical = 16.dp)
                    ) {
                        item {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp)
                            ) {
                                Text(
                                    text = "Mis tarjetas",
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF5B4DBC)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Gestiona tus tarjetas de enseñanza",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = Color(0xFF6B7280)
                                )
                            }
                        }
                        items(myTeachingCards) { card ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                shape = RoundedCornerShape(16.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                onClick = { navController.navigate("teaching_card/${card.id}") }
                            ) {
                                Column {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(200.dp)
                                    ) {
                                        if (card.imageUrl.isNotEmpty()) {
                                            var isLoading by remember { mutableStateOf(true) }
                                            AsyncImage(
                                                model = card.imageUrl,
                                                contentDescription = "Imagen de la tarjeta",
                                                modifier = Modifier.fillMaxSize(),
                                                contentScale = ContentScale.Crop,
                                                onLoading = { isLoading = true },
                                                onSuccess = { isLoading = false },
                                                onError = { 
                                                    isLoading = false
                                                    Log.e("MyTeachingCardsScreen", "Error loading image: ${card.imageUrl}")
                                                }
                                            )
                                            if (isLoading) {
                                                CircularProgressIndicator(
                                                    modifier = Modifier
                                                        .size(32.dp)
                                                        .align(Alignment.Center),
                                                    color = Color(0xFF5B4DBC),
                                                    strokeWidth = 3.dp
                                                )
                                            }
                                        } else {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .background(Color(0xFFF3F4F6)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Image,
                                                    contentDescription = "Sin imagen",
                                                    modifier = Modifier.size(48.dp),
                                                    tint = Color(0xFF9CA3AF)
                                                )
                                            }
                                        }
                                    }
                                    Column(
                                        modifier = Modifier.padding(20.dp)
                                    ) {
                                        Text(
                                            text = card.title,
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF1F2937)
                                        )
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Text(
                                            text = card.description,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = Color(0xFF6B7280),
                                            maxLines = 2,
                                            lineHeight = 20.sp
                                        )
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Surface(
                                                color = Color(0xFF5B4DBC).copy(alpha = 0.1f),
                                                shape = RoundedCornerShape(8.dp)
                                            ) {
                                                Text(
                                                    text = card.category,
                                                    color = Color(0xFF5B4DBC),
                                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = FontWeight.Medium
                                                )
                                            }
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Person,
                                                    contentDescription = null,
                                                    tint = Color(0xFF5B4DBC),
                                                    modifier = Modifier.size(20.dp)
                                                )
                                                Text(
                                                    text = "${card.learnerCount} estudiantes",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = Color(0xFF5B4DBC),
                                                    fontWeight = FontWeight.Medium
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
        }
    }
} 