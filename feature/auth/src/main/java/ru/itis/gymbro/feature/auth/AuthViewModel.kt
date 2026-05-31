package ru.itis.gymbro.feature.auth

import androidx.lifecycle.ViewModel
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.syntax.simple.intent
import org.orbitmvi.orbit.syntax.simple.postSideEffect
import org.orbitmvi.orbit.syntax.simple.reduce
import org.orbitmvi.orbit.viewmodel.container
import ru.itis.gymbro.core.common.Resource
import ru.itis.gymbro.core.datastore.GymBroDataStore
import ru.itis.gymbro.core.domain.repository.AuthRepository
import kotlinx.coroutines.flow.first

data class AuthState(
    val emailValue: String = "",
    val passwordValue: String = "",
    val nameValue: String = "",
    val usernameValue: String = "",
    val isLoading: Boolean = false,
    val errorText: String? = null
)

sealed interface AuthSideEffect {
    data object NavigateToMain : AuthSideEffect
    data object NavigateToOnboarding : AuthSideEffect
    data object NavigateToSignIn : AuthSideEffect
    val message: String get() = "" // Interface helper helper
}

class AuthViewModel(
    private val authRepository: AuthRepository,
    private val dataStore: GymBroDataStore
) : ViewModel(), ContainerHost<AuthState, AuthSideEffect> {

    override val container: Container<AuthState, AuthSideEffect> = container(AuthState())

    fun checkAuthStatus() = intent {
        reduce { state.copy(isLoading = true) }
        
        // 1. Check Onboarding status
        val onboardingDone = dataStore.isOnboardingCompleted.first()
        if (!onboardingDone) {
            reduce { state.copy(isLoading = false) }
            postSideEffect(AuthSideEffect.NavigateToOnboarding)
            return@intent
        }

        // 2. Check Active Session
        val hasSession = authRepository.hasActiveSession()
        if (!hasSession) {
            reduce { state.copy(isLoading = false) }
            postSideEffect(AuthSideEffect.NavigateToSignIn)
        } else {
            // Check session
            when (val res = authRepository.getMe()) {
                is Resource.Success -> {
                    reduce { state.copy(isLoading = false) }
                    postSideEffect(AuthSideEffect.NavigateToMain)
                }
                is Resource.Error -> {
                    reduce { state.copy(isLoading = false) }
                    postSideEffect(AuthSideEffect.NavigateToMain) // Let user enter (since Demo Mode has cached profile)
                }
                is Resource.Loading -> { }
            }
        }
    }

    fun login(username: String, email: String, pass: String) = intent {
        if (username.isBlank() || pass.isBlank()) {
            reduce { state.copy(errorText = "Заполните все поля") }
            return@intent
        }
        
        reduce { state.copy(isLoading = true, errorText = null) }
        
        when (val res = authRepository.login(username, pass)) {
            is Resource.Success -> {
                // Token storage handled directly inside repository logic
                reduce { state.copy(isLoading = false) }
                postSideEffect(AuthSideEffect.NavigateToMain)
            }
            is Resource.Error -> {
                reduce { state.copy(isLoading = false, errorText = res.error.getDisplayMessage()) }
            }
            is Resource.Loading -> { }
        }
    }

    fun register(username: String, email: String, pass: String, name: String) = intent {
        if (username.isBlank() || email.isBlank() || pass.isBlank() || name.isBlank()) {
            reduce { state.copy(errorText = "Заполните все поля") }
            return@intent
        }
        
        reduce { state.copy(isLoading = true, errorText = null) }
        val names = name.split(" ")
        val first = names.getOrNull(0) ?: name
        val last = names.getOrNull(1) ?: ""

        when (val res = authRepository.register(username, email, pass, first, last)) {
            is Resource.Success -> {
                // Auto login after success registration
                login(username, email, pass)
            }
            is Resource.Error -> {
                reduce { state.copy(isLoading = false, errorText = res.error.getDisplayMessage()) }
            }
            is Resource.Loading -> { }
        }
    }
}
