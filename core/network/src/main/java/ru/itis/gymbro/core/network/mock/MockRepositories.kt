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
        name = "Jordan Smith",
        avatarUrl = null,
        goal = "Fitness enthusiast | Calisthenics | NYC",
        level = "Pro",
        preferredWorkouts = listOf("STRENGTH", "CROSSFIT"),
        score = 85,
        workoutsCount = 267,
        reviewsCount = 4,
        bio = "Fitness enthusiast | Calisthenics | NYC"
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

    private val mockPeople = listOf(
        User("u1", "Петр Петров", goal = "Нарастить мышцы", level = "Продвинутый", score = 92, preferredWorkouts = listOf("STRENGTH", "CROSSFIT"), bio = "Занимаюсь силовым троеборьем 3 года. Жму 120 кг. Ищу напарника на субботы."),
        User("u2", "Мария Сидорова", goal = "Похудение", level = "Начальный", score = 15, preferredWorkouts = listOf("CARDIO"), bio = "Хочу сбросить вес к лету. Бегаю по вечерам, ищу компанию для совместных пробежек."),
        User("u3", "Ольга Смирнова", goal = "Гибкость и рельеф", level = "Средний", score = 78, preferredWorkouts = listOf("YOGA", "STRETCHING"), bio = "Сертифицированный инструктор по хатха-йоге. Буду рада совместным практикам в парке."),
        User("u4", "Алексей Ветров", goal = "Выносливость", level = "Начальный", score = 45, preferredWorkouts = listOf("CARDIO", "OTHER"), bio = "Начал готовиться к полумарафону. Бегаю по вечерам. Присоединяйтесь!")
    )

    override suspend fun getPeople(): Resource<List<User>> {
        delay(500)
        return Resource.Success(mockPeople)
    }

    override suspend fun getUserProfile(userId: String): Resource<User> {
        delay(300)
        val profile = mockPeople.find { it.id == userId } ?: User(userId, "Спортсмен", score = 10)
        return Resource.Success(profile)
    }
}

class MockGeoRepository : GeoRepository {
    
    private val spots = mutableListOf(
        LocationSpot(1, "Спорткомплекс Арена", "Современный зал с бассейном и кардио-зоной", "GYM", 55.751244, 37.618423, 4.8, 12, listOf("Штанги", "Тренажеры", "Бассейн"), listOf(), 450.0, true),
        LocationSpot(2, "Воркаут площадка Сокольники", "Открытая площадка в лесопарковой зоне", "WORKOUT", 55.791244, 37.678423, 4.5, 34, listOf("Турники", "Брусья", "Кольца"), listOf(), 1200.0, false),
        LocationSpot(3, "Фитнес Лайф", "Небольшой уютный клуб у метро", "GYM", 55.7658, 37.6173, 4.2, 8, listOf("Гантели", "Беговые дорожки"), listOf(), 150.0, true),
        LocationSpot(4, "Стадион Локомотив", "Беговые дорожки под открытым небом", "STADIUM", 55.7758, 37.6373, 4.6, 21, listOf("Беговая дорожка", "Турники"), listOf(), 800.0, false),
        LocationSpot(5, "Воркаут Парк Горького", "Легендарная площадка у набережной с отличным видом", "WORKOUT", 55.7297, 37.6014, 4.9, 87, listOf("Турники", "Брусья", "Шведская стенка", "Канаты"), listOf(), 1500.0, false),
        LocationSpot(6, "Кроссфит Бокс Ракета", "Профессиональный кроссфит-зал с сертифицированными тренерами", "GYM", 55.7592, 37.6251, 4.7, 45, listOf("Помосты", "Гири", "Гребные тренажеры", "Пегборды"), listOf(), 600.0, true),
        LocationSpot(7, "Зал Тяжелой Атлетики Динамо", "Классический тяжелоатлетический зал для любителей железа", "GYM", 55.7831, 37.5583, 4.4, 19, listOf("Помосты", "Олимпийские грифы", "Блины"), listOf(), 900.0, true),
        LocationSpot(8, "Спортзал МГУ", "Университетский спортивный манеж", "STADIUM", 55.7003, 37.5312, 4.3, 30, listOf("Беговая дорожка", "Сектора для прыжков"), listOf(), 2500.0, false)
    )

