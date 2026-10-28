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
import com.example.lankago.data.TraditionalActivity
import com.example.lankago.repository.AdminRepository
import com.example.lankago.utils.ImagePickerHelper
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch


 // Production-ready Fragment for Adding and Editing Cultural Activities, Workshops, and Tours in LankaGo.

class AddEditActivityFragment : Fragment() {

    private val adminRepository = AdminRepository()

    private var editingActivityId: String? = null
    private var existingActivity: TraditionalActivity? = null
    private var selectedImageUri: Uri? = null

    private lateinit var ivCoverPreview: ImageView
    private lateinit var tvImageStatus: TextView
    private lateinit var etTitle: TextInputEditText
    private lateinit var actCategory: AutoCompleteTextView
    private lateinit var etLocation: TextInputEditText
    private lateinit var etDuration: TextInputEditText
    private lateinit var etAddress: TextInputEditText
    private lateinit var etPrice: TextInputEditText
    private lateinit var etHost: TextInputEditText
    private lateinit var etHighlights: TextInputEditText
    private lateinit var etTimeSlots: TextInputEditText
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
        return inflater.inflate(R.layout.fragment_add_edit_activity, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        toolbar = view.findViewById(R.id.toolbar_add_edit_activity)
        ivCoverPreview = view.findViewById(R.id.iv_activity_cover_preview)
        tvImageStatus = view.findViewById(R.id.tv_activity_image_status)
        etTitle = view.findViewById(R.id.et_activity_title)
        actCategory = view.findViewById(R.id.act_activity_category)
        etLocation = view.findViewById(R.id.et_activity_location)
        etDuration = view.findViewById(R.id.et_activity_duration)
        etAddress = view.findViewById(R.id.et_activity_address)
        etPrice = view.findViewById(R.id.et_activity_price)
        etHost = view.findViewById(R.id.et_activity_host)
        etHighlights = view.findViewById(R.id.et_activity_highlights)
        etTimeSlots = view.findViewById(R.id.et_activity_time_slots)
        etDescription = view.findViewById(R.id.et_activity_description)
        btnSave = view.findViewById(R.id.btn_save_activity)
        pbSave = view.findViewById(R.id.pb_save_activity)

        val btnSelectImage = view.findViewById<MaterialButton>(R.id.btn_select_activity_image)

        toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        // Setup Categories Dropdown
        val categories = listOf(
            "Artisan Workshop",
            "Cultural Tour",
            "Traditional Dance",
            "Cooking Masterclass",
            "Wildlife Safari",
            "Hiking & Nature",
            "Tea Experience",
            "Ayurvedic Workshop"
        )
        val categoryAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, categories)
        actCategory.setAdapter(categoryAdapter)

        btnSelectImage.setOnClickListener {
            imagePickerHelper.showImageSourceDialog("Select Activity Image")
        }

        // Read arguments to determine if we are in Add or Edit mode
        editingActivityId = arguments?.getString("activityId")

        if (!editingActivityId.isNullOrBlank()) {
            toolbar.title = "Edit Activity"
            btnSave.text = "Update Activity"
            loadActivityDetails(editingActivityId!!)
        } else {
            toolbar.title = "Add Activity & Tour"
            btnSave.text = "Save Activity to Database"
            actCategory.setText(categories.first(), false)
            etDuration.setText("2 Hours")
            etTimeSlots.setText("09:00 AM, 02:00 PM")
            etHighlights.setText("English Guide, Traditional Refreshments, Souvenir Included")
        }

