package com.tiktokclone.navigation

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.tiktokclone.ui.auth.*
import com.tiktokclone.ui.home.HomeScreen
import com.tiktokclone.ui.search.DiscoverScreen
import com.tiktokclone.ui.video.UploadScreen
import com.tiktokclone.ui.chat.InboxScreen
import com.tiktokclone.ui.chat.ChatScreen
import com.tiktokclone.ui.profile.ProfileScreen
import com.tiktokclone.ui.profile.EditProfileScreen
import com.tiktokclone.ui.search.SearchScreen
import com.tiktokclone.ui.notifications.NotificationsScreen
import com.tiktokclone.ui.home.CommentsSheet
import com.tiktokclone.ui.common.components.TikTokTheme

data class BottomNavItem(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
)

@Composable
fun TikTokNavHost() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Login.route,
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                onNavigateToRegister = { navController.navigate(Screen.Register.route) },
                onNavigateToForgotPassword = { navController.navigate(Screen.ForgotPassword.route) },
                onLoginSuccess = {
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Register.route) {
            RegisterScreen(
                onNavigateBack = { navController.popBackStack() },
                onRegisterSuccess = {
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.ForgotPassword.route) {
            ForgotPasswordScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Main.route) {
            MainScreen(navController)
        }

        composable(
            Screen.UserProfile.route,
            arguments = listOf(navArgument("userId") { type = NavType.StringType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: return@composable
            ProfileScreen(
                userId = userId,
                isCurrentUser = false,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToChat = { convId -> navController.navigate(Screen.Chat.createRoute(convId)) },
            )
        }

        composable(
            Screen.Chat.route,
            arguments = listOf(navArgument("conversationId") { type = NavType.StringType })
        ) { backStackEntry ->
            val conversationId = backStackEntry.arguments?.getString("conversationId") ?: return@composable
            ChatScreen(
                conversationId = conversationId,
                onNavigateBack = { navController.popBackStack() },
            )
        }

        composable(Screen.EditProfile.route) {
            EditProfileScreen(
                onNavigateBack = { navController.popBackStack() },
            )
        }

        composable(Screen.Search.route) {
            SearchScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToUser = { userId -> navController.navigate(Screen.UserProfile.createRoute(userId)) },
            )
        }

        composable(Screen.Notifications.route) {
            NotificationsScreen(
                onNavigateBack = { navController.popBackStack() },
            )
        }
    }
}

@Composable
fun MainScreen(rootNavController: NavHostController) {
    val navController = rememberNavController()

    val bottomNavItems = listOf(
        BottomNavItem(Screen.Home.route, "Home", Icons.Filled.Home, Icons.Outlined.Home),
        BottomNavItem(Screen.Discover.route, "Discover", Icons.Filled.Explore, Icons.Outlined.Explore),
        BottomNavItem(Screen.Upload.route, "", Icons.Filled.Add, Icons.Outlined.Add),
        BottomNavItem(Screen.Inbox.route, "Inbox", Icons.Filled.ChatBubble, Icons.Outlined.ChatBubble),
        BottomNavItem(Screen.Profile.route, "Profile", Icons.Filled.Person, Icons.Outlined.Person),
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    Scaffold(
        containerColor = Color.Black,
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.97f))
                    .navigationBarsPadding(),
            ) {
                Divider(
                    color = Color.White.copy(alpha = 0.1f),
                    thickness = 0.5.dp,
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    bottomNavItems.forEach { item ->
                        val selected = currentDestination?.hierarchy?.any { it.route == item.route } == true

                        if (item.route == Screen.Upload.route) {
                            // TikTok-style create button
                            Box(
                                modifier = Modifier
                                    .clickable {
                                        navController.navigate(item.route)
                                    }
                                    .height(30.dp)
                                    .width(48.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                // Colored background layers
                                Box(
                                    modifier = Modifier
                                        .offset(x = (-4).dp)
                                        .height(28.dp)
                                        .width(40.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFF25F4EE))
                                )
                                Box(
                                    modifier = Modifier
                                        .offset(x = 4.dp)
                                        .height(28.dp)
                                        .width(40.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFFFE2C55))
                                )
                                // White center with + icon
                                Box(
                                    modifier = Modifier
                                        .height(28.dp)
                                        .width(40.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color.White),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Icon(
                                        Icons.Default.Add,
                                        contentDescription = "Create",
                                        tint = Color.Black,
                                        modifier = Modifier.size(22.dp),
                                    )
                                }
                            }
                        } else {
                            // Regular nav item
                            Column(
                                modifier = Modifier
                                    .clickable {
                                        navController.navigate(item.route) {
                                            popUpTo(navController.graph.findStartDestination().id) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                    .width(56.dp)
                                    .fillMaxHeight(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                            ) {
                                Icon(
                                    imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                    contentDescription = item.label,
                                    modifier = Modifier.size(24.dp),
                                    tint = if (selected) Color.White else Color.White.copy(alpha = 0.6f),
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = item.label,
                                    fontSize = 10.sp,
                                    color = if (selected) Color.White else Color.White.copy(alpha = 0.6f),
                                )
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    onNavigateToUser = { userId ->
                        rootNavController.navigate(Screen.UserProfile.createRoute(userId))
                    },
                    onNavigateToSearch = {
                        rootNavController.navigate(Screen.Search.route)
                    },
                )
            }
            composable(Screen.Discover.route) {
                DiscoverScreen(
                    onNavigateToUser = { userId ->
                        rootNavController.navigate(Screen.UserProfile.createRoute(userId))
                    },
                    onNavigateToSearch = {
                        rootNavController.navigate(Screen.Search.route)
                    },
                )
            }
            composable(Screen.Upload.route) {
                UploadScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onUploadSuccess = { navController.popBackStack() },
                )
            }
            composable(Screen.Inbox.route) {
                InboxScreen(
                    onNavigateToChat = { convId ->
                        rootNavController.navigate(Screen.Chat.createRoute(convId))
                    },
                )
            }
            composable(Screen.Profile.route) {
                ProfileScreen(
                    isCurrentUser = true,
                    onNavigateToEditProfile = {
                        rootNavController.navigate(Screen.EditProfile.route)
                    },
                    onNavigateToSettings = {
                        // Settings screen
                    },
                    onLogout = {
                        rootNavController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                )
            }
        }
    }
}
