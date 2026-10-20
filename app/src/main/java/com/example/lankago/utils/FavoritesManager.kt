package com.example.lankago.utils

import android.content.Context
import android.content.SharedPreferences

object FavoritesManager {

    private const val PREF_NAME = "lankago_favorites_pref"
    private const val KEY_FAVORITES = "key_favorite_place_ids"

    private fun getPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun addFavorite(context: Context, placeId: String) {
        val current = HashSet(getAllFavorites(context))
        current.add(placeId)
        getPreferences(context).edit().putStringSet(KEY_FAVORITES, current).apply()
    }

    fun removeFavorite(context: Context, placeId: String) {
        val current = HashSet(getAllFavorites(context))
        current.remove(placeId)
        getPreferences(context).edit().putStringSet(KEY_FAVORITES, current).apply()
    }

    fun isFavorite(context: Context, placeId: String): Boolean {
        return getAllFavorites(context).contains(placeId)
    }

    fun getAllFavorites(context: Context): Set<String> {
        val set = getPreferences(context).getStringSet(KEY_FAVORITES, null)
        return set?.toSet() ?: emptySet()
    }

    /**
     * Convenience method to toggle favorite status.
     * Returns true if place is now added, false if removed.
     */
    fun toggleFavorite(context: Context, placeId: String): Boolean {
        return if (isFavorite(context, placeId)) {
            removeFavorite(context, placeId)
            false
        } else {
            addFavorite(context, placeId)
            true
        }
    }
}