        btnSave.setOnClickListener {
            validateAndSubmit()
        }
    }

    private fun loadActivityDetails(activityId: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val activities = adminRepository.getAllActivitiesOnce()
                val activity = activities.find { it.id == activityId }
                if (activity != null) {
                    existingActivity = activity
                    populateFields(activity)
                } else {
                    Snackbar.make(requireView(), "Activity not found", Snackbar.LENGTH_LONG).show()
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

    private fun populateFields(activity: TraditionalActivity) {
        etTitle.setText(activity.title)
        actCategory.setText(activity.category, false)
        etLocation.setText(activity.location)
        etDuration.setText(activity.duration)
        etAddress.setText(activity.address)
        etPrice.setText(activity.priceLkr.toString())
        etHost.setText(activity.hostName)
        etHighlights.setText(activity.highlights.joinToString(", "))
        etTimeSlots.setText(activity.timeSlots.joinToString(", "))
        etDescription.setText(activity.description)

        if (activity.imageUrl.isNotBlank()) {
            Glide.with(this)
                .load(activity.imageUrl)
                .placeholder(R.drawable.traditional_dance)
                .centerCrop()
                .into(ivCoverPreview)
            tvImageStatus.text = "Loaded from Firebase Storage"
        } else if (activity.imageRes != 0) {
            ivCoverPreview.setImageResource(activity.imageRes)
            tvImageStatus.text = "Default local artwork"
        }
    }

    private fun validateAndSubmit() {
        val title = etTitle.text?.toString()?.trim().orEmpty()
        val category = actCategory.text?.toString()?.trim().orEmpty()
        val location = etLocation.text?.toString()?.trim().orEmpty()
        val duration = etDuration.text?.toString()?.trim().orEmpty().ifBlank { "2 Hours" }
        val address = etAddress.text?.toString()?.trim().orEmpty()
        val priceStr = etPrice.text?.toString()?.trim().orEmpty()
        val host = etHost.text?.toString()?.trim().orEmpty().ifBlank { "Master Guide" }
        val highlightsStr = etHighlights.text?.toString()?.trim().orEmpty()
        val timeSlotsStr = etTimeSlots.text?.toString()?.trim().orEmpty()
        val description = etDescription.text?.toString()?.trim().orEmpty()

        if (title.isBlank()) {
            etTitle.error = "Activity title is required"
            etTitle.requestFocus()
            return
        }

        if (category.isBlank()) {
            actCategory.error = "Category is required"
            actCategory.requestFocus()
            return
        }

        if (location.isBlank()) {
            etLocation.error = "Location is required"
            etLocation.requestFocus()
            return
        }

        if (address.isBlank()) {
            etAddress.error = "Address is required"
            etAddress.requestFocus()
            return
        }

        val price = priceStr.toDoubleOrNull()
        if (price == null || price <= 0.0) {
            etPrice.error = "Enter a valid price per person"
            etPrice.requestFocus()
            return
        }

        if (description.isBlank()) {
            etDescription.error = "Description is required"
            etDescription.requestFocus()
            return
        }

        val highlightsList = highlightsStr.split(",").map { it.trim() }.filter { it.isNotBlank() }
        val timeSlotsList = timeSlotsStr.split(",").map { it.trim() }.filter { it.isNotBlank() }

        // Processing upload and database write
        setFormEnabled(false)

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                if (editingActivityId == null) {
                    // Create New Activity
                    adminRepository.addActivity(
                        title = title,
                        category = category,
                        location = location,
                        address = address,
                        duration = duration,
                        priceLkr = price,
                        hostName = host,
                        description = description,
                        highlights = highlightsList,
                        timeSlots = timeSlotsList,
                        imageUri = selectedImageUri,
                        existingImageUrl = ""
                    )
                    Snackbar.make(requireView(), "Activity \"$title\" published successfully!", Snackbar.LENGTH_SHORT).show()
                } else {
                    // Update Existing Activity
                    val updates = mapOf<String, Any?>(
                        "title" to title,
                        "category" to category,
                        "location" to location,
                        "address" to address,
                        "duration" to duration,
                        "priceLkr" to price,
                        "hostName" to host,
                        "description" to description,
                        "highlights" to highlightsList,
                        "timeSlots" to timeSlotsList
                    )
                    adminRepository.updateActivity(
                        activityId = editingActivityId!!,
                        updates = updates,
                        newImageUri = selectedImageUri
                    )
                    Snackbar.make(requireView(), "Activity \"$title\" updated successfully!", Snackbar.LENGTH_SHORT).show()
                }

                // Return back to Activity List
                findNavController().popBackStack()
            } catch (e: Exception) {
                setFormEnabled(true)
                Snackbar.make(
                    requireView(),
                    "Error saving activity: ${e.localizedMessage ?: "Network error"}",
                    Snackbar.LENGTH_LONG
                ).setBackgroundTint(requireContext().getColor(R.color.admin_danger)).show()
            }
        }
    }

    private fun setFormEnabled(enabled: Boolean) {
        btnSave.isEnabled = enabled
        pbSave.visibility = if (enabled) View.GONE else View.VISIBLE
        etTitle.isEnabled = enabled
        actCategory.isEnabled = enabled
        etLocation.isEnabled = enabled
        etDuration.isEnabled = enabled
        etAddress.isEnabled = enabled
        etPrice.isEnabled = enabled
        etHost.isEnabled = enabled
        etHighlights.isEnabled = enabled
        etTimeSlots.isEnabled = enabled
        etDescription.isEnabled = enabled
    }
}
