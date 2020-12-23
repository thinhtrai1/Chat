package com.chat.models

import java.io.Serializable

class User: Serializable {
    val id = 0
    val name: String? = null
    val phone: String? = null
    val image: String? = null
    val email: String = ""
    val password: String = ""
    var isAdmin = 0
}