package com.example.lankago.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.lankago.R
import com.example.lankago.data.RentalVehicle
import java.text.NumberFormat
import java.util.Locale

class NearbyRentalAdapter(
    private var rentals: List<RentalVehicle>,
    private val onRentalClick: (RentalVehicle) -> Unit
) : RecyclerView.Adapter<NearbyRentalAdapter.RentalViewHolder>() {

    class RentalViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvName: TextView = itemView.findViewById(R.id.tv_nearby_rental_name)
        val tvType: TextView = itemView.findViewById(R.id.tv_nearby_rental_type)
        val tvPrice: TextView = itemView.findViewById(R.id.tv_nearby_rental_price)
        val tvStatus: TextView = itemView.findViewById(R.id.tv_nearby_status_badge)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RentalViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_rental_card, parent, false)
        return RentalViewHolder(view)
    }

    override fun onBindViewHolder(holder: RentalViewHolder, position: Int) {
        val item = rentals[position]
        holder.tvName.text = item.name
        holder.tvType.text = "${item.type} • ${item.location}"
        val formattedPrice = NumberFormat.getNumberInstance(Locale.US).format(item.dailyRateLkr.toInt())
        holder.tvPrice.text = "Rs. $formattedPrice/day"

        if (item.isAvailable) {
            holder.tvStatus.text = "Available"
            holder.tvStatus.setBackgroundColor(Color.parseColor("#15803D"))
        } else {
            holder.tvStatus.text = "Sold Out"
            holder.tvStatus.setBackgroundColor(Color.parseColor("#B91C1C"))
        }

        holder.itemView.setOnClickListener {
            onRentalClick(item)
        }
    }

    override fun getItemCount(): Int = rentals.size

    fun updateData(newRentals: List<RentalVehicle>) {
        rentals = newRentals
        notifyDataSetChanged()
    }
}
