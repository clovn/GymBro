package ru.itis.gymbro.feature.map

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.first
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.syntax.simple.intent
import org.orbitmvi.orbit.syntax.simple.reduce
import org.orbitmvi.orbit.viewmodel.container
import ru.itis.gymbro.core.common.Resource
import ru.itis.gymbro.core.domain.model.*
import ru.itis.gymbro.core.domain.repository.GeoRepository
import ru.itis.gymbro.core.domain.repository.AuthRepository
import ru.itis.gymbro.core.location.LocationTracker
import ru.itis.gymbro.core.location.UserLocation

data class MapState(
    val isLoading: Boolean = false,
    val userLocation: UserLocation = UserLocation(55.7558, 37.6173),
    val spots: List<LocationSpot> = emptyList(),
    val workouts: List<WorkoutEvent> = emptyList(),
    val people: List<User> = emptyList(),
    
    // Layers toggle
    val showSpots: Boolean = true,
    val showWorkouts: Boolean = true,
    val showPeople: Boolean = true,
    
    // UGC additions
    val isSelectingLocation: Boolean = false,
    val selectedLocationCoords: UserLocation? = null,
    val errorText: String? = null
)

sealed interface MapSideEffect {
    data class OpenPlaceDetails(val id: Long) : MapSideEffect
    data class OpenWorkoutDetails(val id: Long) : MapSideEffect
    data class OpenPeopleProfile(val id: String) : MapSideEffect
    data class ShowMessage(val text: String) : MapSideEffect
}

class MapViewModel(
    private val geoRepository: GeoRepository,
    private val authRepository: AuthRepository,
    private val locationTracker: LocationTracker
) : ViewModel(), ContainerHost<MapState, MapSideEffect> {

    override val container: Container<MapState, MapSideEffect> = container(MapState())

    init {
        loadData()
    }

    fun loadData() = intent {
        reduce { state.copy(isLoading = true) }
        
        // 1. Try to fetch location
        val loc = locationTracker.getCurrentLocation()
        if (loc != null) {
            reduce { state.copy(userLocation = UserLocation(loc.latitude, loc.longitude)) }
        }

        // 2. Fetch spots and workouts
        val currentLoc = state.userLocation
        val spotsRes = geoRepository.getLocationsNearby(currentLoc.latitude, currentLoc.longitude, 5000, null)
        val eventsRes = geoRepository.getEventsNearby(currentLoc.latitude, currentLoc.longitude, 5000, null)
        val peopleRes = authRepository.getPeople()

        val spots = if (spotsRes is Resource.Success) spotsRes.data else emptyList()
        val workouts = if (eventsRes is Resource.Success) eventsRes.data else emptyList()
        val people = if (peopleRes is Resource.Success) peopleRes.data else emptyList()

        reduce {
            state.copy(
                isLoading = false,
                spots = spots,
                workouts = workouts,
                people = people
            )
        }
    }

    fun toggleSpotsLayer() = intent {
        reduce { state.copy(showSpots = !state.showSpots) }
    }

    fun toggleWorkoutsLayer() = intent {
        reduce { state.copy(showWorkouts = !state.showWorkouts) }
    }

    fun togglePeopleLayer() = intent {
        reduce { state.copy(showPeople = !state.showPeople) }
    }

    fun startSelectingLocation() = intent {
        reduce { state.copy(isSelectingLocation = true, selectedLocationCoords = state.userLocation) }
    }

    fun updateSelectedCoords(coords: UserLocation) = intent {
        reduce { state.copy(selectedLocationCoords = coords) }
    }

    fun confirmSelectedLocation() = intent {
        reduce { state.copy(isSelectingLocation = false) }
    }

    fun cancelSelectingLocation() = intent {
        reduce { state.copy(isSelectingLocation = false, selectedLocationCoords = null) }
    }

    fun createLocation(name: String, desc: String?, type: String) = intent {
        val coords = state.selectedLocationCoords ?: state.userLocation
        if (name.isBlank()) {
            reduce { state.copy(errorText = "Название не может быть пустым") }
            return@intent
        }

        reduce { state.copy(isLoading = true, errorText = null) }
        when (val res = geoRepository.createLocation(name, desc, type, coords.latitude, coords.longitude, emptyList())) {
            is Resource.Success -> {
                reduce { state.copy(isLoading = false, selectedLocationCoords = null) }
                loadData() // Refresh
            }
            is Resource.Error -> {
                reduce { state.copy(isLoading = false, errorText = res.error.getDisplayMessage()) }
            }
            is Resource.Loading -> { }
        }
    }
}
