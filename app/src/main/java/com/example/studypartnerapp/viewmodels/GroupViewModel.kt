package com.example.studypartnerapp.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.studypartnerapp.data.models.Student
import com.example.studypartnerapp.data.models.StudyGroup
import com.example.studypartnerapp.data.repositories.AuthRepository
import com.example.studypartnerapp.data.repositories.ChatRepository
import com.example.studypartnerapp.data.repositories.GroupRepository
import com.example.studypartnerapp.data.repositories.StudentRepository
import com.example.studypartnerapp.utils.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class GroupViewModel(
    private val authRepository: AuthRepository = AuthRepository(),
    private val groupRepository: GroupRepository = GroupRepository(),
    private val studentRepository: StudentRepository = StudentRepository(),
    private val chatRepository: ChatRepository = ChatRepository()
) : ViewModel() {

    private val _groups = MutableStateFlow<Resource<List<StudyGroup>>>(Resource.Loading())
    val groups: StateFlow<Resource<List<StudyGroup>>> = _groups

    private val _groupMembers = MutableStateFlow<List<Student>>(emptyList())
    val groupMembers: StateFlow<List<Student>> = _groupMembers

    private val _operationMessage = MutableStateFlow<String?>(null)
    val operationMessage: StateFlow<String?> = _operationMessage

    val currentUserId: String? get() = authRepository.currentUser.value?.uid

    init {
        loadGroups()
    }

    fun loadGroups() {
        viewModelScope.launch {
            _groups.value = Resource.Loading()
            val allGroups = groupRepository.getAllGroups()
            _groups.value = Resource.Success(allGroups)
        }
    }

    fun loadGroupMembers(memberIds: List<String>) {
        viewModelScope.launch {
            _groupMembers.value = emptyList()
            val members = memberIds.map { uid ->
                studentRepository.getStudentProfile(uid)
                    ?: Student(uid = uid, name = "Student")
            }
            _groupMembers.value = members
        }
    }

    suspend fun initGroupChat(group: StudyGroup): String {
        val members = group.members.mapNotNull { studentRepository.getStudentProfile(it) }
        val memberNames = members.associate { it.uid to it.name }
        return chatRepository.createOrGetGroupChatRoom(group, memberNames)
    }

    fun createGroup(name: String, unit: String, meetingTime: String) {
        val currentUid = authRepository.currentUser.value?.uid
        if (currentUid == null) {
            _operationMessage.value = "Sign in before creating a study group."
            return
        }
        if (name.isBlank() || unit.isBlank() || meetingTime.isBlank()) {
            _operationMessage.value = "Enter a group name, unit, and meeting time."
            return
        }

        viewModelScope.launch {
            val group = StudyGroup(
                groupName = name,
                unit = unit,
                meetingTime = meetingTime,
                createdBy = currentUid,
                members = listOf(currentUid)
            )
            val success = groupRepository.createGroup(group)
            if (success) {
                _operationMessage.value = "Study group created."
                loadGroups()
            } else {
                _operationMessage.value = "Could not create the study group. Check your connection and Firebase permissions."
            }
        }
    }

    fun joinGroup(groupId: String) {
        val currentUid = authRepository.currentUser.value?.uid
        if (currentUid == null) {
            _operationMessage.value = "Sign in before joining a study group."
            return
        }

        viewModelScope.launch {
            val success = groupRepository.joinGroup(groupId, currentUid)
            if (success) {
                _operationMessage.value = "You joined the study group."
                loadGroups()
            } else {
                _operationMessage.value = "Could not join the study group. Check your connection and Firebase permissions."
            }
        }
    }

    fun clearOperationMessage() {
        _operationMessage.value = null
    }
}
