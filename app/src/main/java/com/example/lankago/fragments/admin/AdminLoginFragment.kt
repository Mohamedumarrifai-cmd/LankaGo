package com.example.lankago.fragments.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.lankago.R
import com.example.lankago.data.User
import com.example.lankago.viewmodels.AdminViewModel
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * Screen providing dedicated administrator authentication.
 * Uses FirebaseAuth + Firebase Realtime Database `/users/{uid}/role` check.
 */
class AdminLoginFragment : Fragment() {

    private val adminViewModel: AdminViewModel by activityViewModels()
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_admin_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tilUsername = view.findViewById<TextInputLayout>(R.id.til_admin_username)
        val etUsername = view.findViewById<TextInputEditText>(R.id.et_admin_username)
        val tilPassword = view.findViewById<TextInputLayout>(R.id.til_admin_password)
        val etPassword = view.findViewById<TextInputEditText>(R.id.et_admin_password)
        val btnLogin = view.findViewById<MaterialButton>(R.id.btn_admin_login)

        btnLogin.setOnClickListener {
            val rawInput = etUsername.text?.toString().orEmpty().trim()
            val password = etPassword.text?.toString().orEmpty().trim()

            // Reset field errors
            tilUsername.error = null
            tilPassword.error = null

            if (rawInput.isEmpty()) {
                tilUsername.error = "Admin email or username is required"
                showSnackbar(view, "Please enter your admin credentials", isError = true)
                return@setOnClickListener
            }

            if (password.isEmpty()) {
                tilPassword.error = "Password is required"
                showSnackbar(view, "Please enter your admin password", isError = true)
                return@setOnClickListener
            }

            val email = if (rawInput.contains("@")) rawInput else "${rawInput}@lankago.lk"

            btnLogin.isEnabled = false

            viewLifecycleOwner.lifecycleScope.launch {
                try {
                    // Developer bypass for offline demo testing
                    if (rawInput == "admin" && password == "admin123") {
                        adminViewModel.authenticate("admin", "admin123")
                        findNavController().navigate(
                            R.id.action_adminLoginFragment_to_adminDashboardFragment
                        )
                        return@launch
                    }

                    // 1. FirebaseAuth Email & Password Sign-In
                    val result = auth.signInWithEmailAndPassword(email, password).await()
                    val uid = result.user?.uid ?: ""

                    // 2. Verify admin role from Realtime Database /users/{uid} node
                    val snapshot = database.reference.child("users").child(uid).get().await()
                    val user = snapshot.getValue(User::class.java)
                    val role = user?.role ?: snapshot.child("role").getValue(String::class.java) ?: "user"

                    if (role.equals("admin", ignoreCase = true)) {
                        adminViewModel.authenticate(email, password)
                        findNavController().navigate(
                            R.id.action_adminLoginFragment_to_adminDashboardFragment
                        )
                    } else {
                        btnLogin.isEnabled = true
                        tilUsername.error = "Unauthorized role"
                        showSnackbar(view, "Access Denied: Account '$email' does not have admin permissions", isError = true)
                    }

                } catch (e: Exception) {
                    btnLogin.isEnabled = true
                    tilPassword.error = "Authentication failed"
                    showSnackbar(view, "Sign-in Failed: ${e.localizedMessage ?: "Invalid credentials"}", isError = true)
                }
            }
        }
    }

    private fun showSnackbar(rootView: View, message: String, isError: Boolean) {
        val snackbar = Snackbar.make(rootView, message, Snackbar.LENGTH_LONG)
        if (isError) {
            snackbar.setBackgroundTint(requireContext().getColor(R.color.admin_danger))
            snackbar.setTextColor(requireContext().getColor(R.color.white))
        } else {
            snackbar.setBackgroundTint(requireContext().getColor(R.color.admin_primary_navy))
            snackbar.setTextColor(requireContext().getColor(R.color.white))
        }
        snackbar.show()
    }
}
