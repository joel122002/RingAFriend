package com.play.ringafriend

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Send
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import com.play.ringafriend.data.RegisterDevicePostModel
import com.play.ringafriend.ui.theme.RingAFriendTheme
import com.play.ringafriend.viewmodel.HomeViewModel
import java.net.URLEncoder


class MainActivity : ComponentActivity() {
    private lateinit var vm: HomeViewModel

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Handle permission intent
        val startForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                val intent = result.data
                // Handle the Intent
                //do stuff here
            }
        }
        // Request Draw over other apps permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:" + packageName))
            startForResult.launch(intent)
        }
        // Check for battery optimization and disable it
        val pm = this.getSystemService(POWER_SERVICE) as PowerManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && !pm.isIgnoringBatteryOptimizations(packageName)) {
            val intent = Intent()
            val packageName = packageName
            intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
            intent.setData(Uri.parse("package:$packageName"))
            startActivity(intent)
        }
        setContent {
            var displayToken by remember { mutableStateOf("") }
            vm = ViewModelProvider(this)[HomeViewModel::class.java]
            val localClipboardManager = LocalClipboardManager.current

            val TAG = "FIREISCOOL"
            FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                    return@OnCompleteListener
                }

                // Get new FCM registration token
                val token = task.result
                displayToken = token
                val registerDevicePostModel = RegisterDevicePostModel()
                registerDevicePostModel.token = displayToken
                vm.registerDevice(registerDevicePostModel = registerDevicePostModel)
                vm.registerDeviceLiveData?.observe(this, Observer {
                    if (it) {
                        Log.d(TAG, "registered")
                    } else {
                        Log.d(TAG, "Registration failed")
                    }
                })

                Log.d(TAG, token)
                Toast.makeText(baseContext, token, Toast.LENGTH_SHORT).show()
            })

            RingAFriendTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    SmallTopAppBarExample(displayToken = displayToken) { innerPadding ->
                        LazyColumn(
                            modifier = Modifier
                                .padding(innerPadding),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                        ) {
                            item {
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth(0.5f),
                                    onClick = {
                                        localClipboardManager.setText(AnnotatedString(displayToken))
                                    }
                                ) {
                                    Text(text = "Your display token is $displayToken. Click to copy")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    RingAFriendTheme {
        Greeting("Android")
    }
}

@ExperimentalMaterial3Api
@Composable
fun SmallTopAppBarExample(displayToken: String, content: @Composable() (PaddingValues) -> Unit) {
    val context = LocalContext.current
    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
                title = {
                    Text("Ring a Friend")
                },
                actions = {
                    IconButton(onClick = { /* do something */ }) {
                        Icon(
                            imageVector = Icons.Filled.Search,
                            contentDescription = "Search person"
                        )
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                Log.d(TAG, "Clicked")
                val packageManager: PackageManager = context.getPackageManager()
                val i = Intent(Intent.ACTION_VIEW)
                try {
                    val url =
                        "https://api.whatsapp.com/send?phone="+ BuildConfig.PHONE_NUMBER + "&text=" + URLEncoder.encode(
                            displayToken,
                            "UTF-8"
                        )
                    Log.d(TAG, "Parsed")
                    i.setPackage("com.whatsapp")
                    i.setData(Uri.parse(url))
                    context.startActivity(i);
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }) {
                Icon(Icons.Outlined.Send, contentDescription = "Send key")
            }
        }
    ) { innerPadding ->
        content(innerPadding)
    }
}

