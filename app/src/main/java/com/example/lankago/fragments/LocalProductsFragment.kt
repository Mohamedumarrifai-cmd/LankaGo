package com.example.lankago.fragments

import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.lankago.R
import com.example.lankago.adapters.UserProductAdapter
import com.example.lankago.data.LocalProduct
import com.example.lankago.utils.safeNavigate
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

/**
 * Fragment rendering the Local Products showcase screen matching local_products.xml.
 * Connected directly via ValueEventListener to Firebase Realtime Database node `/products`.
 */
class LocalProductsFragment : Fragment() {

    companion object {
        private const val TAG = "LocalProductsSync"
    }

    private lateinit var userProductAdapter: UserProductAdapter
    private lateinit var databaseRef: DatabaseReference
    private var productsValueEventListener: ValueEventListener? = null

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
        setupFirebaseProductsSync()
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

        rvFirebaseProducts.layoutManager = androidx.recyclerview.widget.GridLayoutManager(requireContext(), 2)
        rvFirebaseProducts.adapter = userProductAdapter
        rvFirebaseProducts.isNestedScrollingEnabled = false
    }

    /**
     * Connects directly to the `/products` node in Firebase Realtime Database.
     * Listens in real-time to any addition, edit, or deletion made by Admin.
     */
    private fun setupFirebaseProductsSync() {
        databaseRef = FirebaseDatabase.getInstance().reference.child("products")

        Log.d(TAG, "Attaching ValueEventListener to Firebase node: ${databaseRef.path}")

        productsValueEventListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d(TAG, "onDataChange triggered. Children count under /products: ${snapshot.childrenCount}")

                val productsList = mutableListOf<LocalProduct>()

                for (child in snapshot.children) {
                    try {
                        var product = child.getValue(LocalProduct::class.java)
                        if (product != null) {
                            if (product.id.isBlank()) {
                                product = product.copy(id = child.key ?: "")
                            }
                            productsList.add(product)
                            Log.d(TAG, "✓ Successfully parsed: '${product.name}' [ID: ${product.id}, Category: '${product.category}', Price: LKR ${product.priceLkr}]")
                        } else {
                            Log.w(TAG, "child.getValue returned null for key '${child.key}'. Attempting manual fallback.")
                            parseProductManually(child)?.let {
                                productsList.add(it)
                                Log.d(TAG, "✓ Manually parsed: '${it.name}' [ID: ${it.id}]")
                            }
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed standard deserialization for child '${child.key}': ${e.message}. Using resilient manual parsing.", e)
                        parseProductManually(child)?.let {
                            productsList.add(it)
                            Log.d(TAG, "✓ Manually recovered: '${it.name}' [ID: ${it.id}]")
                        }
                    }
                }

                allFirebaseProducts = productsList
                Log.d(TAG, "Total products ready for display: ${allFirebaseProducts.size}. Applying tab filter: '$currentCategory'")

                applyFilter(currentCategory)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "onCancelled: Firebase Realtime Database read failed. Code: ${error.code}, Message: ${error.message}", error.toException())
            }
        }

        databaseRef.addValueEventListener(productsValueEventListener as ValueEventListener)
    }

    /**
     * Resilient manual parser guarding against potential type mismatches in Firebase (e.g., price stored as Long or String).
     */
    private fun parseProductManually(child: DataSnapshot): LocalProduct? {
        return try {
            val key = child.key ?: ""
            val name = child.child("name").getValue(String::class.java) ?: return null
            val category = child.child("category").getValue(String::class.java) ?: "General"
            val description = child.child("description").getValue(String::class.java) ?: ""
            val origin = child.child("originRegion").getValue(String::class.java) ?: "Sri Lanka"
            val imageUrl = child.child("imageUrl").getValue(String::class.java) ?: ""
            val inStock = child.child("inStock").getValue(Boolean::class.java) ?: true

            val priceVal = child.child("priceLkr").value
            val priceLkr = when (priceVal) {
                is Number -> priceVal.toDouble()
                is String -> priceVal.toDoubleOrNull() ?: 0.0
                else -> 0.0
            }

            val stockVal = child.child("stockQuantity").value
            val stockQuantity = when (stockVal) {
                is Number -> stockVal.toInt()
                is String -> stockVal.toIntOrNull() ?: 50
                else -> 50
            }

            LocalProduct(
                id = key,
                name = name,
                category = category,
                description = description,
                priceLkr = priceLkr,
                originRegion = origin,
                inStock = inStock,
                stockQuantity = stockQuantity,
                imageUrl = imageUrl
            )
        } catch (ex: Exception) {
            Log.e(TAG, "Manual fallback extraction failed for child ${child.key}: ${ex.message}")
            null
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
                it.category.contains("wood", ignoreCase = true) ||
                it.category.contains("mask", ignoreCase = true) ||
                it.category.contains("gem", ignoreCase = true) ||
                it.category.contains("jewelry", ignoreCase = true) ||
                it.category.contains("souvenir", ignoreCase = true)
            }.ifEmpty { allFirebaseProducts }

            "Products" -> allFirebaseProducts.filter {
                it.category.contains("tea", ignoreCase = true) ||
                it.category.contains("spice", ignoreCase = true) ||
                it.category.contains("product", ignoreCase = true) ||
                it.category.contains("food", ignoreCase = true) ||
                it.category.contains("ayurved", ignoreCase = true) ||
                it.category.contains("wellness", ignoreCase = true)
            }.ifEmpty { allFirebaseProducts }

            "Markets" -> allFirebaseProducts.filter {
                it.category.contains("market", ignoreCase = true) ||
                it.category.contains("bazaar", ignoreCase = true)
            }.ifEmpty { allFirebaseProducts }

            "Medicines" -> allFirebaseProducts.filter {
                it.category.contains("medicine", ignoreCase = true) ||
                it.category.contains("ayurveda", ignoreCase = true) ||
                it.category.contains("herb", ignoreCase = true) ||
                it.category.contains("wellness", ignoreCase = true)
            }.ifEmpty { allFirebaseProducts }

            else -> allFirebaseProducts
        }

        Log.d(TAG, "applyFilter: Selected Tab = '$category'. Filtered items = ${filteredFirebase.size} (Total in memory = ${allFirebaseProducts.size})")

        if (filteredFirebase.isNotEmpty()) {
            tvAdminHeading.visibility = View.VISIBLE
            rvFirebaseProducts.visibility = View.VISIBLE
            // Pass a new ArrayList copy to ensure ListAdapter DiffUtil computes diffs properly
            userProductAdapter.submitList(ArrayList(filteredFirebase)) {
                Log.d(TAG, "Adapter submitList completed successfully. ${filteredFirebase.size} items displayed in RecyclerView.")
            }
        } else {
            tvAdminHeading.visibility = View.GONE
            rvFirebaseProducts.visibility = View.GONE
            userProductAdapter.submitList(emptyList())
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

    override fun onDestroyView() {
        productsValueEventListener?.let { listener ->
            databaseRef.removeEventListener(listener)
            Log.d(TAG, "onDestroyView: Successfully detached ValueEventListener from /products")
        }
        super.onDestroyView()
    }
}