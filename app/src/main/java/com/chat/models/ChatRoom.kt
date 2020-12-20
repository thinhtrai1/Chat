package com.chat.models

class ChatRoom {
    val id = 0
    var roomId = -1
    val name: String? = null
    val image: String? = null
    val lastMessage: String? = null
    val lastMessageType = 0
    val lastMessageTime = 0L
    var isHost = 0
    val hostId = 0
    val member = ArrayList<User>()
}