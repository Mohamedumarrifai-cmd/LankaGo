package com.example.lankago.fragments

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
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.lankago.R
import com.example.lankago.adapters.PlaceAdapter
import com.example.lankago.utils.LankaGoDataManager
import com.example.lankago.utils.Place
import com.example.lankago.utils.safeNavigate

class SearchFragment : Fragment() {

    private lateinit var etSearch: EditText
    private lateinit var btnBack: ImageView
    private lateinit var rvSearchResults: RecyclerView
    private lateinit var layoutEmptyState: View
    private var scrollChips: android.widget.HorizontalScrollView? = null

    private lateinit var chipAll: TextView
    private lateinit var chipHeritage: TextView
    private lateinit var chipLakes: TextView
    private lateinit var chipHills: TextView
    private lateinit var chipBeach: TextView
    private lateinit var chipWildlife: TextView
    private lateinit var chipSortAz: TextView

    private lateinit var adapter: PlaceAdapter
    private val displayedPlaces = mutableListOf<Place>()
    private var selectedCategory: String = "All"
    private var isSortAz: Boolean = false

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
        setupChips()
        setupRecyclerView()
        setupSearch()

        // Real-time synchronization with Firebase /destinations
        context?.let { ctx ->
            LankaGoDataManager.syncWithFirebase(ctx) {
                if (isAdded) {
                    filterData()
                }
            }
        }
    }

    private fun initViews(view: View) {
        btnBack = view.findViewById(R.id.btn_back)
        etSearch = view.findViewById(R.id.et_search)
        rvSearchResults = view.findViewById(R.id.rv_search_results)
        layoutEmptyState = view.findViewById(R.id.layout_empty_state)
        scrollChips = view.findViewById(R.id.scroll_chips)

        chipAll = view.findViewById(R.id.chip_all)
        chipHeritage = view.findViewById(R.id.chip_heritage)
        chipLakes = view.findViewById(R.id.chip_lakes)
        chipHills = view.findViewById(R.id.chip_hills)
        chipBeach = view.findViewById(R.id.chip_beach)
        chipWildlife = view.findViewById(R.id.chip_wildlife)
        chipSortAz = view.findViewById(R.id.chip_sort_az)

        btnBack.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupChips() {
        val chips = listOf(
            chipAll to "All",
            chipHeritage to "Heritage",
            chipLakes to "Lakes",
            chipHills to "Hills",
            chipBeach to "Beach",
            chipWildlife to "Wildlife"
        )

        fun updateChipsUi(activeChip: TextView) {
            for ((chip, _) in chips) {
                if (chip == activeChip) {
                    chip.setBackgroundResource(R.drawable.chip_active)
                    chip.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                    chip.setTypeface(null, Typeface.BOLD)
                } else {
                    chip.setBackgroundResource(R.drawable.chip_inactive)
                    chip.setTextColor(0xFF1A2B4C.toInt())
                    chip.setTypeface(null, Typeface.NORMAL)
                }
            }
        }

        chipAll.setOnClickListener {
            selectedCategory = "All"
            updateChipsUi(chipAll)
            filterData()
        }

        chipHeritage.setOnClickListener {
            selectedCategory = "Heritage"
            updateChipsUi(chipHeritage)
            filterData()
        }

        chipLakes.setOnClickListener {
            selectedCategory = "Lakes"
            updateChipsUi(chipLakes)
            filterData()
        }

        chipHills.setOnClickListener {
            selectedCategory = "Hills"
            updateChipsUi(chipHills)
            filterData()
        }

        chipBeach.setOnClickListener {
            selectedCategory = "Beach"
            updateChipsUi(chipBeach)
            filterData()
        }

        chipWildlife.setOnClickListener {
            selectedCategory = "Wildlife"
            updateChipsUi(chipWildlife)
            filterData()
        }

        fun updateSortAzUi() {
            if (isSortAz) {
                chipSortAz.setBackgroundResource(R.drawable.chip_active)
                chipSortAz.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                chipSortAz.setTypeface(null, Typeface.BOLD)
                chipSortAz.text = "✓ Sorted (A-Z)"
            } else {
                chipSortAz.setBackgroundResource(R.drawable.chip_inactive)
                chipSortAz.setTextColor(0xFF1A2B4C.toInt())
                chipSortAz.setTypeface(null, Typeface.NORMAL)
                chipSortAz.text = "🔤 Sort A-Z"
            }
        }

        chipSortAz.setOnClickListener {
            isSortAz = !isSortAz
            updateSortAzUi()
            filterData()
        }

        // Check if an initial sort order was passed
        isSortAz = arguments?.getBoolean("sort_az", false) ?: false
        updateSortAzUi()

        // Check if an initial category was passed from Home or elsewhere
        val incomingCategory = arguments?.getString("selected_category")
        if (!incomingCategory.isNullOrBlank()) {
            val matchedChipPair = chips.find {
                it.second.equals(incomingCategory, ignoreCase = true) ||
                (incomingCategory.startsWith("Beach", ignoreCase = true) && it.second.startsWith("Beach", ignoreCase = true)) ||
                (incomingCategory.startsWith("Lake", ignoreCase = true) && it.second.startsWith("Lake", ignoreCase = true)) ||
                (incomingCategory.startsWith("Hill", ignoreCase = true) && it.second.startsWith("Hill", ignoreCase = true))
            }
            if (matchedChipPair != null) {
                selectedCategory = matchedChipPair.second
                updateChipsUi(matchedChipPair.first)
                scrollChips?.post {
                    scrollChips?.smoothScrollTo(matchedChipPair.first.left - 40, 0)
                }
            }
        }
    }

    private fun setupRecyclerView() {
        adapter = PlaceAdapter(
            places = displayedPlaces,
            onPlaceClick = { place ->
                val bundle = Bundle().apply {
                    putString("place_id", place.id)
                }
                findNavController().safeNavigate(
                    R.id.action_searchFragment_to_destinationDetailFragment,
                    bundle
                )
            }
        )
        rvSearchResults.layoutManager = LinearLayoutManager(requireContext())
        rvSearchResults.adapter = adapter
    }

    private fun setupSearch() {
        // Handle incoming prefilled query
        val initialQuery = arguments?.getString("search_query") ?: ""
        if (initialQuery.isNotBlank()) {
            etSearch.setText(initialQuery)
            etSearch.setSelection(initialQuery.length)
        }

        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterData()
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        filterData()
    }

    private fun filterData() {
        val context = context ?: return
        val allPlaces = LankaGoDataManager.getPlaces(context)
        val query = etSearch.text?.toString()?.trim() ?: ""

        val filtered = allPlaces.filter { place ->
            val matchesCategory = if (selectedCategory.equals("All", ignoreCase = true)) {
                true
            } else {
                place.category.equals(selectedCategory, ignoreCase = true) ||
                (selectedCategory.startsWith("Beach", ignoreCase = true) && place.category.startsWith("Beach", ignoreCase = true)) ||
                (selectedCategory.startsWith("Lake", ignoreCase = true) && place.category.startsWith("Lake", ignoreCase = true)) ||
                (selectedCategory.startsWith("Hill", ignoreCase = true) && place.category.startsWith("Hill", ignoreCase = true))
            }

            val matchesQuery = if (query.isBlank()) {
                true
            } else {
                place.name.contains(query, ignoreCase = true) ||
                place.location.contains(query, ignoreCase = true) ||
                place.description.contains(query, ignoreCase = true) ||
                place.category.contains(query, ignoreCase = true)
            }

            matchesCategory && matchesQuery
        }

        val sortedList = if (isSortAz) {
            filtered.sortedBy { it.name.lowercase() }
        } else {
            filtered
        }

        displayedPlaces.clear()
        displayedPlaces.addAll(sortedList)
        adapter.notifyDataSetChanged()

        if (displayedPlaces.isEmpty()) {
            layoutEmptyState.visibility = View.VISIBLE
            rvSearchResults.visibility = View.GONE
        } else {
            layoutEmptyState.visibility = View.GONE
            rvSearchResults.visibility = View.VISIBLE
        }
    }
}
