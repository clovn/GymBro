package ru.itis.gymbro.feature.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import org.koin.androidx.compose.koinViewModel
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect
import ru.itis.gymbro.core.designsystem.components.*
import ru.itis.gymbro.core.designsystem.theme.GymBroColors
import ru.itis.gymbro.core.designsystem.theme.GymBroTypography

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyProfileScreen(
    onNavigateToEditProfile: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    onLogout: () -> Unit,
    viewModel: ProfileViewModel = koinViewModel()
) {
    val state by viewModel.collectAsState()

    viewModel.collectSideEffect { effect ->
        if (effect is ProfileSideEffect.NavigateToLogin) {
            onLogout()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.loadProfile()
    }

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(GymBroColors.Background)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Profile",
                    style = GymBroTypography.displaySmall.copy(fontWeight = FontWeight.Bold, fontSize = 24.sp),
                    modifier = Modifier.weight(1f)
                )
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(GymBroColors.SurfaceVariant)
                        .clickable { onNavigateToNotifications() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = GymBroColors.TextPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    ) { padding ->
        if (state.isLoading && state.profile == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            val user = state.profile ?: ru.itis.gymbro.core.domain.model.User("me", "Jordan Smith", score = 100)
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(GymBroColors.Background)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                // Avatar, Name, Handle, Bio
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        GymBroAvatar(name = user.name, avatarUrl = user.avatarUrl, size = 96.dp)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = user.name,
                            style = GymBroTypography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                            color = GymBroColors.TextPrimary
                        )
                        Text(
                            text = "@" + (user.name.lowercase().replace(" ", "")),
                            style = GymBroTypography.bodyLarge,
                            color = GymBroColors.TextSecondary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = user.bio ?: "Fitness enthusiast | Calisthenics | NYC",
                            style = GymBroTypography.bodyMedium,
                            color = GymBroColors.TextSecondary,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                // Stats Dashboard Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = GymBroColors.SurfaceVariant)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = if (user.workoutsCount > 0) user.workoutsCount.toString() else "267",
                                    style = GymBroTypography.titleLarge.copy(fontWeight = FontWeight.Bold)
                                )
                                Text("Workouts", style = GymBroTypography.bodyMedium, color = GymBroColors.TextSecondary)
                            }
                            Box(modifier = Modifier.width(1.dp).height(32.dp).background(GymBroColors.Divider))
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("1543", style = GymBroTypography.titleLarge.copy(fontWeight = FontWeight.Bold))
                                Text("Followers", style = GymBroTypography.bodyMedium, color = GymBroColors.TextSecondary)
                            }
                            Box(modifier = Modifier.width(1.dp).height(32.dp).background(GymBroColors.Divider))
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("432", style = GymBroTypography.titleLarge.copy(fontWeight = FontWeight.Bold))
                                Text("Following", style = GymBroTypography.bodyMedium, color = GymBroColors.TextSecondary)
                            }
                        }
                    }
                }

                // Edit Profile & Heart Action Button
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = { onNavigateToEditProfile() },
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = GymBroColors.Primary),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Edit Profile", style = GymBroTypography.labelLarge.copy(color = Color.White))
                            }
                        }
                        
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(GymBroColors.SurfaceVariant)
                                .clickable { },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.FavoriteBorder,
                                contentDescription = "Favorites",
                                tint = GymBroColors.TextPrimary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }

                // My Plans Headers & List items
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("My Plans", style = GymBroTypography.titleLarge.copy(fontWeight = FontWeight.Bold))
                        GymBroTextButton(text = "See all", onClick = {})
                    }
                }

                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = GymBroColors.Surface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, GymBroColors.Divider)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(GymBroColors.PrimaryLight),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FitnessCenter,
                                    contentDescription = null,
                                    tint = GymBroColors.Primary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Push Day", style = GymBroTypography.titleMedium.copy(fontWeight = FontWeight.Bold))
                                Text("6 exercises · 45 min", style = GymBroTypography.bodyMedium, color = GymBroColors.TextSecondary)
                            }
                            Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = GymBroColors.TextSecondary)
                        }
                    }
                }

                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = GymBroColors.Surface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, GymBroColors.Divider)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFFFFECE0)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FitnessCenter,
                                    contentDescription = null,
                                    tint = Color(0xFFFF6B00),
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Pull Day", style = GymBroTypography.titleMedium.copy(fontWeight = FontWeight.Bold))
                                Text("5 exercises · 40 min", style = GymBroTypography.bodyMedium, color = GymBroColors.TextSecondary)
                            }
                            Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = GymBroColors.TextSecondary)
                        }
                    }
                }

                // General options
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        ProfileOptionItem(title = "My workouts", icon = Icons.Default.CalendarToday) {}
                        ProfileOptionItem(title = "Achievements", icon = Icons.Default.EmojiEvents) {}
                        ProfileOptionItem(title = "Workout History", icon = Icons.Default.FitnessCenter) {}
                        
                        Divider(modifier = Modifier.padding(vertical = 12.dp), color = GymBroColors.Divider)
                        
                        ProfileOptionItem(title = "Logout", icon = Icons.Default.ArrowBack, tint = GymBroColors.Error) {
                            viewModel.logout()
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileOptionItem(
    title: String,
    icon: ImageVector,
    tint: Color = GymBroColors.TextPrimary,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = tint,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = title,
            style = GymBroTypography.bodyLarge.copy(fontWeight = FontWeight.Medium),
            color = tint,
            modifier = Modifier.weight(1f)
        )
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = GymBroColors.TextTertiary,
            modifier = Modifier.size(20.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    onNavigateBack: () -> Unit,
    viewModel: ProfileViewModel = koinViewModel()
) {
    val state by viewModel.collectAsState()

    var name by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }
    var goal by remember { mutableStateOf("") }
    var level by remember { mutableStateOf("") }

    LaunchedEffect(state.profile) {
        state.profile?.let {
            name = it.name
            bio = it.bio ?: ""
            goal = it.goal ?: ""
            level = it.level ?: ""
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Profile", style = GymBroTypography.titleLarge.copy(fontWeight = FontWeight.Bold)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = GymBroColors.TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = GymBroColors.Surface)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(GymBroColors.Background)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            GymBroTextField(
                value = name,
                onValueChange = { name = it },
                label = "Full Name"
            )

            GymBroTextField(
                value = bio,
                onValueChange = { bio = it },
                label = "Bio",
                singleLine = false
            )

            GymBroTextField(
                value = goal,
                onValueChange = { goal = it },
                label = "Goal"
            )

            GymBroTextField(
                value = level,
                onValueChange = { level = it },
                label = "Fitness Level"
            )

            Spacer(modifier = Modifier.weight(1f))

            GymBroButton(
                text = "Save Changes",
                onClick = {
                    viewModel.updateProfile(name, bio, goal, level)
                    onNavigateBack()
                },
                isLoading = state.isLoading
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    onNavigateBack: () -> Unit,
    viewModel: ProfileViewModel = koinViewModel()
) {
    val state by viewModel.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadNotifications()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notifications", style = GymBroTypography.titleLarge.copy(fontWeight = FontWeight.Bold)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = GymBroColors.TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = GymBroColors.Surface)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(GymBroColors.SurfaceVariant)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            if (state.notifications.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(48.dp), contentAlignment = Alignment.Center) {
                        Text("No notifications yet.", style = GymBroTypography.bodyMedium)
                    }
                }
            } else {
                items(state.notifications) { item ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.markNotificationRead(item.id) },
                        colors = CardDefaults.cardColors(
                            containerColor = if (item.isRead) GymBroColors.Surface else GymBroColors.PrimaryLight
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = item.title,
                                    style = GymBroTypography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    modifier = Modifier.weight(1f)
                                )
                                if (!item.isRead) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .background(GymBroColors.Primary, CircleShape)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = item.text,
                                style = GymBroTypography.bodyLarge
                            )
                        }
                    }
                }
            }
        }
    }
}
