package ru.itis.gymbro.feature.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.syntax.simple.intent
import org.orbitmvi.orbit.syntax.simple.reduce
import org.orbitmvi.orbit.viewmodel.container
import ru.itis.gymbro.core.common.Resource
import ru.itis.gymbro.core.domain.model.ChatMessage
import ru.itis.gymbro.core.domain.model.Conversation
import ru.itis.gymbro.core.domain.repository.ChatRepository

data class ChatState(
    val isLoading: Boolean = false,
    val conversations: List<Conversation> = emptyList(),
    val activeMessages: List<ChatMessage> = emptyList(),
    val errorText: String? = null
)

sealed interface ChatSideEffect {
    data class ShowMessage(val text: String) : ChatSideEffect
}

class ChatViewModel(
    private val chatRepository: ChatRepository
) : ViewModel(), ContainerHost<ChatState, ChatSideEffect> {

    override val container: Container<ChatState, ChatSideEffect> = container(ChatState())

    init {
        // Automatically listen to WebSocket events for real-time update triggers
        chatRepository.observeWebSocketMessages()
            .onEach { message ->
                handleIncomingMessage(message)
            }
            .launchIn(viewModelScope)
    }

    private fun handleIncomingMessage(message: ChatMessage) = intent {
        // If this message belongs to the current open chat, append it to UI state
        val currentOpenChat = state.activeMessages.firstOrNull()?.conversationId
        if (currentOpenChat == message.conversationId) {
            val updated = state.activeMessages.toMutableList()
            if (updated.none { it.id == message.id }) {
                updated.add(message)
                reduce { state.copy(activeMessages = updated.sortedBy { it.timestamp }) }
            }
        }
        // Also refresh conversations list to update last message preview
        loadConversations()
    }

    fun loadConversations() = intent {
        reduce { state.copy(isLoading = true) }
        when (val res = chatRepository.getConversations(0, 50)) {
            is Resource.Success -> {
                reduce { state.copy(isLoading = false, conversations = res.data.sortedByDescending { it.timestamp }) }
            }
            is Resource.Error -> {
                reduce { state.copy(isLoading = false, errorText = res.error.getDisplayMessage()) }
            }
            is Resource.Loading -> { }
        }
    }

    fun loadChatRoom(conversationId: String) = intent {
        reduce { state.copy(isLoading = true, activeMessages = emptyList()) }
        
        // Ensure conversation exists/registered
        chatRepository.createConversation(conversationId)
        
        when (val res = chatRepository.getMessages(conversationId, 0, 50)) {
            is Resource.Success -> {
                reduce { state.copy(isLoading = false, activeMessages = res.data.sortedBy { it.timestamp }) }
            }
            is Resource.Error -> {
                reduce { state.copy(isLoading = false, errorText = res.error.getDisplayMessage()) }
            }
            is Resource.Loading -> { }
        }
    }

    fun sendMessage(conversationId: String, text: String) = intent {
        if (text.isBlank()) return@intent
        
        // Optimistic UI updates
        val pendingMsg = ChatMessage(
            id = "pending_${System.currentTimeMillis()}",
            conversationId = conversationId,
            senderId = "me",
            text = text,
            timestamp = System.currentTimeMillis(),
            status = "PENDING",
            isOutgoing = true
        )
        val current = state.activeMessages.toMutableList()
        current.add(pendingMsg)
        reduce { state.copy(activeMessages = current) }

        // Send via repository
        when (val res = chatRepository.sendMessage(conversationId, text)) {
            is Resource.Success -> {
                val updated = state.activeMessages.map {
                    if (it.id == pendingMsg.id) res.data else it
                }
                reduce { state.copy(activeMessages = updated) }
            }
            is Resource.Error -> {
                val updated = state.activeMessages.map {
                    if (it.id == pendingMsg.id) it.copy(status = "ERROR") else it
                }
                reduce { state.copy(activeMessages = updated, errorText = res.error.getDisplayMessage()) }
            }
            is Resource.Loading -> { }
        }
    }
}
