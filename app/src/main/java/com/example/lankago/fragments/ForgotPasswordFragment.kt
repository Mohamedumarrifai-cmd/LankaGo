package com.example.lankago.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.lankago.R

class ForgotPasswordFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.forgot_password, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val etEmail = view.findViewById<EditText>(R.id.et_email)
        val btnSend = view.findViewById<Button>(R.id.btn_send)
        val btnBack = view.findViewById<ImageView>(R.id.btn_back)

        btnBack?.setOnClickListener {
            findNavController().navigateUp()
        }

        btnSend?.setOnClickListener {
            val email = etEmail?.text?.toString()?.trim().orEmpty()
            if (email.isBlank() || !email.contains("@")) {
                etEmail?.error = "Please enter a valid email address"
                etEmail?.requestFocus()
                return@setOnClickListener
            }

            Toast.makeText(
                requireContext(),
                "Password reset link sent to $email",
                Toast.LENGTH_LONG
            ).show()

            findNavController().navigateUp()
        }
    }
}