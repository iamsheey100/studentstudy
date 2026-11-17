package com.example.studypartnerapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.studypartnerapp.data.models.*
import com.example.studypartnerapp.ui.components.StudentAvatar
import com.example.studypartnerapp.ui.theme.*
import com.example.studypartnerapp.viewmodels.ChatViewModel
import com.example.studypartnerapp.ui.VideoCallActivity
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    chatId: String,
    otherUserId: String,
    otherUserName: String,
    onBackClick: () -> Unit,
    onProfileClick: (String) -> Unit,
    viewModel: ChatViewModel = viewModel()
) {
    val messages by viewModel.messages.collectAsState()
    val otherParticipant by viewModel.otherParticipant.collectAsState()
    val currentChatRoom by viewModel.currentChatRoom.collectAsState()
    val activeCall by viewModel.activeCall.collectAsState()
    var messageText by remember { mutableStateOf("") }
    var showGroupMembers by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()
    val context = LocalContext.current
    val isGroup = chatId.startsWith("group_") || otherUserId == "group"

    LaunchedEffect(chatId, otherUserId) {
        viewModel.loadMessages(chatId, otherUserId)
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    LaunchedEffect(activeCall) {
        if (activeCall == "ongoing" && !isGroup) {
            VideoCallActivity.launch(context, chatId)
        }
    }

    val darkBackground = Color(0xFF0F172A)
    val chatSurface = Color(0xFF020617)

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { 
                                if (!isGroup) onProfileClick(otherUserId) 
                                else showGroupMembers = true
                            }
                        ) {
                            Surface(modifier = Modifier.size(38.dp), shape = CircleShape, color = if(isGroup) BrandGreen else BrandDeepGreen) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        if(isGroup) Icons.Default.Diversity3 else Icons.Default.Person,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(otherUserName, fontWeight = FontWeight.Bold, fontSize = 17.sp, color = Color.White)
                                Text(
                                    text = if(isGroup) "${currentChatRoom?.participants?.size ?: 0} members" else if(activeCall == "ongoing") "In Video Session" else "Online", 
                                    style = MaterialTheme.typography.bodySmall, 
                                    color = if(activeCall == "ongoing") Color.Yellow else Color(0xFF10B981)
                                )
                            }
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                        }
                    },
                    actions = {
                        if (!isGroup) {
                            IconButton(onClick = { viewModel.initiateVideoCall(chatId, otherUserId) }) {
                                Icon(Icons.Default.Videocam, contentDescription = "Video Call", tint = Color.White)
                            }
                            IconButton(onClick = {
                                val phone = otherParticipant?.phone
                                if (!phone.isNullOrBlank()) {
                                    val intent = Intent(Intent.ACTION_DIAL).apply { data = Uri.parse("tel:$phone") }
                                    context.startActivity(intent)
                                }
                            }) {
                                Icon(Icons.Default.Call, contentDescription = "Call", tint = Color.White)
                            }
                        } else {
                            IconButton(onClick = { showGroupMembers = true }) {
                                Icon(Icons.Default.Info, contentDescription = "Group Info", tint = Color.White)
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1E293B))
                )
                
                if (!isGroup) {
                    activeCall?.let { status ->
                        if (status.startsWith("calling_") && !status.endsWith(viewModel.currentUserId ?: "")) {
                            IncomingCallBanner(
                                onAccept = { viewModel.acceptVideoCall(chatId) },
                                onDecline = { viewModel.endOrDeclineCall(chatId) }
                            )
                        } else if (status.startsWith("calling_")) {
                            Box(modifier = Modifier.fillMaxWidth().background(Color(0xFF2563EB)).padding(8.dp), contentAlignment = Alignment.Center) {
                                Text("Calling study partner...", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        },
        bottomBar = {
            ChatInput(
                messageText = messageText,
                onValueChange = { messageText = it },
                onSendMessage = {
                    if (messageText.isNotBlank()) {
                        viewModel.sendMessage(chatId, otherUserId, messageText)
                        messageText = ""
                    }
                },
                backgroundColor = darkBackground
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding).background(Brush.verticalGradient(listOf(darkBackground, chatSurface)))) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(messages) { message ->
                    val senderName = currentChatRoom?.participantNames?.get(message.senderId) ?: "Student"
                    MessageBubble(
                        message = message, 
                        isCurrentUser = message.senderId == viewModel.currentUserId,
                        senderName = if (isGroup) senderName else null
                    )
                }
            }
        }
    }

    if (showGroupMembers && isGroup && currentChatRoom != null) {
        AlertDialog(
            onDismissRequest = { showGroupMembers = false },
            title = { Text(otherUserName, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Group Members:", fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(bottom = 8.dp))
                    LazyColumn(modifier = Modifier.heightIn(max = 300.dp)) {
                        items(currentChatRoom!!.participantNames.toList()) { (id, name) ->
                            Row(modifier = Modifier.padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                                StudentAvatar(name = name, size = 32.dp, fontSize = 14)
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(name, fontWeight = FontWeight.Medium)
                                if (id == viewModel.currentUserId) {
                                    Text(" (You)", color = Color.Gray, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showGroupMembers = false }) { Text("Close") }
            },
            shape = RoundedCornerShape(24.dp)
        )
    }
}

@Composable
fun IncomingCallBanner(onAccept: () -> Unit, onDecline: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color(0xFF10B981),
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Incoming Video Call...", color = Color.White, fontWeight = FontWeight.ExtraBold)
            Row {
                IconButton(onClick = onAccept) {
                    Icon(Icons.Default.Videocam, contentDescription = "Accept", tint = Color.White)
                }
                IconButton(onClick = onDecline) {
                    Icon(Icons.Default.CallEnd, contentDescription = "Decline", tint = Color.White)
                }
            }
        }
    }
}

@Composable
fun MessageBubble(message: Message, isCurrentUser: Boolean, senderName: String? = null) {
    val alignment = if (isCurrentUser) Alignment.CenterEnd else Alignment.CenterStart
    val backgroundColor = if (isCurrentUser) Color(0xFF2563EB) else Color(0xFF334155)
    
    val shape = if (isCurrentUser) RoundedCornerShape(20.dp, 20.dp, 4.dp, 20.dp)
                else RoundedCornerShape(20.dp, 20.dp, 20.dp, 4.dp)

    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = alignment) {
        Surface(color = backgroundColor, contentColor = Color.White, shape = shape, shadowElevation = 4.dp) {
            Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                if (!isCurrentUser && senderName != null) {
                    Text(
                        text = senderName,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.Yellow.copy(alpha = 0.9f),
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                }
                Text(text = message.text)
                Text(
                    text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(message.timestamp)),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatInput(messageText: String, onValueChange: (String) -> Unit, onSendMessage: () -> Unit, backgroundColor: Color) {
    Surface(color = backgroundColor, tonalElevation = 8.dp, modifier = Modifier.imePadding()) {
        Row(modifier = Modifier.padding(12.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Surface(modifier = Modifier.weight(1f).clip(RoundedCornerShape(28.dp)), color = Color(0xFF1E293B)) {
                TextField(
                    value = messageText,
                    onValueChange = onValueChange,
                    placeholder = { Text("Message...", color = Color.Gray) },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            FloatingActionButton(onClick = onSendMessage, modifier = Modifier.size(52.dp), containerColor = Color(0xFF2563EB), contentColor = Color.White, shape = CircleShape) {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send")
            }
        }
    }
}
