package ru.itis.gymbro.core.network.retrofit

import okhttp3.MultipartBody
import retrofit2.Response
import ru.itis.gymbro.core.common.Resource
import ru.itis.gymbro.core.common.UiError
import ru.itis.gymbro.core.domain.model.*
import ru.itis.gymbro.core.domain.repository.*
import ru.itis.gymbro.core.network.api.*
import ru.itis.gymbro.core.network.mapper.*

import ru.itis.gymbro.core.network.storage.TokenStorage

suspend fun <T, R> safeApiCall(
    call: suspend () -> Response<T>,
    map: (T) -> R
): Resource<R> {
    return try {
        val response = call()
        if (response.isSuccessful) {
            val body = response.body()
            if (body != null) {
                Resource.Success(map(body))
            } else if (response.code() == 204) {
                @Suppress("UNCHECKED_CAST")
                Resource.Success(Unit as R)
            } else {
                Resource.Error(UiError.Server(response.code(), "Пустой ответ сервера"))
            }
        } else {
            val errorMsg = response.errorBody()?.string() ?: "Неизвестная ошибка сервера"
            Resource.Error(UiError.Server(response.code(), errorMsg))
        }
    } catch (e: Exception) {
        Resource.Error(UiError.Network(e.localizedMessage))
    }
}

class RetrofitAuthRepository(
    private val authApi: AuthApi,
    private val tokenStorage: TokenStorage
) : AuthRepository {
    
    override suspend fun login(username: String, password: String): Resource<TokenData> {
        return safeApiCall(
            call = { authApi.login(LoginRequest(username, password)) },
            map = { TokenData(it.access_token, it.refresh_token, it.expires_in) }
        )
    }

    override suspend fun register(
        username: String,
        email: String,
        password: String,
        firstName: String,
        lastName: String
    ): Resource<User> {
        return safeApiCall(
            call = { authApi.register(RegisterRequest(username, email, password, firstName, lastName)) },
            map = { User(it.userId, it.username, null) }
        )
    }

    override suspend fun logout(): Resource<Unit> {
        tokenStorage.clear()
        return Resource.Success(Unit)
    }

    override suspend fun getMe(): Resource<User> {
        return safeApiCall(
            call = { authApi.getCurrentUser("me_id") },
            map = { it.toDomain() }
        )
    }

    override suspend fun updateProfile(
        name: String,
        bio: String?,
        goal: String?,
        level: String?,
        preferredWorkouts: List<String>
    ): Resource<User> {
        return Resource.Success(User("me", name, null, goal, level, preferredWorkouts, bio = bio))
    }

    override suspend fun hasActiveSession(): Boolean {
        return !tokenStorage.getAccessToken().isNullOrBlank()
    }

    override suspend fun getPeople(): Resource<List<User>> {
        return Resource.Success(emptyList())
    }

    override suspend fun getUserProfile(userId: String): Resource<User> {
        return Resource.Success(User(userId, "Спортсмен", score = 10))
    }
}

class RetrofitGeoRepository(
    private val geoApi: GeoApi
) : GeoRepository {

    override suspend fun getLocationsNearby(lat: Double, lon: Double, radius: Int, type: String?): Resource<List<LocationSpot>> {
        return safeApiCall(
            call = { geoApi.getNearby(lat, lon, radius, type, null, null) },
            map = { list -> list.map { it.toDomain() } }
        )
    }

    override suspend fun createLocation(
        name: String,
        description: String?,
        type: String,
        latitude: Double,
        longitude: Double,
        equipmentTags: List<String>
    ): Resource<LocationSpot> {
        return safeApiCall(
            call = { geoApi.createLocation(CreateLocationRequest(name, description, type, latitude, longitude, equipmentTags)) },
            map = { it.toDomain() }
        )
    }

    override suspend fun getPlaceDetails(id: Long): Resource<LocationSpot> {
        return safeApiCall(
            call = { geoApi.getById(id) },
            map = { it.toDomain() }
        )
    }

    override suspend fun addReview(placeId: Long, rating: Int, comment: String?, tags: List<String>): Resource<Review> {
        return safeApiCall(
            call = { geoApi.addReview(placeId, CreateReviewRequest(rating, comment, tags)) },
            map = { it.toDomain() }
        )
    }

    override suspend fun getReviews(placeId: Long, page: Int, size: Int): Resource<List<Review>> {
        // Real server uses page/size inside locations reviews
        return Resource.Success(emptyList())
    }

    override suspend fun getEventsNearby(lat: Double, lon: Double, radius: Int, workoutType: String?): Resource<List<WorkoutEvent>> {
        return safeApiCall(
            call = { geoApi.getEventsNearby(lat, lon, radius, null, workoutType, null, null) },
            map = { list -> list.map { it.toDomain() } }
        )
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
        return safeApiCall(
            call = { geoApi.createEvent(CreateEventRequest(locationId, title, description, workoutType, wishes, startTime, endTime, maxParticipants)) },
            map = { it.toDomain() }
        )
    }

    override suspend fun getEventDetails(id: Long): Resource<WorkoutEvent> {
        return safeApiCall(
            call = { geoApi.getEventById(id) },
            map = { it.toDomain() }
        )
    }

    override suspend fun joinEvent(id: Long): Resource<Participant> {
        return safeApiCall(
            call = { geoApi.joinEvent(id) },
            map = { it.toDomain() }
        )
    }

    override suspend fun confirmParticipant(eventId: Long, userId: String): Resource<Participant> {
        return safeApiCall(
            call = { geoApi.confirmParticipant(eventId, userId) },
            map = { it.toDomain() }
        )
    }

    override suspend fun rejectParticipant(eventId: Long, userId: String): Resource<Participant> {
        return safeApiCall(
            call = { geoApi.rejectParticipant(eventId, userId) },
            map = { it.toDomain() }
        )
    }

    override suspend fun completeEvent(id: Long): Resource<Unit> {
        return safeApiCall(
            call = { geoApi.completeEvent(id) },
            map = { }
        )
    }

    override suspend fun getMyEvents(status: String?): Resource<List<WorkoutEvent>> {
        return safeApiCall(
            call = { geoApi.getMyEvents(status, null, null) },
            map = { page -> page.content.map { it.toDomain() } }
        )
    }

    override suspend fun geocodeAddress(address: String): Resource<GeocodeData> {
        return safeApiCall(
            call = { geoApi.geocode(address) },
            map = { GeocodeData(it.latitude, it.longitude, it.formattedAddress) }
        )
    }
}
