
package com.example.studypartnerapp.firebase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

object FirebaseModule {

    val auth: FirebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }

    val database: FirebaseDatabase by lazy {
        FirebaseDatabase.getInstance(
            "https://aisha-2b01a-default-rtdb.europe-west1.firebasedatabase.app"
        )
    }
}
