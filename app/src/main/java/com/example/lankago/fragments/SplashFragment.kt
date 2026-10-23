package com.example.lankago.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.lankago.LankaGoApp
import com.example.lankago.R
import com.example.lankago.viewmodels.AuthViewModel
import com.example.lankago.viewmodels.SessionCheckState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class SplashFragment : Fragment() {

    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_splash, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val prefs = requireContext().getSharedPreferences(LankaGoApp.PREFS_SETTINGS, Context.MODE_PRIVATE)
        val onboardingCompleted = prefs.getBoolean(LankaGoApp.KEY_ONBOARDING_COMPLETED, false)

        viewLifecycleOwner.lifecycleScope.launch {
            // First time user: show branding for 1.5 seconds, then launch onboarding sequence
            if (!onboardingCompleted) {
                delay(1500)
                if (isAdded) {
                    try {
                        findNavController().navigate(R.id.action_splashFragment_to_discoverFragment)
                    } catch (_: Exception) {
                        try {
                            findNavController().navigate(R.id.discoverFragment)
                        } catch (_: Exception) {}
                    }
                }
                return@launch
            }

            // Subsequent launches: observe session state and route accordingly
            authViewModel.sessionCheckState.collectLatest { state ->
                when (state) {
                    is SessionCheckState.Checking -> {
                        // Keep showing splash branding & spinner
                    }
                    is SessionCheckState.Authenticated -> {
                        if (isAdded) {
                            try {
                                if (state.role.equals("admin", ignoreCase = true)) {
                                    findNavController().navigate(R.id.action_splashFragment_to_adminDashboardFragment)
                                } else {
                                    findNavController().navigate(R.id.action_splashFragment_to_homeFragment)
                                }
                            } catch (_: Exception) {
                                findNavController().navigate(R.id.action_splashFragment_to_homeFragment)
                            }
                        }
                    }
                    is SessionCheckState.Unauthenticated -> {
                        if (isAdded) {
                            try {
                                findNavController().navigate(R.id.action_splashFragment_to_loginFragment)
                            } catch (_: Exception) {
                                // Ignore navigation race conditions
                            }
                        }
                    }
                }
            }
        }

        if (onboardingCompleted) {
            authViewModel.checkSession(requireContext())
        }
    }
}
