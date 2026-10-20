package com.example.lankago.fragments

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.lankago.R
import com.example.lankago.adapters.PlaceAdapter
import com.example.lankago.utils.LankaGoDataManager
import com.example.lankago.utils.Place
import java.util.Locale

class SearchFragment : Fragment() {

    private lateinit var placeAdapter: PlaceAdapter
    private lateinit var rvSearchResults: RecyclerView
    private lateinit var layoutEmptyState: View
    private lateinit var etSearch: EditText

    // Filter Chips
    private lateinit var chipAll: TextView
    private lateinit var chipHeritage: TextView
    private lateinit var chipBeach: TextView
    private lateinit var chipWildlife: TextView

    private var selectedCategory: String = "All"
    private var currentQuery: String = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.search, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews(view)
        setupRecyclerView()
        setupSearchInput()
        setupCategoryChips()

        // Initial populate
        filterData()
    }

    private fun initViews(view: View) {
        view.findViewById<ImageView>(R.id.btn_back)?.setOnClickListener {
            findNavController().navigateUp()
        }

        etSearch = view.findViewById(R.id.et_search)
        rvSearchResults = view.findViewById(R.id.rv_search_results)
        layoutEmptyState = view.findViewById(R.id.layout_empty_state)

        chipAll = view.findViewById(R.id.chip_all)
        chipHeritage = view.findViewById(R.id.chip_heritage)
        chipBeach = view.findViewById(R.id.chip_beach)
        chipWildlife = view.findViewById(R.id.chip_wildlife)
    }

    private fun setupRecyclerView() {
        placeAdapter = PlaceAdapter(
            places = mutableListOf(),
            onPlaceClick = { place ->
                val bundle = bundleOf(
                    "place_id" to place.id,
                    "place_name" to place.name,
                    "place_category" to place.category,
                    "place_location" to place.location,
                    "place_image_uri" to place.imageUri,
                    "place_description" to place.description,
                    "place_rating" to place.rating,
                    "place_entrance_fee" to place.entranceFee
                )
                findNavController().navigate(R.id.destinationDetailFragment, bundle)
            },
            onFavoriteClick = { place, favoriteIcon ->
                // Ready for Feature 2 toggle integration
            }
        )

        rvSearchResults.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = placeAdapter
        }
    }

    private fun setupSearchInput() {
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                currentQuery = s?.toString()?.trim() ?: ""
                filterData()
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun setupCategoryChips() {
        chipAll.setOnClickListener { selectCategory("All", chipAll) }
        chipHeritage.setOnClickListener { selectCategory("Heritage", chipHeritage) }
        chipBeach.setOnClickListener { selectCategory("Beach", chipBeach) }
        chipWildlife.setOnClickListener { selectCategory("Wildlife", chipWildlife) }
    }

    private fun selectCategory(category: String, selectedChip: TextView) {
        selectedCategory = category

        // Reset all chips to inactive style
        val chips = listOf(chipAll, chipHeritage, chipBeach, chipWildlife)
        for (chip in chips) {
            chip.setBackgroundResource(R.drawable.chip_inactive)
            chip.setTextColor(Color.parseColor("#1A2B4C"))
            chip.setTypeface(null, Typeface.NORMAL)
        }

        // Set clicked chip to active style
        selectedChip.setBackgroundResource(R.drawable.chip_active)
        selectedChip.setTextColor(Color.WHITE)
        selectedChip.setTypeface(null, Typeface.BOLD)

        filterData()
    }

    private fun filterData() {
        val query = currentQuery.lowercase(Locale.getDefault())

        val filtered = LankaGoDataManager.placesList.filter { place ->
            val matchesCategory = if (selectedCategory == "All") {
                true
            } else {
                place.category.contains(selectedCategory, ignoreCase = true)
            }

            val matchesQuery = if (query.isEmpty()) {
                true
            } else {
                place.name.lowercase(Locale.getDefault()).contains(query) ||
                        place.location.lowercase(Locale.getDefault()).contains(query) ||
                        place.description.lowercase(Locale.getDefault()).contains(query)
            }

            matchesCategory && matchesQuery
        }

        placeAdapter.updateData(filtered)

        // Handle Empty State
        if (filtered.isEmpty()) {
            layoutEmptyState.visibility = View.VISIBLE
            rvSearchResults.visibility = View.GONE
        } else {
            layoutEmptyState.visibility = View.GONE
            rvSearchResults.visibility = View.VISIBLE
        }
    }
}