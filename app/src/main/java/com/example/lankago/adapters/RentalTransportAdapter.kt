package com.example.lankago.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.lankago.R
import com.example.lankago.data.RentalShop
import java.util.Locale

//  ListAdapter for rendering rental cards in RentalsTransportFragment
//  matching the user's exact UI design style.

class RentalTransportAdapter(
    private val onShopClick: (RentalShop) -> Unit
) : ListAdapter<RentalShop, RentalTransportAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_rental_transport_card, parent, false)
        return ViewHolder(view, onShopClick)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(
        itemView: View,
        private val onShopClick: (RentalShop) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        private val ivThumb: ImageView = itemView.findViewById(R.id.img_rental_thumb)
        private val tvTitle: TextView = itemView.findViewById(R.id.tv_rental_title)
        private val tvSubtitle: TextView = itemView.findViewById(R.id.tv_rental_subtitle)
        private val tvRating: TextView = itemView.findViewById(R.id.tv_rental_rating)

        fun bind(shop: RentalShop) {
            val context = itemView.context

            tvTitle.text = shop.name

            // Detect category display
            val category = resolveCategoryName(shop)
            val price = shop.priceStartingFrom.ifBlank { "LKR 3,500/day" }
            val city = shop.city.ifBlank { "Sri Lanka" }
            tvSubtitle.text = "$category · $price · $city"

            val ratingValue = if (shop.rating > 0f) shop.rating else 4.9f
            tvRating.text = "★ ${String.format(Locale.US, "%.1f", ratingValue)}"

            // Resolve authentic shop logo or vehicle drawable
            val fallbackRes = resolveShopDrawable(shop)

            // If an admin uploaded an image or a valid data URI/web URL exists, load with Glide
            if (shop.imageUrl.isNotBlank() && (shop.imageUrl.startsWith("data:image") || shop.imageUrl.startsWith("http"))) {
                Glide.with(context)
                    .load(shop.imageUrl)
                    .placeholder(fallbackRes)
                    .error(fallbackRes)
                    .centerCrop()
                    .into(ivThumb)
            } else {
                // Instantly display the shop's designated authentic drawable
                ivThumb.setImageResource(fallbackRes)
            }

            itemView.setOnClickListener { onShopClick(shop) }
        }

        private fun resolveCategoryName(shop: RentalShop): String {
            val combinedText = "${shop.name} ${shop.description} ${shop.fleetAvailable.joinToString(" ")}".lowercase()
            return when {
                combinedText.contains("scooter") -> "Scooter"
                combinedText.contains("bike") || combinedText.contains("moto") -> "Bike"
                combinedText.contains("van") || combinedText.contains("kdh") -> "Van"
                combinedText.contains("tuktuk") || combinedText.contains("tuk tuk") || combinedText.contains("tuk-tuk") -> "Tuk Tuk"
                combinedText.contains("bus") || combinedText.contains("coach") -> "Bus"
                combinedText.contains("car") || combinedText.contains("suv") || combinedText.contains("sedan") -> "Car"
                else -> "Vehicle"
            }
        }
    }

    companion object {

        //  Resolves the distinct authentic logo or vehicle graphic for any given rental shop.

        fun resolveShopDrawable(shop: RentalShop): Int {
            if (shop.imageRes != 0) return shop.imageRes

            val name = shop.name.lowercase(Locale.ROOT)
            val desc = shop.description.lowercase(Locale.ROOT)
            val fleet = shop.fleetAvailable.joinToString(" ").lowercase(Locale.ROOT)
            val all = "$name $desc $fleet"

            return when {
                name.contains("epic") -> R.drawable.epic_ride
                name.contains("colombo") && (name.contains("scooter") || name.contains("scotter")) -> R.drawable.colombo_scotter
                name.contains("unawatuna") -> R.drawable.unawatune_bike
                name.contains("dambulla") -> R.drawable.dambulla_bike
                name.contains("hikka") -> R.drawable.hikka_rent
                name.contains("kandy") && (name.contains("bike") || name.contains("ride")) -> R.drawable.kandy_bike
                name.contains("ella") -> R.drawable.ella_rental
                name.contains("mirissa") -> R.drawable.tuktuk_rental
                name.contains("arugam") || name.contains("bentota") -> R.drawable.beach_bentota
                name.contains("malkey") && all.contains("van") -> R.drawable.casons_rent
                name.contains("kings") -> R.drawable.kings_rent
                name.contains("malkey") -> R.drawable.malkey_rent
                name.contains("casons") && all.contains("van") -> R.drawable.casons_rent
                name.contains("casons") || name.contains("euro") -> R.drawable.alto
                all.contains("tuktuk") || all.contains("tuk tuk") || all.contains("tuk-tuk") || all.contains("three wheel") -> {
                    when {
                        name.contains("tourist") || name.contains("negombo") -> R.drawable.tourist_tuktuk
                        name.contains("colombo") -> R.drawable.tuktuk_rental
                        else -> R.drawable.galle_tuktuk
                    }
                }
                all.contains("van") || all.contains("kdh") || all.contains("bus") || all.contains("coach") -> R.drawable.casons_rent
                all.contains("scooter") -> R.drawable.colombo_scotter
                all.contains("bike") || all.contains("moto") -> R.drawable.kandy_bike
                all.contains("car") || all.contains("suv") || all.contains("sedan") -> R.drawable.alto
                else -> R.drawable.casons_rent
            }
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<RentalShop>() {
        override fun areItemsTheSame(oldItem: RentalShop, newItem: RentalShop): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: RentalShop, newItem: RentalShop): Boolean =
            oldItem == newItem
    }
}
