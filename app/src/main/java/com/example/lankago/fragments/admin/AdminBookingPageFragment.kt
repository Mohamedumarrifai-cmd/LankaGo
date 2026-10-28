package com.example.lankago.fragments.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.lankago.R
import com.example.lankago.adapters.AdminBookingsAdapter
import com.example.lankago.data.Booking
import com.example.lankago.data.BookingType
import com.example.lankago.repository.AdminRepository
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch


//  Child page fragment hosted within ViewPager2 in AdminBookingsListFragment.
//  Filters reservations according to bookingType ("RENTAL", "ACTIVITY", or "ALL").

class AdminBookingPageFragment : Fragment() {

    private val adminRepository = AdminRepository()
    private lateinit var bookingsAdapter: AdminBookingsAdapter
    private var filterType: String = FILTER_ALL

    companion object {
        const val ARG_FILTER_TYPE = "arg_filter_type"
        const val FILTER_RENTAL = "RENTAL"
        const val FILTER_ACTIVITY = "ACTIVITY"
        const val FILTER_ALL = "ALL"

        fun newInstance(filterType: String): AdminBookingPageFragment {
            val fragment = AdminBookingPageFragment()
            val args = Bundle().apply {
                putString(ARG_FILTER_TYPE, filterType)
            }
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        filterType = arguments?.getString(ARG_FILTER_TYPE) ?: FILTER_ALL
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_admin_bookings_page, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val rvBookings = view.findViewById<RecyclerView>(R.id.rv_page_bookings)
        val pbLoading = view.findViewById<ProgressBar>(R.id.pb_page_loading)
        val layoutEmpty = view.findViewById<LinearLayout>(R.id.layout_empty_page_bookings)
        val tvEmptyTitle = view.findViewById<TextView>(R.id.tv_empty_page_title)
        val tvEmptySubtitle = view.findViewById<TextView>(R.id.tv_empty_page_subtitle)

        // Customise empty state messaging
        when (filterType) {
            FILTER_RENTAL -> {
                tvEmptyTitle.text = "No Rental Bookings"
                tvEmptySubtitle.text = "Customer vehicle and scooter rental reservations will show up here."
            }
            FILTER_ACTIVITY -> {
                tvEmptyTitle.text = "No Activity Bookings"
                tvEmptySubtitle.text = "Customer heritage, cultural tour, and festival bookings will show up here."
            }
            else -> {
                tvEmptyTitle.text = "No Customer Bookings"
                tvEmptySubtitle.text = "All customer bookings will show up here in real time."
            }
        }

        bookingsAdapter = AdminBookingsAdapter(
            onStatusChangeClick = { booking ->
                showStatusChangeDialog(booking)
            }
        )

        rvBookings.layoutManager = LinearLayoutManager(requireContext())
        rvBookings.adapter = bookingsAdapter

        // Realtime observation
        pbLoading.visibility = View.VISIBLE
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                try {
                    adminRepository.observeBookings().collect { allBookings ->
                        pbLoading.visibility = View.GONE
                        val filtered = when (filterType) {
                            FILTER_RENTAL -> allBookings.filter { it.bookingType == BookingType.RENTAL }
                            FILTER_ACTIVITY -> allBookings.filter { it.bookingType == BookingType.ACTIVITY }
                            else -> allBookings
                        }

                        if (filtered.isEmpty()) {
                            loadFallbackBookings(pbLoading, rvBookings, layoutEmpty)
                        } else {
                            rvBookings.visibility = View.VISIBLE
                            layoutEmpty.visibility = View.GONE
                            bookingsAdapter.submitList(filtered)
                        }
                    }
                } catch (e: Exception) {
                    pbLoading.visibility = View.GONE
                    loadFallbackBookings(pbLoading, rvBookings, layoutEmpty)
                }
            }
        }
    }

    private fun loadFallbackBookings(
        pbLoading: ProgressBar,
        rvBookings: RecyclerView,
        layoutEmpty: LinearLayout
    ) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val onceList = adminRepository.getAllBookingsOnce()
                pbLoading.visibility = View.GONE
                val filtered = when (filterType) {
                    FILTER_RENTAL -> onceList.filter { it.bookingType == BookingType.RENTAL }
                    FILTER_ACTIVITY -> onceList.filter { it.bookingType == BookingType.ACTIVITY }
                    else -> onceList
                }
                if (filtered.isEmpty()) {
                    rvBookings.visibility = View.GONE
                    layoutEmpty.visibility = View.VISIBLE
                } else {
                    rvBookings.visibility = View.VISIBLE
                    layoutEmpty.visibility = View.GONE
                    bookingsAdapter.submitList(filtered)
                }
            } catch (_: Exception) {
                pbLoading.visibility = View.GONE
                rvBookings.visibility = View.GONE
                layoutEmpty.visibility = View.VISIBLE
            }
        }
    }

    private fun showStatusChangeDialog(booking: Booking) {
        val statuses = arrayOf("CONFIRMED", "PENDING", "CANCELLED")
        val currentIndex = statuses.indexOf(booking.status.name).coerceAtLeast(0)

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Update Booking Status")
            .setSingleChoiceItems(statuses, currentIndex) { dialog, which ->
                val newStatus = statuses[which]
                dialog.dismiss()
                updateBookingStatusInFirebase(booking, newStatus)
            }
            .setNegativeButton("Dismiss", null)
            .show()
    }

    private fun updateBookingStatusInFirebase(booking: Booking, newStatus: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                adminRepository.updateBookingStatus(booking.id, newStatus)
                Snackbar.make(
                    requireView(),
                    "Booking status updated to $newStatus",
                    Snackbar.LENGTH_SHORT
                ).show()
            } catch (e: Exception) {
                Snackbar.make(
                    requireView(),
                    "Status update failed: ${e.localizedMessage ?: "Network error"}",
                    Snackbar.LENGTH_LONG
                ).setBackgroundTint(requireContext().getColor(R.color.admin_danger)).show()
            }
        }
    }
}
