package com.play.ringafriend.ui.sceens

import android.app.KeyguardManager
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.play.ringafriend.R
import com.play.ringafriend.helpers.AudioService
import com.play.ringafriend.helpers.SocketEvent
import com.play.ringafriend.network.SocketClient
import io.socket.client.Ack
import org.json.JSONObject

val TAG = "FIREISCOOL"

class RingerActivity : ComponentActivity() {
    private lateinit var audioService: AudioService
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
        val username = getIntent().getStringExtra("USERNAME");
        var userMessage = getIntent().getStringExtra("USER_MESSAGE");
        userMessage = userMessage ?: ""
        audioService = AudioService(this)
        audioService.setMaxVolume()
        var mediaPlayer = MediaPlayer.create(applicationContext, R.raw.alarm)
        mediaPlayer.start()
        val socket = SocketClient.getClient(applicationContext)
        fun onEnd() {
            try {
                mediaPlayer.stop()
                mediaPlayer.reset()
                mediaPlayer.release()
            } catch (e: Error) {
                e.printStackTrace()
            }
            audioService.setOriginalVolume()
            socket.emit(SocketEvent.COMPLETION.event, JSONObject().put("room", username).put("message", "${username} dismissed the ring"), Ack {
                socket.emit(SocketEvent.LEAVE.event, username, Ack {
                    socket.disconnect()
                    finish()
                })
            })
        }
        setContent {
            RingerScreen(onEnd = {
                onEnd()
            }, userMessage = userMessage)
        }
    }

    override fun onStop() {
        super.onStop()
        audioService.setOriginalVolume()
    }
}