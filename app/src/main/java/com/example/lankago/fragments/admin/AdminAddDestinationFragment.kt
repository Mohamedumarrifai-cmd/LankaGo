package com.example.lankago.fragments.admin

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.lankago.R
import com.example.lankago.utils.ImagePickerHelper
import com.example.lankago.utils.LankaGoDataManager
import com.example.lankago.utils.Place

class AdminAddDestinationFragment : Fragment() {

    private var selectedImageUri: Uri? = null

    private val imagePickerHelper = ImagePickerHelper(this) { uri ->
        selectedImageUri = uri
        val view = view ?: return@ImagePickerHelper
        val ivPreview = view.findViewById<ImageView>(R.id.iv_dest_cover_preview)
        val placeholder = view.findViewById<LinearLayout>(R.id.layout_dest_placeholder)
        val badge = view.findViewById<TextView>(R.id.tv_dest_retake_badge)

        ivPreview.visibility = View.VISIBLE
        placeholder.visibility = View.GONE
        badge.visibility = View.VISIBLE

        Glide.with(this)
            .load(uri)
            .centerCrop()
            .into(ivPreview)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.admin_add_destination, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<ImageView>(R.id.btn_back)?.setOnClickListener {
            findNavController().navigateUp()
        }

        val cardUpload = view.findViewById<View>(R.id.card_upload_destination_photo)
        val etName = view.findViewById<EditText>(R.id.et_dest_name)
        val etLocation = view.findViewById<EditText>(R.id.et_dest_location)
        val btnPublish = view.findViewById<AppCompatButton>(R.id.btn_publish_destination)

        // Trigger Camera / Gallery Chooser Dialog
        cardUpload?.setOnClickListener {
            imagePickerHelper.showImageSourceDialog("Select Destination Cover Photo")
        }

        btnPublish?.setOnClickListener {
            val name = etName?.text?.toString()?.trim() ?: ""
            val location = etLocation?.text?.toString()?.trim() ?: "Sri Lanka"

            if (name.isEmpty()) {
                etName?.error = "Please enter destination name"
                etName?.requestFocus()
                return@setOnClickListener
            }

            val imageUriString = selectedImageUri?.toString()
                ?: "android.resource://com.example.lankago/drawable/sigiriya"

            // Create new Place
            val newPlace = Place(
                id = "place_${System.currentTimeMillis()}",
                name = name,
                category = "Heritage",
                location = if (location.isNotEmpty()) location else "Sri Lanka",
                imageUri = imageUriString,
                description = "Newly published destination in Sri Lanka. Experience its cultural richness and natural beauty.",
                rating = "4.8",
                entranceFee = "Free"
            )

            // Save persistently via SharedPreferences & Gson
            LankaGoDataManager.addPlace(requireContext(), newPlace)

            Toast.makeText(requireContext(), "Destination published successfully!", Toast.LENGTH_SHORT).show()
            findNavController().navigateUp()
        }
    }
}
