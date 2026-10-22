package com.example.lankago.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.lankago.R
import com.example.lankago.adapters.NotificationsAdapter
import com.example.lankago.data.AppNotification
import com.example.lankago.data.NotificationType
import com.example.lankago.utils.LankaGoNotificationHelper

/**
 * Screen displaying user notifications with smart deep-link routing on click.
 */
class NotificationsFragment : Fragment() {

    private lateinit var notificationsAdapter: NotificationsAdapter
    private val notificationList = mutableListOf<AppNotification>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.notifications, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnBack = view.findViewById<ImageView>(R.id.btn_back_notifications)
        val btnClearAll = view.findViewById<TextView>(R.id.btn_clear_all_notifications)
        val rvNotifications = view.findViewById<RecyclerView>(R.id.rv_notifications)
        val layoutEmpty = view.findViewById<View>(R.id.layout_empty_notifications)

        btnBack.setOnClickListener {
            findNavController().navigateUp()
        }

        // Setup Adapter with smart click navigation
        notificationsAdapter = NotificationsAdapter { notification ->
            handleNotificationNavigation(notification)
        }

        rvNotifications.layoutManager = LinearLayoutManager(requireContext())
        rvNotifications.adapter = notificationsAdapter

        btnClearAll.setOnClickListener {
            LankaGoNotificationHelper.clearAllInAppNotifications(requireContext())
            notificationList.clear()
            notificationsAdapter.submitList(emptyList())
            rvNotifications.visibility = View.GONE
            layoutEmpty.visibility = View.VISIBLE
        }

        // Populate notifications
        loadNotifications(rvNotifications, layoutEmpty)
    }

    private fun loadNotifications(recyclerView: RecyclerView, emptyView: View) {
        notificationList.clear()

        // 1. Load dynamic user notifications (trip creations, bookings)
        val dynamicNotifications = LankaGoNotificationHelper.getAllInAppNotifications(requireContext())
        notificationList.addAll(dynamicNotifications)

        // 2. Load system announcements and alerts
        notificationList.addAll(
            listOf(
                AppNotification(
                    id = "notif_1",
                    title = "Rental Booking Confirmed",
                    message = "Your scooter reservation at Casons Rent Galle is confirmed and ready for pickup.",
                    timeAgo = "10 mins ago",
                    type = NotificationType.RENTAL_BOOKING,
                    targetId = "shop_galle_bike",
                    iconRes = R.drawable.ic_home_rental_car
                ),
                AppNotification(
                    id = "notif_2",
                    title = "Artisan Workshop Reminder",
                    message = "Traditional Mask Carving & Painting starts tomorrow at 09:30 AM in Ambalangoda.",
                    timeAgo = "1 hour ago",
                    type = NotificationType.ACTIVITY_BOOKING,
                    targetId = "act_mask_carving",
                    iconRes = R.drawable.ic_home_artisan
                ),
                AppNotification(
                    id = "notif_3",
                    title = "Cultural Festival Alert",
                    message = "Kandy Esala Perahera grand procession begins tonight! View tips and route guidance.",
                    timeAgo = "3 hours ago",
                    type = NotificationType.FESTIVAL_ALERT,
                    targetId = "fest_kandy_esala",
                    iconRes = R.drawable.ic_home_festival
                ),
                AppNotification(
                    id = "notif_4",
                    title = "Itinerary Updated",
                    message = "Ella Highlands 3-day itinerary has been synced with your offline Trip Planner.",
                    timeAgo = "Yesterday",
                    type = NotificationType.TRIP_UPDATE,
                    targetId = "trip_ella_adventure",
                    iconRes = R.drawable.ic_home_trip_plan
                ),
                AppNotification(
                    id = "notif_5",
                    title = "Featured Local Heritage Product",
                    message = "Pure Ceylon Single-Estate Tea sets now available at verified artisan stores.",
                    timeAgo = "2 days ago",
                    type = NotificationType.LOCAL_PRODUCT,
                    targetId = "item_ceylon_tea",
                    iconRes = R.drawable.ic_store
                )
            )
        )

        if (notificationList.isEmpty()) {
            recyclerView.visibility = View.GONE
            emptyView.visibility = View.VISIBLE
        } else {
            recyclerView.visibility = View.VISIBLE
            emptyView.visibility = View.GONE
            notificationsAdapter.submitList(notificationList.toList())
        }
    }

    /**
     * Smart Deep-Link Navigation: Reads notification.type and targetId to route to exact screen.
     */
    private fun handleNotificationNavigation(notification: AppNotification) {
        val navController = findNavController()

        when (notification.type) {
            NotificationType.RENTAL_BOOKING -> {
                // Navigate to Shop Details for the booked rental shop
                val bundle = bundleOf(
                    "shopId" to notification.targetId.ifBlank { "shop_epic_ride" }
                )
                navController.navigate(R.id.shopDetailsFragment, bundle)
            }

            NotificationType.ACTIVITY_BOOKING -> {
                // Navigate to Traditional Activity Details
                val bundle = bundleOf(
                    "traditionalActivityId" to notification.targetId.ifBlank { "act_pottery_making" }
                )
                navController.navigate(R.id.traditionalActivityDetailFragment, bundle)
            }

            NotificationType.FESTIVAL_ALERT -> {
                // Navigate to Cultural Festival Details
                val bundle = bundleOf(
                    "festivalId" to notification.targetId.ifBlank { "fest_kandy_perahera" }
                )
                navController.navigate(R.id.festivalDetailFragment, bundle)
            }

            NotificationType.TRIP_UPDATE -> {
                // Navigate to Trip History or Trip Planner
                try {
                    navController.navigate(R.id.tripHistoryFragment)
                } catch (e: Exception) {
                    navController.navigate(R.id.tripPlannerFragment)
                }
            }

            NotificationType.LOCAL_PRODUCT -> {
                // Navigate to Local Product Heritage Details
                val bundle = bundleOf(
                    "itemId" to notification.targetId.ifBlank { "item_ceylon_tea" }
                )
                navController.navigate(R.id.localItemDetailsFragment, bundle)
            }
        }
    }
}
