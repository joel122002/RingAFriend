package com.play.ringafriend.auth

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.play.ringafriend.auth.ui.theme.RingAFriendTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.play.ringafriend.ui.sceens.MainActivity
import com.play.ringafriend.data.AuthModel
import com.play.ringafriend.helpers.AppState
import com.play.ringafriend.helpers.AppStateManager
import com.play.ringafriend.viewmodel.HomeViewModel

val TAG = "LoginScreen"

class CredentialsActivity : ComponentActivity() {
    private lateinit var vm: HomeViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            vm = ViewModelProvider(this)[HomeViewModel::class.java]
            var login by remember {
                mutableStateOf(true)
            }

            RingAFriendTheme {
                if (login) {
                    Login(vm, setLogin = { login = it })
                } else {
                    SignUp(vm, setLogin = { login = it })
                }

            }
        }
    }
}

@Composable
fun Login(vm: HomeViewModel, setLogin: (Boolean) -> Unit) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current
    val activity = (LocalContext.current as? Activity)
    // A surface container using the 'background' color from the theme
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth(0.75f)
                    .weight(1f),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Username") }
                )
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                )
                TextButton(modifier = Modifier.fillMaxWidth(0.75f), onClick = {
                    val authModel = AuthModel()
                    authModel.username = username
                    authModel.password = password
                    vm.login(authModel)
                    vm.loginLiveData?.observe(lifecycleOwner, Observer {
                        if (!it.username.isNullOrEmpty()) {
                            AppStateManager.setAppState(context, AppState.LOGGED_IN)
                            val intent = Intent(context, MainActivity::class.java)
                            activity?.startActivity(intent)
                            activity?.finish()
                        } else if (it.error != null) {
                            Toast.makeText(context, it.error, Toast.LENGTH_LONG).show()
                        }
                    })
                }) {
                    Text("Login")
                }
            }
            Row(
                modifier = Modifier.padding(0.dp, 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Don't have an account? ")
                TextButton(onClick = { setLogin(false) }) {
                    Text(text = "Sign up")
                }
            }
        }

    }
}

@Composable
fun SignUp(vm: HomeViewModel, setLogin: (Boolean) -> Unit) {
    var email by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassoword by remember { mutableStateOf("") }

    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current
    val activity = (LocalContext.current as? Activity)

    // A surface container using the 'background' color from the theme
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth(0.75f)
                    .weight(1f),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") }
                )
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Username") }
                )
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                )
                OutlinedTextField(
                    value = confirmPassoword,
                    onValueChange = { confirmPassoword = it },
                    label = { Text("Confirm Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                )
                TextButton(modifier = Modifier.fillMaxWidth(0.75f), onClick = {
                    val authModel = AuthModel()
                    authModel.username = username
                    authModel.password = password
                    authModel.email = email
                    vm.signup(authModel)
                    vm.signupLiveData?.observe(lifecycleOwner, Observer {
                        if (!it?.username.isNullOrEmpty()) {
                            AppStateManager.setAppState(context, AppState.LOGGED_IN)
                            val intent = Intent(context, MainActivity::class.java)
                            activity?.startActivity(intent)
                            activity?.finish()
                        } else if (!it?.error.isNullOrEmpty()) {
                            Toast.makeText(context, it.error, Toast.LENGTH_LONG).show()
                        }
                    })
                }) {
                    Text("Sign Up")
                }
            }
            Row(
                modifier = Modifier.padding(0.dp, 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Already have an account? ")
                TextButton(onClick = { setLogin(true) }) {
                    Text(text = "Login")
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginPreview() {

}