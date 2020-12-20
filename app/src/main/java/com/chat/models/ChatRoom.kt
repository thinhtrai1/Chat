package com.chat.models

class ChatRoom {
    val id = 0
    var roomId = -1
    val name: String? = null
    val image: String? = null
    var lastMessage: String? = null
    var lastMessageType = 0
    var lastMessageTime = 0L
    var isHost = false
    val member = ArrayList<User>()
}