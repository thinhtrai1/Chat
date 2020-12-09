package com.chat.activities

import android.app.Application
import android.content.Context
import com.chat.utils.Utility

class Application : Application() {
    override fun onCreate() {
        super.onCreate()

        Utility.sharedPreferences = getSharedPreferences("BASE_APPLICATION", Context.MODE_PRIVATE)
    }
}