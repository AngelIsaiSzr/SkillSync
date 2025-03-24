package com.ics.skillsync.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material.icons.sharp.*
import androidx.compose.material.icons.twotone.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.ics.skillsync.R
import com.ics.skillsync.model.Skill
import com.ics.skillsync.model.SkillCategory
import com.ics.skillsync.ui.components.SkillCard
import com.ics.skillsync.ui.components.StepItem
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val categories = remember { SkillCategory.values().toList() }
    var selectedCategory by remember { mutableStateOf<SkillCategory?>(null) }

    // Mostrar principales habilidades
    val popularSkills = remember {
        listOf(
            Skill(
                id = "1",
                name = "Inglés Conversacional",
                description = "Practica inglés con hablantes nativos y mejora tu fluidez, pronunciación y confianza al hablar.",
                category = SkillCategory.IDIOMAS,
                imageUrl = "https://images.unsplash.com/photo-1546410531-bb4caa6b424d?q=80&w=800&auto=format&fit=crop",
                mentorsCount = 5,
                learnersCount = 12
            ),
            Skill(
                id = "4",
                name = "Desarrollo Web Frontend",
                description = "Aprende HTML, CSS, JavaScript y frameworks populares como React y Vue para crear sitios web interactivos.",
                category = SkillCategory.TECNOLOGIA,
                imageUrl = "https://images.unsplash.com/photo-1633356122544-f134324a6cee?q=80&w=800&auto=format&fit=crop",
                mentorsCount = 8,
                learnersCount = 20
            ),
            Skill(
                id = "7",
                name = "Guitarra Acústica",
                description = "Aprende a tocar guitarra desde cero o mejora tus habilidades con técnicas avanzadas de rasgueo y punteo.",
                category = SkillCategory.MUSICA,
                imageUrl = "https://images.unsplash.com/photo-1525201548942-d8732f6617a0?q=80&w=800&auto=format&fit=crop",
                mentorsCount = 4,
                learnersCount = 12
            ),
            Skill(
                id = "10",
                name = "Dibujo Artístico",
                description = "Aprende técnicas de dibujo, perspectiva y composición para crear obras de arte.",
                category = SkillCategory.ARTE,
                imageUrl = "https://images.unsplash.com/photo-1605721911519-3dfeb3be25e7?q=80&w=800&auto=format&fit=crop",
                mentorsCount = 3,
                learnersCount = 9
            )
        )
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    // Logo y nombre en el drawer
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
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

                    Divider(modifier = Modifier.padding(vertical = 8.dp))

                    // Opciones del menú
                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.Search, contentDescription = "Buscar") },
                        label = { Text("Buscar") },
                        selected = false,
                        onClick = {
                            scope.launch {
                                drawerState.close()
                            }
                            navController.navigate("search")
                        }
                    )
                    
                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.DateRange, contentDescription = "Sesiones") },
                        label = { Text("Sesiones") },
                        selected = false,
                        onClick = {
                            scope.launch {
                                drawerState.close()
                            }
                            navController.navigate("sessions")
                        }
                    )
                    
                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.Settings, contentDescription = "Configuración") },
                        label = { Text("Configuración") },
                        selected = false,
                        onClick = {
                            scope.launch {
                                drawerState.close()
                            }
                            navController.navigate("settings")
                        }
                    )
                }
            }
        }
    ) {
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
                    actions = {
                        IconButton(
                            onClick = {
                                scope.launch {
                                    drawerState.open()
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Menú",
                                tint = Color(0xFF5B4DBC)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.White,
                        titleContentColor = Color(0xFF5B4DBC)
                    )
                )
            },
            bottomBar = {
                NavigationBar(
                    containerColor = Color.White
                ) {
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Home, "Inicio") },
                        label = { Text("Inicio") },
                        selected = true,
                        onClick = { }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Explore, "Explorar") },
                        label = { Text("Explorar") },
                        selected = false,
                        onClick = { navController.navigate("explore") }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Message, "Chats") },
                        label = { Text("Chats") },
                        selected = false,
                        onClick = { navController.navigate("chats") }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Person, "Perfil") },
                        label = { Text("Perfil") },
                        selected = false,
                        onClick = { navController.navigate("profile") }
                    )
                }
            }
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF5B4DBC))
                    .padding(paddingValues)
            ) {
                // Header Section
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 48.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Títulos principales
                        Column(
                            modifier = Modifier.padding(bottom = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "Intercambia\nconocimientos.",
                                color = Color.White,
                                fontSize = 40.sp,
                                fontWeight = FontWeight.Bold,
                                lineHeight = 44.sp
                            )
                            Text(
                                text = "Crece juntos.",
                                color = Color(0xFFFFD700),
                                fontSize = 40.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Descripción
                        Text(
                            text = "SkillSync conecta mentores y aprendices para compartir habilidades y conocimientos. Encuentra tu comunidad de aprendizaje hoy.",
                            color = Color.White,
                            fontSize = 18.sp,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        // Botones de acción
                        Button(
                            onClick = { navController.navigate("profile") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                "Quiero enseñar",
                                color = Color(0xFF5B4DBC),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        OutlinedButton(
                            onClick = { navController.navigate("profile") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = Color(0xFF6C5DD3),
                                contentColor = Color.White
                            ),
                            border = BorderStroke(1.dp, Color.White),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                "Quiero aprender",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        // Espaciado extra entre botones e imagen
                        Spacer(modifier = Modifier.height(12.dp))

                        // Imagen de estudiantes
                        AsyncImage(
                            model = "https://images.unsplash.com/photo-1531482615713-2afd69097998",
                            contentDescription = "Personas estudiando",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .clip(RoundedCornerShape(16.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }
                }

                // How it works section
                item {
                    Surface(
                        color = Color.White,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color.White
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "¿Cómo funciona SkillSync?",
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(bottom = 8.dp, top = 10.dp)
                                    )
                                    Text(
                                        text = "Tres pasos simples para comenzar a intercambiar habilidades",
                                        fontSize = 16.sp,
                                        color = Color.Gray,
                                        modifier = Modifier.padding(bottom = 24.dp),
                                        textAlign = TextAlign.Center
                                    )
                                    
                                    StepItem(
                                        icon = R.drawable.ic_profile_create,
                                        title = "Crea tu perfil",
                                        description = "Regístrate y configura tu perfil indicando tus habilidades para enseñar o aprender."
                                    )
                                    
                                    StepItem(
                                        icon = R.drawable.ic_search,
                                        title = "Encuentra coincidencias",
                                        description = "Busca personas con intereses complementarios a los tuyos."
                                    )
                                    
                                    StepItem(
                                        icon = R.drawable.ic_calendar,
                                        title = "Programa sesiones",
                                        description = "Coordina y agenda sesiones de intercambio de conocimientos."
                                    )
                                }
                            }
                        }
                    }
                }

                // Popular skills section
                item {
                    Surface(
                        color = Color(0xFFF9FAFB),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(vertical = 24.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Habilidades populares",
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Text(
                                text = "Explora las categorías más solicitadas en nuestra comunidad",
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color.Gray,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )

                            // Lista de habilidades populares
                            popularSkills.forEach { skill ->
                                SkillCard(
                                    skill = skill,
                                    navController = navController,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                                )
                            }

                            // Botón Ver más habilidades
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            OutlinedButton(
                                onClick = { navController.navigate("explore") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp),
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, Color(0xFF5B4DBC)),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = Color(0xFF5B4DBC)
                                )
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "Ver más habilidades",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Icon(
                                        imageVector = Icons.Default.ArrowForward,
                                        contentDescription = "Ver más",
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Testimonials section
                item {
                    Surface(
                        color = Color.White,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(vertical = 24.dp)
                        ) {
                            Text(
                                text = "Lo que dice nuestra comunidad",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )

                            Text(
                                text = "Experiencias reales de miembros de SkillSync",
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color.Gray,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )

                            // Testimonios
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color.White
                                ),
                                elevation = CardDefaults.cardElevation(
                                    defaultElevation = 2.dp
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(bottom = 8.dp)
                                    ) {
                                        AsyncImage(
                                            model = "https://images.unsplash.com/photo-1633332755192-727a05c4013d",
                                            contentDescription = "Miguel Ángel",
                                            modifier = Modifier
                                                .size(48.dp)
                                                .clip(CircleShape),
                                            contentScale = ContentScale.Crop
                                        )
                                        Column(
                                            modifier = Modifier
                                                .padding(start = 12.dp)
                                                .weight(1f)
                                        ) {
                                            Text(
                                                text = "Miguel Ángel",
                                                fontWeight = FontWeight.Bold
                                            )
                                            Row {
                                                repeat(5) {
                                                    Icon(
                                                        imageVector = Icons.Default.Star,
                                                        contentDescription = null,
                                                        tint = Color(0xFFFFD700),
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                    Text(
                                        text = "\"Gracias a SkillSync pude intercambiar mis conocimientos de programación por clases de francés. ¡Ha sido una experiencia increíble y he conocido personas fantásticas!\"",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = Color.Gray,
                                        modifier = Modifier.padding(bottom = 8.dp)
                                    )
                                }
                            }

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color.White
                                ),
                                elevation = CardDefaults.cardElevation(
                                    defaultElevation = 2.dp
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(bottom = 8.dp)
                                    ) {
                                        AsyncImage(
                                            model = "https://images.unsplash.com/photo-1494790108377-be9c29b29330",
                                            contentDescription = "Laura Martínez",
                                            modifier = Modifier
                                                .size(48.dp)
                                                .clip(CircleShape),
                                            contentScale = ContentScale.Crop
                                        )
                                        Column(
                                            modifier = Modifier
                                                .padding(start = 12.dp)
                                                .weight(1f)
                                        ) {
                                            Text(
                                                text = "Laura Martínez",
                                                fontWeight = FontWeight.Bold
                                            )
                                            Row {
                                                repeat(5) {
                                                    Icon(
                                                        imageVector = Icons.Default.Star,
                                                        contentDescription = null,
                                                        tint = Color(0xFFFFD700),
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                    Text(
                                        text = "\"Como profesora de yoga, quería aprender fotografía. En SkillSync encontré el intercambio perfecto y ahora puedo documentar mis clases con fotos profesionales.\"",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = Color.Gray,
                                        modifier = Modifier.padding(bottom = 8.dp)
                                    )
                                }
                            }

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color.White
                                ),
                                elevation = CardDefaults.cardElevation(
                                    defaultElevation = 2.dp
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(bottom = 8.dp)
                                    ) {
                                        AsyncImage(
                                            model = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d",
                                            contentDescription = "Carlos Rodríguez",
                                            modifier = Modifier
                                                .size(48.dp)
                                                .clip(CircleShape),
                                            contentScale = ContentScale.Crop
                                        )
                                        Column(
                                            modifier = Modifier
                                                .padding(start = 12.dp)
                                                .weight(1f)
                                        ) {
                                            Text(
                                                text = "Carlos Rodríguez",
                                                fontWeight = FontWeight.Bold
                                            )
                                            Row {
                                                repeat(4) {
                                                    Icon(
                                                        imageVector = Icons.Default.Star,
                                                        contentDescription = null,
                                                        tint = Color(0xFFFFD700),
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                                Icon(
                                                    imageVector = Icons.Default.StarHalf,
                                                    contentDescription = null,
                                                    tint = Color(0xFFFFD700),
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    }
                                    Text(
                                        text = "\"Buscaba alguien que me enseñara a tocar la guitarra y encontré una comunidad. Ahora no solo toco, sino que he aprendido mucho sobre producción musical.\"",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = Color.Gray,
                                        modifier = Modifier.padding(bottom = 8.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Call to action section
                item {
                    Surface(
                        color = Color(0xFF5B4DBC),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "¿Listo para comenzar tu intercambio de habilidades?",
                                style = MaterialTheme.typography.headlineMedium,
                                color = Color.White,
                                textAlign = TextAlign.Center,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )

                            Text(
                                text = "Únete a miles de personas que ya están compartiendo conocimientos en nuestra plataforma.",
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color.White,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(bottom = 24.dp)
                            )

                            Button(
                                onClick = { navController.navigate("explore") },
                                modifier = Modifier
                                    .width(200.dp)
                                    .height(44.dp)
                                    .align(Alignment.CenterHorizontally),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.White
                                ),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = "Explorar habilidades",
                                    color = Color(0xFF5B4DBC),
                                    fontSize = 14.sp,
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