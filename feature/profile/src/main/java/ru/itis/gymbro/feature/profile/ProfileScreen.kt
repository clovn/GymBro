package ru.itis.gymbro.feature.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
            TopAppBar(
                title = { Text("Мой профиль") },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = GymBroColors.Surface)
            )
        }
    ) { padding ->
        if (state.isLoading && state.profile == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            val user = state.profile
            if (user != null) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .background(GymBroColors.SurfaceVariant)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(16.dp))
                    GymBroAvatar(name = user.name, avatarUrl = user.avatarUrl, size = 96.dp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = user.name,
                        style = GymBroTypography.headlineMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "⭐️ GymBro Score: ${user.score}",
                        style = GymBroTypography.labelLarge,
                        color = GymBroColors.Primary,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Bio Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = GymBroColors.Surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Инфо:",
                                style = GymBroTypography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(text = "Цель: ${user.goal ?: "Не указана"}", style = GymBroTypography.bodyLarge)
                            Text(text = "Уровень: ${user.level ?: "Средний"}", style = GymBroTypography.bodyLarge)
                            Text(text = "О себе: ${user.bio ?: "Нет описания"}", style = GymBroTypography.bodyLarge)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Settings Options List
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = GymBroColors.Surface)
                    ) {
                        Column {
                            ListItem(
                                headlineContent = { Text("Редактировать профиль") },
                                leadingContent = { Text("✏️") },
                                modifier = Modifier.clickable { onNavigateToEditProfile() }
                            )
                            Divider(color = GymBroColors.Divider)
                            ListItem(
                                headlineContent = { Text("Уведомления") },
                                leadingContent = { Text("🔔") },
                                trailingContent = {
                                    if (state.notifications.any { !it.isRead }) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .background(GymBroColors.Primary, CircleShape)
                                        )
                                    }
                                },
                                modifier = Modifier.clickable { onNavigateToNotifications() }
                            )
                            Divider(color = GymBroColors.Divider)
                            
                            // Demo Mode Toggle (for immediate user-side testing)
                            ListItem(
                                headlineContent = { Text("Демо-режим (оффлайн моки)") },
                                leadingContent = { Text("⚙️") },
                                trailingContent = {
                                    Switch(
                                        checked = state.isDemoModeActive,
                                        onCheckedChange = { viewModel.toggleDemoMode(it) }
                                    )
                                }
                            )
                            Divider(color = GymBroColors.Divider)
                            ListItem(
                                headlineContent = { Text("Выйти", color = GymBroColors.Error) },
                                leadingContent = { Text("🚪") },
                                modifier = Modifier.clickable { viewModel.logout() }
                            )
                        }
                    }
                }
            }
        }
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
                title = { Text("Редактировать профиль") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Text("←", fontSize = 24.sp, color = GymBroColors.Primary)
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
                label = "Имя и фамилия"
            )

            GymBroTextField(
                value = bio,
                onValueChange = { bio = it },
                label = "О себе",
                singleLine = false
            )

            GymBroTextField(
                value = goal,
                onValueChange = { goal = it },
                label = "Спортивная цель"
            )

            GymBroTextField(
                value = level,
                onValueChange = { level = it },
                label = "Уровень подготовки"
            )

            Spacer(modifier = Modifier.weight(1f))

            GymBroButton(
                text = "Сохранить изменения",
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
                title = { Text("Уведомления") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Text("←", fontSize = 24.sp, color = GymBroColors.Primary)
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
                        Text("У вас пока нет уведомлений.", style = GymBroTypography.bodyMedium)
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
