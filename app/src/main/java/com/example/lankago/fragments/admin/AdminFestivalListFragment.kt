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
import com.example.lankago.adapters.AdminFestivalAdapter
import com.example.lankago.data.CulturalFestival
import com.example.lankago.repository.CulturalFestivalRepository
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.snackbar.Snackbar

class AdminFestivalListFragment : Fragment() {

    private lateinit var festivalAdapter: AdminFestivalAdapter
    private var allFestivals: List<CulturalFestival> = emptyList()
    private var currentSearch: String = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_admin_festival_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val toolbar = view.findViewById<Toolbar>(R.id.toolbar_admin_festivals)
        val rvFestivals = view.findViewById<RecyclerView>(R.id.rv_admin_festivals)
        val etSearch = view.findViewById<EditText>(R.id.et_admin_festivals_search)
        val fabAdd = view.findViewById<ExtendedFloatingActionButton>(R.id.fab_add_festival)

        toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        festivalAdapter = AdminFestivalAdapter(
            onEditClick = { festival ->
                val bundle = bundleOf("festivalId" to festival.id)
                findNavController().navigate(
                    R.id.action_adminFestivalListFragment_to_addEditFestivalFragment,
                    bundle
                )
            },
            onDeleteClick = { festival ->
                confirmDeleteFestival(festival)
            }
        )

        rvFestivals.layoutManager = LinearLayoutManager(requireContext())
        rvFestivals.adapter = festivalAdapter

        fabAdd.setOnClickListener {
            val bundle = bundleOf("festivalId" to null)
            findNavController().navigate(
                R.id.action_adminFestivalListFragment_to_addEditFestivalFragment,
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

        loadFestivals()
    }

    override fun onResume() {
        super.onResume()
        loadFestivals()
    }

    private fun loadFestivals() {
        allFestivals = CulturalFestivalRepository.getFestivals(requireContext())
        filterAndSubmit()
    }

    private fun filterAndSubmit() {
        val view = view ?: return
        val rvFestivals = view.findViewById<RecyclerView>(R.id.rv_admin_festivals)
        val layoutEmpty = view.findViewById<LinearLayout>(R.id.layout_empty_festivals)

        var filtered = allFestivals
        if (currentSearch.isNotEmpty()) {
            val q = currentSearch.lowercase()
            filtered = filtered.filter {
                it.name.lowercase().contains(q) ||
                it.locationName.lowercase().contains(q) ||
                it.category.lowercase().contains(q) ||
                it.historicalBackground.lowercase().contains(q)
            }
        }

        festivalAdapter.submitList(filtered)
        if (filtered.isEmpty()) {
            rvFestivals.visibility = View.GONE
            layoutEmpty.visibility = View.VISIBLE
        } else {
            rvFestivals.visibility = View.VISIBLE
            layoutEmpty.visibility = View.GONE
        }
    }

    private fun confirmDeleteFestival(festival: CulturalFestival) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Delete Festival")
            .setMessage("Are you sure you want to delete \"${festival.name}\"?")
            .setPositiveButton("Delete") { _, _ ->
                CulturalFestivalRepository.deleteFestival(requireContext(), festival.id)
                loadFestivals()
                Snackbar.make(
                    requireView(),
                    "\"${festival.name}\" deleted successfully",
                    Snackbar.LENGTH_SHORT
                ).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
