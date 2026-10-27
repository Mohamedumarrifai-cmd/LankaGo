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
        val ivThumb: android.widget.ImageView? = itemView.findViewById(R.id.iv_vehicle_thumb)
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

        val localDrawable = when {
            item.id.contains("galle", ignoreCase = true) || item.name.contains("Galle", ignoreCase = true) -> R.drawable.galle_tuktuk
            item.name.contains("Tuk", ignoreCase = true) || item.type.contains("Tuk", ignoreCase = true) -> R.drawable.tourist_tuktuk
            item.name.contains("KDH", ignoreCase = true) || item.name.contains("Van", ignoreCase = true) || item.type.contains("Van", ignoreCase = true) -> R.drawable.kdh
            item.name.contains("Montero", ignoreCase = true) -> R.drawable.montero
            item.name.contains("X-Trail", ignoreCase = true) || item.type.contains("SUV", ignoreCase = true) || item.type.contains("4x4", ignoreCase = true) -> R.drawable.xtrail
            item.name.contains("Grace", ignoreCase = true) -> R.drawable.grace
            item.name.contains("Prius", ignoreCase = true) -> R.drawable.prius
            item.name.contains("Aqua", ignoreCase = true) -> R.drawable.aqua
            item.name.contains("Alto", ignoreCase = true) -> R.drawable.alto
            item.name.contains("Wagon", ignoreCase = true) -> R.drawable.wagonr
            item.name.contains("NS200", ignoreCase = true) || item.name.contains("Pulsar", ignoreCase = true) -> R.drawable.ns200
            item.name.contains("NMAX", ignoreCase = true) -> R.drawable.nmax
            item.name.contains("Dio", ignoreCase = true) -> R.drawable.dio
            item.name.contains("PCX", ignoreCase = true) -> R.drawable.pcx
            item.name.contains("FZ", ignoreCase = true) -> R.drawable.fz
            item.name.contains("Enfield", ignoreCase = true) || item.name.contains("Himalayan", ignoreCase = true) -> R.drawable.royalenflied
            item.type.contains("Bike", ignoreCase = true) || item.name.contains("Yamaha", ignoreCase = true) -> R.drawable.fz
            else -> R.drawable.aqua
        }

        holder.ivThumb?.let { iv ->
            val firstImg = item.images.firstOrNull()
            if (!firstImg.isNullOrBlank()) {
                com.bumptech.glide.Glide.with(iv.context)
                    .load(firstImg)
                    .placeholder(localDrawable)
                    .error(localDrawable)
                    .centerCrop()
                    .into(iv) //image eka image view ekata daanawa
            } else {
                iv.setImageResource(localDrawable) //url eka netnam
            }
        }

        holder.itemView.setOnClickListener {   //user kenek card eka click karama setonclicklistener eka process weno
            onRentalClick(item)
        }
    }

    override fun getItemCount(): Int = rentals.size //list eken mulu waahana gaana aran denawa

    fun updateData(newRentals: List<RentalVehicle>) {
        rentals = newRentals
        notifyDataSetChanged()
    }
}
