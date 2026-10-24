package com.example.lankago.fragments

import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.lankago.LankaGoApp
import com.example.lankago.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.switchmaterial.SwitchMaterial
import com.example.lankago.utils.ImagePickerHelper
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * SettingsFragment supporting complete, production-grade preferences management
 * and seamless A-to-Z navigation across system settings, map routes, offline saved
 * itineraries, notifications, support hotline, and storage management.
 */
class SettingsFragment : Fragment() {

    private val imagePickerHelper = ImagePickerHelper(this) { uri: Uri ->
        val ivAvatar = view?.findViewById<ImageView>(R.id.iv_settings_avatar)
        com.example.lankago.utils.ProfileAvatarManager.saveAvatar(this, uri, ivAvatar) { success, msg ->
            if (isAdded) {
                Toast.makeText(requireContext(), if (success) "✓ Profile photo updated for your account!" else msg, Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val prefs = requireContext().getSharedPreferences(LankaGoApp.PREFS_SETTINGS, Context.MODE_PRIVATE)

        // 0. User Profile Header Card & Image Picker
        setupProfileHeader(view)

        // 1. Back Navigation
        view.findViewById<ImageView>(R.id.btn_back_settings)?.setOnClickListener {
            findNavController().navigateUp()
        }

        // 2. Share App Action
        view.findViewById<ImageView>(R.id.btn_share_settings)?.setOnClickListener {
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, "LankaGo - Smart Travel Companion")
                putExtra(
                    Intent.EXTRA_TEXT,
                    "Discover the wonders of Sri Lanka with LankaGo! Explore destinations, book rentals, and experience local culture. Download now: https://lankago.lk"
                )
            }
            startActivity(Intent.createChooser(shareIntent, "Share LankaGo with Friends"))
        }

        // 3. Dark Mode Toggle
        val switchDarkMode = view.findViewById<SwitchMaterial>(R.id.switch_dark_mode)
        val isDarkMode = prefs.getBoolean(LankaGoApp.KEY_DARK_MODE, false)
        switchDarkMode?.isChecked = isDarkMode

        switchDarkMode?.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean(LankaGoApp.KEY_DARK_MODE, isChecked).apply()
            val newMode = if (isChecked) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
            if (AppCompatDelegate.getDefaultNightMode() != newMode) {
                AppCompatDelegate.setDefaultNightMode(newMode)
            }
            val msg = if (isChecked) "🌙 Dark Mode Enabled" else "☀️ Light Mode Enabled"
            Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
        }

        view.findViewById<View>(R.id.row_setting_dark_mode)?.setOnClickListener {
            switchDarkMode?.let { switch ->
                switch.isChecked = !switch.isChecked
            }
        }

        // 4. Push Notifications Toggle & Navigation
        val switchNotifications = view.findViewById<SwitchMaterial>(R.id.switch_notifications)
        val isNotificationsEnabled = prefs.getBoolean("notifications_enabled", true)
        switchNotifications?.isChecked = isNotificationsEnabled

        switchNotifications?.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("notifications_enabled", isChecked).apply()
            val msg = if (isChecked) "🔔 Notifications Enabled" else "🔕 Notifications Muted"
            Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
        }

        view.findViewById<View>(R.id.row_setting_notifications)?.setOnClickListener {
            try {
                findNavController().navigate(R.id.action_settingsFragment_to_notificationsFragment)
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Opening notifications...", Toast.LENGTH_SHORT).show()
            }
        }

        // 5. GPS & Location System Settings
        val tvLocStatus = view.findViewById<TextView>(R.id.tv_loc_status)
        val locManager = requireContext().getSystemService(Context.LOCATION_SERVICE) as? LocationManager
        val isGpsEnabled = locManager?.isProviderEnabled(LocationManager.GPS_PROVIDER) == true
        tvLocStatus?.text = if (isGpsEnabled) "GPS Enabled" else "GPS Disabled"
        tvLocStatus?.setTextColor(if (isGpsEnabled) 0xFF10B981.toInt() else 0xFFEF4444.toInt())

