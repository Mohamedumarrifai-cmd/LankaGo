package com.example.lankago.fragments.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ProgressBar
import androidx.appcompat.widget.Toolbar
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.lankago.R
import com.example.lankago.adapters.AdminProductAdapter
import com.example.lankago.data.LocalProduct
import com.example.lankago.repository.AdminRepository
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch


 // Production-ready Admin screen displaying all Local Products directly from Firebase Realtime Database.

class AdminProductListFragment : Fragment() {

    private val adminRepository = AdminRepository()
    private lateinit var productAdapter: AdminProductAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_admin_product_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val toolbar = view.findViewById<Toolbar>(R.id.toolbar_admin_products)
        val rvProducts = view.findViewById<RecyclerView>(R.id.rv_admin_products)
        val pbLoading = view.findViewById<ProgressBar>(R.id.pb_products_loading)
        val layoutEmpty = view.findViewById<LinearLayout>(R.id.layout_empty_products)
        val fabAdd = view.findViewById<ExtendedFloatingActionButton>(R.id.fab_add_product)

        toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        // Initialize Adapter with Edit and Delete callbacks
        productAdapter = AdminProductAdapter(
            onEditClick = { product ->
                val bundle = bundleOf("productId" to product.id)
                findNavController().navigate(
                    R.id.action_adminProductListFragment_to_addEditProductFragment,
                    bundle
                )
            },
            onDeleteClick = { product ->
                confirmDeleteProduct(product)
            }
        )

        rvProducts.layoutManager = LinearLayoutManager(requireContext())
        rvProducts.adapter = productAdapter

        // FAB -> Add New Product (SafeArgs productId is null)
        fabAdd.setOnClickListener {
            val bundle = bundleOf("productId" to null)
            findNavController().navigate(
                R.id.action_adminProductListFragment_to_addEditProductFragment,
                bundle
            )
        }

        // Real-time observation of Firebase Realtime Database
        pbLoading.visibility = View.VISIBLE
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                try {
                    adminRepository.observeProducts().collect { products ->
                        pbLoading.visibility = View.GONE
                        if (products.isEmpty()) {
                            loadFallbackProducts(pbLoading, rvProducts, layoutEmpty)
                        } else {
                            rvProducts.visibility = View.VISIBLE
                            layoutEmpty.visibility = View.GONE
                            productAdapter.submitList(products)
                        }
                    }
                } catch (e: Exception) {
                    pbLoading.visibility = View.GONE
                    loadFallbackProducts(pbLoading, rvProducts, layoutEmpty)
                }
            }
        }
    }

    private fun loadFallbackProducts(
        pbLoading: ProgressBar,
        rvProducts: RecyclerView,
        layoutEmpty: LinearLayout
    ) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val onceList = adminRepository.getAllProductsOnce()
                pbLoading.visibility = View.GONE
                val resolved = if (onceList.isNotEmpty()) onceList else getDefaultProducts()
                if (resolved.isEmpty()) {
                    rvProducts.visibility = View.GONE
                    layoutEmpty.visibility = View.VISIBLE
                } else {
                    rvProducts.visibility = View.VISIBLE
                    layoutEmpty.visibility = View.GONE
                    productAdapter.submitList(resolved)
                }
            } catch (_: Exception) {
                pbLoading.visibility = View.GONE
                val fallback = getDefaultProducts()
                if (fallback.isNotEmpty()) {
                    rvProducts.visibility = View.VISIBLE
                    layoutEmpty.visibility = View.GONE
                    productAdapter.submitList(fallback)
                } else {
                    rvProducts.visibility = View.GONE
                    layoutEmpty.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun getDefaultProducts(): List<LocalProduct> {
        return com.example.lankago.data.LocalItemDataProvider.items.map { item ->
            LocalProduct(
                id = item.id,
                name = item.title,
                category = item.category,
                description = item.description,
                priceLkr = 2500.0,
                originRegion = item.famousCity,
                imageUrl = item.imageUrl,
                imageRes = item.imageRes,
                rating = item.rating,
                reviewCount = item.reviewCount
            )
        }
    }

    private fun confirmDeleteProduct(product: LocalProduct) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Delete Product")
            .setMessage("Are you sure you want to permanently remove \"${product.name}\" from the database and storage?")
            .setPositiveButton("Delete") { _, _ ->
                performDelete(product)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun performDelete(product: LocalProduct) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                adminRepository.deleteProduct(productId = product.id, imageUrl = product.imageUrl)
                Snackbar.make(
                    requireView(),
                    "Product \"${product.name}\" removed successfully",
                    Snackbar.LENGTH_SHORT
                ).show()
            } catch (e: Exception) {
                Snackbar.make(
                    requireView(),
                    "Delete failed: ${e.localizedMessage ?: "Unknown error"}",
                    Snackbar.LENGTH_LONG
                ).setBackgroundTint(requireContext().getColor(R.color.admin_danger)).show()
            }
        }
    }
}
