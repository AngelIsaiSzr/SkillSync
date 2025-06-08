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
import com.ics.skillsync.ui.viewmodel.TeachingCardViewModel
import com.ics.skillsync.model.TeachingCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExploreScreen(
    navController: NavController,
    viewModel: ProfileViewModel,
    teachingCardViewModel: TeachingCardViewModel
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val teachingCards by teachingCardViewModel.teachingCards.collectAsState()
    
    // Convertir TeachingCards a Skills para mantener la compatibilidad
    val skills = teachingCards.map { card ->
            Skill(
            id = card.id,
            name = card.title,
            description = card.description,
            category = SkillCategory.fromString(card.category),
            imageUrl = card.imageUrl,
            mentorsCount = 1, // Mantenemos esto ya que cada tarjeta tiene un mentor
            learnersCount = card.learnerCount,
            mentorName = card.mentorName,
            level = when (card.experienceLevel) {
                TeachingCard.ExperienceLevel.PRINCIPIANTE -> 1
                TeachingCard.ExperienceLevel.BASICO -> 2
                TeachingCard.ExperienceLevel.INTERMEDIO -> 3
                TeachingCard.ExperienceLevel.AVANZADO -> 4
                TeachingCard.ExperienceLevel.EXPERTO -> 5
            }
        )
    }

    var selectedCategory by remember { mutableStateOf<SkillCategory?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var filterType by remember { mutableStateOf("Todas") }

    val filteredSkills = remember(selectedCategory, searchQuery, filterType, skills) {
        skills.filter { skill ->
            val matchesCategory = selectedCategory?.let { skill.category == it } ?: true
            val matchesSearch = skill.name.contains(searchQuery, ignoreCase = true) ||
                    skill.description.contains(searchQuery, ignoreCase = true)
            val matchesFilter = when (filterType) {
                "Principiante" -> skill.level == 1
                "Básico" -> skill.level == 2
                "Intermedio" -> skill.level == 3
                "Avanzado" -> skill.level == 4
                "Experto" -> skill.level == 5
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
                                    selected = filterType == "Principiante",
                                    onClick = { filterType = "Principiante" },
                                    label = { Text("Principiante") },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Color(0xFF5B4DBC),
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                            item {
                                FilterChip(
                                    selected = filterType == "Básico",
                                    onClick = { filterType = "Básico" },
                                    label = { Text("Básico") },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Color(0xFF5B4DBC),
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                            item {
                                FilterChip(
                                    selected = filterType == "Intermedio",
                                    onClick = { filterType = "Intermedio" },
                                    label = { Text("Intermedio") },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Color(0xFF5B4DBC),
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                            item {
                                FilterChip(
                                    selected = filterType == "Avanzado",
                                    onClick = { filterType = "Avanzado" },
                                    label = { Text("Avanzado") },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Color(0xFF5B4DBC),
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                            item {
                                FilterChip(
                                    selected = filterType == "Experto",
                                    onClick = { filterType = "Experto" },
                                    label = { Text("Experto") },
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
                                    label = { Text(category.getDisplayName()) },
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

    LaunchedEffect(Unit) {
        teachingCardViewModel.refreshTeachingCards()
    }
}