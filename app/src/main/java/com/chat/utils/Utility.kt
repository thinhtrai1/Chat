package com.chat.utils

import android.content.SharedPreferences
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object Utility {

    lateinit var sharedPreferences: SharedPreferences

    private var retrofit: ApiService? = null
    val apiClient: ApiService
        get() {
            if (retrofit == null) {
                retrofit = Retrofit.Builder()
                    .baseUrl(Constants.BASE_URL + "api/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                    .create(ApiService::class.java)
            }
            return retrofit!!
        }

    fun getTimeDisplayString(milliTime: Long): String {
        return milliTime.toString()
    }
}