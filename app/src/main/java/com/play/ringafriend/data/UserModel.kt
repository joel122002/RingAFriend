package com.play.ringafriend.data

import java.util.Date

data class UserModel (
    val username: String?,
//    val full_name: String,
    val join_date: Date?,
    var error: String?
)