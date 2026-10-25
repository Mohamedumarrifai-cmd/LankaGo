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
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.lankago.R
import com.example.lankago.data.CulturalFestival
import com.example.lankago.repository.CulturalFestivalRepository
import com.example.lankago.utils.ImagePickerHelper
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText

class AddEditFestivalFragment : Fragment() {

    private var festivalId: String? = null
    private var existingFestival: CulturalFestival? = null
    private var selectedImageUri: Uri? = null
    private var existingImageUri: String = ""
    private var existingBannerRes: Int = 0

    private val imagePickerHelper = ImagePickerHelper(this) { uri ->
        selectedImageUri = uri
        displayPreview(uri.toString(), 0)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        festivalId = arguments?.getString("festivalId")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_add_edit_festival, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val toolbar = view.findViewById<Toolbar>(R.id.toolbar_add_edit_festival)
        val cardImagePicker = view.findViewById<View>(R.id.card_festival_image_picker)
        val etName = view.findViewById<TextInputEditText>(R.id.et_festival_name)
        val etCategory = view.findViewById<TextInputEditText>(R.id.et_festival_category)
        val etDates = view.findViewById<TextInputEditText>(R.id.et_festival_dates)
        val etTime = view.findViewById<TextInputEditText>(R.id.et_festival_time)
        val etLocation = view.findViewById<TextInputEditText>(R.id.et_festival_location)
        val etCoords = view.findViewById<TextInputEditText>(R.id.et_festival_coords)
        val etRating = view.findViewById<TextInputEditText>(R.id.et_festival_rating)
        val etDesc = view.findViewById<TextInputEditText>(R.id.et_festival_desc)
        val btnSave = view.findViewById<MaterialButton>(R.id.btn_save_festival)

        toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        cardImagePicker.setOnClickListener {
            imagePickerHelper.showImageSourceDialog("Select Festival Banner Photo")
        }

        if (!festivalId.isNullOrBlank()) {
            toolbar.title = "Edit Festival"
            btnSave.text = "Update Festival"

            existingFestival = CulturalFestivalRepository.getFestivalById(requireContext(), festivalId!!)
            existingFestival?.let { fest ->
                etName.setText(fest.name)
                etCategory.setText(fest.category)
                etDates.setText(fest.dates)
                etTime.setText(fest.time)
                etLocation.setText(fest.locationName)
                etCoords.setText(fest.coordinates)
                etRating.setText(fest.rating.toString())
                etDesc.setText(fest.historicalBackground)
                existingImageUri = fest.imageUrl
                existingBannerRes = fest.bannerRes
                displayPreview(fest.imageUrl, fest.bannerRes)
            }
        } else {
            toolbar.title = "Add Festival / Show"
            btnSave.text = "Publish Festival"
        }

        btnSave.setOnClickListener {
            val name = etName.text?.toString()?.trim().orEmpty()
            val category = etCategory.text?.toString()?.trim().orEmpty()
            val dates = etDates.text?.toString()?.trim().orEmpty()
            val time = etTime.text?.toString()?.trim().orEmpty()
            val location = etLocation.text?.toString()?.trim().orEmpty()
            val coords = etCoords.text?.toString()?.trim().orEmpty()
            val ratingStr = etRating.text?.toString()?.trim().orEmpty()
            val desc = etDesc.text?.toString()?.trim().orEmpty()

            if (name.isEmpty()) {
                etName.error = "Please enter festival name"
                etName.requestFocus()
                return@setOnClickListener
            }
            if (dates.isEmpty()) {
                etDates.error = "Please enter dates"
                etDates.requestFocus()
                return@setOnClickListener
            }
            if (location.isEmpty()) {
                etLocation.error = "Please enter location"
                etLocation.requestFocus()
                return@setOnClickListener
            }
            if (desc.isEmpty()) {
                etDesc.error = "Please enter background details"
                etDesc.requestFocus()
                return@setOnClickListener
            }

            val finalImageUri = selectedImageUri?.toString()
                ?: if (existingImageUri.isNotBlank()) existingImageUri else ""
            val finalBannerRes = if (finalImageUri.isNotBlank()) 0 else (if (existingBannerRes != 0) existingBannerRes else R.drawable.kandy_esala)
            val parsedRating = ratingStr.toFloatOrNull() ?: 4.8f

            if (existingFestival != null) {
                val updated = existingFestival!!.copy(
                    name = name,
                    category = if (category.isNotBlank()) category else "Cultural Celebration",
                    dates = dates,
                    time = if (time.isNotBlank()) time else "All Day",
                    locationName = location,
                    coordinates = if (coords.isNotBlank()) coords else "Sri Lanka",
                    historicalBackground = desc,
                    rating = parsedRating,
                    imageUrl = finalImageUri,
                    bannerRes = finalBannerRes
                )
                CulturalFestivalRepository.updateFestival(requireContext(), updated)
                Toast.makeText(requireContext(), "\"$name\" updated successfully!", Toast.LENGTH_SHORT).show()
            } else {
                val newFest = CulturalFestival(
                    id = "fest_${System.currentTimeMillis()}",
                    name = name,
                    category = if (category.isNotBlank()) category else "Cultural Celebration",
                    dates = dates,
                    time = if (time.isNotBlank()) time else "All Day",
                    locationName = location,
                    coordinates = if (coords.isNotBlank()) coords else "Sri Lanka",
                    historicalBackground = desc,
                    rating = parsedRating,
                    reviewCount = 50,
                    imageUrl = finalImageUri,
                    bannerRes = finalBannerRes
                )
                CulturalFestivalRepository.addFestival(requireContext(), newFest)
                Toast.makeText(requireContext(), "\"$name\" published successfully!", Toast.LENGTH_SHORT).show()
            }

            findNavController().navigateUp()
        }
    }

    private fun displayPreview(imageUriString: String, bannerRes: Int) {
        val view = view ?: return
        val ivPreview = view.findViewById<ImageView>(R.id.iv_festival_cover_preview)
        val placeholder = view.findViewById<LinearLayout>(R.id.layout_festival_placeholder)
        val badge = view.findViewById<TextView>(R.id.tv_festival_retake_badge)

        ivPreview.visibility = View.VISIBLE
        placeholder.visibility = View.GONE
        badge.visibility = View.VISIBLE

        when {
            imageUriString.isNotBlank() -> {
                try {
                    val resName = imageUriString.substringAfterLast("/")
                    val resId = requireContext().resources.getIdentifier(resName, "drawable", requireContext().packageName)
                    if (resId != 0) {
                        Glide.with(this).load(resId).centerCrop().into(ivPreview)
                    } else {
                        Glide.with(this).load(imageUriString).centerCrop().placeholder(R.drawable.kandy_esala).into(ivPreview)
                    }
                } catch (_: Exception) {
                    ivPreview.setImageResource(R.drawable.kandy_esala)
                }
            }
            bannerRes != 0 -> {
                Glide.with(this).load(bannerRes).centerCrop().into(ivPreview)
            }
            else -> {
                ivPreview.setImageResource(R.drawable.kandy_esala)
            }
        }
    }
}
