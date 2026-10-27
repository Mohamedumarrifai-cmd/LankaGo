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
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView

/**
 * ListAdapter for rendering rental shops in RentalShopsListFragment.
 */
class RentalShopAdapter(
    private val onShopClick: (RentalShop) -> Unit
) : ListAdapter<RentalShop, RentalShopAdapter.RentalShopViewHolder>(RentalShopDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RentalShopViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_rental_shop, parent, false)
        return RentalShopViewHolder(view, onShopClick)
    }

    override fun onBindViewHolder(holder: RentalShopViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class RentalShopViewHolder(
        itemView: View,
        private val onShopClick: (RentalShop) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {

        private val cardRoot: MaterialCardView = itemView.findViewById(R.id.card_rental_shop)
        private val ivImage: ImageView = itemView.findViewById(R.id.iv_shop_image)
        private val tvName: TextView = itemView.findViewById(R.id.tv_shop_name)
        private val tvRating: TextView = itemView.findViewById(R.id.tv_shop_rating)
        private val tvReviews: TextView = itemView.findViewById(R.id.tv_shop_reviews)
        private val tvCity: TextView = itemView.findViewById(R.id.tv_shop_city)
        private val tvDesc: TextView = itemView.findViewById(R.id.tv_shop_desc)
        private val tvPriceStart: TextView = itemView.findViewById(R.id.tv_shop_price_start)
        private val btnViewDetails: MaterialButton = itemView.findViewById(R.id.btn_view_shop_details)

        fun bind(shop: RentalShop) {
            tvName.text = shop.name
            tvRating.text = String.format("%.2f", shop.rating)
            tvReviews.text = "(${shop.reviewCount} reviews)"
            tvCity.text = "${shop.city}, Sri Lanka"
            tvDesc.text = shop.description
            tvPriceStart.text = shop.priceStartingFrom

            val fallbackRes = RentalTransportAdapter.resolveShopDrawable(shop)
            if (!shop.imageUrl.isNullOrBlank() && (shop.imageUrl.startsWith("data:image") || shop.imageUrl.startsWith("http"))) {
                Glide.with(itemView.context)
                    .load(shop.imageUrl)
                    .placeholder(fallbackRes)
                    .error(fallbackRes)
                    .centerCrop()
                    .into(ivImage)
            } else {
                ivImage.setImageResource(fallbackRes)
            }

            cardRoot.setOnClickListener { onShopClick(shop) }
            btnViewDetails.setOnClickListener { onShopClick(shop) }
        }
    }

    class RentalShopDiffCallback : DiffUtil.ItemCallback<RentalShop>() {
        override fun areItemsTheSame(oldItem: RentalShop, newItem: RentalShop): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: RentalShop, newItem: RentalShop): Boolean {
            return oldItem == newItem
        }
    }
}
