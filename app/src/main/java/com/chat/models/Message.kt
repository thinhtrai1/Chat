package com.chat.models

class Message {
    var id = 0
    var senderId = 0
    var name: String? = null
    var avatar: String? = null
    var message: String? = null
    var type = 0
    var time: Long = 0
    var isLoadMore = false
}