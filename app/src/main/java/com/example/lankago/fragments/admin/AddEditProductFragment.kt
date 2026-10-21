package com.example.lankago.fragments.admin

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.lankago.R
import com.example.lankago.data.LocalProduct
import com.example.lankago.repository.AdminRepository
import com.example.lankago.utils.ImagePickerHelper
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch

/**
 * Production-ready Fragment for Adding and Editing Sri Lankan Local Products in LankaGo.
 * Supports:
 * - Cover image gallery picker via ActivityResultContracts.
 * - Firebase Storage photo uploading to `/products/`.
 * - Firebase Realtime Database writes (`setValue` / `updateChildren`) to `/products/{id}`.
 * - Form validation with feedback.
 */
class AddEditProductFragment : Fragment() {

    private val adminRepository = AdminRepository()

    private var editingProductId: String? = null
    private var existingProduct: LocalProduct? = null
    private var selectedImageUri: Uri? = null

    private lateinit var ivCoverPreview: ImageView
    private lateinit var tvImageStatus: TextView
    private lateinit var etName: TextInputEditText
    private lateinit var actCategory: AutoCompleteTextView
    private lateinit var etOrigin: TextInputEditText
    private lateinit var etPrice: TextInputEditText
    private lateinit var etStock: TextInputEditText
    private lateinit var etDescription: TextInputEditText
    private lateinit var btnSave: MaterialButton
    private lateinit var pbSave: ProgressBar
    private lateinit var toolbar: Toolbar

    private val imagePickerHelper = ImagePickerHelper(this) { uri ->
        selectedImageUri = uri
        ivCoverPreview.setImageURI(uri)
        tvImageStatus.text = "New image selected (will upload to Firebase Storage)"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_add_edit_product, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        toolbar = view.findViewById(R.id.toolbar_add_edit_product)
        ivCoverPreview = view.findViewById(R.id.iv_product_cover_preview)
        tvImageStatus = view.findViewById(R.id.tv_product_image_status)
        etName = view.findViewById(R.id.et_product_name)
        actCategory = view.findViewById(R.id.act_product_category)
        etOrigin = view.findViewById(R.id.et_product_origin)
        etPrice = view.findViewById(R.id.et_product_price)
        etStock = view.findViewById(R.id.et_product_stock)
        etDescription = view.findViewById(R.id.et_product_description)
        btnSave = view.findViewById(R.id.btn_save_product)
        pbSave = view.findViewById(R.id.pb_save_product)

        val btnSelectImage = view.findViewById<MaterialButton>(R.id.btn_select_product_image)

        toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        // Setup Categories Dropdown
        val categories = listOf(
            "Ceylon Tea",
            "Spices",
            "Handicrafts",
            "Gems & Jewelry",
            "Batik & Textiles",
            "Ayurvedic & Wellness",
            "Traditional Masks",
            "Souvenirs"
        )
        val categoryAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, categories)
        actCategory.setAdapter(categoryAdapter)

        btnSelectImage.setOnClickListener {
            imagePickerHelper.showImageSourceDialog("Select Product Image")
        }

        // Read arguments to determine if we are in Add or Edit mode
        editingProductId = arguments?.getString("productId")

        if (!editingProductId.isNullOrBlank()) {
            toolbar.title = "Edit Product"
            btnSave.text = "Update Product"
            loadProductDetails(editingProductId!!)
        } else {
            toolbar.title = "Add Local Product"
            btnSave.text = "Save Product to Database"
            actCategory.setText(categories.first(), false)
            etOrigin.setText("Sri Lanka")
        }

