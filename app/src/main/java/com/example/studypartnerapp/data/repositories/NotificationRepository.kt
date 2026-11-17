package com.example.studypartnerapp.data.repositories

import android.util.Log
import com.example.studypartnerapp.data.models.Notification
import com.example.studypartnerapp.firebase.FirebaseModule
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await

class NotificationRepository(
    private val db: FirebaseDatabase = FirebaseModule.database
) {
    private val notificationsRef = db.getReference("notifications")

    companion object {
        private const val TAG = "NotificationRepository"
    }

    suspend fun sendNotification(notification: Notification): Boolean {
        return try {
            val currentUid = FirebaseModule.auth.currentUser?.uid ?: return false

            if (
                notification.recipient.isBlank() ||
                notification.recipient == currentUid
            ) {
                Log.w(TAG, "Invalid notification recipient")
                return false
            }

            val notificationKey = notificationsRef.push().key ?: return false

            val newNotification = notification.copy(
                notificationId = notificationKey,
                read = false,
                timestamp = System.currentTimeMillis()
            )

            notificationsRef.child(notificationKey)
                .setValue(newNotification)
                .await()

            Log.d(TAG, "Notification saved: $notificationKey")
            true

        } catch (e: Exception) {
            Log.e(TAG, "Failed to send notification", e)
            false
        }
    }

    suspend fun getNotificationsForStudent(studentId: String): List<Notification> {
        return try {
            val currentUid = FirebaseModule.auth.currentUser?.uid ?: return emptyList()

            if (studentId != currentUid) {
                Log.w(TAG, "Unauthorized notification retrieval attempt")
                return emptyList()
            }

            val snapshot = notificationsRef
                .orderByChild("recipient")
                .equalTo(currentUid)
                .get()
                .await()

            snapshot.children.mapNotNull { child ->
                child.getValue(Notification::class.java)
            }.sortedByDescending { it.timestamp }

        } catch (e: Exception) {
            Log.e(TAG, "Failed to retrieve notifications", e)
            emptyList()
        }
    }

    suspend fun markAsRead(notificationId: String): Boolean {
        return try {
            val currentUid = FirebaseModule.auth.currentUser?.uid ?: return false

            if (notificationId.isBlank()) return false

            val notificationRef = notificationsRef.child(notificationId)
            val snapshot = notificationRef.get().await()

            if (!snapshot.exists()) return false

            val recipient = snapshot.child("recipient")
                .getValue(String::class.java)

            if (recipient != currentUid) {
                Log.w(TAG, "Unauthorized notification update attempt")
                return false
            }

            notificationRef.child("read").setValue(true).await()

            Log.d(TAG, "Notification marked as read: $notificationId")
            true

        } catch (e: Exception) {
            Log.e(TAG, "Failed to mark notification as read", e)
            false
        }
    }

    suspend fun deleteNotification(notificationId: String): Boolean {
        return try {
            val currentUid = FirebaseModule.auth.currentUser?.uid ?: return false

            if (notificationId.isBlank()) return false

            val notificationRef = notificationsRef.child(notificationId)
            val snapshot = notificationRef.get().await()

            if (!snapshot.exists()) return false

            val recipient = snapshot.child("recipient")
                .getValue(String::class.java)

            if (recipient != currentUid) {
                Log.w(TAG, "Unauthorized notification deletion attempt")
                return false
            }

            notificationRef.removeValue().await()

            Log.d(TAG, "Notification deleted: $notificationId")
            true

        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete notification", e)
            false
        }
    }
}
