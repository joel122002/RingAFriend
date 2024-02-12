package com.play.ringafriend.helpers

enum class AppState(val state: Int) {
    LOGGED_IN(1),
    LOGGED_OUT(2);
    companion object {
        fun fromInt(state: Int) = AppState.values().first { it.state == state }
    }
}