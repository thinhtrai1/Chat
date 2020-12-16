package com.chat.utils

import android.content.SharedPreferences
import android.text.format.DateUtils
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.*

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
        return when {
            DateUtils.isToday(milliTime) -> {
                "Today, " + SimpleDateFormat("hh:mm aa", Locale.getDefault()).format(Date(milliTime))
            }
            DateUtils.isToday(milliTime + DateUtils.DAY_IN_MILLIS) -> {
                "Yesterday, " + SimpleDateFormat("hh:mm aa", Locale.getDefault()).format(Date(milliTime))
            }
            DateUtils.isToday(milliTime + DateUtils.WEEK_IN_MILLIS) -> {
                SimpleDateFormat("EE hh:mm aa", Locale.getDefault()).format(Date(milliTime))
            }
            else -> {
                SimpleDateFormat("dd/MM/yyyy hh:mm aa", Locale.getDefault()).format(Date(milliTime))
            }
        }
    }
}