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

class PlanOnboardingFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.onboarding_plan, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // "Get Started" button completes onboarding and navigates to Login
        view.findViewById<View>(R.id.btn_next)?.setOnClickListener {
            completeOnboardingAndGoToLogin()
        }

        // Skip button also completes onboarding and navigates to Login
        view.findViewById<View>(R.id.tv_skip)?.setOnClickListener {
            completeOnboardingAndGoToLogin()
        }
    }

    private fun completeOnboardingAndGoToLogin() {
        val prefs = requireContext().getSharedPreferences(LankaGoApp.PREFS_SETTINGS, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(LankaGoApp.KEY_ONBOARDING_COMPLETED, true).apply()
        try {
            findNavController().navigate(R.id.action_planOnboardingFragment_to_loginFragment)
        } catch (_: Exception) {
            findNavController().navigate(R.id.loginFragment)
        }
    }
}
