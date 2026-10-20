package com.example.lankago.fragments.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.lankago.R

class AdminManageVendorsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.admin_manage_vendors, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Back Button
        view.findViewById<View>(R.id.btn_back)?.setOnClickListener {
            findNavController().navigateUp()
        }

        // Setup Filter Chips (Pending, Approved, Rejected)
        setupFilterChips(view)

        // Vendor 1: Ceylon Artisan Pottery
        view.findViewById<AppCompatButton>(R.id.btn_approve_vendor_1)?.setOnClickListener {
            Toast.makeText(requireContext(), "Vendor Approved! Published to User Marketplace.", Toast.LENGTH_SHORT).show()
            view.findViewById<TextView>(R.id.tv_vendor_status_1)?.apply {
                text = "Approved"
                setBackgroundResource(R.drawable.bg_badge_approved)
                setTextColor(resources.getColor(R.color.admin_success, null))
            }
        }

        view.findViewById<AppCompatButton>(R.id.btn_reject_vendor_1)?.setOnClickListener {
            Toast.makeText(requireContext(), "Vendor Application Rejected", Toast.LENGTH_SHORT).show()
            view.findViewById<TextView>(R.id.tv_vendor_status_1)?.apply {
                text = "Rejected"
                setBackgroundResource(R.drawable.bg_button_danger_solid_pill)
                setTextColor(resources.getColor(R.color.white, null))
            }
        }

        // Vendor 2: Mirissa Cinnamon Grove
        view.findViewById<AppCompatButton>(R.id.btn_approve_vendor_2)?.setOnClickListener {
            Toast.makeText(requireContext(), "Vendor Approved! Published to User Marketplace.", Toast.LENGTH_SHORT).show()
            view.findViewById<TextView>(R.id.tv_vendor_status_2)?.apply {
                text = "Approved"
                setBackgroundResource(R.drawable.bg_badge_approved)
                setTextColor(resources.getColor(R.color.admin_success, null))
            }
        }

        view.findViewById<AppCompatButton>(R.id.btn_reject_vendor_2)?.setOnClickListener {
            Toast.makeText(requireContext(), "Vendor Application Rejected", Toast.LENGTH_SHORT).show()
            view.findViewById<TextView>(R.id.tv_vendor_status_2)?.apply {
                text = "Rejected"
                setBackgroundResource(R.drawable.bg_button_danger_solid_pill)
                setTextColor(resources.getColor(R.color.white, null))
            }
        }
    }

    private fun setupFilterChips(view: View) {
        val chipPending = view.findViewById<TextView>(R.id.chip_pending)
        val chipApproved = view.findViewById<TextView>(R.id.chip_approved)
        val chipRejected = view.findViewById<TextView>(R.id.chip_rejected)

        val chips = listOf(chipPending, chipApproved, chipRejected)

        fun selectChip(selected: TextView, filterName: String) {
            chips.forEach { chip ->
                if (chip == selected) {
                    chip?.setBackgroundResource(R.drawable.bg_admin_chip_active)
                    chip?.setTextColor(resources.getColor(R.color.white, null))
                } else {
                    chip?.setBackgroundResource(R.drawable.bg_admin_chip_inactive)
                    chip?.setTextColor(resources.getColor(R.color.admin_dark_blue, null))
                }
            }
            Toast.makeText(requireContext(), "Viewing: $filterName Vendors", Toast.LENGTH_SHORT).show()
        }

        chipPending?.setOnClickListener { selectChip(chipPending, "Pending") }
        chipApproved?.setOnClickListener { selectChip(chipApproved, "Approved") }
        chipRejected?.setOnClickListener { selectChip(chipRejected, "Rejected") }
    }
}
