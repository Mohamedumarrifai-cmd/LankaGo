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
import com.example.lankago.data.Mall
import com.google.android.material.button.MaterialButton

//  ListAdapter displaying shopping malls to travelers in LankaGo.
//  Supports image loading via Glide with placeholder fallback, rating badge,
//  operating hours, and exploration callback.

class MallAdapter(
    private val onMallClick: (Mall) -> Unit
) : ListAdapter<Mall, MallAdapter.MallViewHolder>(MallDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MallViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_mall_card, parent, false)
        return MallViewHolder(view)
    }

    override fun onBindViewHolder(holder: MallViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class MallViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivImage: ImageView = itemView.findViewById(R.id.iv_mall_image)
        private val tvCategoryPill: TextView = itemView.findViewById(R.id.tv_mall_category_pill)
        private val tvRating: TextView = itemView.findViewById(R.id.tv_mall_rating)
        private val tvStoreBadge: TextView = itemView.findViewById(R.id.tv_mall_store_badge)
        private val tvName: TextView = itemView.findViewById(R.id.tv_mall_name)
        private val tvAddress: TextView = itemView.findViewById(R.id.tv_mall_address)
        private val tvDescription: TextView = itemView.findViewById(R.id.tv_mall_description)
        private val tvHours: TextView = itemView.findViewById(R.id.tv_mall_hours)
        private val btnExplore: MaterialButton = itemView.findViewById(R.id.btn_mall_explore)

        fun bind(mall: Mall) {
            val context = itemView.context

            tvName.text = mall.name
            tvCategoryPill.text = mall.category
            tvRating.text = String.format(java.util.Locale.US, "%.1f", mall.rating)
            tvStoreBadge.text = "${mall.storeCount}+ Stores & Outlets"
            tvAddress.text = "${mall.address} (${mall.city})"
            tvDescription.text = mall.description
            tvHours.text = mall.operatingHours

            val imageSource: Any = when {
                mall.imageUrl.isNotBlank() -> mall.imageUrl
                mall.imageRes != 0 -> mall.imageRes
                else -> R.drawable.product
            }

            Glide.with(context)
                .load(imageSource)
                .placeholder(R.drawable.product)
                .error(R.drawable.product)
                .centerCrop()
                .into(ivImage)

            itemView.setOnClickListener { onMallClick(mall) }
            btnExplore.setOnClickListener { onMallClick(mall) }
        }
    }

    private class MallDiffCallback : DiffUtil.ItemCallback<Mall>() {
        override fun areItemsTheSame(oldItem: Mall, newItem: Mall): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Mall, newItem: Mall): Boolean {
            return oldItem == newItem
        }
    }
}
