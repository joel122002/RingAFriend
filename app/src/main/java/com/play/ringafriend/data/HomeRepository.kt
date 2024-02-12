package com.play.ringafriend.data

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.play.ringafriend.helpers.APIUtils
import com.play.ringafriend.network.ApiClient
import com.play.ringafriend.network.ApiInterface
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HomeRepository(context: Context) {
    private var apiInterface: ApiInterface?=null

    init {
        apiInterface = ApiClient(context).getApiClient().create(ApiInterface::class.java)
    }

    fun registerDevice(registerDevicePostModel: RegisterDevicePostModel): LiveData<RegisterDevicePostModel> {
        val data = MutableLiveData<RegisterDevicePostModel>()

        apiInterface?.registerDevice(registerDevicePostModel)?.enqueue(object : Callback<RegisterDevicePostModel>{
            override fun onFailure(call: Call<RegisterDevicePostModel>, t: Throwable) {
                data.value = RegisterDevicePostModel(error = "Failed to register")
            }
            override fun onResponse(call: Call<RegisterDevicePostModel>, response: Response<RegisterDevicePostModel>) {
                if (response.code() == 204){
                    data.value = registerDevicePostModel
                }else{
                    data.value = RegisterDevicePostModel(error = APIUtils.getErrorMessage(response))
                }
            }
        })
        return data
    }

    fun login(authModel: AuthModel): LiveData<Boolean> {
        val data = MutableLiveData<Boolean>()

        apiInterface?.login(authModel)?.enqueue(object : Callback<AuthModel>{
            override fun onFailure(call: Call<AuthModel>, t: Throwable) {
                data.value = false
            }
            override fun onResponse(call: Call<AuthModel>, response: Response<AuthModel>) {
                if (response.code() == 204){
                    data.value = true
                }else{
                    data.value = false
                }
            }
        })
        return data
    }

    fun profile(): LiveData<UserModel> {
        var data = MutableLiveData<UserModel>()

        apiInterface?.profile()?.enqueue(object : Callback<UserModel>{
            override fun onFailure(call: Call<UserModel>, t: Throwable) {
                data.value = null
            }
            override fun onResponse(call: Call<UserModel>, response: Response<UserModel>) {
                val res = response.body()
                if (response.code() == 200){
                    data.value = res
                }else if  (response.code() == 401) {
                    data.value = UserModel(null, null, "Unauthorized")
                } else {
                    data.value = null
                }
            }
        })
        return data
    }
}