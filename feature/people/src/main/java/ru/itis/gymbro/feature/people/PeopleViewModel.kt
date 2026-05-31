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
        
        // Mock loading search people
        val list = listOf(
            User("c1", "Петр Петров", goal = "Нарастить мышцы", level = "Продвинутый", score = 92, preferredWorkouts = listOf("STRENGTH", "CROSSFIT"), bio = "Занимаюсь силовым троеборьем 3 года. Жму 120 кг. Ищу напарника на субботы."),
            User("c2", "Ольга Смирнова", goal = "Гибкость и рельеф", level = "Средний", score = 78, preferredWorkouts = listOf("YOGA", "STRETCHING"), bio = "Сертифицированный инструктор по хатха-йоге. Буду рада совместным практикам в парке."),
            User("u4", "Алексей Ветров", goal = "Выносливость", level = "Начальный", score = 45, preferredWorkouts = listOf("CARDIO", "OTHER"), bio = "Начал готовиться к полумарафону. Бегаю по вечерам. Присоединяйтесь!")
        )
        
        reduce { state.copy(isLoading = false, people = list) }
    }

    fun loadUserProfile(id: String) = intent {
        reduce { state.copy(isLoading = true) }
        
        // Mock get user by ID details
        val list = listOf(
            User("c1", "Петр Петров", goal = "Нарастить мышцы", level = "Продвинутый", score = 92, preferredWorkouts = listOf("STRENGTH", "CROSSFIT"), bio = "Занимаюсь силовым троеборьем 3 года. Жму 120 кг. Ищу напарника на субботы."),
            User("c2", "Ольга Смирнова", goal = "Гибкость и рельеф", level = "Средний", score = 78, preferredWorkouts = listOf("YOGA", "STRETCHING"), bio = "Сертифицированный инструктор по хатха-йоге. Буду рада совместным практикам в парке."),
            User("u4", "Алексей Ветров", goal = "Выносливость", level = "Начальный", score = 45, preferredWorkouts = listOf("CARDIO", "OTHER"), bio = "Начал готовиться к полумарафону. Бегаю по вечерам. Присоединяйтесь!")
        )

        val profile = list.find { it.id == id } ?: User(id, "Спортсмен", score = 10)
        reduce { state.copy(isLoading = false, activeProfile = profile) }
    }

    fun startChat(userId: String) = intent {
        postSideEffect(PeopleSideEffect.OpenChat(userId)) // Create conversation or open directly
    }
}
