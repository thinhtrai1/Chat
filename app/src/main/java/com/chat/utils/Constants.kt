package com.chat.utils

import com.chat.BuildConfig

object Constants {
    const val BASE_URL = "http://ducthinh-bestbus.000webhostapp.com/"

    const val PREF_USER_NAME = "PREF_USER_NAME"
    const val PREF_PASSWORD = "PREF_PASSWORD"
    const val PREF_USER = "PREF_USER"

    const val ACTION_NEW_MESSAGE = BuildConfig.APPLICATION_ID.plus("ACTION_NEW_MESSAGE")
    const val ACTION_OPEN_ROOM = BuildConfig.APPLICATION_ID.plus("ACTION_OPEN_ROOM")

    const val EXTRA_MESSAGE_ID = "EXTRA_MESSAGE_ID"
    const val EXTRA_MESSAGE = "EXTRA_MESSAGE"
    const val EXTRA_MESSAGE_TYPE = "EXTRA_MESSAGE_TYPE"
    const val EXTRA_MESSAGE_TIME = "EXTRA_MESSAGE_TIME"
    const val EXTRA_ROOM_ID = "EXTRA_MESSAGE_ID"
    const val EXTRA_SENDER_ID = "EXTRA_MESSAGE_SENDER_ID"
    const val EXTRA_SENDER_NAME = "EXTRA_MESSAGE_SENDER_NAME"
    const val EXTRA_SENDER_IMAGE = "EXTRA_MESSAGE_SENDER_IMAGE"
}