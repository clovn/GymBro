package ru.itis.gymbro.feature.people

import androidx.lifecycle.ViewModel
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.syntax.simple.intent
import org.orbitmvi.orbit.syntax.simple.postSideEffect
import org.orbitmvi.orbit.syntax.simple.reduce
import org.orbitmvi.orbit.viewmodel.container
import ru.itis.gymbro.core.common.Resource
import ru.itis.gymbro.core.domain.model.User
import ru.itis.gymbro.core.domain.repository.AuthRepository

data class PeopleState(
    val isLoading: Boolean = false,
    val people: List<User> = emptyList(),
    val activeProfile: User? = null,
    val errorText: String? = null
)

sealed interface PeopleSideEffect {
    data class OpenChat(val conversationId: String) : PeopleSideEffect
    data class ShowMessage(val text: String) : PeopleSideEffect
}

class PeopleViewModel(
    private val authRepository: AuthRepository
) : ViewModel(), ContainerHost<PeopleState, PeopleSideEffect> {

    override val container: Container<PeopleState, PeopleSideEffect> = container(PeopleState())

    init {
        loadPeople()
    }

    fun loadPeople() = intent {
        reduce { state.copy(isLoading = true) }
        when (val res = authRepository.getPeople()) {
            is Resource.Success -> {
                reduce { state.copy(isLoading = false, people = res.data) }
            }
            is Resource.Error -> {
                reduce { state.copy(isLoading = false, errorText = res.error.getDisplayMessage()) }
            }
            is Resource.Loading -> {}
        }
    }

    fun loadUserProfile(id: String) = intent {
        reduce { state.copy(isLoading = true, activeProfile = null) }
        when (val res = authRepository.getUserProfile(id)) {
            is Resource.Success -> {
                reduce { state.copy(isLoading = false, activeProfile = res.data) }
            }
            is Resource.Error -> {
                reduce { state.copy(isLoading = false, errorText = res.error.getDisplayMessage()) }
            }
            is Resource.Loading -> {}
        }
    }

    fun startChat(userId: String) = intent {
        postSideEffect(PeopleSideEffect.OpenChat(userId)) // Create conversation or open directly
    }
}
