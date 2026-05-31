package ru.itis.gymbro.core.network.mock

import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flow
import ru.itis.gymbro.core.common.Resource
import ru.itis.gymbro.core.common.UiError
import ru.itis.gymbro.core.domain.model.*
import ru.itis.gymbro.core.domain.repository.*

class MockAuthRepository : AuthRepository {
    private var currentUser = User(
        id = "550e8400-e29b-41d4-a716-446655440000",
        name = "Иван Иванов",
        avatarUrl = null,
        goal = "Набрать мышечную массу",
        level = "Средний",
        preferredWorkouts = listOf("STRENGTH", "CROSSFIT"),
        score = 85,
        workoutsCount = 24,
        reviewsCount = 4,
        bio = "Люблю приседать со штангой и бегать по утрам. Ищу напарника для тренировок в Сокольниках."
    )

    override suspend fun login(username: String, password: String): Resource<TokenData> {
        delay(1000)
        return if (username.length >= 3 && password.length >= 4) {
            Resource.Success(TokenData("mock_access_token", "mock_refresh_token", 3600))
        } else {
            Resource.Error(UiError.Validation(mapOf("username" to "Неверные учетные данные")))
        }
    }

    override suspend fun register(
        username: String,
        email: String,
        password: String,
        firstName: String,
        lastName: String
    ): Resource<User> {
        delay(1000)
        currentUser = currentUser.copy(name = "$firstName $lastName")
        return Resource.Success(currentUser)
    }

    override suspend fun logout(): Resource<Unit> {
        return Resource.Success(Unit)
    }

    override suspend fun getMe(): Resource<User> {
        return Resource.Success(currentUser)
    }

    override suspend fun updateProfile(
        name: String,
        bio: String?,
        goal: String?,
        level: String?,
        preferredWorkouts: List<String>
    ): Resource<User> {
        currentUser = currentUser.copy(
            name = name,
            bio = bio,
            goal = goal,
            level = level,
            preferredWorkouts = preferredWorkouts
        )
        return Resource.Success(currentUser)
    }

    override suspend fun hasActiveSession(): Boolean {
        return true
    }
}

class MockGeoRepository : GeoRepository {
    
    private val spots = mutableListOf(
        LocationSpot(1, "Спорткомплекс Арена", "Современный зал с бассейном и кардио-зоной", "GYM", 55.751244, 37.618423, 4.8, 12, listOf("Штанги", "Тренажеры", "Бассейн"), listOf(), 450.0, true),
        LocationSpot(2, "Воркаут площадка Сокольники", "Открытая площадка в лесопарковой зоне", "WORKOUT", 55.791244, 37.678423, 4.5, 34, listOf("Турники", "Брусья", "Кольца"), listOf(), 1200.0, false),
        LocationSpot(3, "Фитнес Лайф", "Небольшой уютный клуб у метро", "GYM", 55.7658, 37.6173, 4.2, 8, listOf("Гантели", "Беговые дорожки"), listOf(), 150.0, true),
        LocationSpot(4, "Стадион Локомотив", "Беговые дорожки под открытым небом", "STADIUM", 55.7758, 37.6373, 4.6, 21, listOf("Беговая дорожка", "Турники"), listOf(), 800.0, false)
    )

    private val reviews = mutableMapOf<Long, MutableList<Review>>(
        1L to mutableListOf(
            Review(101, 5, "Отличный чистый зал, вежливый персонал.", listOf("чисто"), "2026-05-30T12:00:00Z", User("u1", "Петр Петров", score = 32)),
            Review(102, 4, "Много тренажеров, но в час пик бывает душно.", listOf("многолюдно"), "2026-05-29T18:30:00Z", User("u2", "Мария Сидорова", score = 15))
        )
    )

    private val events = mutableListOf(
        WorkoutEvent(
            id = 10,
            title = "Силовая тренировка на плечи/грудь",
            description = "Жмем лежа, качаем плечи. Ждем всех уровней.",
            workoutType = "POWER",
            wishes = "Возьмите с собой полотенце и воду",
            startTime = "2026-06-01T18:00:00Z",
            endTime = "2026-06-01T19:30:00Z",
            maxParticipants = 5,
            status = "ACTIVE",
            host = User("u1", "Петр Петров", score = 32),
            participants = mutableListOf(
                Participant("u1", "Петр Петров", status = "CONFIRMED", joinedAt = "2026-05-30T12:00:00Z"),
                Participant("u2", "Мария Сидорова", status = "CONFIRMED", joinedAt = "2026-05-30T14:00:00Z")
            ),
            location = spots[0]
        ),
        WorkoutEvent(
            id = 20,
            title = "Йога на открытом воздухе",
            description = "Мягкая хатха-йога на траве в парке. Коврики свои.",
            workoutType = "YOGA",
            startTime = "2026-06-02T09:00:00Z",
            endTime = "2026-06-02T10:30:00Z",
            maxParticipants = 10,
            status = "ACTIVE",
            host = User("u3", "Ольга Смирнова", score = 56),
            participants = mutableListOf(
                Participant("u3", "Ольга Смирнова", status = "CONFIRMED", joinedAt = "2026-05-29T10:00:00Z")
            ),
            location = spots[1]
        )
    )

