package ru.itis.gymbro.core.common

sealed interface Resource<out T> {
    data object Loading : Resource<Nothing>
    data class Success<out T>(val data: T) : Resource<T>
    data class Error(val error: UiError) : Resource<Nothing>
}
