package com.example.lankago.fragments.admin

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.lankago.R
import com.example.lankago.data.Destination
import com.example.lankago.data.LankaGoDataManager
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AdminAddDestinationFragment : Fragment() {

    // Camera Uri and Path Tracking
    private var photoUri: Uri? = null
    private var currentPhotoPath: String? = null

    // UI references
    private var imgPreview: ImageView? = null
    private var layoutPlaceholder: View? = null
    private var btnRetake: TextView? = null

    // 1. Modern Camera Result Launcher: Takes full-resolution picture into provided Uri
    private val takePictureLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { isSuccess ->
        if (isSuccess && photoUri != null) {
            // Display captured image in ImageView
            imgPreview?.setImageURI(null) // Reset cache
            imgPreview?.setImageURI(photoUri)
            imgPreview?.visibility = View.VISIBLE
            layoutPlaceholder?.visibility = View.GONE
            btnRetake?.visibility = View.VISIBLE

            Toast.makeText(requireContext(), "Destination photo captured successfully!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(requireContext(), "Camera capture cancelled", Toast.LENGTH_SHORT).show()
        }
    }

    // 2. Camera Permission Request Launcher
    private val requestCameraPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            launchCamera()
        } else {
            Toast.makeText(
                requireContext(),
                "Camera permission is required to capture photos",
                Toast.LENGTH_LONG
            ).show()
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

        // Back button navigation
        view.findViewById<View>(R.id.btn_back_add_destination)?.setOnClickListener {
            findNavController().navigateUp()
        }

        // Initialize UI Elements
        imgPreview = view.findViewById(R.id.img_cover_preview)
        layoutPlaceholder = view.findViewById(R.id.layout_camera_placeholder)
        btnRetake = view.findViewById(R.id.btn_retake_photo)

        val cardUpload = view.findViewById<View>(R.id.card_upload_image)
        val etName = view.findViewById<EditText>(R.id.et_destination_name)
        val actCategory = view.findViewById<AutoCompleteTextView>(R.id.act_category)
        val etLocation = view.findViewById<EditText>(R.id.et_location)
        val etFee = view.findViewById<EditText>(R.id.et_entrance_fee)
        val etDescription = view.findViewById<EditText>(R.id.et_description)
        val btnPublish = view.findViewById<AppCompatButton>(R.id.btn_publish_destination)

        // Setup Category Exposed Dropdown
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

        // Trigger Camera when tapping upload card
        cardUpload?.setOnClickListener {
            checkCameraPermissionAndLaunch()
        }

        // Trigger Camera from Retake Badge
        btnRetake?.setOnClickListener {
            checkCameraPermissionAndLaunch()
        }

        // Publish to App Click ("Admin adds here, User sees there")
        btnPublish?.setOnClickListener {
            val destinationName = etName?.text?.toString()?.trim() ?: ""
            val category = actCategory?.text?.toString()?.trim() ?: "Heritage & History"
            val location = etLocation?.text?.toString()?.trim() ?: ""
            val fee = etFee?.text?.toString()?.trim() ?: "Free"
            val description = etDescription?.text?.toString()?.trim() ?: ""

            if (destinationName.isEmpty()) {
                etName?.error = "Please enter destination name"
                etName?.requestFocus()
                return@setOnClickListener
            }

            // Create Destination entity with captured image URI
            val newDestination = Destination(
                id = "dest_${System.currentTimeMillis()}",
                name = destinationName,
                category = category,
                location = location,
                entranceFee = fee,
                description = description,
                imageUri = photoUri?.toString() ?: "android.resource://${requireContext().packageName}/drawable/sigiriya",
                isApproved = true
            )

            // Save to Local Data Manager singleton so User app can read it
            LankaGoDataManager.addDestination(newDestination)

            Toast.makeText(
                requireContext(),
                "Destination \"$destinationName\" published to User App successfully!",
                Toast.LENGTH_LONG
            ).show()

            // Navigate back
            findNavController().navigateUp()
        }
    }

    /**
     * Checks CAMERA runtime permission. If granted, launches camera; otherwise requests permission.
     */
    private fun checkCameraPermissionAndLaunch() {
        val permission = Manifest.permission.CAMERA
        if (ContextCompat.checkSelfPermission(requireContext(), permission) == PackageManager.PERMISSION_GRANTED) {
            launchCamera()
        } else {
            requestCameraPermissionLauncher.launch(permission)
        }
    }

    /**
     * Creates a temporary image file and launches the Camera contract with FileProvider Uri.
     */
    private fun launchCamera() {
        try {
            val photoFile = createImageFile()
            val authority = "${requireContext().packageName}.fileprovider"
            val uri = FileProvider.getUriForFile(requireContext(), authority, photoFile)
            photoUri = uri
            takePictureLauncher.launch(uri)
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Failed to open camera: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Creates a unique JPEG file in app's internal cache directory.
     */
    private fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "LANKAGO_COVER_${timeStamp}_"
        val storageDir = requireContext().cacheDir
        return File.createTempFile(fileName, ".jpg", storageDir).apply {
            currentPhotoPath = absolutePath
        }
    }
}
