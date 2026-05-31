package ru.itis.gymbro.core.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: String,
    val name: String,
    val avatarUrl: String? = null,
    val goal: String? = null,
    val level: String? = null,
    val preferredWorkouts: List<String> = emptyList(),
    val score: Int = 0,
    val workoutsCount: Int = 0,
    val reviewsCount: Int = 0,
    val bio: String? = null
)

@Serializable
data class LocationSpot(
    val id: Long,
    val name: String,
    val description: String? = null,
    val type: String, // GYM, WORKOUT, POOL, STADIUM, OTHER
    val latitude: Double,
    val longitude: Double,
    val avgRating: Double = 0.0,
    val reviewCount: Int = 0,
    val equipmentTags: List<String> = emptyList(),
    val photoUrls: List<String> = emptyList(),
    val distance: Double? = null,
    val isVerified: Boolean = false
)

@Serializable
data class Participant(
    val userId: String,
    val name: String,
    val avatarUrl: String? = null,
    val status: String, // PENDING, CONFIRMED, REJECTED
    val joinedAt: String
)

@Serializable
data class WorkoutEvent(
    val id: Long,
    val title: String,
    val description: String? = null,
    val workoutType: String, // POWER, CARDIO, WORKOUT, CROSSFIT, YOGA, STRETCHING, OTHER
    val wishes: String? = null,
    val startTime: String,
    val endTime: String,
    val maxParticipants: Int,
    val status: String, // ACTIVE, CANCELLED, COMPLETED
    val host: User,
    val participants: List<Participant> = emptyList(),
    val location: LocationSpot,
    val distance: Double? = null
)

@Serializable
data class ChatMessage(
    val id: String,
    val conversationId: String,
    val senderId: String,
    val text: String,
    val timestamp: Long,
    val status: String, // PENDING, SENT, READ
    val isOutgoing: Boolean = false
)

@Serializable
data class Conversation(
    val id: String,
    val name: String,
    val avatarUrl: String? = null,
    val lastMessage: String? = null,
    val timestamp: Long = 0L,
    val unreadCount: Int = 0
)

@Serializable
data class GymNotification(
    val id: String,
    val title: String,
    val text: String,
    val timestamp: Long,
    val isRead: Boolean = false,
    val type: String,
    val referenceId: String? = null
)

@Serializable
data class Review(
    val id: Long,
    val rating: Int,
    val comment: String? = null,
    val tags: List<String> = emptyList(),
    val createdAt: String,
    val author: User
)
