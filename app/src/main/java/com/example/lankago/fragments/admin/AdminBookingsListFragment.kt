package com.example.lankago.fragments.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.example.lankago.R
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator


//  Production-ready Admin screen displaying customer reservations.
//  Organized cleanly via TabLayout and ViewPager2 into:
//  "Rental Bookings"
//  "Activity Bookings"
//  "All Bookings"

class AdminBookingsListFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_admin_bookings_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val toolbar = view.findViewById<Toolbar>(R.id.toolbar_admin_bookings)
        val tabLayout = view.findViewById<TabLayout>(R.id.tab_layout_admin_bookings)
        val viewPager = view.findViewById<ViewPager2>(R.id.vp_admin_bookings)

        toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        val tabTitles = listOf("Rental Bookings", "Activity Bookings", "All Bookings")

        viewPager.adapter = object : FragmentStateAdapter(this) {
            override fun getItemCount(): Int = tabTitles.size

            override fun createFragment(position: Int): Fragment {
                return when (position) {
                    0 -> AdminBookingPageFragment.newInstance(AdminBookingPageFragment.FILTER_RENTAL)
                    1 -> AdminBookingPageFragment.newInstance(AdminBookingPageFragment.FILTER_ACTIVITY)
                    else -> AdminBookingPageFragment.newInstance(AdminBookingPageFragment.FILTER_ALL)
                }
            }
        }

        // Attach TabLayout to ViewPager2
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = tabTitles[position]
        }.attach()
    }
}
