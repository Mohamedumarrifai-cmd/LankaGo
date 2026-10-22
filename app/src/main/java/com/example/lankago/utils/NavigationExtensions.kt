package com.example.lankago.utils

import android.os.Bundle
import android.util.Log
import androidx.annotation.IdRes
import androidx.navigation.NavController
import androidx.navigation.NavDirections
import androidx.navigation.NavOptions

/**
 * Extension functions for NavController to guard against rapid multi-click race conditions,
 * invalid destination calls, and unexpected fragment transition states.
 */
fun NavController.safeNavigate(
    @IdRes resId: Int,
    args: Bundle? = null,
    navOptions: NavOptions? = null
) {
    try {
        val currentDest = currentDestination ?: return
        val action = currentDest.getAction(resId)
        if (action != null) {
            navigate(resId, args, navOptions)
            return
        }

        // Check if resId is another destination rather than action
        if (currentDest.id != resId) {
            navigate(resId, args, navOptions)
        }
    } catch (e: Exception) {
        Log.w("NavigationExtensions", "safeNavigate prevented crash: ${e.message}")
    }
}

fun NavController.safeNavigate(
    directions: NavDirections,
    navOptions: NavOptions? = null
) {
    try {
        val currentDest = currentDestination ?: return
        val action = currentDest.getAction(directions.actionId)
        if (action != null) {
            navigate(directions, navOptions)
        }
    } catch (e: Exception) {
        Log.w("NavigationExtensions", "safeNavigate prevented crash: ${e.message}")
    }
}

fun NavController.safePopBackStack(
    @IdRes destinationId: Int? = null,
    inclusive: Boolean = false
): Boolean {
    return try {
        if (destinationId != null) {
            popBackStack(destinationId, inclusive)
        } else {
            popBackStack()
        }
    } catch (e: Exception) {
        Log.w("NavigationExtensions", "safePopBackStack prevented crash: ${e.message}")
        false
    }
}

/**
 * Prevents multiple rapid clicks (debounce) by ignoring clicks within [throttleTimeMs].
 * Highly recommended for buttons triggering navigation transitions.
 */
fun android.view.View.setOnSafeClickListener(
    throttleTimeMs: Long = 600L,
    onSafeClick: (android.view.View) -> Unit
) {
    var lastClickTime = 0L
    this.setOnClickListener { view ->
        val currentTime = android.os.SystemClock.elapsedRealtime()
        if (currentTime - lastClickTime >= throttleTimeMs) {
            lastClickTime = currentTime
            onSafeClick(view)
        }
    }
}

