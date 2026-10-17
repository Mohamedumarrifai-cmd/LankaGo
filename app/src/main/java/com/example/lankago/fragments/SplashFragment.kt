package com.example.lankago.fragments

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.lankago.R

class SplashFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_splash, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 2 seconds nawaathila Discover ekata yanna
        Handler(Looper.getMainLooper()).postDelayed({
            // check karanna fragment eka thama active da kiyala
            if (isAdded && view != null) {
                try {
                    findNavController().navigate(R.id.action_splashFragment_to_discoverFragment)
                } catch (e: Exception) {
                    // Error ekak awoth app eka crash wenna denne nathiwa handle karanawa
                    e.printStackTrace()
                }
            }
        }, 2000)
    }
}