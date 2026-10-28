package com.example.lankago.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.lankago.R
import com.example.lankago.data.LocalItem
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView

//  Modern ListAdapter for rendering items in the Local Products, Markets, and Traditional Medicine tabs.

class LocalItemListAdapter(
    private val onItemClick: (LocalItem) -> Unit
) : ListAdapter<LocalItem, LocalItemListAdapter.LocalItemViewHolder>(LocalItemDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LocalItemViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_local_product_card, parent, false)
        return LocalItemViewHolder(view, onItemClick)
    }

    override fun onBindViewHolder(holder: LocalItemViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class LocalItemViewHolder(
        itemView: View,
        private val onItemClick: (LocalItem) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {

        private val cardRoot: MaterialCardView = itemView.findViewById(R.id.card_local_item)
        private val ivCover: ImageView = itemView.findViewById(R.id.iv_local_card_image)
        private val tvCategory: TextView = itemView.findViewById(R.id.tv_local_card_category)
        private val tvPrice: TextView = itemView.findViewById(R.id.tv_local_card_price)
        private val tvTitle: TextView = itemView.findViewById(R.id.tv_local_card_title)
        private val tvDesc: TextView = itemView.findViewById(R.id.tv_local_card_desc)
        private val tvRegion: TextView = itemView.findViewById(R.id.tv_local_card_region)
        private val tvRating: TextView = itemView.findViewById(R.id.tv_local_card_rating)
        private val btnExplore: MaterialButton = itemView.findViewById(R.id.btn_local_card_explore)

        fun bind(item: LocalItem) {
            tvTitle.text = item.title
            tvCategory.text = item.category.uppercase()
            tvDesc.text = item.description
            tvRegion.text = item.famousCity
            tvPrice.text = item.priceIndication
            tvRating.text = String.format("%.2f", item.rating)

            if (item.imageRes != 0) {
                ivCover.setImageResource(item.imageRes)
            } else {
                ivCover.setImageResource(R.drawable.ceylon_tea_set)
            }

            cardRoot.setOnClickListener { onItemClick(item) }
            btnExplore.setOnClickListener { onItemClick(item) }
        }
    }

    class LocalItemDiffCallback : DiffUtil.ItemCallback<LocalItem>() {
        override fun areItemsTheSame(oldItem: LocalItem, newItem: LocalItem): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: LocalItem, newItem: LocalItem): Boolean {
            return oldItem == newItem
        }
    }
}
