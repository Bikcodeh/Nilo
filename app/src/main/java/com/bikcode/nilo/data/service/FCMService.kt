package com.bikcode.nilo.data.service

import android.util.Log
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import com.bikcode.nilo.presentation.util.Constants.PROPERTY_TOKEN
import com.google.firebase.messaging.FirebaseMessagingService

class FCMService : FirebaseMessagingService() {

    //This is executed only the first time when the app starts
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        registerNewTokenLocal(token)
    }

    private fun registerNewTokenLocal(newToken: String) {
        val preferences = PreferenceManager.getDefaultSharedPreferences(this)

        preferences.edit {
            putString(PROPERTY_TOKEN, newToken)
                .apply()
        }

        Log.i("NEW TOKEN *-*-*-*", newToken)
    }
}