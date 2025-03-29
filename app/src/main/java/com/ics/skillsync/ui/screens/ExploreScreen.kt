package com.ics.skillsync.ui.screens

import androidx.compose.foundation.Image
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
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import kotlinx.coroutines.launch
import com.ics.skillsync.ui.components.SkillCard
import com.ics.skillsync.ui.components.StepItem
import com.ics.skillsync.ui.components.SharedNavigationDrawer
import com.ics.skillsync.ui.components.SharedTopBar
import com.ics.skillsync.ui.components.SharedBottomBar
import com.ics.skillsync.ui.viewmodel.ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExploreScreen(
    navController: NavController,
    viewModel: ProfileViewModel
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    
    val skills = remember {
        listOf(
            Skill(
                id = "1",
                name = "Inglés Conversacional",
                description = "Practica inglés con hablantes nativos y mejora tu fluidez, pronunciación y confianza al hablar.",
                category = SkillCategory.IDIOMAS,
                imageUrl = "https://images.unsplash.com/photo-1546410531-bb4caa6b424d",
                mentorsCount = 1,
                learnersCount = 1
            ),
            Skill(
                id = "2",
                name = "Español para Principiantes",
                description = "Aprende vocabulario básico, gramática y frases útiles para comunicarte en español.",
                category = SkillCategory.IDIOMAS,
                imageUrl = "https://images.unsplash.com/photo-1489945052260-4f21c52268b9",
                mentorsCount = 0,
                learnersCount = 0
            ),
            Skill(
                id = "3",
                name = "Francés Básico",
                description = "Aprende los fundamentos del idioma francés, desde pronunciación hasta conversaciones simples.",
                category = SkillCategory.IDIOMAS,
                imageUrl = "https://images.unsplash.com/photo-1551866442-64e75e911c23",
                mentorsCount = 0,
                learnersCount = 1
            ),
            Skill(
                id = "4",
                name = "Desarrollo Web Frontend",
                description = "Aprende HTML, CSS, JavaScript y frameworks populares como React y Vue para crear sitios web interactivos.",
                category = SkillCategory.TECNOLOGIA,
                imageUrl = "https://images.unsplash.com/photo-1633356122544-f134324a6cee",
                mentorsCount = 1,
                learnersCount = 0
            ),
            Skill(
                id = "5",
                name = "Python para Principiantes",
                description = "Introducción a la programación con Python, desde conceptos básicos hasta aplicaciones prácticas.",
                category = SkillCategory.TECNOLOGIA,
                imageUrl = "https://images.unsplash.com/photo-1649180556628-9ba704115795",
                mentorsCount = 0,
                learnersCount = 0
            ),
            Skill(
                id = "6",
                name = "JavaScript Avanzado",
                description = "Aprende conceptos avanzados de JavaScript como promesas, async/await, y manipulación del DOM.",
                category = SkillCategory.TECNOLOGIA,
                imageUrl = "https://images.unsplash.com/photo-1592609931095-54a2168ae893",
                mentorsCount = 0,
                learnersCount = 0
            ),
            Skill(
                id = "7",
                name = "Guitarra Acústica",
                description = "Aprende a tocar guitarra desde cero o mejora tus habilidades con técnicas avanzadas de rasgueo y punteo.",
                category = SkillCategory.MUSICA,
                imageUrl = "https://images.unsplash.com/photo-1525201548942-d8732f6617a0",
                mentorsCount = 0,
                learnersCount = 0
            ),
            Skill(
                id = "8",
                name = "Piano Básico",
                description = "Aprende a leer partituras, técnicas básicas de piano y tus primeras canciones.",
                category = SkillCategory.MUSICA,
                imageUrl = "https://images.unsplash.com/photo-1552422535-c45813c61732",
                mentorsCount = 1,
                learnersCount = 0
            ),
            Skill(
                id = "9",
                name = "Canto: Técnica Vocal",
                description = "Mejora tu capacidad vocal con técnicas de respiración, proyección y control de tono.",
                category = SkillCategory.MUSICA,
                imageUrl = "https://images.unsplash.com/photo-1516450360452-9312f5e86fc7",
                mentorsCount = 1,
                learnersCount = 0
            ),
            Skill(
                id = "10",
                name = "Dibujo Artístico",
                description = "Aprende técnicas fundamentales de dibujo, proporción, sombreado y perspectiva.",
                category = SkillCategory.ARTE,
                imageUrl = "https://images.unsplash.com/photo-1605721911519-3dfeb3be25e7",
                mentorsCount = 0,
                learnersCount = 0
            ),
            Skill(
                id = "11",
                name = "Edición de Video Básica",
                description = "Domina las técnicas fundamentales de edición de video usando software accesible.",
                category = SkillCategory.ARTE,
                imageUrl = "https://images.unsplash.com/photo-1536240478700-b869070f9279",
                mentorsCount = 0,
                learnersCount = 0
            ),
            Skill(
                id = "12",
                name = "Marketing Digital",
                description = "Conoce las estrategias fundamentales para promocionar productos y servicios en el entorno digital.",
                category = SkillCategory.OTROS,
                imageUrl = "https://images.unsplash.com/photo-1460925895917-afdab827c52f",
                mentorsCount = 0,
                learnersCount = 0
            ),
            Skill(
                id = "13",
                name = "Ventas en Línea",
                description = "Aprende a crear y gestionar una tienda online y estrategias para incrementar tus ventas.",
                category = SkillCategory.OTROS,
                imageUrl = "https://images.unsplash.com/photo-1556740738-b6a63e27c4df",
                mentorsCount = 0,
                learnersCount = 0
            )
        )
    }

    var selectedCategory by remember { mutableStateOf<SkillCategory?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var filterType by remember { mutableStateOf("Todas") }

    val filteredSkills = remember(selectedCategory, searchQuery, filterType) {
        skills.filter { skill ->
            val matchesCategory = selectedCategory?.let { skill.category == it } ?: true
            val matchesSearch = skill.name.contains(searchQuery, ignoreCase = true) ||
                    skill.description.contains(searchQuery, ignoreCase = true)
            val matchesFilter = when (filterType) {
                "Quiero enseñar" -> skill.mentorsCount > 0
                "Quiero aprender" -> skill.learnersCount > 0
                else -> true
            }
            matchesCategory && matchesSearch && matchesFilter
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
            containerColor = Color.White
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFF9FAFB))
                    .padding(paddingValues)
            ) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    ) {
                        Text(
                            text = "Explora Habilidades",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 8.dp, top = 16.dp)
                        )
                        Text(
                            text = "Descubre habilidades para aprender o encuentra personas a quienes enseñar",
                            color = Color.Gray,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        TextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            placeholder = { Text("Buscar habilidades...") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Buscar",
                                    tint = Color.Gray
                                )
                            },
                            colors = TextFieldDefaults.colors(
                                unfocusedContainerColor = Color(0xFFF5F5F5),
                                focusedContainerColor = Color(0xFFF5F5F5),
                                unfocusedIndicatorColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent
                            ),
                            shape = RoundedCornerShape(8.dp),
                            singleLine = true
                        )

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(bottom = 8.dp)
                        ) {
                            item {
                                FilterChip(
                                    selected = filterType == "Todas",
                                    onClick = { filterType = "Todas" },
                                    label = { Text("Todas") },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Color(0xFF5B4DBC),
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                            item {
                                FilterChip(
                                    selected = filterType == "Quiero enseñar",
                                    onClick = { filterType = "Quiero enseñar" },
                                    label = { Text("Quiero enseñar") },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Color(0xFF5B4DBC),
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                            item {
                                FilterChip(
                                    selected = filterType == "Quiero aprender",
                                    onClick = { filterType = "Quiero aprender" },
                                    label = { Text("Quiero aprender") },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Color(0xFF5B4DBC),
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(bottom = 16.dp)
                        ) {
                            item {
                                FilterChip(
                                    selected = selectedCategory == null,
                                    onClick = { selectedCategory = null },
                                    label = { Text("Todas") },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Color(0xFF5B4DBC),
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                            items(SkillCategory.values()) { category ->
                                FilterChip(
                                    selected = selectedCategory == category,
                                    onClick = { selectedCategory = category },
                                    label = { Text(category.name.lowercase().replaceFirstChar { it.uppercase() }) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Color(0xFF5B4DBC),
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }
                    }
                }

                items(filteredSkills) { skill ->
                    SkillCard(
                        skill = skill,
                        navController = navController,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun StepItem(
    icon: Int,
    title: String,
    description: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.size(48.dp),
            shape = RoundedCornerShape(24.dp),
            color = Color(0xFFECEAFD)
        ) {
            Icon(
                painter = painterResource(id = icon),
                contentDescription = null,
                tint = Color(0xFF5B4DBC),
                modifier = Modifier.padding(12.dp)
            )
        }
        Column {
            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = description,
                fontSize = 14.sp,
                color = Color.Gray
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SkillCard(
    skill: Skill,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        ),
        shape = RoundedCornerShape(8.dp),
        onClick = { navController.navigate("skill/${skill.id}") }
    ) {
        Column {
            AsyncImage(
                model = skill.imageUrl,
                contentDescription = skill.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp),
                contentScale = ContentScale.Crop
            )
            
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = skill.name,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Surface(
                        color = when(skill.category) {
                            SkillCategory.IDIOMAS -> Color(0xFF4CAF50)
                            SkillCategory.TECNOLOGIA -> Color(0xFF2196F3)
                            SkillCategory.MUSICA -> Color(0xFFF44336)
                            SkillCategory.ARTE -> Color(0xFFE91E63)
                            else -> Color(0xFF9C27B0)
                        },
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = skill.category.name.lowercase().replaceFirstChar { it.uppercase() },
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            fontSize = 12.sp
                        )
                    }
                }

                Text(
                    text = skill.description,
                    fontSize = 14.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Group,
                            contentDescription = null,
                            tint = Color(0xFF5B4DBC),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "${skill.mentorsCount} mentores disponibles",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Group,
                            contentDescription = null,
                            tint = Color(0xFF5B4DBC),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "${skill.learnersCount} aprendices",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                }

                Button(
                    onClick = { navController.navigate("skill/${skill.id}") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF5B4DBC)
                    )
                ) {
                    Text("Ver detalles")
                }
            }
        }
    }
} 