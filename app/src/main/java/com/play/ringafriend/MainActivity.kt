package com.play.ringafriend

import android.app.Activity
import android.content.Context
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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import com.play.ringafriend.auth.CredentialsActivity
import com.play.ringafriend.data.RegisterDevicePostModel
import com.play.ringafriend.helpers.AppState
import com.play.ringafriend.helpers.AppStateManager
import com.play.ringafriend.ui.theme.RingAFriendTheme
import com.play.ringafriend.viewmodel.HomeViewModel
import java.net.URLEncoder
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalLifecycleOwner
import com.play.ringafriend.data.UserModel
import com.play.ringafriend.helpers.SocketEvent
import com.play.ringafriend.network.SocketClient
import io.socket.client.Ack
import io.socket.client.Socket

class MainActivity : ComponentActivity() {
    private lateinit var vm: HomeViewModel
    private lateinit var socket: Socket

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        vm = ViewModelProvider(this)[HomeViewModel::class.java]
        socket = SocketClient.getClient(applicationContext)
        val appState = AppStateManager.getAppState(applicationContext)
        if (appState == AppState.LOGGED_OUT) {
            val intent = Intent(applicationContext, CredentialsActivity::class.java)
            startActivity(intent)
            finish()
            return
        }
        val applicationContext = applicationContext
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:" + packageName)
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
            var displayToken by remember { mutableStateOf("") }
            var username by remember { mutableStateOf("") }
            var users = vm.getAllUsersLiveData?.observeAsState()

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
                registerDevicePostModel.device_name = Build.MANUFACTURER + " " + Build.MODEL
                vm.registerDevice(registerDevicePostModel = registerDevicePostModel)
                vm.registerDeviceLiveData?.observe(this, Observer {
                    if (!it.token.isNullOrEmpty()) {
                        Log.d(TAG, "registered")
                    } else if (!it.error.isNullOrEmpty()) {
                        Toast.makeText(baseContext, it.error!!, Toast.LENGTH_SHORT).show()
                    }
                })

                Log.i(TAG, token)
            })
            vm.profile()
            vm.profileLiveData?.observe(this, Observer {
                if (it != null && !it.username.isNullOrEmpty()) {
                    username = it.username
                } else if (it != null && !it.error.isNullOrEmpty()) {
                    if (it.error == "Unauthorized") {
                        AppStateManager.setAppState(applicationContext, AppState.LOGGED_OUT)
                        val intent = Intent(applicationContext, CredentialsActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                    Toast.makeText(baseContext, it.error!!, Toast.LENGTH_SHORT).show()
                }
            })
            vm.getAllUsers()

            if (username.isNotEmpty()) {
                FirebaseMessaging.getInstance().subscribeToTopic(username)
                    .addOnCompleteListener { task ->
                        var msg = "Subscribed"
                        if (!task.isSuccessful) {
                            msg = "Subscribe failed"
                        }
                        Log.d(TAG, msg)
                    }
            }

            RingAFriendTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    SmallTopAppBarExample(displayToken = displayToken) { innerPadding ->
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            modifier = Modifier.padding(innerPadding),
                            contentPadding = PaddingValues(12.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            if (users?.value != null) {
                                items(users.value!!) { user ->
                                    UserCard(
                                        user = user,
                                        context = this@MainActivity,
                                        vm = vm,
                                        socket = socket
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        socket.disconnect()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserCard(user: UserModel, context: Context, vm: HomeViewModel, socket: Socket) {
    context as Activity
    val onSuccessCardColor = MaterialTheme.colorScheme.primaryContainer
    val originalColor = MaterialTheme.colorScheme.surfaceVariant
    val lifecycleOwner = rememberUpdatedState(LocalLifecycleOwner.current)
    var cardColor by remember { mutableStateOf(originalColor) }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = cardColor,
        ),
        modifier = Modifier
            .fillMaxWidth(0.5f)
            .height(80.dp),
        onClick = {
            if (!socket.connected()) {
                socket.connect()
            }
            socket.off(SocketEvent.COMPLETION.event)
            socket.off(SocketEvent.MESSAGE_TO_GROUP.event)
            socket.emit(SocketEvent.JOIN.event, user.username, Ack { args ->
                Log.i(TAG, "Successfully joined ${user.username}")
            })
            socket.on(SocketEvent.MESSAGE_TO_GROUP.event) { args ->
                val message = args[0] as String
                cardColor = onSuccessCardColor
                context.runOnUiThread(Runnable {
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                })

                socket.on(SocketEvent.COMPLETION.event) { args ->
                    val message = args[0] as String
                    cardColor = originalColor
                    socket.off(SocketEvent.COMPLETION.event)
                    socket.off(SocketEvent.MESSAGE_TO_GROUP.event)
                    context.runOnUiThread(Runnable {
                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                    })
                    socket.emit("leave", user.username, Ack {
                        socket.disconnect()
                    })
                }
            }
            vm.sendToUser(user.username!!)
            vm.sendToUserLiveData?.observe(
                lifecycleOwner.value,
                Observer {
                    if (it != null) {
                        Toast.makeText(
                            context,
                            it,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                })
        }
    ) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize(),
        ) {
            Text(text = "${user.username}")
        }

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
                val packageManager: PackageManager = context.getPackageManager()
                val i = Intent(Intent.ACTION_VIEW)
                try {
                    val url =
                        "https://api.whatsapp.com/send?phone=" + BuildConfig.PHONE_NUMBER + "&text=" + URLEncoder.encode(
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

