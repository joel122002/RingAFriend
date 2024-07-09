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
                data.value = RegisterDevicePostModel(error = "Request failed")
            }
            override fun onResponse(call: Call<RegisterDevicePostModel>, response: Response<RegisterDevicePostModel>) {
                if (response.code() == 204){
                    data.value = registerDevicePostModel
                } else {
                    if (response.code() == 400) {
                        data.value = RegisterDevicePostModel()
                        return
                    }
                    try {
                        data.value = RegisterDevicePostModel(error = APIUtils.getErrorMessage(response))
                    } catch (e: Exception) {
                        data.value = RegisterDevicePostModel(error = "${response.code()} ${response.message()}")
                    }
                }
            }
        })
        return data
    }

    fun login(authModel: AuthModel): LiveData<AuthModel> {
        val data = MutableLiveData<AuthModel>()

        apiInterface?.login(authModel)?.enqueue(object : Callback<AuthModel>{
            override fun onFailure(call: Call<AuthModel>, t: Throwable) {
                data.value = AuthModel(error = "Request failed")
            }
            override fun onResponse(call: Call<AuthModel>, response: Response<AuthModel>) {
                if (response.code() == 204){
                    data.value = authModel
                }else{
                    try {
                        data.value = AuthModel(error = APIUtils.getErrorMessage(response))
                    } catch (e: Exception) {
                        data.value = AuthModel(error = "${response.code()} ${response.message()}")
                    }
                }
            }
        })
        return data
    }

    fun signup(authModel: AuthModel): LiveData<AuthModel> {
        val data = MutableLiveData<AuthModel>()

        apiInterface?.signup(authModel)?.enqueue(object : Callback<AuthModel>{
            override fun onFailure(call: Call<AuthModel>, t: Throwable) {
                data.value = AuthModel(error = "Request failed")
            }
            override fun onResponse(call: Call<AuthModel>, response: Response<AuthModel>) {
                if (response.code() == 204){
                    data.value = authModel
                } else if (response.code() == 401) {
                  data.value = AuthModel(error = "Unauthorized")
                } else{
                    try {
                        data.value = AuthModel(error = APIUtils.getErrorMessage(response))
                    } catch (e: Exception) {
                        data.value = AuthModel(error = "${response.code()} ${response.message()}")
                    }
                }
            }
        })
        return data
    }

    fun profile(): LiveData<UserModel> {
        var data = MutableLiveData<UserModel>()

        apiInterface?.profile()?.enqueue(object : Callback<UserModel>{
            override fun onFailure(call: Call<UserModel>, t: Throwable) {
                data.value = UserModel(null, null, "Request Failed")
            }
            override fun onResponse(call: Call<UserModel>, response: Response<UserModel>) {
                val res = response.body()
                if (response.code() == 200){
                    data.value = res
                } else if  (response.code() == 401) {
                    data.value = UserModel(null, null, "Unauthorized")
                } else {
                    data.value = UserModel(null, null, "${response.code()} ${response.message()}")
                }
            }
        })
        return data
    }

    fun getAllUsers(): LiveData<List<UserModel>> {
        var data = MutableLiveData<List<UserModel>>()

        apiInterface?.getAllUsers()?.enqueue(object : Callback<List<UserModel>>{
            override fun onFailure(call: Call<List<UserModel>>, t: Throwable) {
                data.value = listOf(UserModel(null, null, "Request failed"))
            }
            override fun onResponse(call: Call<List<UserModel>>, response: Response<List<UserModel>>) {
                val res = response.body()
                if (response.code() == 200){
                    data.value = res
                } else if  (response.code() == 401) {
                    data.value = listOf(UserModel(null, null, "Unauthorized"))
                } else {
                    data.value = listOf(UserModel(null, null, "${response.code()} ${response.message()}"))
                }
            }
        })
        return data
    }

    fun sendToUser(username: String): LiveData<String> {
        var data = MutableLiveData<String>()

        apiInterface?.sendToUser(username)?.enqueue(object : Callback<String>{
            override fun onFailure(call: Call<String>, t: Throwable) {
                data.value = "Request failed"
            }
            override fun onResponse(call: Call<String>, response: Response<String>) {
                val res = response.body()
                if (response.code() == 204){
                    data.value = null
                } else if  (response.code() == 401) {
                    data.value = "Unauthorized"
                } else {
                    try {
                        data.value = APIUtils.getErrorMessage(response)
                    } catch (e: Exception) {
                        data.value = "${response.code()} ${response.message()}"
                    }
                }
            }
        })
        return data
    }
}