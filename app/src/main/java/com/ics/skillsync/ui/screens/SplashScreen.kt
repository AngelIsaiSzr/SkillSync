package com.ics.skillsync.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(navController: NavController) {
    var startAnimation by remember { mutableStateOf(false) }
    val circleAlpha by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 1000)
    )
    
    val rightArrowOffset by animateFloatAsState(
        targetValue = if (startAnimation) 0f else -100f,
        animationSpec = tween(durationMillis = 1000, easing = LinearOutSlowInEasing)
    )
    
    val leftArrowOffset by animateFloatAsState(
        targetValue = if (startAnimation) 0f else 100f,
        animationSpec = tween(durationMillis = 1000, easing = LinearOutSlowInEasing)
    )
    
    val textAlpha by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 1000, delayMillis = 1000)
    )

    LaunchedEffect(key1 = true) {
        startAnimation = true
        delay(2500)
        navController.navigate("home") {
            popUpTo("splash") { inclusive = true }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Spacer(modifier = Modifier.weight(1f))
            
            Box(
                modifier = Modifier.size(200.dp),
                contentAlignment = Alignment.Center
            ) {
                // Círculo morado de fondo
                Canvas(
                    modifier = Modifier
                        .size(200.dp)
                        .alpha(circleAlpha)
                ) {
                    drawCircle(
                        color = Color(0xFF5B4DBC),
                        radius = size.minDimension / 2
                    )
                }
                
                // Flecha superior (izquierda a derecha)
                Canvas(
                    modifier = Modifier
                        .size(120.dp)
                        .offset(x = rightArrowOffset.dp, y = -20.dp)
                ) {
                    val path = Path().apply {
                        // Cuerpo rectangular de la flecha
                        moveTo(size.width * 0.2f, size.height * 0.45f)
                        lineTo(size.width * 0.6f, size.height * 0.45f)
                        lineTo(size.width * 0.6f, size.height * 0.55f)
                        lineTo(size.width * 0.2f, size.height * 0.55f)
                        close()
                        
                        // Punta de la flecha
                        moveTo(size.width * 0.6f, size.height * 0.35f)
                        lineTo(size.width * 0.8f, size.height * 0.5f)
                        lineTo(size.width * 0.6f, size.height * 0.65f)
                        close()
                    }
                    drawPath(path = path, color = Color.White, style = Fill)
                }
                
                // Flecha inferior (derecha a izquierda)
                Canvas(
                    modifier = Modifier
                        .size(120.dp)
                        .offset(x = leftArrowOffset.dp, y = 20.dp)
                ) {
                    val path = Path().apply {
                        // Cuerpo rectangular de la flecha
                        moveTo(size.width * 0.4f, size.height * 0.45f)
                        lineTo(size.width * 0.8f, size.height * 0.45f)
                        lineTo(size.width * 0.8f, size.height * 0.55f)
                        lineTo(size.width * 0.4f, size.height * 0.55f)
                        close()
                        
                        // Punta de la flecha
                        moveTo(size.width * 0.4f, size.height * 0.35f)
                        lineTo(size.width * 0.2f, size.height * 0.5f)
                        lineTo(size.width * 0.4f, size.height * 0.65f)
                        close()
                    }
                    drawPath(path = path, color = Color.White, style = Fill)
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .alpha(textAlpha)
                    .padding(bottom = 48.dp)
            ) {
                Text(
                    text = "By",
                    color = Color.Black,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "ICS",
                    color = Color(0xFF5B4DBC),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
} 