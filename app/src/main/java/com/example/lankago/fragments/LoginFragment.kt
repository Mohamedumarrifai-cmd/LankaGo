package com.example.lankago.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.lankago.R
import com.example.lankago.viewmodels.AuthState
import com.example.lankago.viewmodels.AuthViewModel
import com.example.lankago.utils.safeNavigate
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class LoginFragment : Fragment() {

    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tilEmail = view.findViewById<TextInputLayout>(R.id.til_email)
        val etEmail = view.findViewById<EditText>(R.id.et_email)
        val tilPassword = view.findViewById<TextInputLayout>(R.id.til_password)
        val etPassword = view.findViewById<EditText>(R.id.et_password)
        val btnLogin = view.findViewById<Button>(R.id.btn_login)
        val pbLoading = view.findViewById<ProgressBar>(R.id.pb_login_loading)
        val tvRegisterLink = view.findViewById<View>(R.id.tv_register_link)

        // Observe Authentication State
        viewLifecycleOwner.lifecycleScope.launch {
            authViewModel.authState.collectLatest { state ->
                when (state) {
                    is AuthState.Idle -> {
                        pbLoading?.visibility = View.GONE
                        btnLogin?.isEnabled = true
                    }
                    is AuthState.Loading -> {
                        pbLoading?.visibility = View.VISIBLE
                        btnLogin?.isEnabled = false
                        tilEmail?.error = null
                        tilPassword?.error = null
                    }
                    is AuthState.Authenticated -> {
                        pbLoading?.visibility = View.GONE
                        btnLogin?.isEnabled = true
                        if (isAdded) {
                            if (state.role.equals("admin", ignoreCase = true)) {
                                findNavController().safeNavigate(R.id.action_loginFragment_to_adminDashboardFragment)
                            } else {
                                findNavController().safeNavigate(R.id.action_loginFragment_to_homeFragment)
                            }
                        }
                    }
                    is AuthState.Error -> {
                        pbLoading?.visibility = View.GONE
                        btnLogin?.isEnabled = true
                        Snackbar.make(view, state.message, Snackbar.LENGTH_LONG).show()
                    }
                }
            }
        }

        // 1. Top Navigation: Back and Skip as Guest
        val btnBack = view.findViewById<View>(R.id.btn_back_login)
        val btnSkip = view.findViewById<View>(R.id.btn_skip_login)

        btnBack?.setOnClickListener {
            if (!findNavController().navigateUp()) {
                findNavController().safeNavigate(R.id.action_loginFragment_to_homeFragment)
            }
        }

        btnSkip?.setOnClickListener {
            findNavController().safeNavigate(R.id.action_loginFragment_to_homeFragment)
        }

        // 2. Role Switcher: Navigate to Admin Portal when clicked
        val btnRoleUser = view.findViewById<View>(R.id.btn_role_user)
        val btnRoleAdmin = view.findViewById<View>(R.id.btn_role_admin)
        val tvSwitchToAdmin = view.findViewById<View>(R.id.tv_switch_to_admin)

        btnRoleUser?.setOnClickListener {
            // Already in Traveler mode
            btnLogin?.text = "Log In as Traveler"
        }

        btnRoleAdmin?.setOnClickListener {
            findNavController().safeNavigate(R.id.action_loginFragment_to_adminLoginFragment)
        }

        tvSwitchToAdmin?.setOnClickListener {
            findNavController().safeNavigate(R.id.action_loginFragment_to_adminLoginFragment)
        }

        // 3. Trigger Login with Role Routing
        btnLogin?.setOnClickListener {
            val email = etEmail?.text?.toString() ?: ""
            val password = etPassword?.text?.toString() ?: ""

            if (email.isBlank()) {
                tilEmail?.error = "Please enter your email address"
                etEmail?.requestFocus()
                return@setOnClickListener
            }
            tilEmail?.error = null

            if (password.isBlank()) {
                tilPassword?.error = "Please enter your password"
                etPassword?.requestFocus()
                return@setOnClickListener
            }
            tilPassword?.error = null

            authViewModel.login(requireContext(), email, password)
        }

        // 4. Navigate to Forgot Password Screen
        view.findViewById<View>(R.id.tv_forgot_password)?.setOnClickListener {
            findNavController().safeNavigate(R.id.action_loginFragment_to_forgotPasswordFragment)
        }

        // 5. Navigate to Register Screen (both link and footer)
        val layoutRegisterFooter = view.findViewById<View>(R.id.layout_register_footer)
        tvRegisterLink?.setOnClickListener {
            findNavController().safeNavigate(R.id.action_loginFragment_to_registerFragment)
        }
        layoutRegisterFooter?.setOnClickListener {
            findNavController().safeNavigate(R.id.action_loginFragment_to_registerFragment)
        }
    }
}