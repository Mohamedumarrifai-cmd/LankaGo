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
import com.example.lankago.data.AppNotification
import com.example.lankago.data.NotificationType
import com.google.android.material.card.MaterialCardView

/**
 * ListAdapter for rendering notifications with smart click handling.
 */
class NotificationsAdapter(
    private val onNotificationClick: (AppNotification) -> Unit
) : ListAdapter<AppNotification, NotificationsAdapter.NotificationViewHolder>(NotificationDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_notification, parent, false)
        return NotificationViewHolder(view, onNotificationClick)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class NotificationViewHolder(
        itemView: View,
        private val onNotificationClick: (AppNotification) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {

        private val cardRoot: MaterialCardView = itemView.findViewById(R.id.card_notification_root)
        private val ivIcon: ImageView = itemView.findViewById(R.id.iv_notification_icon)
        private val tvTitle: TextView = itemView.findViewById(R.id.tv_notification_title)
        private val tvMessage: TextView = itemView.findViewById(R.id.tv_notification_message)
        private val tvTime: TextView = itemView.findViewById(R.id.tv_notification_time)

        fun bind(item: AppNotification) {
            tvTitle.text = item.title
            tvMessage.text = item.message
            tvTime.text = item.timeAgo

            // Smart icon allocation based on notification type
            val iconDrawableRes = if (item.iconRes != 0) {
                item.iconRes
            } else {
                when (item.type) {
                    NotificationType.RENTAL_BOOKING -> R.drawable.ic_home_rental_car
                    NotificationType.ACTIVITY_BOOKING -> R.drawable.ic_home_artisan
                    NotificationType.TRIP_UPDATE -> R.drawable.ic_home_trip_plan
                    NotificationType.FESTIVAL_ALERT -> R.drawable.ic_home_festival
                    NotificationType.LOCAL_PRODUCT -> R.drawable.ic_store
                }
            }
            ivIcon.setImageResource(iconDrawableRes)

            cardRoot.setOnClickListener {
                onNotificationClick(item)
            }
        }
    }

    class NotificationDiffCallback : DiffUtil.ItemCallback<AppNotification>() {
        override fun areItemsTheSame(oldItem: AppNotification, newItem: AppNotification): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: AppNotification, newItem: AppNotification): Boolean {
            return oldItem == newItem
        }
    }
}
