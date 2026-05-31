package ru.itis.gymbro.core.domain.repository

import kotlinx.coroutines.flow.Flow
import ru.itis.gymbro.core.common.Resource
import ru.itis.gymbro.core.domain.model.*

interface AuthRepository {
    suspend fun login(username: String, password: String): Resource<TokenData>
    suspend fun register(username: String, email: String, password: String, firstName: String, lastName: String): Resource<User>
    suspend fun logout(): Resource<Unit>
    suspend fun getMe(): Resource<User>
    suspend fun updateProfile(name: String, bio: String?, goal: String?, level: String?, preferredWorkouts: List<String>): Resource<User>
    suspend fun hasActiveSession(): Boolean
}

data class TokenData(
    val accessToken: String,
    val refreshToken: String,
    val expiresIn: Long
)

interface GeoRepository {
    suspend fun getLocationsNearby(lat: Double, lon: Double, radius: Int, type: String?): Resource<List<LocationSpot>>
    suspend fun createLocation(name: String, description: String?, type: String, latitude: Double, longitude: Double, equipmentTags: List<String>): Resource<LocationSpot>
    suspend fun getPlaceDetails(id: Long): Resource<LocationSpot>
    suspend fun addReview(placeId: Long, rating: Int, comment: String?, tags: List<String>): Resource<Review>
    suspend fun getReviews(placeId: Long, page: Int, size: Int): Resource<List<Review>>
    
    suspend fun getEventsNearby(lat: Double, lon: Double, radius: Int, workoutType: String?): Resource<List<WorkoutEvent>>
    suspend fun createEvent(locationId: Long, title: String, description: String?, workoutType: String, wishes: String?, startTime: String, endTime: String, maxParticipants: Int): Resource<WorkoutEvent>
    suspend fun getEventDetails(id: Long): Resource<WorkoutEvent>
    
    suspend fun joinEvent(id: Long): Resource<Participant>
    suspend fun confirmParticipant(eventId: Long, userId: String): Resource<Participant>
    suspend fun rejectParticipant(eventId: Long, userId: String): Resource<Participant>
    suspend fun completeEvent(id: Long): Resource<Unit>
    suspend fun getMyEvents(status: String?): Resource<List<WorkoutEvent>>
    
    suspend fun geocodeAddress(address: String): Resource<GeocodeData>
}

data class GeocodeData(
    val latitude: Double,
    val longitude: Double,
    val formattedAddress: String
)

interface ChatRepository {
    suspend fun getConversations(page: Int, size: Int): Resource<List<Conversation>>
    suspend fun getMessages(conversationId: String, page: Int, size: Int): Resource<List<ChatMessage>>
    suspend fun createConversation(userId: String): Resource<Conversation>
    suspend fun sendMessage(conversationId: String, text: String): Resource<ChatMessage>
    
    fun observeWebSocketMessages(): Flow<ChatMessage>
    fun observeConnectionState(): Flow<Boolean>
}

interface NotificationRepository {
    suspend fun getNotifications(page: Int, size: Int): Resource<List<GymNotification>>
    suspend fun markRead(id: String): Resource<Unit>
    suspend fun markAllRead(): Resource<Unit>
}
