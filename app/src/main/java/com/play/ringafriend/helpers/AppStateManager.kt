package com.play.ringafriend.helpers

import android.app.Activity
import android.content.Context

class AppStateManager {

    companion object{
        val PREF_NAME = "APP"
        val APP_STATE = "APP_STATE"

        fun getAppState(context: Context): AppState {
            val preferences = context.getSharedPreferences(
                PREF_NAME, Activity.MODE_PRIVATE
            )
            val appState = preferences.getInt(APP_STATE, AppState.LOGGED_OUT.state)
            return AppState.fromInt(appState)
        }

        fun setAppState(context: Context, appState: AppState) {
            val preferences = context.getSharedPreferences(
                PREF_NAME, Activity.MODE_PRIVATE
            )
            val editor = preferences.edit()
            editor.putInt(APP_STATE, appState.state)
            editor.apply()
        }
    }
}