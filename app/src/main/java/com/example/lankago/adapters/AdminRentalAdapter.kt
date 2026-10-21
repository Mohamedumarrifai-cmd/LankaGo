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

/**
 * RecyclerView Adapter for displaying rental shops with Admin Edit and Delete controls.
 */
class AdminRentalAdapter(
    private val onEditClick: (RentalShop) -> Unit,
    private val onDeleteClick: (RentalShop) -> Unit
) : ListAdapter<RentalShop, AdminRentalAdapter.RentalViewHolder>(RentalDiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RentalViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_admin_rental, parent, false)
        return RentalViewHolder(view)
    }

    override fun onBindViewHolder(holder: RentalViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class RentalViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivThumb: ImageView = itemView.findViewById(R.id.iv_admin_rental_thumb)
        private val tvName: TextView = itemView.findViewById(R.id.tv_admin_rental_name)
        private val tvLocation: TextView = itemView.findViewById(R.id.tv_admin_rental_location)
        private val tvHours: TextView = itemView.findViewById(R.id.tv_admin_rental_hours)
        private val tvPrice: TextView = itemView.findViewById(R.id.tv_admin_rental_price)
        private val tvContact: TextView = itemView.findViewById(R.id.tv_admin_rental_contact)
        private val btnEdit: MaterialButton = itemView.findViewById(R.id.btn_edit_rental)
        private val btnDelete: MaterialButton = itemView.findViewById(R.id.btn_delete_rental)

        fun bind(shop: RentalShop) {
            tvName.text = shop.name
            tvLocation.text = "${shop.city} · ${shop.address}"
            tvHours.text = shop.operatingHours
            tvPrice.text = shop.priceStartingFrom
            tvContact.text = "Phone: ${shop.contactNumber}"

            // Load Cover Thumbnail with Glide
            if (shop.imageUrl.isNotBlank()) {
                Glide.with(itemView.context)
                    .load(shop.imageUrl)
                    .placeholder(R.drawable.casons_rent)
                    .error(R.drawable.casons_rent)
                    .centerCrop()
                    .into(ivThumb)
            } else if (shop.imageRes != 0) {
                ivThumb.setImageResource(shop.imageRes)
            } else {
                ivThumb.setImageResource(R.drawable.casons_rent)
            }

            btnEdit.setOnClickListener {
                onEditClick(shop)
            }

            btnDelete.setOnClickListener {
                onDeleteClick(shop)
            }
        }
    }

    object RentalDiffCallback : DiffUtil.ItemCallback<RentalShop>() {
        override fun areItemsTheSame(oldItem: RentalShop, newItem: RentalShop): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: RentalShop, newItem: RentalShop): Boolean {
            return oldItem == newItem
        }
    }
}
