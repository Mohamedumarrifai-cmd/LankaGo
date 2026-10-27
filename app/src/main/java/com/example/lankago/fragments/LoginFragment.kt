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
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
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
import com.google.android.gms.common.api.CommonStatusCodes
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

    private var googleSignInClient: GoogleSignInClient? = null
    private lateinit var googleSignInLauncher: ActivityResultLauncher<Intent>

    private var pbLoading: ProgressBar? = null
    private var btnLogin: Button? = null
    private var btnGoogleSignIn: MaterialButton? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initGoogleSignInLauncher()
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
                        showError(state.message)
                    }
                }
            }
        }

        // 2. Google Sign-In Click Listener
        btnGoogleSignIn?.setOnClickListener {
            launchGoogleSignInFlow()
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
     * Registers ActivityResultLauncher for Google Sign-In.
     */
    private fun initGoogleSignInLauncher() {
        googleSignInLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            setLoadingState(false)
            if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                try {
                    val account = task.getResult(ApiException::class.java)
                    val idToken = account?.idToken
                    Log.d("LoginFragment", "Google Sign-In successful for email: ${account?.email}, idToken present: ${!idToken.isNullOrBlank()}")

                    if (!idToken.isNullOrBlank()) {
                        authenticateWithFirebaseGoogle(idToken, account)
                    } else {
                        val errorMsg = "Google Account connected (${account?.email}), but no ID Token was returned. Your Firebase Web Client ID must be added in strings.xml."
                        Log.e("LoginFragment", errorMsg)
                        showDialog("Configuration Notice", errorMsg)
                    }
                } catch (e: ApiException) {
                    val statusCode = e.statusCode
                    Log.e("LoginFragment", "Google Sign-In ApiException: code=$statusCode, msg=${e.message}", e)
                    val userFriendlyMsg = when (statusCode) {
                        CommonStatusCodes.DEVELOPER_ERROR -> {
                            "Google Sign-In Error (Code 10: DEVELOPER_ERROR).\n\nPlease add your SHA-1 fingerprint (from gradlew signingReport) to your Firebase Console under Project Settings -> Android App."
                        }
                        CommonStatusCodes.NETWORK_ERROR -> {
                            "Network connection failed. Please check your internet connection."
                        }
                        12500 -> {
                            "Google Play Services configuration error (Code 12500). Please verify Google Sign-in is enabled in Firebase Console."
                        }
                        12501 -> {
                            "Sign-in was cancelled."
                        }
                        else -> {
                            "Google Sign-In failed (Status Code: $statusCode): ${e.localizedMessage}"
                        }
                    }
                    if (statusCode != 12501) {
                        showDialog("Google Sign-In Failed", userFriendlyMsg)
                    }
                }
            } else {
                Log.w("LoginFragment", "Google Sign-In canceled or no data: resultCode=${result.resultCode}")
            }
        }
    }

    /**
     * Resolves the Web Client ID and launches the Google Sign-In Intent.
     */
    private fun launchGoogleSignInFlow() {
        val webClientId = getWebClientId()

        val gsoBuilder = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()

        if (webClientId.isNotBlank()) {
            gsoBuilder.requestIdToken(webClientId)
        } else {
            Log.w("LoginFragment", "default_web_client_id is not yet set in strings.xml or google-services.json")
        }

        val gso = gsoBuilder.build()
        val client = GoogleSignIn.getClient(requireActivity(), gso)
        googleSignInClient = client

        setLoadingState(true)
        // Clear cached credentials so account chooser always pops up
        client.signOut().addOnCompleteListener {
            val signInIntent = client.signInIntent
            googleSignInLauncher.launch(signInIntent)
        }
    }

    /**
     * Resolves the Web Client ID from auto-generated resource or strings.xml.
     */
    private fun getWebClientId(): String {
        val autoResId = resources.getIdentifier("default_web_client_id", "string", requireContext().packageName)
        if (autoResId != 0) {
            val id = getString(autoResId)
            if (id.isNotBlank()) return id
        }

        val manualResId = resources.getIdentifier("google_web_client_id", "string", requireContext().packageName)
        if (manualResId != 0) {
            val id = getString(manualResId)
            if (id.isNotBlank() && !id.contains("YOUR_")) return id
        }
        return ""
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
                    showError("Authentication finished, but Firebase user is null.")
                }
            } catch (e: Exception) {
                setLoadingState(false)
                Log.e("LoginFragment", "Firebase Google Auth failed: ${e.message}", e)
                showDialog("Authentication Failed", e.localizedMessage ?: "Failed to authenticate with Firebase.")
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
        val displayName = firebaseUser.displayName
            ?: googleAccount.displayName
            ?: "Traveler"
        val email = firebaseUser.email
            ?: googleAccount.email
            ?: ""
        val photoUrl = firebaseUser.photoUrl?.toString()
            ?: googleAccount.photoUrl?.toString()
            ?: ""

        var finalRole = "user"

        try {
            val userRef = database.reference.child("users").child(firebaseUser.uid)
            val snapshot = userRef.get().await()

            if (!snapshot.exists()) {
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
        } catch (dbError: Exception) {
            // Log database sync failure but continue login so traveler is not blocked
            Log.e("LoginFragment", "Database write exception: ${dbError.message}", dbError)
        }

        if (isAdded) {
            AuthManager.login(requireContext(), email, displayName)
            setLoadingState(false)
            Toast.makeText(requireContext(), "Welcome back, $displayName!", Toast.LENGTH_SHORT).show()
            navigateToHomeAndClearBackstack()
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
        if (!isAdded) return
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
    }

    private fun showDialog(title: String, message: String) {
        if (!isAdded) return
        AlertDialog.Builder(requireContext())
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show()
    }
}