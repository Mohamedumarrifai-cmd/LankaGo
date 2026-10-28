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
import com.example.lankago.adapters.AdminMallAdapter
import com.example.lankago.data.Mall
import com.example.lankago.data.MallDataProvider
import com.example.lankago.repository.AdminRepository
import com.example.lankago.utils.safeNavigate
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch


class AdminMallListFragment : Fragment() {

    private val adminRepository = AdminRepository()
    private lateinit var mallAdapter: AdminMallAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_admin_mall_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val toolbar = view.findViewById<Toolbar>(R.id.toolbar_admin_malls)
        val rvMalls = view.findViewById<RecyclerView>(R.id.rv_admin_malls)
        val pbLoading = view.findViewById<ProgressBar>(R.id.pb_admin_malls_loading)
        val layoutEmpty = view.findViewById<LinearLayout>(R.id.layout_empty_admin_malls)
        val fabAdd = view.findViewById<ExtendedFloatingActionButton>(R.id.fab_add_mall)

        toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        mallAdapter = AdminMallAdapter(
            onEditClick = { mall ->
                val bundle = bundleOf("mallId" to mall.id)
                findNavController().safeNavigate(
                    R.id.action_adminMallListFragment_to_addEditMallFragment,
                    bundle
                )
            },
            onDeleteClick = { mall ->
                confirmDeleteMall(mall)
            }
        )

        rvMalls.layoutManager = LinearLayoutManager(requireContext())
        rvMalls.adapter = mallAdapter

        fabAdd.setOnClickListener {
            val bundle = bundleOf("mallId" to null)
            findNavController().safeNavigate(
                R.id.action_adminMallListFragment_to_addEditMallFragment,
                bundle
            )
        }

        // Realtime sync from Firebase
        pbLoading.visibility = View.VISIBLE
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                try {
                    adminRepository.observeMalls().collect { malls ->
                        pbLoading.visibility = View.GONE
                        if (malls.isEmpty()) {
                            // Seed or fallback to default malls
                            mallAdapter.submitList(MallDataProvider.defaultMalls)
                            rvMalls.visibility = View.VISIBLE
                            layoutEmpty.visibility = View.GONE
                        } else {
                            rvMalls.visibility = View.VISIBLE
                            layoutEmpty.visibility = View.GONE
                            mallAdapter.submitList(malls)
                        }
                    }
                } catch (_: Exception) {
                    pbLoading.visibility = View.GONE
                    mallAdapter.submitList(MallDataProvider.defaultMalls)
                    rvMalls.visibility = View.VISIBLE
                    layoutEmpty.visibility = View.GONE
                }
            }
        }
    }

    private fun confirmDeleteMall(mall: Mall) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Delete Shopping Mall?")
            .setMessage("Are you sure you want to permanently delete '${mall.name}'? This change will reflect in real time for all travelers.")
            .setPositiveButton("Delete") { _, _ ->
                viewLifecycleOwner.lifecycleScope.launch {
                    try {
                        adminRepository.deleteMall(mall.id, mall.imageUrl)
                        Snackbar.make(requireView(), "${mall.name} successfully deleted", Snackbar.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        Snackbar.make(requireView(), "Error deleting: ${e.message}", Snackbar.LENGTH_LONG).show()
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
