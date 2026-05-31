package ru.itis.gymbro.feature.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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

    fun getRelativeTime(timestamp: Long): String {
        val diff = System.currentTimeMillis() - timestamp
        return when {
            diff < 5 * 60 * 1000 -> "2 min ago"
            diff < 20 * 60 * 1000 -> "15 min ago"
            diff < 90 * 60 * 1000 -> "1 hr ago"
            else -> "3 hrs ago"
        }
    }

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(GymBroColors.Background)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Messages",
                    style = GymBroTypography.displaySmall.copy(fontWeight = FontWeight.Bold, fontSize = 24.sp),
                    modifier = Modifier.weight(1f)
                )
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(GymBroColors.SurfaceVariant)
                        .clickable { },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "New Message",
                        tint = GymBroColors.TextPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
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
                    .background(GymBroColors.Background)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.Top,
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                if (state.conversations.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(48.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No active conversations.", style = GymBroTypography.bodyMedium)
                        }
                    }
                } else {
                    items(state.conversations) { conv ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onNavigateToChatRoom(conv.id) }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(modifier = Modifier.size(56.dp)) {
                                    GymBroAvatar(name = conv.name, avatarUrl = conv.avatarUrl, size = 56.dp)
                                    // Green presence dot
                                    Box(
                                        modifier = Modifier
                                            .size(14.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF22C55E))
                                            .border(2.dp, Color.White, CircleShape)
                                            .align(Alignment.BottomEnd)
                                    )
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = conv.name,
                                        style = GymBroTypography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = GymBroColors.TextPrimary
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = conv.lastMessage ?: "",
                                        style = GymBroTypography.bodyMedium,
                                        maxLines = 1,
                                        color = GymBroColors.TextSecondary
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = getRelativeTime(conv.timestamp),
                                        style = GymBroTypography.labelSmall,
                                        color = GymBroColors.TextSecondary
                                    )
                                    if (conv.unreadCount > 0) {
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Box(
                                            modifier = Modifier
                                                .size(20.dp)
                                                .clip(CircleShape)
                                                .background(GymBroColors.Primary),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = conv.unreadCount.toString(),
                                                color = Color.White,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                            Divider(color = GymBroColors.Divider.copy(alpha = 0.5f))
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

    LaunchedEffect(state.activeMessages.size) {
        if (state.activeMessages.isNotEmpty()) {
            lazyListState.animateScrollToItem(state.activeMessages.size - 1)
        }
    }

    val chatTitle = state.conversations.find { it.id == conversationId }?.name ?: "Chat"

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(chatTitle, style = GymBroTypography.titleLarge.copy(fontWeight = FontWeight.Bold)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = GymBroColors.TextPrimary)
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
                TextField(
                    value = text,
                    onValueChange = { text = it },
                    placeholder = { Text("Message...") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(24.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = GymBroColors.SurfaceVariant,
                        unfocusedContainerColor = GymBroColors.SurfaceVariant,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    maxLines = 3
                )
                Spacer(modifier = Modifier.width(12.dp))
                IconButton(
                    onClick = {
                        if (text.isNotBlank()) {
                            viewModel.sendMessage(conversationId, text)
                            text = ""
                        }
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(GymBroColors.Primary)
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Send",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
