package com.example.lankago.fragments

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.lankago.R
import com.example.lankago.adapters.FestivalScheduleAdapter
import com.example.lankago.data.CulturalFestival
import com.example.lankago.viewmodels.FestivalDetailViewModel
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class FestivalDetailFragment : Fragment() {

    private val viewModel: FestivalDetailViewModel by viewModels()
    private lateinit var scheduleAdapter: FestivalScheduleAdapter
    private var currentFestival: CulturalFestival? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_festival_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val festivalId = arguments?.getString("festivalId") ?: "fest_kandy_perahera"

        initViews(view)
        observeData(view)
        viewModel.loadFestival(festivalId, requireContext())
    }

    private fun initViews(view: View) {
        view.findViewById<ImageButton>(R.id.btn_back_festival)?.setOnClickListener {
            findNavController().navigateUp()
        }

        // Initialize Timeline Schedule RecyclerView
        val rvSchedule = view.findViewById<RecyclerView>(R.id.rv_festival_schedule)
        scheduleAdapter = FestivalScheduleAdapter(emptyList())
        rvSchedule?.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = scheduleAdapter
            isNestedScrollingEnabled = false
        }

        // Initialize Set Reminder Button Toggle
        val btnSetReminder = view.findViewById<MaterialButton>(R.id.btn_set_reminder)
        btnSetReminder?.setOnClickListener {
            val isNowActive = viewModel.toggleReminder(requireContext())
            val festivalName = currentFestival?.name ?: "Festival"

            if (isNowActive) {
                Snackbar.make(
                    view,
                    "Reminder scheduled for $festivalName! You will be notified before the event begins.",
                    Snackbar.LENGTH_LONG
                ).show()
            } else {
                Snackbar.make(
                    view,
                    "Reminder removed for $festivalName.",
                    Snackbar.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun observeData(rootView: View) {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.festival.collectLatest { item ->
                if (item != null) {
                    currentFestival = item
                    bindFestivalDetails(rootView, item)
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.isReminderSet.collectLatest { isReminderSet ->
                updateReminderButtonState(rootView, isReminderSet)
            }
        }
    }

    private fun bindFestivalDetails(view: View, item: CulturalFestival) {
        view.findViewById<ImageView>(R.id.iv_festival_banner)?.setImageResource(item.bannerRes)
        view.findViewById<TextView>(R.id.tv_festival_category)?.text = item.category
        view.findViewById<TextView>(R.id.tv_festival_title)?.text = item.name
        view.findViewById<TextView>(R.id.tv_festival_rating)?.text =
            "${item.rating} (${item.reviewCount} reviews)"

        view.findViewById<TextView>(R.id.tv_festival_dates)?.text = item.dates
        view.findViewById<TextView>(R.id.tv_festival_time)?.text = item.time
        view.findViewById<TextView>(R.id.tv_festival_location_name)?.text = item.locationName
        view.findViewById<TextView>(R.id.tv_festival_coordinates)?.text = item.coordinates
        view.findViewById<TextView>(R.id.tv_festival_history)?.text = item.historicalBackground

        // Populate Timeline Schedule
        scheduleAdapter.updateData(item.schedule)

        // Populate Visitor Etiquette Tips
        val tipsContainer = view.findViewById<LinearLayout>(R.id.layout_festival_tips)
        tipsContainer?.removeAllViews()

        item.visitorTips.forEach { tipText ->
            val tipRow = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 0, 0, 14)
                }
            }

            val icon = ImageView(requireContext()).apply {
                layoutParams = LinearLayout.LayoutParams(38, 38).apply {
                    setMargins(0, 4, 14, 0)
                }
                setImageResource(R.drawable.ic_shield)
                imageTintList = ColorStateList.valueOf(Color.parseColor("#5D5FEF"))
            }

            val label = TextView(requireContext()).apply {
                text = tipText
                textSize = 14f
                setTextColor(Color.parseColor("#4B5563"))
                setLineSpacing(4f, 1f)
            }

            tipRow.addView(icon)
            tipRow.addView(label)
            tipsContainer?.addView(tipRow)
        }
    }

    /**
     * Toggles the UI appearance of the Set Reminder button between active and inactive states.
     */
    private fun updateReminderButtonState(view: View, isSet: Boolean) {
        val btnReminder = view.findViewById<MaterialButton>(R.id.btn_set_reminder) ?: return
        val tvStatusLabel = view.findViewById<TextView>(R.id.tv_reminder_status_label)
        val tvSubtitle = view.findViewById<TextView>(R.id.tv_reminder_subtitle)

        if (isSet) {
            btnReminder.text = "Reminder Set"
            btnReminder.setIconResource(R.drawable.ic_check)
            btnReminder.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#10B981")) // Emerald Green
            btnReminder.setTextColor(Color.WHITE)
            btnReminder.iconTint = ColorStateList.valueOf(Color.WHITE)

            tvStatusLabel?.text = "Reminder active"
            tvStatusLabel?.setTextColor(Color.parseColor("#10B981"))
            tvSubtitle?.text = "We will notify you 24h prior"
        } else {
            btnReminder.text = "Set Reminder"
            btnReminder.setIconResource(R.drawable.ic_bell)
            btnReminder.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#5D5FEF")) // Indigo Primary
            btnReminder.setTextColor(Color.WHITE)
            btnReminder.iconTint = ColorStateList.valueOf(Color.WHITE)

            tvStatusLabel?.text = "Never miss the festival"
            tvStatusLabel?.setTextColor(Color.parseColor("#8A8F98"))
            tvSubtitle?.text = "Get notified before start"
        }
    }
}
