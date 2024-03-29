package com.play.ringafriend.network

import com.play.ringafriend.data.AuthModel
import com.play.ringafriend.data.RegisterDevicePostModel
import com.play.ringafriend.data.UserModel
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiInterface {
    @POST("register-device")
    fun registerDevice(@Body registerDevicePostModel: RegisterDevicePostModel): Call<RegisterDevicePostModel>

    @POST("login")
    fun login(@Body authModel: AuthModel): Call<AuthModel>

    @POST("signup")
    fun signup(@Body authModel: AuthModel): Call<AuthModel>

    @GET("profile")
    fun profile(): Call<UserModel>
}