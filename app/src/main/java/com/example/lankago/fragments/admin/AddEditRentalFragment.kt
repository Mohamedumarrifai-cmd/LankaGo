package com.example.lankago.fragments.admin

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.lankago.R
import com.example.lankago.data.RentalShop
import com.example.lankago.repository.AdminRepository
import com.example.lankago.utils.ImagePickerHelper
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.launch



//  1. Photo selection from local gallery.
//  2. Coroutine-based upload of cover image to `/rentals/` in Firebase Storage.
//  3. Saving complete record with lat/lng coordinates to `/rentals/{shopId}` in Realtime Database.
//  4. Backstack pop upon successful operation.
class AddEditRentalFragment : Fragment() {

    private val adminRepository = AdminRepository()

    private var selectedImageUri: Uri? = null
    private var existingShopId: String? = null
    private var existingImageUrl: String = ""

    private val imagePickerHelper = ImagePickerHelper(this) { uri ->
        selectedImageUri = uri
        view?.findViewById<ImageView>(R.id.iv_rental_cover_preview)?.let { ivPreview ->
            Glide.with(this)
                .load(uri)
                .centerCrop()
                .into(ivPreview)
        }
        view?.findViewById<TextView>(R.id.tv_image_status)?.text = "New image selected for upload"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_add_edit_rental, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        existingShopId = arguments?.getString("rentalShopId")

        val toolbar = view.findViewById<Toolbar>(R.id.toolbar_add_edit_rental)
        val btnSelectImage = view.findViewById<MaterialButton>(R.id.btn_select_image)
        val ivPreview = view.findViewById<ImageView>(R.id.iv_rental_cover_preview)
        val btnSave = view.findViewById<MaterialButton>(R.id.btn_save_rental)
        val layoutProgress = view.findViewById<LinearLayout>(R.id.layout_progress)
        val tvUploadStatus = view.findViewById<TextView>(R.id.tv_upload_status)

        val tilName = view.findViewById<TextInputLayout>(R.id.til_shop_name)
        val etName = view.findViewById<TextInputEditText>(R.id.et_shop_name)
        val tilDesc = view.findViewById<TextInputLayout>(R.id.til_shop_description)
        val etDesc = view.findViewById<TextInputEditText>(R.id.et_shop_description)
        val tilHours = view.findViewById<TextInputLayout>(R.id.til_operating_hours)
        val etHours = view.findViewById<TextInputEditText>(R.id.et_operating_hours)
        val tilContact = view.findViewById<TextInputLayout>(R.id.til_contact_number)
        val etContact = view.findViewById<TextInputEditText>(R.id.et_contact_number)
        val tilWhatsapp = view.findViewById<TextInputLayout>(R.id.til_whatsapp_number)
        val etWhatsapp = view.findViewById<TextInputEditText>(R.id.et_whatsapp_number)
        val etCity = view.findViewById<TextInputEditText>(R.id.et_shop_city)
        val etPrice = view.findViewById<TextInputEditText>(R.id.et_price_starting)
        val etAddress = view.findViewById<TextInputEditText>(R.id.et_shop_address)
        val etLat = view.findViewById<TextInputEditText>(R.id.et_shop_lat)
        val etLng = view.findViewById<TextInputEditText>(R.id.et_shop_lng)

        // Setup Toolbar
        toolbar.title = if (existingShopId == null) "Add New Rental Shop" else "Edit Rental Shop"
        toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

        // Image picker trigger (Camera or Gallery)
        btnSelectImage.setOnClickListener {
            imagePickerHelper.showImageSourceDialog("Select Rental Cover Image")
        }

        // Pre-fill fields if in Edit Mode
        if (existingShopId != null) {
            loadExistingRentalData(
                shopId = existingShopId!!,
                etName = etName,
                etDesc = etDesc,
                etHours = etHours,
                etContact = etContact,
                etWhatsapp = etWhatsapp,
                etCity = etCity,
                etPrice = etPrice,
                etAddress = etAddress,
                etLat = etLat,
                etLng = etLng,
                ivPreview = ivPreview
            )
        }

        // Save Button Click
        btnSave.setOnClickListener {
            val name = etName.text?.toString().orEmpty().trim()
            val description = etDesc.text?.toString().orEmpty().trim()
            val hours = etHours.text?.toString().orEmpty().trim()
            val contact = etContact.text?.toString().orEmpty().trim()
            val whatsapp = etWhatsapp.text?.toString().orEmpty().trim()
            val city = etCity.text?.toString().orEmpty().trim().ifEmpty { "Sri Lanka" }
            val price = etPrice.text?.toString().orEmpty().trim().ifEmpty { "LKR 3,500/day" }
            val address = etAddress.text?.toString().orEmpty().trim().ifEmpty { "Sri Lanka" }
            val lat = etLat.text?.toString()?.toDoubleOrNull() ?: 6.9271
            val lng = etLng.text?.toString()?.toDoubleOrNull() ?: 79.8612

            // Validation
            tilName.error = null
            tilDesc.error = null
            tilHours.error = null
            tilContact.error = null
            tilWhatsapp.error = null

            if (name.isEmpty()) {
                tilName.error = "Shop name is required"
                return@setOnClickListener
            }
            if (description.isEmpty()) {
                tilDesc.error = "Overview description is required"
                return@setOnClickListener
            }
            if (hours.isEmpty()) {
                tilHours.error = "Operating hours are required"
                return@setOnClickListener
            }
            if (contact.isEmpty()) {
                tilContact.error = "Contact phone number is required"
                return@setOnClickListener
            }
            if (whatsapp.isEmpty()) {
                tilWhatsapp.error = "WhatsApp number is required"
                return@setOnClickListener
            }

            // Begin Firebase Operation
            btnSave.isEnabled = false
            layoutProgress.visibility = View.VISIBLE
            tvUploadStatus.text = if (selectedImageUri != null) {
                "Uploading cover image to Firebase Storage..."
            } else {
                "Saving rental record to Realtime Database..."
            }

            viewLifecycleOwner.lifecycleScope.launch {
                try {
                    if (existingShopId == null) {
                        // CREATE MODE: Upload image & push to /rentals
                        tvUploadStatus.text = "Uploading image and pushing to /rentals..."
                        adminRepository.addRentalShop(
                            name = name,
                            description = description,
                            operatingHours = hours,
                            contactNumber = contact,
                            whatsappNumber = whatsapp,
                            priceStartingFrom = price,
                            city = city,
                            address = address,
                            lat = lat,
                            lng = lng,
                            imageUri = selectedImageUri,
                            existingImageUrl = existingImageUrl
                        )
                        Toast.makeText(requireContext(), "Rental shop created successfully!", Toast.LENGTH_SHORT).show()
                    } else {
                        // EDIT MODE: Update existing /rentals/{shopId}
                        tvUploadStatus.text = "Updating record in Realtime Database..."
                        val updates = mapOf(
                            "name" to name,
                            "description" to description,
                            "operatingHours" to hours,
                            "contactNumber" to contact,
                            "whatsappNumber" to whatsapp,
                            "priceStartingFrom" to price,
                            "city" to city,
                            "address" to address,
                            "lat" to lat,
                            "lng" to lng
                        )
                        adminRepository.updateRentalShop(
                            shopId = existingShopId!!,
                            updates = updates,
                            newImageUri = selectedImageUri
                        )
                        Toast.makeText(requireContext(), "Rental shop updated successfully!", Toast.LENGTH_SHORT).show()
                    }

                    // Return to previous list screen
                    findNavController().popBackStack()

                } catch (e: Exception) {
                    btnSave.isEnabled = true
                    layoutProgress.visibility = View.GONE
                    Snackbar.make(
                        view,
                        "Firebase Operation Failed: ${e.localizedMessage ?: "Unknown error"}",
                        Snackbar.LENGTH_LONG
                    ).setBackgroundTint(requireContext().getColor(R.color.admin_danger)).show()
                }
            }
        }
    }

