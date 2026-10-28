package com.example.lankago.fragments

import android.graphics.Typeface
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.lankago.R
import com.example.lankago.adapters.UserActivityAdapter
import com.example.lankago.data.TraditionalActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

/**
 * Traditional Activities & Cultural Tours Fragment.
 * Connected directly via ValueEventListener to Firebase Realtime Database node `/activities`.
 * Displays live artisan workshops, masterclasses, and guided tours added from the Admin panel.
 */
class TraditionalActivitiesFragment : Fragment() {

    companion object {
        private const val TAG = "ActivitiesSync"
    }

    private lateinit var userActivityAdapter: UserActivityAdapter
    private lateinit var databaseRef: DatabaseReference
    private var activitiesValueEventListener: ValueEventListener? = null

    private lateinit var tvAdminHeading: TextView
    private lateinit var rvFirebaseActivities: RecyclerView
    private lateinit var etSearch: EditText

    private lateinit var chipAll: TextView
    private lateinit var chipCrafts: TextView
    private lateinit var chipTours: TextView
    private lateinit var chipWorkshops: TextView

    private var allFirebaseActivities: List<TraditionalActivity> = emptyList()
    private var currentCategoryTag: String = "All"
    private var currentSearchQuery: String = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.traditional_activities, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews(view)
        setupTopBar(view)
        setupRecyclerView()
        setupSearchAndFilterChips(view)
        setupStaticCards(view)
        setupFirebaseActivitiesSync()
    }

    private fun initViews(view: View) {
        tvAdminHeading = view.findViewById(R.id.tv_admin_activities_heading)
        rvFirebaseActivities = view.findViewById(R.id.rv_firebase_activities)
        etSearch = view.findViewById(R.id.et_search_activities)

        chipAll = view.findViewById(R.id.chip_activity_all)
        chipCrafts = view.findViewById(R.id.chip_activity_crafts)
        chipTours = view.findViewById(R.id.chip_activity_tours)
        chipWorkshops = view.findViewById(R.id.chip_activity_workshops)
    }

    private fun setupTopBar(view: View) {
        view.findViewById<ImageView>(R.id.btn_back_traditional)?.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupRecyclerView() {
        val activeTripId = arguments?.getString("activeTripId") ?: ""

        userActivityAdapter = UserActivityAdapter { activity ->
            val bundle = bundleOf(
                "traditionalActivityId" to activity.id,
                "activityId" to activity.id,
                "activityTitle" to activity.title,
                "activeTripId" to activeTripId
            )
            findNavController().navigate(
                R.id.action_traditionalActivitiesFragment_to_traditionalActivityDetailFragment,
                bundle
            )
        }

        rvFirebaseActivities.layoutManager = LinearLayoutManager(requireContext())
        rvFirebaseActivities.adapter = userActivityAdapter
        rvFirebaseActivities.isNestedScrollingEnabled = false
    }

    /**
     * Attaches a real-time ValueEventListener to `/activities`.
     * Receives live updates whenever an Admin adds, updates, or deletes an activity.
     */
    private fun setupFirebaseActivitiesSync() {
        databaseRef = FirebaseDatabase.getInstance().reference.child("activities")

        Log.d(TAG, "Attaching ValueEventListener to Firebase node: ${databaseRef.path}")

        activitiesValueEventListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d(TAG, "onDataChange: Snapshot received from /activities. Children count = ${snapshot.childrenCount}")

                val activitiesList = mutableListOf<TraditionalActivity>()

                for (child in snapshot.children) {
                    try {
                        var activity = child.getValue(TraditionalActivity::class.java)
                        if (activity != null) {
                            if (activity.id.isBlank()) {
                                activity = activity.copy(id = child.key ?: "")
                            }
                            activitiesList.add(activity)
                            Log.d(TAG, "✓ Parsed activity: '${activity.title}' [ID: ${activity.id}, Category: '${activity.category}', Price: LKR ${activity.priceLkr}]")
                        } else {
                            Log.w(TAG, "child.getValue returned null for key '${child.key}'. Attempting manual extraction.")
                            parseActivityManually(child)?.let {
                                activitiesList.add(it)
                                Log.d(TAG, "✓ Manually parsed fallback: '${it.title}' [ID: ${it.id}]")
                            }
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error deserializing child '${child.key}': ${e.message}. Using resilient manual parsing.", e)
                        parseActivityManually(child)?.let {
                            activitiesList.add(it)
                            Log.d(TAG, "✓ Manually recovered: '${it.title}' [ID: ${it.id}]")
                        }
                    }
                }

                allFirebaseActivities = activitiesList
                Log.d(TAG, "Total activities in memory: ${allFirebaseActivities.size}. Applying filter: Category='$currentCategoryTag', Query='$currentSearchQuery'")

                applyFilter()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "onCancelled: Firebase Realtime Database read failed. Code: ${error.code}, Message: ${error.message}", error.toException())
            }
        }

        databaseRef.addValueEventListener(activitiesValueEventListener as ValueEventListener)
    }

    /**
     * Resilient manual parser guarding against potential type conversion issues (e.g. price stored as Long or String).
     */
    private fun parseActivityManually(child: DataSnapshot): TraditionalActivity? {
        return try {
            val key = child.key ?: ""
            val title = child.child("title").getValue(String::class.java) ?: return null
            val category = child.child("category").getValue(String::class.java) ?: "Culture"
            val location = child.child("location").getValue(String::class.java) ?: "Sri Lanka"
            val address = child.child("address").getValue(String::class.java) ?: location
            val duration = child.child("duration").getValue(String::class.java) ?: "2.0 Hours"
            val hostName = child.child("hostName").getValue(String::class.java) ?: "Master Artisan"
            val description = child.child("description").getValue(String::class.java) ?: ""
            val imageUrl = child.child("imageUrl").getValue(String::class.java) ?: ""

            val priceVal = child.child("priceLkr").value
            val priceLkr = when (priceVal) {
                is Number -> priceVal.toDouble()
                is String -> priceVal.toDoubleOrNull() ?: 0.0
                else -> 0.0
            }

            val ratingVal = child.child("rating").value
            val rating = when (ratingVal) {
                is Number -> ratingVal.toFloat()
                is String -> ratingVal.toFloatOrNull() ?: 5.0f
                else -> 5.0f
            }

            TraditionalActivity(
                id = key,
                title = title,
                category = category,
                location = location,
                address = address,
                duration = duration,
                hostName = hostName,
                priceLkr = priceLkr,
                rating = rating,
                description = description,
                imageUrl = imageUrl
            )
        } catch (ex: Exception) {
            Log.e(TAG, "Manual fallback extraction failed for child ${child.key}: ${ex.message}")
            null
        }
    }

    private fun setupSearchAndFilterChips(view: View) {
        val chips = listOf(chipAll, chipCrafts, chipTours, chipWorkshops)

        fun selectChip(selectedChip: TextView, categoryTag: String) {
            currentCategoryTag = categoryTag
            for (chip in chips) {
                if (chip == selectedChip) {
                    chip.setBackgroundResource(R.drawable.bg_chip_selected)
                    chip.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                    chip.setTypeface(null, Typeface.BOLD)
                } else {
                    chip.setBackgroundResource(R.drawable.bg_chip_unselected)
                    chip.setTextColor(0xFF666666.toInt())
                    chip.setTypeface(null, Typeface.NORMAL)
                }
            }
            applyFilter()
        }

        chipAll.setOnClickListener { selectChip(chipAll, "All") }
        chipCrafts.setOnClickListener { selectChip(chipCrafts, "Crafts") }
        chipTours.setOnClickListener { selectChip(chipTours, "Tours") }
        chipWorkshops.setOnClickListener { selectChip(chipWorkshops, "Workshops") }

        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                currentSearchQuery = s?.toString()?.trim().orEmpty()
                applyFilter()
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun applyFilter() {
        val query = currentSearchQuery.lowercase()
        val category = currentCategoryTag

        val filtered = allFirebaseActivities.filter { activity ->
            // Category condition
            val matchesCategory = when (category) {
                "All" -> true
                "Crafts" -> activity.category.contains("craft", ignoreCase = true) ||
                        activity.category.contains("pottery", ignoreCase = true) ||
                        activity.category.contains("batik", ignoreCase = true) ||
                        activity.category.contains("carving", ignoreCase = true)
                "Tours" -> activity.category.contains("tour", ignoreCase = true) ||
                        activity.category.contains("estate", ignoreCase = true) ||
                        activity.category.contains("walk", ignoreCase = true) ||
                        activity.category.contains("safari", ignoreCase = true)
                "Workshops" -> activity.category.contains("workshop", ignoreCase = true) ||
                        activity.category.contains("class", ignoreCase = true) ||
                        activity.category.contains("cooking", ignoreCase = true) ||
                        activity.category.contains("dance", ignoreCase = true)
                else -> true
            }

            // Search query condition
            val matchesQuery = query.isEmpty() ||
                    activity.title.lowercase().contains(query) ||
                    activity.location.lowercase().contains(query) ||
                    activity.category.lowercase().contains(query) ||
                    activity.description.lowercase().contains(query) ||
                    activity.hostName.lowercase().contains(query)

            matchesCategory && matchesQuery
        }.ifEmpty {
            // Resilient fallback: If specific chip filter has no items, but query is empty, show all activities
            if (query.isEmpty() && category != "All") allFirebaseActivities else emptyList()
        }

        Log.d(TAG, "applyFilter: Tag='$category', Query='$query'. Filtered count = ${filtered.size} (Total = ${allFirebaseActivities.size})")

        if (filtered.isNotEmpty()) {
            tvAdminHeading.visibility = View.VISIBLE
            rvFirebaseActivities.visibility = View.VISIBLE
            userActivityAdapter.submitList(ArrayList(filtered)) {
                Log.d(TAG, "submitList completed. ${filtered.size} activities rendered in RecyclerView.")
            }
        } else {
            tvAdminHeading.visibility = View.GONE
            rvFirebaseActivities.visibility = View.GONE
            userActivityAdapter.submitList(emptyList())
        }
    }

    private fun setupStaticCards(view: View) {
        val activeTripId = arguments?.getString("activeTripId") ?: ""
        fun openDetail(activityId: String) {
            val bundle = bundleOf(
                "traditionalActivityId" to activityId,
                "activeTripId" to activeTripId
            )
            findNavController().navigate(
                R.id.action_traditionalActivitiesFragment_to_traditionalActivityDetailFragment,
                bundle
            )
        }

        view.findViewById<CardView>(R.id.card_clay_pottery)?.setOnClickListener {
            openDetail("act_pottery_making")
        }

        view.findViewById<CardView>(R.id.card_tea_estate)?.setOnClickListener {
            openDetail("act_tea_estate")
        }

        view.findViewById<CardView>(R.id.card_mask_carving)?.setOnClickListener {
            openDetail("act_wood_carving")
        }

        view.findViewById<CardView>(R.id.card_cooking_village)?.setOnClickListener {
            openDetail("act_cooking_class")
        }

        view.findViewById<CardView>(R.id.card_batik_workshop)?.setOnClickListener {
            openDetail("act_batik_workshop")
        }

        view.findViewById<CardView>(R.id.card_traditional_dance)?.setOnClickListener {
            openDetail("act_traditional_dance")
        }
    }

    override fun onDestroyView() {
        activitiesValueEventListener?.let { listener ->
            databaseRef.removeEventListener(listener)
            Log.d(TAG, "onDestroyView: ValueEventListener detached from /activities")
        }
        super.onDestroyView()
    }
}
