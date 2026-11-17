package com.example.studypartnerapp.data.repositories

import com.example.studypartnerapp.firebase.FirebaseModule
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class AuthRepository(private val firebaseAuth: FirebaseAuth = FirebaseModule.auth) {
    
    private val _currentUser = MutableStateFlow(firebaseAuth.currentUser)
    val currentUser: StateFlow<FirebaseUser?> = _currentUser
    private val _authStateInitialized = MutableStateFlow(false)
    val authStateInitialized: StateFlow<Boolean> = _authStateInitialized

    init {
        firebaseAuth.addAuthStateListener {
            _currentUser.value = it.currentUser
            _authStateInitialized.value = true
        }
    }

    fun login(email: String, password: String, onResult: (FirebaseUser?, String?) -> Unit) {
        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onResult(task.result?.user, null)
                } else {
                    onResult(null, task.exception?.message)
                }
            }
    }

    fun register(email: String, password: String, onResult: (FirebaseUser?, String?) -> Unit) {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onResult(task.result?.user, null)
                } else {
                    onResult(null, task.exception?.message)
                }
            }
    }

    fun logout() {
        firebaseAuth.signOut()
    }
}
