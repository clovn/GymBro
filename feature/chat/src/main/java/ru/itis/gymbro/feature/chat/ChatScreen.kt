package ru.itis.gymbro.feature.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.koin.androidx.compose.koinViewModel
import org.orbitmvi.orbit.compose.collectAsState
import ru.itis.gymbro.core.designsystem.components.*
import ru.itis.gymbro.core.designsystem.theme.GymBroColors
import ru.itis.gymbro.core.designsystem.theme.GymBroTypography
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatsListScreen(
    onNavigateToChatRoom: (String) -> Unit,
    viewModel: ChatViewModel = koinViewModel()
) {
    val state by viewModel.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadConversations()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Сообщения") },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = GymBroColors.Surface)
            )
        }
    ) { padding ->
        if (state.isLoading && state.conversations.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(GymBroColors.SurfaceVariant)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                if (state.conversations.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(48.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("У вас пока нет активных диалогов.", style = GymBroTypography.bodyMedium)
                        }
                    }
                } else {
                    items(state.conversations) { conv ->
                        GymBroCard(
                            onClick = { onNavigateToChatRoom(conv.id) }
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                GymBroAvatar(name = conv.name, avatarUrl = conv.avatarUrl)
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = conv.name,
                                        style = GymBroTypography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Text(
                                        text = conv.lastMessage ?: "",
                                        style = GymBroTypography.bodyMedium,
                                        maxLines = 1,
                                        color = GymBroColors.TextSecondary
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    val timeString = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(conv.timestamp))
                                    Text(text = timeString, style = GymBroTypography.labelSmall)
                                    if (conv.unreadCount > 0) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Box(
                                            modifier = Modifier
                                                .size(20.dp)
                                                .clip(CircleShape)
                                                .background(GymBroColors.Primary),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = conv.unreadCount.toString(),
                                                color = GymBroColors.Background,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatRoomScreen(
    conversationId: String,
    onNavigateBack: () -> Unit,
    viewModel: ChatViewModel = koinViewModel()
) {
    val state by viewModel.collectAsState()
    var text by remember { mutableStateOf("") }
    val lazyListState = rememberLazyListState()

    LaunchedEffect(conversationId) {
        viewModel.loadChatRoom(conversationId)
    }

    // Scroll to bottom on new messages
    LaunchedEffect(state.activeMessages.size) {
        if (state.activeMessages.isNotEmpty()) {
            lazyListState.animateScrollToItem(state.activeMessages.size - 1)
        }
    }

    // Resolve dialogue title
    val chatTitle = state.conversations.find { it.id == conversationId }?.name ?: "Чат"

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(chatTitle) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Text("←", fontSize = 24.sp, color = GymBroColors.Primary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = GymBroColors.Surface)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(GymBroColors.SurfaceVariant)
        ) {
            // Messages Area
            LazyColumn(
                state = lazyListState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                items(state.activeMessages) { msg ->
                    val isMe = msg.isOutgoing || msg.senderId == "me"
                    
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = if (isMe) Alignment.CenterEnd else Alignment.CenterStart
                    ) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (isMe) GymBroColors.Primary else GymBroColors.Surface
                            ),
                            shape = RoundedCornerShape(
                                topStart = 16.dp,
                                topEnd = 16.dp,
                                bottomStart = if (isMe) 16.dp else 4.dp,
                                bottomEnd = if (isMe) 4.dp else 16.dp
                            ),
                            modifier = Modifier.widthIn(max = 280.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = msg.text,
                                    color = if (isMe) GymBroColors.Background else GymBroColors.TextPrimary,
                                    style = GymBroTypography.bodyLarge
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier.align(Alignment.End),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    val timeStr = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(msg.timestamp))
                                    Text(
                                        text = timeStr,
                                        color = if (isMe) GymBroColors.PrimaryLight.copy(alpha = 0.8f) else GymBroColors.TextTertiary,
                                        fontSize = 10.sp
                                    )
                                    if (isMe) {
                                        Spacer(modifier = Modifier.width(4.dp))
                                        val statusIcon = when (msg.status) {
                                            "PENDING" -> "⏳"
                                            "ERROR" -> "⚠️"
                                            else -> "✓"
                                        }
                                        Text(
                                            text = statusIcon,
                                            color = GymBroColors.Background,
                                            fontSize = 10.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Input Send Area
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(GymBroColors.Surface)
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    placeholder = { Text("Сообщение...") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GymBroColors.Primary,
                        unfocusedBorderColor = GymBroColors.Divider
                    ),
                    maxLines = 3
                )
                Spacer(modifier = Modifier.width(12.dp))
                Button(
                    onClick = {
                        viewModel.sendMessage(conversationId, text)
                        text = ""
                    },
                    modifier = Modifier.height(48.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GymBroColors.Primary)
                ) {
                    Text("Отпр.")
                }
            }
        }
    }
}
