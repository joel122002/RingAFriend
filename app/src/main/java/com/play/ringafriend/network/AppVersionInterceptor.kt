package com.play.ringafriend.network

import com.play.ringafriend.BuildConfig
import okhttp3.Interceptor
import okhttp3.Response

class AppVersionInterceptor: Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val builder = chain.request().newBuilder()
        builder.addHeader("Version", BuildConfig.VERSION_CODE.toString())
        return chain.proceed(builder.build())
    }
}