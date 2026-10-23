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
            vehicle.name.contains("Tuk", ignoreCase = true) || vehicle.type.contains("Tuk", ignoreCase = true) -> R.drawable.tourist_tuktuk
            vehicle.name.contains("KDH", ignoreCase = true) || vehicle.name.contains("Van", ignoreCase = true) || vehicle.type.contains("Van", ignoreCase = true) -> R.drawable.kdh
            vehicle.name.contains("Montero", ignoreCase = true) -> R.drawable.montero
            vehicle.name.contains("X-Trail", ignoreCase = true) || vehicle.type.contains("SUV", ignoreCase = true) || vehicle.type.contains("4x4", ignoreCase = true) -> R.drawable.xtrail
            vehicle.name.contains("Grace", ignoreCase = true) -> R.drawable.grace
            vehicle.name.contains("Prius", ignoreCase = true) -> R.drawable.prius
            vehicle.name.contains("Aqua", ignoreCase = true) -> R.drawable.aqua
            vehicle.name.contains("Alto", ignoreCase = true) -> R.drawable.alto
            vehicle.name.contains("Wagon", ignoreCase = true) -> R.drawable.wagonr
            vehicle.name.contains("NS200", ignoreCase = true) || vehicle.name.contains("Pulsar", ignoreCase = true) -> R.drawable.ns200
            vehicle.name.contains("FZ", ignoreCase = true) || vehicle.name.contains("Yamaha", ignoreCase = true) -> R.drawable.fz
            vehicle.name.contains("Dio", ignoreCase = true) -> R.drawable.dio
            vehicle.name.contains("NMAX", ignoreCase = true) -> R.drawable.nmax
            vehicle.name.contains("PCX", ignoreCase = true) -> R.drawable.pcx
            vehicle.name.contains("Enfield", ignoreCase = true) || vehicle.name.contains("Himalayan", ignoreCase = true) -> R.drawable.royalenflied
            vehicle.type.contains("Bike", ignoreCase = true) -> R.drawable.fz
            else -> R.drawable.aqua
        }

        if (vehicle.imageUrl.isNotBlank()) {
            com.bumptech.glide.Glide.with(holder.itemView.context)
                .load(vehicle.imageUrl)
                .placeholder(localDrawable)
                .error(localDrawable)
                .centerCrop()
                .into(holder.ivThumb)
        } else {
            holder.ivThumb.setImageResource(localDrawable)
        }

        holder.itemView.setOnClickListener { onVehicleClick(vehicle) }
        holder.btnView.setOnClickListener { onVehicleClick(vehicle) }
    }

    override fun getItemCount(): Int = vehicles.size

    fun updateData(newVehicles: List<Vehicle>) {
        this.vehicles = newVehicles
        notifyDataSetChanged()
    }
}
