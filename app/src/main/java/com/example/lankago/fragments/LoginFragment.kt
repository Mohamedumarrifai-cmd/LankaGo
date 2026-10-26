package com.example.lankago.fragments

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.example.lankago.R
import com.example.lankago.data.User
import com.example.lankago.utils.AuthManager
import com.example.lankago.utils.safeNavigate
import com.example.lankago.viewmodels.AuthState
import com.example.lankago.viewmodels.AuthViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class LoginFragment : Fragment() {

    private val authViewModel: AuthViewModel by viewModels()
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()

    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var googleSignInLauncher: ActivityResultLauncher<Intent>

    private var pbLoading: ProgressBar? = null
    private var btnLogin: Button? = null
    private var btnGoogleSignIn: MaterialButton? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initGoogleSignIn()
    }

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
        val tvRegisterLink = view.findViewById<View>(R.id.tv_register_link)

        btnLogin = view.findViewById(R.id.btn_login)
        btnGoogleSignIn = view.findViewById(R.id.btn_google_sign_in)
        pbLoading = view.findViewById(R.id.pb_login_loading)

        // 1. Observe Email/Password AuthState
        viewLifecycleOwner.lifecycleScope.launch {
            authViewModel.authState.collectLatest { state ->
                when (state) {
                    is AuthState.Idle -> {
                        setLoadingState(false)
                    }
                    is AuthState.Loading -> {
                        setLoadingState(true)
                        tilEmail?.error = null
                        tilPassword?.error = null
                    }
                    is AuthState.Authenticated -> {
                        setLoadingState(false)
                        if (isAdded) {
                            if (state.role.equals("admin", ignoreCase = true)) {
                                findNavController().safeNavigate(R.id.action_loginFragment_to_adminDashboardFragment)
                            } else {
                                navigateToHomeAndClearBackstack()
                            }
                        }
                    }
                    is AuthState.Error -> {
                        setLoadingState(false)
                        Snackbar.make(view, state.message, Snackbar.LENGTH_LONG).show()
                    }
                }
            }
        }

        // 2. Google Sign-In Click Listener
        btnGoogleSignIn?.setOnClickListener {
            googleSignInClient.signOut().addOnCompleteListener {
                val signInIntent = googleSignInClient.signInIntent
                googleSignInLauncher.launch(signInIntent)
            }
        }

        // 3. Top Navigation: Back and Skip as Guest
        val btnBack = view.findViewById<View>(R.id.btn_back_login)
        val btnSkip = view.findViewById<View>(R.id.btn_skip_login)

        btnBack?.setOnClickListener {
            if (!findNavController().navigateUp()) {
                navigateToHomeAndClearBackstack()
            }
        }

        btnSkip?.setOnClickListener {
            navigateToHomeAndClearBackstack()
        }

        // 4. Role Switcher
        val btnRoleUser = view.findViewById<View>(R.id.btn_role_user)
        val btnRoleAdmin = view.findViewById<View>(R.id.btn_role_admin)
        val tvSwitchToAdmin = view.findViewById<View>(R.id.tv_switch_to_admin)

        btnRoleUser?.setOnClickListener {
            btnLogin?.text = "Log In as Traveler"
        }

        btnRoleAdmin?.setOnClickListener {
            findNavController().safeNavigate(R.id.action_loginFragment_to_adminLoginFragment)
        }

        tvSwitchToAdmin?.setOnClickListener {
            findNavController().safeNavigate(R.id.action_loginFragment_to_adminLoginFragment)
        }

        // 5. Email/Password Login
        btnLogin?.setOnClickListener {
            val email = etEmail?.text?.toString()?.trim() ?: ""
            val password = etPassword?.text?.toString()?.trim() ?: ""

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

        // 6. Navigate to Forgot Password Screen
        view.findViewById<View>(R.id.tv_forgot_password)?.setOnClickListener {
            findNavController().safeNavigate(R.id.action_loginFragment_to_forgotPasswordFragment)
        }

        // 7. Navigate to Register Screen
        val layoutRegisterFooter = view.findViewById<View>(R.id.layout_register_footer)
        tvRegisterLink?.setOnClickListener {
            findNavController().safeNavigate(R.id.action_loginFragment_to_registerFragment)
        }
        layoutRegisterFooter?.setOnClickListener {
            findNavController().safeNavigate(R.id.action_loginFragment_to_registerFragment)
        }
    }

    /**
     * Initializes GoogleSignInOptions and registerForActivityResult launcher.
     */
    private fun initGoogleSignIn() {
        val webClientIdResId = resources.getIdentifier(
            "default_web_client_id",
            "string",
            requireContext().packageName
        )

        val webClientId = if (webClientIdResId != 0) {
            getString(webClientIdResId)
        } else {
            ""
        }

        val gsoBuilder = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()

        if (webClientId.isNotEmpty()) {
            gsoBuilder.requestIdToken(webClientId)
        }

        googleSignInClient = GoogleSignIn.getClient(requireActivity(), gsoBuilder.build())

        googleSignInLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                try {
                    val account = task.getResult(ApiException::class.java)
                    val idToken = account?.idToken
                    if (!idToken.isNullOrBlank()) {
                        authenticateWithFirebaseGoogle(idToken, account)
                    } else {
                        showError("Failed to obtain Google ID token. Please verify Web Client ID.")
                    }
                } catch (e: ApiException) {
                    Log.e("LoginFragment", "Google Sign-In failed: code=${e.statusCode}, msg=${e.message}", e)
                    showError("Google Sign-In canceled or failed (Code: ${e.statusCode})")
                }
            } else {
                setLoadingState(false)
            }
        }
    }

    /**
     * Authenticates the received Google account with FirebaseAuth using signInWithCredential.
     */
    private fun authenticateWithFirebaseGoogle(idToken: String, googleAccount: GoogleSignInAccount) {
        viewLifecycleOwner.lifecycleScope.launch {
            setLoadingState(true)
            try {
                val credential = GoogleAuthProvider.getCredential(idToken, null)
                val authResult = firebaseAuth.signInWithCredential(credential).await()
                val firebaseUser = authResult.user

                if (firebaseUser != null) {
                    syncGoogleUserWithDatabase(firebaseUser, googleAccount)
                } else {
                    setLoadingState(false)
                    showError("Authentication completed but user is null.")
                }
            } catch (e: Exception) {
                setLoadingState(false)
                Log.e("LoginFragment", "Firebase Google Auth error: ${e.message}", e)
                showError(e.localizedMessage ?: "Google Authentication failed.")
            }
        }
    }

    /**
     * If user is new, saves display name and email under /users/{uid} with default role="user"
     * and empty strings for address/contact.
     */
    private suspend fun syncGoogleUserWithDatabase(
        firebaseUser: FirebaseUser,
        googleAccount: GoogleSignInAccount
    ) {
        try {
            val userRef = database.reference.child("users").child(firebaseUser.uid)
            val snapshot = userRef.get().await()

            val displayName = firebaseUser.displayName
                ?: googleAccount.displayName
                ?: "Traveler"
            val email = firebaseUser.email
                ?: googleAccount.email
                ?: ""
            val photoUrl = firebaseUser.photoUrl?.toString()
                ?: googleAccount.photoUrl?.toString()
                ?: ""

            val finalRole: String

            if (!snapshot.exists()) {
                finalRole = "user"
                val newUser = User(
                    uid = firebaseUser.uid,
                    name = displayName,
                    email = email,
                    role = finalRole,
                    contactNumber = "",
                    phoneNumber = "",
                    address = "",
                    profileImageUrl = photoUrl,
                    createdAt = System.currentTimeMillis()
                )
                userRef.setValue(newUser).await()
            } else {
                finalRole = snapshot.child("role").getValue(String::class.java) ?: "user"
            }

            if (isAdded) {
                AuthManager.login(requireContext(), email, displayName)
                setLoadingState(false)
                navigateToHomeAndClearBackstack()
            }
        } catch (e: Exception) {
            setLoadingState(false)
            Log.e("LoginFragment", "Database sync error: ${e.message}", e)
            showError("Logged in, but failed to sync profile: ${e.localizedMessage}")
        }
    }

    /**
     * Clears the backstack and navigates to HomeFragment.
     */
    private fun navigateToHomeAndClearBackstack() {
        if (!isAdded) return
        val navOptions = NavOptions.Builder()
            .setPopUpTo(R.id.nav_graph, true)
            .build()
        findNavController().safeNavigate(R.id.homeFragment, null, navOptions)
    }

    private fun setLoadingState(isLoading: Boolean) {
        pbLoading?.visibility = if (isLoading) View.VISIBLE else View.GONE
        btnLogin?.isEnabled = !isLoading
        btnGoogleSignIn?.isEnabled = !isLoading
    }

    private fun showError(message: String) {
        view?.let {
            Snackbar.make(it, message, Snackbar.LENGTH_LONG).show()
        }
    }
}