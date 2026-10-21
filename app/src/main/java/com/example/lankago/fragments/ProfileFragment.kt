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
import com.google.android.material.materialswitch.MaterialSwitch
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID

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

        // Load existing user info
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

        // Navigation shortcuts
        view.findViewById<View>(R.id.layout_profile_favorites)?.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_favoritesFragment)
        }

        view.findViewById<View>(R.id.layout_profile_rentals)?.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_rentalListFragment)
        }

        view.findViewById<View>(R.id.layout_profile_offline)?.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_offlineSavedFragment)
        }

        view.findViewById<View>(R.id.layout_profile_reviews)?.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_reviewsFragment)
        }

        view.findViewById<View>(R.id.layout_profile_notifications)?.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_notificationsFragment)
        }

        view.findViewById<View>(R.id.layout_profile_settings)?.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_settingsFragment)
        }

        // Logout
        view.findViewById<View>(R.id.btn_profile_logout)?.setOnClickListener {
            auth.signOut()
            Toast.makeText(requireContext(), "Logged out successfully", Toast.LENGTH_SHORT).show()
            findNavController().navigate(R.id.action_profileFragment_to_loginFragment)
        }
    }

    private fun loadUserProfile(tvName: TextView?, tvEmail: TextView?, ivAvatar: ImageView?) {
        val currentUser = auth.currentUser ?: return
        val uid = currentUser.uid

        tvEmail?.text = currentUser.email ?: "traveler@lankago.lk"

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val snapshot = database.reference.child("users").child(uid).get().await()
                val name = snapshot.child("name").getValue(String::class.java)
                val profileUrl = snapshot.child("profileImageUrl").getValue(String::class.java)

                if (!name.isNullOrBlank()) {
                    tvName?.text = name
                } else {
                    tvName?.text = currentUser.displayName ?: "LankaGo Explorer"
                }

                if (!profileUrl.isNullOrBlank() && ivAvatar != null) {
                    ivAvatar.imageTintList = null
                    Glide.with(this@ProfileFragment)
                        .load(profileUrl)
                        .circleCrop()
                        .into(ivAvatar)
                }
            } catch (_: Exception) {
                // Keep default placeholder
            }
        }
    }

    private fun onProfilePhotoSelected(uri: Uri) {
        val view = view ?: return
        val ivAvatar = view.findViewById<ImageView>(R.id.iv_profile_avatar) ?: return

        // 1. Instantly display locally with circular crop
        ivAvatar.imageTintList = null
        Glide.with(this)
            .load(uri)
            .circleCrop()
            .into(ivAvatar)

        val currentUser = auth.currentUser
        if (currentUser == null) {
            Snackbar.make(view, "Profile photo updated locally", Snackbar.LENGTH_SHORT).show()
            return
        }

        val uid = currentUser.uid

        // 2. Upload to Firebase Storage in background and update Realtime Database
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val storageRef = storage.reference.child("profile_pictures/$uid.jpg")
                storageRef.putFile(uri).await()
                val downloadUrl = storageRef.downloadUrl.await().toString()

                database.reference
                    .child("users")
                    .child(uid)
                    .child("profileImageUrl")
                    .setValue(downloadUrl)
                    .await()

                Snackbar.make(view, "Profile photo updated successfully!", Snackbar.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Snackbar.make(view, "Photo updated locally. (Upload failed: ${e.localizedMessage})", Snackbar.LENGTH_LONG).show()
            }
        }
    }
}
