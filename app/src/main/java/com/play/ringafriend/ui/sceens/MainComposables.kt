package com.play.ringafriend.ui.sceens

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat.startActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import com.play.ringafriend.BuildConfig
import com.play.ringafriend.auth.CredentialsActivity
import com.play.ringafriend.data.RegisterDevicePostModel
import com.play.ringafriend.data.RingModel
import com.play.ringafriend.data.UserModel
import com.play.ringafriend.helpers.AppState
import com.play.ringafriend.helpers.AppStateManager
import com.play.ringafriend.helpers.SocketEvent
import com.play.ringafriend.network.SocketClient
import com.play.ringafriend.ui.theme.RingAFriendTheme
import com.play.ringafriend.viewmodel.HomeViewModel
import io.socket.client.Ack
import io.socket.client.Socket
import java.net.URLEncoder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(context: Context, application: Application, activity: MainActivity) {
    val vm = viewModel {
        HomeViewModel(application)
    }
    val socket = SocketClient.getClient(application.applicationContext)
    var displayToken by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    val users = vm.getAllUsersLiveData?.observeAsState()
    val lifecycleOwner = LocalLifecycleOwner.current
    val TAG = "FIREISCOOL"
    LaunchedEffect(Unit) {
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
            vm.registerDeviceLiveData?.observe(lifecycleOwner, Observer {
                if (!it.token.isNullOrEmpty()) {
                    Log.d(TAG, "registered")
                } else if (!it.error.isNullOrEmpty()) {
                    Toast.makeText(context, it.error!!, Toast.LENGTH_SHORT).show()
                }
            })

            Log.i(TAG, token)
        })
        vm.profile()
        vm.profileLiveData?.observe(lifecycleOwner, Observer {
            if (it != null && !it.username.isNullOrEmpty()) {
                username = it.username
            } else if (it != null && !it.error.isNullOrEmpty()) {
                if (it.error == "Unauthorized") {
                    AppStateManager.setAppState(context, AppState.LOGGED_OUT)
                    val intent = Intent(context, CredentialsActivity::class.java)
                    startActivity(context, intent, null)
                    activity.finish()
                }
                Toast.makeText(context, it.error!!, Toast.LENGTH_SHORT).show()
            }
        })
        vm.getAllUsers()
    }

    LaunchedEffect(username) {
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
                                context = context,
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

@Composable
fun MessageDialog(
    onDismissRequest: () -> Unit,
    onConfirmation: (message: String) -> Unit,
) {
    var message by remember {
        mutableStateOf("")
    }
    Dialog(onDismissRequest = { onDismissRequest() }) {
        // Draw a rectangle shape with rounded corners inside the dialog
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth().padding(16.dp, 16.dp, 16.dp, 0.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.Start,
            ) {
                Text(text = "Message", modifier = Modifier.padding(0.dp, 0.dp, 0.dp, 16.dp), style = MaterialTheme.typography.bodyLarge)
                OutlinedTextField(value = message, onValueChange = { message = it }, label = {
                    Text(text = "Message")
                })
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    TextButton(
                        onClick = { onDismissRequest() },
                        modifier = Modifier.padding(8.dp),
                    ) {
                        Text("Dismiss")
                    }
                    TextButton(
                        onClick = { onConfirmation(message) },
                        modifier = Modifier.padding(8.dp),
                    ) {
                        Text("Confirm")
                    }
                }
            }
        }
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
    var selectedUsername by remember {
        mutableStateOf<String?>(null)
    }


    Card(
        colors = CardDefaults.cardColors(
            containerColor = cardColor,
        ),
        modifier = Modifier
            .fillMaxWidth(0.5f)
            .height(80.dp),
        onClick = {
            selectedUsername = user.username
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
    if (selectedUsername != null) {
        MessageDialog(onDismissRequest = { selectedUsername = null }, onConfirmation = { message ->
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
                    socket.emit(SocketEvent.LEAVE.event, user.username, Ack {
                        socket.disconnect()
                    })
                }
            }
            vm.sendToUser(user.username!!, RingModel(message))
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
            selectedUsername = null
        })
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