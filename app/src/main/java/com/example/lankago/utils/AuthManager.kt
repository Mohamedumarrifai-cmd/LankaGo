package com.example.lankago.utils

import android.content.Context
import android.content.SharedPreferences


  //Singleton managing local user session persistence for LankaGo.

object AuthManager {

    private const val PREF_NAME = "lankago_auth_pref"
    private const val KEY_IS_LOGGED_IN = "key_is_logged_in"
    private const val KEY_USER_EMAIL = "key_user_email"
    private const val KEY_USER_NAME = "key_user_name"

    private fun getPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun login(context: Context, email: String, name: String? = null) {
        val editor = getPreferences(context).edit()
        editor.putBoolean(KEY_IS_LOGGED_IN, true)
        editor.putString(KEY_USER_EMAIL, email)
        if (!name.isNullOrBlank()) {
            editor.putString(KEY_USER_NAME, name)
        } else {
            val defaultName = email.substringBefore("@").replace(".", " ")
                .split(" ").joinToString(" ") { word ->
                    word.lowercase().replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
                }
            editor.putString(KEY_USER_NAME, defaultName)
        }
        editor.apply()
    }

    fun register(context: Context, name: String, email: String) {
        login(context, email, name)
    }

    fun isLoggedIn(context: Context): Boolean {
        return getPreferences(context).getBoolean(KEY_IS_LOGGED_IN, false)
    }

    fun getLoggedInEmail(context: Context): String {
        return getPreferences(context).getString(KEY_USER_EMAIL, "traveler@lankago.lk") ?: "traveler@lankago.lk"
    }

    fun getLoggedInName(context: Context): String {
        return getPreferences(context).getString(KEY_USER_NAME, "Sri Lanka Traveler") ?: "Sri Lanka Traveler"
    }

    fun logout(context: Context) {
        getPreferences(context).edit().clear().apply()
    }
}
