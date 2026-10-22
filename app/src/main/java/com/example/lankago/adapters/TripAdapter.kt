package com.example.lankago.adapters

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.lankago.R
import com.example.lankago.data.Trip

class TripAdapter(
    private var trips: MutableList<Trip>,
    private val onTripClick: (Trip) -> Unit,
    private val onDeleteClick: (Trip) -> Unit
) : RecyclerView.Adapter<TripAdapter.TripViewHolder>() {

    class TripViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTripName: TextView = itemView.findViewById(R.id.tv_trip_name)
        val tvTripDate: TextView = itemView.findViewById(R.id.tv_trip_date)
        val tvPlacesCount: TextView = itemView.findViewById(R.id.tv_trip_places_count)
        val ivTravelerIdThumb: ImageView = itemView.findViewById(R.id.iv_traveler_id_thumb)
        val tvIdVerificationBadge: TextView = itemView.findViewById(R.id.tv_id_verification_badge)
        val btnDelete: ImageView = itemView.findViewById(R.id.btn_delete_trip)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TripViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_trip, parent, false)
        return TripViewHolder(view)
    }

    override fun onBindViewHolder(holder: TripViewHolder, position: Int) {
        val trip = trips[position]

        holder.tvTripName.text = trip.tripName
        holder.tvTripDate.text = if (trip.date.isNotBlank()) trip.date else "Flexible dates"

        // Display number of destinations, rentals, and activities
        val placesCount = trip.places.size
        val rentalCount = trip.bookedRentals.size
        val activityCount = trip.bookedActivities.size

        val summaryParts = mutableListOf<String>()
        if (placesCount > 0) summaryParts.add("$placesCount Places")
        if (rentalCount > 0) summaryParts.add("$rentalCount Vehicle" + if (rentalCount > 1) "s" else "")
        if (activityCount > 0) summaryParts.add("$activityCount Activit" + if (activityCount > 1) "ies" else "y")

        holder.tvPlacesCount.text = if (summaryParts.isNotEmpty()) {
            summaryParts.joinToString(" • ")
        } else {
            "Empty Itinerary"
        }

        // Display Traveler Verification Badge & Captured Image
        if (!trip.idImageUri.isNullOrEmpty()) {
            try {
                com.bumptech.glide.Glide.with(holder.itemView.context)
                    .load(trip.idImageUri)
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .error(android.R.drawable.ic_menu_gallery)
                    .centerCrop()
                    .into(holder.ivTravelerIdThumb)
                holder.ivTravelerIdThumb.imageTintList = null
            } catch (e: Exception) {
                holder.ivTravelerIdThumb.setImageResource(android.R.drawable.ic_menu_gallery)
            }

            if (trip.travelerType.contains("Foreign", ignoreCase = true) || trip.travelerType.contains("Passport", ignoreCase = true)) {
                holder.tvIdVerificationBadge.text = "✓ Passport Verified"
            } else {
                holder.tvIdVerificationBadge.text = "✓ ID Verified"
            }
            holder.tvIdVerificationBadge.visibility = View.VISIBLE
        } else {
            holder.ivTravelerIdThumb.setImageResource(android.R.drawable.ic_menu_gallery)
            holder.tvIdVerificationBadge.text = "Verification Pending"
            holder.tvIdVerificationBadge.visibility = View.VISIBLE
        }

        holder.itemView.setOnClickListener {
            onTripClick(trip)
        }

        holder.btnDelete.setOnClickListener {
            onDeleteClick(trip)
        }
    }

    override fun getItemCount(): Int = trips.size

    fun updateData(newTrips: List<Trip>) {
        trips.clear()
        trips.addAll(newTrips)
        notifyDataSetChanged()
    }
}
