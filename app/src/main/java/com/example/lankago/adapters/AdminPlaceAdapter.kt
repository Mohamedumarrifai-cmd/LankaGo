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
import com.example.lankago.utils.Place
import com.google.android.material.button.MaterialButton

//  Production-ready Adapter for displaying travel destinations in Admin Panel.
//  Includes photo preview, category tag, location, entrance fee, and Edit / Delete actions.

class AdminPlaceAdapter(
    private val onEditClick: (Place) -> Unit,
    private val onDeleteClick: (Place) -> Unit
) : ListAdapter<Place, AdminPlaceAdapter.PlaceViewHolder>(PlaceDiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaceViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_admin_place, parent, false)
        return PlaceViewHolder(view)
    }

    override fun onBindViewHolder(holder: PlaceViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class PlaceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivThumb: ImageView = itemView.findViewById(R.id.iv_admin_place_thumb)
        private val tvName: TextView = itemView.findViewById(R.id.tv_admin_place_name)
        private val tvCategory: TextView = itemView.findViewById(R.id.tv_admin_place_category)
        private val tvRating: TextView = itemView.findViewById(R.id.tv_admin_place_rating)
        private val tvLocation: TextView = itemView.findViewById(R.id.tv_admin_place_location)
        private val tvFee: TextView = itemView.findViewById(R.id.tv_admin_place_fee)
        private val tvDesc: TextView = itemView.findViewById(R.id.tv_admin_place_desc)
        private val tvId: TextView = itemView.findViewById(R.id.tv_admin_place_id)
        private val btnEdit: MaterialButton = itemView.findViewById(R.id.btn_edit_place)
        private val btnDelete: MaterialButton = itemView.findViewById(R.id.btn_delete_place)

        fun bind(place: Place) {
            val context = itemView.context

            tvName.text = place.name
            tvCategory.text = place.category.ifBlank { "Destinations" }
            tvRating.text = "★ ${place.rating}"
            tvLocation.text = place.location.ifBlank { "Sri Lanka" }
            tvFee.text = "Fee: ${place.entranceFee}"
            tvDesc.text = place.description
            tvId.text = "ID: ${place.id}"

            // Load Image with Glide
            try {
                val resName = place.imageUri.substringAfterLast("/")
                val resId = context.resources.getIdentifier(resName, "drawable", context.packageName)
                if (resId != 0) {
                    Glide.with(context)
                        .load(resId)
                        .placeholder(R.drawable.heritage_sigiriya)
                        .error(R.drawable.heritage_sigiriya)
                        .centerCrop()
                        .into(ivThumb)
                } else if (place.imageUri.isNotBlank()) {
                    Glide.with(context)
                        .load(place.imageUri)
                        .placeholder(R.drawable.heritage_sigiriya)
                        .error(R.drawable.heritage_sigiriya)
                        .centerCrop()
                        .into(ivThumb)
                } else {
                    ivThumb.setImageResource(R.drawable.heritage_sigiriya)
                }
            } catch (_: Exception) {
                ivThumb.setImageResource(R.drawable.heritage_sigiriya)
            }

            btnEdit.setOnClickListener {
                onEditClick(place)
            }

            btnDelete.setOnClickListener {
                onDeleteClick(place)
            }
        }
    }

    object PlaceDiffCallback : DiffUtil.ItemCallback<Place>() {
        override fun areItemsTheSame(oldItem: Place, newItem: Place): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Place, newItem: Place): Boolean {
            return oldItem == newItem
        }
    }
}
