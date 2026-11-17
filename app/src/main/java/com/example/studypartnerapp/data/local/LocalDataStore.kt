package com.example.studypartnerapp.data.local

import com.example.studypartnerapp.data.models.Notification
import com.example.studypartnerapp.data.models.StudyRequest
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * A Global Singleton that simulates a backend for Notifications and StudyRequests locally.
 * This allows logic to work across logout/login on the same device without Firebase nodes.
 */
object LocalDataStore {
    val studyRequests = mutableListOf<StudyRequest>()
    val notifications = mutableListOf<Notification>()

    fun addRequest(request: StudyRequest) {
        // Ensure unique ID
        val id = "local_req_${System.currentTimeMillis()}"
        studyRequests.add(request.copy(requestId = id))
    }

    fun addNotification(notification: Notification) {
        val id = "local_notif_${System.currentTimeMillis()}"
        notifications.add(notification.copy(notificationId = id))
    }

    fun updateRequestStatus(requestId: String, status: String) {
        val index = studyRequests.indexOfFirst { it.requestId == requestId }
        if (index != -1) {
            studyRequests[index] = studyRequests[index].copy(status = status)
        }
    }

    fun markNotificationRead(notificationId: String) {
        val index = notifications.indexOfFirst { it.notificationId == notificationId }
        if (index != -1) {
            notifications[index] = notifications[index].copy(read = true)
        }
    }

    fun deleteNotification(notificationId: String) {
        notifications.removeAll { it.notificationId == notificationId }
    }
}
