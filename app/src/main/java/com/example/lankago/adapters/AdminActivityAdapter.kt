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
import com.example.lankago.data.TraditionalActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.imageview.ShapeableImageView
import java.text.NumberFormat
import java.util.Locale


//  Production-ready ListAdapter for administering Cultural Activities & Tours in LankaGo.
//  Supports Glide image loading with placeholder/fallback and fast DiffUtil comparisons.

class AdminActivityAdapter(
    private val onEditClick: (TraditionalActivity) -> Unit,  //call the function when click the edit button
    private val onDeleteClick: (TraditionalActivity) -> Unit
) : ListAdapter<TraditionalActivity, AdminActivityAdapter.ActivityViewHolder>(ActivityDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ActivityViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_admin_activity, parent, false)
        return ActivityViewHolder(view)
    }
    //inflate()- Turn the UI layout created in XML into a View that can be shown on Android.

    override fun onBindViewHolder(holder: ActivityViewHolder, position: Int) {
        holder.bind(getItem(position))  //onBindViewHolder==list eke eka eka item ekata addala data show kereema.
    }
    //onCreateViewHolder- item eke UI View eka hadanawa
    //onBindViewHolder- e view ekata adala data eka daanawa


    inner class ActivityViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivThumb: ShapeableImageView = itemView.findViewById(R.id.iv_admin_activity_thumb)
        private val tvTitle: TextView = itemView.findViewById(R.id.tv_admin_activity_title)
        private val tvCategory: TextView = itemView.findViewById(R.id.tv_admin_activity_category)
        private val tvMeta: TextView = itemView.findViewById(R.id.tv_admin_activity_meta)
        private val tvPrice: TextView = itemView.findViewById(R.id.tv_admin_activity_price)
        private val tvHost: TextView = itemView.findViewById(R.id.tv_admin_activity_host)
        private val tvLocation: TextView = itemView.findViewById(R.id.tv_admin_activity_location)
        private val btnEdit: MaterialButton = itemView.findViewById(R.id.btn_edit_activity)
        private val btnDelete: MaterialButton = itemView.findViewById(R.id.btn_delete_activity)

        fun bind(activity: TraditionalActivity) {   //provide data for UI
            val context = itemView.context

            tvTitle.text = activity.title
            tvCategory.text = activity.category.ifBlank { "Cultural Tour" }
            tvMeta.text = "${activity.duration} · ${activity.location}"

            // Currency formatting
            val formatter = NumberFormat.getNumberInstance(Locale.US)
            tvPrice.text = "LKR ${formatter.format(activity.priceLkr)} / person"

            tvHost.text = if (activity.hostName.isNotBlank()) "Host: ${activity.hostName}"
            else "Certified Tour Guide"
            tvLocation.text = "Address: ${activity.address.ifBlank { activity.location }}"

            // Image loading with Glide
            val imageSource: Any = when {
                activity.imageUrl.isNotBlank() -> activity.imageUrl
                activity.imageRes != 0 -> activity.imageRes
                else -> R.drawable.traditional_dance
            }

            Glide.with(context)
                .load(imageSource)
                .placeholder(R.drawable.traditional_dance)
                .error(R.drawable.traditional_dance)
                .centerCrop()
                .into(ivThumb)

            btnEdit.setOnClickListener { onEditClick(activity) }
            btnDelete.setOnClickListener { onDeleteClick(activity) }
        }
    }

    private class ActivityDiffCallback : DiffUtil.ItemCallback<TraditionalActivity>() {
        override fun areItemsTheSame(oldItem: TraditionalActivity, newItem: TraditionalActivity): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: TraditionalActivity, newItem: TraditionalActivity): Boolean {
            return oldItem == newItem
        }
    }
}
