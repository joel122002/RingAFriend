package com.play.ringafriend.ui.sceens

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.play.ringafriend.auth.CredentialsActivity
import com.play.ringafriend.helpers.AppState
import com.play.ringafriend.helpers.AppStateManager
import com.play.ringafriend.network.SocketClient
import io.socket.client.Socket

class MainActivity : ComponentActivity() {
    private lateinit var socket: Socket

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        socket = SocketClient.getClient(applicationContext)
        val appState = AppStateManager.getAppState(applicationContext)
        if (appState == AppState.LOGGED_OUT) {
            val intent = Intent(applicationContext, CredentialsActivity::class.java)
            startActivity(intent)
            finish()
            return
        }
        // Handle permission intent
        val startForResult =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val intent = result.data
                    // Handle the Intent
                    //do stuff here
                }
            }
        // Request Draw over other apps permission
        if (!Settings.canDrawOverlays(this)) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            startForResult.launch(intent)
        }
        // Check for battery optimization and disable it
        val pm = this.getSystemService(POWER_SERVICE) as PowerManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && !pm.isIgnoringBatteryOptimizations(
                packageName
            )
        ) {
            val intent = Intent()
            val packageName = packageName
            intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
            intent.setData(Uri.parse("package:$packageName"))
            startActivity(intent)
        }
        setContent {
            MainScreen(context = this@MainActivity, application, activity = this)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        socket.disconnect()
    }
}

