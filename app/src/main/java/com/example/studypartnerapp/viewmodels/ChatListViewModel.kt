package com.example.studypartnerapp.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.studypartnerapp.data.models.ChatRoom
import com.example.studypartnerapp.data.repositories.AuthRepository
import com.example.studypartnerapp.data.repositories.ChatRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class ChatListViewModel(
    private val authRepository: AuthRepository = AuthRepository(),
    private val chatRepository: ChatRepository = ChatRepository()
) : ViewModel() {

    private val _chatRooms = MutableStateFlow<List<ChatRoom>>(emptyList())
    val chatRooms: StateFlow<List<ChatRoom>> = _chatRooms

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    val currentUserId: String?
        get() = authRepository.currentUser.value?.uid

    private val _totalUnread = MutableStateFlow(0)
    val totalUnread: StateFlow<Int> = _totalUnread

    init {
        loadChatRooms()
    }

    private fun loadChatRooms() {
        val uid = currentUserId ?: return
        viewModelScope.launch {
            chatRepository.getChatRooms(uid).collectLatest {
                _chatRooms.value = it
                _isLoading.value = false
                _totalUnread.value = it.sumOf { room -> room.unreadCount[uid] ?: 0 }
            }
        }
    }

    fun deleteChat(chatId: String) {
        viewModelScope.launch {
            chatRepository.deleteChatRoom(chatId)
        }
    }
}
