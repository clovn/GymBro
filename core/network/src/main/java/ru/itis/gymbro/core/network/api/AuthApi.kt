package ru.itis.gymbro.core.network.api

import kotlinx.serialization.Serializable
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

interface AuthApi {

    @POST("api/v1/auth/register")
    suspend fun register(
        @Body request: RegisterRequest
    ): Response<RegisterResponse>

    @POST("api/v1/auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<TokenResponse>

    @POST("api/v1/auth/refresh")
    suspend fun refresh(
        @Body request: RefreshTokenRequest
    ): Response<TokenResponse>

    @POST("api/v1/auth/logout")
    suspend fun logout(
        @Body request: LogoutRequest
    ): Response<Unit>

    @GET("api/v1/users/me")
    suspend fun getCurrentUser(
        @Header("X-Auth-Subject") userId: String
    ): Response<UserShortResponse>

    @Multipart
    @POST("api/v1/users/me/avatar")
    suspend fun uploadAvatar(
        @Header("X-Auth-Subject") userId: String,
        @Part file: MultipartBody.Part
    ): Response<Unit>
}

@Serializable
data class RegisterRequest(
    val username: String,
    val email: String,
    val password: String,
    val firstName: String,
    val lastName: String
)

@Serializable
data class RegisterResponse(
    val userId: String,
    val username: String,
    val email: String
)

@Serializable
data class LoginRequest(
    val username: String,
    val password: String
)

@Serializable
data class TokenResponse(
    val access_token: String,
    val expires_in: Long,
    val refresh_expires_in: Long? = null,
    val refresh_token: String,
    val token_type: String
)

@Serializable
data class RefreshTokenRequest(
    val refresh_token: String
)

@Serializable
data class LogoutRequest(
    val refresh_token: String
)

@Serializable
data class UserShortResponse(
    val id: String,
    val name: String,
    val avatarUrl: String? = null
)
