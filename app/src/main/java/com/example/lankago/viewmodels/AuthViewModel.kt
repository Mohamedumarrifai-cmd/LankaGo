package com.example.lankago.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lankago.data.User
import com.example.lankago.utils.AuthManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Authenticated(val email: String, val name: String, val role: String = "user") : AuthState()
    data class Error(val message: String) : AuthState()
}

sealed class SessionCheckState {
    object Checking : SessionCheckState()
    data class Authenticated(val role: String = "user") : SessionCheckState()
    object Unauthenticated : SessionCheckState()
}

/**
 * Production AuthViewModel integrated with FirebaseAuth and Realtime Database `/users/{uid}`.
 */
class AuthViewModel(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
) : ViewModel() {

    private val _sessionCheckState = MutableStateFlow<SessionCheckState>(SessionCheckState.Checking)
    val sessionCheckState: StateFlow<SessionCheckState> = _sessionCheckState.asStateFlow()

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    /**
     * Checks whether an active user session exists in FirebaseAuth or local storage.
     */
    fun checkSession(context: Context) {
        viewModelScope.launch {
            _sessionCheckState.value = SessionCheckState.Checking
            delay(1200L) // Branding display
            val currentUser = auth.currentUser
            val localLoggedIn = AuthManager.isLoggedIn(context)
            if (currentUser != null) {
                val role = try {
                    val snap = database.reference.child("users").child(currentUser.uid).child("role").get().await()
                    snap.getValue(String::class.java) ?: "user"
                } catch (e: Exception) {
                    "user"
                }
                _sessionCheckState.value = SessionCheckState.Authenticated(role)
            } else if (localLoggedIn) {
                _sessionCheckState.value = SessionCheckState.Authenticated("user")
            } else {
                _sessionCheckState.value = SessionCheckState.Unauthenticated
            }
        }
    }

    /**
     * Performs authentication using FirebaseAuth and retrieves user role from `/users/{uid}`.
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
            try {
                // Firebase Auth Sign In
                val authResult = auth.signInWithEmailAndPassword(trimmedEmail, trimmedPass).await()
                val uid = authResult.user?.uid ?: ""

                // Fetch user record from /users/{uid}
                val userSnapshot = database.reference.child("users").child(uid).get().await()
                val user = userSnapshot.getValue(User::class.java)

                val name = user?.name ?: authResult.user?.displayName ?: "Traveler"
                val role = user?.role ?: "user"

                AuthManager.login(context, trimmedEmail, name)
                _authState.value = AuthState.Authenticated(trimmedEmail, name, role)

            } catch (e: Exception) {
                // Developer fallback for mock admin testing
                if (trimmedEmail == "admin@lankago.lk" && trimmedPass == "admin123") {
                    AuthManager.login(context, trimmedEmail)
                    _authState.value = AuthState.Authenticated(trimmedEmail, "Super Admin", "admin")
                } else {
                    _authState.value = AuthState.Error(e.localizedMessage ?: "Authentication failed")
                }
            }
        }
    }

    /**
     * Performs new user registration with FirebaseAuth and creates `/users/{uid}` node.
     */
    fun register(
        context: Context,
        name: String,
        email: String,
        pass: String,
        contactNumber: String = "",
        address: String = "",
        role: String = "user"
    ) {
        val trimmedName = name.trim()
        val trimmedEmail = email.trim()
        val trimmedPass = pass.trim()
        val trimmedContact = contactNumber.trim()
        val trimmedAddress = address.trim()

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
            try {
                // 1. Create User in FirebaseAuth
                val result = auth.createUserWithEmailAndPassword(trimmedEmail, trimmedPass).await()
                val uid = result.user?.uid ?: ""

                // 2. Save User Details under /users/{uid} node
                val user = User(
                    uid = uid,
                    name = trimmedName,
                    email = trimmedEmail,
                    role = role,
                    contactNumber = trimmedContact,
                    phoneNumber = trimmedContact,
                    address = trimmedAddress,
                    createdAt = System.currentTimeMillis()
                )
                database.reference.child("users").child(uid).setValue(user).await()

                AuthManager.register(context, trimmedName, trimmedEmail)
                _authState.value = AuthState.Authenticated(trimmedEmail, trimmedName, role)

            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.localizedMessage ?: "Registration failed")
            }
        }
    }

    fun logout() {
        auth.signOut()
        _authState.value = AuthState.Idle
    }

    fun resetAuthState() {
        _authState.value = AuthState.Idle
    }
}
