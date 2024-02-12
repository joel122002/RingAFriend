package com.play.ringafriend.data

data class RegisterDevicePostModel (
    var token:String?=null,
    var device_name:String?=null,
    var error: String?=null
)