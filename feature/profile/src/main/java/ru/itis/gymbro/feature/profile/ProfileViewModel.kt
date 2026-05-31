package ru.itis.gymbro.feature.profile

import androidx.lifecycle.ViewModel
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.syntax.simple.intent
import org.orbitmvi.orbit.syntax.simple.postSideEffect
import org.orbitmvi.orbit.syntax.simple.reduce
import org.orbitmvi.orbit.viewmodel.container
import ru.itis.gymbro.core.common.Resource
import ru.itis.gymbro.core.datastore.GymBroDataStore
import ru.itis.gymbro.core.domain.model.GymNotification
import ru.itis.gymbro.core.domain.model.User
import ru.itis.gymbro.core.domain.repository.AuthRepository
import ru.itis.gymbro.core.domain.repository.NotificationRepository
import ru.itis.gymbro.core.network.storage.TokenStorage

data class ProfileState(
    val isLoading: Boolean = false,
    val profile: User? = null,
    val notifications: List<GymNotification> = emptyList(),
    
    // Setting selections
    val isDemoModeActive: Boolean = true,
    val errorText: String? = null
)

sealed interface ProfileSideEffect {
    data object NavigateToLogin : ProfileSideEffect
    data class ShowMessage(val text: String) : ProfileSideEffect
}

class ProfileViewModel(
    private val authRepository: AuthRepository,
    private val notificationRepository: NotificationRepository,
    private val tokenStorage: TokenStorage,
    private val dataStore: GymBroDataStore
) : ViewModel(), ContainerHost<ProfileState, ProfileSideEffect> {

    override val container: Container<ProfileState, ProfileSideEffect> = container(ProfileState())

    init {
        loadProfile()
        loadNotifications()
    }

    fun loadProfile() = intent {
        reduce { state.copy(isLoading = true) }
        when (val res = authRepository.getMe()) {
            is Resource.Success -> {
                reduce { state.copy(isLoading = false, profile = res.data) }
            }
            is Resource.Error -> {
                reduce { state.copy(isLoading = false, errorText = res.error.getDisplayMessage()) }
            }
            is Resource.Loading -> { }
        }
    }

    fun loadNotifications() = intent {
        when (val res = notificationRepository.getNotifications(0, 50)) {
            is Resource.Success -> {
                reduce { state.copy(notifications = res.data) }
            }
            is Resource.Error -> { }
            is Resource.Loading -> { }
        }
    }

    fun updateProfile(name: String, bio: String?, goal: String?, level: String?) = intent {
        reduce { state.copy(isLoading = true) }
        when (val res = authRepository.updateProfile(name, bio, goal, level, emptyList())) {
            is Resource.Success -> {
                reduce { state.copy(isLoading = false, profile = res.data) }
            }
            is Resource.Error -> {
                reduce { state.copy(isLoading = false, errorText = res.error.getDisplayMessage()) }
            }
            is Resource.Loading -> { }
        }
    }

    fun markNotificationRead(id: String) = intent {
        notificationRepository.markRead(id)
        loadNotifications() // Reload
    }

    fun toggleDemoMode(enabled: Boolean) = intent {
        dataStore.setDemoMode(enabled)
        reduce { state.copy(isDemoModeActive = enabled) }
        loadProfile() // Re-load profile to load correct data
    }

    fun logout() = intent {
        authRepository.logout()
        tokenStorage.clear()
        postSideEffect(ProfileSideEffect.NavigateToLogin)
    }
}
