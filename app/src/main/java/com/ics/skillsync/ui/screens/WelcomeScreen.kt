package com.ics.skillsync.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.ics.skillsync.R

@Composable
fun WelcomeScreen(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF5B4DBC))
    ) {
        // Header con logo
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_logo),
                    contentDescription = "Logo",
                    modifier = Modifier.size(32.dp)
                )
                Text(
                    text = "SkillSync",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            IconButton(onClick = { /* TODO: Menú */ }) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_menu),
                    contentDescription = "Menú",
                    tint = Color.White
                )
            }
        }

        // Contenido principal
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Intercambia\nconocimientos.",
                color = Color.White,
                fontSize = 40.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 48.sp
            )
            Text(
                text = "Crece juntos.",
                color = Color(0xFFFFD700),
                fontSize = 40.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp)
            )
            Text(
                text = "SkillSync conecta mentores y aprendices para compartir habilidades y conocimientos. Encuentra tu comunidad de aprendizaje hoy.",
                color = Color.White,
                fontSize = 16.sp,
                modifier = Modifier.padding(top = 24.dp)
            )

            // Botones
            Button(
                onClick = { navController.navigate("register_mentor") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 32.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White
                )
            ) {
                Text(
                    text = "Quiero enseñar",
                    color = Color(0xFF5B4DBC),
                    fontSize = 16.sp,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            Button(
                onClick = { navController.navigate("register_learner") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF6C5DD3)
                )
            ) {
                Text(
                    text = "Quiero aprender",
                    color = Color.White,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        }

        // Barra de navegación inferior
        NavigationBar(
            modifier = Modifier.fillMaxWidth(),
            containerColor = Color.White
        ) {
            NavigationBarItem(
                selected = true,
                onClick = { navController.navigate("explore") },
                icon = {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_explore),
                        contentDescription = "Explorar"
                    )
                },
                label = { Text("Explorar") }
            )
            NavigationBarItem(
                selected = false,
                onClick = { navController.navigate("skills") },
                icon = {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_skills),
                        contentDescription = "Habilidades"
                    )
                },
                label = { Text("Habilidades") }
            )
            NavigationBarItem(
                selected = false,
                onClick = { navController.navigate("messages") },
                icon = {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_messages),
                        contentDescription = "Mensajes"
                    )
                },
                label = { Text("Mensajes") }
            )
            NavigationBarItem(
                selected = false,
                onClick = { navController.navigate("sessions") },
                icon = {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_sessions),
                        contentDescription = "Sesiones"
                    )
                },
                label = { Text("Sesiones") }
            )
            NavigationBarItem(
                selected = false,
                onClick = { navController.navigate("profile") },
                icon = {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_profile),
                        contentDescription = "Perfil"
                    )
                },
                label = { Text("Perfil") }
            )
        }
    }
} 