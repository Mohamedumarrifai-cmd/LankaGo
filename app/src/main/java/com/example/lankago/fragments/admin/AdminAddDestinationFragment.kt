package com.example.lankago.fragments.admin

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.lankago.R
import com.example.lankago.utils.LankaGoDataManager
import com.example.lankago.utils.Place
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AdminAddDestinationFragment : Fragment() {

    private var photoUri: Uri? = null
    private var ivCapturedImage: ImageView? = null
    private var layoutPlaceholder: View? = null

    // 1. Camera Activity Result Contract
    private val takePictureLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { isSuccess ->
        if (isSuccess && photoUri != null) {
            ivCapturedImage?.setImageURI(null) // Clear cache
            ivCapturedImage?.setImageURI(photoUri)
            ivCapturedImage?.visibility = View.VISIBLE
            layoutPlaceholder?.visibility = View.GONE
            Toast.makeText(requireContext(), "Photo captured successfully!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(requireContext(), "Camera capture cancelled", Toast.LENGTH_SHORT).show()
        }
    }

    // 2. Camera Runtime Permission Contract
    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            launchCamera()
        } else {
            Toast.makeText(requireContext(), "Camera permission is required to capture destination photos", Toast.LENGTH_LONG).show()
        }
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

        // Back button
        view.findViewById<View>(R.id.btn_back_add_dest)?.setOnClickListener {
            findNavController().navigateUp()
        }

        // Initialize UI Views
        ivCapturedImage = view.findViewById(R.id.ivCapturedImage)
        layoutPlaceholder = view.findViewById(R.id.layout_placeholder)
        val cardCameraTrigger = view.findViewById<View>(R.id.card_camera_trigger)

        val etName = view.findViewById<EditText>(R.id.et_place_name)
        val etCategory = view.findViewById<EditText>(R.id.et_place_category)
        val etLocation = view.findViewById<EditText>(R.id.et_place_location)
        val etDescription = view.findViewById<EditText>(R.id.et_place_description)
        val btnPublish = view.findViewById<AppCompatButton>(R.id.btn_publish_destination)

        // Launch Camera on tapping the image card or image view
        cardCameraTrigger?.setOnClickListener {
            checkPermissionAndLaunchCamera()
        }
        ivCapturedImage?.setOnClickListener {
            checkPermissionAndLaunchCamera()
        }

        // Publish Destination Click
        btnPublish?.setOnClickListener {
            val name = etName?.text?.toString()?.trim() ?: ""
            val category = etCategory?.text?.toString()?.trim() ?: "General"
            val location = etLocation?.text?.toString()?.trim() ?: "Sri Lanka"
            val description = etDescription?.text?.toString()?.trim() ?: ""

            if (name.isEmpty()) {
                etName?.error = "Please enter destination name"
                etName?.requestFocus()
                return@setOnClickListener
            }

            // Image URI from captured photo or fallback to bundled sample image
            val finalImageUri = photoUri?.toString()
                ?: "android.resource://${requireContext().packageName}/drawable/sigiriya"

            // Create new Place
            val newPlace = Place(
                id = "place_${System.currentTimeMillis()}",
                name = name,
                category = category,
                location = location,
                imageUri = finalImageUri,
                description = description
            )

            // Add to singleton list (Prepend so it appears at top)
            LankaGoDataManager.placesList.add(0, newPlace)

            Toast.makeText(requireContext(), "Destination Published!", Toast.LENGTH_SHORT).show()
            findNavController().navigateUp()
        }
    }

    private fun checkPermissionAndLaunchCamera() {
        val cameraPermission = Manifest.permission.CAMERA
        if (ContextCompat.checkSelfPermission(requireContext(), cameraPermission) == PackageManager.PERMISSION_GRANTED) {
            launchCamera()
        } else {
            requestPermissionLauncher.launch(cameraPermission)
        }
    }

    private fun launchCamera() {
        try {
            val photoFile = createImageFile()
            val authority = "${requireContext().packageName}.fileprovider"
            val uri = FileProvider.getUriForFile(requireContext(), authority, photoFile)
            photoUri = uri
            takePictureLauncher.launch(uri)
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Error opening camera: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "LANKAGO_IMG_${timeStamp}_"
        val storageDir = requireContext().cacheDir
        return File.createTempFile(fileName, ".jpg", storageDir)
    }
}
