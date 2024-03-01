package com.play.ringafriend.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.play.ringafriend.helpers.MyFirebaseMessagingService

class StartFirebaseMessagingService: BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent?.action)) {
            val serviceIntent = Intent(context, MyFirebaseMessagingService::class.java)
            context?.startService(serviceIntent)
            Toast.makeText(context, "Service started", Toast.LENGTH_LONG).show()
        }
    }
}