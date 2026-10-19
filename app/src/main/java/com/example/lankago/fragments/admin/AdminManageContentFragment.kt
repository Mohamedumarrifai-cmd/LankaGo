package com.example.lankago.fragments.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.lankago.R

class AdminManageContentFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.admin_manage_content, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Back Button
        view.findViewById<View>(R.id.btn_back_content)?.setOnClickListener {
            findNavController().navigateUp()
        }

        // Setup Tabs
        setupContentTabs(view)

        // Edit Item 1
        view.findViewById<View>(R.id.btn_edit_content_1)?.setOnClickListener {
            Toast.makeText(requireContext(), "Edit: Sigiriya Rock Fortress", Toast.LENGTH_SHORT).show()
        }

        // Delete Item 1
        view.findViewById<View>(R.id.btn_delete_content_1)?.setOnClickListener {
            Toast.makeText(requireContext(), "Deleted 'Sigiriya Rock Fortress'", Toast.LENGTH_SHORT).show()
            view.findViewById<View>(R.id.card_content_1)?.visibility = View.GONE
        }

        // Edit Item 2
        view.findViewById<View>(R.id.btn_edit_content_2)?.setOnClickListener {
            Toast.makeText(requireContext(), "Edit: Nine Arches Bridge", Toast.LENGTH_SHORT).show()
        }

        // Delete Item 2
        view.findViewById<View>(R.id.btn_delete_content_2)?.setOnClickListener {
            Toast.makeText(requireContext(), "Deleted 'Nine Arches Bridge'", Toast.LENGTH_SHORT).show()
            view.findViewById<View>(R.id.card_content_2)?.visibility = View.GONE
        }

        // Edit Item 3
        view.findViewById<View>(R.id.btn_edit_content_3)?.setOnClickListener {
            Toast.makeText(requireContext(), "Edit: Kandy Esala Perahera", Toast.LENGTH_SHORT).show()
        }

        // Delete Item 3
        view.findViewById<View>(R.id.btn_delete_content_3)?.setOnClickListener {
            Toast.makeText(requireContext(), "Deleted 'Kandy Esala Perahera'", Toast.LENGTH_SHORT).show()
            view.findViewById<View>(R.id.card_content_3)?.visibility = View.GONE
        }

        // Add New Item FAB
        view.findViewById<View>(R.id.fab_add_content)?.setOnClickListener {
            Toast.makeText(requireContext(), "Add New Destination or Event", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupContentTabs(view: View) {
        val tabPlaces = view.findViewById<TextView>(R.id.tab_places)
        val tabActivities = view.findViewById<TextView>(R.id.tab_activities)
        val tabFestivals = view.findViewById<TextView>(R.id.tab_festivals)

        val tabs = listOf(tabPlaces, tabActivities, tabFestivals)

        fun selectTab(selected: TextView, tabName: String) {
            tabs.forEach { tab ->
                if (tab == selected) {
                    tab?.setBackgroundResource(R.drawable.bg_admin_chip_active)
                    tab?.setTextColor(resources.getColor(R.color.white, null))
                } else {
                    tab?.setBackgroundResource(R.drawable.bg_admin_chip_inactive)
                    tab?.setTextColor(resources.getColor(R.color.admin_dark_blue, null))
                }
            }
            Toast.makeText(requireContext(), "Showing: $tabName", Toast.LENGTH_SHORT).show()
        }

        tabPlaces?.setOnClickListener { selectTab(tabPlaces, "Places") }
        tabActivities?.setOnClickListener { selectTab(tabActivities, "Activities") }
        tabFestivals?.setOnClickListener { selectTab(tabFestivals, "Festivals") }
    }
}
