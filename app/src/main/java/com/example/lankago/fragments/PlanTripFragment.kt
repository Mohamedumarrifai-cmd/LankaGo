package com.example.lankago.fragments

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.example.lankago.R
import com.example.lankago.data.Trip
import com.example.lankago.utils.TripManager
import com.google.android.material.button.MaterialButton
import com.google.android.material.materialswitch.MaterialSwitch
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class PlanTripFragment : Fragment() {

    private var travelerCount = 2

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_plan_trip, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews(view)
    }

    private fun initViews(view: View) {
        val btnBack = view.findViewById<ImageButton>(R.id.btn_back_plan_trip)
        val tilDestination = view.findViewById<TextInputLayout>(R.id.til_plan_destination)
        val etDestination = view.findViewById<TextInputEditText>(R.id.et_plan_destination)
        val tilDates = view.findViewById<TextInputLayout>(R.id.til_plan_dates)
        val etDates = view.findViewById<TextInputEditText>(R.id.et_plan_dates)
        val btnMinus = view.findViewById<ImageButton>(R.id.btn_plan_minus_traveler)
        val btnPlus = view.findViewById<ImageButton>(R.id.btn_plan_plus_traveler)
        val tvTravelerCount = view.findViewById<TextView>(R.id.tv_plan_traveler_count)
        val switchRental = view.findViewById<MaterialSwitch>(R.id.switch_need_rental)
        val btnConfirm = view.findViewById<MaterialButton>(R.id.btn_confirm_plan_trip)

        // Back button navigation
        btnBack?.setOnClickListener {
            findNavController().navigateUp()
        }

        // Default Date Setup & DatePickerDialog
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, 3)
        val startDateFormat = SimpleDateFormat("dd MMM", Locale.US)
        val startDateStr = startDateFormat.format(calendar.time)
        calendar.add(Calendar.DAY_OF_YEAR, 5)
        val endDateFormat = SimpleDateFormat("dd MMM yyyy", Locale.US)
        val endDateStr = endDateFormat.format(calendar.time)
        etDates?.setText("$startDateStr – $endDateStr")

        etDates?.setOnClickListener {
            val datePicker = DatePickerDialog(
                requireContext(),
                { _, year, month, dayOfMonth ->
                    val chosen = Calendar.getInstance()
                    chosen.set(year, month, dayOfMonth)
                    val s = SimpleDateFormat("dd MMM", Locale.US).format(chosen.time)
                    chosen.add(Calendar.DAY_OF_YEAR, 5)
                    val e = SimpleDateFormat("dd MMM yyyy", Locale.US).format(chosen.time)
                    etDates.setText("$s – $e")
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
            datePicker.datePicker.minDate = System.currentTimeMillis()
            datePicker.show()
        }

        // Travelers Stepper
        btnMinus?.setOnClickListener {
            if (travelerCount > 1) {
                travelerCount--
                tvTravelerCount?.text = travelerCount.toString()
            }
        }

        btnPlus?.setOnClickListener {
            if (travelerCount < 12) {
                travelerCount++
                tvTravelerCount?.text = travelerCount.toString()
            }
        }

        // Next / Create Trip Confirmation Logic
        btnConfirm?.setOnClickListener {
            val destination = etDestination?.text?.toString()?.trim() ?: ""
            val dates = etDates?.text?.toString()?.trim() ?: ""

            if (destination.isEmpty()) {
                tilDestination?.error = "Please specify your destination (e.g., Ella, Mirissa, Sigiriya)"
                etDestination?.requestFocus()
                return@setOnClickListener
            }
            tilDestination?.error = null

            // Construct new trip and save persistently
            val newTrip = Trip(
                id = "trip_${System.currentTimeMillis()}",
                tripName = "$destination Adventure ($travelerCount Travelers)",
                date = dates,
                travelerType = "Local",
                idImageUri = null,
                places = mutableListOf(),
                bookedRentals = mutableListOf(),
                bookedActivities = mutableListOf()
            )
            TripManager.saveTrip(requireContext(), newTrip)

            // CRITICAL USER FLOW:
            // If rental switch is ON -> navigate to RentalListFragment
            // If rental switch is OFF -> return cleanly to HomeFragment
            if (switchRental?.isChecked == true) {
                val bundle = bundleOf("destinationLocation" to destination)
                findNavController().navigate(
                    R.id.action_planTripFragment_to_rentalShopsListFragment,
                    bundle
                )
            } else {
                Snackbar.make(
                    view,
                    "Trip \"${newTrip.tripName}\" created successfully!",
                    Snackbar.LENGTH_LONG
                ).show()

                val navOptions = NavOptions.Builder()
                    .setPopUpTo(R.id.homeFragment, false)
                    .setLaunchSingleTop(true)
                    .build()
                findNavController().navigate(R.id.homeFragment, null, navOptions)
            }
        }
    }
}
