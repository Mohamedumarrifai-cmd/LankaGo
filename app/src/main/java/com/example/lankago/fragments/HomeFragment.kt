package com.example.lankago.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.lankago.R
import com.example.lankago.utils.safeNavigate

class HomeFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupCategoryButtons(view)
        setupSearchBar(view)
        setupHeroDestination(view)
        setupPopularDestinations(view)
        setupHeaderActionButtons(view)
    }

    private fun setupHeroDestination(view: View) {
        view.findViewById<View>(R.id.card_hero_destination)?.setOnClickListener {
            val bundle = Bundle().apply {
                putString("place_id", "place_1")
            }
            findNavController().safeNavigate(R.id.action_homeFragment_to_destinationDetailFragment, bundle)
        }
    }

    private fun setupPopularDestinations(view: View) {
        view.findViewById<View>(R.id.card_popular_1)?.setOnClickListener {
            val bundle = Bundle().apply {
                putString("selected_category", "Heritage")
            }
            findNavController().safeNavigate(R.id.action_homeFragment_to_searchFragment, bundle)
        }

        view.findViewById<View>(R.id.card_popular_2)?.setOnClickListener {
            val bundle = Bundle().apply {
                putString("selected_category", "Lakes")
            }
            findNavController().safeNavigate(R.id.action_homeFragment_to_searchFragment, bundle)
        }

        view.findViewById<View>(R.id.card_popular_3)?.setOnClickListener {
            val bundle = Bundle().apply {
                putString("selected_category", "Hills")
            }
            findNavController().safeNavigate(R.id.action_homeFragment_to_searchFragment, bundle)
        }
    }

    private fun setupHeaderActionButtons(view: View) {
        // Upcoming Events "More"
        view.findViewById<View>(R.id.btn_more_events)?.setOnClickListener {
            findNavController().safeNavigate(R.id.action_homeFragment_to_showsAndFestivalsFragment)
        }

        // Popular Destinations "See all"
        view.findViewById<View>(R.id.btn_see_all_destinations)?.setOnClickListener {
            findNavController().safeNavigate(R.id.action_homeFragment_to_discoverFragment)
        }

        // Category "More"
        view.findViewById<View>(R.id.btn_category_more)?.setOnClickListener {
            findNavController().safeNavigate(R.id.action_homeFragment_to_discoverFragment)
        }
    }

    private fun setupCategoryButtons(view: View) {
        // Products Hub (from Category Grid)
        view.findViewById<View>(R.id.btn_products_hub)?.setOnClickListener {
            findNavController().safeNavigate(R.id.action_homeFragment_to_localProductsFragment)
        }

        // Local Products (Malls)
        view.findViewById<View>(R.id.btn_local_products)?.setOnClickListener {
            findNavController().safeNavigate(R.id.action_homeFragment_to_localProductsFragment)
        }

        // Traditional Activities
        view.findViewById<View>(R.id.btn_traditional_activities)?.setOnClickListener {
            findNavController().safeNavigate(R.id.action_homeFragment_to_traditionalActivitiesFragment)
        }

        // Shows & Festivals
        view.findViewById<View>(R.id.btn_shows_festivals)?.setOnClickListener {
            findNavController().safeNavigate(R.id.action_homeFragment_to_showsAndFestivalsFragment)
        }

        // Rentals & Transport Hub
        view.findViewById<View>(R.id.btn_rentals_transport)?.setOnClickListener {
            findNavController().safeNavigate(R.id.action_homeFragment_to_rentalsTransportFragment)
        }

        // Trip Planner
        view.findViewById<View>(R.id.btn_trip_planner)?.setOnClickListener {
            findNavController().safeNavigate(R.id.action_homeFragment_to_tripPlannerFragment)
        }

        // Favorites
        view.findViewById<View>(R.id.btn_favorites)?.setOnClickListener {
            findNavController().safeNavigate(R.id.action_homeFragment_to_favoritesFragment)
        }

        // Notifications (Top bar icon)
        view.findViewById<View>(R.id.btn_notifications)?.setOnClickListener {
            findNavController().safeNavigate(R.id.action_homeFragment_to_notificationsFragment)
        }
    }

    private fun setupSearchBar(view: View) {
        val etSearch = view.findViewById<android.widget.EditText>(R.id.et_search)

        fun navigateToSearch(query: String = "") {
            val bundle = Bundle().apply {
                putString("search_query", query)
            }
            findNavController().safeNavigate(R.id.action_homeFragment_to_searchFragment, bundle)
        }

        view.findViewById<View>(R.id.search_bar)?.setOnClickListener {
            val q = etSearch?.text?.toString()?.trim() ?: ""
            navigateToSearch(q)
        }

        etSearch?.setOnEditorActionListener { v, actionId, _ ->
            val q = v.text?.toString()?.trim() ?: ""
            navigateToSearch(q)
            true
        }

        etSearch?.setOnClickListener {
            val q = etSearch.text?.toString()?.trim() ?: ""
            navigateToSearch(q)
        }
    }
}
