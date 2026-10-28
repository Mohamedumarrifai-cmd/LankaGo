package com.example.lankago.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.lankago.R
import com.example.lankago.data.LocalProduct
import com.google.android.material.button.MaterialButton
import com.google.android.material.imageview.ShapeableImageView
import java.text.NumberFormat
import java.util.Locale

// Production-ready ListAdapter for administering Local Products in LankaGo.
// Supports Glide image loading with placeholder/fallback, stock badge color state,
// and high-performance DiffUtil comparisons.

class AdminProductAdapter(
    private val onEditClick: (LocalProduct) -> Unit,
    private val onDeleteClick: (LocalProduct) -> Unit
) : ListAdapter<LocalProduct, AdminProductAdapter.ProductViewHolder>(ProductDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_admin_product, parent, false)
        return ProductViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivThumb: ShapeableImageView = itemView.findViewById(R.id.iv_admin_product_thumb)
        private val tvName: TextView = itemView.findViewById(R.id.tv_admin_product_name)
        private val tvCategory: TextView = itemView.findViewById(R.id.tv_admin_product_category)
        private val tvStockBadge: TextView = itemView.findViewById(R.id.tv_admin_product_stock_badge)
        private val tvPrice: TextView = itemView.findViewById(R.id.tv_admin_product_price)
        private val tvInventory: TextView = itemView.findViewById(R.id.tv_admin_product_inventory)
        private val tvRegion: TextView = itemView.findViewById(R.id.tv_admin_product_region)
        private val btnEdit: MaterialButton = itemView.findViewById(R.id.btn_edit_product)
        private val btnDelete: MaterialButton = itemView.findViewById(R.id.btn_delete_product)

        fun bind(product: LocalProduct) {
            val context = itemView.context

            tvName.text = product.name
            tvCategory.text = if (product.originRegion.isNotBlank()) {
                "${product.category} · ${product.originRegion}"
            } else {
                product.category
            }

            // Currency formatting
            val formatter = NumberFormat.getNumberInstance(Locale.US)
            tvPrice.text = "LKR ${formatter.format(product.priceLkr)}"

            // Inventory & Stock Status Badge
            tvInventory.text = "Available: ${product.stockQuantity} units"
            if (product.stockQuantity > 0) {
                tvStockBadge.text = "In Stock"
                tvStockBadge.background = ContextCompat.getDrawable(context, R.drawable.bg_badge_approved)
                tvStockBadge.setTextColor(ContextCompat.getColor(context, R.color.admin_success))
            } else {
                tvStockBadge.text = "Out of Stock"
                tvStockBadge.background = ContextCompat.getDrawable(context, R.drawable.bg_badge_pending)
                tvStockBadge.setTextColor(ContextCompat.getColor(context, R.color.admin_danger))
            }

            tvRegion.text = "Origin: ${product.originRegion.ifBlank { "Sri Lanka" }}"

            // Image loading via Glide
            val imageSource: Any = when {
                product.imageUrl.isNotBlank() -> product.imageUrl
                product.imageRes != 0 -> product.imageRes
                else -> R.drawable.ceylon_tea_set
            }

            Glide.with(context)
                .load(imageSource)
                .placeholder(R.drawable.ceylon_tea_set)
                .error(R.drawable.ceylon_tea_set)
                .centerCrop()
                .into(ivThumb)

            btnEdit.setOnClickListener { onEditClick(product) }
            btnDelete.setOnClickListener { onDeleteClick(product) }
        }
    }

    private class ProductDiffCallback : DiffUtil.ItemCallback<LocalProduct>() {
        override fun areItemsTheSame(oldItem: LocalProduct, newItem: LocalProduct): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: LocalProduct, newItem: LocalProduct): Boolean {
            return oldItem == newItem
        }
    }
}
