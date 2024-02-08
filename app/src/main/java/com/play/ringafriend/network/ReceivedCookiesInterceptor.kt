package com.play.ringafriend.network

import android.content.Context
import okhttp3.Interceptor
import okhttp3.Response


class ReceivedCookiesInterceptor(val context: Context) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalResponse = chain.proceed(chain.request())
        if (!originalResponse.headers("Set-Cookie").isEmpty()) {
            val cookieHandler = CookieHandler()
            var cookies = cookieHandler.getCookies(context)
            cookies += originalResponse.headers("Set-Cookie")
            cookieHandler.setCookies(context, cookies)
        }
        return originalResponse
    }
}