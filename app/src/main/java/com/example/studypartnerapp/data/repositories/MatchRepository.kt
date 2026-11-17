package com.example.studypartnerapp.data.repositories

import android.util.Log
import com.example.studypartnerapp.data.models.Match
import com.example.studypartnerapp.firebase.FirebaseModule
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await

class MatchRepository(private val db: FirebaseDatabase = FirebaseModule.database) {
    private val matchesRef = db.getReference("matches")

    companion object {
        private const val TAG = "MatchRepository"
    }

    suspend fun createMatch(match: Match): Boolean {
        return try {
            val currentUid = FirebaseModule.auth.currentUser?.uid ?: return false
            if (
                match.student1.isBlank() ||
                match.student2.isBlank() ||
                match.student1 == match.student2 ||
                (match.student1 != currentUid && match.student2 != currentUid)
            ) {
                Log.w(TAG, "Invalid match details")
                return false
            }

            val key = matchesRef.push().key ?: return false
            val newMatch = match.copy(matchId = key)
            matchesRef.child(key).setValue(newMatch).await()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create match", e)
            false
        }
    }

    suspend fun getMatchesForStudent(studentId: String): List<Match> {
        return try {
            val currentUid = FirebaseModule.auth.currentUser?.uid ?: return emptyList()
            if (studentId != currentUid) {
                Log.w(TAG, "Unauthorized match retrieval attempt")
                return emptyList()
            }

            val asStudent1 = matchesRef
                .orderByChild("student1")
                .equalTo(currentUid)
                .get()
                .await()
            val asStudent2 = matchesRef
                .orderByChild("student2")
                .equalTo(currentUid)
                .get()
                .await()

            (asStudent1.children + asStudent2.children)
                .mapNotNull { child ->
                    child.getValue(Match::class.java)?.let { match ->
                        if (match.matchId.isBlank()) {
                            match.copy(matchId = child.key.orEmpty())
                        } else {
                            match
                        }
                    }
                }
                .distinctBy { it.matchId }
                .sortedByDescending { it.createdAt }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to retrieve matches", e)
            emptyList()
        }
    }

    suspend fun removeMatch(matchId: String): Boolean {
        return try {
            val currentUid = FirebaseModule.auth.currentUser?.uid ?: return false
            if (matchId.isBlank()) return false

            val snapshot = matchesRef.child(matchId).get().await()
            val match = snapshot.getValue(Match::class.java) ?: return false
            if (match.student1 != currentUid && match.student2 != currentUid) {
                Log.w(TAG, "Unauthorized match removal attempt")
                return false
            }

            matchesRef.child(matchId).removeValue().await()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to remove match", e)
            false
        }
    }
}
