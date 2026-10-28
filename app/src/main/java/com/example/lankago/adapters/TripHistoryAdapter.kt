package com.example.lankago.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.lankago.R
import com.example.lankago.data.Trip
import com.example.lankago.utils.setOnSafeClickListener


  //High-performance Material ListAdapter for displaying past and planned trips in LankaGo.
  //Uses DiffUtil for smooth insertions and updates.

class TripHistoryAdapter(
    private val onTripClick: (Trip) -> Unit
) : ListAdapter<Trip, TripHistoryAdapter.TripHistoryViewHolder>(TripDiffCallback()) {

    class TripHistoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cardItem: View = itemView.findViewById(R.id.card_trip_history_item)
        val tvTitle: TextView = itemView.findViewById(R.id.tv_history_trip_title)
        val tvStatus: TextView = itemView.findViewById(R.id.tv_history_trip_status)
        val tvDates: TextView = itemView.findViewById(R.id.tv_history_trip_dates)
        val tvDestination: TextView = itemView.findViewById(R.id.tv_history_trip_destination)
        val tvRentalsBadge: TextView = itemView.findViewById(R.id.tv_history_badge_rentals)
        val tvActivitiesBadge: TextView = itemView.findViewById(R.id.tv_history_badge_activities)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TripHistoryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_trip_history, parent, false)
        return TripHistoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: TripHistoryViewHolder, position: Int) {
        val trip = getItem(position)

        holder.tvTitle.text = if (trip.tripName.isNotBlank()) trip.tripName else "Sri Lanka Adventure"
        holder.tvDates.text = if (trip.date.isNotBlank()) trip.date else "Flexible Schedule"
        holder.tvDestination.text = if (trip.destination.isNotBlank()) trip.destination else "Sri Lanka"

        // Status
        holder.tvStatus.text = if (trip.status.isNotBlank()) trip.status else "Upcoming"

        // Badges
        val rentalCount = trip.bookedRentals.size
        holder.tvRentalsBadge.text = "$rentalCount Rental${if (rentalCount != 1) "s" else ""}"

        val activityCount = trip.bookedActivities.size
        holder.tvActivitiesBadge.text = "$activityCount Activit${if (activityCount != 1) "ies" else "y"}"

        // Click with debounce
        holder.cardItem.setOnSafeClickListener {
            onTripClick(trip)
        }
    }

    class TripDiffCallback : DiffUtil.ItemCallback<Trip>() {
        override fun areItemsTheSame(oldItem: Trip, newItem: Trip): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Trip, newItem: Trip): Boolean {
            return oldItem == newItem
        }
    }
}
