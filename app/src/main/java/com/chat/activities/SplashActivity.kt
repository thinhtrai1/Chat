package com.chat.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.chat.models.User
import com.chat.utils.Constants
import com.chat.utils.Utility
import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import kotlin.concurrent.schedule

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Utility.sharedPreferences.getString(Constants.PREF_USER_NAME, null) != null
            && Utility.sharedPreferences.getString(Constants.PREF_PASSWORD, null) != null) {
            Utility.apiClient.login(
                Utility.sharedPreferences.getString(Constants.PREF_USER_NAME, "")!!,
                Utility.sharedPreferences.getString(Constants.PREF_PASSWORD, "")!!
            ).enqueue(object : Callback<User> {
                override fun onFailure(call: Call<User>, t: Throwable) {
                    Toast.makeText(this@SplashActivity, t.message, Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this@SplashActivity, LoginActivity::class.java)
                        .putExtra(Constants.EXTRA_ROOM_ID, intent.getStringExtra(Constants.EXTRA_ROOM_ID)))
                    finish()
                }

                override fun onResponse(call: Call<User>, response: Response<User>) {
                    if (response.isSuccessful && response.body() != null) {
                        response.body()!!.let {
                            Utility.sharedPreferences.edit()
                                .putString(Constants.PREF_USER_NAME, it.email)
                                .putString(Constants.PREF_PASSWORD, it.password)
                                .putString(Constants.PREF_USER, Gson().toJson(it))
                                .apply()
                            startActivity(Intent(this@SplashActivity, HomeActivity::class.java)
                                .putExtra(Constants.EXTRA_ROOM_ID, intent.getStringExtra(Constants.EXTRA_ROOM_ID)))
                            finish()
                        }
                    } else {
                        Toast.makeText(this@SplashActivity, response.errorBody()?.string(), Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this@SplashActivity, LoginActivity::class.java)
                            .putExtra(Constants.EXTRA_ROOM_ID, intent.getStringExtra(Constants.EXTRA_ROOM_ID)))
                        finish()
                    }
                }
            })
        } else {
            Timer().schedule(500) {
                startActivity(Intent(this@SplashActivity, LoginActivity::class.java))
                finish()
            }
        }

        FirebaseMessaging.getInstance().token.addOnCompleteListener {
            Log.e("NEW_FCM_TOKEN", it.result ?: "")
        }
    }

    override fun onPause() {
        overridePendingTransition(0, 0)
        super.onPause()
    }
}