package com.ankivoice.agent.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ankivoice.agent.ui.viewmodel.PodcastAgentViewModel
import com.ankivoice.agent.ui.viewmodel.StudyUiState
import com.ankivoice.agent.ui.theme.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Button
import androidx.compose.foundation.shape.RoundedCornerShape

@Composable
fun PodcastPlayerScreen(
    deckId: Long,
    viewModel: PodcastAgentViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(deckId) {
        viewModel.startSession(deckId)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        // Background Ambient Glow
        val ambientColor by animateColorAsState(
            targetValue = when (uiState) {
                is StudyUiState.Listening -> SecondaryAccent.copy(alpha = 0.15f)
                is StudyUiState.SpeakingQuestion -> AccentColor.copy(alpha = 0.15f)
                is StudyUiState.SpeakingAnswer -> SecondaryAccent.copy(alpha = 0.15f)
                else -> Color.Transparent
            },
            animationSpec = tween(1000),
            label = "AmbientColor"
        )
        
        Box(modifier = Modifier.fillMaxSize().background(ambientColor))

        Column(
            modifier = Modifier.fillMaxSize().padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            AnimatedContent(
                targetState = uiState,
                transitionSpec = {
                    fadeIn(tween(600)) + scaleIn(initialScale = 0.9f) togetherWith 
                    fadeOut(tween(400)) + scaleOut(targetScale = 1.1f)
                },
                label = "StateTransition"
            ) { state ->
                StudyStateContent(
                    state = state, 
                    textColor = MaterialTheme.colorScheme.onBackground,
                    onRetry = { viewModel.initializeEngines() }
                )
            }
        }

        // Orb Visualizer
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 80.dp)
        ) {
            Orb(uiState)
        }
    }
}

@Composable
fun StudyStateContent(state: StudyUiState, textColor: Color, onRetry: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        when (state) {
            is StudyUiState.Loading, is StudyUiState.Configuring -> {
                Text("INITIALIZING AI AGENT...", style = StatusTextStyle.copy(color = textColor.copy(alpha = 0.5f)))
                Spacer(modifier = Modifier.height(16.dp))
                CircularProgressIndicator(color = AccentColor)
            }
            is StudyUiState.Ready -> {
                Text("READY", style = MassiveTextStyle.copy(color = textColor))
            }
            is StudyUiState.SpeakingQuestion -> {
                Text("LISTEN", style = StatusTextStyle, color = AccentColor)
                Spacer(modifier = Modifier.height(24.dp))
                Text(state.text.uppercase(), style = ContentTextStyle.copy(color = textColor))
            }
            is StudyUiState.Listening -> {
                Text("YOUR TURN", style = MassiveTextStyle, color = SecondaryAccent)
                Text("TRANSCRIBING...", style = StatusTextStyle.copy(fontSize = 14.sp, color = textColor.copy(alpha = 0.5f)))
            }
            is StudyUiState.SpeakingAnswer -> {
                Text("THE TRUTH", style = StatusTextStyle, color = SecondaryAccent)
                Spacer(modifier = Modifier.height(24.dp))
                Text(state.text.uppercase(), style = ContentTextStyle.copy(color = textColor))
            }
            is StudyUiState.Finished -> {
                Text("DONE", style = MassiveTextStyle.copy(color = textColor))
                Text("SESSION COMPLETE OR NO CARDS FOUND", style = StatusTextStyle.copy(color = textColor.copy(alpha = 0.5f)))
            }
            is StudyUiState.Error -> {
                Text("ERROR", style = MassiveTextStyle, color = Color.Red)
                Text(state.message, style = StatusTextStyle.copy(color = textColor.copy(alpha = 0.7f)), textAlign = TextAlign.Center)
                Spacer(modifier = Modifier.height(32.dp))
                Button(
                    onClick = onRetry,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("RETRY INITIALIZATION")
                }
            }
        }
    }
}

@Composable
fun Orb(state: StudyUiState) {
    val infiniteTransition = rememberInfiniteTransition()
    
    val sizeScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (state is StudyUiState.Listening) 1.5f else 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    val blurRadius by animateDpAsState(
        targetValue = if (state is StudyUiState.Listening) 50.dp else 25.dp
    )

    val color by animateColorAsState(
        targetValue = when(state) {
            is StudyUiState.Listening -> SecondaryAccent
            is StudyUiState.SpeakingQuestion, is StudyUiState.SpeakingAnswer -> AccentColor
            else -> MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f)
        }
    )

    Box(
        modifier = Modifier
            .size(120.dp)
            .blur(blurRadius)
            .scale(sizeScale)
            .clip(CircleShape)
            .background(
                Brush.radialGradient(
                    colors = listOf(color, Color.Transparent)
                )
            )
    )
}

val MassiveTextStyle = androidx.compose.ui.text.TextStyle(
    fontSize = 64.sp,
    fontWeight = FontWeight.Black,
    letterSpacing = (-2).sp,
    textAlign = TextAlign.Center
)

val ContentTextStyle = androidx.compose.ui.text.TextStyle(
    fontSize = 32.sp,
    fontWeight = FontWeight.Bold,
    letterSpacing = (-1).sp,
    lineHeight = 40.sp,
    textAlign = TextAlign.Center
)

val StatusTextStyle = androidx.compose.ui.text.TextStyle(
    fontSize = 18.sp,
    fontWeight = FontWeight.Medium,
    letterSpacing = 4.sp,
    textAlign = TextAlign.Center
)
