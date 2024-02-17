package com.play.ringafriend.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.play.ringafriend.data.AuthModel
import com.play.ringafriend.data.HomeRepository
import com.play.ringafriend.data.RegisterDevicePostModel
import com.play.ringafriend.data.UserModel

class HomeViewModel(application: Application): AndroidViewModel(application){

    private var homeRepository: HomeRepository?=null
    var registerDeviceLiveData:LiveData<RegisterDevicePostModel>?=null
    var loginLiveData:LiveData<AuthModel>?=null
    var profileLiveData:LiveData<UserModel>?=null
    var signupLiveData:LiveData<AuthModel>?=null
    var getAllUsersLiveData:LiveData<List<UserModel>>?=null

    init {
        homeRepository = HomeRepository(application.applicationContext)
        registerDeviceLiveData = MutableLiveData()
    }

    fun registerDevice(registerDevicePostModel: RegisterDevicePostModel){
        registerDeviceLiveData = homeRepository?.registerDevice(registerDevicePostModel)
    }

    fun login(authModel: AuthModel){
        loginLiveData = homeRepository?.login(authModel)
    }

    fun signup(authModel: AuthModel){
        signupLiveData = homeRepository?.signup(authModel)
    }

    fun profile(){
        profileLiveData = homeRepository?.profile()
    }

    fun getAllUsers(){
        getAllUsersLiveData = homeRepository?.getAllUsers()
    }
}