        view.findViewById<View>(R.id.row_setting_location)?.setOnClickListener {
            try {
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "GPS Location Services are ${if (isGpsEnabled) "Active" else "Disabled"}", Toast.LENGTH_SHORT).show()
            }
        }

        // 6. Wi-Fi & Network Settings
        val tvWifiStatus = view.findViewById<TextView>(R.id.tv_wifi_status)
        val isNetworkConnected = checkNetworkConnected()
        tvWifiStatus?.text = if (isNetworkConnected) "Connected" else "Offline"
        tvWifiStatus?.setTextColor(if (isNetworkConnected) 0xFF10B981.toInt() else 0xFF8A8F98.toInt())

        view.findViewById<View>(R.id.row_setting_wifi)?.setOnClickListener {
            try {
                val intent = Intent(Settings.ACTION_WIFI_SETTINGS)
                startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Network status: ${if (isNetworkConnected) "Connected" else "Offline"}", Toast.LENGTH_SHORT).show()
            }
        }

        // 7. Live Route & Maps Tracker Navigation
        view.findViewById<View>(R.id.row_setting_location_services)?.setOnClickListener {
            try {
                findNavController().navigate(R.id.action_settingsFragment_to_mapRouteFragment)
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Opening Live Map...", Toast.LENGTH_SHORT).show()
            }
        }

        // 7b. Browse Vehicle & Bike Rentals Navigation
        view.findViewById<View>(R.id.row_setting_rentals)?.setOnClickListener {
            try {
                findNavController().navigate(R.id.action_settingsFragment_to_rentalsTransportFragment)
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Opening Rentals Hub...", Toast.LENGTH_SHORT).show()
            }
        }

        // 8. Offline Saved Guides Navigation
        view.findViewById<View>(R.id.row_setting_offline)?.setOnClickListener {
            try {
                findNavController().navigate(R.id.action_settingsFragment_to_offlineSavedFragment)
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Opening Offline Guides...", Toast.LENGTH_SHORT).show()
            }
        }

        // 9. Help & Emergency Support Dialog
        view.findViewById<View>(R.id.row_setting_help)?.setOnClickListener {
            showSupportDialog()
        }

        // 10. Clear Storage Cache
        view.findViewById<View>(R.id.row_setting_clear_cache)?.setOnClickListener {
            showClearCacheDialog()
        }

        // 11. Privacy Policy & Terms
        view.findViewById<View>(R.id.row_setting_privacy)?.setOnClickListener {
            showPrivacyDialog()
        }

        // 12. About LankaGo Dialog
        view.findViewById<View>(R.id.row_setting_about)?.setOnClickListener {
            showAboutDialog()
        }
    }

    private fun checkNetworkConnected(): Boolean {
        val connectivityManager = requireContext().getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            ?: return false
        val activeNetwork = connectivityManager.activeNetwork ?: return false
        val caps = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    private fun showSupportDialog() {
        val options = arrayOf(
            "📞 Call 24/7 Hotline (+94 11 234 5678)",
            "💬 WhatsApp Support (+94 77 123 4567)",
            "✉️ Email LankaGo (support@lankago.lk)",
            "🚨 Sri Lanka Tourist Police (1912)"
        )

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("LankaGo Help & Emergency Support")
            .setIcon(R.drawable.ic_bell)
            .setItems(options) { _, which ->
                when (which) {
                    0 -> {
                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:+94112345678"))
                        startActivity(intent)
                    }
                    1 -> {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/94771234567"))
                        startActivity(intent)
                    }
                    2 -> {
                        val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:support@lankago.lk")).apply {
                            putExtra(Intent.EXTRA_SUBJECT, "LankaGo App Support Request")
                        }
                        startActivity(intent)
                    }
                    3 -> {
                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:1912"))
                        startActivity(intent)
                    }
                }
            }
            .setPositiveButton("Close", null)
            .show()
    }

    private fun showClearCacheDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Clear App Cache")
            .setMessage("This will clear temporary downloaded images and cached travel files. Your saved trips and favorites will not be deleted.")
            .setPositiveButton("Clear Now") { _, _ ->
                lifecycleScope.launch(Dispatchers.IO) {
                    try {
                        requireContext().cacheDir?.deleteRecursively()
                        Glide.get(requireContext()).clearDiskCache()
                    } catch (_: Exception) {}
                    withContext(Dispatchers.Main) {
                        Toast.makeText(requireContext(), "🧹 App storage cache cleared successfully!", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showPrivacyDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Privacy & Terms")
            .setMessage(
                "LankaGo Privacy Policy & Safety Commitment:\n\n" +
                "• Data Protection: Your profile and booking information are encrypted and never sold to third parties.\n\n" +
                "• Location Usage: GPS is only accessed during active travel routing to calculate accurate ETAs and nearby heritage sites.\n\n" +
                "• Verified Vendors: All tour operators, vehicle rentals, and artisan craft shops are authenticated by LankaGo administrators.\n\n" +
                "© 2026 LankaGo Travel Technologies. All rights reserved."
            )
            .setPositiveButton("Got It", null)
            .show()
    }

    private fun showAboutDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("About LankaGo")
            .setMessage(
                "LankaGo v1.0.0 (Build 2026)\n" +
                "Smart Travel Companion for Sri Lanka 🇱🇰\n\n" +
                "Developed with love to empower travelers, support local craftsmen, and showcase the timeless heritage of Sri Lanka from Sigiriya to Mirissa.\n\n" +
                "Official Website: https://lankago.lk"
            )
            .setPositiveButton("Awesome", null)
            .show()
    }

    private fun setupProfileHeader(view: View) {
        val ivAvatar = view.findViewById<ImageView>(R.id.iv_settings_avatar)
        val btnChangeAvatar = view.findViewById<View>(R.id.btn_change_settings_avatar)
        val layoutAvatar = view.findViewById<View>(R.id.layout_settings_avatar)
        val tvName = view.findViewById<TextView>(R.id.tv_settings_user_name)
        val tvEmail = view.findViewById<TextView>(R.id.tv_settings_user_email)
        val layoutUserInfo = view.findViewById<View>(R.id.layout_settings_user_info)

        val context = context ?: return

        // 1. Load user avatar via ProfileAvatarManager (user-specific local file + Firebase RTDB)
        com.example.lankago.utils.ProfileAvatarManager.loadAvatar(this, ivAvatar)

        // 2. Load user name & email
        val authUser = FirebaseAuth.getInstance().currentUser
        if (authUser != null) {
            tvEmail?.text = authUser.email ?: "traveler@lankago.lk"
            viewLifecycleOwner.lifecycleScope.launch {
                try {
                    val snapshot = FirebaseDatabase.getInstance().reference.child("users").child(authUser.uid).get().await()
                    val name = snapshot.child("name").getValue(String::class.java)
                    tvName?.text = if (!name.isNullOrBlank()) name else (authUser.displayName ?: "LankaGo Traveler")
                } catch (_: Exception) {
                    tvName?.text = authUser.displayName ?: "LankaGo Traveler"
                }
            }
        } else {
            tvName?.text = com.example.lankago.utils.AuthManager.getLoggedInName(context)
            tvEmail?.text = com.example.lankago.utils.AuthManager.getLoggedInEmail(context)
        }

        // 3. Avatar click opens camera/gallery chooser
        val openPicker = View.OnClickListener {
            imagePickerHelper.showImageSourceDialog("Change Profile Picture")
        }
        layoutAvatar?.setOnClickListener(openPicker)
        btnChangeAvatar?.setOnClickListener(openPicker)
        ivAvatar?.setOnClickListener(openPicker)

        // 4. Clicking user info row opens ProfileFragment
        val openProfile = View.OnClickListener {
            try {
                findNavController().navigate(R.id.action_settingsFragment_to_profileFragment)
            } catch (_: Exception) {
                findNavController().navigateUp()
            }
        }
        layoutUserInfo?.setOnClickListener(openProfile)
    }
}
