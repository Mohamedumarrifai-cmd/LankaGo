package com.example.lankago.fragments.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.lankago.R
import com.example.lankago.utils.LankaGoDataManager
import com.example.lankago.utils.Place

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

        view.findViewById<ImageView>(R.id.btn_back)?.setOnClickListener {
            findNavController().navigateUp()
        }

        val etName = view.findViewById<EditText>(R.id.et_dest_name)
        val etLocation = view.findViewById<EditText>(R.id.et_dest_location)
        val btnPublish = view.findViewById<AppCompatButton>(R.id.btn_publish_destination)

        btnPublish?.setOnClickListener {
            val name = etName?.text?.toString()?.trim() ?: ""
            val location = etLocation?.text?.toString()?.trim() ?: "Sri Lanka"

            if (name.isEmpty()) {
                etName?.error = "Please enter destination name"
                etName?.requestFocus()
                return@setOnClickListener
            }

            // Create new Place
            val newPlace = Place(
                id = "place_${System.currentTimeMillis()}",
                name = name,
                category = "Heritage",
                location = if (location.isNotEmpty()) location else "Sri Lanka",
                imageUri = "android.resource://com.example.lankago/drawable/sigiriya",
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
