package com.example.lankago.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.lankago.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_splash, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Wait 2 seconds and safely navigate to DiscoverFragment
        viewLifecycleOwner.lifecycleScope.launch {
            delay(2000L)
            if (isAdded && findNavController().currentDestination?.id == R.id.splashFragment) {
                findNavController().navigate(R.id.action_splashFragment_to_discoverFragment)
            }
        }
    }
}