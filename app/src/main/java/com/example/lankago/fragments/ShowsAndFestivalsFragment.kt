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

class ShowsAndFestivalsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.shows_and_festivals, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<ImageView>(R.id.btn_back_shows)?.setOnClickListener {
            findNavController().navigateUp()
        }

        fun openFestival(festivalId: String) {
            val bundle = bundleOf("festivalId" to festivalId)
            findNavController().navigate(
                R.id.action_showsAndFestivalsFragment_to_festivalDetailFragment,
                bundle
            )
        }

        view.findViewById<CardView>(R.id.card_kandy_esala)?.setOnClickListener {
            openFestival("fest_kandy_perahera")
        }

        view.findViewById<CardView>(R.id.card_vesak_literary)?.setOnClickListener {
            openFestival("fest_vesak")
        }

        view.findViewById<CardView>(R.id.card_vel_parade)?.setOnClickListener {
            openFestival("fest_vel_festival")
        }
    }
}
