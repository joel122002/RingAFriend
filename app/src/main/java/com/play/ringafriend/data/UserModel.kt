package com.play.ringafriend.data

import java.util.Date

data class UserModel (
    val username: String?=null,
//    val full_name: String,
    val join_date: Date?=null,
    var error: String?=null
)