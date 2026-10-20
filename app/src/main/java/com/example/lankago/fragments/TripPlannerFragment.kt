package com.example.lankago.fragments

import android.app.AlertDialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.cardview.widget.CardView
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.lankago.R
import com.example.lankago.adapters.TripAdapter
import com.example.lankago.data.Trip
import com.example.lankago.utils.TripManager
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import java.io.File

class TripPlannerFragment : Fragment() {

    private lateinit var tripAdapter: TripAdapter
    private lateinit var rvTrips: RecyclerView
    private lateinit var layoutEmptyTrips: View
    private lateinit var fabCreateTrip: ExtendedFloatingActionButton

    // Camera photo capture state
    private var currentPhotoUri: Uri? = null
    private var activeCapturedUri: Uri? = null
    private var activeDialogIv: ImageView? = null
    private var activeDialogPlaceholder: View? = null
    private var activeDialogRetakeBadge: View? = null

    private val takePictureLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success && currentPhotoUri != null) {
            activeCapturedUri = currentPhotoUri
            activeDialogIv?.setImageURI(activeCapturedUri)
            activeDialogIv?.visibility = View.VISIBLE
            activeDialogPlaceholder?.visibility = View.GONE
            activeDialogRetakeBadge?.visibility = View.VISIBLE
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.trip_planner, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews(view)
        setupRecyclerView()
        setupFab()
        loadTrips()
    }

    override fun onResume() {
        super.onResume()
        loadTrips()
    }

    private fun initViews(view: View) {
        view.findViewById<ImageView>(R.id.btn_back)?.setOnClickListener {
            findNavController().navigateUp()
        }

        rvTrips = view.findViewById(R.id.rvTrips)
        layoutEmptyTrips = view.findViewById(R.id.layout_empty_trips)
        fabCreateTrip = view.findViewById(R.id.fab_create_trip)
    }

    private fun setupRecyclerView() {
        tripAdapter = TripAdapter(
            trips = mutableListOf(),
            onTripClick = { trip ->
                showTripItineraryDialog(trip)
            },
            onDeleteClick = { trip ->
                confirmDeleteTrip(trip)
            }
        )

        rvTrips.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = tripAdapter
        }
    }

    private fun setupFab() {
        fabCreateTrip.setOnClickListener {
            showCreateTripDialog()
        }
    }

    private fun loadTrips() {
        val trips = TripManager.getAllTrips(requireContext())
        tripAdapter.updateData(trips)

        if (trips.isEmpty()) {
            layoutEmptyTrips.visibility = View.VISIBLE
            rvTrips.visibility = View.GONE
        } else {
            layoutEmptyTrips.visibility = View.GONE
            rvTrips.visibility = View.VISIBLE
        }
    }

    private fun showCreateTripDialog() {
        // Reset active capture variables
        activeCapturedUri = null
        currentPhotoUri = null

        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_create_trip, null)
        val tilName = dialogView.findViewById<TextInputLayout>(R.id.til_trip_name)
        val etName = dialogView.findViewById<EditText>(R.id.et_trip_name)
        val etDate = dialogView.findViewById<EditText>(R.id.et_trip_date)
        val rgTravelerType = dialogView.findViewById<RadioGroup>(R.id.rg_traveler_type)
        val rbLocal = dialogView.findViewById<RadioButton>(R.id.rb_local)
        val rbForeign = dialogView.findViewById<RadioButton>(R.id.rb_foreign)
        val cardIdCapture = dialogView.findViewById<CardView>(R.id.card_id_capture)
        val ivIdCapture = dialogView.findViewById<ImageView>(R.id.ivIdCapture)
        val layoutPlaceholder = dialogView.findViewById<View>(R.id.layout_id_placeholder)
        val tvIdPlaceholder = dialogView.findViewById<TextView>(R.id.tv_id_capture_placeholder)
        val tvRetakeBadge = dialogView.findViewById<TextView>(R.id.tv_retake_badge)
        val btnCancel = dialogView.findViewById<Button>(R.id.btn_cancel_trip)
        val btnSave = dialogView.findViewById<Button>(R.id.btn_save_trip_dialog)

        // Store dialog view references for camera callback
        activeDialogIv = ivIdCapture
        activeDialogPlaceholder = layoutPlaceholder
        activeDialogRetakeBadge = tvRetakeBadge

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        // Dynamic placeholder text based on traveler type selection
        rgTravelerType.setOnCheckedChangeListener { _, checkedId ->
            if (checkedId == R.id.rb_foreign) {
                tvIdPlaceholder.text = "Tap to capture Foreign Passport"
            } else {
                tvIdPlaceholder.text = "Tap to capture NIC"
            }
        }

        // Camera trigger on tapping capture card or image view
        val captureClickListener = View.OnClickListener {
            launchCameraForIdCapture()
        }
        cardIdCapture.setOnClickListener(captureClickListener)
        ivIdCapture.setOnClickListener(captureClickListener)

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        btnSave.setOnClickListener {
            val name = etName.text?.toString()?.trim() ?: ""
            val date = etDate.text?.toString()?.trim() ?: ""

            // 1. Validate that Trip Name is not empty
            if (name.isEmpty()) {
                tilName.error = "Please enter a trip name"
                return@setOnClickListener
            }
            tilName.error = null

            // 2. Validate that an ID/Passport image is captured
            if (activeCapturedUri == null) {
                val requiredDoc = if (rbForeign.isChecked) "Passport" else "NIC"
                Toast.makeText(
                    requireContext(),
                    "Please capture traveler $requiredDoc before saving",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            // 3. Create the Trip object with travelerType and idImageUri
            val travelerType = if (rbForeign.isChecked) "Foreign" else "Local"
            val newTrip = Trip(
                id = "trip_${System.currentTimeMillis()}",
                tripName = name,
                date = if (date.isNotEmpty()) date else "Flexible dates",
                travelerType = travelerType,
                idImageUri = activeCapturedUri.toString(),
                places = mutableListOf()
            )

            // 4. Save using TripManager (persist in SharedPreferences via Gson)
            TripManager.saveTrip(requireContext(), newTrip)

            Snackbar.make(
                requireView(),
                "Trip \"$name\" Saved Successfully!",
                Snackbar.LENGTH_SHORT
            ).show()

            // 5. Dismiss dialog and refresh the list
            dialog.dismiss()
            loadTrips()
        }

        dialog.show()
    }

    private fun launchCameraForIdCapture() {
        try {
            val tempFile = File.createTempFile("traveler_id_${System.currentTimeMillis()}", ".jpg", requireContext().cacheDir).apply {
                createNewFile()
            }
            val uri = FileProvider.getUriForFile(
                requireContext(),
                "${requireContext().packageName}.fileprovider",
                tempFile
            )
            currentPhotoUri = uri
            takePictureLauncher.launch(uri)
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Camera error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showTripItineraryDialog(trip: Trip) {
        val placesCount = trip.places.size
        val items = mutableListOf<String>()

        items.add("Traveler Verification: ${trip.travelerType} (${if (trip.travelerType.equals("Foreign", true)) "Passport" else "NIC"})")

        if (placesCount > 0) {
            trip.places.forEachIndexed { index, place ->
                items.add("Stop ${index + 1}: ${place.name} (${place.location})")
            }
        } else {
            items.add("No destinations added yet.\nOpen any place in LankaGo and tap 'Add to Trip'!")
        }

        AlertDialog.Builder(requireContext())
            .setTitle(trip.tripName)
            .setItems(items.toTypedArray(), null)
            .setPositiveButton("Close", null)
            .show()
    }

    private fun confirmDeleteTrip(trip: Trip) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Trip")
            .setMessage("Are you sure you want to delete \"${trip.tripName}\"?")
            .setPositiveButton("Delete") { _, _ ->
                TripManager.deleteTrip(requireContext(), trip.id)
                Snackbar.make(
                    requireView(),
                    "Trip Deleted Successfully",
                    Snackbar.LENGTH_SHORT
                ).show()
                loadTrips()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
