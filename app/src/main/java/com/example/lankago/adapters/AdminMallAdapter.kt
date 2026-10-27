package com.example.lankago.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.lankago.R
import com.example.lankago.data.Mall
import com.google.android.material.button.MaterialButton
import com.google.android.material.imageview.ShapeableImageView

/**
 * ListAdapter for administering Shopping Malls in LankaGo.
 * Provides Edit and Delete action callbacks with DiffUtil item animations.
 */
class AdminMallAdapter(
    private val onEditClick: (Mall) -> Unit,
    private val onDeleteClick: (Mall) -> Unit
) : ListAdapter<Mall, AdminMallAdapter.AdminMallViewHolder>(AdminMallDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdminMallViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_admin_mall, parent, false)
        return AdminMallViewHolder(view)
    }

    override fun onBindViewHolder(holder: AdminMallViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class AdminMallViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivThumb: ShapeableImageView = itemView.findViewById(R.id.iv_admin_mall_thumb)
        private val tvName: TextView = itemView.findViewById(R.id.tv_admin_mall_name)
        private val tvCity: TextView = itemView.findViewById(R.id.tv_admin_mall_city)
        private val tvStores: TextView = itemView.findViewById(R.id.tv_admin_mall_stores)
        private val tvAddress: TextView = itemView.findViewById(R.id.tv_admin_mall_address)
        private val btnEdit: MaterialButton = itemView.findViewById(R.id.btn_edit_mall)
        private val btnDelete: MaterialButton = itemView.findViewById(R.id.btn_delete_mall)

        fun bind(mall: Mall) {
            val context = itemView.context

            tvName.text = mall.name
            tvCity.text = "${mall.city} · ${mall.category}"
            tvStores.text = "${mall.storeCount}+ Stores · ★ ${String.format(java.util.Locale.US, "%.1f", mall.rating)}"
            tvAddress.text = mall.address

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
                .into(ivThumb)

            btnEdit.setOnClickListener { onEditClick(mall) }
            btnDelete.setOnClickListener { onDeleteClick(mall) }
        }
    }

    private class AdminMallDiffCallback : DiffUtil.ItemCallback<Mall>() {
        override fun areItemsTheSame(oldItem: Mall, newItem: Mall): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Mall, newItem: Mall): Boolean {
            return oldItem == newItem
        }
    }
}
