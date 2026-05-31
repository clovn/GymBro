package ru.itis.gymbro.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.StarHalf
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import ru.itis.gymbro.core.designsystem.theme.GymBroColors
import ru.itis.gymbro.core.designsystem.theme.GymBroTypography

@Composable
fun GymBroCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    if (onClick != null) {
        Card(
            onClick = onClick,
            modifier = modifier,
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = GymBroColors.Surface
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 2.dp
            ),
            border = androidx.compose.foundation.BorderStroke(1.dp, GymBroColors.Divider)
        ) {
            Box(modifier = Modifier.padding(16.dp)) {
                content()
            }
        }
    } else {
        Card(
            modifier = modifier,
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = GymBroColors.Surface
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 2.dp
            ),
            border = androidx.compose.foundation.BorderStroke(1.dp, GymBroColors.Divider)
        ) {
            Box(modifier = Modifier.padding(16.dp)) {
                content()
            }
        }
    }
}

@Composable
fun GymBroAvatar(
    name: String,
    avatarUrl: String?,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp
) {
    val firstChar = name.firstOrNull()?.uppercaseChar()?.toString() ?: "?"
    
    // Hash name to get a consistent soft background color for initials avatar
    val colorIndex = name.hashCode().coerceAtLeast(0) % 5
    val avatarBgColor = when (colorIndex) {
        0 -> Color(0xFFFEE2E2) // Light red
        1 -> Color(0xFFFEF3C7) // Light yellow
        2 -> Color(0xFFD1FAE5) // Light green
        3 -> Color(0xFFDBEAFE) // Light blue
        else -> Color(0xFFF3E8FF) // Light purple
    }
    val avatarTextColor = when (colorIndex) {
        0 -> Color(0xFF991B1B)
        1 -> Color(0xFF92400E)
        2 -> Color(0xFF065F46)
        3 -> Color(0xFF1E40AF)
        else -> Color(0xFF6B21A8)
    }

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(avatarBgColor),
        contentAlignment = Alignment.Center
    ) {
        if (!avatarUrl.isNullOrBlank()) {
            AsyncImage(
                model = avatarUrl,
                contentDescription = "Avatar $name",
                modifier = Modifier
                    .size(size)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        } else {
            Text(
                text = firstChar,
                color = avatarTextColor,
                style = GymBroTypography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = (size.value * 0.35).sp
                )
            )
        }
    }
}

@Composable
fun GymBroRatingBar(
    rating: Float,
    modifier: Modifier = Modifier,
    stars: Int = 5,
    starsColor: Color = Color(0xFFFFB300),
    starSize: Dp = 16.dp
) {
    val filledStars = rating.toInt()
    val halfStars = if (rating - filledStars >= 0.5f) 1 else 0
    val unfilledStars = stars - filledStars - halfStars

    Row(modifier = modifier) {
        repeat(filledStars) {
            Icon(
                imageVector = Icons.Filled.Star,
                contentDescription = null,
                tint = starsColor,
                modifier = Modifier.size(starSize)
            )
        }
        repeat(halfStars) {
            Icon(
                imageVector = Icons.Filled.StarHalf,
                contentDescription = null,
                tint = starsColor,
                modifier = Modifier.size(starSize)
            )
        }
        repeat(unfilledStars) {
            Icon(
                imageVector = Icons.Filled.StarBorder,
                contentDescription = null,
                tint = starsColor,
                modifier = Modifier.size(starSize)
            )
        }
    }
}
