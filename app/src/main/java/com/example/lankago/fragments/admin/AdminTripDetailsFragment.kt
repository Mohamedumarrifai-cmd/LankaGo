package com.example.lankago.fragments.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.lankago.R
import com.example.lankago.repository.SmsNotificationRepository
import com.example.lankago.utils.setOnSafeClickListener
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * Admin screen for confirming trips and triggering automated SMS notifications
 * to all booked travelers via [SmsNotificationRepository].
 */
class AdminTripDetailsFragment : Fragment() {

    private val smsNotificationRepository = SmsNotificationRepository()

    private var tripId: String = "trip_demo_101"
    private var tripRoute: String = "Colombo to Ella Scenic Express"
    private var tripDate: String = "Oct 28, 2026"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_admin_trip_details, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Read arguments if provided
        arguments?.let {
            tripId = it.getString("tripId", tripId)
            tripRoute = it.getString("tripRoute", tripRoute)
            tripDate = it.getString("tripDate", tripDate)
        }

        val btnBack = view.findViewById<ImageButton>(R.id.btn_back_admin_trip)
        val tvTitle = view.findViewById<TextView>(R.id.tv_admin_trip_title)
        val tvRoute = view.findViewById<TextView>(R.id.tv_admin_trip_route)
        val tvDate = view.findViewById<TextView>(R.id.tv_admin_trip_date)
        val tvStatus = view.findViewById<TextView>(R.id.tv_admin_trip_status)
        val btnConfirmTrip = view.findViewById<MaterialButton>(R.id.btn_confirm_trip_sms)
        val layoutSmsStatus = view.findViewById<LinearLayout>(R.id.layout_sms_dispatch_status)
        val tvSmsLabel = view.findViewById<TextView>(R.id.tv_sms_sending_label)

        tvTitle.text = tripRoute
        tvRoute.text = "Trip ID: $tripId"
        tvDate.text = tripDate

        btnBack.setOnSafeClickListener {
            findNavController().navigateUp()
        }

        // =========================================================================
        // REQUIREMENT: Admin clicks "Confirm Trip" -> "Sending SMS..." state -> Dispatches SMS
        // =========================================================================
        btnConfirmTrip.setOnSafeClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Confirm Trip & Dispatch SMS")
                .setMessage("Are you sure you want to mark this trip as CONFIRMED? This will query all booked traveler numbers from Firebase and send them an SMS alert.")
                .setPositiveButton("Confirm & Send") { _, _ ->
                    // 1. Enter "Sending SMS..." state
                    btnConfirmTrip.isEnabled = false
                    layoutSmsStatus.visibility = View.VISIBLE
                    tvSmsLabel.text = "Querying Firebase & Sending SMS to travelers..."

                    viewLifecycleOwner.lifecycleScope.launch {
                        try {
                            // 2. Update Trip status in Firebase Realtime Database
                            val rtdb = FirebaseDatabase.getInstance().reference
                            try {
                                rtdb.child("admin_trips")
                                    .child(tripId)
                                    .child("status")
                                    .setValue("CONFIRMED")
                                    .await()
                            } catch (_: Exception) {
                                // Continue dispatch even if local admin node doesn't exist
                            }

                            // 3. Invoke Coroutine function in SmsNotificationRepository
                            val result = smsNotificationRepository.fetchNumbersAndSendSms(
                                tripId = tripId,
                                tripRoute = tripRoute,
                                tripDate = tripDate
                            )

                            if (!isAdded) return@launch

                            // 4. Update UI after dispatch
                            layoutSmsStatus.visibility = View.GONE
                            btnConfirmTrip.isEnabled = true
                            btnConfirmTrip.text = "Trip Confirmed (SMS Sent)"
                            tvStatus.text = "CONFIRMED"
                            tvStatus.setBackgroundResource(R.drawable.bg_badge_approved)
                            tvStatus.setTextColor(android.graphics.Color.parseColor("#065F46"))

                            // 5. Present result feedback to Admin
                            MaterialAlertDialogBuilder(requireContext())
                                .setTitle("SMS Dispatch Completed")
                                .setMessage(
                                    "Status: ${result.statusMessage}\n\n" +
                                            "• Passengers Found: ${result.totalFound}\n" +
                                            "• Successful SMS: ${result.successCount}\n" +
                                            "• Failed SMS: ${result.failureCount}\n\n" +
                                            if (result.recipientNumbers.isNotEmpty()) {
                                                "Recipients: ${result.recipientNumbers.joinToString(", ")}"
                                            } else {
                                                "No passenger contact numbers were registered in Firebase."
                                            }
                                )
                                .setPositiveButton("OK", null)
                                .show()

                        } catch (e: Exception) {
                            if (!isAdded) return@launch
                            layoutSmsStatus.visibility = View.GONE
                            btnConfirmTrip.isEnabled = true
                            Snackbar.make(
                                view,
                                "Error dispatching SMS: ${e.localizedMessage ?: "Unknown error"}",
                                Snackbar.LENGTH_LONG
                            ).show()
                        }
                    }
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }
}
