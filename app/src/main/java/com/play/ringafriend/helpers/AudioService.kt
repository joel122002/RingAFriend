package com.play.ringafriend.helpers

import android.app.Activity
import android.content.Context
import android.media.AudioManager
import android.os.Build
import kotlin.properties.Delegates

class AudioService(activity: Activity) {
    private val audioManager: AudioManager
    private val musicVolume: Int
    private var accessibilityVolume: Int by Delegates.notNull()
    init {
        audioManager = activity.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        musicVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            accessibilityVolume = audioManager.getStreamVolume(AudioManager.STREAM_ACCESSIBILITY)
        }
    }

    fun setMaxVolume() {
        audioManager.setStreamVolume(
            AudioManager.STREAM_MUSIC,
            audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC),
            0
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioManager.setStreamVolume(
                AudioManager.STREAM_ACCESSIBILITY,
                audioManager.getStreamMaxVolume(AudioManager.STREAM_ACCESSIBILITY),
                0
            )
        }
    }

    fun setOriginalVolume() {
        audioManager.setStreamVolume(
            AudioManager.STREAM_MUSIC,
            musicVolume,
            0
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioManager.setStreamVolume(
                AudioManager.STREAM_ACCESSIBILITY,
                accessibilityVolume,
                0
            )
        }
    }
}