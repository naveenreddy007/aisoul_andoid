package com.aisoul.privateassistant

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.*
import com.aisoul.privateassistant.ui.screens.chat.ChatScreen
import com.aisoul.privateassistant.ui.screens.models.ModelsScreen
import com.aisoul.privateassistant.ui.screens.privacy.PrivacyScreen
import com.aisoul.privateassistant.ui.theme.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // ✨ ENHANCED AI SOUL THEME WITH PREMIUM EFFECTS ✨
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
    
    // 🎆 Premium transition animations
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = currentBackStackEntry?.destination?.route ?: "chat"
    
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.Transparent, // Allow frozen background to show through
        bottomBar = {
            PremiumBottomNavigationBar(
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
        // 🌌 Premium navigation with smooth transitions
        NavHost(
            navController = navController,
            startDestination = "chat",
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            enterTransition = {
                slideInHorizontally(
                    initialOffsetX = { it },
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                ) + fadeIn(
                    animationSpec = tween(300)
                )
            },
            exitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { -it },
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                ) + fadeOut(
                    animationSpec = tween(300)
                )
            }
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
        }
    }
}

// 💎 GOD-LEVEL PREMIUM NAVIGATION BAR WITH FROZEN EFFECTS
@Composable
fun PremiumBottomNavigationBar(
    selectedTab: String,
    onTabSelected: (String) -> Unit
) {
    // ✨ Premium floating navigation bar with frozen glass effect
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        NavigationBar(
            modifier = Modifier
                .clip(RoundedCornerShape(24.dp))
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            FrozenGlassStart,
                            FrozenGlassEnd
                        )
                    )
                ),
            containerColor = Color.Transparent,
            contentColor = StardustSilver
        ) {
            // 🏠 Chat Tab
            PremiumNavigationItem(
                icon = Icons.Filled.Home,
                label = stringResource(R.string.title_chat),
                selected = selectedTab == "chat",
                onClick = { onTabSelected("chat") }
            )
            
            // 🔧 Models Tab
            PremiumNavigationItem(
                icon = Icons.Filled.Dashboard,
                label = stringResource(R.string.title_models),
                selected = selectedTab == "models",
                onClick = { onTabSelected("models") }
            )
            
            // 🔒 Privacy Tab
            PremiumNavigationItem(
                icon = Icons.Filled.Settings,
                label = stringResource(R.string.title_privacy),
                selected = selectedTab == "privacy",
                onClick = { onTabSelected("privacy") }
            )
            
            // 🔔 Dev Panel Tab
            PremiumNavigationItem(
                icon = Icons.Filled.Notifications,
                label = stringResource(R.string.title_dev_panel),
                selected = selectedTab == "devpanel",
                onClick = { onTabSelected("devpanel") }
            )
        }
    }
}

// 🎯 PREMIUM NAVIGATION ITEM WITH GOD-LEVEL ANIMATIONS
@Composable
fun RowScope.PremiumNavigationItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    // ✨ Divine selection animations
    val animatedWeight by animateFloatAsState(
        targetValue = if (selected) 1.5f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "tab_weight"
    )
    
    val animatedIconColor by animateColorAsState(
        targetValue = if (selected) DivinePurple else MoonbeamGray,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "icon_color"
    )
    
    val animatedTextColor by animateColorAsState(
        targetValue = if (selected) PlatinumWhite else StardustSilver,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "text_color"
    )
    
    NavigationBarItem(
        icon = {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .background(
                        brush = if (selected) {
                            Brush.radialGradient(
                                colors = listOf(
                                    DivinePurple.copy(alpha = 0.2f),
                                    Color.Transparent
                                )
                            )
                        } else {
                            Brush.radialGradient(
                                colors = listOf(Color.Transparent, Color.Transparent)
                            )
                        },
                        shape = RoundedCornerShape(14.dp)
                    ),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = animatedIconColor,
                    modifier = Modifier.size(24.dp)
                )
            }
        },
        label = {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = animatedTextColor,
                    fontWeight = if (selected) androidx.compose.ui.text.font.FontWeight.Bold 
                                else androidx.compose.ui.text.font.FontWeight.Medium
                )
            )
        },
        selected = selected,
        onClick = onClick,
        colors = NavigationBarItemDefaults.colors(
            selectedIconColor = Color.Transparent,
            unselectedIconColor = Color.Transparent,
            selectedTextColor = Color.Transparent,
            unselectedTextColor = Color.Transparent,
            indicatorColor = Color.Transparent
        ),
        modifier = Modifier.weight(animatedWeight)
    )
}

@Preview(showBackground = true)
@Composable
fun AISoulAppPreview() {
    AISoulTheme {
        AISoulApp()
    }
}