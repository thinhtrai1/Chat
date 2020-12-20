package com.chat.services

import android.app.ActivityManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.chat.activities.SplashActivity
import com.chat.utils.Constants

class ChatMessagingBroadcast : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.hasExtra(Constants.EXTRA_ROOM_ID)) {
            val roomId = intent.getStringExtra(Constants.EXTRA_ROOM_ID)
            if (isApplicationRunning(context)) {
                LocalBroadcastManager.getInstance(context)
                    .sendBroadcast(Intent(Constants.ACTION_OPEN_ROOM).putExtra(Constants.EXTRA_ROOM_ID, roomId))
            } else {
                context.startActivity(
                    Intent(context, SplashActivity::class.java)
                    .putExtra("roomId", roomId)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            }
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