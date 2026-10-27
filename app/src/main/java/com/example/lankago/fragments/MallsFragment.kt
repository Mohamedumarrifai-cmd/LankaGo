package com.example.lankago.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.lankago.R
import com.example.lankago.adapters.MallAdapter
import com.example.lankago.data.Mall
import com.example.lankago.data.MallDataProvider
import com.example.lankago.repository.AdminRepository
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.ChipGroup
import kotlinx.coroutines.launch

/**
 * Modern screen showcasing top Sri Lankan shopping malls:
 * Havelock City Mall, One Galle Face (GCC), Colombo City Centre (CCC),
 * Kandy City Centre (KCC), Marino Mall, Crescat, etc.
 * Features real-time sync with Firebase Realtime Database and interactive detail bottom sheet.
 */
class MallsFragment : Fragment() {

    private val adminRepository = AdminRepository()
    private lateinit var mallAdapter: MallAdapter

    private var allMalls: List<Mall> = emptyList()
    private var activeFilterCity: String = "All"
    private var searchQuery: String = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_malls, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val toolbar = view.findViewById<Toolbar>(R.id.toolbar_malls)
        val etSearch = view.findViewById<EditText>(R.id.et_search_malls)
        val chipGroup = view.findViewById<ChipGroup>(R.id.chip_group_mall_filters)
        val rvMalls = view.findViewById<RecyclerView>(R.id.rv_malls)
        val pbLoading = view.findViewById<ProgressBar>(R.id.pb_malls_loading)
        val layoutEmpty = view.findViewById<LinearLayout>(R.id.layout_empty_malls)

        toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        mallAdapter = MallAdapter { mall ->
            showMallDetailBottomSheet(mall)
        }

        rvMalls.layoutManager = LinearLayoutManager(requireContext())
        rvMalls.adapter = mallAdapter

        // Search Filter
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                searchQuery = s?.toString()?.trim().orEmpty()
                applyFilters(layoutEmpty, rvMalls)
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        // Chips Filter
        chipGroup.setOnCheckedStateChangeListener { _, checkedIds ->
            activeFilterCity = when {
                checkedIds.contains(R.id.chip_colombo) -> "Colombo"
                checkedIds.contains(R.id.chip_kandy) -> "Kandy"
                checkedIds.contains(R.id.chip_cinemas) -> "Cinemas"
                checkedIds.contains(R.id.chip_food_courts) -> "Food"
                else -> "All"
            }
            applyFilters(layoutEmpty, rvMalls)
        }

        // Real-time observation from Firebase Realtime Database
        pbLoading.visibility = View.VISIBLE
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                try {
                    adminRepository.observeMalls().collect { fbMalls ->
                        pbLoading.visibility = View.GONE
                        allMalls = if (fbMalls.isNotEmpty()) {
                            fbMalls
                        } else {
                            MallDataProvider.defaultMalls
                        }
                        applyFilters(layoutEmpty, rvMalls)
                    }
                } catch (_: Exception) {
                    pbLoading.visibility = View.GONE
                    allMalls = MallDataProvider.defaultMalls
                    applyFilters(layoutEmpty, rvMalls)
                }
            }
        }
    }

    private fun applyFilters(layoutEmpty: LinearLayout, rvMalls: RecyclerView) {
        val filtered = allMalls.filter { mall ->
            val matchesSearch = searchQuery.isBlank() ||
                    mall.name.contains(searchQuery, ignoreCase = true) ||
                    mall.city.contains(searchQuery, ignoreCase = true) ||
                    mall.address.contains(searchQuery, ignoreCase = true) ||
                    mall.description.contains(searchQuery, ignoreCase = true)

            val matchesCategory = when (activeFilterCity) {
                "Colombo" -> mall.city.contains("Colombo", ignoreCase = true) || mall.address.contains("Colombo", ignoreCase = true)
                "Kandy" -> mall.city.contains("Kandy", ignoreCase = true) || mall.address.contains("Kandy", ignoreCase = true)
                "Cinemas" -> mall.features.any { it.contains("Cinema", ignoreCase = true) || it.contains("IMAX", ignoreCase = true) } || mall.description.contains("Cinema", ignoreCase = true)
                "Food" -> mall.features.any { it.contains("Food", ignoreCase = true) || it.contains("Dining", ignoreCase = true) } || mall.description.contains("Food", ignoreCase = true)
                else -> true
            }

            matchesSearch && matchesCategory
        }

        mallAdapter.submitList(filtered)
        if (filtered.isEmpty()) {
            layoutEmpty.visibility = View.VISIBLE
            rvMalls.visibility = View.GONE
        } else {
            layoutEmpty.visibility = View.GONE
            rvMalls.visibility = View.VISIBLE
        }
    }

    private fun showMallDetailBottomSheet(mall: Mall) {
        val dialog = BottomSheetDialog(requireContext())
        val dialogView = layoutInflater.inflate(R.layout.bottom_sheet_mall_detail, null)
        dialog.setContentView(dialogView)

        val tvName = dialogView.findViewById<TextView>(R.id.tv_bs_mall_name)
        val tvAddress = dialogView.findViewById<TextView>(R.id.tv_bs_mall_address)
        val tvHours = dialogView.findViewById<TextView>(R.id.tv_bs_mall_hours)
        val tvRating = dialogView.findViewById<TextView>(R.id.tv_bs_mall_rating)
        val tvStores = dialogView.findViewById<TextView>(R.id.tv_bs_mall_stores)
        val tvDescription = dialogView.findViewById<TextView>(R.id.tv_bs_mall_description)
        val tvFeatures = dialogView.findViewById<TextView>(R.id.tv_bs_mall_features)
        val btnCall = dialogView.findViewById<MaterialButton>(R.id.btn_bs_call_mall)
        val btnMaps = dialogView.findViewById<MaterialButton>(R.id.btn_bs_directions_mall)

        tvName?.text = mall.name
        tvAddress?.text = "${mall.address} (${mall.city})"
        tvHours?.text = mall.operatingHours
        tvRating?.text = "★ ${String.format(java.util.Locale.US, "%.1f", mall.rating)}"
        tvStores?.text = "${mall.storeCount}+ Stores"
        tvDescription?.text = mall.description
        tvFeatures?.text = mall.features.joinToString(" • ")

        btnCall?.setOnClickListener {
            try {
                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${mall.contactNumber}"))
                startActivity(intent)
            } catch (_: Exception) {}
        }

        btnMaps?.setOnClickListener {
            try {
                val mapUri = Uri.parse("geo:0,0?q=${Uri.encode("${mall.name}, ${mall.address}")}")
                val mapIntent = Intent(Intent.ACTION_VIEW, mapUri)
                startActivity(mapIntent)
            } catch (_: Exception) {}
        }

        dialog.show()
    }
}
