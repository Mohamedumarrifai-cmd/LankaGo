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
import com.google.android.material.imageview.ShapeableImageView
import java.text.NumberFormat
import java.util.Locale

/**
 * ListAdapter for rendering live Firebase-backed local products in LocalProductsFragment.
 */
class UserProductAdapter(
    private val onProductClick: (LocalProduct) -> Unit
) : ListAdapter<LocalProduct, UserProductAdapter.UserProductViewHolder>(ProductDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserProductViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_user_product, parent, false)
        return UserProductViewHolder(view, onProductClick)
    }

    override fun onBindViewHolder(holder: UserProductViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class UserProductViewHolder(
        itemView: View,
        private val onProductClick: (LocalProduct) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        private val ivImage: ShapeableImageView = itemView.findViewById(R.id.iv_user_product_image)
        private val tvName: TextView = itemView.findViewById(R.id.tv_user_product_name)
        private val tvCategory: TextView = itemView.findViewById(R.id.tv_user_product_category)
        private val tvPrice: TextView = itemView.findViewById(R.id.tv_user_product_price)
        private val tvBadge: TextView = itemView.findViewById(R.id.tv_user_product_badge)
        private val tvRating: TextView = itemView.findViewById(R.id.tv_user_product_rating)

        fun bind(product: LocalProduct) {
            val context = itemView.context

            tvName.text = product.name
            tvCategory.text = if (product.originRegion.isNotBlank()) {
                "${product.category} · ${product.originRegion}"
            } else {
                product.category
            }

            val formatter = NumberFormat.getNumberInstance(Locale.US)
            tvPrice.text = "LKR ${formatter.format(product.priceLkr)}"

            if (product.stockQuantity > 0) {
                tvBadge.text = "In Stock"
                tvBadge.background = ContextCompat.getDrawable(context, R.drawable.bg_badge_approved)
                tvBadge.setTextColor(ContextCompat.getColor(context, R.color.admin_success))
            } else {
                tvBadge.text = "Out of Stock"
                tvBadge.background = ContextCompat.getDrawable(context, R.drawable.bg_badge_pending)
                tvBadge.setTextColor(ContextCompat.getColor(context, R.color.admin_danger))
            }

            tvRating.text = "★ ${String.format(Locale.US, "%.1f", product.rating)} (${product.originRegion.ifBlank { "Sri Lanka" }})"

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
                .into(ivImage)

            itemView.setOnClickListener { onProductClick(product) }
        }
    }

    private class ProductDiffCallback : DiffUtil.ItemCallback<LocalProduct>() {
        override fun areItemsTheSame(oldItem: LocalProduct, newItem: LocalProduct): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: LocalProduct, newItem: LocalProduct): Boolean =
            oldItem == newItem
    }
}
