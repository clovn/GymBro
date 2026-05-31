package ru.itis.gymbro.feature.workout

import androidx.lifecycle.ViewModel
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.syntax.simple.intent
import org.orbitmvi.orbit.syntax.simple.postSideEffect
import org.orbitmvi.orbit.syntax.simple.reduce
import org.orbitmvi.orbit.viewmodel.container
import ru.itis.gymbro.core.common.Resource
import ru.itis.gymbro.core.domain.model.WorkoutEvent
import ru.itis.gymbro.core.domain.repository.GeoRepository

data class WorkoutState(
    val isLoading: Boolean = false,
    val event: WorkoutEvent? = null,
    val isJoined: Boolean = false,
    val errorText: String? = null
)

sealed interface WorkoutSideEffect {
    data object EventCreatedSuccessfully : WorkoutSideEffect
    data class ShowMessage(val text: String) : WorkoutSideEffect
}

class WorkoutViewModel(
    private val geoRepository: GeoRepository
) : ViewModel(), ContainerHost<WorkoutState, WorkoutSideEffect> {

    override val container: Container<WorkoutState, WorkoutSideEffect> = container(WorkoutState())

    fun loadWorkoutDetails(id: Long) = intent {
        reduce { state.copy(isLoading = true) }
        
        when (val res = geoRepository.getEventDetails(id)) {
            is Resource.Success -> {
                val hasJoined = res.data.participants.any { it.userId == "me" }
                reduce {
                    state.copy(
                        isLoading = false,
                        event = res.data,
                        isJoined = hasJoined
                    )
                }
            }
            is Resource.Error -> {
                reduce { state.copy(isLoading = false, errorText = res.error.getDisplayMessage()) }
            }
            is Resource.Loading -> { }
        }
    }

    fun joinWorkout(id: Long) = intent {
        reduce { state.copy(isLoading = true) }
        when (val res = geoRepository.joinEvent(id)) {
            is Resource.Success -> {
                reduce { state.copy(isLoading = false, isJoined = true) }
                loadWorkoutDetails(id) // Refresh list
            }
            is Resource.Error -> {
                reduce { state.copy(isLoading = false, errorText = res.error.getDisplayMessage()) }
            }
            is Resource.Loading -> { }
        }
    }

    fun createWorkout(
        locationId: Long,
        title: String,
        description: String?,
        workoutType: String,
        wishes: String?,
        startTime: String,
        endTime: String,
        maxParticipants: Int
    ) = intent {
        if (title.isBlank()) {
            reduce { state.copy(errorText = "Название обязательно для заполнения") }
            return@intent
        }

        reduce { state.copy(isLoading = true, errorText = null) }
        when (val res = geoRepository.createEvent(locationId, title, description, workoutType, wishes, startTime, endTime, maxParticipants)) {
            is Resource.Success -> {
                reduce { state.copy(isLoading = false) }
                postSideEffect(WorkoutSideEffect.EventCreatedSuccessfully)
            }
            is Resource.Error -> {
                reduce { state.copy(isLoading = false, errorText = res.error.getDisplayMessage()) }
            }
            is Resource.Loading -> { }
        }
    }
}
