package com.example.lankago.fragments

import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.lankago.R
import com.example.lankago.adapters.UserProductAdapter
import com.example.lankago.data.LocalProduct
import com.example.lankago.repository.AdminRepository
import com.example.lankago.utils.safeNavigate
import kotlinx.coroutines.launch


 // Fragment rendering the Local Products showcase screen matching local_products.xml.

class LocalProductsFragment : Fragment() {

    private val adminRepository = AdminRepository()
    private lateinit var userProductAdapter: UserProductAdapter

    private lateinit var tabArtsCrafts: TextView
    private lateinit var tabProducts: TextView
    private lateinit var tabMarkets: TextView
    private lateinit var tabMedicines: TextView

    private lateinit var tvAdminHeading: TextView
    private lateinit var rvFirebaseProducts: RecyclerView

    private var allFirebaseProducts: List<LocalProduct> = emptyList()
    private var currentCategory: String = "Arts & Crafts"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.local_products, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tvAdminHeading = view.findViewById(R.id.tv_admin_products_heading)
        rvFirebaseProducts = view.findViewById(R.id.rv_firebase_products)

        setupTopBar(view)
        setupFirebaseProductsRecyclerView()
        setupCategoryTabs(view)
        setupProductCards(view)
        setupBottomNavigation(view)
        observeLiveProducts()
    }

    private fun setupTopBar(view: View) {
        view.findViewById<View>(R.id.btn_back_local_products)?.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupFirebaseProductsRecyclerView() {
        userProductAdapter = UserProductAdapter { product ->
            val bundle = bundleOf(
                "itemId" to product.id,
                "productId" to product.id,
                "productName" to product.name
            )
            findNavController().safeNavigate(
                R.id.action_localProductsFragment_to_localItemDetailsFragment,
                bundle
            )
        }

        rvFirebaseProducts.layoutManager = LinearLayoutManager(requireContext())
        rvFirebaseProducts.adapter = userProductAdapter
    }

    private fun observeLiveProducts() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                adminRepository.observeProducts().collect { products ->
                    allFirebaseProducts = products
                    applyFilter(currentCategory)
                }
            }
        }
    }

    private fun setupCategoryTabs(view: View) {
        tabArtsCrafts = view.findViewById(R.id.tab_arts_crafts)
        tabProducts = view.findViewById(R.id.tab_products)
        tabMarkets = view.findViewById(R.id.tab_markets)
        tabMedicines = view.findViewById(R.id.tab_medicines)

        val tabs = listOf(tabArtsCrafts, tabProducts, tabMarkets, tabMedicines)

        fun selectTab(selectedTab: TextView, category: String) {
            currentCategory = category
            for (tab in tabs) {
                if (tab == selectedTab) {
                    tab.setBackgroundResource(R.drawable.bg_chip_selected)
                    tab.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                    tab.setTypeface(null, Typeface.BOLD)
                } else {
                    tab.setBackgroundResource(R.drawable.bg_chip_unselected)
                    tab.setTextColor(0xFF666666.toInt())
                    tab.setTypeface(null, Typeface.NORMAL)
                }
            }
            applyFilter(category)
        }

        tabArtsCrafts.setOnClickListener { selectTab(tabArtsCrafts, "Arts & Crafts") }
        tabProducts.setOnClickListener { selectTab(tabProducts, "Products") }
        tabMarkets.setOnClickListener { selectTab(tabMarkets, "Markets") }
        tabMedicines.setOnClickListener { selectTab(tabMedicines, "Medicines") }
    }

    private fun applyFilter(category: String) {
        // 1. Filter Live Firebase Products
        val filteredFirebase = when (category) {
            "Arts & Crafts" -> allFirebaseProducts.filter {
                it.category.contains("craft", ignoreCase = true) ||
                it.category.contains("art", ignoreCase = true) ||
                it.category.contains("batik", ignoreCase = true) ||
                it.category.contains("pottery", ignoreCase = true) ||
                it.category.contains("handicraft", ignoreCase = true) ||
                it.category.contains("wood", ignoreCase = true)
            }.ifEmpty { allFirebaseProducts } // Resilient fallback: show all if specific tag isn't set

            "Products" -> allFirebaseProducts.filter {
                it.category.contains("tea", ignoreCase = true) ||
                it.category.contains("spice", ignoreCase = true) ||
                it.category.contains("product", ignoreCase = true) ||
                it.category.contains("food", ignoreCase = true)
            }.ifEmpty { allFirebaseProducts }

            "Markets" -> allFirebaseProducts.filter {
                it.category.contains("market", ignoreCase = true) ||
                it.category.contains("bazaar", ignoreCase = true)
            }.ifEmpty { allFirebaseProducts }

            "Medicines" -> allFirebaseProducts.filter {
                it.category.contains("medicine", ignoreCase = true) ||
                it.category.contains("ayurveda", ignoreCase = true) ||
                it.category.contains("herb", ignoreCase = true)
            }.ifEmpty { allFirebaseProducts }

            else -> allFirebaseProducts
        }

        if (filteredFirebase.isNotEmpty()) {
            tvAdminHeading.visibility = View.VISIBLE
            rvFirebaseProducts.visibility = View.VISIBLE
            userProductAdapter.submitList(filteredFirebase)
        } else {
            tvAdminHeading.visibility = View.GONE
            rvFirebaseProducts.visibility = View.GONE
        }

        // 2. Filter Static Cultural Heritage Cards
        val cardPottery = view?.findViewById<CardView>(R.id.card_pottery)
        val cardSpices = view?.findViewById<CardView>(R.id.card_spices)
        val cardElephant = view?.findViewById<CardView>(R.id.card_elephant)
        val cardTextiles = view?.findViewById<CardView>(R.id.card_textiles)
        val cardShellBowls = view?.findViewById<CardView>(R.id.card_shell_bowls)
        val cardTeaSet = view?.findViewById<CardView>(R.id.card_tea_set)

        when (category) {
            "Arts & Crafts" -> {
                cardPottery?.visibility = View.VISIBLE
                cardSpices?.visibility = View.VISIBLE
                cardElephant?.visibility = View.VISIBLE
                cardTextiles?.visibility = View.VISIBLE
                cardShellBowls?.visibility = View.VISIBLE
                cardTeaSet?.visibility = View.VISIBLE
            }
            "Products" -> {
                cardPottery?.visibility = View.VISIBLE
                cardSpices?.visibility = View.VISIBLE
                cardElephant?.visibility = View.GONE
                cardTextiles?.visibility = View.GONE
                cardShellBowls?.visibility = View.GONE
                cardTeaSet?.visibility = View.VISIBLE
            }
            "Markets" -> {
                cardPottery?.visibility = View.GONE
                cardSpices?.visibility = View.VISIBLE
                cardElephant?.visibility = View.VISIBLE
                cardTextiles?.visibility = View.VISIBLE
                cardShellBowls?.visibility = View.GONE
                cardTeaSet?.visibility = View.GONE
            }
            "Medicines" -> {
                cardPottery?.visibility = View.GONE
                cardSpices?.visibility = View.VISIBLE
                cardElephant?.visibility = View.GONE
                cardTextiles?.visibility = View.GONE
                cardShellBowls?.visibility = View.GONE
                cardTeaSet?.visibility = View.VISIBLE
            }
        }
    }

    private fun setupProductCards(view: View) {
        fun openDetail(itemId: String) {
            val bundle = bundleOf("itemId" to itemId)
            findNavController().safeNavigate(
                R.id.action_localProductsFragment_to_localItemDetailsFragment,
                bundle
            )
        }

        view.findViewById<CardView>(R.id.card_pottery)?.setOnClickListener { openDetail("item_pottery") }
        view.findViewById<CardView>(R.id.card_spices)?.setOnClickListener { openDetail("item_spices") }
        view.findViewById<CardView>(R.id.card_elephant)?.setOnClickListener { openDetail("item_elephant") }
        view.findViewById<CardView>(R.id.card_textiles)?.setOnClickListener { openDetail("item_textiles") }
        view.findViewById<CardView>(R.id.card_shell_bowls)?.setOnClickListener { openDetail("item_shell_bowls") }
        view.findViewById<CardView>(R.id.card_tea_set)?.setOnClickListener { openDetail("item_ceylon_tea") }
    }

    private fun setupBottomNavigation(view: View) {
        view.findViewById<View>(R.id.nav_home)?.setOnClickListener {
            if (findNavController().currentDestination?.id == R.id.homeFragment) return@setOnClickListener
            val popped = findNavController().popBackStack(R.id.homeFragment, false)
            if (!popped && findNavController().currentDestination?.id == R.id.localProductsFragment) {
                findNavController().safeNavigate(R.id.action_localProductsFragment_to_homeFragment)
            }
        }

        view.findViewById<View>(R.id.nav_products)?.setOnClickListener {
            view.findViewById<androidx.core.widget.NestedScrollView>(R.id.scroll_products)?.smoothScrollTo(0, 0)
            if (::tabArtsCrafts.isInitialized) {
                tabArtsCrafts.performClick()
            }
        }

        view.findViewById<View>(R.id.nav_maps)?.setOnClickListener {
            findNavController().safeNavigate(R.id.action_localProductsFragment_to_mapRouteFragment)
        }

        view.findViewById<View>(R.id.nav_profile)?.setOnClickListener {
            findNavController().safeNavigate(R.id.action_localProductsFragment_to_profileFragment)
        }
    }
}