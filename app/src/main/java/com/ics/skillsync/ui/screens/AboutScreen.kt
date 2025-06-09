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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(
    navController: NavController,
    viewModel: ProfileViewModel
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Logo y Nombre
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF5B4DBC)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.SwapHoriz,
                        contentDescription = "Logo",
                        tint = Color.White,
                        modifier = Modifier.size(60.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "SkillSync",
                    style = MaterialTheme.typography.headlineLarge,
                    color = Color(0xFF5B4DBC),
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "v1.0.0",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color(0xFF6B7280)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Intercambia conocimientos. Crece juntos.",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color(0xFF5B4DBC),
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Contenido
                AboutSection(
                    title = "Nuestra Historia",
                    content = "Vivimos en una era donde el conocimiento es el recurso más valioso y la educación " +
                            "ha trascendido las aulas tradicionales para convertirse en un proceso continuo y " +
                            "global. En un mundo donde la tecnología ha acortado distancias y facilitado la " +
                            "comunicación, el aprendizaje ya no debe ser un privilegio limitado a unos pocos, " +
                            "sino una oportunidad accesible para todos. Es bajo esta premisa que nace " +
                            "SkillSync, una plataforma diseñada para conectar a personas de todo el mundo " +
                            "con un mismo propósito: aprender y enseñar."
                )

                AboutSection(
                    title = "Misión",
                    content = "Proporcionar una plataforma accesible y fácil de usar, donde los usuarios puedan " +
                            "encontrar y compartir conocimientos, habilidades y experiencias, promoviendo la " +
                            "educación continua y el crecimiento personal."
                )

                AboutSection(
                    title = "Visión",
                    content = "Ser la plataforma líder en intercambio de habilidades y conocimientos, conectando " +
                            "a personas de todo el mundo y fomentando una comunidad de aprendizaje y " +
                            "crecimiento mutuo."
                )

                AboutSection(
                    title = "Objetivo",
                    content = "Facilitar la conexión entre personas que desean aprender y enseñar habilidades, " +
                            "fomentando el intercambio de conocimientos y el desarrollo personal."
                )

                AboutSection(
                    title = "El poder del intercambio de conocimientos",
                    content = "La educación formal cumple un papel crucial en la formación de las personas, pero " +
                            "muchas veces deja vacíos que solo pueden ser llenados a través del intercambio " +
                            "de experiencias y conocimientos prácticos. SkillSync se presenta como un puente " +
                            "entre quienes buscan aprender una nueva habilidad y aquellos que desean " +
                            "compartir su experiencia. No se trata únicamente de recibir información, sino de " +
                            "establecer conexiones significativas que fomenten el crecimiento mutuo.\n\n" +
                            "En esta plataforma, cualquier persona puede ser mentor o aprendiz. No es " +
                            "necesario ser un experto reconocido para compartir conocimientos valiosos; basta " +
                            "con la disposición de enseñar desde la propia experiencia. Aprender y enseñar " +
                            "son dos caras de la misma moneda, y SkillSync permite que ambas se " +
                            "complementen de manera fluida y accesible."
                )

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun AboutSection(
    title: String,
    content: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = Color(0xFF5B4DBC),
                fontWeight = FontWeight.Bold
            )
            Text(
                text = content,
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF374151),
                lineHeight = 24.sp
            )
        }
    }
} 