    override suspend fun getLocationsNearby(lat: Double, lon: Double, radius: Int, type: String?): Resource<List<LocationSpot>> {
        delay(500)
        return Resource.Success(if (type != null) spots.filter { it.type == type } else spots)
    }

    override suspend fun createLocation(name: String, description: String?, type: String, latitude: Double, longitude: Double, equipmentTags: List<String>): Resource<LocationSpot> {
        val newSpot = LocationSpot(
            id = (spots.size + 1).toLong(),
            name = name,
            description = description,
            type = type,
            latitude = latitude,
            longitude = longitude,
            equipmentTags = equipmentTags,
            distance = 50.0
        )
        spots.add(newSpot)
        return Resource.Success(newSpot)
    }

    override suspend fun getPlaceDetails(id: Long): Resource<LocationSpot> {
        val spot = spots.find { it.id == id } ?: return Resource.Error(UiError.Server(404, "Место не найдено"))
        return Resource.Success(spot)
    }

    override suspend fun addReview(placeId: Long, rating: Int, comment: String?, tags: List<String>): Resource<Review> {
        val list = reviews.getOrPut(placeId) { mutableListOf() }
        val newReview = Review(
            id = System.currentTimeMillis(),
            rating = rating,
            comment = comment,
            tags = tags,
            createdAt = "2026-05-31T16:00:00Z",
            author = User("me", "Иван Иванов", score = 85)
        )
        list.add(newReview)
        return Resource.Success(newReview)
    }

    override suspend fun getReviews(placeId: Long, page: Int, size: Int): Resource<List<Review>> {
        return Resource.Success(reviews[placeId] ?: emptyList())
    }

    override suspend fun getEventsNearby(lat: Double, lon: Double, radius: Int, workoutType: String?): Resource<List<WorkoutEvent>> {
        delay(500)
        return Resource.Success(if (workoutType != null) events.filter { it.workoutType == workoutType } else events)
    }

    override suspend fun createEvent(
        locationId: Long,
        title: String,
        description: String?,
        workoutType: String,
        wishes: String?,
        startTime: String,
        endTime: String,
        maxParticipants: Int
    ): Resource<WorkoutEvent> {
        val location = spots.find { it.id == locationId } ?: spots[0]
        val newEvent = WorkoutEvent(
            id = (events.size + 1).toLong() * 10,
            title = title,
            description = description,
            workoutType = workoutType,
            wishes = wishes,
            startTime = startTime,
            endTime = endTime,
            maxParticipants = maxParticipants,
            status = "ACTIVE",
            host = User("me", "Иван Иванов", score = 85),
            location = location
        )
        events.add(newEvent)
        return Resource.Success(newEvent)
    }

    override suspend fun getEventDetails(id: Long): Resource<WorkoutEvent> {
        val event = events.find { it.id == id } ?: return Resource.Error(UiError.Server(404, "Тренировка не найдена"))
        return Resource.Success(event)
    }

    override suspend fun joinEvent(id: Long): Resource<Participant> {
        val event = events.find { it.id == id } ?: return Resource.Error(UiError.Server(404, "Тренировка не найдена"))
        val p = Participant("me", "Иван Иванов", status = "CONFIRMED", joinedAt = "2026-05-31T16:00:00Z")
        val updatedParticipants = event.participants.toMutableList()
        if (updatedParticipants.none { it.userId == "me" }) {
            updatedParticipants.add(p)
            events[events.indexOf(event)] = event.copy(participants = updatedParticipants)
        }
        return Resource.Success(p)
    }

    override suspend fun confirmParticipant(eventId: Long, userId: String): Resource<Participant> {
        val event = events.find { it.id == eventId } ?: return Resource.Error(UiError.Server(404, "Тренировка не найдена"))
        val updated = event.participants.map {
            if (it.userId == userId) it.copy(status = "CONFIRMED") else it
        }
        events[events.indexOf(event)] = event.copy(participants = updated)
        return Resource.Success(updated.first { it.userId == userId })
    }

    override suspend fun rejectParticipant(eventId: Long, userId: String): Resource<Participant> {
        val event = events.find { it.id == eventId } ?: return Resource.Error(UiError.Server(404, "Тренировка не найдена"))
        val updated = event.participants.map {
            if (it.userId == userId) it.copy(status = "REJECTED") else it
        }
        events[events.indexOf(event)] = event.copy(participants = updated)
        return Resource.Success(updated.first { it.userId == userId })
    }

    override suspend fun completeEvent(id: Long): Resource<Unit> {
        val event = events.find { it.id == id } ?: return Resource.Error(UiError.Server(404, "Тренировка не найдена"))
        events[events.indexOf(event)] = event.copy(status = "COMPLETED")
        return Resource.Success(Unit)
    }

    override suspend fun getMyEvents(status: String?): Resource<List<WorkoutEvent>> {
        return Resource.Success(if (status != null) events.filter { it.status == status } else events)
    }

