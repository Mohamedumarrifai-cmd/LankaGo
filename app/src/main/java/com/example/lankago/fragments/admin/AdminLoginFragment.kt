package com.example.lankago.fragments.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.example.lankago.R
import com.example.lankago.viewmodels.AdminViewModel
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

/**
 * Screen providing dedicated administrator authentication.
 */
class AdminLoginFragment : Fragment() {

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

        // Return Navigation: Move from Admin Login to Traveler Login
        val navigateToUserLogin = {
            try {
                findNavController().navigate(R.id.action_adminLoginFragment_to_loginFragment)
            } catch (e: Exception) {
                findNavController().navigateUp()
            }
        }
        view.findViewById<View>(R.id.btn_back_to_user_login)?.setOnClickListener { navigateToUserLogin() }
        view.findViewById<View>(R.id.btn_admin_role_user)?.setOnClickListener { navigateToUserLogin() }
        view.findViewById<View>(R.id.btn_switch_to_user_login)?.setOnClickListener { navigateToUserLogin() }

        btnLogin.setOnClickListener {
            val username = etUsername.text?.toString().orEmpty().trim()
            val password = etPassword.text?.toString().orEmpty().trim()

            // Reset field errors
            tilUsername?.error = null
            tilPassword?.error = null

            if (username == "admin" && password == "admin123") {
                adminViewModel.authenticate("admin", "admin123")
                val navOptions = NavOptions.Builder()
                    .setPopUpTo(R.id.adminLoginFragment, true)
                    .build()
                findNavController().navigate(
                    R.id.action_adminLoginFragment_to_adminDashboardFragment,
                    null,
                    navOptions
                )
            } else {
                tilUsername?.error = "Invalid credentials"
                tilPassword?.error = "Invalid credentials"
                Toast.makeText(requireContext(), "Invalid Admin Credentials", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
