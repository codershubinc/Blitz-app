package com.quazaar.remote

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quazaar.remote.ui.theme.QuazaarTheme
import kotlinx.coroutines.delay

class SplashActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            QuazaarTheme {
                SplashScreen {
                    // Navigate to MainActivity after animation completes
                    startActivity(Intent(this@SplashActivity, MainActivity::class.java))
                    finish()
                }
            }
        }
    }
}

@Composable
fun SplashScreen(onAnimationComplete: () -> Unit) {
    val projectName = "QUAZAAR"
    var displayedText by remember { mutableStateOf("") }
    var isAnimationComplete by remember { mutableStateOf(false) }
    val scaleAnimation = remember { Animatable(0.5f) }

    LaunchedEffect(Unit) {
        // Animate scale
        scaleAnimation.animateTo(1f, animationSpec = tween(800, easing = LinearEasing))

        // Type out text
        projectName.forEach { char ->
            displayedText += char
            delay(150) // Delay between each character (typing speed)
        }
        delay(500) // Pause after full text is displayed
        isAnimationComplete = true
        delay(800) // Wait before transitioning
        onAnimationComplete()
    }

    val cursorAlpha = remember { Animatable(1f) }

    LaunchedEffect(isAnimationComplete.not()) {
        while (!isAnimationComplete) {
            cursorAlpha.animateTo(0f, animationSpec = tween(500, easing = LinearEasing))
            cursorAlpha.animateTo(1f, animationSpec = tween(500, easing = LinearEasing))
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0a0e27),
                        Color(0xFF1a1f3a),
                        Color(0xFF0a0e27)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            // Animated decorative circles
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0xFF6366f1).copy(alpha = 0.3f),
                                Color(0xFF6366f1).copy(alpha = 0.1f),
                                Color.Transparent
                            )
                        ),
                        shape = androidx.compose.foundation.shape.CircleShape
                    )
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Typing animation text
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .scale(scaleAnimation.value)
            ) {
                Text(
                    text = displayedText,
                    fontSize = 56.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF6366f1),
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 3.sp
                )

                // Blinking cursor
                if (!isAnimationComplete) {
                    Text(
                        text = "|",
                        fontSize = 56.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF6366f1).copy(alpha = cursorAlpha.value),
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            // Subtitle
            Text(
                text = "Remote Control",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFFa5b4fc),
                letterSpacing = 2.sp
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Loading indicator dots (subtle animation)
            if (!isAnimationComplete) {
                LoadingDots()
            }
        }
    }
}

@Composable
fun LoadingDots() {
    val animationDuration = 1000
    val delay1 = remember { Animatable(0f) }
    val delay2 = remember { Animatable(0f) }
    val delay3 = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        delay1.animateTo(1f, animationSpec = tween(animationDuration, delayMillis = 0))
        delay1.snapTo(0f)
    }

    LaunchedEffect(Unit) {
        delay(300)
        delay2.animateTo(1f, animationSpec = tween(animationDuration, delayMillis = 0))
        delay2.snapTo(0f)
    }

    LaunchedEffect(Unit) {
        delay(600)
        delay3.animateTo(1f, animationSpec = tween(animationDuration, delayMillis = 0))
        delay3.snapTo(0f)
    }

    Row(
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier.padding(bottom = 48.dp)
    ) {
        repeat(3) { index ->
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(
                        color = Color(0xFF6366f1).copy(alpha = 0.6f),
                        shape = androidx.compose.foundation.shape.CircleShape
                    )
                    .padding(4.dp)
            )
        }
    }
}


