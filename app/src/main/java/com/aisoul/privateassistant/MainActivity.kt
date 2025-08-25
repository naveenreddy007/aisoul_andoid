package com.aisoul.privateassistant

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.aisoul.privateassistant.ui.screens.chat.ChatScreen
import com.aisoul.privateassistant.ui.screens.models.ModelsScreen
import com.aisoul.privateassistant.ui.screens.privacy.PrivacyScreen
import com.aisoul.privateassistant.ui.screens.devpanel.DevPanelScreen
import com.aisoul.privateassistant.ui.theme.AISoulTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AISoulTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AISoulApp()
                }
            }
        }
    }
}

@Composable
fun AISoulApp() {
    val navController = rememberNavController()
    var selectedTab by remember { mutableStateOf("chat") }

    Scaffold(
        bottomBar = {
            BottomNavigationBar(
                selectedTab = selectedTab,
                onTabSelected = { tab ->
                    selectedTab = tab
                    navController.navigate(tab) {
                        popUpTo(navController.graph.startDestinationId)
                        launchSingleTop = true
                    }
                }
            )
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "chat",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("chat") {
                ChatScreen()
            }
            composable("models") {
                ModelsScreen()
            }
            composable("privacy") {
                PrivacyScreen()
            }
            composable("devpanel") {
                DevPanelScreen()
            }
        }
    }
}

@Composable
fun BottomNavigationBar(
    selectedTab: String,
    onTabSelected: (String) -> Unit
) {
    NavigationBar {
        NavigationBarItem(
            icon = { Icon(Icons.Filled.Home, contentDescription = "Chat") },
            label = { Text(stringResource(R.string.title_chat)) },
            selected = selectedTab == "chat",
            onClick = { onTabSelected("chat") }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Filled.Dashboard, contentDescription = "Models") },
            label = { Text(stringResource(R.string.title_models)) },
            selected = selectedTab == "models",
            onClick = { onTabSelected("models") }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Filled.Settings, contentDescription = "Privacy") },
            label = { Text(stringResource(R.string.title_privacy)) },
            selected = selectedTab == "privacy",
            onClick = { onTabSelected("privacy") }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Filled.Notifications, contentDescription = "Dev Panel") },
            label = { Text(stringResource(R.string.title_dev_panel)) },
            selected = selectedTab == "devpanel",
            onClick = { onTabSelected("devpanel") }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun AISoulAppPreview() {
    AISoulTheme {
        AISoulApp()
    }
}