package com.chat.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.chat.R
import com.chat.models.User
import com.chat.utils.Constants
import com.chat.utils.Utility
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException
import java.util.*
import kotlin.concurrent.schedule

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        var preIntent: Intent? = null
        Timer().schedule(500) {
            if (preIntent != null) {
                startActivity(preIntent)
                finish()
            } else {
                preIntent = Intent()
            }
        }

        if (Utility.sharedPreferences.getString(Constants.PREF_USER_NAME, null) != null
            && Utility.sharedPreferences.getString(Constants.PREF_PASSWORD, null) != null) {
            Utility.apiClient.login(
                Utility.sharedPreferences.getString(Constants.PREF_USER_NAME, "")!!,
                Utility.sharedPreferences.getString(Constants.PREF_PASSWORD, "")!!
            ).enqueue(object : Callback<User> {
                override fun onFailure(call: Call<User>, t: Throwable) {
                    showToast(t)
                    val intent = Intent(this@SplashActivity, LoginActivity::class.java)
                        .putExtra(Constants.EXTRA_ROOM_ID, intent.getStringExtra(Constants.EXTRA_ROOM_ID))
                    if (preIntent == null) {
                        preIntent = intent
                    } else {
                        startActivity(intent)
                        finish()
                    }
                }

                override fun onResponse(call: Call<User>, response: Response<User>) {
                    if (response.isSuccessful && response.body() != null) {
                        response.body()!!.let {
                            Utility.sharedPreferences.edit()
                                .putString(Constants.PREF_USER_NAME, it.email)
                                .putString(Constants.PREF_PASSWORD, it.password)
                                .putString(Constants.PREF_USER, Gson().toJson(it))
                                .apply()
                            val intent = Intent(this@SplashActivity, HomeActivity::class.java)
                                .putExtra(Constants.EXTRA_ROOM_ID, intent.getStringExtra("roomId"))
                            if (preIntent == null) {
                                preIntent = intent
                            } else {
                                startActivity(intent)
                                finish()
                            }
                        }
                    } else {
                        val intent = Intent(this@SplashActivity, LoginActivity::class.java)
                            .putExtra(Constants.EXTRA_ROOM_ID, intent.getStringExtra("roomId"))
                        if (preIntent == null) {
                            preIntent = intent
                        } else {
                            startActivity(intent)
                            finish()
                        }
                    }
                }
            })
        } else {
            preIntent = Intent(this@SplashActivity, LoginActivity::class.java)
        }
    }

    private fun showToast(throwable: Throwable) {
        when {
            throwable is IOException -> {
                Toast.makeText(this, getString(R.string.please_check_the_network_connection), Toast.LENGTH_SHORT).show()
            }
            throwable.message != null -> {
                Toast.makeText(this, getString(R.string.an_error_occurred_message, throwable.message), Toast.LENGTH_SHORT).show()
            }
            else -> {
                Toast.makeText(this, getString(R.string.an_error_occurred), Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onPause() {
        overridePendingTransition(0, 0)
        super.onPause()
    }
}