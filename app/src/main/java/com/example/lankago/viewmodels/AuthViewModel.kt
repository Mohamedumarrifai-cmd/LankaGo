package com.example.lankago.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lankago.utils.AuthManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Authenticated(val email: String, val name: String) : AuthState()
    data class Error(val message: String) : AuthState()
}

sealed class SessionCheckState {
    object Checking : SessionCheckState()
    object Authenticated : SessionCheckState()
    object Unauthenticated : SessionCheckState()
}

class AuthViewModel : ViewModel() {

    private val _sessionCheckState = MutableStateFlow<SessionCheckState>(SessionCheckState.Checking)
    val sessionCheckState: StateFlow<SessionCheckState> = _sessionCheckState.asStateFlow()

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    /**
     * Checks whether an active user session exists in local storage.
     * Includes a 1.5s simulated splash delay for smooth branding display.
     */
    fun checkSession(context: Context) {
        viewModelScope.launch {
            _sessionCheckState.value = SessionCheckState.Checking
            delay(1500L) // Ensure splash logo is appreciated
            val loggedIn = AuthManager.isLoggedIn(context)
            if (loggedIn) {
                _sessionCheckState.value = SessionCheckState.Authenticated
            } else {
                _sessionCheckState.value = SessionCheckState.Unauthenticated
            }
        }
    }

    /**
     * Performs authentication with email and password validation.
     */
    fun login(context: Context, email: String, pass: String) {
        val trimmedEmail = email.trim()
        val trimmedPass = pass.trim()

        if (trimmedEmail.isEmpty()) {
            _authState.value = AuthState.Error("Please enter your email address")
            return
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(trimmedEmail).matches()) {
            _authState.value = AuthState.Error("Please enter a valid email address")
            return
        }

        if (trimmedPass.length < 6) {
            _authState.value = AuthState.Error("Password must be at least 6 characters")
            return
        }

        viewModelScope.launch {
            _authState.value = AuthState.Loading
            delay(800L) // Simulate network/auth verification
            AuthManager.login(context, trimmedEmail)
            val name = AuthManager.getLoggedInName(context)
            _authState.value = AuthState.Authenticated(trimmedEmail, name)
        }
    }

    /**
     * Performs new user registration with full name, email, and password validation.
     */
    fun register(context: Context, name: String, email: String, pass: String) {
        val trimmedName = name.trim()
        val trimmedEmail = email.trim()
        val trimmedPass = pass.trim()

        if (trimmedName.isEmpty()) {
            _authState.value = AuthState.Error("Please enter your full name")
            return
        }

        if (trimmedEmail.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(trimmedEmail).matches()) {
            _authState.value = AuthState.Error("Please enter a valid email address")
            return
        }

        if (trimmedPass.length < 6) {
            _authState.value = AuthState.Error("Password must be at least 6 characters")
            return
        }

        viewModelScope.launch {
            _authState.value = AuthState.Loading
            delay(800L) // Simulate network/auth registration
            AuthManager.register(context, trimmedName, trimmedEmail)
            _authState.value = AuthState.Authenticated(trimmedEmail, trimmedName)
        }
    }

    fun resetAuthState() {
        _authState.value = AuthState.Idle
    }
}
