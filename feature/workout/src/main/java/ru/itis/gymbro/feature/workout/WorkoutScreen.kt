package ru.itis.gymbro.feature.workout

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
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
fun WorkoutInfoScreen(
    eventId: Long,
    onNavigateBack: () -> Unit,
    onOpenChat: (String) -> Unit,
    viewModel: WorkoutViewModel = koinViewModel()
) {
    val state by viewModel.collectAsState()

    LaunchedEffect(eventId) {
        viewModel.loadWorkoutDetails(eventId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.event?.title ?: "Детали тренировки") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Text("←", fontSize = 24.sp, color = GymBroColors.Primary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = GymBroColors.Surface)
            )
        }
    ) { padding ->
        if (state.isLoading && state.event == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            val event = state.event
            if (event != null) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .background(GymBroColors.SurfaceVariant)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(vertical = 16.dp)
                ) {
                    // Title & Description Card
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = GymBroColors.Surface)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = event.title,
                                    style = GymBroTypography.headlineMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Категория: ${event.workoutType}",
                                    style = GymBroTypography.labelLarge,
                                    color = GymBroColors.Primary
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = event.description ?: "Описание тренировки отсутствует.",
                                    style = GymBroTypography.bodyLarge
                                )
                            }
                        }
                    }

                    // Host Profile Details
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = GymBroColors.Surface)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                GymBroAvatar(name = event.host.name, avatarUrl = event.host.avatarUrl)
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = event.host.name,
                                        style = GymBroTypography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Text(
                                        text = "Организатор • Уровень: ${event.host.level ?: "Средний"}",
                                        style = GymBroTypography.bodyMedium
                                    )
                                }
                            }
                        }
                    }

                    // Location Info details
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = GymBroColors.Surface)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Место проведения",
                                    style = GymBroTypography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = event.location.name,
                                    style = GymBroTypography.titleLarge,
                                    color = GymBroColors.Primary
                                )
                                Text(
                                    text = "Расстояние: ${event.location.distance?.let { "${it.toInt()} м" } ?: "Рядом"}",
                                    style = GymBroTypography.bodyMedium,
                                    color = GymBroColors.TextSecondary
                                )
                            }
                        }
                    }

                    // Participants List Header
                    item {
                        Text(
                            text = "Участники (${event.participants.size}/${event.maxParticipants})",
                            style = GymBroTypography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }

                    // Participants list
                    if (event.participants.isEmpty()) {
                        item {
                            Text("Пока никто не записался.", style = GymBroTypography.bodyMedium)
                        }
                    } else {
                        items(event.participants) { attendee ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = GymBroColors.Surface)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    GymBroAvatar(name = attendee.name, avatarUrl = attendee.avatarUrl, size = 36.dp)
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = attendee.name,
                                        style = GymBroTypography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        modifier = Modifier.weight(1f)
                                    )
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(
                                                if (attendee.status == "CONFIRMED")
                                                    GymBroColors.PrimaryLight
                                                else GymBroColors.SurfaceVariant
                                            )
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = if (attendee.status == "CONFIRMED") "В команде" else "Ожидает",
                                            color = if (attendee.status == "CONFIRMED") GymBroColors.Primary else GymBroColors.TextSecondary,
                                            style = GymBroTypography.labelSmall
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Bottom actions CTA
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            if (!state.isJoined) {
                                GymBroButton(
                                    text = "Присоединиться к тренировке",
                                    onClick = { viewModel.joinWorkout(event.id) }
                                )
                            } else {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    GymBroOutlinedButton(
                                        text = "Покинуть",
                                        onClick = { onNavigateBack() }, // Fallback back on cancel
                                        modifier = Modifier.weight(1f)
                                    )
                                    GymBroButton(
                                        text = "Чат участников",
                                        onClick = { onOpenChat(event.id.toString()) },
                                        modifier = Modifier.weight(1f)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateWorkoutScreen(
    locationId: Long,
    onNavigateBack: () -> Unit,
    viewModel: WorkoutViewModel = koinViewModel()
) {
    val state by viewModel.collectAsState()

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var wishes by remember { mutableStateOf("") }
    var workoutType by remember { mutableStateOf("POWER") }
    var maxParticipants by remember { mutableStateOf("5") }

    viewModel.collectSideEffect { effect ->
        if (effect is WorkoutSideEffect.EventCreatedSuccessfully) {
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Создать тренировку") },
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
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            GymBroTextField(
                value = title,
                onValueChange = { title = it },
                label = "Название тренировки",
                placeholder = "Например: Качаем ноги в субботу"
            )

            GymBroTextField(
                value = description,
                onValueChange = { description = it },
                label = "Описание тренировки",
                placeholder = "Напишите план тренировки",
                singleLine = false
            )

            GymBroTextField(
                value = wishes,
                onValueChange = { wishes = it },
                label = "Пожелания к участникам",
                placeholder = "Например: Возьмите с собой коврик"
            )

            GymBroTextField(
                value = maxParticipants,
                onValueChange = { maxParticipants = it },
                label = "Макс. участников (2-20)",
                placeholder = "5"
            )

            Text("Вид активности:", style = GymBroTypography.labelLarge)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                listOf("POWER", "CARDIO", "YOGA", "CROSSFIT").forEach { type ->
                    val selected = workoutType == type
                    FilterChip(
                        selected = selected,
                        onClick = { workoutType = type },
                        label = { Text(type) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = GymBroColors.Primary,
                            selectedLabelColor = GymBroColors.Background
                        )
                    )
                }
            }

            if (state.errorText != null) {
                Text(text = state.errorText ?: "", color = GymBroColors.Error)
            }

            Spacer(modifier = Modifier.height(24.dp))

            GymBroButton(
                text = "Создать тренировку",
                onClick = {
                    val participantsCount = maxParticipants.toIntOrNull() ?: 5
                    viewModel.createWorkout(
                        locationId = locationId,
                        title = title,
                        description = description,
                        workoutType = workoutType,
                        wishes = wishes,
                        startTime = "2026-06-01T18:00:00Z",
                        endTime = "2026-06-01T19:30:00Z",
                        maxParticipants = participantsCount
                    )
                },
                isLoading = state.isLoading
            )
        }
    }
}
