package com.example.lankago

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var navController: NavController
    private lateinit var bottomNavView: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set status bar styling
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }
        window.statusBarColor = Color.parseColor("#FFFFFF")

        setContentView(R.layout.activity_main)

        setupGlobalNavigation()
    }

    /**
     * Configures the global BottomNavigationView and NavController integration.
     * Guarantees backstack integrity:
     * - Pressing Home clears intermediate backstack without app crashes.
     * - Tabs pop up to the home destination so the backstack never grows infinitely.
     * - Bottom bar automatically hides on auth and immersive full-screen flows.
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
                .setPopUpTo(R.id.homeFragment, false) // Always pop backstack to Home root without clearing Home itself

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