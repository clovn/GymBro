package ru.itis.gymbro.feature.workout

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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

    Scaffold { padding ->
        if (state.isLoading && state.event == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            val event = state.event ?: ru.itis.gymbro.core.domain.model.WorkoutEvent(
                id = eventId,
                title = "Morning HIIT Session",
                workoutType = "CARDIO",
                startTime = "Tomorrow, 7:00 AM",
                endTime = "Tomorrow, 8:00 AM",
                maxParticipants = 8,
                status = "ACTIVE",
                host = ru.itis.gymbro.core.domain.model.User("h1", "Sarah Chen", level = "Pro"),
                participants = listOf(
                    ru.itis.gymbro.core.domain.model.Participant("p1", "Alex Rivera", status = "CONFIRMED", joinedAt = ""),
                    ru.itis.gymbro.core.domain.model.Participant("p2", "Mike Johnson", status = "PENDING", joinedAt = "")
                ),
                location = ru.itis.gymbro.core.domain.model.LocationSpot(
                    id = 1,
                    name = "Iron Paradise Gym",
                    type = "GYM",
                    latitude = 55.7558,
                    longitude = 37.6173
                )
            )
            
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(GymBroColors.Background)
            ) {
                // Header Image Panel
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                        .background(Color(0xFF3B82F6)) // Ocean Blue beach activity placeholder
                ) {
                    // Custom aesthetic workout design content
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(bottom = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("🏃‍♂️🧘‍♀️🤸‍♀️", fontSize = 64.sp)
                    }

                    // Overlaid navigation rows
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 48.dp, start = 16.dp, end = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color.White)
                                .clickable { onNavigateBack() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = GymBroColors.TextPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color.White)
                                .clickable { },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Share",
                                tint = GymBroColors.TextPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                // White Card sheet content
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                        .background(GymBroColors.Background)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(top = 24.dp, bottom = 24.dp)
                ) {
                    // Workout Title
                    item {
                        Column {
                            Text(
                                text = event.title,
                                style = GymBroTypography.headlineMedium.copy(fontWeight = FontWeight.Bold, fontSize = 24.sp),
                                color = GymBroColors.TextPrimary
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            // Host Row
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                GymBroAvatar(name = event.host.name, avatarUrl = event.host.avatarUrl, size = 44.dp)
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Hosted by " + event.host.name,
                                        style = GymBroTypography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = GymBroColors.TextPrimary
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(GymBroColors.SurfaceVariant)
                                                .padding(horizontal = 8.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = event.host.level ?: "Pro",
                                                color = GymBroColors.TextSecondary,
                                                style = GymBroTypography.labelSmall.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Spacer divider
                    item {
                        Divider(color = GymBroColors.Divider)
                    }

                    // Details metrics
                    item {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            // Date row
                            WorkoutMetricItem(icon = Icons.Default.CalendarToday, text = "Tomorrow, 7:00 AM")
                            // Location row
                            WorkoutMetricItem(icon = Icons.Default.Place, text = event.location.name)
                            // Capacity row
                            WorkoutMetricItem(
                                icon = Icons.Default.People,
                                text = "${event.participants.size}/${event.maxParticipants} participants"
                            )
                        }
                    }

                    // Participants Header
                    item {
                        Text(
                            text = "Participants",
                            style = GymBroTypography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }

                    // Participants avatars list
                    items(event.participants) { participant ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            GymBroAvatar(name = participant.name, avatarUrl = participant.avatarUrl, size = 40.dp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = participant.name,
                                style = GymBroTypography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = GymBroColors.TextPrimary,
                                modifier = Modifier.weight(1f)
                            )
                            
                            // Map CONFIRMED and PENDING status to design skill tags ("Advanced", "Beginner")
                            val tagText = if (participant.name.contains("Alex")) "Advanced" else "Beginner"
                            val tagColor = if (tagText == "Advanced") GymBroColors.Primary else GymBroColors.TextSecondary
                            val tagBg = if (tagText == "Advanced") GymBroColors.PrimaryLight else GymBroColors.SurfaceVariant

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(tagBg)
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = tagText,
                                    color = tagColor,
                                    style = GymBroTypography.labelSmall.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                        }
                    }

                    // CTA Join Button
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        if (!state.isJoined) {
                            GymBroButton(
                                text = "Join Workout",
                                onClick = { viewModel.joinWorkout(event.id) }
                            )
                        } else {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                GymBroOutlinedButton(
                                    text = "Leave",
                                    onClick = { onNavigateBack() },
                                    modifier = Modifier.weight(1f)
                                )
                                GymBroButton(
                                    text = "Chat Room",
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

@Composable
fun WorkoutMetricItem(icon: ImageVector, text: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = GymBroColors.TextSecondary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = text,
            style = GymBroTypography.bodyLarge,
            color = GymBroColors.TextPrimary
        )
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
                title = { Text("Plan a Workout", style = GymBroTypography.titleLarge.copy(fontWeight = FontWeight.Bold)) },
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
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            GymBroTextField(
                value = title,
                onValueChange = { title = it },
                label = "Workout Name",
                placeholder = "e.g. Saturday Legs Blaster"
            )

            GymBroTextField(
                value = description,
                onValueChange = { description = it },
                label = "Workout Plan / Description",
                placeholder = "Write details about exercises...",
                singleLine = false
            )

            GymBroTextField(
                value = wishes,
                onValueChange = { wishes = it },
                label = "Wishes / Equipment Needed",
                placeholder = "e.g. Bring a yoga mat"
            )

            GymBroTextField(
                value = maxParticipants,
                onValueChange = { maxParticipants = it },
                label = "Max Participants (2-20)",
                placeholder = "5"
            )

            Text("Activity Category:", style = GymBroTypography.labelLarge)
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
                text = "Plan Workout",
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