    private fun loadExistingRentalData(
        shopId: String,
        etName: TextInputEditText,
        etDesc: TextInputEditText,
        etHours: TextInputEditText,
        etContact: TextInputEditText,
        etWhatsapp: TextInputEditText,
        etCity: TextInputEditText,
        etPrice: TextInputEditText,
        etAddress: TextInputEditText,
        etLat: TextInputEditText,
        etLng: TextInputEditText,
        ivPreview: ImageView
    ) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val allRentals = adminRepository.getAllRentalsOnce()
                val shop = allRentals.find { it.id == shopId }
                if (shop != null) {
                    etName.setText(shop.name)
                    etDesc.setText(shop.description)
                    etHours.setText(shop.operatingHours)
                    etContact.setText(shop.contactNumber)
                    etWhatsapp.setText(shop.whatsappNumber)
                    etCity.setText(shop.city)
                    etPrice.setText(shop.priceStartingFrom)
                    etAddress.setText(shop.address)
                    etLat.setText(shop.lat.toString())
                    etLng.setText(shop.lng.toString())
                    existingImageUrl = shop.imageUrl

                    if (shop.imageUrl.isNotBlank()) {
                        Glide.with(this@AddEditRentalFragment)
                            .load(shop.imageUrl)
                            .placeholder(R.drawable.casons_rent)
                            .centerCrop()
                            .into(ivPreview)
                    }
                }
            } catch (_: Exception) {
                // Ignore load error
            }
        }
    }
}
