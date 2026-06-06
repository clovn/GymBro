package ru.itis.gymbro.di

import androidx.room.Room
import com.google.android.gms.location.LocationServices
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import ru.itis.gymbro.core.database.GymBroDatabase
import ru.itis.gymbro.core.datastore.GymBroDataStore
import ru.itis.gymbro.core.domain.repository.*
import ru.itis.gymbro.core.location.DefaultLocationTracker
import ru.itis.gymbro.core.location.LocationTracker
import ru.itis.gymbro.core.network.DelegatingAuthRepository
import ru.itis.gymbro.core.network.DelegatingChatRepository
import ru.itis.gymbro.core.network.DelegatingGeoRepository
import ru.itis.gymbro.core.network.DelegatingNotificationRepository
import ru.itis.gymbro.core.network.api.AuthApi
import ru.itis.gymbro.core.network.api.GeoApi
import ru.itis.gymbro.core.network.mock.MockAuthRepository
import ru.itis.gymbro.core.network.mock.MockChatRepository
import ru.itis.gymbro.core.network.mock.MockGeoRepository
import ru.itis.gymbro.core.network.mock.MockNotificationRepository
import ru.itis.gymbro.core.network.retrofit.RetrofitAuthRepository
import ru.itis.gymbro.core.network.retrofit.RetrofitGeoRepository
import ru.itis.gymbro.core.network.storage.TokenStorage
import ru.itis.gymbro.feature.auth.AuthViewModel
import ru.itis.gymbro.feature.chat.ChatViewModel
import ru.itis.gymbro.feature.map.MapViewModel
import ru.itis.gymbro.feature.place.PlaceViewModel
import ru.itis.gymbro.feature.profile.ProfileViewModel
import ru.itis.gymbro.feature.workout.WorkoutViewModel
import ru.itis.gymbro.feature.people.PeopleViewModel
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor

val appModule = module {

    // 1. Storage & Datastore
    single { TokenStorage(androidContext()) }
    single { GymBroDataStore(androidContext()) }

    // 2. Database Room
    single {
        Room.databaseBuilder(
            androidContext(),
            GymBroDatabase::class.java,
            "gymbro_database"
        ).fallbackToDestructiveMigration().build()
    }
    single { get<GymBroDatabase>().locationDao() }
    single { get<GymBroDatabase>().eventDao() }

    // 3. Geolocation client
    single { LocationServices.getFusedLocationProviderClient(androidContext()) }
    single<LocationTracker> { DefaultLocationTracker(androidContext(), get()) }

    // 4. Retrofit REST clients
    single {
        OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY })
            .build()
    }
    single {
        val contentType = "application/json".toMediaType()
        Retrofit.Builder()
            .baseUrl("https://api.gymbro.ru/api/v1/")
            .client(get())
            .addConverterFactory(Json.asConverterFactory(contentType))
            .build()
    }
    single { get<Retrofit>().create(AuthApi::class.java) }
    single { get<Retrofit>().create(GeoApi::class.java) }

    // 5. Mock Repositories
    single { MockAuthRepository() }
    single { MockGeoRepository() }
    single { MockChatRepository() }
    single { MockNotificationRepository() }

    // 6. Retrofit Repositories
    single { RetrofitAuthRepository(get(), get()) }
    single { RetrofitGeoRepository(get()) }

    // 7. Delegating (Dynamic routing based on isDemoMode flag) Repositories
    single<AuthRepository> { DelegatingAuthRepository(get<MockAuthRepository>(), get<RetrofitAuthRepository>(), get()) }
    single<GeoRepository> { DelegatingGeoRepository(get<MockGeoRepository>(), get<RetrofitGeoRepository>(), get()) }
    single<ChatRepository> { DelegatingChatRepository(get(), get()) }
    single<NotificationRepository> { DelegatingNotificationRepository(get(), get()) }

    // 8. Feature ViewModels
    viewModel { AuthViewModel(get(), get()) }
    viewModel { MapViewModel(get(), get(), get()) }
    viewModel { PlaceViewModel(get()) }
    viewModel { WorkoutViewModel(get()) }
    viewModel { PeopleViewModel(get()) }
    viewModel { ChatViewModel(get()) }
    viewModel { ProfileViewModel(get(), get(), get(), get()) }
}
