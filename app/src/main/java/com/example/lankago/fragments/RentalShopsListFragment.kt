package com.example.lankago.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.lankago.R
import com.example.lankago.adapters.RentalShopAdapter
import com.example.lankago.viewmodels.RentalShopsViewModel
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch


 // Dedicated Rental Shops List screen.

class RentalShopsListFragment : Fragment() {

    private val viewModel: RentalShopsViewModel by viewModels()
    private lateinit var shopAdapter: RentalShopAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_rental_shops_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val toolbar = view.findViewById<Toolbar>(R.id.toolbar_rental_shops)
        val rvShops = view.findViewById<RecyclerView>(R.id.rv_rental_shops)
        val etSearch = view.findViewById<EditText>(R.id.et_shops_search)
        val emptyStateView = view.findViewById<View>(R.id.layout_empty_state)
        val btnEmptyAction = emptyStateView.findViewById<MaterialButton>(R.id.btn_empty_action)

        // Top Toolbar Navigation
        toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        // CRITICAL NAVIGATION: Navigate to the separate ShopDetailsFragment passing shopId
        shopAdapter = RentalShopAdapter { shop ->
            val bundle = bundleOf("shopId" to shop.id)
            findNavController().navigate(
                R.id.action_rentalShopsListFragment_to_shopDetailsFragment,
                bundle
            )
        }

        rvShops.layoutManager = LinearLayoutManager(requireContext())
        rvShops.adapter = shopAdapter

        // Real-Time Search Filter
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.searchRentals(s?.toString().orEmpty())
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        val tvEmptyTitle = emptyStateView.findViewById<TextView>(R.id.tv_empty_title)
        val tvEmptySubtitle = emptyStateView.findViewById<TextView>(R.id.tv_empty_subtitle)

        btnEmptyAction?.setOnClickListener {
            etSearch.text?.clear()
            viewModel.loadShops()
        }

        // Observe searchResultsRentals StateFlow from ViewModel
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.searchResultsRentals.collect { rentals ->
                    if (rentals.isEmpty()) {
                        rvShops.visibility = View.GONE
                        emptyStateView.visibility = View.VISIBLE
                        tvEmptyTitle?.text = "No results found"
                        val query = etSearch.text?.toString().orEmpty()
                        tvEmptySubtitle?.text = if (query.isNotBlank()) {
                            "No rental shops matched \"$query\". Try checking for typos or searching another city."
                        } else {
                            "No rental shops currently available."
                        }
                    } else {
                        rvShops.visibility = View.VISIBLE
                        emptyStateView.visibility = View.GONE
                        shopAdapter.submitList(rentals)
                    }
                }
            }
        }
    }
}
