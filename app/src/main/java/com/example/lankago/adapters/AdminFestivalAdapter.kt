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
import com.example.lankago.data.CulturalFestival
import com.google.android.material.button.MaterialButton

/**
 * Adapter for Admin Festival & Shows list with banners, details, and Edit / Delete actions.
 */
class AdminFestivalAdapter(
    private val onEditClick: (CulturalFestival) -> Unit,
    private val onDeleteClick: (CulturalFestival) -> Unit
) : ListAdapter<CulturalFestival, AdminFestivalAdapter.FestivalViewHolder>(FestivalDiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FestivalViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_admin_festival, parent, false)
        return FestivalViewHolder(view)
    }

    override fun onBindViewHolder(holder: FestivalViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class FestivalViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivThumb: ImageView = itemView.findViewById(R.id.iv_admin_festival_thumb)
        private val tvName: TextView = itemView.findViewById(R.id.tv_admin_festival_name)
        private val tvCategory: TextView = itemView.findViewById(R.id.tv_admin_festival_category)
        private val tvRating: TextView = itemView.findViewById(R.id.tv_admin_festival_rating)
        private val tvDates: TextView = itemView.findViewById(R.id.tv_admin_festival_dates)
        private val tvLocation: TextView = itemView.findViewById(R.id.tv_admin_festival_location)
        private val tvId: TextView = itemView.findViewById(R.id.tv_admin_festival_id)
        private val btnEdit: MaterialButton = itemView.findViewById(R.id.btn_edit_festival)
        private val btnDelete: MaterialButton = itemView.findViewById(R.id.btn_delete_festival)

        fun bind(festival: CulturalFestival) {
            val context = itemView.context

            tvName.text = festival.name
            tvCategory.text = festival.category.ifBlank { "Cultural Festival" }
            tvRating.text = "★ ${festival.rating}"
            tvDates.text = festival.dates
            tvLocation.text = festival.locationName
            tvId.text = "ID: ${festival.id}"

            // Image loading via Glide
            when {
                festival.imageUrl.isNotBlank() -> {
                    try {
                        val resName = festival.imageUrl.substringAfterLast("/")
                        val resId = context.resources.getIdentifier(resName, "drawable", context.packageName)
                        if (resId != 0) {
                            Glide.with(context)
                                .load(resId)
                                .centerCrop()
                                .placeholder(R.drawable.kandy_esala)
                                .into(ivThumb)
                        } else {
                            Glide.with(context)
                                .load(festival.imageUrl)
                                .centerCrop()
                                .placeholder(R.drawable.kandy_esala)
                                .into(ivThumb)
                        }
                    } catch (_: Exception) {
                        ivThumb.setImageResource(R.drawable.kandy_esala)
                    }
                }
                festival.bannerRes != 0 -> {
                    Glide.with(context)
                        .load(festival.bannerRes)
                        .centerCrop()
                        .placeholder(R.drawable.kandy_esala)
                        .into(ivThumb)
                }
                else -> {
                    ivThumb.setImageResource(R.drawable.kandy_esala)
                }
            }

            btnEdit.setOnClickListener {
                onEditClick(festival)
            }

            btnDelete.setOnClickListener {
                onDeleteClick(festival)
            }
        }
    }

    object FestivalDiffCallback : DiffUtil.ItemCallback<CulturalFestival>() {
        override fun areItemsTheSame(oldItem: CulturalFestival, newItem: CulturalFestival): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: CulturalFestival, newItem: CulturalFestival): Boolean {
            return oldItem == newItem
        }
    }
}