    override suspend fun geocodeAddress(address: String): Resource<GeocodeData> {
        return Resource.Success(GeocodeData(55.7558, 37.6173, address))
    }
}

class MockChatRepository : ChatRepository {
    
    private val webSocketMessages = MutableSharedFlow<ChatMessage>(extraBufferCapacity = 10)
    
    private val conversations = mutableListOf(
        Conversation("c1", "Петр Петров", null, "Привет! Пойдешь сегодня качаться?", System.currentTimeMillis() - 600000, 1),
        Conversation("c2", "Ольга Смирнова", null, "Отличная йога была, спасибо!", System.currentTimeMillis() - 3600000, 0)
    )

    private val messages = mutableMapOf(
        "c1" to mutableListOf(
            ChatMessage("m1", "c1", "u1", "Привет! Пойдешь сегодня качаться?", System.currentTimeMillis() - 600000, "SENT")
        ),
        "c2" to mutableListOf(
            ChatMessage("m2", "c2", "u3", "Отличная йога была, спасибо!", System.currentTimeMillis() - 3600000, "READ")
        )
    )

    override suspend fun getConversations(page: Int, size: Int): Resource<List<Conversation>> {
        return Resource.Success(conversations)
    }

    override suspend fun getMessages(conversationId: String, page: Int, size: Int): Resource<List<ChatMessage>> {
        return Resource.Success(messages[conversationId] ?: emptyList())
    }

    override suspend fun createConversation(userId: String): Resource<Conversation> {
        val existing = conversations.find { it.id == userId }
        if (existing != null) return Resource.Success(existing)
        
        val newConv = Conversation(userId, "Тренер / GymBro", null, "Диалог начат", System.currentTimeMillis(), 0)
        conversations.add(0, newConv)
        messages[userId] = mutableListOf()
        return Resource.Success(newConv)
    }

    override suspend fun sendMessage(conversationId: String, text: String): Resource<ChatMessage> {
        val msg = ChatMessage(
            id = System.currentTimeMillis().toString(),
            conversationId = conversationId,
            senderId = "me",
            text = text,
            timestamp = System.currentTimeMillis(),
            status = "SENT",
            isOutgoing = true
        )
        
        // Save locally
        messages.getOrPut(conversationId) { mutableListOf() }.add(msg)
        
        // Update last message in conversation
        val index = conversations.indexOfFirst { it.id == conversationId }
        if (index != -1) {
            conversations[index] = conversations[index].copy(
                lastMessage = text,
                timestamp = System.currentTimeMillis()
            )
        }

        // Simulate reply from the other person
        val targetConv = conversations.find { it.id == conversationId }
        val partnerName = targetConv?.name ?: "Собеседник"
        
        // Run in background / fire websocket event after 1.5s
        val responseText = when {
            text.contains("привет", ignoreCase = true) -> "Привет! Как тренировка?"
            text.contains("когда", ignoreCase = true) -> "Давай сегодня в 18:30 в Арене."
            else -> "Супер, до встречи на тренировке!"
        }

        @OptIn(kotlinx.coroutines.DelicateCoroutinesApi::class)
        kotlinx.coroutines.GlobalScope.launch {
            delay(1500)
            val reply = ChatMessage(
                id = (System.currentTimeMillis() + 1).toString(),
                conversationId = conversationId,
                senderId = conversationId,
                text = responseText,
                timestamp = System.currentTimeMillis(),
                status = "SENT",
                isOutgoing = false
            )
            messages[conversationId]?.add(reply)
            val convIndex = conversations.indexOfFirst { it.id == conversationId }
            if (convIndex != -1) {
                conversations[convIndex] = conversations[convIndex].copy(
                    lastMessage = responseText,
                    timestamp = System.currentTimeMillis()
                )
            }
            webSocketMessages.emit(reply)
        }

        return Resource.Success(msg)
    }

    override fun observeWebSocketMessages(): Flow<ChatMessage> {
        return webSocketMessages
    }

    override fun observeConnectionState(): Flow<Boolean> {
        return flow {
            emit(true)
        }
    }
}

class MockNotificationRepository : NotificationRepository {
    
    private val list = mutableListOf(
        GymNotification("n1", "Новая тренировка рядом", "Петр создал тренировку 'Силовая на плечи' в Спорткомплекс Арена", System.currentTimeMillis() - 1200000, false, "EVENT", "10"),
        GymNotification("n2", "Новый отзыв о месте", "Ольга оставила отзыв о Воркаут площадке Сокольники", System.currentTimeMillis() - 7200000, true, "REVIEW", "2")
    )

    override suspend fun getNotifications(page: Int, size: Int): Resource<List<GymNotification>> {
        return Resource.Success(list)
    }

    override suspend fun markRead(id: String): Resource<Unit> {
        val index = list.indexOfFirst { it.id == id }
        if (index != -1) {
            list[index] = list[index].copy(isRead = true)
        }
        return Resource.Success(Unit)
    }

    override suspend fun markAllRead(): Resource<Unit> {
        for (i in list.indices) {
            list[i] = list[i].copy(isRead = true)
        }
        return Resource.Success(Unit)
    }
}
