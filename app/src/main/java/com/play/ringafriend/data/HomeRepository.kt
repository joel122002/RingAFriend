package com.play.ringafriend.data

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
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

    fun registerDevice(registerDevicePostModel: RegisterDevicePostModel): LiveData<Boolean> {
        val data = MutableLiveData<Boolean>()

        apiInterface?.registerDevice(registerDevicePostModel)?.enqueue(object : Callback<RegisterDevicePostModel>{
            override fun onFailure(call: Call<RegisterDevicePostModel>, t: Throwable) {
                data.value = false
            }
            override fun onResponse(call: Call<RegisterDevicePostModel>, response: Response<RegisterDevicePostModel>) {
                if (response.code() == 204){
                    data.value = true
                }else{
                    data.value = false
                }
            }
        })
        return data
    }
}