package com.example.lankago.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.io.File


//  Production-ready helper that manages the complete A-Z lifecycle of image selection:
//  - Runtime CAMERA permission request
//  - FileProvider temporary URI creation in internal cache
//  - ActivityResultContracts.TakePicture for direct hardware Camera capture
//  - ActivityResultContracts.GetContent for device Gallery picking
//  - Material 3 option chooser dialog

class ImagePickerHelper(
    private val fragment: Fragment,
    private val onImageSelected: (Uri) -> Unit
) {

    private var currentCameraPhotoUri: Uri? = null

    // 1. Camera Runtime Permission Request
    private val cameraPermissionLauncher: ActivityResultLauncher<String> =
        fragment.registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                launchCamera()
            } else {
                Toast.makeText(
                    fragment.requireContext(),
                    "Camera permission is required to capture photos",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    // 2. Camera Take Picture Launcher
    private val takePictureLauncher: ActivityResultLauncher<Uri> =
        fragment.registerForActivityResult(ActivityResultContracts.TakePicture()) { isSuccess ->
            if (isSuccess) {
                currentCameraPhotoUri?.let { uri ->
                    onImageSelected(uri)
                }
            }
        }

    // 3. Gallery Content Picker Launcher
    private val pickGalleryLauncher: ActivityResultLauncher<String> =
        fragment.registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            if (uri != null) {
                onImageSelected(uri)
            }
        }

    /**
     * Displays a Material dialog allowing user to choose between Camera and Gallery.
     */
    fun showImageSourceDialog(dialogTitle: String = "Select Image Source") {
        val context = fragment.requireContext()
        val options = arrayOf("📸  Take Photo from Camera", "🖼️  Choose from Gallery")

        MaterialAlertDialogBuilder(context)
            .setTitle(dialogTitle)
            .setItems(options) { dialog, which ->
                when (which) {
                    0 -> checkCameraPermissionAndLaunch()
                    1 -> pickGalleryLauncher.launch("image/*")
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    /**
     * Checks if CAMERA permission is granted; if so, opens camera, else requests permission.
     */
    private fun checkCameraPermissionAndLaunch() {
        val context = fragment.requireContext()
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            launchCamera()
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    /**
     * Creates a temporary photo file in app's cache directory and invokes the Camera app.
     */
    private fun launchCamera() {
        val context = fragment.requireContext()
        try {
            val photoFile = File.createTempFile(
                "lankago_capture_${System.currentTimeMillis()}",
                ".jpg",
                context.cacheDir
            )

            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                photoFile
            )

            currentCameraPhotoUri = uri
            takePictureLauncher.launch(uri)
        } catch (e: Exception) {
            Toast.makeText(
                context,
                "Unable to initialize camera storage: ${e.localizedMessage}",
                Toast.LENGTH_LONG
            ).show()
        }
    }
}
