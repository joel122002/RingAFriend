package com.play.ringafriend.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.play.ringafriend.data.HomeRepository
import com.play.ringafriend.data.RegisterDevicePostModel

class HomeViewModel(application: Application): AndroidViewModel(application){

    private var homeRepository: HomeRepository?=null
    var registerDeviceLiveData:LiveData<Boolean>?=null

    init {
        homeRepository = HomeRepository(application.applicationContext)
        registerDeviceLiveData = MutableLiveData()
    }

    fun registerDevice(registerDevicePostModel: RegisterDevicePostModel){
        registerDeviceLiveData = homeRepository?.registerDevice(registerDevicePostModel)
    }
}