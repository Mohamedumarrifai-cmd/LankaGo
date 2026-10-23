package com.example.lankago.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.lankago.LankaGoApp
import com.example.lankago.R

class ExperienceFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.experience, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Next button navigates to Screen 3 (Vehicle & Bike Rentals)
        view.findViewById<View>(R.id.btn_next)?.setOnClickListener {
            try {
                findNavController().navigate(R.id.action_experienceFragment_to_rentalsOnboardingFragment)
            } catch (_: Exception) {
                findNavController().navigate(R.id.rentalsOnboardingFragment)
            }
        }

        // Skip button marks completed and skips directly to Login
        view.findViewById<View>(R.id.tv_skip)?.setOnClickListener {
            completeOnboardingAndGoToLogin()
        }
    }

    private fun completeOnboardingAndGoToLogin() {
        val prefs = requireContext().getSharedPreferences(LankaGoApp.PREFS_SETTINGS, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(LankaGoApp.KEY_ONBOARDING_COMPLETED, true).apply()
        try {
            findNavController().navigate(R.id.action_experienceFragment_to_loginFragment)
        } catch (_: Exception) {
            findNavController().navigate(R.id.loginFragment)
        }
    }
}
