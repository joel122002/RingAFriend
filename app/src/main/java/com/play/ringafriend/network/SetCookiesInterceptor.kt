package com.play.ringafriend.network

import android.content.Context
import okhttp3.Interceptor
import okhttp3.Response

class SetCookiesInterceptor(val context: Context) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val builder = chain.request().newBuilder()
        val cookieHandler = CookieHandler()
        var cookies = cookieHandler.getCookies(context)
        if (cookies.size > 0)
            builder.addHeader("Cookie", cookies[cookies.size-1].split(';').toTypedArray()[0])
        return chain.proceed(builder.build())
    }
}