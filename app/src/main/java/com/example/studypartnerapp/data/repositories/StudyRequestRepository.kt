
package com.example.studypartnerapp.data.repositories

import android.util.Log
import com.example.studypartnerapp.data.models.StudyRequest
import com.example.studypartnerapp.firebase.FirebaseModule
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await

class StudyRequestRepository(
    private val db: FirebaseDatabase = FirebaseModule.database
) {

    private val requestsRef = db.getReference("studyRequests")

    companion object {
        private const val TAG = "StudyRequestRepository"
        private const val STATUS_PENDING = "pending"
        private const val STATUS_ACCEPTED = "accepted"
        private const val STATUS_REJECTED = "rejected"
    }

    /**
     * Sends a study request and saves it in Firebase.
     */
    suspend fun sendStudyRequest(request: StudyRequest): Boolean {
        return try {
            val currentUid = FirebaseModule.auth.currentUser?.uid
                ?: return false

            // Validate the sender and recipient.
            if (request.from != currentUid ||
                request.to.isBlank() ||
                request.to == currentUid
            ) {
                Log.w(TAG, "Invalid study request details")
                return false
            }

            // Generate a unique Firebase request ID.
            val requestKey = requestsRef.push().key
                ?: return false

            // Prepare the request.
            val newRequest = request.copy(
                requestId = requestKey,
                status = STATUS_PENDING,
                timestamp = System.currentTimeMillis()
            )

            // Save the request in Firebase.
            requestsRef.child(requestKey)
                .setValue(newRequest)
                .await()

            Log.d(TAG, "Study request successfully saved: $requestKey")
            true

        } catch (e: Exception) {
            Log.e(TAG, "Failed to send study request", e)
            false
        }
    }

    /**
     * Retrieves pending requests addressed to the logged-in student.
     */
    suspend fun getRequestsForStudent(
        studentId: String
    ): List<StudyRequest> {
        return try {
            val currentUid = FirebaseModule.auth.currentUser?.uid
                ?: return emptyList()

            // Students can only retrieve their own incoming requests.
            if (studentId != currentUid) {
                Log.w(TAG, "Unauthorized request retrieval attempt")
                return emptyList()
            }

            val snapshot = requestsRef
                .orderByChild("to")
                .equalTo(currentUid)
                .get()
                .await()

            val requests = snapshot.children.mapNotNull { child ->
                child.getValue(StudyRequest::class.java)
            }.filter { request ->
                request.to == currentUid &&
                    request.status == STATUS_PENDING
            }.sortedByDescending { request ->
                request.timestamp
            }

            Log.d(TAG, "Retrieved ${requests.size} pending requests")
            requests

        } catch (e: Exception) {
            Log.e(TAG, "Failed to retrieve study requests", e)
            emptyList()
        }
    }

    /**
     * Accepts or rejects a request.
     * Only the intended recipient can update its status.
     */
    suspend fun updateRequestStatus(
        requestId: String,
        status: String
    ): Boolean {
        return try {
            val currentUid = FirebaseModule.auth.currentUser?.uid
                ?: return false

            if (requestId.isBlank() ||
                (status != STATUS_ACCEPTED && status != STATUS_REJECTED)
            ) {
                return false
            }

            val requestRef = requestsRef.child(requestId)

            // Retrieve the existing request.
            val snapshot = requestRef.get().await()

            if (!snapshot.exists()) {
                Log.w(TAG, "Study request does not exist")
                return false
            }

            val recipient = snapshot.child("to")
                .getValue(String::class.java)

            val currentStatus = snapshot.child("status")
                .getValue(String::class.java)

            // Verify that the logged-in user is the recipient.
            if (recipient != currentUid ||
                currentStatus != STATUS_PENDING
            ) {
                Log.w(TAG, "Request cannot be updated by this account")
                return false
            }

            // Update only the status field.
            requestRef.child("status")
                .setValue(status)
                .await()

            Log.d(TAG, "Study request updated: $requestId -> $status")
            true

        } catch (e: Exception) {
            Log.e(TAG, "Failed to update study request", e)
            false
        }
    }
}
