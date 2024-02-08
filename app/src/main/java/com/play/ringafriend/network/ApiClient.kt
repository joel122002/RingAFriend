package com.play.ringafriend.network

import android.content.Context
import com.play.ringafriend.BuildConfig
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

import java.util.concurrent.TimeUnit

val BASEURL = BuildConfig.BACKEND_URL
class ApiClient(val context: Context) {
    private var retrofit: Retrofit?=null
    fun getApiClient(): Retrofit {
        val gson = GsonBuilder()
            .setLenient()
            .create()
        val okHttpClient = OkHttpClient.Builder()
            .readTimeout(100, TimeUnit.SECONDS)
            .connectTimeout(100, TimeUnit.SECONDS)
            .addInterceptor(SetCookiesInterceptor(context))
            .addInterceptor(ReceivedCookiesInterceptor(context))
            .build()
        if (retrofit == null) {
            retrofit = Retrofit.Builder()
                .baseUrl(BASEURL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()
        }
        return retrofit!!
    }
}