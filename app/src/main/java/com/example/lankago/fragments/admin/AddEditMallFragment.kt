package com.example.lankago.fragments.admin

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.lankago.R
import com.example.lankago.data.Mall
import com.example.lankago.data.MallDataProvider
import com.example.lankago.repository.AdminRepository
import com.example.lankago.utils.ImagePickerHelper
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch

/**
 * Admin Fragment to Add a New Mall or Edit an Existing Mall.
 * Supports image picker with Firebase Storage / Base64 upload and Realtime Database sync.
 */
class AddEditMallFragment : Fragment() {

    private val adminRepository = AdminRepository()

    private var editingMallId: String? = null
    private var existingMall: Mall? = null
    private var selectedImageUri: Uri? = null

    private lateinit var ivCoverPreview: ImageView
    private lateinit var tvImageStatus: TextView
    private lateinit var etName: TextInputEditText
    private lateinit var etCity: TextInputEditText
    private lateinit var etCategory: TextInputEditText
    private lateinit var etAddress: TextInputEditText
    private lateinit var etHours: TextInputEditText
    private lateinit var etStoreCount: TextInputEditText
    private lateinit var etContact: TextInputEditText
    private lateinit var etRating: TextInputEditText
    private lateinit var etDescription: TextInputEditText
    private lateinit var btnSave: MaterialButton
    private lateinit var pbSave: ProgressBar
    private lateinit var toolbar: Toolbar

    private val imagePickerHelper = ImagePickerHelper(this) { uri ->
        selectedImageUri = uri
        ivCoverPreview.setImageURI(uri)
        tvImageStatus.text = "New image selected from device"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_add_edit_mall, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        toolbar = view.findViewById(R.id.toolbar_add_edit_mall)
        ivCoverPreview = view.findViewById(R.id.iv_mall_cover_preview)
        tvImageStatus = view.findViewById(R.id.tv_mall_image_status)
        etName = view.findViewById(R.id.et_mall_name)
        etCity = view.findViewById(R.id.et_mall_city)
        etCategory = view.findViewById(R.id.et_mall_category)
        etAddress = view.findViewById(R.id.et_mall_address)
        etHours = view.findViewById(R.id.et_mall_hours)
        etStoreCount = view.findViewById(R.id.et_mall_store_count)
        etContact = view.findViewById(R.id.et_mall_contact)
        etRating = view.findViewById(R.id.et_mall_rating)
        etDescription = view.findViewById(R.id.et_mall_description)
        btnSave = view.findViewById(R.id.btn_save_mall)
        pbSave = view.findViewById(R.id.pb_save_mall)

        val btnSelectImage = view.findViewById<MaterialButton>(R.id.btn_select_mall_image)

        toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        editingMallId = arguments?.getString("mallId")

        if (editingMallId.isNullOrBlank()) {
            toolbar.title = "Add Shopping Mall"
            btnSave.text = "Add Shopping Mall"
        } else {
            toolbar.title = "Edit Shopping Mall"
            btnSave.text = "Update Shopping Mall"
            loadExistingMall(editingMallId!!)
        }

        btnSelectImage.setOnClickListener {
            imagePickerHelper.showImageSourceDialog("Select Mall Image")
        }

        btnSave.setOnClickListener {
            validateAndSubmit()
        }
    }

    private fun loadExistingMall(mallId: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            val liveMalls = try {
                adminRepository.getAllMallsOnce()
            } catch (_: Exception) {
                emptyList()
            }

            existingMall = liveMalls.find { it.id == mallId }
                ?: MallDataProvider.defaultMalls.find { it.id == mallId }

            existingMall?.let { mall ->
                populateFields(mall)
            }
        }
    }

    private fun populateFields(mall: Mall) {
        etName.setText(mall.name)
        etCity.setText(mall.city)
        etCategory.setText(mall.category)
        etAddress.setText(mall.address)
        etHours.setText(mall.operatingHours)
        etStoreCount.setText(mall.storeCount.toString())
        etContact.setText(mall.contactNumber)
        etRating.setText(mall.rating.toString())
        etDescription.setText(mall.description)

        if (mall.imageUrl.isNotBlank()) {
            Glide.with(this)
                .load(mall.imageUrl)
                .placeholder(R.drawable.product)
                .centerCrop()
                .into(ivCoverPreview)
            tvImageStatus.text = "Current cover photo loaded"
        } else if (mall.imageRes != 0) {
            ivCoverPreview.setImageResource(mall.imageRes)
            tvImageStatus.text = "Default artwork"
        }
    }

    private fun validateAndSubmit() {
        val name = etName.text?.toString()?.trim().orEmpty()
        val city = etCity.text?.toString()?.trim().orEmpty().ifBlank { "Colombo" }
        val category = etCategory.text?.toString()?.trim().orEmpty().ifBlank { "Mega Mall" }
        val address = etAddress.text?.toString()?.trim().orEmpty()
        val hours = etHours.text?.toString()?.trim().orEmpty().ifBlank { "10:00 AM - 10:00 PM" }
        val storeCountStr = etStoreCount.text?.toString()?.trim().orEmpty()
        val contact = etContact.text?.toString()?.trim().orEmpty().ifBlank { "+94 11 200 0000" }
        val ratingStr = etRating.text?.toString()?.trim().orEmpty()
        val description = etDescription.text?.toString()?.trim().orEmpty()

        if (name.isBlank()) {
            etName.error = "Mall name is required"
            etName.requestFocus()
            return
        }

        if (address.isBlank()) {
            etAddress.error = "Address is required"
            etAddress.requestFocus()
            return
        }

        if (description.isBlank()) {
            etDescription.error = "Description is required"
            etDescription.requestFocus()
            return
        }

        val storeCount = storeCountStr.toIntOrNull() ?: 100
        val rating = ratingStr.toDoubleOrNull() ?: 4.8

        btnSave.isEnabled = false
        btnSave.text = ""
        pbSave.visibility = View.VISIBLE

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                if (editingMallId.isNullOrBlank()) {
                    adminRepository.addMall(
                        name = name,
                        city = city,
                        address = address,
                        description = description,
                        operatingHours = hours,
                        contactNumber = contact,
                        storeCount = storeCount,
                        rating = rating,
                        category = category,
                        imageUri = selectedImageUri
                    )
                } else {
                    val updates = mapOf(
                        "name" to name,
                        "city" to city,
                        "category" to category,
                        "address" to address,
                        "operatingHours" to hours,
                        "storeCount" to storeCount,
                        "contactNumber" to contact,
                        "rating" to rating,
                        "description" to description
                    )
                    adminRepository.updateMall(
                        mallId = editingMallId!!,
                        updates = updates,
                        newImageUri = selectedImageUri
                    )
                }

                Snackbar.make(requireView(), "Mall '$name' saved successfully!", Snackbar.LENGTH_SHORT).show()
                findNavController().navigateUp()
            } catch (e: Exception) {
                btnSave.isEnabled = true
                btnSave.text = "Save Shopping Mall"
                pbSave.visibility = View.GONE
                Snackbar.make(requireView(), "Failed to save: ${e.message}", Snackbar.LENGTH_LONG).show()
            }
        }
    }
}
