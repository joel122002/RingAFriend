package com.play.ringafriend.helpers

enum class SocketEvent(val event: String) {
    COMPLETION("completion"),
    MESSAGE_TO_GROUP("messageToGroup"),
    JOIN("join"),
    LEAVE("leave");

    companion object {
        fun fromString(event: String) = SocketEvent.values().first { it.event == event }
    }
}