package com.example.lankago.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
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

        // Next button navigates to PlanTrip
        view.findViewById<View>(R.id.btn_next)?.setOnClickListener {
            findNavController().navigate(R.id.action_experienceFragment_to_planTripFragment)
        }

        // Skip button also navigates to PlanTrip
        view.findViewById<View>(R.id.tv_skip)?.setOnClickListener {
            findNavController().navigate(R.id.action_experienceFragment_to_planTripFragment)
        }
    }
}
