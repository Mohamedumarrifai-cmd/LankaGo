package com.example.lankago.fragments.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ProgressBar
import androidx.appcompat.widget.Toolbar
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.lankago.R
import com.example.lankago.adapters.AdminActivityAdapter
import com.example.lankago.data.TraditionalActivity
import com.example.lankago.repository.AdminRepository
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

/**
 * Production-ready Admin screen displaying all Cultural Activities & Tours directly from Firebase Realtime Database.
 * Features:
 * - Real-time observation of `/activities` node.
 * - FAB to create a new activity.
 * - Edit action navigating to AddEditActivityFragment with SafeArgs activityId.
 * - Delete action with MaterialAlertDialog confirmation calling `removeValue()` and Storage photo cleanup.
 */
class AdminActivityListFragment : Fragment() {

    private val adminRepository = AdminRepository()
    private lateinit var activityAdapter: AdminActivityAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_admin_activity_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val toolbar = view.findViewById<Toolbar>(R.id.toolbar_admin_activities)
        val rvActivities = view.findViewById<RecyclerView>(R.id.rv_admin_activities)
        val pbLoading = view.findViewById<ProgressBar>(R.id.pb_activities_loading)
        val layoutEmpty = view.findViewById<LinearLayout>(R.id.layout_empty_activities)
        val fabAdd = view.findViewById<ExtendedFloatingActionButton>(R.id.fab_add_activity)

        toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        // Initialize Adapter with Edit and Delete callbacks
        activityAdapter = AdminActivityAdapter(
            onEditClick = { activity ->
                val bundle = bundleOf("activityId" to activity.id)
                findNavController().navigate(
                    R.id.action_adminActivityListFragment_to_addEditActivityFragment,
                    bundle
                )
            },
            onDeleteClick = { activity ->
                confirmDeleteActivity(activity)
            }
        )

        rvActivities.layoutManager = LinearLayoutManager(requireContext())
        rvActivities.adapter = activityAdapter

        // FAB -> Add New Activity (SafeArgs activityId is null)
        fabAdd.setOnClickListener {
            val bundle = bundleOf("activityId" to null)
            findNavController().navigate(
                R.id.action_adminActivityListFragment_to_addEditActivityFragment,
                bundle
            )
        }

        // Real-time observation of Firebase Realtime Database
        pbLoading.visibility = View.VISIBLE
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                try {
                    adminRepository.observeActivities().collect { activities ->
                        pbLoading.visibility = View.GONE
                        if (activities.isEmpty()) {
                            loadFallbackActivities(pbLoading, rvActivities, layoutEmpty)
                        } else {
                            rvActivities.visibility = View.VISIBLE
                            layoutEmpty.visibility = View.GONE
                            activityAdapter.submitList(activities)
                        }
                    }
                } catch (e: Exception) {
                    pbLoading.visibility = View.GONE
                    loadFallbackActivities(pbLoading, rvActivities, layoutEmpty)
                }
            }
        }
    }

    private fun loadFallbackActivities(
        pbLoading: ProgressBar,
        rvActivities: RecyclerView,
        layoutEmpty: LinearLayout
    ) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val onceList = adminRepository.getAllActivitiesOnce()
                pbLoading.visibility = View.GONE
                if (onceList.isEmpty()) {
                    rvActivities.visibility = View.GONE
                    layoutEmpty.visibility = View.VISIBLE
                } else {
                    rvActivities.visibility = View.VISIBLE
                    layoutEmpty.visibility = View.GONE
                    activityAdapter.submitList(onceList)
                }
            } catch (_: Exception) {
                pbLoading.visibility = View.GONE
                rvActivities.visibility = View.GONE
                layoutEmpty.visibility = View.VISIBLE
            }
        }
    }

    private fun confirmDeleteActivity(activity: TraditionalActivity) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Delete Activity")
            .setMessage("Are you sure you want to permanently remove \"${activity.title}\" from the database and storage?")
            .setPositiveButton("Delete") { _, _ ->
                performDelete(activity)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun performDelete(activity: TraditionalActivity) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                adminRepository.deleteActivity(activityId = activity.id, imageUrl = activity.imageUrl)
                Snackbar.make(
                    requireView(),
                    "Activity \"${activity.title}\" removed successfully",
                    Snackbar.LENGTH_SHORT
                ).show()
            } catch (e: Exception) {
                Snackbar.make(
                    requireView(),
                    "Delete failed: ${e.localizedMessage ?: "Unknown error"}",
                    Snackbar.LENGTH_LONG
                ).setBackgroundTint(requireContext().getColor(R.color.admin_danger)).show()
            }
        }
    }
}
