package com.example.studypartnerapp.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.studypartnerapp.data.models.Message
import com.example.studypartnerapp.data.models.Notification
import com.example.studypartnerapp.data.models.Student
import com.example.studypartnerapp.data.repositories.AuthRepository
import com.example.studypartnerapp.data.repositories.ChatRepository
import com.example.studypartnerapp.data.repositories.NotificationRepository
import com.example.studypartnerapp.data.repositories.StudentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ChatViewModel(
    private val authRepository: AuthRepository = AuthRepository(),
    private val chatRepository: ChatRepository = ChatRepository(),
    private val notificationRepository: NotificationRepository = NotificationRepository(),
    private val studentRepository: StudentRepository = StudentRepository()
) : ViewModel() {

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages

    private val _otherParticipant = MutableStateFlow<Student?>(null)
    val otherParticipant: StateFlow<Student?> = _otherParticipant

    private val _currentChatRoom = MutableStateFlow<com.example.studypartnerapp.data.models.ChatRoom?>(null)
    val currentChatRoom: StateFlow<com.example.studypartnerapp.data.models.ChatRoom?> = _currentChatRoom

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _activeCall = MutableStateFlow<String?>(null)
    val activeCall: StateFlow<String?> = _activeCall

    val currentUserId: String?
        get() = authRepository.currentUser.value?.uid

    fun loadMessages(chatId: String, otherUserId: String) {
        val uid = currentUserId ?: return
        viewModelScope.launch {
            _isLoading.value = true
            
            if (otherUserId != "group") {
                _otherParticipant.value = studentRepository.getStudentProfile(otherUserId)
            }
            
            _currentChatRoom.value = chatRepository.getChatRoom(chatId)

            chatRepository.markChatAsRead(chatId, uid)
            
            launch {
                chatRepository.getMessages(chatId).collectLatest {
                    _messages.value = it
                    _isLoading.value = false
                }
            }

            launch {
                chatRepository.getActiveCall(chatId).collectLatest {
                    _activeCall.value = it
                }
            }
        }
    }

    fun initiateVideoCall(chatId: String, receiverId: String) {
        val senderId = currentUserId ?: return
        viewModelScope.launch {
            chatRepository.updateCallStatus(chatId, "calling_$senderId")
            
            // Send a persistent alert to the receiver
            notificationRepository.sendNotification(
                Notification(
                    recipient = receiverId,
                    message = "Incoming Video Call...",
                    type = "video_call",
                    relatedId = chatId,
                    timestamp = System.currentTimeMillis()
                )
            )
        }
    }

    fun acceptVideoCall(chatId: String) {
        viewModelScope.launch {
            chatRepository.updateCallStatus(chatId, "ongoing")
        }
    }

    fun endOrDeclineCall(chatId: String) {
        viewModelScope.launch {
            chatRepository.updateCallStatus(chatId, null)
        }
    }

    fun sendMessage(chatId: String, receiverId: String, text: String, type: String = "text") {
        val senderId = currentUserId ?: return
        if (text.isBlank()) return
        val isGroup = chatId.startsWith("group_")

        val message = Message(
            senderId = senderId,
            receiverId = receiverId,
            text = text,
            type = type,
            timestamp = System.currentTimeMillis()
        )

        viewModelScope.launch {
            val success = chatRepository.sendMessage(chatId, message)
            if (success && type == "text") {
                if (isGroup) {
                    // Notify other group members
                    val room = _currentChatRoom.value ?: chatRepository.getChatRoom(chatId)
                    room?.participants?.keys?.filter { it != senderId }?.forEach { memberId ->
                        notificationRepository.sendNotification(
                            Notification(
                                recipient = memberId,
                                message = "You have received a message in ${room.groupName}",
                                type = "chat",
                                relatedId = chatId,
                                timestamp = System.currentTimeMillis()
                            )
                        )
                    }
                } else {
                    val senderName = studentRepository.getStudentProfile(senderId)?.name ?: "Student"
                    notificationRepository.sendNotification(
                        Notification(
                            recipient = receiverId,
                            message = "You have received a message from $senderName",
                            type = "chat",
                            relatedId = chatId,
                            timestamp = System.currentTimeMillis()
                        )
                    )
                }
            }
        }
    }

    suspend fun startChat(otherUserId: String, otherUserName: String): String {
        val currentUid = currentUserId ?: return ""
        val currentStudent = studentRepository.getStudentProfile(currentUid)
        val currentName = currentStudent?.name ?: "User"
        return chatRepository.createOrGetChatRoom(currentUid, currentName, otherUserId, otherUserName)
    }
}
