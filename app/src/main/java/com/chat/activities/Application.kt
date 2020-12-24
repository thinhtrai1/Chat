package com.chat.activities

import android.app.Application
import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import com.chat.utils.Constants
import com.chat.utils.Utility

class Application : Application() {
    override fun onCreate() {
        super.onCreate()

        Utility.sharedPreferences = getSharedPreferences("CHAT_APPLICATION", Context.MODE_PRIVATE)

        AppCompatDelegate.setDefaultNightMode(Utility.sharedPreferences.getInt(Constants.PREF_DARK_MODE, AppCompatDelegate.MODE_NIGHT_YES))
    }
}