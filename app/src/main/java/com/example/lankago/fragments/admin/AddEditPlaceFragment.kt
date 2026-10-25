package com.example.lankago.fragments.admin

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.lankago.R
import com.example.lankago.utils.ImagePickerHelper
import com.example.lankago.utils.LankaGoDataManager
import com.example.lankago.utils.Place
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText

class AddEditPlaceFragment : Fragment() {

    private var placeId: String? = null
    private var existingPlace: Place? = null
    private var selectedImageUri: Uri? = null
    private var existingImageUri: String = ""

    private val categories = arrayOf("Heritage", "Hills", "Lakes", "Wildlife", "Beach")

    private val imagePickerHelper = ImagePickerHelper(this) { uri ->
        selectedImageUri = uri
        displayPreview(uri.toString())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        placeId = arguments?.getString("placeId")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_add_edit_place, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val toolbar = view.findViewById<Toolbar>(R.id.toolbar_add_edit_place)
        val cardImagePicker = view.findViewById<View>(R.id.card_place_image_picker)
        val etName = view.findViewById<TextInputEditText>(R.id.et_place_name)
        val actvCategory = view.findViewById<AutoCompleteTextView>(R.id.actv_place_category)
        val etLocation = view.findViewById<TextInputEditText>(R.id.et_place_location)
        val etFee = view.findViewById<TextInputEditText>(R.id.et_place_fee)
        val etRating = view.findViewById<TextInputEditText>(R.id.et_place_rating)
        val etDesc = view.findViewById<TextInputEditText>(R.id.et_place_description)
        val btnSave = view.findViewById<MaterialButton>(R.id.btn_save_place)

        toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        // Category dropdown setup
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, categories)
        actvCategory.setAdapter(adapter)

        // Photo picker trigger
        cardImagePicker.setOnClickListener {
            imagePickerHelper.showImageSourceDialog("Select Destination Photo")
        }

        // If editing an existing place, populate fields
        if (!placeId.isNullOrBlank()) {
            toolbar.title = "Edit Destination"
            btnSave.text = "Update Destination"

            existingPlace = LankaGoDataManager.getPlaceById(requireContext(), placeId!!)
            existingPlace?.let { place ->
                etName.setText(place.name)
                actvCategory.setText(place.category, false)
                etLocation.setText(place.location)
                etFee.setText(place.entranceFee)
                etRating.setText(place.rating)
                etDesc.setText(place.description)
                existingImageUri = place.imageUri
                displayPreview(place.imageUri)
            }
        } else {
            toolbar.title = "Add Destination"
            btnSave.text = "Publish Destination"
            actvCategory.setText(categories[0], false)
        }

        btnSave.setOnClickListener {
            val name = etName.text?.toString()?.trim().orEmpty()
            val category = actvCategory.text?.toString()?.trim().orEmpty()
            val location = etLocation.text?.toString()?.trim().orEmpty()
            val fee = etFee.text?.toString()?.trim().orEmpty()
            val rating = etRating.text?.toString()?.trim().orEmpty()
            val desc = etDesc.text?.toString()?.trim().orEmpty()

            if (name.isEmpty()) {
                etName.error = "Please enter destination name"
                etName.requestFocus()
                return@setOnClickListener
            }
            if (location.isEmpty()) {
                etLocation.error = "Please enter location"
                etLocation.requestFocus()
                return@setOnClickListener
            }
            if (desc.isEmpty()) {
                etDesc.error = "Please enter a description"
                etDesc.requestFocus()
                return@setOnClickListener
            }

            val finalImageUri = selectedImageUri?.toString()
                ?: if (existingImageUri.isNotBlank()) existingImageUri else "android.resource://com.example.lankago/drawable/heritage_sigiriya"

            if (existingPlace != null) {
                // Update
                val updated = existingPlace!!.copy(
                    name = name,
                    category = category,
                    location = location,
                    entranceFee = if (fee.isNotBlank()) fee else "Free",
                    rating = if (rating.isNotBlank()) rating else "4.8",
                    description = desc,
                    imageUri = finalImageUri
                )
                LankaGoDataManager.updatePlace(requireContext(), updated)
                Toast.makeText(requireContext(), "\"$name\" updated successfully!", Toast.LENGTH_SHORT).show()
            } else {
                // Add new
                val newPlace = Place(
                    id = "place_${System.currentTimeMillis()}",
                    name = name,
                    category = if (category.isNotBlank()) category else "Heritage",
                    location = location,
                    entranceFee = if (fee.isNotBlank()) fee else "Free",
                    rating = if (rating.isNotBlank()) rating else "4.8",
                    description = desc,
                    imageUri = finalImageUri
                )
                LankaGoDataManager.addPlace(requireContext(), newPlace)
                Toast.makeText(requireContext(), "\"$name\" published successfully!", Toast.LENGTH_SHORT).show()
            }

            findNavController().navigateUp()
        }
    }

    private fun displayPreview(imageUriString: String) {
        val view = view ?: return
        val ivPreview = view.findViewById<ImageView>(R.id.iv_place_cover_preview)
        val placeholder = view.findViewById<LinearLayout>(R.id.layout_place_placeholder)
        val badge = view.findViewById<TextView>(R.id.tv_place_retake_badge)

        ivPreview.visibility = View.VISIBLE
        placeholder.visibility = View.GONE
        badge.visibility = View.VISIBLE

        try {
            val resName = imageUriString.substringAfterLast("/")
            val resId = requireContext().resources.getIdentifier(resName, "drawable", requireContext().packageName)
            if (resId != 0) {
                Glide.with(this)
                    .load(resId)
                    .centerCrop()
                    .into(ivPreview)
            } else {
                Glide.with(this)
                    .load(imageUriString)
                    .centerCrop()
                    .placeholder(R.drawable.heritage_sigiriya)
                    .into(ivPreview)
            }
        } catch (_: Exception) {
            ivPreview.setImageResource(R.drawable.heritage_sigiriya)
        }
    }
}
