package com.example.lankago.fragments.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.lankago.R

class AdminAddDestinationFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.admin_add_destination, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Back button navigation
        view.findViewById<View>(R.id.btn_back_add_destination)?.setOnClickListener {
            findNavController().navigateUp()
        }

        // Setup Category Exposed Dropdown
        val actCategory = view.findViewById<AutoCompleteTextView>(R.id.act_category)
        val categories = arrayOf(
            "Heritage & History",
            "Beach & Coastal",
            "Wildlife & Safari",
            "Hill Country & Nature",
            "Cultural Festivals",
            "Adventure & Hiking"
        )
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, categories)
        actCategory?.setAdapter(adapter)

        // Find Form Input Views
        val etName = view.findViewById<EditText>(R.id.et_destination_name)
        val etLocation = view.findViewById<EditText>(R.id.et_location)
        val etFee = view.findViewById<EditText>(R.id.et_entrance_fee)
        val etDescription = view.findViewById<EditText>(R.id.et_description)
        val tvUploadStatus = view.findViewById<TextView>(R.id.tv_upload_status)
        val cardUploadImage = view.findViewById<View>(R.id.card_upload_image)
        val btnPublish = view.findViewById<AppCompatButton>(R.id.btn_publish_destination)

        // Simulated Image Picker click
        cardUploadImage?.setOnClickListener {
            tvUploadStatus?.text = "Cover Image Selected (sigiriya_banner.jpg)"
            Toast.makeText(requireContext(), "Image selected successfully", Toast.LENGTH_SHORT).show()
        }

        // Publish to App Click
        btnPublish?.setOnClickListener {
            val destinationName = etName?.text?.toString()?.trim() ?: ""

            if (destinationName.isEmpty()) {
                etName?.error = "Please enter destination name"
                etName?.requestFocus()
                return@setOnClickListener
            }

            // Simulate saving to Firebase Firestore ("Add here, Show there")
            Toast.makeText(
                requireContext(),
                "Destination \"$destinationName\" published to User App successfully!",
                Toast.LENGTH_LONG
            ).show()

            // Return back to previous screen
            findNavController().navigateUp()
        }
    }
}
