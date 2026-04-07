package com.ankivoice.agent

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.core.content.ContextCompat
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import com.ankivoice.agent.ui.theme.AnkiVoiceTheme
import com.ankivoice.agent.ui.theme.PrimaryColor
import com.ankivoice.agent.ui.screens.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AnkiVoiceApp()
        }
    }
}

@Composable
fun AnkiVoiceApp(themeViewModel: com.ankivoice.agent.ui.viewmodel.ThemeViewModel = androidx.lifecycle.viewmodel.compose.viewModel()) {
    val navController = rememberNavController()
    val isDark = themeViewModel.isDarkMode.value
    
    AnkiVoiceTheme(darkTheme = isDark) {
        val context = LocalContext.current
        
        var permissionsGranted by remember {
            mutableStateOf(
                ContextCompat.checkSelfPermission(context, "com.ichi2.anki.permission.READ_WRITE_DATABASE") == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
            )
        }

        val permissionLauncher = rememberLauncherForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { results ->
            permissionsGranted = results.values.all { it }
        }

        LaunchedEffect(Unit) {
            if (!permissionsGranted) {
                permissionLauncher.launch(
                    arrayOf(
                        "com.ichi2.anki.permission.READ_WRITE_DATABASE",
                        Manifest.permission.RECORD_AUDIO
                    )
                )
            }
        }

        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            bottomBar = {
                if (permissionsGranted) {
                    MainNavigationBar(navController)
                }
            }
        ) { innerPadding ->
            Box(modifier = Modifier.padding(innerPadding)) {
                if (!permissionsGranted) {
                    PermissionRequiredOverlay {
                        permissionLauncher.launch(
                            arrayOf(
                                "com.ichi2.anki.permission.READ_WRITE_DATABASE",
                                Manifest.permission.RECORD_AUDIO
                            )
                        )
                    }
                } else {
                    AppNavigation(navController, themeViewModel, permissionLauncher)
                }
            }
        }
    }
}

@Composable
fun MainNavigationBar(navController: androidx.navigation.NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 24.dp)
            .clip(RoundedCornerShape(32.dp)),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 16.dp,
        tonalElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val items = listOf(
                Triple("decks", "Home", Icons.Default.Home),
                Triple("discover", "Discover", Icons.Default.PlayArrow),
                Triple("history", "Activity", Icons.Default.DateRange),
                Triple("settings", "Settings", Icons.Default.Settings)
            )

            items.forEach { (route, label, icon) ->
                val selected = currentDestination?.hierarchy?.any { it.route == route } == true
                
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(24.dp))
                        .background(if (selected) PrimaryColor.copy(alpha = 0.15f) else Color.Transparent)
                        .clickable {
                            navController.navigate(route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            icon,
                            contentDescription = label,
                            tint = if (selected) PrimaryColor else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(26.dp)
                        )
                        if (selected) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = PrimaryColor,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PermissionRequiredOverlay(onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Connect AnkiDroid", style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black))
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            "AnkiVoice needs access to your Anki database to generate voice sessions from your cards.",
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = onRetry,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) {
            Text("Grant Access")
        }
    }
}

@Composable
fun AppNavigation(
    navController: androidx.navigation.NavHostController,
    themeViewModel: com.ankivoice.agent.ui.viewmodel.ThemeViewModel,
    permissionLauncher: androidx.activity.result.ActivityResultLauncher<Array<String>>,
    viewModel: com.ankivoice.agent.ui.viewmodel.PodcastAgentViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    NavHost(navController = navController, startDestination = "decks") {
        composable("decks") {
            DeckSelectionScreen(
                onDeckSelected = { deckId, deckName -> navController.navigate("deck_config/$deckId/$deckName") },
                onSyncRequest = { 
                    permissionLauncher.launch(
                        arrayOf(
                            "com.ichi2.anki.permission.READ_WRITE_DATABASE",
                            Manifest.permission.RECORD_AUDIO
                        )
                    )
                }
            )
        }
        composable("deck_config/{deckId}/{deckName}") { backStackEntry ->
            val deckId = backStackEntry.arguments?.getString("deckId")?.toLongOrNull() ?: 0L
            val deckName = backStackEntry.arguments?.getString("deckName") ?: "Selected Deck"
            DeckConfigScreen(
                deckName = deckName, 
                onStartSession = { size ->
                    viewModel.configureSession(deckId, size)
                    navController.navigate("player/$deckId")
                },
                onBack = { navController.popBackStack() }
            )
        }
        composable("player/{deckId}") { backStackEntry ->
            val deckId = backStackEntry.arguments?.getString("deckId")?.toLongOrNull() ?: 0L
            PodcastPlayerScreen(deckId = deckId, viewModel = viewModel)
        }
        composable("discover") {
            DiscoverScreen(onStartSession = { deckId -> navController.navigate("deck_config/$deckId") })
        }
        composable("history") { HistoryScreen() }
        composable("settings") { SettingsScreen(themeViewModel = themeViewModel) }
    }
}
