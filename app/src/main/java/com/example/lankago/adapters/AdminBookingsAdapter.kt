package com.example.lankago.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.lankago.R
import com.example.lankago.data.Booking
import com.example.lankago.data.BookingStatus
import com.example.lankago.data.BookingType
import com.google.android.material.button.MaterialButton
import java.text.NumberFormat
import java.util.Locale

//  Production-ready ListAdapter for administering customer bookings in LankaGo.
//  Supports:
//  - Realtime updates via DiffUtil.
//  - Dynamic color tokens for booking types (RENTAL vs ACTIVITY).
//  - Dynamic pill backgrounds for booking states (CONFIRMED, PENDING, CANCELLED).
//  - Status update interaction.
class AdminBookingsAdapter(
    private val onStatusChangeClick: (Booking) -> Unit
) : ListAdapter<Booking, AdminBookingsAdapter.BookingViewHolder>(BookingDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookingViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_admin_booking, parent, false)
        return BookingViewHolder(view)
    }

    override fun onBindViewHolder(holder: BookingViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class BookingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvItemName: TextView = itemView.findViewById(R.id.tv_booking_item_name)
        private val tvTypeBadge: TextView = itemView.findViewById(R.id.tv_booking_type_badge)
        private val tvUserName: TextView = itemView.findViewById(R.id.tv_booking_user_name)
        private val tvUserContact: TextView = itemView.findViewById(R.id.tv_booking_user_contact)
        private val tvDateTime: TextView = itemView.findViewById(R.id.tv_booking_date_time)
        private val tvNotes: TextView = itemView.findViewById(R.id.tv_booking_notes)
        private val tvPrice: TextView = itemView.findViewById(R.id.tv_booking_price)
        private val tvStatus: TextView = itemView.findViewById(R.id.tv_booking_status)
        private val btnChangeStatus: MaterialButton = itemView.findViewById(R.id.btn_change_status)

        fun bind(booking: Booking) {
            val context = itemView.context

            tvItemName.text = booking.itemName.ifBlank { "Reservation #${booking.id.takeLast(6)}" }
            tvUserName.text = booking.userName.ifBlank { "Guest Traveler" }

            // User contact
            val contactInfo = buildString {
                if (booking.userEmail.isNotBlank()) append(booking.userEmail)
                if (booking.userPhone.isNotBlank()) {
                    if (isNotEmpty()) append(" · ")
                    append(booking.userPhone)
                }
            }
            tvUserContact.text = if (contactInfo.isNotBlank()) "($contactInfo)" else ""
            tvUserContact.visibility = if (contactInfo.isNotBlank()) View.VISIBLE else View.GONE

            // Date & Time
            val dateTime = buildString {
                append(booking.bookingDate.ifBlank { "Flexible Date" })
                if (booking.bookingTime.isNotBlank()) {
                    append(" · ")
                    append(booking.bookingTime)
                }
            }
            tvDateTime.text = dateTime

            // Special notes
            if (booking.specialNotes.isNotBlank()) {
                tvNotes.text = "Note: ${booking.specialNotes}"
                tvNotes.visibility = View.VISIBLE
            } else {
                tvNotes.visibility = View.GONE
            }

            // Currency formatting
            val formatter = NumberFormat.getNumberInstance(Locale.US)
            tvPrice.text = "LKR ${formatter.format(booking.totalPriceLkr)}"

            // Booking Type Badge styling
            if (booking.bookingType == BookingType.RENTAL) {
                tvTypeBadge.text = "RENTAL"
                tvTypeBadge.background = ContextCompat.getDrawable(context, R.drawable.bg_badge_admin)
                tvTypeBadge.setTextColor(ContextCompat.getColor(context, R.color.admin_accent_indigo))
            } else {
                tvTypeBadge.text = "ACTIVITY"
                tvTypeBadge.background = ContextCompat.getDrawable(context, R.drawable.bg_badge_pending)
                tvTypeBadge.setTextColor(ContextCompat.getColor(context, R.color.cultural_gold))
            }

            // Status Badge styling
            when (booking.status) {
                BookingStatus.CONFIRMED -> {
                    tvStatus.text = "CONFIRMED"
                    tvStatus.background = ContextCompat.getDrawable(context, R.drawable.bg_badge_approved)
                    tvStatus.setTextColor(ContextCompat.getColor(context, R.color.admin_success))
                }
                BookingStatus.PENDING -> {
                    tvStatus.text = "PENDING"
                    tvStatus.background = ContextCompat.getDrawable(context, R.drawable.bg_badge_pending)
                    tvStatus.setTextColor(ContextCompat.getColor(context, R.color.admin_warning))
                }
                BookingStatus.CANCELLED -> {
                    tvStatus.text = "CANCELLED"
                    tvStatus.background = ContextCompat.getDrawable(context, R.drawable.bg_circle_rose_soft)
                    tvStatus.setTextColor(ContextCompat.getColor(context, R.color.admin_danger))
                }
            }

            btnChangeStatus.setOnClickListener {
                onStatusChangeClick(booking)
            }
        }
    }

    private class BookingDiffCallback : DiffUtil.ItemCallback<Booking>() {
        override fun areItemsTheSame(oldItem: Booking, newItem: Booking): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Booking, newItem: Booking): Boolean {
            return oldItem == newItem
        }
    }
}
