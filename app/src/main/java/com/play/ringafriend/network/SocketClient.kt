package com.play.ringafriend.network

import android.app.Activity
import android.content.Context
import android.widget.Toast
import com.play.ringafriend.BuildConfig
import io.socket.client.IO
import io.socket.client.Manager
import io.socket.client.Socket
import io.socket.emitter.Emitter
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

class SocketClient(val context: Context) {
    companion object {
        lateinit var socket: Socket
        fun getClient(context: Context): Socket {
            if (this::socket.isInitialized) {
                return socket
            }
            val okHttpClient = OkHttpClient.Builder()
                .addNetworkInterceptor(SetCookiesInterceptor(context))
                .addNetworkInterceptor(ReceivedCookiesInterceptor(context))
                .addNetworkInterceptor(AppVersionInterceptor())
                .build()
            val options = IO.Options()
            options.webSocketFactory = okHttpClient
            options.callFactory = okHttpClient
            options.path = BuildConfig.BACKEND_SOCKET_PATH
            socket = IO.socket(BuildConfig.BACKEND_SOCKET_URL, options)
            return socket
        }
    }
}