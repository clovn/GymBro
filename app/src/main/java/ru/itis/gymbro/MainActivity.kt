package ru.itis.gymbro

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import ru.itis.gymbro.core.designsystem.theme.GymBroColors
import ru.itis.gymbro.core.designsystem.theme.GymBroTheme
import ru.itis.gymbro.core.navigation.Screen
import ru.itis.gymbro.feature.auth.*
import ru.itis.gymbro.feature.chat.*
import ru.itis.gymbro.feature.map.MapScreen
import ru.itis.gymbro.feature.onboarding.OnboardingScreen
import ru.itis.gymbro.feature.place.PlaceScreen
import ru.itis.gymbro.feature.people.*
import ru.itis.gymbro.feature.profile.*
import ru.itis.gymbro.feature.workout.*

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GymBroTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    GymBroAppNavigation()
                }
            }
        }
    }
}

@Composable
fun GymBroAppNavigation() {
    val navController = rememberNavController()
    
    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        // 1. Splash Screen
        composable(Screen.Splash.route) {
            SplashScreen(
                onNavigateToMain = {
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                onNavigateToOnboarding = {
                    navController.navigate(Screen.Onboarding.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                onNavigateToSignIn = {
                    navController.navigate(Screen.SignIn.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        // 2. Onboarding Screen
        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                onFinish = {
                    navController.navigate(Screen.SignIn.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }

        // 3. Sign In Screen
        composable(Screen.SignIn.route) {
            SignInScreen(
                onNavigateToSignUp = { navController.navigate(Screen.SignUp.route) },
                onNavigateToForgotPassword = { navController.navigate(Screen.ResetPassword.route) },
                onNavigateToMain = {
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.SignIn.route) { inclusive = true }
                    }
                }
            )
        }

        // 4. Sign Up Screen
        composable(Screen.SignUp.route) {
            SignUpScreen(
                onNavigateToSignIn = { navController.navigate(Screen.SignIn.route) },
                onNavigateToMain = {
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.SignUp.route) { inclusive = true }
                    }
                }
            )
        }

        // 5. Reset Password
        composable(Screen.ResetPassword.route) {
            ResetPasswordScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // 6. Main Tab Host (Map, Search, Messages, Profile)
        composable(Screen.Main.route) {
            MainTabsContainer(navController)
        }

        // 7. Details: Place Info
        composable(
            route = Screen.PlaceInfo.ROUTE_PATTERN,
            arguments = listOf(navArgument("id") { type = NavType.LongType })
        ) { backStackEntry ->
            val placeId = backStackEntry.arguments?.getLong("id") ?: 0L
            PlaceScreen(
                placeId = placeId,
                onNavigateBack = { navController.popBackStack() },
                onPlanWorkout = { id -> navController.navigate("create_workout/$id") }
            )
        }

        // 8. Details: Workout Info
        composable(
            route = Screen.WorkoutInfo.ROUTE_PATTERN,
            arguments = listOf(navArgument("id") { type = NavType.LongType })
        ) { backStackEntry ->
            val eventId = backStackEntry.arguments?.getLong("id") ?: 0L
            WorkoutInfoScreen(
                eventId = eventId,
                onNavigateBack = { navController.popBackStack() },
                onOpenChat = { conversationId -> navController.navigate(Screen.Chat(conversationId).route) }
            )
        }

        // 9. Details: People Profile
        composable(
            route = Screen.PeopleProfile.ROUTE_PATTERN,
            arguments = listOf(navArgument("userId") { type = NavType.StringType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            PeopleProfileScreen(
                userId = userId,
                onNavigateBack = { navController.popBackStack() },
                onOpenChat = { conversationId -> navController.navigate(Screen.Chat(conversationId).route) }
            )
        }

        // 10. Forms: Create Workout Plan (tied to placeId)
        composable(
            route = "create_workout/{locationId}",
            arguments = listOf(navArgument("locationId") { type = NavType.LongType })
        ) { backStackEntry ->
            val locId = backStackEntry.arguments?.getLong("locationId") ?: 0L
            CreateWorkoutScreen(
                locationId = locId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // 11. Chat Room
        composable(
            route = Screen.Chat.ROUTE_PATTERN,
            arguments = listOf(navArgument("conversationId") { type = NavType.StringType })
        ) { backStackEntry ->
            val convId = backStackEntry.arguments?.getString("conversationId") ?: ""
            ChatRoomScreen(
                conversationId = convId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // 12. Settings subpages: Edit Profile
        composable(Screen.EditProfile.route) {
            EditProfileScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // 13. Settings subpages: Notifications Center
        composable(Screen.Notifications.route) {
            NotificationsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}

@Composable
fun MainTabsContainer(
    rootNavController: NavHostController
) {
    val tabNavController = rememberNavController()
    
    val items = listOf(
        TabItem("Explore", "📍", Screen.Map.route),
        TabItem("Search", "🔍", Screen.Search.route),
        TabItem("Messages", "✉️", Screen.Chats.route),
        TabItem("Profile", "👤", Screen.Profile.route)
    )

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = GymBroColors.Surface,
                tonalElevation = 8.dp
            ) {
                val navBackStackEntry by tabNavController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                items.forEach { item ->
                    val selected = currentRoute == item.route
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            tabNavController.navigate(item.route) {
                                popUpTo(tabNavController.graph.startDestinationId) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Text(item.icon, fontSize = 20.sp) },
                        label = { Text(item.title) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = GymBroColors.Primary,
                            selectedTextColor = GymBroColors.Primary,
                            indicatorColor = GymBroColors.PrimaryLight
                        )
                    )
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = tabNavController,
            startDestination = Screen.Map.route,
            modifier = Modifier.padding(padding)
        ) {
            composable(Screen.Map.route) {
                MapScreen(
                    onNavigateToPlace = { id -> rootNavController.navigate(Screen.PlaceInfo(id).route) },
                    onNavigateToWorkout = { id -> rootNavController.navigate(Screen.WorkoutInfo(id).route) },
                    onNavigateToUser = { id -> rootNavController.navigate(Screen.PeopleProfile(id).route) }
                )
            }
            composable(Screen.Search.route) {
                PeopleSearchScreen(
                    onNavigateToProfile = { id -> rootNavController.navigate(Screen.PeopleProfile(id).route) }
                )
            }
            composable(Screen.Chats.route) {
                ChatsListScreen(
                    onNavigateToChatRoom = { id -> rootNavController.navigate(Screen.Chat(id).route) }
                )
            }
            composable(Screen.Profile.route) {
                MyProfileScreen(
                    onNavigateToEditProfile = { rootNavController.navigate(Screen.EditProfile.route) },
                    onNavigateToNotifications = { rootNavController.navigate(Screen.Notifications.route) },
                    onLogout = {
                        rootNavController.navigate(Screen.SignIn.route) {
                            popUpTo(Screen.Main.route) { inclusive = true }
                        }
                    }
                )
            }
        }
    }
}

data class TabItem(
    val title: String,
    val icon: String,
    val route: String
)
