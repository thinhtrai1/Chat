package com.chat.services

import android.annotation.SuppressLint
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
import com.chat.utils.Constants
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class ChatMessagingService : FirebaseMessagingService() {

    companion object {
        private var NOTIFICATION_ID = 0
        var CURRENT_ROOM_ID = ""
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
            val type = data["type"]
            val body = data["body"]
            val time = data["time"]
            val roomId = data["roomId"]
            val senderId = data["senderId"]
            val senderName = data["senderName"]
            val senderImage = data["senderImage"]
            val receiverId = data["receiverId"]

            if (CURRENT_ROOM_ID == roomId) {
                val intent = Intent(Constants.ACTION_NEW_MESSAGE)
                intent.putExtra(Constants.EXTRA_MESSAGE_ID, id)
                intent.putExtra(Constants.EXTRA_MESSAGE_TYPE, type)
                intent.putExtra(Constants.EXTRA_MESSAGE_BODY, body)
                intent.putExtra(Constants.EXTRA_MESSAGE_TIME, time)
                intent.putExtra(Constants.EXTRA_ROOM_ID, roomId)
                intent.putExtra(Constants.EXTRA_SENDER_ID, senderId)
                intent.putExtra(Constants.EXTRA_SENDER_NAME, senderName)
                intent.putExtra(Constants.EXTRA_SENDER_IMAGE, senderImage)
                intent.putExtra(Constants.EXTRA_RECEIVER_ID, receiverId)
                LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
            } else {
                showNotification(roomId, remoteMessage.notification?.title, remoteMessage.notification?.body)
            }
        }
    }

    @SuppressLint("HardwareIds")
    override fun onNewToken(token: String) {
        val deviceId = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
//        mApi.updateFireBaseToken(token, deviceId)
        Log.e("NEW_FCM_TOKEN", "NEW_FCM_TOKEN: $token")
    }

    private fun showNotification(roomId: String?, notificationTitle: String?, notificationMessage: String?) {
        val intent = Intent(this, ChatMessagingBroadcast::class.java)
            .putExtra(Constants.EXTRA_ROOM_ID, roomId)
        val pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        val notificationBuilder = NotificationCompat.Builder(this, getString(R.string.notification_channel_id))
            .setSmallIcon(R.drawable.ic_app)
            .setContentTitle(notificationTitle ?: getString(R.string.app_name))
            .setContentText(notificationMessage)
//            .setStyle(NotificationCompat.BigTextStyle().bigText(notificationMessage))
            .setColor(ContextCompat.getColor(this, R.color.colorPrimary))
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