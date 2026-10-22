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
import androidx.navigation.fragment.findNavController
import com.example.lankago.R
import com.example.lankago.utils.safeNavigate

/**
 * Fragment rendering the Local Products showcase screen matching local_products.xml.
 * Features:
 * - Top header with Back navigation, Title, and Gallery icon
 * - Category filter chips ("Arts & Crafts", "Products", "Markets", "Medicines")
 * - 2-Column Product Grid with instant SafeArgs navigation to LocalItemDetailsFragment
 * - Bottom Navigation for seamless cross-module routing
 */
class LocalProductsFragment : Fragment() {

    private lateinit var tabArtsCrafts: TextView
    private lateinit var tabProducts: TextView
    private lateinit var tabMarkets: TextView
    private lateinit var tabMedicines: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.local_products, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupTopBar(view)
        setupCategoryTabs(view)
        setupProductCards(view)
        setupBottomNavigation(view)
    }

    private fun setupTopBar(view: View) {
        view.findViewById<View>(R.id.btn_back_local_products)?.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupCategoryTabs(view: View) {
        tabArtsCrafts = view.findViewById(R.id.tab_arts_crafts)
        tabProducts = view.findViewById(R.id.tab_products)
        tabMarkets = view.findViewById(R.id.tab_markets)
        tabMedicines = view.findViewById(R.id.tab_medicines)

        val tabs = listOf(tabArtsCrafts, tabProducts, tabMarkets, tabMedicines)

        fun selectTab(selectedTab: TextView, category: String) {
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
            filterProductsByCategory(view, category)
        }

        tabArtsCrafts.setOnClickListener { selectTab(tabArtsCrafts, "Arts & Crafts") }
        tabProducts.setOnClickListener { selectTab(tabProducts, "Products") }
        tabMarkets.setOnClickListener { selectTab(tabMarkets, "Markets") }
        tabMedicines.setOnClickListener { selectTab(tabMedicines, "Medicines") }
    }

    private fun filterProductsByCategory(view: View, category: String) {
        val cardPottery = view.findViewById<CardView>(R.id.card_pottery)
        val cardSpices = view.findViewById<CardView>(R.id.card_spices)
        val cardElephant = view.findViewById<CardView>(R.id.card_elephant)
        val cardTextiles = view.findViewById<CardView>(R.id.card_textiles)
        val cardShellBowls = view.findViewById<CardView>(R.id.card_shell_bowls)
        val cardTeaSet = view.findViewById<CardView>(R.id.card_tea_set)

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

        // 1. Handmade pottery (Trincomalee)
        view.findViewById<CardView>(R.id.card_pottery)?.setOnClickListener {
            openDetail("item_pottery")
        }

        // 2. Sri Lankan spices (Matale)
        view.findViewById<CardView>(R.id.card_spices)?.setOnClickListener {
            openDetail("item_spices")
        }

        // 3. Wooden elephant (Lakmali / Colombo)
        view.findViewById<CardView>(R.id.card_elephant)?.setOnClickListener {
            openDetail("item_elephant")
        }

        // 4. Woven textiles (Embilipitiya / Loomcrafts)
        view.findViewById<CardView>(R.id.card_textiles)?.setOnClickListener {
            openDetail("item_textiles")
        }

        // 5. Coconut shell bowls (Galle)
        view.findViewById<CardView>(R.id.card_shell_bowls)?.setOnClickListener {
            openDetail("item_shell_bowls")
        }

        // 6. Ceylon tea set (Kandy)
        view.findViewById<CardView>(R.id.card_tea_set)?.setOnClickListener {
            openDetail("item_ceylon_tea")
        }
    }

    private fun setupBottomNavigation(view: View) {
        // Home Navigation (pop back to Home or navigate safely)
        view.findViewById<View>(R.id.nav_home)?.setOnClickListener {
            if (findNavController().currentDestination?.id == R.id.homeFragment) return@setOnClickListener
            val popped = findNavController().popBackStack(R.id.homeFragment, false)
            if (!popped && findNavController().currentDestination?.id == R.id.localProductsFragment) {
                findNavController().safeNavigate(R.id.action_localProductsFragment_to_homeFragment)
            }
        }

        // Products Navigation (Already on products screen - scroll to top and reset tab)
        view.findViewById<View>(R.id.nav_products)?.setOnClickListener {
            view.findViewById<androidx.core.widget.NestedScrollView>(R.id.scroll_products)?.smoothScrollTo(0, 0)
            if (::tabArtsCrafts.isInitialized) {
                tabArtsCrafts.performClick()
            }
        }

        // Maps Navigation
        view.findViewById<View>(R.id.nav_maps)?.setOnClickListener {
            findNavController().safeNavigate(R.id.action_localProductsFragment_to_mapRouteFragment)
        }

        // Profile Navigation
        view.findViewById<View>(R.id.nav_profile)?.setOnClickListener {
            findNavController().safeNavigate(R.id.action_localProductsFragment_to_profileFragment)
        }
    }
}