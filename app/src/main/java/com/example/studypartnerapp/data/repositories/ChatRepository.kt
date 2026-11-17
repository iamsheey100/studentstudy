package com.example.studypartnerapp.data.repositories

import com.example.studypartnerapp.data.models.ChatRoom
import com.example.studypartnerapp.data.models.Message
import com.example.studypartnerapp.firebase.FirebaseModule
import com.google.firebase.database.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class ChatRepository(private val db: FirebaseDatabase = FirebaseModule.database) {
    private val chatsRef = db.getReference("chats")

    fun getChatRooms(currentUserId: String): Flow<List<ChatRoom>> = callbackFlow {
        val query = chatsRef.orderByChild("participants/$currentUserId").equalTo(true)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val rooms = snapshot.children.mapNotNull { it.getValue(ChatRoom::class.java) }
                trySend(rooms.sortedByDescending { it.lastTimestamp })
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        query.addValueEventListener(listener)
        awaitClose { query.removeEventListener(listener) }
    }

    fun getMessages(chatId: String): Flow<List<Message>> = callbackFlow {
        val chatMessagesRef = chatsRef.child(chatId).child("messages")
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val messages = snapshot.children.mapNotNull { it.getValue(Message::class.java) }
                trySend(messages.sortedBy { it.timestamp })
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        chatMessagesRef.addValueEventListener(listener)
        awaitClose { chatMessagesRef.removeEventListener(listener) }
    }

    suspend fun sendMessage(chatId: String, message: Message): Boolean {
        return try {
            val newMessageRef = chatsRef.child(chatId).child("messages").push()
            val messageWithId = message.copy(messageId = newMessageRef.key ?: "")
            newMessageRef.setValue(messageWithId).await()
            
            val snapshot = chatsRef.child(chatId).get().await()
            val isGroup = snapshot.child("isGroup").getValue(Boolean::class.java) ?: false
            
            val updates = mutableMapOf<String, Any>(
                "lastMessage" to message.text,
                "lastTimestamp" to message.timestamp,
                "lastSenderId" to message.senderId
            )
            
            if (isGroup) {
                // For groups, increment unread for all participants except sender
                snapshot.child("participants").children.forEach { partSnap ->
                    val partId = partSnap.key
                    if (partId != null && partId != message.senderId) {
                        val currentUnread = snapshot.child("unreadCount").child(partId).getValue(Int::class.java) ?: 0
                        updates["unreadCount/$partId"] = currentUnread + 1
                    }
                }
            } else {
                // For 1-on-1, increment unread for receiver
                val currentUnread = snapshot.child("unreadCount").child(message.receiverId).getValue(Int::class.java) ?: 0
                updates["unreadCount/${message.receiverId}"] = currentUnread + 1
            }
            
            chatsRef.child(chatId).updateChildren(updates).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun markChatAsRead(chatId: String, userId: String): Boolean {
        return try {
            chatsRef.child(chatId).child("unreadCount").child(userId).setValue(0).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun deleteChatRoom(chatId: String): Boolean {
        return try {
            chatsRef.child(chatId).removeValue().await()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun getChatRoom(chatId: String): ChatRoom? {
        return try {
            chatsRef.child(chatId).get().await().getValue(ChatRoom::class.java)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun createOrGetChatRoom(currentUserId: String, currentUserName: String, otherUserId: String, otherUserName: String): String {
        val chatId = if (currentUserId < otherUserId) "${currentUserId}_${otherUserId}" else "${otherUserId}_${currentUserId}"
        
        val snapshot = chatsRef.child(chatId).get().await()
        if (!snapshot.exists()) {
            val chatRoom = ChatRoom(
                chatId = chatId,
                participants = mapOf(currentUserId to true, otherUserId to true),
                participantNames = mapOf(currentUserId to currentUserName, otherUserId to otherUserName),
                lastMessage = "Started a conversation",
                lastTimestamp = System.currentTimeMillis()
            )
            chatsRef.child(chatId).setValue(chatRoom).await()
        }
        return chatId
    }

    suspend fun createOrGetGroupChatRoom(group: com.example.studypartnerapp.data.models.StudyGroup, memberNames: Map<String, String>): String {
        val chatId = "group_${group.groupId}"
        val snapshot = chatsRef.child(chatId).get().await()
        
        if (!snapshot.exists()) {
            val chatRoom = ChatRoom(
                chatId = chatId,
                participants = group.members.associateWith { true },
                participantNames = memberNames,
                lastMessage = "Group created for ${group.groupName}",
                lastTimestamp = System.currentTimeMillis(),
                isGroup = true,
                groupName = group.groupName
            )
            chatsRef.child(chatId).setValue(chatRoom).await()
        } else {
            // Update members and ensure group info is present
            val updates = mapOf(
                "participants" to group.members.associateWith { true },
                "participantNames" to memberNames,
                "isGroup" to true,
                "groupName" to group.groupName
            )
            chatsRef.child(chatId).updateChildren(updates).await()
        }
        return chatId
    }

    suspend fun updateCallStatus(chatId: String, status: String?) {
        chatsRef.child(chatId).child("activeCall").setValue(status).await()
    }

    fun getActiveCall(chatId: String): Flow<String?> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                trySend(snapshot.getValue(String::class.java))
            }
            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        chatsRef.child(chatId).child("activeCall").addValueEventListener(listener)
        awaitClose { chatsRef.child(chatId).child("activeCall").removeEventListener(listener) }
    }
}
