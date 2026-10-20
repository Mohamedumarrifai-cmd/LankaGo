package com.example.lankago.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.lankago.R
import com.example.lankago.adapters.LocalItemListAdapter
import com.example.lankago.viewmodels.LocalProductsViewModel
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch

/**
 * Child tab page fragment hosted inside ViewPager2 in LocalProductsFragment.
 * Observes ViewModel StateFlow and gracefully toggles between the items list
 * and the Material 3 Empty State UI.
 */
class LocalProductTabFragment : Fragment() {

    private val viewModel: LocalProductsViewModel by activityViewModels()
    private lateinit var itemAdapter: LocalItemListAdapter
    private var category: String = CATEGORY_PRODUCTS

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        category = arguments?.getString(ARG_CATEGORY) ?: CATEGORY_PRODUCTS
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_tab_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val rvItems = view.findViewById<RecyclerView>(R.id.rv_tab_items)
        val emptyStateView = view.findViewById<View>(R.id.layout_empty_state)
        val btnEmptyAction = emptyStateView.findViewById<MaterialButton>(R.id.btn_empty_action)

        itemAdapter = LocalItemListAdapter { item ->
            val bundle = bundleOf("itemId" to item.id)
            findNavController().navigate(
                R.id.action_localProductsFragment_to_localItemDetailsFragment,
                bundle
            )
        }

        rvItems.layoutManager = LinearLayoutManager(requireContext())
        rvItems.adapter = itemAdapter

        btnEmptyAction?.setOnClickListener {
            viewModel.loadCultureItems()
        }

        // Observe StateFlow from ViewModel
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    val items = when (category) {
                        CATEGORY_MARKETS -> state.markets
                        CATEGORY_MEDICINE -> state.traditionalMedicine
                        else -> state.localProducts
                    }

                    if (items.isEmpty()) {
                        rvItems.visibility = View.GONE
                        emptyStateView.visibility = View.VISIBLE
                    } else {
                        rvItems.visibility = View.VISIBLE
                        emptyStateView.visibility = View.GONE
                        itemAdapter.submitList(items)
                    }
                }
            }
        }
    }

    companion object {
        const val CATEGORY_PRODUCTS = "Local Products"
        const val CATEGORY_MARKETS = "Markets"
        const val CATEGORY_MEDICINE = "Traditional Medicine"
        private const val ARG_CATEGORY = "arg_category"

        fun newInstance(category: String): LocalProductTabFragment {
            return LocalProductTabFragment().apply {
                arguments = bundleOf(ARG_CATEGORY to category)
            }
        }
    }
}
