package com.example.lankago

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.switchmaterial.SwitchMaterial
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }
        window.statusBarColor = Color.parseColor("#FFFFFF")

        setContentView(R.layout.settings)

        val prefs = getSharedPreferences(LankaGoApp.PREFS_SETTINGS, Context.MODE_PRIVATE)

        // 1. Back button
        findViewById<ImageView>(R.id.btn_back_settings)?.setOnClickListener {
            finish()
        }

        // 2. Share
        findViewById<ImageView>(R.id.btn_share_settings)?.setOnClickListener {
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, "LankaGo - Smart Travel Companion")
                putExtra(
                    Intent.EXTRA_TEXT,
                    "Discover the wonders of Sri Lanka with LankaGo! Download now: https://lankago.lk"
                )
            }
            startActivity(Intent.createChooser(shareIntent, "Share LankaGo"))
        }

        // 3. Dark Mode
        val switchDarkMode = findViewById<SwitchMaterial>(R.id.switch_dark_mode)
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

        // 4. Notifications
        val switchNotifications = findViewById<SwitchMaterial>(R.id.switch_notifications)
        val isNotificationsEnabled = prefs.getBoolean("notifications_enabled", true)
        switchNotifications?.isChecked = isNotificationsEnabled

        switchNotifications?.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("notifications_enabled", isChecked).apply()
            val msg = if (isChecked) "🔔 Notifications Enabled" else "🔕 Notifications Muted"
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
        }

        findViewById<View>(R.id.row_setting_notifications)?.setOnClickListener {
            val intent = Intent(this, NotificationsActivity::class.java)
            startActivity(intent)
        }

        // 5. GPS & Location
        val tvLocStatus = findViewById<TextView>(R.id.tv_loc_status)
        val locManager = getSystemService(Context.LOCATION_SERVICE) as? LocationManager
        val isGpsEnabled = locManager?.isProviderEnabled(LocationManager.GPS_PROVIDER) == true
        tvLocStatus?.text = if (isGpsEnabled) "GPS Enabled" else "GPS Disabled"
        tvLocStatus?.setTextColor(if (isGpsEnabled) 0xFF10B981.toInt() else 0xFFEF4444.toInt())

        findViewById<View>(R.id.row_setting_location)?.setOnClickListener {
            try {
                startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            } catch (e: Exception) {
                Toast.makeText(this, "GPS Location Settings", Toast.LENGTH_SHORT).show()
            }
        }

        // 6. Wi-Fi
        val tvWifiStatus = findViewById<TextView>(R.id.tv_wifi_status)
        val connManager = getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        val activeNet = connManager?.activeNetwork
        val caps = connManager?.getNetworkCapabilities(activeNet)
        val isConnected = caps?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
        tvWifiStatus?.text = if (isConnected) "Connected" else "Offline"
        tvWifiStatus?.setTextColor(if (isConnected) 0xFF10B981.toInt() else 0xFF8A8F98.toInt())

        findViewById<View>(R.id.row_setting_wifi)?.setOnClickListener {
            try {
                startActivity(Intent(Settings.ACTION_WIFI_SETTINGS))
            } catch (e: Exception) {
                Toast.makeText(this, "Wi-Fi Settings", Toast.LENGTH_SHORT).show()
            }
        }

        // 7. Maps
        findViewById<View>(R.id.row_setting_location_services)?.setOnClickListener {
            startActivity(Intent(this, MapRouteActivity::class.java))
        }

        // 8. Offline
        findViewById<View>(R.id.row_setting_offline)?.setOnClickListener {
            startActivity(Intent(this, OfflineSavedActivity::class.java))
        }

        // 9. Help Support
        findViewById<View>(R.id.row_setting_help)?.setOnClickListener {
            val options = arrayOf(
                "📞 Call 24/7 Hotline (+94 11 234 5678)",
                "💬 WhatsApp Support (+94 77 123 4567)",
                "✉️ Email LankaGo (support@lankago.lk)",
                "🚨 Sri Lanka Tourist Police (1912)"
            )
            MaterialAlertDialogBuilder(this)
                .setTitle("LankaGo Help & Emergency Support")
                .setIcon(R.drawable.ic_bell)
                .setItems(options) { _, which ->
                    when (which) {
                        0 -> startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:+94112345678")))
                        1 -> startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/94771234567")))
                        2 -> startActivity(Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:support@lankago.lk")))
                        3 -> startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:1912")))
                    }
                }
                .setPositiveButton("Close", null)
                .show()
        }

        // 10. Clear Cache
        findViewById<View>(R.id.row_setting_clear_cache)?.setOnClickListener {
            MaterialAlertDialogBuilder(this)
                .setTitle("Clear App Cache")
                .setMessage("Clear temporary cached images and files?")
                .setPositiveButton("Clear Now") { _, _ ->
                    lifecycleScope.launch(Dispatchers.IO) {
                        try {
                            cacheDir?.deleteRecursively()
                            Glide.get(this@SettingsActivity).clearDiskCache()
                        } catch (_: Exception) {}
                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@SettingsActivity, "🧹 App storage cache cleared successfully!", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        // 11. Privacy
        findViewById<View>(R.id.row_setting_privacy)?.setOnClickListener {
            MaterialAlertDialogBuilder(this)
                .setTitle("Privacy & Terms")
                .setMessage("LankaGo values your privacy. Your data is encrypted and location is used only for active navigation.")
                .setPositiveButton("Got It", null)
                .show()
        }

        // 12. About
        findViewById<View>(R.id.row_setting_about)?.setOnClickListener {
            MaterialAlertDialogBuilder(this)
                .setTitle("About LankaGo")
                .setMessage("LankaGo v1.0.0 (Build 2026)\nSmart Travel Companion for Sri Lanka 🇱🇰\nhttps://lankago.lk")
                .setPositiveButton("Awesome", null)
                .show()
        }
    }
}
