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

        val allFestivals = com.example.lankago.repository.CulturalFestivalRepository.getFestivals(requireContext())
        val baseIds = setOf("fest_kandy_perahera", "fest_vesak", "fest_vel_festival")

        val cardKandy = view.findViewById<CardView>(R.id.card_kandy_esala)
        val cardVesak = view.findViewById<CardView>(R.id.card_vesak_literary)
        val cardVel = view.findViewById<CardView>(R.id.card_vel_parade)

        cardKandy?.visibility = if (allFestivals.any { it.id == "fest_kandy_perahera" }) View.VISIBLE else View.GONE
        cardVesak?.visibility = if (allFestivals.any { it.id == "fest_vesak" }) View.VISIBLE else View.GONE
        cardVel?.visibility = if (allFestivals.any { it.id == "fest_vel_festival" }) View.VISIBLE else View.GONE

        cardKandy?.setOnClickListener { openFestival("fest_kandy_perahera") }
        cardVesak?.setOnClickListener { openFestival("fest_vesak") }
        cardVel?.setOnClickListener { openFestival("fest_vel_festival") }

        // Dynamically add extra or newly added festivals from Admin
        val dynamicContainer = view.findViewById<android.widget.LinearLayout>(R.id.layout_dynamic_festivals)
        dynamicContainer?.removeAllViews()

        val extraFestivals = allFestivals.filter { it.id !in baseIds }
        for (fest in extraFestivals) {
            val card = LayoutInflater.from(requireContext()).inflate(R.layout.item_admin_festival, dynamicContainer, false)
            card.findViewById<View>(R.id.btn_edit_festival)?.visibility = View.GONE
            card.findViewById<View>(R.id.btn_delete_festival)?.visibility = View.GONE
            card.findViewById<android.widget.TextView>(R.id.tv_admin_festival_id)?.visibility = View.GONE

            card.findViewById<android.widget.TextView>(R.id.tv_admin_festival_name)?.text = fest.name
            card.findViewById<android.widget.TextView>(R.id.tv_admin_festival_category)?.text = fest.category
            card.findViewById<android.widget.TextView>(R.id.tv_admin_festival_rating)?.text = "★ ${fest.rating}"
            card.findViewById<android.widget.TextView>(R.id.tv_admin_festival_dates)?.text = fest.dates
            card.findViewById<android.widget.TextView>(R.id.tv_admin_festival_location)?.text = fest.locationName

            val ivThumb = card.findViewById<ImageView>(R.id.iv_admin_festival_thumb)
            if (ivThumb != null) {
                when {
                    fest.imageUrl.isNotBlank() -> {
                        try {
                            val resName = fest.imageUrl.substringAfterLast("/")
                            val resId = requireContext().resources.getIdentifier(resName, "drawable", requireContext().packageName)
                            if (resId != 0) {
                                com.bumptech.glide.Glide.with(this).load(resId).centerCrop().into(ivThumb)
                            } else {
                                com.bumptech.glide.Glide.with(this).load(fest.imageUrl).centerCrop().placeholder(R.drawable.kandy_esala).into(ivThumb)
                            }
                        } catch (_: Exception) {
                            ivThumb.setImageResource(R.drawable.kandy_esala)
                        }
                    }
                    fest.bannerRes != 0 -> ivThumb.setImageResource(fest.bannerRes)
                    else -> ivThumb.setImageResource(R.drawable.kandy_esala)
                }
            }

            card.setOnClickListener {
                openFestival(fest.id)
            }
            dynamicContainer?.addView(card)
        }
    }
}
