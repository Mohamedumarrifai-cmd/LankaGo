package com.example.lankago.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.lankago.R
import com.example.lankago.viewmodels.AuthState
import com.example.lankago.viewmodels.AuthViewModel
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class RegisterFragment : Fragment() {

    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_register, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnBack = view.findViewById<ImageView>(R.id.btn_back)
        val tilName = view.findViewById<TextInputLayout>(R.id.til_name)
        val etName = view.findViewById<EditText>(R.id.et_name)
        val tilEmail = view.findViewById<TextInputLayout>(R.id.til_email)
        val etEmail = view.findViewById<EditText>(R.id.et_email)
        val tilPassword = view.findViewById<TextInputLayout>(R.id.til_password)
        val etPassword = view.findViewById<EditText>(R.id.et_password)
        val btnRegister = view.findViewById<Button>(R.id.btn_register)
        val pbLoading = view.findViewById<ProgressBar>(R.id.pb_register_loading)
        val tvLoginLink = view.findViewById<View>(R.id.tv_login_link)

        btnBack?.setOnClickListener {
            findNavController().navigateUp()
        }

        // Observe Authentication State
        viewLifecycleOwner.lifecycleScope.launch {
            authViewModel.authState.collectLatest { state ->
                when (state) {
                    is AuthState.Idle -> {
                        pbLoading?.visibility = View.GONE
                        btnRegister?.isEnabled = true
                    }
                    is AuthState.Loading -> {
                        pbLoading?.visibility = View.VISIBLE
                        btnRegister?.isEnabled = false
                        tilName?.error = null
                        tilEmail?.error = null
                        tilPassword?.error = null
                    }
                    is AuthState.Authenticated -> {
                        pbLoading?.visibility = View.GONE
                        btnRegister?.isEnabled = true
                        if (isAdded) {
                            try {
                                findNavController().navigate(R.id.action_registerFragment_to_homeFragment)
                            } catch (e: Exception) {
                                // Ignore navigation race conditions
                            }
                        }
                    }
                    is AuthState.Error -> {
                        pbLoading?.visibility = View.GONE
                        btnRegister?.isEnabled = true
                        Snackbar.make(view, state.message, Snackbar.LENGTH_LONG).show()
                    }
                }
            }
        }

        // Trigger Registration
        btnRegister?.setOnClickListener {
            val name = etName?.text?.toString() ?: ""
            val email = etEmail?.text?.toString() ?: ""
            val password = etPassword?.text?.toString() ?: ""

            if (name.isBlank()) {
                tilName?.error = "Please enter your full name"
                etName?.requestFocus()
                return@setOnClickListener
            }
            tilName?.error = null

            if (email.isBlank()) {
                tilEmail?.error = "Please enter your email address"
                etEmail?.requestFocus()
                return@setOnClickListener
            }
            tilEmail?.error = null

            if (password.length < 6) {
                tilPassword?.error = "Password must be at least 6 characters"
                etPassword?.requestFocus()
                return@setOnClickListener
            }
            tilPassword?.error = null

            authViewModel.register(requireContext(), name, email, password)
        }

        // Navigate back to Login Screen
        tvLoginLink?.setOnClickListener {
            findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
        }
    }
}