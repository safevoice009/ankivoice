package com.ankivoice.agent.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.style.TextAlign
import com.ankivoice.agent.ui.theme.*
import com.ankivoice.agent.ui.viewmodel.PodcastAgentViewModel

@Composable
fun DeckSelectionScreen(
    viewModel: PodcastAgentViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    onDeckSelected: (Long, String) -> Unit,
    onSyncRequest: () -> Unit
) {
    val repository = com.ankivoice.agent.data.AnkiRepository(androidx.compose.ui.platform.LocalContext.current)
    var decks by remember { mutableStateOf(emptyList<Deck>()) }
    
    LaunchedEffect(Unit) {
        decks = repository.getDeckList().map { Deck(it.id, it.name) }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Column(modifier = Modifier.padding(24.dp).padding(top = 16.dp)) {
                Text(
                    text = "AnkiVoice",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = (-1.5).sp
                    ),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Ready to elevate your learning?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp)
        ) {
            if (decks.isEmpty()) {
                EmptyState(onSyncRequest)
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                    contentPadding = PaddingValues(bottom = 32.dp)
                ) {
                    itemsIndexed(decks) { index, deck ->
                        PremiumDeckCard(deck, index, onDeckSelected)
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyState(onSyncRequest: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().padding(top = 40.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Your library is empty",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Connect with AnkiDroid to see your decks and start your session.",
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = onSyncRequest,
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                Text("Sync AnkiDroid", style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

@Composable
fun PremiumDeckCard(deck: Deck, index: Int, onDeckSelected: (Long, String) -> Unit) {
    val gradientColors = when (index % 3) {
        1 -> listOf(Color(0xFF6366F1), Color(0xFFA855F7)) // Indigo to Purple
        2 -> listOf(Color(0xFFF43F5E), Color(0xFFFB923C)) // Rose to Orange
        else -> listOf(Color(0xFF10B981), Color(0xFF3B82F6)) // Emerald to Blue
    }
    
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .clickable { onDeckSelected(deck.id, deck.name) },
        shadowElevation = 8.dp,
        tonalElevation = 4.dp
    ) {
        Box(
            modifier = Modifier
                .background(
                    brush = androidx.compose.ui.graphics.Brush.linearGradient(
                        colors = gradientColors
                    )
                )
                .padding(24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = deck.name,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = (-0.5).sp
                        ),
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Voice Session Ready",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
                
                Surface(
                    modifier = Modifier.size(44.dp),
                    color = Color.White.copy(alpha = 0.2f),
                    shape = androidx.compose.foundation.shape.CircleShape
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("→", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

data class Deck(val id: Long, val name: String)
