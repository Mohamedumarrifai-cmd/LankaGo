package com.example.lankago.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.lankago.R
import com.example.lankago.data.FestivalScheduleItem

class FestivalScheduleAdapter(
    private var items: List<FestivalScheduleItem>
) : RecyclerView.Adapter<FestivalScheduleAdapter.ScheduleViewHolder>() {

    class ScheduleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvDayLabel: TextView = itemView.findViewById(R.id.tv_schedule_day_label)
        val tvTime: TextView = itemView.findViewById(R.id.tv_schedule_time)
        val tvTitle: TextView = itemView.findViewById(R.id.tv_schedule_title)
        val tvDescription: TextView = itemView.findViewById(R.id.tv_schedule_description)
        val lineView: View = itemView.findViewById(R.id.view_timeline_line)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScheduleViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_festival_schedule, parent, false)
        return ScheduleViewHolder(view)
    }

    override fun onBindViewHolder(holder: ScheduleViewHolder, position: Int) {
        val item = items[position]
        holder.tvDayLabel.text = item.dayLabel
        holder.tvTime.text = item.time
        holder.tvTitle.text = item.title
        holder.tvDescription.text = item.description

        // Hide bottom connecting line on the last timeline item
        if (position == items.size - 1) {
            holder.lineView.visibility = View.INVISIBLE
        } else {
            holder.lineView.visibility = View.VISIBLE
        }
    }

    override fun getItemCount(): Int = items.size

    fun updateData(newItems: List<FestivalScheduleItem>) {
        this.items = newItems
        notifyDataSetChanged()
    }
}
