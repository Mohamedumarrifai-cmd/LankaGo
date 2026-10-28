package com.example.lankago.fragments

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.lankago.R
import com.example.lankago.utils.AuthManager
import com.example.lankago.utils.safeNavigate
import com.example.lankago.viewmodels.AuthState
import com.example.lankago.viewmodels.AuthViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.auth.api.signin.GoogleSignInStatusCodes
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class LoginFragment : Fragment() {

    companion object {
        private const val TAG = "LoginFragment"
    }

    private val authViewModel: AuthViewModel by viewModels()

    private var pbLoading: ProgressBar? = null
    private var btnLogin: Button? = null
    private var btnGoogleSignIn: MaterialButton? = null

    private lateinit var googleSignInClient: GoogleSignInClient

//     Modern Activity Result API launcher for Google Sign-In.
//     Catches and decodes all status codes so issues (like missing SHA-1 or Web Client ID) are never silent.

    private val googleSignInLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            Log.d(TAG, "Google Sign-In ActivityResult: resultCode=${result.resultCode}")     //for using google sign in

            if (result.resultCode == Activity.RESULT_OK) {
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                try {
                    val account: GoogleSignInAccount = task.getResult(ApiException::class.java)
                    val idToken = account.idToken
                    Log.d(
                        TAG,
                        "Google Sign-In successful. email=${account.email}, name=${account.displayName}, hasToken=${!idToken.isNullOrEmpty()}"
                    )

                    if (!idToken.isNullOrEmpty()) {
                        firebaseAuthWithGoogle(idToken, account.displayName, account.email)
                    } else {
                        setLoading(false)
                        val errorMsg = "Google Sign-In returned null ID token. Verify Web Client ID configuration."
                        Log.e(TAG, errorMsg)
                        showError(errorMsg)
                    }
                } catch (e: ApiException) {
                    setLoading(false)
                    val detailedMessage = when (e.statusCode) {
                        GoogleSignInStatusCodes.SIGN_IN_CANCELLED ->
                            "Sign-in cancelled by user."
                        CommonStatusCodes.DEVELOPER_ERROR ->
                            "Developer Error (Code 10): Ensure debug SHA-1 fingerprint is registered in Firebase Console and google-services.json is refreshed."
                        CommonStatusCodes.NETWORK_ERROR ->
                            "Network Error (Code 7): Please check your internet connection and try again."
                        GoogleSignInStatusCodes.SIGN_IN_FAILED ->
                            "Sign-In Failed (Code 12500): Google Play Services error or SHA-1 mismatch."
                        else ->
                            "Google Sign-In error (Status Code ${e.statusCode}): ${e.localizedMessage ?: "Unknown error"}"
                    }
                    Log.e(TAG, "Google Sign-In ApiException: statusCode=${e.statusCode}, message=${e.message}", e)
                    showError(detailedMessage)
                } catch (e: Exception) {
                    setLoading(false)
                    Log.e(TAG, "Unexpected error parsing Google Sign-In result", e)
                    showError("Unexpected error: ${e.localizedMessage}")
                }
            } else {
                setLoading(false)
                Log.w(TAG, "Google Sign-In activity returned non-OK result: ${result.resultCode}")
                if (result.resultCode != Activity.RESULT_CANCELED) {
                    showError("Google Sign-In failed with result code ${result.resultCode}")
                }
            }
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

        val tilEmail = view.findViewById<TextInputLayout>(R.id.til_email)  //email eka wate tiyana input layout eka
        val etEmail = view.findViewById<EditText>(R.id.et_email)    //email eka type karana edit text eka
        val tilPassword = view.findViewById<TextInputLayout>(R.id.til_password)
        val etPassword = view.findViewById<EditText>(R.id.et_password)
        btnLogin = view.findViewById(R.id.btn_login)
        btnGoogleSignIn = view.findViewById(R.id.btn_google_sign_in)
        pbLoading = view.findViewById(R.id.pb_login_loading)
        val tvRegisterLink = view.findViewById<View>(R.id.tv_register_link)

        // Initialize Google Sign-In Client
        setupGoogleSignIn()

        // Handle "Continue with Google" click
        btnGoogleSignIn?.setOnClickListener {
            handleGoogleSignInClick(view)
        }

        // Observe Authentication State
        viewLifecycleOwner.lifecycleScope.launch {
            authViewModel.authState.collectLatest { state ->
                when (state) {
                    is AuthState.Idle -> {
                        setLoading(false)
                    }
                    is AuthState.Loading -> {
                        setLoading(true)
                        tilEmail?.error = null
                        tilPassword?.error = null
                    }
                    is AuthState.Authenticated -> {
                        setLoading(false)
                        if (isAdded) {
                            if (state.role.equals("admin", ignoreCase = true)) {
                                findNavController().safeNavigate(R.id.action_loginFragment_to_adminDashboardFragment)
                            } else {
                                findNavController().safeNavigate(R.id.action_loginFragment_to_homeFragment)
                            }
                        }       //check the user role(admin or user)
                    }
                    is AuthState.Error -> {
                        setLoading(false)
                        showError(state.message)
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
            btnLogin?.text = "Log In as Traveler"
        }

        btnRoleAdmin?.setOnClickListener {
            findNavController().safeNavigate(R.id.action_loginFragment_to_adminLoginFragment)
        }

        tvSwitchToAdmin?.setOnClickListener {
            findNavController().safeNavigate(R.id.action_loginFragment_to_adminLoginFragment)
        }

        // 3. Trigger Email/Password Login
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

        // 4. Navigate to Forgot Password Screen
        view.findViewById<View>(R.id.tv_forgot_password)?.setOnClickListener {
            findNavController().safeNavigate(R.id.action_loginFragment_to_forgotPasswordFragment)
        }

        // 5. Navigate to Register Screen
        val layoutRegisterFooter = view.findViewById<View>(R.id.layout_register_footer)
        tvRegisterLink?.setOnClickListener {
            findNavController().safeNavigate(R.id.action_loginFragment_to_registerFragment)
        }
        layoutRegisterFooter?.setOnClickListener {
            findNavController().safeNavigate(R.id.action_loginFragment_to_registerFragment)
        }
    }


//     Initializes GoogleSignInOptions with default_web_client_id.

    private fun setupGoogleSignIn() {
        val webClientId = getString(R.string.default_web_client_id)
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(webClientId)
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)
    }


//      Clears cached sign-in state to guarantee the account chooser dialog is always presented.

    private fun handleGoogleSignInClick(view: View) {
        val webClientId = getString(R.string.default_web_client_id)
        if (webClientId.isBlank() || webClientId == "PASTE_YOUR_WEB_CLIENT_ID_HERE") {
            val msg = "Google Sign-In is not configured yet! Please add SHA-1 in Firebase and add default_web_client_id."
            Log.e(TAG, msg)
            Snackbar.make(view, msg, Snackbar.LENGTH_INDEFINITE)
                .setAction("DISMISS") {}
                .show()
            return
        }

        setLoading(true)
        googleSignInClient.signOut().addOnCompleteListener {
            val signInIntent = googleSignInClient.signInIntent
            googleSignInLauncher.launch(signInIntent)
        }
    }


//      Authenticates the received Google ID Token with Firebase Authentication.
     //meken tamai authentication success kiyala show karanne
    private fun firebaseAuthWithGoogle(idToken: String, displayName: String?, email: String?) {
        setLoading(true)
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        FirebaseAuth.getInstance().signInWithCredential(credential)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    val firebaseUser = FirebaseAuth.getInstance().currentUser
                    val uid = firebaseUser?.uid
                    if (uid != null) {
                        val resolvedName = displayName ?: firebaseUser.displayName ?: "Traveler"
                        val resolvedEmail = email ?: firebaseUser.email ?: ""
                        saveGoogleUserToDatabase(uid, resolvedName, resolvedEmail)
                    } else {
                        setLoading(false)
                        showError("Firebase user is null after authentication.")
                    }
                } else {
                    setLoading(false)
                    val error = task.exception?.localizedMessage ?: "Firebase Google authentication failed."
                    Log.e(TAG, "Firebase signInWithCredential failed", task.exception)
                    showError(error)
                }
            }
    }


