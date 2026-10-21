package com.example.lankago

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.lankago.services.MyFirebaseMessagingService
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private lateinit var navController: NavController
    private lateinit var bottomNavView: BottomNavigationView

    private val requestNotificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Log.d("MainActivity", "Notification permission granted.")
            MyFirebaseMessagingService.syncCurrentUserToken()
        } else {
            Log.w("MainActivity", "Notification permission denied.")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set status bar styling
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }
        window.statusBarColor = Color.parseColor("#FFFFFF")

        setContentView(R.layout.activity_main)

        setupGlobalNavigation()
        checkAndRequestNotificationPermission()
        handleNotificationIntent()
    }

    override fun onResume() {
        super.onResume()
        // Ensure token is synced if user is authenticated
        if (FirebaseAuth.getInstance().currentUser != null) {
            MyFirebaseMessagingService.syncCurrentUserToken()
        }
    }

    /**
     * Checks and requests runtime notification permission for Android 13+ (API 33).
     */
    private fun checkAndRequestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                MyFirebaseMessagingService.syncCurrentUserToken()
            }
        } else {
            MyFirebaseMessagingService.syncCurrentUserToken()
        }
    }

    /**
     * Handles incoming notification payload when app is opened via Heads-Up Notification tap.
     */
    private fun handleNotificationIntent() {
        val bookingId = intent?.getStringExtra("bookingId")
        val status = intent?.getStringExtra("status")
        if (!bookingId.isNullOrBlank()) {
            Log.d("MainActivity", "Launched from push notification for booking: $bookingId (status: $status)")
        }
    }

    /**
     * Configures the global BottomNavigationView and NavController integration.
     */
    private fun setupGlobalNavigation() {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as? NavHostFragment
            ?: return

        navController = navHostFragment.navController
        bottomNavView = findViewById(R.id.bottom_nav_view)

        // Default setup with NavController to sync highlighted tabs
        bottomNavView.setupWithNavController(navController)

        // Custom item selection listener enforcing strict backstack management
        bottomNavView.setOnItemSelectedListener { item ->
            val builder = NavOptions.Builder()
                .setLaunchSingleTop(true)
                .setPopUpTo(R.id.homeFragment, false)

            when (item.itemId) {
                R.id.homeFragment -> {
                    if (navController.currentDestination?.id != R.id.homeFragment) {
                        navController.navigate(R.id.homeFragment, null, builder.build())
                    }
                    true
                }
                R.id.mapRouteFragment -> {
                    if (navController.currentDestination?.id != R.id.mapRouteFragment) {
                        navController.navigate(R.id.mapRouteFragment, null, builder.build())
                    }
                    true
                }
                R.id.planTripFragment -> {
                    if (navController.currentDestination?.id != R.id.planTripFragment) {
                        navController.navigate(R.id.planTripFragment, null, builder.build())
                    }
                    true
                }
                R.id.profileFragment -> {
                    if (navController.currentDestination?.id != R.id.profileFragment) {
                        navController.navigate(R.id.profileFragment, null, builder.build())
                    }
                    true
                }
                else -> false
            }
        }

        // Reselection handler: popping to destination top
        bottomNavView.setOnItemReselectedListener { item ->
            if (item.itemId == R.id.homeFragment) {
                navController.popBackStack(R.id.homeFragment, false)
            }
        }

        // Manage Bottom Navigation Visibility across screens
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.homeFragment,
                R.id.mapRouteFragment,
                R.id.planTripFragment,
                R.id.profileFragment,
                R.id.tripPlannerFragment -> {
                    bottomNavView.visibility = View.VISIBLE
                }
                else -> {
                    // Hide on Splash, Login, Register, Admin, Details screens
                    bottomNavView.visibility = View.GONE
                }
            }
        }
    }
}