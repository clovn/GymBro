package ru.itis.gymbro.core.network

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import ru.itis.gymbro.core.common.Resource
import ru.itis.gymbro.core.datastore.GymBroDataStore
import ru.itis.gymbro.core.domain.model.*
import ru.itis.gymbro.core.domain.repository.*
import ru.itis.gymbro.core.network.mock.*
import ru.itis.gymbro.core.network.retrofit.*

class DelegatingAuthRepository(
    private val mockRepo: MockAuthRepository,
    private val retrofitRepo: RetrofitAuthRepository,
    private val dataStore: GymBroDataStore
) : AuthRepository {

    private suspend fun getRepo(): AuthRepository {
        return if (dataStore.isDemoMode.first()) mockRepo else retrofitRepo
    }

    override suspend fun login(username: String, password: String): Resource<TokenData> =
        getRepo().login(username, password)

    override suspend fun register(
        username: String,
        email: String,
        password: String,
        firstName: String,
        lastName: String
    ): Resource<User> =
        getRepo().register(username, email, password, firstName, lastName)

    override suspend fun logout(): Resource<Unit> =
        getRepo().logout()

    override suspend fun getMe(): Resource<User> =
        getRepo().getMe()

    override suspend fun updateProfile(
        name: String,
        bio: String?,
        goal: String?,
        level: String?,
        preferredWorkouts: List<String>
    ): Resource<User> =
        getRepo().updateProfile(name, bio, goal, level, preferredWorkouts)

    override suspend fun hasActiveSession(): Boolean =
        getRepo().hasActiveSession()
}

class DelegatingGeoRepository(
    private val mockRepo: MockGeoRepository,
    private val retrofitRepo: RetrofitGeoRepository,
    private val dataStore: GymBroDataStore
) : GeoRepository {

    private suspend fun getRepo(): GeoRepository {
        return if (dataStore.isDemoMode.first()) mockRepo else retrofitRepo
    }

    override suspend fun getLocationsNearby(
        lat: Double,
        lon: Double,
        radius: Int,
        type: String?
    ): Resource<List<LocationSpot>> =
        getRepo().getLocationsNearby(lat, lon, radius, type)

    override suspend fun createLocation(
        name: String,
        description: String?,
        type: String,
        latitude: Double,
        longitude: Double,
        equipmentTags: List<String>
    ): Resource<LocationSpot> =
        getRepo().createLocation(name, description, type, latitude, longitude, equipmentTags)

    override suspend fun getPlaceDetails(id: Long): Resource<LocationSpot> =
        getRepo().getPlaceDetails(id)

    override suspend fun addReview(
        placeId: Long,
        rating: Int,
        comment: String?,
        tags: List<String>
    ): Resource<Review> =
        getRepo().addReview(placeId, rating, comment, tags)

    override suspend fun getReviews(placeId: Long, page: Int, size: Int): Resource<List<Review>> =
        getRepo().getReviews(placeId, page, size)

    override suspend fun getEventsNearby(
        lat: Double,
        lon: Double,
        radius: Int,
        workoutType: String?
    ): Resource<List<WorkoutEvent>> =
        getRepo().getEventsNearby(lat, lon, radius, workoutType)

    override suspend fun createEvent(
        locationId: Long,
        title: String,
        description: String?,
        workoutType: String,
        wishes: String?,
        startTime: String,
        endTime: String,
        maxParticipants: Int
    ): Resource<WorkoutEvent> =
        getRepo().createEvent(locationId, title, description, workoutType, wishes, startTime, endTime, maxParticipants)

    override suspend fun getEventDetails(id: Long): Resource<WorkoutEvent> =
        getRepo().getEventDetails(id)

    override suspend fun joinEvent(id: Long): Resource<Participant> =
        getRepo().joinEvent(id)

    override suspend fun confirmParticipant(eventId: Long, userId: String): Resource<Participant> =
        getRepo().confirmParticipant(eventId, userId)

    override suspend fun rejectParticipant(eventId: Long, userId: String): Resource<Participant> =
        getRepo().rejectParticipant(eventId, userId)

    override suspend fun completeEvent(id: Long): Resource<Unit> =
        getRepo().completeEvent(id)

    override suspend fun getMyEvents(status: String?): Resource<List<WorkoutEvent>> =
        getRepo().getMyEvents(status)

    override suspend fun geocodeAddress(address: String): Resource<GeocodeData> =
        getRepo().geocodeAddress(address)
}

class DelegatingChatRepository(
    private val mockRepo: MockChatRepository,
    // Real chat network repository can be added here if needed, otherwise fallback to mock
    private val dataStore: GymBroDataStore
) : ChatRepository {

    override suspend fun getConversations(page: Int, size: Int): Resource<List<Conversation>> =
        mockRepo.getConversations(page, size)

    override suspend fun getMessages(conversationId: String, page: Int, size: Int): Resource<List<ChatMessage>> =
        mockRepo.getMessages(conversationId, page, size)

    override suspend fun createConversation(userId: String): Resource<Conversation> =
        mockRepo.createConversation(userId)

    override suspend fun sendMessage(conversationId: String, text: String): Resource<ChatMessage> =
        mockRepo.sendMessage(conversationId, text)

    override fun observeWebSocketMessages(): Flow<ChatMessage> =
        mockRepo.observeWebSocketMessages()

    override fun observeConnectionState(): Flow<Boolean> =
        mockRepo.observeConnectionState()
}

class DelegatingNotificationRepository(
    private val mockRepo: MockNotificationRepository,
    private val dataStore: GymBroDataStore
) : NotificationRepository {

    override suspend fun getNotifications(page: Int, size: Int): Resource<List<GymNotification>> =
        mockRepo.getNotifications(page, size)

    override suspend fun markRead(id: String): Resource<Unit> =
        mockRepo.markRead(id)

    override suspend fun markAllRead(): Resource<Unit> =
        mockRepo.markAllRead()
}
