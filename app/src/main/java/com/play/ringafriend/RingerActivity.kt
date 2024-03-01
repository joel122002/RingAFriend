package com.play.ringafriend

import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import com.play.ringafriend.ui.theme.RingAFriendTheme
import kotlin.properties.Delegates

val TAG = "FIREISCOOL"

class RingerActivity : ComponentActivity() {
    var currentAudioVolume: Int by Delegates.notNull<Int>()
    var currentAccessibilityVolume: Int by Delegates.notNull<Int>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Open Activity even if screen locked
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
            val keyguardManager = getSystemService(KEYGUARD_SERVICE) as KeyguardManager
            keyguardManager.requestDismissKeyguard(this, null)
        } else {
            window.addFlags(
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                        WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            )
        }
        setContent {
            var mediaPlayer = MediaPlayer.create(applicationContext, R.raw.alarm)
            mediaPlayer.start()
            val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
            currentAudioVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
            Log.d(TAG, "Vol is " + audioManager.getStreamVolume(AudioManager.STREAM_MUSIC))
            audioManager.setStreamVolume(
                AudioManager.STREAM_MUSIC,
                audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC),
                0
            )
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                currentAccessibilityVolume =
                    audioManager.getStreamVolume(AudioManager.STREAM_ACCESSIBILITY)
                Log.d(
                    TAG,
                    "Vol is " + audioManager.getStreamVolume(AudioManager.STREAM_ACCESSIBILITY)
                )
                audioManager.setStreamVolume(
                    AudioManager.STREAM_ACCESSIBILITY,
                    audioManager.getStreamMaxVolume(AudioManager.STREAM_ACCESSIBILITY),
                    0
                )
            }

            RingAFriendTheme {
                KeepScreenOn()
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    IconButton(onClick = {
                        mediaPlayer.stop()
                        mediaPlayer.reset()
                        mediaPlayer.release()
                        audioManager.setStreamVolume(
                            AudioManager.STREAM_MUSIC,
                            currentAudioVolume,
                            0
                        )
                        finish()
                    }) {
                        Icon(Icons.Filled.Close, contentDescription = "Dismiss")
                    }
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        Log.d(TAG, currentAudioVolume.toString())
        if (currentAudioVolume >= 0) {
            audioManager.setStreamVolume(
                AudioManager.STREAM_MUSIC,
                currentAudioVolume,
                0
            )
        }
        if (currentAccessibilityVolume >= 0 && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioManager.setStreamVolume(
                AudioManager.STREAM_ACCESSIBILITY,
                currentAccessibilityVolume,
                0
            )
        }
    }

}

@Composable
fun KeepScreenOn() {
    val currentView = LocalView.current
    DisposableEffect(Unit) {
        currentView.keepScreenOn = true
        onDispose {
            currentView.keepScreenOn = false
        }
    }
}