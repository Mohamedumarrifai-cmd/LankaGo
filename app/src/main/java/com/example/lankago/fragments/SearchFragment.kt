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

    private lateinit var chipAll: TextView
    private lateinit var chipHeritage: TextView
    private lateinit var chipBeach: TextView
    private lateinit var chipWildlife: TextView

    private lateinit var adapter: PlaceAdapter
    private val displayedPlaces = mutableListOf<Place>()
    private var selectedCategory: String = "All"

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
    }

    private fun initViews(view: View) {
        btnBack = view.findViewById(R.id.btn_back)
        etSearch = view.findViewById(R.id.et_search)
        rvSearchResults = view.findViewById(R.id.rv_search_results)
        layoutEmptyState = view.findViewById(R.id.layout_empty_state)

        chipAll = view.findViewById(R.id.chip_all)
        chipHeritage = view.findViewById(R.id.chip_heritage)
        chipBeach = view.findViewById(R.id.chip_beach)
        chipWildlife = view.findViewById(R.id.chip_wildlife)

        btnBack.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupChips() {
        val chips = listOf(
            chipAll to "All",
            chipHeritage to "Heritage",
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
            val matchesCategory = if (selectedCategory == "All") {
                true
            } else {
                place.category.equals(selectedCategory, ignoreCase = true)
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

        displayedPlaces.clear()
        displayedPlaces.addAll(filtered)
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
