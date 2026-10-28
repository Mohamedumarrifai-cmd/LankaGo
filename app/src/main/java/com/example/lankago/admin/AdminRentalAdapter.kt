package com.example.lankago.admin

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.lankago.databinding.ItemAdminRentalCardBinding
import com.example.lankago.model.Rental

class AdminRentalAdapter(
    private val onEditClick: (Rental) -> Unit,
    private val onDeleteClick: (Rental) -> Unit
) : ListAdapter<Rental, AdminRentalAdapter.RentalViewHolder>(RentalDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RentalViewHolder {
        val binding = ItemAdminRentalCardBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return RentalViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RentalViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class RentalViewHolder(
        private val binding: ItemAdminRentalCardBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(rental: Rental) {
            binding.tvShopName.text = rental.shopName
            binding.tvCategoryBadge.text = rental.category.ifBlank { "Vehicle Rental" }
            binding.tvLocation.text = rental.location.ifBlank { "Location unspecified" }
            binding.tvContactNumber.text = rental.contactNumber.ifBlank { "No contact info" }
            binding.tvPricePerDay.text = String.format("$%.2f / day", rental.pricePerDay)

            // Edit Icon Click
            binding.btnEdit.setOnClickListener {
                onEditClick(rental)
            }

            // Delete Icon Click
            binding.btnDelete.setOnClickListener {
                onDeleteClick(rental)
            }
        }
    }

    class RentalDiffCallback : DiffUtil.ItemCallback<Rental>() {
        override fun areItemsTheSame(oldItem: Rental, newItem: Rental): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Rental, newItem: Rental): Boolean {
            return oldItem == newItem
        }
    }
}
