package com.example.lankago.fragments.admin

import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.lankago.R

class AdminLoginFragment : Fragment() {

    private var isPasswordVisible: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.admin_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val etEmail = view.findViewById<EditText>(R.id.et_admin_email)
        val etPassword = view.findViewById<EditText>(R.id.et_admin_password)
        val btnLogin = view.findViewById<AppCompatButton>(R.id.btn_admin_login)

        // Password visibility toggle
        btnTogglePassword?.setOnClickListener {
            isPasswordVisible = !isPasswordVisible
            if (isPasswordVisible) {
                etPassword?.transformationMethod = HideReturnsTransformationMethod.getInstance()
            } else {
                etPassword?.transformationMethod = PasswordTransformationMethod.getInstance()
            }
            etPassword?.text?.let { text -> etPassword.setSelection(text.length) }
        }

        btnLogin?.setOnClickListener {
            val email = etEmail?.text?.toString()?.trim() ?: ""
            if (email.isNotEmpty() && !email.contains("@")) {
                Toast.makeText(requireContext(), "Please enter a valid admin email", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

        }
    }
}
