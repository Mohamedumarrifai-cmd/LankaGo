package com.example.lankago.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.cardview.widget.CardView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.lankago.R

class TraditionalActivitiesFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.traditional_activities, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<ImageView>(R.id.btn_back_traditional)?.setOnClickListener {
            findNavController().navigateUp()
        }

        val activeTripId = arguments?.getString("activeTripId") ?: ""
        fun openDetail(activityId: String) {
            val bundle = bundleOf(
                "traditionalActivityId" to activityId,
                "activeTripId" to activeTripId
            )
            findNavController().navigate(
                R.id.action_traditionalActivitiesFragment_to_traditionalActivityDetailFragment,
                bundle
            )
        }

        view.findViewById<CardView>(R.id.card_clay_pottery)?.setOnClickListener {
            openDetail("act_pottery_making")
        }

        view.findViewById<CardView>(R.id.card_tea_estate)?.setOnClickListener {
            openDetail("act_tea_estate")
        }

        view.findViewById<CardView>(R.id.card_mask_carving)?.setOnClickListener {
            openDetail("act_wood_carving")
        }

        view.findViewById<CardView>(R.id.card_cooking_village)?.setOnClickListener {
            openDetail("act_cooking_class")
        }

        view.findViewById<CardView>(R.id.card_batik_workshop)?.setOnClickListener {
            openDetail("act_batik_workshop")
        }

        view.findViewById<CardView>(R.id.card_traditional_dance)?.setOnClickListener {
            openDetail("act_traditional_dance")
        }
    }
}
