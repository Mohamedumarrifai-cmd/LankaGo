package com.example.lankago.fragments

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.lankago.LankaGoApp
import com.example.lankago.R
import com.example.lankago.utils.ImagePickerHelper
import com.example.lankago.utils.safeNavigate
import com.example.lankago.utils.setOnSafeClickListener
import com.google.android.material.materialswitch.MaterialSwitch
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class ProfileFragment : Fragment() {

    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val database: FirebaseDatabase by lazy { FirebaseDatabase.getInstance() }
    private val storage: FirebaseStorage by lazy { FirebaseStorage.getInstance() }

    private val imagePickerHelper = ImagePickerHelper(this) { uri: Uri ->
        onProfilePhotoSelected(uri)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val ivAvatar = view.findViewById<ImageView>(R.id.iv_profile_avatar)
        val btnChangePhoto = view.findViewById<View>(R.id.btn_change_avatar_photo)
        val layoutAvatar = view.findViewById<View>(R.id.layout_avatar_container)
        val tvName = view.findViewById<TextView>(R.id.tv_profile_name)
        val tvEmail = view.findViewById<TextView>(R.id.tv_profile_email)

        // Load existing user info and persistent avatar
        loadUserProfile(tvName, tvEmail, ivAvatar)

        // Camera / Gallery Image Chooser Triggers
        val openPicker = View.OnClickListener {
            imagePickerHelper.showImageSourceDialog("Change Profile Picture")
        }
        layoutAvatar?.setOnClickListener(openPicker)
        btnChangePhoto?.setOnClickListener(openPicker)
        ivAvatar?.setOnClickListener(openPicker)

        // Dark Mode Toggle Switch
        val switchDarkMode = view.findViewById<MaterialSwitch>(R.id.switch_profile_dark_mode)
        val prefs = requireContext().getSharedPreferences(LankaGoApp.PREFS_SETTINGS, Context.MODE_PRIVATE)

        val isDarkMode = prefs.getBoolean(LankaGoApp.KEY_DARK_MODE, false)
        switchDarkMode?.isChecked = isDarkMode

        switchDarkMode?.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean(LankaGoApp.KEY_DARK_MODE, isChecked).apply()

            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
        }

        // Navigation shortcuts with debounce and destination guards
        view.findViewById<View>(R.id.layout_profile_trips_history)?.setOnSafeClickListener {
            findNavController().safeNavigate(R.id.action_profileFragment_to_tripHistoryFragment)
        }

        view.findViewById<View>(R.id.layout_profile_favorites)?.setOnSafeClickListener {
            findNavController().safeNavigate(R.id.action_profileFragment_to_favoritesFragment)
        }

        view.findViewById<View>(R.id.layout_profile_rentals)?.setOnSafeClickListener {
            findNavController().safeNavigate(R.id.action_profileFragment_to_rentalListFragment)
        }

        view.findViewById<View>(R.id.layout_profile_offline)?.setOnSafeClickListener {
            findNavController().safeNavigate(R.id.action_profileFragment_to_offlineSavedFragment)
        }

        view.findViewById<View>(R.id.layout_profile_reviews)?.setOnSafeClickListener {
            findNavController().safeNavigate(R.id.action_profileFragment_to_reviewsFragment)
        }

        view.findViewById<View>(R.id.layout_profile_notifications)?.setOnSafeClickListener {
            findNavController().safeNavigate(R.id.action_profileFragment_to_notificationsFragment)
        }

        view.findViewById<View>(R.id.layout_profile_settings)?.setOnSafeClickListener {
            findNavController().safeNavigate(R.id.action_profileFragment_to_settingsFragment)
        }

        // Logout
        view.findViewById<View>(R.id.btn_profile_logout)?.setOnSafeClickListener {
            auth.signOut()
            com.example.lankago.utils.AuthManager.logout(requireContext())
            ivAvatar?.setImageResource(R.drawable.ic_admin_profile)
            Toast.makeText(requireContext(), "Logged out successfully", Toast.LENGTH_SHORT).show()
            findNavController().safeNavigate(R.id.action_profileFragment_to_loginFragment)
        }
    }

    private fun loadUserProfile(tvName: TextView?, tvEmail: TextView?, ivAvatar: ImageView?) {
        val context = context ?: return
        val currentUser = auth.currentUser

        // 1. Load avatar via ProfileAvatarManager (Instant local cache + Firebase RTDB Base64 / Cloud)
        com.example.lankago.utils.ProfileAvatarManager.loadAvatar(this, ivAvatar)

        if (currentUser == null) {
            tvName?.text = com.example.lankago.utils.AuthManager.getLoggedInName(context)
            tvEmail?.text = com.example.lankago.utils.AuthManager.getLoggedInEmail(context)
            return
        }

        val uid = currentUser.uid
        tvEmail?.text = currentUser.email ?: "traveler@lankago.lk"

        // 2. Fetch User display name from Firebase Realtime Database
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val snapshot = database.reference.child("users").child(uid).get().await()
                val name = snapshot.child("name").getValue(String::class.java)

                if (!name.isNullOrBlank()) {
                    tvName?.text = name
                } else {
                    tvName?.text = currentUser.displayName ?: "LankaGo Explorer"
                }
            } catch (_: Exception) {
                tvName?.text = currentUser.displayName ?: "LankaGo Explorer"
            }
        }
    }

    private fun onProfilePhotoSelected(uri: Uri) {
        val view = view ?: return
        val ivAvatar = view.findViewById<ImageView>(R.id.iv_profile_avatar) ?: return

        com.example.lankago.utils.ProfileAvatarManager.saveAvatar(this, uri, ivAvatar) { success, msg ->
            if (isAdded) {
                Snackbar.make(view, if (success) "✓ Profile photo saved for your account!" else msg, Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    companion object {
        const val PREFS_USER_PROFILE = "lankago_user_profile"
        const val KEY_AVATAR_PATH = "key_avatar_local_path"
        const val KEY_USER_NAME = "key_user_name"
        const val KEY_USER_EMAIL = "key_user_email"
    }
}
