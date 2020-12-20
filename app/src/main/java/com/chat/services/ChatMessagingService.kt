package com.chat.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.chat.R
import com.chat.models.User
import com.chat.utils.Constants
import com.chat.utils.Utility
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ChatMessagingService : FirebaseMessagingService() {

    companion object {
        private var NOTIFICATION_ID = 0
        var CURRENT_ROOM_ID = -1
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        /**
         * messageType:
         * 0: normal message
         * 1: image
         * 2: video
         * 3: audio
         **/
        remoteMessage.data.let { data ->
            val id = data["id"]
            val body = data["message"]
            val type = data["messageType"]
            val time = data["messageTime"]
            val roomId = data["roomId"]
            val senderId = data["senderId"]
            val senderName = data["senderName"]
            val senderImage = data["senderImage"]

            if (CURRENT_ROOM_ID.toString() == roomId) {
                val intent = Intent(Constants.ACTION_NEW_MESSAGE)
                intent.putExtra(Constants.EXTRA_MESSAGE_ID, id)
                intent.putExtra(Constants.EXTRA_MESSAGE, body)
                intent.putExtra(Constants.EXTRA_MESSAGE_TYPE, type)
                intent.putExtra(Constants.EXTRA_MESSAGE_TIME, time)
                intent.putExtra(Constants.EXTRA_SENDER_ID, senderId)
                intent.putExtra(Constants.EXTRA_SENDER_NAME, senderName)
                intent.putExtra(Constants.EXTRA_SENDER_IMAGE, senderImage)
                LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
            } else {
                showNotification(roomId, remoteMessage.notification?.title, remoteMessage.notification?.body)
            }
        }
    }

    override fun onNewToken(token: String) {
        Log.e("NEW_FCM_TOKEN", "NEW_FCM_TOKEN: $token")
        Gson().fromJson(Utility.sharedPreferences.getString(Constants.PREF_USER, ""), User::class.java)?.id?.let {
            val deviceId = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
            Utility.apiClient.updateFireBaseToken(it, deviceId, token).enqueue(object : Callback<ResponseBody> {
                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                }

                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                }
            })
        }
    }

    private fun showNotification(roomId: String?, notificationTitle: String?, notificationMessage: String?) {
        val intent = Intent(this, ChatMessagingBroadcast::class.java)
            .putExtra(Constants.EXTRA_ROOM_ID, roomId)
        val pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        val notificationBuilder = NotificationCompat.Builder(this, getString(R.string.notification_channel_id))
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(notificationTitle ?: getString(R.string.app_name))
            .setContentText(notificationMessage)
//            .setStyle(NotificationCompat.BigTextStyle().bigText(notificationMessage))
            .setColor(ContextCompat.getColor(this, R.color.black))
            .setAutoCancel(true)
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
            .setVibrate(longArrayOf(1000, 1000, 1000, 1000, 1000))
            .setContentIntent(pendingIntent)

        with(NotificationManagerCompat.from(this)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(getString(R.string.notification_channel_id), getString(R.string.app_name), NotificationManager.IMPORTANCE_DEFAULT)
                createNotificationChannel(channel)
            }
            notify(NOTIFICATION_ID++, notificationBuilder.build())
        }
    }
}