package com.ankivoice.agent.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ankivoice.agent.ui.theme.*

@Composable
fun DiscoverScreen(onStartSession: (Long) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp)
    ) {
        HeaderSection("Discover", "Featured Voice Collections")
        
        Spacer(modifier = Modifier.height(24.dp))
        
        DiscoverCard(
            title = "Daily Expressions",
            description = "Master common phrases with natural conversation practice.",
            gradient = listOf(Color(0xFF6366F1), Color(0xFF8B5CF6)),
            onClick = { onStartSession(1L) }
        )
        
        Spacer(modifier = Modifier.height(20.dp))
        
        DiscoverCard(
            title = "Tech Terminology",
            description = "Stay sharp on the latest industry jargon and concepts.",
            gradient = listOf(Color(0xFFEC4899), Color(0xFFF43F5E)),
            onClick = { onStartSession(2L) }
        )
    }
}

@Composable
fun DiscoverCard(title: String, description: String, gradient: List<Color>, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(32.dp))
            .clickable { onClick() },
        color = Color.Transparent,
        shadowElevation = 12.dp
    ) {
        Box(
            modifier = Modifier
                .background(Brush.linearGradient(gradient))
                .padding(28.dp)
        ) {
            Column {
                Text(title, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black), color = Color.White)
                Spacer(modifier = Modifier.height(8.dp))
                Text(description, style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.8f))
                Spacer(modifier = Modifier.height(24.dp))
                Surface(
                    color = Color.White.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("START PRACTICE", modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), 
                        style = MaterialTheme.typography.labelLarge, color = Color.White)
                }
            }
        }
    }
}

@Composable
fun HistoryScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp)
    ) {
        HeaderSection("Activity", "Your study progress")
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Premium Stat Card
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(32.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            shadowElevation = 2.dp
        ) {
            Row(modifier = Modifier.padding(28.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                StatItem("STREAK", "12 Days", SecondaryAccent)
                StatItem("CARDS", "450", PrimaryColor)
                StatItem("LEVEL", "Learner", AccentColor)
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        Text("RECENT SESSIONS", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(16.dp))
        
        listOf("Medical Terms", "Italian Basics", "AWS Cloud Concepts").forEach { session ->
            HistoryItem(session)
        }
    }
}

@Composable
fun StatItem(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black), color = color)
    }
}

@Composable
fun HistoryItem(name: String) {
    Surface(
        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(PrimaryColor))
            Spacer(modifier = Modifier.width(16.dp))
            Text(name, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold))
        }
    }
}

@Composable
fun SettingsScreen(themeViewModel: com.ankivoice.agent.ui.viewmodel.ThemeViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp)
    ) {
        HeaderSection("Settings", "Customize your experience")
        
        Spacer(modifier = Modifier.height(32.dp))
        
        SettingToggle("Dark Mode", themeViewModel.isDarkMode.value) { themeViewModel.toggleTheme() }
        SettingItem("Voice Speed", "1.0x")
        SettingItem("Haptic Feedback", "On")
        SettingItem("Version", "1.0.0 (Open Source)")
    }
}

@Composable
fun HeaderSection(title: String, subtitle: String) {
    Column {
        Text(title, style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Black, letterSpacing = (-1.5).sp))
        Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun SettingToggle(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Row(modifier = Modifier.padding(24.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold))
            Switch(checked = checked, onCheckedChange = onCheckedChange, colors = SwitchDefaults.colors(checkedThumbColor = PrimaryColor))
        }
    }
}

@Composable
fun SettingItem(label: String, value: String) {
    Surface(
        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Row(modifier = Modifier.padding(24.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold))
            Text(value, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
