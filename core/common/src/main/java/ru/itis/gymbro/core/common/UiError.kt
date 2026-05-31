package ru.itis.gymbro.core.common

sealed interface UiError {
    data class Network(val message: String? = null) : UiError
    data class Server(val code: Int, val message: String? = null) : UiError
    data object Unauthorized : UiError
    data class Validation(val errors: Map<String, String>) : UiError
    data class Unknown(val throwable: Throwable) : UiError

    fun getDisplayMessage(): String {
        return when (this) {
            is Network -> message ?: "Ошибка сети. Проверьте подключение к интернету."
            is Server -> message ?: "Ошибка сервера ($code). Пожалуйста, попробуйте позже."
            is Unauthorized -> "Сессия истекла. Пожалуйста, войдите снова."
            is Validation -> errors.values.firstOrNull() ?: "Ошибка заполнения формы."
            is Unknown -> "Произошла неизвестная ошибка: ${throwable.localizedMessage ?: "попробуйте позже"}"
        }
    }
}
