package com.chat.services

import android.app.ActivityManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.RemoteInput
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.chat.R
import com.chat.activities.SplashActivity
import com.chat.models.Message
import com.chat.models.User
import com.chat.utils.Constants
import com.chat.utils.Utility
import com.google.gson.Gson
import okhttp3.MediaType
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ChatMessagingBroadcast : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.hasExtra(Constants.EXTRA_ROOM_ID)) {
            val roomId = intent.getStringExtra(Constants.EXTRA_ROOM_ID)!!
            if (!handleReplyIntent(context, intent, roomId)) {
                if (isApplicationRunning(context)) {
                    LocalBroadcastManager.getInstance(context)
                        .sendBroadcast(Intent(Constants.ACTION_OPEN_ROOM).putExtra(Constants.EXTRA_ROOM_ID, roomId))
                } else {
                    context.startActivity(
                        Intent(context, SplashActivity::class.java)
                            .putExtra("roomId", roomId)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    )
                }
            }
        }
    }

    private fun handleReplyIntent(context: Context, intent: Intent, roomId: String): Boolean {
        val remoteInput = RemoteInput.getResultsFromIntent(intent) ?: return false

        val reply = remoteInput.getCharSequence("KEY_TEXT_REPLY").toString()
        val notificationId = intent.getIntExtra("notificationId", 0)
        val userId = Gson().fromJson(Utility.sharedPreferences.getString(Constants.PREF_USER, ""), User::class.java)?.id.toString()
        val mediaType: MediaType? = MediaType.parse("text/plain")
        Utility.apiClient.sendMessage(
            RequestBody.create(mediaType, userId),
            RequestBody.create(mediaType, roomId),
            RequestBody.create(mediaType, "0"),
            RequestBody.create(mediaType, reply),
            null
        ).enqueue(object : Callback<Message> {
            override fun onFailure(call: Call<Message>, t: Throwable) {
                cancelNotification(notificationId, context, context.getString(R.string.an_error_occurred_message, t.message))
            }

            override fun onResponse(call: Call<Message>, response: Response<Message>) {
                if (response.isSuccessful) {
                    cancelNotification(notificationId, context, context.getString(R.string.you_message_has_been_sent))
                } else {
                    cancelNotification(notificationId, context, context.getString(R.string.an_error_occurred_message, response.errorBody()?.string()))
                }
            }
        })
        return true
    }

    private fun cancelNotification(notificationId: Int, context: Context, toast: String) {
        NotificationManagerCompat.from(context).cancel(notificationId)
        Handler(Looper.getMainLooper()).post {
            Toast.makeText(context, toast, Toast.LENGTH_SHORT).show()
        }
    }

    private fun isApplicationRunning(context: Context): Boolean {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val tasks = activityManager.getRunningTasks(Integer.MAX_VALUE)
        for (task in tasks) {
            if (context.packageName == task.baseActivity?.packageName) {
                return true
            }
        }
        return false
    }
}