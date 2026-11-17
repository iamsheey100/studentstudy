package com.example.studypartnerapp.data.repositories

import android.util.Log
import com.example.studypartnerapp.data.models.StudyGroup
import com.example.studypartnerapp.firebase.FirebaseModule
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await

class GroupRepository(private val db: FirebaseDatabase = FirebaseModule.database) {
    private val groupsRef = db.getReference("groups")

    companion object {
        private const val TAG = "GroupRepository"
    }

    suspend fun createGroup(group: StudyGroup): Boolean {
        return try {
            val currentUid = FirebaseModule.auth.currentUser?.uid ?: return false
            if (
                group.createdBy != currentUid ||
                group.groupName.isBlank() ||
                group.unit.isBlank() ||
                group.meetingTime.isBlank()
            ) {
                Log.w(TAG, "Invalid study group details")
                return false
            }

            val key = groupsRef.push().key ?: return false
            val members = (group.members + currentUid).distinct()
                .associateWith { true }
            val newGroup = mapOf(
                "groupId" to key,
                "groupName" to group.groupName,
                "unit" to group.unit,
                "meetingTime" to group.meetingTime,
                "createdBy" to currentUid,
                "members" to members,
                "createdAt" to group.createdAt
            )
            groupsRef.child(key).setValue(newGroup).await()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create study group", e)
            false
        }
    }

    suspend fun getAllGroups(): List<StudyGroup> {
        return try {
            val snapshot = groupsRef.get().await()
            snapshot.children.mapNotNull(::parseGroup)
                .sortedByDescending { it.createdAt }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to retrieve study groups", e)
            emptyList()
        }
    }

    suspend fun joinGroup(groupId: String, studentId: String): Boolean {
        return try {
            val currentUid = FirebaseModule.auth.currentUser?.uid ?: return false
            if (groupId.isBlank() || studentId != currentUid) {
                Log.w(TAG, "Invalid study group join request")
                return false
            }

            val groupSnapshot = groupsRef.child(groupId).get().await()
            val group = parseGroup(groupSnapshot) ?: return false

            if (!group.members.contains(currentUid)) {
                groupsRef.child(groupId).child("members").child(currentUid)
                    .setValue(true)
                    .await()
            }
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to join study group", e)
            false
        }
    }

    private fun parseGroup(snapshot: com.google.firebase.database.DataSnapshot): StudyGroup? {
        if (!snapshot.exists()) return null

        val members = when (val value = snapshot.child("members").value) {
            is List<*> -> value.filterIsInstance<String>()
            is Map<*, *> -> {
                if (value.values.all { it is Boolean }) {
                    value.filterValues { it == true }.keys.filterIsInstance<String>()
                } else {
                    value.values.filterIsInstance<String>()
                }
            }
            else -> emptyList()
        }

        return StudyGroup(
            groupId = snapshot.child("groupId").getValue(String::class.java)
                ?: snapshot.key.orEmpty(),
            groupName = snapshot.child("groupName").getValue(String::class.java).orEmpty(),
            unit = snapshot.child("unit").getValue(String::class.java).orEmpty(),
            meetingTime = snapshot.child("meetingTime").getValue(String::class.java).orEmpty(),
            createdBy = snapshot.child("createdBy").getValue(String::class.java).orEmpty(),
            members = members,
            createdAt = snapshot.child("createdAt").getValue(Long::class.java)
                ?: System.currentTimeMillis()
        )
    }
}
