package com.example.lankago.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.lankago.R
import com.example.lankago.data.Vehicle
import com.google.android.material.button.MaterialButton
import java.text.NumberFormat
import java.util.Locale

class RentalListAdapter(
    private var vehicles: List<Vehicle>,
    private val onVehicleClick: (Vehicle) -> Unit
) : RecyclerView.Adapter<RentalListAdapter.RentalViewHolder>() {

    class RentalViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivThumb: ImageView = itemView.findViewById(R.id.iv_vehicle_thumb)
        val tvName: TextView = itemView.findViewById(R.id.tv_nearby_rental_name)
        val tvTypeBadge: TextView = itemView.findViewById(R.id.tv_nearby_rental_type)
        val tvAvailabilityBadge: TextView = itemView.findViewById(R.id.tv_nearby_status_badge)
        val tvProvider: TextView = itemView.findViewById(R.id.tv_vehicle_provider)
        val tvRating: TextView = itemView.findViewById(R.id.tv_vehicle_rating)
        val tvPrice: TextView = itemView.findViewById(R.id.tv_nearby_rental_price)
        val btnView: MaterialButton = itemView.findViewById(R.id.btn_view_vehicle)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RentalViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_rental_card, parent, false)
        return RentalViewHolder(view)
    }

    override fun onBindViewHolder(holder: RentalViewHolder, position: Int) {
        val vehicle = vehicles[position]
        val format = NumberFormat.getNumberInstance(Locale.US)

        holder.tvName.text = vehicle.name
        holder.tvTypeBadge.text = vehicle.type
        holder.tvProvider.text = "${vehicle.provider.name} • ${vehicle.provider.location}"
        holder.tvRating.text = "${vehicle.rating} (${vehicle.reviewCount})"
        holder.tvPrice.text = "LKR ${format.format(vehicle.pricePerDayLkr.toInt())} / day"

        if (vehicle.isAvailable) {
            holder.tvAvailabilityBadge.text = "Available"
            holder.tvAvailabilityBadge.visibility = View.VISIBLE
        } else {
            holder.tvAvailabilityBadge.text = "Sold Out"
            holder.tvAvailabilityBadge.visibility = View.VISIBLE
        }

        val localDrawable = when {
            vehicle.name.contains("Tuk", ignoreCase = true) -> R.drawable.tuktuk_rental
            vehicle.name.contains("Aqua", ignoreCase = true) || vehicle.type.contains("Car", ignoreCase = true) -> R.drawable.colombo_scotter
            vehicle.name.contains("PCX", ignoreCase = true) -> R.drawable.colombo_scotter
            vehicle.name.contains("Enfield", ignoreCase = true) -> R.drawable.kandy_bike
            else -> R.drawable.ella_rental
        }
        holder.ivThumb.setImageResource(localDrawable)

        holder.itemView.setOnClickListener { onVehicleClick(vehicle) }
        holder.btnView.setOnClickListener { onVehicleClick(vehicle) }
    }

    override fun getItemCount(): Int = vehicles.size

    fun updateData(newVehicles: List<Vehicle>) {
        this.vehicles = newVehicles
        notifyDataSetChanged()
    }
}
