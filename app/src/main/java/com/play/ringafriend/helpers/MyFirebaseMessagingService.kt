package com.play.ringafriend.helpers

import android.content.Intent
import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.play.ringafriend.ui.sceens.RingerActivity
import com.play.ringafriend.network.SocketClient
import io.socket.client.Ack
import org.json.JSONObject

class MyFirebaseMessagingService : FirebaseMessagingService() {
    // [START on_new_token]
    /**
     * Called if the FCM registration token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is called when the
     * FCM registration token is initially generated so this is where you would retrieve the token.
     */
    val TAG = "FIREISCOOL"
    override fun onNewToken(token: String) {
        Log.d(TAG, "Refreshed token: $token")

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // FCM registration token to your app server.
        sendRegistrationToServer(token)
    }
    // [END on_new_token]

    private fun sendRegistrationToServer(token: String?) {
        // TODO: Implement this method to send token to your app server.
        Log.d(TAG, "sendRegistrationTokenToServer($token)")
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        // TODO(developer): Handle FCM messages here.
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        Log.d(TAG, "From: ${remoteMessage.from}")

        // Check if message contains a data payload.
        if (remoteMessage.data.isNotEmpty()) {
            Log.d(TAG, "Message data payload: ${remoteMessage.data}")
            val username = remoteMessage.data.get("username")
            val userMessage = remoteMessage.data.get("message")
            val socket = SocketClient.getClient(applicationContext)
            socket.connect()
            socket.emit("join", username, Ack {
                socket.emit(
                    SocketEvent.MESSAGE_TO_GROUP.event,
                    JSONObject().put("room", username)
                        .put("message", "$username's phone is now ringing"),
                    Ack {
                        Log.i(TAG, "Call received")
                        val intent = Intent(applicationContext, RingerActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        intent.putExtra("USERNAME", username);
                        intent.putExtra("USER_MESSAGE", userMessage);
                        startActivity(intent)
                    })
            })

        }

        // Check if message contains a notification payload.
        remoteMessage.notification?.let {
            Log.d(TAG, "Message Notification Body: ${it.body}")
        }

        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
    }
}