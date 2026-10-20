package com.example.lankago.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.lankago.LankaGoApp
import com.example.lankago.R
import com.google.android.material.switchmaterial.SwitchMaterial

/**
 * SettingsFragment supporting instant, persistent Dark Mode toggle
 * and smooth navigation.
 */
class SettingsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.settings, container, false)

        // Top bar back navigation
        root.findViewById<ImageView>(R.id.top_bar)?.setOnClickListener {
            findNavController().navigateUp()
        }
        root.findViewById<View>(R.id.top_bar)?.findViewById<ImageView>(R.id.top_bar)?.setOnClickListener {
            findNavController().navigateUp()
        }

        val btnBack = root.findViewById<View>(R.id.top_bar)?.findViewWithTag<View>("back")
            ?: root.findViewById<View>(R.id.top_bar)
        btnBack?.setOnClickListener {
            findNavController().navigateUp()
        }

        val switchDarkMode = root.findViewById<SwitchMaterial>(R.id.switch_dark_mode)
        val prefs = requireContext().getSharedPreferences(LankaGoApp.PREFS_SETTINGS, Context.MODE_PRIVATE)

        // Read saved preference
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

        return root
    }
}