        btnSave.setOnClickListener {
            validateAndSubmit()
        }
    }

    private fun loadProductDetails(productId: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val products = adminRepository.getAllProductsOnce()
                val product = products.find { it.id == productId }
                if (product != null) {
                    existingProduct = product
                    populateFields(product)
                } else {
                    Snackbar.make(requireView(), "Product not found", Snackbar.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Snackbar.make(
                    requireView(),
                    "Failed to load details: ${e.localizedMessage ?: "Unknown error"}",
                    Snackbar.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun populateFields(product: LocalProduct) {
        etName.setText(product.name)
        actCategory.setText(product.category, false)
        etOrigin.setText(product.originRegion)
        etPrice.setText(product.priceLkr.toString())
        etStock.setText(product.stockQuantity.toString())
        etDescription.setText(product.description)

        if (product.imageUrl.isNotBlank()) {
            Glide.with(this)
                .load(product.imageUrl)
                .placeholder(R.drawable.ceylon_tea_set)
                .centerCrop()
                .into(ivCoverPreview)
            tvImageStatus.text = "Loaded from Firebase Storage"
        } else if (product.imageRes != 0) {
            ivCoverPreview.setImageResource(product.imageRes)
            tvImageStatus.text = "Default local artwork"
        }
    }

    private fun validateAndSubmit() {
        val name = etName.text?.toString()?.trim().orEmpty()
        val category = actCategory.text?.toString()?.trim().orEmpty()
        val origin = etOrigin.text?.toString()?.trim().orEmpty().ifBlank { "Sri Lanka" }
        val priceStr = etPrice.text?.toString()?.trim().orEmpty()
        val stockStr = etStock.text?.toString()?.trim().orEmpty()
        val description = etDescription.text?.toString()?.trim().orEmpty()

        if (name.isBlank()) {
            etName.error = "Product name is required"
            etName.requestFocus()
            return
        }

        if (category.isBlank()) {
            actCategory.error = "Category is required"
            actCategory.requestFocus()
            return
        }

        val price = priceStr.toDoubleOrNull()
        if (price == null || price <= 0.0) {
            etPrice.error = "Enter a valid price greater than 0"
            etPrice.requestFocus()
            return
        }

        val stock = stockStr.toIntOrNull()
        if (stock == null || stock < 0) {
            etStock.error = "Enter a valid stock number"
            etStock.requestFocus()
            return
        }

        if (description.isBlank()) {
            etDescription.error = "Description is required"
            etDescription.requestFocus()
            return
        }

        // Processing upload and database write
        setFormEnabled(false)

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                if (editingProductId == null) {
                    // Create New Product
                    adminRepository.addProduct(
                        name = name,
                        category = category,
                        description = description,
                        priceLkr = price,
                        originRegion = origin,
                        stockQuantity = stock,
                        imageUri = selectedImageUri,
                        existingImageUrl = ""
                    )
                    Snackbar.make(requireView(), "Product \"$name\" published successfully!", Snackbar.LENGTH_SHORT).show()
                } else {
                    // Update Existing Product
                    val updates = mapOf<String, Any?>(
                        "name" to name,
                        "category" to category,
                        "originRegion" to origin,
                        "priceLkr" to price,
                        "stockQuantity" to stock,
                        "inStock" to (stock > 0),
                        "description" to description
                    )
                    adminRepository.updateProduct(
                        productId = editingProductId!!,
                        updates = updates,
                        newImageUri = selectedImageUri
                    )
                    Snackbar.make(requireView(), "Product \"$name\" updated successfully!", Snackbar.LENGTH_SHORT).show()
                }

                // Return back to Product List
                findNavController().popBackStack()
            } catch (e: Exception) {
                setFormEnabled(true)
                Snackbar.make(
                    requireView(),
                    "Error saving product: ${e.localizedMessage ?: "Network error"}",
                    Snackbar.LENGTH_LONG
                ).setBackgroundTint(requireContext().getColor(R.color.admin_danger)).show()
            }
        }
    }

    private fun setFormEnabled(enabled: Boolean) {
        btnSave.isEnabled = enabled
        pbSave.visibility = if (enabled) View.GONE else View.VISIBLE
        etName.isEnabled = enabled
        actCategory.isEnabled = enabled
        etOrigin.isEnabled = enabled
        etPrice.isEnabled = enabled
        etStock.isEnabled = enabled
        etDescription.isEnabled = enabled
    }
}