    private val reviews = mutableMapOf<Long, MutableList<Review>>(
        1L to mutableListOf(
            Review(101, 5, "Отличный чистый зал, вежливый персонал.", listOf("чисто", "персонал"), "2026-05-30T12:00:00Z", User("u1", "Петр Петров", score = 32)),
            Review(102, 4, "Много тренажеров, но в час пик бывает душно.", listOf("многолюдно", "вентиляция"), "2026-05-29T18:30:00Z", User("u2", "Мария Сидорова", score = 15))
        ),
        2L to mutableListOf(
            Review(201, 5, "Лучшие турники в городе! Всегда чисто и есть свободные снаряды.", listOf("турники", "чисто"), "2026-05-28T10:15:00Z", User("u3", "Ольга Смирнова", score = 56)),
            Review(202, 5, "Шикарное покрытие, брусья разной ширины. Рекомендую!", listOf("покрытие", "брусья"), "2026-05-27T14:40:00Z", User("u4", "Дмитрий Кузнецов", score = 78))
        ),
        3L to mutableListOf(
            Review(301, 4, "Уютный зал, цены не кусаются. Мало беговых дорожек.", listOf("уют", "цена"), "2026-05-26T09:00:00Z", User("u5", "Елена Васильева", score = 24))
        ),
        5L to mutableListOf(
            Review(501, 5, "Просто супер! Вид на Москву-реку вдохновляет на рекорды.", listOf("вид", "атмосфера"), "2026-05-25T19:20:00Z", User("u6", "Алексей Смирнов", score = 95)),
            Review(502, 5, "Оборудование новое, ночью горит подсветка.", listOf("подсветка", "новое оборудование"), "2026-05-24T22:10:00Z", User("u7", "Артем Соколов", score = 110))
        )
    )

