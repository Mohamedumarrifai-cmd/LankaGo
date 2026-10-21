package com.example.lankago.fragments.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.lankago.R
import com.example.lankago.viewmodels.AdminAuthState
import com.example.lankago.viewmodels.AdminUiEvent
import com.example.lankago.viewmodels.AdminViewModel
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.launch

/**
 * Screen providing dedicated administrator authentication.
 * Uses mock credentials (admin / admin123) and coordinates with [AdminViewModel].
 */
class AdminLoginFragment : Fragment() {

    // Shared across the admin flow so state persists seamlessly
    private val adminViewModel: AdminViewModel by activityViewModels()

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

        // Observe single-event UI notifications (e.g., error snackbars)
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                adminViewModel.uiEvent.collect { event ->
                    when (event) {
                        is AdminUiEvent.ShowSnackbar -> {
                            showSnackbar(view, event.message, event.isError)
                        }
                        else -> Unit
                    }
                }
            }
        }

        btnLogin.setOnClickListener {
            val username = etUsername.text?.toString().orEmpty().trim()
            val password = etPassword.text?.toString().orEmpty().trim()

            // Reset field errors
            tilUsername.error = null
            tilPassword.error = null

            if (username.isEmpty()) {
                tilUsername.error = "Admin username is required"
                showSnackbar(view, "Please enter your admin username", isError = true)
                return@setOnClickListener
            }

            if (password.isEmpty()) {
                tilPassword.error = "Password is required"
                showSnackbar(view, "Please enter your admin password", isError = true)
                return@setOnClickListener
            }

            // Perform mock authentication via ViewModel
            val isSuccess = adminViewModel.authenticate(username, password)

            if (isSuccess) {
                // Navigate to AdminDashboardFragment and clear login from backstack
                findNavController().navigate(
                    R.id.action_adminLoginFragment_to_adminDashboardFragment
                )
            } else {
                tilPassword.error = "Incorrect credentials"
                showSnackbar(view, "Access Denied: Invalid admin credentials", isError = true)
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
