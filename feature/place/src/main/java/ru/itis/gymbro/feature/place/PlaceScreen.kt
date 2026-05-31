package ru.itis.gymbro.feature.place

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
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
fun PlaceScreen(
    placeId: Long,
    onNavigateBack: () -> Unit,
    onPlanWorkout: (Long) -> Unit,
    viewModel: PlaceViewModel = koinViewModel()
) {
    val state by viewModel.collectAsState()
    var showReviewDialog by remember { mutableStateOf(false) }

    LaunchedEffect(placeId) {
        viewModel.loadPlaceDetails(placeId)
    }

    viewModel.collectSideEffect { effect ->
        if (effect is PlaceSideEffect.ReviewAddedSuccessfully) {
            showReviewDialog = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.place?.name ?: "Детали места") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Text("←", fontSize = 24.sp, color = GymBroColors.Primary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = GymBroColors.Surface)
            )
        }
    ) { padding ->
        if (state.isLoading && state.place == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            val spot = state.place
            if (spot != null) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .background(GymBroColors.SurfaceVariant)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(vertical = 16.dp)
                ) {
                    // Header Image Placeholder & details
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = GymBroColors.Surface)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(180.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(GymBroColors.PrimaryLight),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(if (spot.type == "GYM") "🏢" else "🌳", fontSize = 64.sp)
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = spot.name,
                                    style = GymBroTypography.headlineMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = if (spot.type == "GYM") "🏢 Фитнес-клуб" else "🌳 Воркаут-площадка",
                                    style = GymBroTypography.labelLarge,
                                    color = GymBroColors.Primary
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = spot.description ?: "Отличное место для занятий спортом.",
                                    style = GymBroTypography.bodyLarge
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    GymBroRatingBar(rating = spot.avgRating.toFloat())
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "${spot.avgRating} (${spot.reviewCount} отзывов)",
                                        style = GymBroTypography.labelSmall
                                    )
                                }
                            }
                        }
                    }

                    // Actions
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            GymBroOutlinedButton(
                                text = "Отзыв",
                                onClick = { showReviewDialog = true },
                                modifier = Modifier.weight(1f)
                            )
                            GymBroButton(
                                text = "Запланировать",
                                onClick = { onPlanWorkout(spot.id) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    // Reviews header
                    item {
                        Text(
                            text = "Отзывы пользователей",
                            style = GymBroTypography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }

                    // Reviews list
                    if (state.reviews.isEmpty()) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = GymBroColors.Surface)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(24.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("Нет отзывов. Будьте первым!", style = GymBroTypography.bodyMedium)
                                }
                            }
                        }
                    } else {
                        items(state.reviews) { review ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = GymBroColors.Surface)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        GymBroAvatar(name = review.author.name, avatarUrl = review.author.avatarUrl, size = 36.dp)
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = review.author.name,
                                                style = GymBroTypography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                            )
                                            Text(
                                                text = review.createdAt.take(10),
                                                style = GymBroTypography.labelSmall
                                            )
                                        }
                                        GymBroRatingBar(rating = review.rating.toFloat(), starSize = 12.dp)
                                    }
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = review.comment ?: "",
                                        style = GymBroTypography.bodyLarge
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Add Review Dialog
        if (showReviewDialog) {
            var rating by remember { mutableStateOf(5) }
            var comment by remember { mutableStateOf("") }

            Dialog(onDismissRequest = { showReviewDialog = false }) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = GymBroColors.Surface)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Оставить отзыв",
                            style = GymBroTypography.headlineMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        
                        // Interactive rating selector
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            repeat(5) { i ->
                                val starRating = i + 1
                                Text(
                                    text = if (starRating <= rating) "★" else "☆",
                                    fontSize = 32.sp,
                                    color = Color(0xFFFFB300),
                                    modifier = Modifier.clickable { rating = starRating }
                                )
                            }
                        }

                        GymBroTextField(
                            value = comment,
                            onValueChange = { comment = it },
                            label = "Ваш отзыв",
                            placeholder = "Поделитесь вашим мнением...",
                            singleLine = false
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { showReviewDialog = false },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = GymBroColors.SurfaceVariant, contentColor = GymBroColors.TextPrimary)
                            ) {
                                Text("Отмена")
                            }
                            Button(
                                onClick = {
                                    viewModel.submitReview(placeId, rating, comment, emptyList())
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = GymBroColors.Primary)
                            ) {
                                Text("Отправить")
                            }
                        }
                    }
                }
            }
        }
    }
}
