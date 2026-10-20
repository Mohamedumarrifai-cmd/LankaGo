package com.example.lankago.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.example.lankago.R
import com.example.lankago.adapters.LocalProductsPagerAdapter
import com.example.lankago.viewmodels.LocalProductsViewModel
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

/**
 * Hub fragment for Local Products, Markets, and Traditional Medicine.
 * Integrates TabLayout and ViewPager2 with smooth tab transitions and search filtering.
 */
class LocalProductsFragment : Fragment() {

    private val viewModel: LocalProductsViewModel by activityViewModels()
    private lateinit var pagerAdapter: LocalProductsPagerAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_local_products, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val toolbar = view.findViewById<Toolbar>(R.id.toolbar_local_products)
        val tabLayout = view.findViewById<TabLayout>(R.id.tab_layout_local_products)
        val viewPager = view.findViewById<ViewPager2>(R.id.view_pager_local_products)
        val etSearch = view.findViewById<EditText>(R.id.et_culture_search)

        // Top Toolbar Navigation
        toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        // ViewPager2 & TabLayout Setup
        pagerAdapter = LocalProductsPagerAdapter(this)
        viewPager.adapter = pagerAdapter

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = pagerAdapter.getCategoryTitle(position)
        }.attach()

        // Real-Time Search Filter
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.searchItems(s?.toString().orEmpty())
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }
}