package com.example.lankago.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.lankago.R
import com.example.lankago.data.Review


 // ListAdapter for rendering customer reviews in ShopDetailsFragment.

class ShopReviewAdapter : ListAdapter<Review, ShopReviewAdapter.ReviewViewHolder>(ReviewDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_review, parent, false)
        return ReviewViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReviewViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ReviewViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvInitial: TextView = itemView.findViewById(R.id.tv_avatar_initial)
        private val tvUserName: TextView = itemView.findViewById(R.id.tv_reviewer_name)
        private val tvDate: TextView = itemView.findViewById(R.id.tv_review_date)
        private val tvRating: TextView = itemView.findViewById(R.id.tv_review_rating)
        private val tvComment: TextView = itemView.findViewById(R.id.tv_review_comment)

        fun bind(review: Review) {
            tvUserName.text = review.userName
            tvDate.text = review.date
            tvRating.text = String.format("%.1f", review.rating)
            tvComment.text = review.comment
            tvInitial.text = review.userName.firstOrNull()?.uppercase() ?: "U"
        }
    }

    class ReviewDiffCallback : DiffUtil.ItemCallback<Review>() {
        override fun areItemsTheSame(oldItem: Review, newItem: Review): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Review, newItem: Review): Boolean {
            return oldItem == newItem
        }
    }
}