//     Saves user details in Firebase Realtime Database at `/users/{uid}`.
//     Preserves existing role/contact if this is a returning user, otherwise initializes with user role.
    private fun saveGoogleUserToDatabase(uid: String, name: String, email: String) {
        val databaseRef = FirebaseDatabase.getInstance().reference.child("users").child(uid)

        databaseRef.get().addOnSuccessListener { snapshot ->
            val existingRole = snapshot.child("role").getValue(String::class.java) ?: "user"
            val existingContact = snapshot.child("contactNumber").getValue(String::class.java)
                ?: snapshot.child("phoneNumber").getValue(String::class.java)
                ?: ""
            val existingAddress = snapshot.child("address").getValue(String::class.java) ?: ""

            val userData = hashMapOf<String, Any>(
                "uid" to uid,
                "name" to name,
                "email" to email,
                "role" to existingRole,
                "contactNumber" to existingContact,
                "phoneNumber" to existingContact,
                "address" to existingAddress,
                "updatedAt" to System.currentTimeMillis()
            )

            if (!snapshot.exists()) {
                userData["createdAt"] = System.currentTimeMillis()
            }

            databaseRef.updateChildren(userData).addOnCompleteListener { saveTask ->
                setLoading(false)
                if (!saveTask.isSuccessful) {
                    Log.w(TAG, "Failed to update RTDB /users node", saveTask.exception)
                }
                // Persist session locally
                context?.let { ctx ->
                    AuthManager.login(ctx, email, name)
                }
                navigateToHome(existingRole)
            }
        }.addOnFailureListener { e ->
            Log.w(TAG, "Failed reading /users/$uid, fallback to direct write", e)
            val defaultUser = hashMapOf<String, Any>(
                "uid" to uid,
                "name" to name,
                "email" to email,
                "role" to "user",
                "contactNumber" to "",
                "phoneNumber" to "",
                "address" to "",
                "createdAt" to System.currentTimeMillis()
            )
            databaseRef.setValue(defaultUser).addOnCompleteListener {
                setLoading(false)
                context?.let { ctx ->
                    AuthManager.login(ctx, email, name)
                }
                navigateToHome("user")
            }
        }
    }

    /**
     * Clears backstack and navigates to destination based on user role.
     */
    private fun navigateToHome(role: String) {
        if (!isAdded) return
        if (role.equals("admin", ignoreCase = true)) {
            findNavController().safeNavigate(R.id.action_loginFragment_to_adminDashboardFragment)
        } else {
            findNavController().safeNavigate(R.id.action_loginFragment_to_homeFragment)
        }
    }

    private fun setLoading(isLoading: Boolean) {
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