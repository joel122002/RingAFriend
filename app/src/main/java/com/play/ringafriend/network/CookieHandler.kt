package com.play.ringafriend.network

import android.app.Activity
import android.content.Context
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString

class CookieHandler {
    protected val KEY_COOKIES = "KEY_COOKIES"
    protected val PREF_NAME = "COOKIES"

    protected fun putStringPreference(
        context: Context,
        value: String
    ) {
        val preferences = context.getSharedPreferences(PREF_NAME, Activity.MODE_PRIVATE)
        val editor = preferences.edit()
        editor.putString(KEY_COOKIES, value)
        editor.commit()
    }

    protected fun getStringPreference(
        context: Context,
    ): String {
        val preferences = context.getSharedPreferences(
            PREF_NAME, Activity.MODE_PRIVATE
        )
        val cookieString = preferences.getString(KEY_COOKIES, "")
        return if(cookieString == null){
            ""
        }  else {
            cookieString // kotlin casts this to non null automatically
        }
    }
    fun getCookies(context: Context): List<String> {
        val cookies = getStringPreference(context)
        return if (cookies != "")
            Json.decodeFromString(cookies)
        else
            listOf<String>()
    }

    fun setCookies(context: Context, cookies: List<String>) {
        putStringPreference(context, Json.encodeToString(cookies))
    }
}