package com.example.studypartnerapp.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import org.jitsi.meet.sdk.JitsiMeetActivity
import org.jitsi.meet.sdk.JitsiMeetConferenceOptions
import java.net.URL

/**
 * Independent Activity to handle Video Sessions.
 * Can be easily removed by deleting this file.
 */
class VideoCallActivity : ComponentActivity() {
    companion object {
        fun launch(context: Context, chatId: String) {
            val options = JitsiMeetConferenceOptions.Builder()
                .setServerURL(URL("https://meet.jit.si"))
                .setRoom(chatId) // Unique room for this match
                .setAudioMuted(false)
                .setVideoMuted(false)
                .setFeatureFlag("welcomepage.enabled", false)
                .setFeatureFlag("invite.enabled", false)
                .setFeatureFlag("help.enabled", false)
                .build()
            JitsiMeetActivity.launch(context, options)
        }
    }
}
