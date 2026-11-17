package com.example.studypartnerapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.studypartnerapp.navigation.AppNavigation
import com.example.studypartnerapp.ui.theme.StudyPartnerAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            StudyPartnerAppTheme {
                AppNavigation()
            }
        }
    }
}
