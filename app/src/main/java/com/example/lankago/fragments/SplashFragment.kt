package com.example.lankago.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.lankago.R
import com.example.lankago.viewmodels.AuthViewModel
import com.example.lankago.viewmodels.SessionCheckState
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

        // Observe session state and trigger navigation accordingly
        viewLifecycleOwner.lifecycleScope.launch {
            authViewModel.sessionCheckState.collectLatest { state ->
                when (state) {
                    is SessionCheckState.Checking -> {
                        // Keep showing splash branding & spinner
                    }
                    is SessionCheckState.Authenticated -> {
                        if (isAdded) {
                            try {
                                findNavController().navigate(R.id.action_splashFragment_to_homeFragment)
                            } catch (e: Exception) {
                                // Ignore navigation race conditions
                            }
                        }
                    }
                    is SessionCheckState.Unauthenticated -> {
                        if (isAdded) {
                            try {
                                findNavController().navigate(R.id.action_splashFragment_to_loginFragment)
                            } catch (e: Exception) {
                                // Ignore navigation race conditions
                            }
                        }
                    }
                }
            }
        }

        // Start session check
        authViewModel.checkSession(requireContext())
    }
}
