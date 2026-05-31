package ru.itis.gymbro.feature.people

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import org.koin.androidx.compose.koinViewModel
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect
import ru.itis.gymbro.core.designsystem.components.*
import ru.itis.gymbro.core.designsystem.theme.GymBroColors
import ru.itis.gymbro.core.designsystem.theme.GymBroTypography

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PeopleSearchScreen(
    onNavigateToProfile: (String) -> Unit,
    viewModel: PeopleViewModel = koinViewModel()
) {
    val state by viewModel.collectAsState()
    var searchVal by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Поиск напарников") },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = GymBroColors.Surface)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(GymBroColors.SurfaceVariant)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            GymBroTextField(
                value = searchVal,
                onValueChange = { searchVal = it },
                label = "Поиск людей",
                placeholder = "Введите имя или цель..."
            )

            if (state.isLoading) {
                Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                val filteredList = state.people.filter {
                    it.name.contains(searchVal, ignoreCase = true) ||
                            (it.goal ?: "").contains(searchVal, ignoreCase = true)
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    if (filteredList.isEmpty()) {
                        item {
                            Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                                Text("По вашему запросу никто не найден", style = GymBroTypography.bodyMedium)
                            }
                        }
                    } else {
                        items(filteredList) { user ->
                            GymBroCard(
                                onClick = { onNavigateToProfile(user.id) }
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    GymBroAvatar(name = user.name, avatarUrl = user.avatarUrl)
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = user.name,
                                            style = GymBroTypography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                        )
                                        Text(
                                            text = "Цель: ${user.goal ?: "Спорт"}",
                                            style = GymBroTypography.bodyMedium
                                        )
                                        Text(
                                            text = "Уровень: ${user.level ?: "Средний"}",
                                            style = GymBroTypography.labelSmall
                                        )
                                    }
                                    
                                    // GymBro Score Badge
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(GymBroColors.PrimaryLight)
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = "⭐️ ${user.score}",
                                            color = GymBroColors.Primary,
                                            style = GymBroTypography.labelSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PeopleProfileScreen(
    userId: String,
    onNavigateBack: () -> Unit,
    onOpenChat: (String) -> Unit,
    viewModel: PeopleViewModel = koinViewModel()
) {
    val state by viewModel.collectAsState()

    LaunchedEffect(userId) {
        viewModel.loadUserProfile(userId)
    }

    viewModel.collectSideEffect { effect ->
        if (effect is PeopleSideEffect.OpenChat) {
            onOpenChat(effect.conversationId)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Профиль напарника") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Text("←", fontSize = 24.sp, color = GymBroColors.Primary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = GymBroColors.Surface)
            )
        }
    ) { padding ->
        if (state.isLoading && state.activeProfile == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            val user = state.activeProfile
            if (user != null) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .background(GymBroColors.Background)
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    GymBroAvatar(name = user.name, avatarUrl = user.avatarUrl, size = 110.dp)
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = user.name,
                        style = GymBroTypography.headlineMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))

                    // Bio Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = GymBroColors.SurfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "О себе:",
                                style = GymBroTypography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = user.bio ?: "Пользователь не добавил описание.",
                                style = GymBroTypography.bodyLarge
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Stats grid
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                            Text("GymBro Score", style = GymBroTypography.labelSmall)
                            Text("⭐️ ${user.score}", style = GymBroTypography.titleLarge, fontWeight = FontWeight.Bold, color = GymBroColors.Primary)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                            Text("Тренировок", style = GymBroTypography.labelSmall)
                            Text("${user.workoutsCount}", style = GymBroTypography.titleLarge, fontWeight = FontWeight.Bold)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                            Text("Отзывов", style = GymBroTypography.labelSmall)
                            Text("${user.reviewsCount}", style = GymBroTypography.titleLarge, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    // Action buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        GymBroOutlinedButton(
                            text = "Тренироваться вместе",
                            onClick = { onNavigateBack() }, // Return to plan a workout
                            modifier = Modifier.weight(1f)
                        )
                        GymBroButton(
                            text = "Написать",
                            onClick = { viewModel.startChat(user.id) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}
