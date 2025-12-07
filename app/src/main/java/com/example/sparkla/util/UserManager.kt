package com.example.sparkla.util

import com.example.sparkla.api.UserData

object UserManager {
    var currentUser: UserData? = null

    fun clear() {
        currentUser = null
    }
}
