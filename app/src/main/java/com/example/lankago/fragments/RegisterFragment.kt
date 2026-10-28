package com.example.lankago.fragments

import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.lankago.R
import com.example.lankago.data.User
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class RegisterFragment : Fragment() {

            //lazy--first time access karana welawedi instance eka initialize karanawa.
    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val database: FirebaseDatabase by lazy { FirebaseDatabase.getInstance() }

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
        val tilContactNumber = view.findViewById<TextInputLayout>(R.id.til_contact_number)
        val etContactNumber = view.findViewById<EditText>(R.id.et_contact_number)
        val tilAddress = view.findViewById<TextInputLayout>(R.id.til_address)
        val etAddress = view.findViewById<EditText>(R.id.et_address)
        val btnRegister = view.findViewById<Button>(R.id.btn_register)
        val pbLoading = view.findViewById<ProgressBar>(R.id.pb_register_loading)
        val tvLoginLink = view.findViewById<TextView>(R.id.tv_login_link)

        btnBack?.setOnClickListener {
            findNavController().navigateUp()
        }

        tvLoginLink?.setOnClickListener {
            findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
        }

        btnRegister?.setOnClickListener {
            val name = etName?.text?.toString()?.trim() ?: ""
            val email = etEmail?.text?.toString()?.trim() ?: ""
            val password = etPassword?.text?.toString()?.trim() ?: ""
            val contactNumber = etContactNumber?.text?.toString()?.trim() ?: ""
            val address = etAddress?.text?.toString()?.trim() ?: ""

            // Reset error states
            tilName?.error = null
            tilEmail?.error = null
            tilPassword?.error = null
            tilContactNumber?.error = null
            tilAddress?.error = null

            // Comprehensive field validations(input validation)
            if (name.isEmpty()) {
                tilName?.error = "Please enter your full name"
                etName?.requestFocus()
                return@setOnClickListener
            }

            if (email.isEmpty()) {
                tilEmail?.error = "Please enter your email address"
                etEmail?.requestFocus()
                return@setOnClickListener
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                tilEmail?.error = "Please enter a valid email address"
                etEmail?.requestFocus()         //check the valid email format
                return@setOnClickListener
            }

            if (password.isEmpty()) {
                tilPassword?.error = "Please enter a password"
                etPassword?.requestFocus()
                return@setOnClickListener
            }

            if (password.length < 6) {
                tilPassword?.error = "Password must be at least 6 characters"
                etPassword?.requestFocus()
                return@setOnClickListener
            }

            if (contactNumber.isEmpty()) {
                tilContactNumber?.error = "Please enter your contact number"
                etContactNumber?.requestFocus()
                return@setOnClickListener
            }

            if (address.isEmpty()) {
                tilAddress?.error = "Please enter your address"
                etAddress?.requestFocus()
                return@setOnClickListener
            }

            // Lock UI and show progress indicator
            pbLoading?.visibility = View.VISIBLE
            btnRegister.isEnabled = false

            viewLifecycleOwner.lifecycleScope.launch {
                try {
                    // 1. Create User in Firebase Authentication
                    val authResult = auth.createUserWithEmailAndPassword(email, password).await()
                    val firebaseUser = authResult.user
                        ?: throw IllegalStateException("Firebase user creation failed: user object is null")
                    val uid = firebaseUser.uid

                    // 2. Persist user details into Firebase Realtime Database under /users/{uid}
                    val userProfile = User(
                        uid = uid,
                        name = name,
                        email = email,
                        role = "user",
                        contactNumber = contactNumber,
                        phoneNumber = contactNumber,
                        address = address,
                        profileImageUrl = "",
                        createdAt = System.currentTimeMillis()
                    )

                    database.reference      //save details to realtime database
                        .child("users")
                        .child(uid)
                        .setValue(userProfile)
                        .await()

                    // 3. UI Feedback and automatic navigation
                    pbLoading?.visibility = View.GONE
                    btnRegister.isEnabled = true

                    Snackbar.make(
                        view,
                        "Registration successful! Please log in.",
                        Snackbar.LENGTH_SHORT
                    ).show()

                    // Navigate to LoginFragment using Navigation Component
                    if (isAdded) {
                        findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
                    }

                } catch (e: Exception) {
                    pbLoading?.visibility = View.GONE
                    btnRegister.isEnabled = true

                    val errorMessage = when (e) {
                        is FirebaseAuthWeakPasswordException -> "Password is too weak. Please choose a stronger password."
                        is FirebaseAuthUserCollisionException -> "An account with this email address already exists."
                        is FirebaseAuthInvalidCredentialsException -> "The email address is improperly formatted."
                        else -> e.localizedMessage ?: "Registration failed. Please check your network and try again."
                    }

                    Snackbar.make(view, errorMessage, Snackbar.LENGTH_LONG).show()
                }
            }
        }
    }
}