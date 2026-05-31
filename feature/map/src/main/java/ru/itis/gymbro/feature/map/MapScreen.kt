package ru.itis.gymbro.feature.map

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
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
import ru.itis.gymbro.core.domain.model.LocationSpot
import ru.itis.gymbro.core.domain.model.User
import ru.itis.gymbro.core.domain.model.WorkoutEvent
import ru.itis.gymbro.core.location.UserLocation
import kotlin.math.sqrt

@Composable
fun MapScreen(
    onNavigateToPlace: (Long) -> Unit,
    onNavigateToWorkout: (Long) -> Unit,
    onNavigateToUser: (String) -> Unit,
    viewModel: MapViewModel = koinViewModel()
) {
    val state by viewModel.collectAsState()
    
    var selectedSpot by remember { mutableStateOf<LocationSpot?>(null) }
    var selectedWorkout by remember { mutableStateOf<WorkoutEvent?>(null) }
    var selectedPerson by remember { mutableStateOf<User?>(null) }
    
    var showCreateDialog by remember { mutableStateOf(false) }

    viewModel.collectSideEffect { effect ->
        when (effect) {
            is MapSideEffect.OpenPlaceDetails -> onNavigateToPlace(effect.id)
            is MapSideEffect.OpenWorkoutDetails -> onNavigateToWorkout(effect.id)
            is MapSideEffect.OpenPeopleProfile -> onNavigateToUser(effect.id)
            is MapSideEffect.ShowMessage -> { }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        
        // Mock Map Canvas
        MockMapView(
            userLocation = state.userLocation,
            spots = if (state.showSpots) state.spots else emptyList(),
            workouts = if (state.showWorkouts) state.workouts else emptyList(),
            people = if (state.showPeople) state.people else emptyList(),
            isSelectingLocation = state.isSelectingLocation,
            onLocationPan = { panOffset ->
                if (state.isSelectingLocation) {
                    val current = state.selectedLocationCoords ?: state.userLocation
                    val newLat = current.latitude - panOffset.y * 0.00005
                    val newLon = current.longitude + panOffset.x * 0.00005
                    viewModel.updateSelectedCoords(UserLocation(newLat, newLon))
                }
            },
            onSpotClick = {
                selectedSpot = it
                selectedWorkout = null
                selectedPerson = null
            },
            onWorkoutClick = {
                selectedWorkout = it
                selectedSpot = null
                selectedPerson = null
            },
            onPersonClick = {
                selectedPerson = it
                selectedSpot = null
                selectedWorkout = null
            }
        )

        // Top Search & Layer Filters
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, start = 16.dp, end = 16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(GymBroColors.Surface)
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🔍 Поиск мест и тренировок...",
                    style = GymBroTypography.bodyLarge,
                    color = GymBroColors.TextTertiary,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = Icons.Default.FilterList,
                    contentDescription = "Filters",
                    tint = GymBroColors.Primary
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))

            // Filter Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = state.showSpots,
                    onClick = { viewModel.toggleSpotsLayer() },
                    label = { Text("📍 Места") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = GymBroColors.Primary,
                        selectedLabelColor = GymBroColors.Background
                    )
                )
                FilterChip(
                    selected = state.showWorkouts,
                    onClick = { viewModel.toggleWorkoutsLayer() },
                    label = { Text("💪 Тренировки") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = GymBroColors.Primary,
                        selectedLabelColor = GymBroColors.Background
                    )
                )
                FilterChip(
                    selected = state.showPeople,
                    onClick = { viewModel.togglePeopleLayer() },
                    label = { Text("🤝 Люди") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = GymBroColors.Primary,
                        selectedLabelColor = GymBroColors.Background
                    )
                )
            }
        }

        // Location Crosshair for UGC Creation
        if (state.isSelectingLocation) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                // Crosshair marker
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color.Red.copy(alpha = 0.2f))
                        .border(2.dp, Color.Red, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Box(modifier = Modifier.size(6.dp).background(Color.Red, CircleShape))
                }
            }

            // Confirm bar at bottom
            Card(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = GymBroColors.Surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Переместите карту для выбора координат",
                        style = GymBroTypography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Координаты: ${String.format("%.4f", state.selectedLocationCoords?.latitude ?: 0.0)}, ${String.format("%.4f", state.selectedLocationCoords?.longitude ?: 0.0)}",
                        style = GymBroTypography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { viewModel.cancelSelectingLocation() },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = GymBroColors.SurfaceVariant, contentColor = GymBroColors.TextPrimary)
                        ) {
                            Text("Отмена")
                        }
                        Button(
                            onClick = {
                                viewModel.confirmSelectedLocation()
                                showCreateDialog = true
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = GymBroColors.Primary)
                        ) {
                            Text("Выбрать")
                        }
                    }
                }
            }
        }

        // Floating Action Buttons (MyLocation, Add Spot/Workout)
        if (!state.isSelectingLocation) {
            Column(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = 120.dp, end = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // My Location FAB
                FloatingActionButton(
                    onClick = { viewModel.loadData() },
                    containerColor = GymBroColors.Surface,
                    contentColor = GymBroColors.Primary,
                    shape = CircleShape
                ) {
                    Icon(imageVector = Icons.Default.MyLocation, contentDescription = "My Location")
                }

                // Add Point FAB
                FloatingActionButton(
                    onClick = { viewModel.startSelectingLocation() },
                    containerColor = GymBroColors.Primary,
                    contentColor = GymBroColors.Background,
                    shape = CircleShape
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Add UGC Point")
                }
            }
        }

        // Active Spot Bottom Sheet details
        selectedSpot?.let { spot ->
            GymBroCard(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(bottom = 24.dp, start = 16.dp, end = 16.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = spot.name,
                            style = GymBroTypography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { selectedSpot = null }) {
                            Text("✕", fontSize = 16.sp, color = GymBroColors.TextTertiary)
                        }
                    }
                    Text(
                        text = if (spot.type == "GYM") "🏢 Фитнес-клуб" else "🌳 Воркаут-площадка",
                        style = GymBroTypography.labelSmall,
                        color = GymBroColors.Primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = spot.description ?: "Нет описания",
                        maxLines = 2,
                        style = GymBroTypography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        GymBroOutlinedButton(
                            text = "Подробнее",
                            onClick = { onNavigateToPlace(spot.id) },
                            modifier = Modifier.weight(1f)
                        )
                        GymBroButton(
                            text = "Тренироваться",
                            onClick = { onNavigateToPlace(spot.id) }, // Redirect to detail to create plan
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // Active Workout Bottom Sheet details
        selectedWorkout?.let { event ->
            GymBroCard(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(bottom = 24.dp, start = 16.dp, end = 16.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = event.title,
                            style = GymBroTypography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { selectedWorkout = null }) {
                            Text("✕", fontSize = 16.sp, color = GymBroColors.TextTertiary)
                        }
                    }
                    Text(
                        text = "📅 Начало: ${event.startTime.replace("T", " ").take(16)}",
                        style = GymBroTypography.labelSmall,
                        color = GymBroColors.Primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = event.description ?: "Нет описания тренировки",
                        maxLines = 2,
                        style = GymBroTypography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        GymBroOutlinedButton(
                            text = "Подробнее",
                            onClick = { onNavigateToWorkout(event.id) },
                            modifier = Modifier.weight(1f)
                        )
                        GymBroButton(
                            text = "Присоединиться",
                            onClick = { onNavigateToWorkout(event.id) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // Active Person Bottom Sheet details
        selectedPerson?.let { user ->
            GymBroCard(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(bottom = 24.dp, start = 16.dp, end = 16.dp)
            ) {
                Column {
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
                                style = GymBroTypography.labelSmall,
                                color = GymBroColors.TextSecondary
                            )
                        }
                        IconButton(onClick = { selectedPerson = null }) {
                            Text("✕", fontSize = 16.sp, color = GymBroColors.TextTertiary)
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        GymBroOutlinedButton(
                            text = "Профиль",
                            onClick = { onNavigateToUser(user.id) },
                            modifier = Modifier.weight(1f)
                        )
                        GymBroButton(
                            text = "Написать",
                            onClick = { onNavigateToUser(user.id) }, // Direct connection through profile
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // Add Spot Form Dialog
        if (showCreateDialog) {
            var spotName by remember { mutableStateOf("") }
            var spotDesc by remember { mutableStateOf("") }
            var spotType by remember { mutableStateOf("WORKOUT") }

            Dialog(onDismissRequest = { showCreateDialog = false }) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = GymBroColors.Surface)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Добавить новое место",
                            style = GymBroTypography.headlineMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        
                        GymBroTextField(
                            value = spotName,
                            onValueChange = { spotName = it },
                            label = "Название места",
                            placeholder = "Например: Воркаут на Красной Площади"
                        )

                        GymBroTextField(
                            value = spotDesc,
                            onValueChange = { spotDesc = it },
                            label = "Описание места",
                            placeholder = "Какое оборудование здесь есть?",
                            singleLine = false
                        )

                        Text("Категория спортивного места:", style = GymBroTypography.labelLarge)
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(selected = spotType == "WORKOUT", onClick = { spotType = "WORKOUT" })
                                Text("Площадка")
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(selected = spotType == "GYM", onClick = { spotType = "GYM" })
                                Text("Зал")
                            }
                        }

                        if (state.errorText != null) {
                            Text(text = state.errorText ?: "", color = GymBroColors.Error)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { showCreateDialog = false },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = GymBroColors.SurfaceVariant, contentColor = GymBroColors.TextPrimary)
                            ) {
                                Text("Отмена")
                            }
                            Button(
                                onClick = {
                                    viewModel.createLocation(spotName, spotDesc, spotType)
                                    showCreateDialog = false
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = GymBroColors.Primary)
                            ) {
                                Text("Создать")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MockMapView(
    userLocation: UserLocation,
    spots: List<LocationSpot>,
    workouts: List<WorkoutEvent>,
    people: List<User>,
    isSelectingLocation: Boolean,
    onLocationPan: (Offset) -> Unit,
    onSpotClick: (LocationSpot) -> Unit,
    onWorkoutClick: (WorkoutEvent) -> Unit,
    onPersonClick: (User) -> Unit
) {
    // Map offsets for simulated panning
    var mapOffsetLat by remember { mutableStateOf(userLocation.latitude) }
    var mapOffsetLon by remember { mutableStateOf(userLocation.longitude) }

    // Synchronize starting map position with user coords
    LaunchedEffect(userLocation) {
        mapOffsetLat = userLocation.latitude
        mapOffsetLon = userLocation.longitude
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFE5F1FD)) // Light map-water background
            .pointerInput(isSelectingLocation) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    if (isSelectingLocation) {
                        onLocationPan(dragAmount)
                    } else {
                        // Regular map drag/panning
                        mapOffsetLat -= dragAmount.y * 0.00005
                        mapOffsetLon += dragAmount.x * 0.00005
                    }
                }
            }
    ) {
        // Draw grid system
        Canvas(modifier = Modifier.fillMaxSize()) {
            val gridSpacing = 160f
            // Vertical grid lines
            for (x in 0..size.width.toInt() step gridSpacing.toInt()) {
                drawLine(
                    color = Color.White.copy(alpha = 0.5f),
                    start = Offset(x.toFloat(), 0f),
                    end = Offset(x.toFloat(), size.height),
                    strokeWidth = 2f
                )
            }
            // Horizontal grid lines
            for (y in 0..size.height.toInt() step gridSpacing.toInt()) {
                drawLine(
                    color = Color.White.copy(alpha = 0.5f),
                    start = Offset(0f, y.toFloat()),
                    end = Offset(size.width, y.toFloat()),
                    strokeWidth = 2f
                )
            }
        }

        // Draw User Blue Circle
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .size(24.dp)
                .background(GymBroColors.Primary.copy(alpha = 0.2f), CircleShape)
                .border(2.dp, GymBroColors.Primary, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(Color.White, CircleShape)
                    .padding(2.dp)
            ) {
                Box(modifier = Modifier.background(GymBroColors.Primary, CircleShape))
            }
        }

        // Draw Nearby Markers
        Box(modifier = Modifier.fillMaxSize()) {
            val scope = this
            
            // Render Location Spots
            spots.forEach { spot ->
                val pos = getScreenOffset(spot.latitude, spot.longitude, mapOffsetLat, mapOffsetLon, 1080f, 1920f)
                if (pos.x in 0f..1080f && pos.y in 0f..1920f) {
                    Box(
                        modifier = Modifier
                            .offset(pos.x.dp, pos.y.dp)
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(GymBroColors.Primary)
                            .border(2.dp, Color.White, CircleShape)
                            .clickable { onSpotClick(spot) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(if (spot.type == "GYM") "🏢" else "📍", fontSize = 16.sp)
                    }
                }
            }

            // Render Workouts
            workouts.forEach { event ->
                val pos = getScreenOffset(event.location.latitude + 0.001, event.location.longitude + 0.001, mapOffsetLat, mapOffsetLon, 1080f, 1920f)
                if (pos.x in 0f..1080f && pos.y in 0f..1920f) {
                    Box(
                        modifier = Modifier
                            .offset(pos.x.dp, pos.y.dp)
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(GymBroColors.Success)
                            .border(2.dp, Color.White, CircleShape)
                            .clickable { onWorkoutClick(event) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("💪", fontSize = 16.sp)
                    }
                }
            }

            // Render Users
            people.forEachIndexed { i, user ->
                // Stagger user locations around map center for mock display
                val latOffset = 0.0015 * (i - 1)
                val lonOffset = 0.002 * (if (i % 2 == 0) 1 else -1)
                val pos = getScreenOffset(mapOffsetLat + latOffset, mapOffsetLon + lonOffset, mapOffsetLat, mapOffsetLon, 1080f, 1920f)
                if (pos.x in 0f..1080f && pos.y in 0f..1920f) {
                    Box(
                        modifier = Modifier
                            .offset(pos.x.dp, pos.y.dp)
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE040FB))
                            .border(2.dp, Color.White, CircleShape)
                            .clickable { onPersonClick(user) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🤝", fontSize = 16.sp)
                    }
                }
            }
        }
    }
}

// Convert geographic lat/lon offsets to screen offsets
fun getScreenOffset(
    lat: Double,
    lon: Double,
    centerLat: Double,
    centerLon: Double,
    screenWidthPx: Float,
    screenHeightPx: Float
): Offset {
    val scaleFactor = 150000f // Scaling factor for zoom level
    
    val dLat = lat - centerLat
    val dLon = lon - centerLon
    
    val x = (screenWidthPx / 2f) + (dLon * scaleFactor).toFloat()
    val y = (screenHeightPx / 2f) - (dLat * scaleFactor).toFloat() // Y axis in screen space goes down
    
    return Offset(x / 4f, y / 4f) // Scale to fit compose coordinate space
}
