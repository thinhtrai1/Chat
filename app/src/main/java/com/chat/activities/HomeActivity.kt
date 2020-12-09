package com.chat.activities

import android.os.Bundle
import com.chat.R
import com.chat.utils.Utility

class HomeActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        Utility.apiClient
    }
}