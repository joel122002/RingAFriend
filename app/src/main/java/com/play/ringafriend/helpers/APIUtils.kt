package com.play.ringafriend.helpers

import org.json.JSONObject
import retrofit2.Response


class APIUtils {
    companion object {
        fun <T> getErrorMessage(response: Response<T>): String? {
            if (response.errorBody() != null) {
                val jObjError = JSONObject (response.errorBody()!!.string())
                return jObjError.get("error").toString()
            }
            return null
        }
    }
}