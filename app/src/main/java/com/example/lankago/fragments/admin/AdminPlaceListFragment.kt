package com.example.lankago.fragments.admin

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import androidx.appcompat.widget.Toolbar
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.lankago.R
import com.example.lankago.adapters.AdminPlaceAdapter
import com.example.lankago.utils.LankaGoDataManager
import com.example.lankago.utils.Place
import com.google.android.material.chip.ChipGroup
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.snackbar.Snackbar

class AdminPlaceListFragment : Fragment() {

    private lateinit var placeAdapter: AdminPlaceAdapter
    private var allPlaces: List<Place> = emptyList()
    private var currentCategory: String = "All"
    private var currentSearch: String = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_admin_place_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val toolbar = view.findViewById<Toolbar>(R.id.toolbar_admin_places)
        val rvPlaces = view.findViewById<RecyclerView>(R.id.rv_admin_places)
        val etSearch = view.findViewById<EditText>(R.id.et_admin_places_search)
        val chipGroup = view.findViewById<ChipGroup>(R.id.chip_group_admin_place_categories)
        val fabAdd = view.findViewById<ExtendedFloatingActionButton>(R.id.fab_add_place)

        toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        placeAdapter = AdminPlaceAdapter(
            onEditClick = { place ->
                val bundle = bundleOf("placeId" to place.id)
                findNavController().navigate(
                    R.id.action_adminPlaceListFragment_to_addEditPlaceFragment,
                    bundle
                )
            },
            onDeleteClick = { place ->
                confirmDeletePlace(place)
            }
        )

        rvPlaces.layoutManager = LinearLayoutManager(requireContext())
        rvPlaces.adapter = placeAdapter

        fabAdd.setOnClickListener {
            val bundle = bundleOf("placeId" to null)
            findNavController().navigate(
                R.id.action_adminPlaceListFragment_to_addEditPlaceFragment,
                bundle
            )
        }

        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                currentSearch = s?.toString()?.trim() ?: ""
                filterAndSubmit()
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        chipGroup.setOnCheckedStateChangeListener { _, checkedIds ->
            currentCategory = when {
                checkedIds.contains(R.id.chip_place_cat_heritage) -> "Heritage"
                checkedIds.contains(R.id.chip_place_cat_hills) -> "Hills"
                checkedIds.contains(R.id.chip_place_cat_lakes) -> "Lakes"
                checkedIds.contains(R.id.chip_place_cat_wildlife) -> "Wildlife"
                checkedIds.contains(R.id.chip_place_cat_beaches) -> "Beaches"
                else -> "All"
            }
            filterAndSubmit()
        }

        loadPlaces()
    }

    override fun onResume() {
        super.onResume()
        loadPlaces()
    }

    private fun loadPlaces() {
        allPlaces = LankaGoDataManager.getPlaces(requireContext())
        filterAndSubmit()
    }

    private fun filterAndSubmit() {
        val view = view ?: return
        val rvPlaces = view.findViewById<RecyclerView>(R.id.rv_admin_places)
        val layoutEmpty = view.findViewById<LinearLayout>(R.id.layout_empty_places)

        var filtered = allPlaces
        if (currentCategory != "All") {
            filtered = filtered.filter {
                it.category.equals(currentCategory, ignoreCase = true) ||
                (currentCategory.equals("Beaches", ignoreCase = true) && it.category.startsWith("Beach", ignoreCase = true))
            }
        }

        if (currentSearch.isNotEmpty()) {
            val q = currentSearch.lowercase()
            filtered = filtered.filter {
                it.name.lowercase().contains(q) ||
                it.location.lowercase().contains(q) ||
                it.category.lowercase().contains(q) ||
                it.description.lowercase().contains(q)
            }
        }

        placeAdapter.submitList(filtered)
        if (filtered.isEmpty()) {
            rvPlaces.visibility = View.GONE
            layoutEmpty.visibility = View.VISIBLE
        } else {
            rvPlaces.visibility = View.VISIBLE
            layoutEmpty.visibility = View.GONE
        }
    }

    private fun confirmDeletePlace(place: Place) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Delete Destination")
            .setMessage("Are you sure you want to delete \"${place.name}\"? This destination will be removed from LankaGo.")
            .setPositiveButton("Delete") { _, _ ->
                LankaGoDataManager.deletePlace(requireContext(), place.id)
                loadPlaces()
                Snackbar.make(
                    requireView(),
                    "\"${place.name}\" deleted successfully",
                    Snackbar.LENGTH_SHORT
                ).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
