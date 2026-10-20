package com.example.lankago.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.lankago.R
import com.example.lankago.data.Review

class ReviewAdapter(
    private var reviews: List<Review>
) : RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder>() {

    class ReviewViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvAvatarInitial: TextView = itemView.findViewById(R.id.tv_avatar_initial)
        val tvReviewerName: TextView = itemView.findViewById(R.id.tv_reviewer_name)
        val tvReviewDate: TextView = itemView.findViewById(R.id.tv_review_date)
        val tvReviewRating: TextView = itemView.findViewById(R.id.tv_review_rating)
        val tvReviewComment: TextView = itemView.findViewById(R.id.tv_review_comment)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_review, parent, false)
        return ReviewViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReviewViewHolder, position: Int) {
        val item = reviews[position]
        holder.tvAvatarInitial.text = item.userName.firstOrNull()?.toString()?.uppercase() ?: "U"
        holder.tvReviewerName.text = item.userName
        holder.tvReviewDate.text = item.date
        holder.tvReviewRating.text = item.rating.toString()
        holder.tvReviewComment.text = item.comment
    }

    override fun getItemCount(): Int = reviews.size

    fun updateData(newReviews: List<Review>) {
        reviews = newReviews
        notifyDataSetChanged()
    }
}
