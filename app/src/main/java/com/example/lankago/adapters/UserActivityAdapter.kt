package com.example.lankago.adapters

import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.lankago.R
import com.example.lankago.data.TraditionalActivity
import com.google.android.material.imageview.ShapeableImageView
import java.text.NumberFormat
import java.util.Locale

/**
 * ListAdapter for rendering live Firebase-backed traditional cultural activities and tours
 * on the User-facing TraditionalActivitiesFragment.
 */
class UserActivityAdapter(
    private val onActivityClick: (TraditionalActivity) -> Unit
) : ListAdapter<TraditionalActivity, UserActivityAdapter.UserActivityViewHolder>(ActivityDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserActivityViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_user_activity, parent, false)
        return UserActivityViewHolder(view, onActivityClick)
    }

    override fun onBindViewHolder(holder: UserActivityViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class UserActivityViewHolder(
        itemView: View,
        private val onActivityClick: (TraditionalActivity) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        private val ivImage: ShapeableImageView = itemView.findViewById(R.id.iv_user_activity_image)
        private val tvTitle: TextView = itemView.findViewById(R.id.tv_user_activity_title)
        private val tvLocation: TextView = itemView.findViewById(R.id.tv_user_activity_location)
        private val tvRating: TextView = itemView.findViewById(R.id.tv_user_activity_rating)

        fun bind(activity: TraditionalActivity) {
            val context = itemView.context

            tvTitle.text = activity.title
            tvLocation.text = activity.location.ifBlank { "Sri Lanka" }
            tvRating.text = String.format(Locale.US, "%.1f", if (activity.rating > 0) activity.rating else 4.8f)

            val imageSource: Any = when {
                activity.imageUrl.startsWith("data:image") -> {
                    try {
                        val base64Data = activity.imageUrl.substringAfter(",")
                        Base64.decode(base64Data, Base64.DEFAULT)
                    } catch (_: Exception) {
                        R.drawable.traditional_dance
                    }
                }
                activity.imageUrl.isNotBlank() -> activity.imageUrl
                activity.imageRes != 0 -> activity.imageRes
                else -> R.drawable.traditional_dance
            }

            Glide.with(context)
                .load(imageSource)
                .placeholder(R.drawable.traditional_dance)
                .error(R.drawable.traditional_dance)
                .centerCrop()
                .into(ivImage)

            itemView.setOnClickListener { onActivityClick(activity) }
        }
    }

    private class ActivityDiffCallback : DiffUtil.ItemCallback<TraditionalActivity>() {
        override fun areItemsTheSame(oldItem: TraditionalActivity, newItem: TraditionalActivity): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: TraditionalActivity, newItem: TraditionalActivity): Boolean =
            oldItem == newItem
    }
}
