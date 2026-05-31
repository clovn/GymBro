package ru.itis.gymbro.feature.place

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Star
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

    Scaffold { padding ->
        if (state.isLoading && state.place == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            val spot = state.place ?: ru.itis.gymbro.core.domain.model.LocationSpot(
                id = placeId,
                name = "Iron Paradise Gym",
                type = "GYM",
                latitude = 55.7558,
                longitude = 37.6173,
                avgRating = 4.8,
                reviewCount = 124,
                description = "Best gym in the area! Great equipment and friendly staff."
            )
            
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(GymBroColors.Background)
            ) {
                // Header Image Panel with Overlaid controls
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                        .background(Color(0xFF1E3A8A)) // Deep blue gradient placeholder
                ) {
                    // Custom illustration or aesthetic content
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(bottom = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("💪", fontSize = 64.sp)
                    }

                    // Top Action Overlay row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 48.dp, start = 16.dp, end = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Back Icon Box
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

                        // Right action boxes
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color.White)
                                    .clickable { },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FavoriteBorder,
                                    contentDescription = "Favorite",
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
                                    imageVector = Icons.Default.MoreVert,
                                    contentDescription = "Options",
                                    tint = GymBroColors.TextPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
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
                    item {
                        Column {
                            Text(
                                text = spot.name,
                                style = GymBroTypography.headlineMedium.copy(fontWeight = FontWeight.Bold, fontSize = 24.sp),
                                color = GymBroColors.TextPrimary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(GymBroColors.PrimaryLight)
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = if (spot.type == "GYM") "Gym" else "Outdoor",
                                        color = GymBroColors.Primary,
                                        style = GymBroTypography.labelLarge.copy(fontWeight = FontWeight.Bold)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Icon(imageVector = Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFB300), modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "4.8 (${spot.reviewCount} reviews)",
                                    style = GymBroTypography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                                    color = GymBroColors.TextSecondary
                                )
                            }
                        }
                    }

                    // Contact & Details panel
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            DetailRowItem(icon = Icons.Default.Place, label = "123 Fitness Ave, Downtown")
                            DetailRowItem(icon = Icons.Default.Schedule, label = "6:00 AM - 11:00 PM")
                            DetailRowItem(icon = Icons.Default.Phone, label = "+1 555-0123")
                        }
                    }

                    // Action buttons (directions, chat, plan workout)
                    item {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Button(
                                    onClick = { },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(52.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = GymBroColors.Primary),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(imageVector = Icons.Default.Place, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Get Directions", style = GymBroTypography.labelLarge.copy(color = Color.White))
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
                                        imageVector = Icons.Default.Forum,
                                        contentDescription = "Chat",
                                        tint = GymBroColors.TextPrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }

                            Button(
                                onClick = { onPlanWorkout(spot.id) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = GymBroColors.Primary),
                                border = androidx.compose.foundation.BorderStroke(1.dp, GymBroColors.Primary),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(imageVector = Icons.Default.FitnessCenter, contentDescription = null, tint = GymBroColors.Primary, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Plan a workout here", style = GymBroTypography.labelLarge.copy(color = GymBroColors.Primary))
                                }
                            }
                        }
                    }

                    // Reviews header
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Reviews",
                                style = GymBroTypography.titleLarge.copy(fontWeight = FontWeight.Bold)
                            )
                            GymBroTextButton(text = "See all", onClick = { showReviewDialog = true })
                        }
                    }

                    // Reviews List
                    val mockReviews = listOf(
                        ru.itis.gymbro.core.domain.model.Review(
                            1, 5, "Best gym in the area! Great equipment and friendly staff.", emptyList(), "Today",
                            ru.itis.gymbro.core.domain.model.User("r1", "Alex Rivera")
                        ),
                        ru.itis.gymbro.core.domain.model.Review(
                            2, 4, "Clean and well-maintained. Could use more squat racks.", emptyList(), "Yesterday",
                            ru.itis.gymbro.core.domain.model.User("r2", "Sarah Chen")
                        )
                    )

                    items(mockReviews) { review ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                GymBroAvatar(name = review.author.name, avatarUrl = review.author.avatarUrl, size = 36.dp)
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = review.author.name,
                                        style = GymBroTypography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = GymBroColors.TextPrimary
                                    )
                                    GymBroRatingBar(rating = review.rating.toFloat(), starSize = 12.dp)
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = review.comment ?: "",
                                style = GymBroTypography.bodyLarge,
                                color = GymBroColors.TextSecondary
                            )
                        }
                    }

                    item {
                        Text(
                            text = "Upcoming Workouts",
                            style = GymBroTypography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.padding(top = 16.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = GymBroColors.SurfaceVariant)
                        ) {
                            Box(modifier = Modifier.padding(16.dp)) {
                                Text("No workouts planned yet. Tap 'Plan a workout here' to start one!", style = GymBroTypography.bodyMedium)
                            }
                        }
                    }
                }
            }
        }
    }

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
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text("Write a Review", style = GymBroTypography.titleLarge.copy(fontWeight = FontWeight.Bold))
                    
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        (1..5).forEach { star ->
                            val active = star <= rating
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = if (active) Color(0xFFFFB300) else GymBroColors.Divider,
                                modifier = Modifier
                                    .size(32.dp)
                                    .clickable { rating = star }
                            )
                        }
                    }

                    GymBroTextField(
                        value = comment,
                        onValueChange = { comment = it },
                        label = "Comment",
                        placeholder = "Tell us about your experience...",
                        singleLine = false
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = { showReviewDialog = false },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = GymBroColors.SurfaceVariant, contentColor = GymBroColors.TextPrimary)
                        ) {
                            Text("Cancel")
                        }
                        Button(
                            onClick = {
                                viewModel.submitReview(placeId, rating, comment, emptyList())
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = GymBroColors.Primary)
                        ) {
                            Text("Submit")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DetailRowItem(icon: ImageVector, label: String) {
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
            text = label,
            style = GymBroTypography.bodyLarge,
            color = GymBroColors.TextPrimary
        )
    }
}
