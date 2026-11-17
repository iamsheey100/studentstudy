package com.example.studypartnerapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.studypartnerapp.data.models.ChatRoom
import com.example.studypartnerapp.ui.theme.*
import com.example.studypartnerapp.viewmodels.ChatListViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatListScreen(
    navController: NavController,
    onChatClick: (String, String, String) -> Unit,
    viewModel: ChatListViewModel = viewModel()
) {
    val chatRooms by viewModel.chatRooms.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    var chatToDelete by remember { mutableStateOf<ChatRoom?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Messages", fontWeight = FontWeight.ExtraBold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BrandDeepGreen
                )
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (chatRooms.isEmpty()) {
                EmptyChatsState()
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(chatRooms) { room ->
                        // Robust detection: ID prefix is the source of truth for group chats
                        val isRoomGroup = room.chatId.startsWith("group_") || room.isGroup
                        
                        val chatName = if (isRoomGroup) {
                            room.groupName ?: "Study Group"
                        } else {
                            val otherUserId = room.participants.keys.find { it != viewModel.currentUserId } ?: ""
                            room.participantNames[otherUserId] ?: "Student"
                        }

                        val chatTargetId = if (isRoomGroup) "group" else {
                            room.participants.keys.find { it != viewModel.currentUserId } ?: ""
                        }
                        
                        ChatRoomItem(
                            room = room, 
                            currentUserId = viewModel.currentUserId, 
                            chatName = chatName,
                            isGroupChat = isRoomGroup,
                            onClick = { onChatClick(room.chatId, chatTargetId, chatName) },
                            onLongClick = { chatToDelete = room }
                        )
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp)
                    }
                }
            }
        }
    }

    chatToDelete?.let { room ->
        AlertDialog(
            onDismissRequest = { chatToDelete = null },
            title = { Text("Delete Chat", fontWeight = FontWeight.Bold) },
            text = { Text("Do you want to delete this chat? This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteChat(room.chatId)
                        chatToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("Yes, Delete", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { chatToDelete = null }) {
                    Text("No, Cancel")
                }
            },
            shape = RoundedCornerShape(24.dp)
        )
    }
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun ChatRoomItem(
    room: ChatRoom, 
    currentUserId: String?, 
    chatName: String, 
    isGroupChat: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val unreadCount = room.unreadCount[currentUserId ?: ""] ?: 0
    val isUnread = unreadCount > 0

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar Placeholder
        Surface(
            modifier = Modifier
                .size(50.dp)
                .clip(CircleShape),
            color = if (isGroupChat) BrandGreen.copy(alpha = 0.2f) else if (isUnread) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer
        ) {
            Box(contentAlignment = Alignment.Center) {
                if (isGroupChat) {
                    Icon(Icons.Default.Diversity3, contentDescription = null, tint = BrandGreen)
                } else {
                    Text(
                        text = chatName.take(1).uppercase(),
                        style = MaterialTheme.typography.titleLarge,
                        color = if (isUnread) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = chatName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = if (isUnread) FontWeight.ExtraBold else FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = formatTimestamp(room.lastTimestamp),
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isUnread) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                )
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = room.lastMessage,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isUnread) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                if (isUnread) {
                    Box(
                        modifier = Modifier
                            .size(22.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF10B981)), // WhatsApp Green
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = unreadCount.toString(),
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyChatsState() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.Chat,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.outline
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No conversations yet",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.outline
        )
        Text(
            text = "Match with students to start chatting!",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.outline
        )
    }
}

fun formatTimestamp(timestamp: Long): String {
    val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