    private val events = mutableListOf(
        WorkoutEvent(
            id = 10,
            title = "Силовая тренировка на плечи/грудь",
            description = "Жмем лежа, качаем плечи. Ждем всех уровней.",
            workoutType = "POWER",
            wishes = "Возьмите с собой полотенце и воду",
            startTime = "2026-06-07T18:00:00Z",
            endTime = "2026-06-07T19:30:00Z",
            maxParticipants = 5,
            status = "ACTIVE",
            host = User("u1", "Петр Петров", score = 32),
            participants = mutableListOf(
                Participant("u1", "Петр Петров", status = "CONFIRMED", joinedAt = "2026-06-05T12:00:00Z"),
                Participant("u2", "Мария Сидорова", status = "CONFIRMED", joinedAt = "2026-06-05T14:00:00Z")
            ),
            location = spots[0]
        ),
        WorkoutEvent(
            id = 20,
            title = "Йога на открытом воздухе",
            description = "Мягкая хатха-йога на траве в парке. Коврики свои.",
            workoutType = "YOGA",
            startTime = "2026-06-08T09:00:00Z",
            endTime = "2026-06-08T10:30:00Z",
            maxParticipants = 10,
            status = "ACTIVE",
            host = User("u3", "Ольга Смирнова", score = 56),
            participants = mutableListOf(
                Participant("u3", "Ольга Смирнова", status = "CONFIRMED", joinedAt = "2026-06-04T10:00:00Z")
            ),
            location = spots[1]
        ),
        WorkoutEvent(
            id = 30,
            title = "Кроссфит WOD: Субботняя жара",
            description = "Интенсивный круговой комплекс на выносливость. Подходит для продолжающих.",
            workoutType = "CROSSFIT",
            wishes = "Хорошее настроение и спортивная обувь",
            startTime = "2026-06-13T12:00:00Z",
            endTime = "2026-06-13T13:30:00Z",
            maxParticipants = 8,
            status = "ACTIVE",
            host = User("u4", "Дмитрий Кузнецов", score = 78),
            participants = mutableListOf(
                Participant("u4", "Дмитрий Кузнецов", status = "CONFIRMED", joinedAt = "2026-06-04T12:00:00Z")
            ),
            location = spots[5]
        ),
        WorkoutEvent(
            id = 40,
            title = "Кардио пробежка 10км",
            description = "Бежим в легком темпе (6:00 мин/км) по набережной.",
            workoutType = "RUN",
            startTime = "2026-06-09T08:00:00Z",
            endTime = "2026-06-09T09:15:00Z",
            maxParticipants = 15,
            status = "ACTIVE",
            host = User("u6", "Алексей Смирнов", score = 95),
            participants = mutableListOf(
                Participant("u6", "Алексей Смирнов", status = "CONFIRMED", joinedAt = "2026-06-05T08:00:00Z")
            ),
            location = spots[7]
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
        Conversation("u1", "Петр Петров", null, "Увидимся в зале в 6 вечера!", System.currentTimeMillis() - 2 * 60 * 1000, 2),
        Conversation("u3", "Ольга Смирнова", null, "Отличная была тренировка сегодня!", System.currentTimeMillis() - 15 * 60 * 1000, 0),
        Conversation("u2", "Мария Сидорова", null, "Можешь показать правильную технику?", System.currentTimeMillis() - 60 * 60 * 1000, 1),
        Conversation("u4", "Алексей Ветров", null, "Бежим завтра утром?", System.currentTimeMillis() - 180 * 60 * 1000, 0)
    )

    private val messages = mutableMapOf(
        "u1" to mutableListOf(
            ChatMessage("m1", "u1", "u1", "Увидимся в зале в 6 вечера!", System.currentTimeMillis() - 2 * 60 * 1000, "SENT")
        ),
        "u3" to mutableListOf(
            ChatMessage("m2", "u3", "u3", "Отличная была тренировка сегодня!", System.currentTimeMillis() - 15 * 60 * 1000, "READ")
        ),
        "u2" to mutableListOf(
            ChatMessage("m3", "u2", "u2", "Можешь показать правильную технику?", System.currentTimeMillis() - 60 * 60 * 1000, "SENT")
        ),
        "u4" to mutableListOf(
            ChatMessage("m4", "u4", "u4", "Бежим завтра утром?", System.currentTimeMillis() - 180 * 60 * 1000, "READ")
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
        
        val partnerName = when (userId) {
            "u1" -> "Петр Петров"
            "u2" -> "Мария Сидорова"
            "u3" -> "Ольга Смирнова"
            "u4" -> "Алексей Ветров"
            else -> "Спортсмен / GymBro"
        }
        val newConv = Conversation(userId, partnerName, null, "Диалог начат", System.currentTimeMillis(), 0)
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
        GymNotification("n2", "Новый отзыв о месте", "Ольга оставила отзыв о Воркаут площадке Сокольники", System.currentTimeMillis() - 7200000, true, "REVIEW", "2"),
        GymNotification("n3", "Ваш запрос подтвержден", "Дмитрий подтвердил ваше участие в 'Кроссфит WOD: Субботняя жара'", System.currentTimeMillis() - 3600000, false, "EVENT", "30"),
        GymNotification("n4", "Новое сообщение", "Alex Rivera прислал вам сообщение в чате", System.currentTimeMillis() - 120000, false, "CHAT", "c1"),
        GymNotification("n5", "Напоминание о тренировке", "Пробежка 10км начнется через 2 часа", System.currentTimeMillis() - 1800000, true, "EVENT", "40")
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
