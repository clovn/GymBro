package ru.itis.gymbro.feature.place

import androidx.lifecycle.ViewModel
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.syntax.simple.intent
import org.orbitmvi.orbit.syntax.simple.postSideEffect
import org.orbitmvi.orbit.syntax.simple.reduce
import org.orbitmvi.orbit.viewmodel.container
import ru.itis.gymbro.core.common.Resource
import ru.itis.gymbro.core.domain.model.LocationSpot
import ru.itis.gymbro.core.domain.model.Review
import ru.itis.gymbro.core.domain.repository.GeoRepository

data class PlaceState(
    val isLoading: Boolean = false,
    val place: LocationSpot? = null,
    val reviews: List<Review> = emptyList(),
    val errorText: String? = null
)

sealed interface PlaceSideEffect {
    data object ReviewAddedSuccessfully : PlaceSideEffect
    data class ShowMessage(val text: String) : PlaceSideEffect
}

class PlaceViewModel(
    private val geoRepository: GeoRepository
) : ViewModel(), ContainerHost<PlaceState, PlaceSideEffect> {

    override val container: Container<PlaceState, PlaceSideEffect> = container(PlaceState())

    fun loadPlaceDetails(id: Long) = intent {
        reduce { state.copy(isLoading = true) }
        
        val detailsRes = geoRepository.getPlaceDetails(id)
        val reviewsRes = geoRepository.getReviews(id, 0, 20)

        if (detailsRes is Resource.Success && reviewsRes is Resource.Success) {
            reduce {
                state.copy(
                    isLoading = false,
                    place = detailsRes.data,
                    reviews = reviewsRes.data
                )
            }
        } else if (detailsRes is Resource.Error) {
            reduce { state.copy(isLoading = false, errorText = detailsRes.error.getDisplayMessage()) }
        } else {
            reduce { state.copy(isLoading = false, errorText = "Ошибка загрузки отзывов") }
        }
    }

    fun submitReview(placeId: Long, rating: Int, comment: String?, tags: List<String>) = intent {
        reduce { state.copy(isLoading = true) }
        when (val res = geoRepository.addReview(placeId, rating, comment, tags)) {
            is Resource.Success -> {
                reduce { state.copy(isLoading = false) }
                postSideEffect(PlaceSideEffect.ReviewAddedSuccessfully)
                loadPlaceDetails(placeId) // Reload
            }
            is Resource.Error -> {
                reduce { state.copy(isLoading = false, errorText = res.error.getDisplayMessage()) }
            }
            is Resource.Loading -> { }
        }
    }
}
