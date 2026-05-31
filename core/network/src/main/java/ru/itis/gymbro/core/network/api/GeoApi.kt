package ru.itis.gymbro.core.network.api

import kotlinx.serialization.Serializable
import retrofit2.Response
import retrofit2.http.*

interface GeoApi {

    @GET("api/v1/geo/locations/{id}")
    suspend fun getById(
        @Path("id") id: Long
    ): Response<LocationDetailResponse>

    @PUT("api/v1/geo/locations/{id}")
    suspend fun updateLocation(
        @Path("id") id: Long,
        @Body request: UpdateLocationRequest
    ): Response<LocationDetailResponse>

    @DELETE("api/v1/geo/locations/{id}")
    suspend fun deleteLocation(
        @Path("id") id: Long
    ): Response<Unit>

    @POST("api/v1/geo/locations")
    suspend fun createLocation(
        @Body request: CreateLocationRequest
    ): Response<LocationDetailResponse>

    @GET("api/v1/geo/locations/nearby")
    suspend fun getNearby(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("radius") radius: Int,
        @Query("type") type: String?,
        @Query("page") page: Int?,
        @Query("size") size: Int?
    ): Response<List<LocationSummaryResponse>>

    @POST("api/v1/geo/locations/{id}/review")
    suspend fun addReview(
        @Path("id") id: Long,
        @Body request: CreateReviewRequest
    ): Response<ReviewResponse>

    @DELETE("api/v1/geo/locations/{id}/review")
    suspend fun deleteReview(
        @Path("id") id: Long
    ): Response<ReviewResponse>

    @GET("api/v1/geo/geocode")
    suspend fun geocode(
        @Query("address") address: String
    ): Response<GeocodeResponse>

    @GET("api/v1/geo/events/{id}")
    suspend fun getEventById(
        @Path("id") id: Long
    ): Response<EventDetailResponse>

    @PUT("api/v1/geo/events/{id}")
    suspend fun updateEvent(
        @Path("id") id: Long,
        @Body request: UpdateEventRequest
    ): Response<EventDetailResponse>

    @DELETE("api/v1/geo/events/{id}")
    suspend fun cancelEvent(
        @Path("id") id: Long
    ): Response<Unit>

    @POST("api/v1/geo/events")
    suspend fun createEvent(
        @Body request: CreateEventRequest
    ): Response<EventDetailResponse>

    @GET("api/v1/geo/events/nearby")
    suspend fun getEventsNearby(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("radius") radius: Int,
        @Query("date") date: String?,
        @Query("workoutType") workoutType: String?,
        @Query("page") page: Int?,
        @Query("size") size: Int?
    ): Response<List<EventSummaryResponse>>

    @GET("api/v1/geo/events/my")
    suspend fun getMyEvents(
        @Query("status") status: String?,
        @Query("page") page: Int?,
        @Query("size") size: Int?
    ): Response<PageEventSummaryResponse>

    @POST("api/v1/geo/events/{id}/join")
    suspend fun joinEvent(
        @Path("id") id: Long
    ): Response<ParticipantResponse>

    @POST("api/v1/geo/events/{id}/confirm/{userId}")
    suspend fun confirmParticipant(
        @Path("id") id: Long,
        @Path("userId") userId: String
    ): Response<ParticipantResponse>

    @POST("api/v1/geo/events/{id}/reject/{userId}")
    suspend fun rejectParticipant(
        @Path("id") id: Long,
        @Path("userId") userId: String
    ): Response<ParticipantResponse>

    @POST("api/v1/geo/events/{id}/complete")
    suspend fun completeEvent(
        @Path("id") id: Long
    ): Response<Unit>
}

@Serializable
data class CreateLocationRequest(
    val name: String,
    val description: String?,
    val type: String,
    val latitude: Double,
    val longitude: Double,
    val equipmentTags: List<String> = emptyList(),
    val photoUrls: List<String> = emptyList()
)

@Serializable
data class UpdateLocationRequest(
    val name: String?,
    val description: String?,
    val type: String?,
    val equipmentTags: List<String>?,
    val photoUrls: List<String>?
)

@Serializable
data class LocationDetailResponse(
    val id: Long,
    val name: String,
    val description: String? = null,
    val type: String,
    val latitude: Double,
    val longitude: Double,
    val photoUrls: List<String> = emptyList(),
    val equipmentTags: List<String> = emptyList(),
    val creator: UserShortResponse? = null,
    val isVerified: Boolean = false,
    val avgRating: Double = 0.0,
    val reviewCount: Int = 0,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

@Serializable
data class LocationSummaryResponse(
    val id: Long,
    val name: String,
    val type: String,
    val latitude: Double,
    val longitude: Double,
    val avgRating: Double = 0.0,
    val reviewCount: Int = 0,
    val equipmentTags: List<String> = emptyList(),
    val distance: Double? = null,
    val verified: Boolean = false
)

@Serializable
data class CreateReviewRequest(
    val rating: Int,
    val comment: String?,
    val tags: List<String> = emptyList()
)

@Serializable
data class ReviewResponse(
    val id: Long,
    val userId: String,
    val author: UserShortResponse,
    val rating: Int,
    val comment: String? = null,
    val tags: List<String> = emptyList(),
    val createdAt: String
)

@Serializable
data class GeocodeResponse(
    val latitude: Double,
    val longitude: Double,
    val formattedAddress: String
)

@Serializable
data class CreateEventRequest(
    val locationId: Long,
    val title: String,
    val description: String?,
    val workoutType: String,
    val wishes: String?,
    val startTime: String,
    val endTime: String,
    val maxParticipants: Int
)

@Serializable
data class UpdateEventRequest(
    val title: String?,
    val description: String?,
    val wishes: String?,
    val startTime: String?,
    val endTime: String?,
    val maxParticipants: Int?
)

@Serializable
data class EventDetailResponse(
    val id: Long,
    val location: LocationSummaryResponse,
    val host: UserShortResponse,
    val title: String,
    val description: String? = null,
    val workoutType: String,
    val wishes: String? = null,
    val startTime: String,
    val endTime: String,
    val maxParticipants: Int,
    val status: String,
    val participants: List<ParticipantResponse> = emptyList(),
    val createdAt: String? = null,
    val updatedAt: String? = null
)

@Serializable
data class EventSummaryResponse(
    val id: Long,
    val title: String,
    val workoutType: String,
    val startTime: String,
    val endTime: String,
    val maxParticipants: Int,
    val currentParticipants: Int = 0,
    val status: String,
    val location: LocationSummaryResponse,
    val host: UserShortResponse,
    val distance: Double? = null
)

@Serializable
data class ParticipantResponse(
    val userId: String,
    val userInfo: UserShortResponse,
    val status: String,
    val joinedAt: String
)

@Serializable
data class PageEventSummaryResponse(
    val content: List<EventSummaryResponse> = emptyList(),
    val totalPages: Int = 0,
    val totalElements: Long = 0L,
    val number: Int = 0,
    val size: Int = 0
)
