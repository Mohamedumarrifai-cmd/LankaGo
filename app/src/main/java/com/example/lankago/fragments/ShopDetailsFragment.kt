package com.example.lankago.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.lankago.R
import com.example.lankago.adapters.ShopReviewAdapter
import com.example.lankago.data.RentalShop
import com.example.lankago.viewmodels.RentalShopsViewModel
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

/**
 * Separate, full-page screen displaying complete details for a selected Rental Shop.
 * Features:
 * - High-res cover image with CollapsingToolbarLayout
 * - Shop and Fleet info (operating hours, fleet chips, address)
 * - Safe Google Maps Intent with geo URI
 * - Safe Phone Call Intent (ACTION_DIAL)
 * - Safe WhatsApp Intent
 * - Embedded Customer Reviews RecyclerView
 */
class ShopDetailsFragment : Fragment() {

    private val viewModel: RentalShopsViewModel by viewModels()
    private lateinit var reviewAdapter: ShopReviewAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_shop_details, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val shopId = arguments?.getString("shopId") ?: "shop_galle_bike"

        // Initialize Views
        val toolbar = view.findViewById<Toolbar>(R.id.toolbar_shop_details)
        val tvToolbarName = view.findViewById<TextView>(R.id.tv_toolbar_shop_name)
        val ivCover = view.findViewById<ImageView>(R.id.iv_shop_cover)
        val btnShare = view.findViewById<ImageButton>(R.id.btn_shop_share)

        val tvName = view.findViewById<TextView>(R.id.tv_detail_shop_name)
        val tvRating = view.findViewById<TextView>(R.id.tv_detail_rating)
        val tvReviewCount = view.findViewById<TextView>(R.id.tv_detail_review_count)
        val tvCity = view.findViewById<TextView>(R.id.tv_detail_city)
        val tvPriceStarting = view.findViewById<TextView>(R.id.tv_detail_price_starting)
        val tvDescription = view.findViewById<TextView>(R.id.tv_detail_description)

        val tvHours = view.findViewById<TextView>(R.id.tv_detail_hours)
        val tvAddress = view.findViewById<TextView>(R.id.tv_detail_address)
        val chipGroupFleet = view.findViewById<ChipGroup>(R.id.chip_group_shop_fleet)

        val btnWhatsApp = view.findViewById<MaterialButton>(R.id.btn_shop_whatsapp)
        val btnCall = view.findViewById<MaterialButton>(R.id.btn_shop_call)
        val btnGoogleMaps = view.findViewById<MaterialButton>(R.id.btn_view_on_google_maps)

        val rvReviews = view.findViewById<RecyclerView>(R.id.rv_shop_reviews)
        val tvReviewsHeaderCount = view.findViewById<TextView>(R.id.tv_reviews_header_count)
        val tvNoReviews = view.findViewById<TextView>(R.id.tv_no_reviews)

        // Toolbar Navigation
        toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        // Setup Reviews Adapter
        reviewAdapter = ShopReviewAdapter()
        rvReviews.layoutManager = LinearLayoutManager(requireContext())
        rvReviews.adapter = reviewAdapter

        btnShare.setOnClickListener {
            val shop = viewModel.uiState.value.selectedShop
            if (shop != null) {
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_SUBJECT, shop.name)
                    putExtra(
                        Intent.EXTRA_TEXT,
                        "Check out ${shop.name} in ${shop.city} on LankaGo! Contact: ${shop.contactNumber}"
                    )
                }
                startActivity(Intent.createChooser(shareIntent, "Share Rental Shop"))
            }
        }

        // Observe ViewModel State
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    val shop = state.selectedShop
                    if (shop != null) {
                        bindShopDetails(
                            shop = shop,
                            tvToolbarName = tvToolbarName,
                            ivCover = ivCover,
                            tvName = tvName,
                            tvRating = tvRating,
                            tvReviewCount = tvReviewCount,
                            tvCity = tvCity,
                            tvPriceStarting = tvPriceStarting,
                            tvDescription = tvDescription,
                            tvHours = tvHours,
                            tvAddress = tvAddress,
                            chipGroupFleet = chipGroupFleet,
                            btnWhatsApp = btnWhatsApp,
                            btnCall = btnCall,
                            btnGoogleMaps = btnGoogleMaps,
                            tvReviewsHeaderCount = tvReviewsHeaderCount,
                            tvNoReviews = tvNoReviews
                        )
                    }
                }
            }
        }

        viewModel.loadShopDetails(shopId)
    }

    private fun bindShopDetails(
        shop: RentalShop,
        tvToolbarName: TextView,
        ivCover: ImageView,
        tvName: TextView,
        tvRating: TextView,
        tvReviewCount: TextView,
        tvCity: TextView,
        tvPriceStarting: TextView,
        tvDescription: TextView,
        tvHours: TextView,
        tvAddress: TextView,
        chipGroupFleet: ChipGroup,
        btnWhatsApp: MaterialButton,
        btnCall: MaterialButton,
        btnGoogleMaps: MaterialButton,
        tvReviewsHeaderCount: TextView,
        tvNoReviews: TextView
    ) {
        tvToolbarName.text = shop.name
        tvName.text = shop.name
        tvRating.text = String.format("%.2f", shop.rating)
        tvReviewCount.text = "(${shop.reviewCount} reviews)"
        tvCity.text = "${shop.city}, Sri Lanka"
        tvPriceStarting.text = shop.priceStartingFrom
        tvDescription.text = shop.description

        tvHours.text = shop.operatingHours
        tvAddress.text = shop.address

        if (shop.imageRes != 0) {
            ivCover.setImageResource(shop.imageRes)
        } else {
            ivCover.setImageResource(R.drawable.casons_rent)
        }

        // Fleet Chips
        chipGroupFleet.removeAllViews()
        for (fleet in shop.fleetAvailable) {
            val chip = Chip(requireContext()).apply {
                text = fleet
                isClickable = false
                isCheckable = false
                setChipBackgroundColorResource(R.color.chip_bg)
                setTextColor(resources.getColor(R.color.text_primary, null))
            }
            chipGroupFleet.addView(chip)
        }

        // 1. WhatsApp Intent (Protected by try-catch)
        btnWhatsApp.setOnClickListener {
            openWhatsApp(shop)
        }

        // 2. Call Now Intent (ACTION_DIAL, protected by try-catch)
        btnCall.setOnClickListener {
            dialPhoneNumber(shop.contactNumber)
        }

        // 3. View on Google Maps Intent (Protected by try-catch)
        btnGoogleMaps.setOnClickListener {
            openGoogleMaps(shop)
        }

        // 4. Reviews List Binding
        if (shop.reviews.isEmpty()) {
            tvNoReviews.visibility = View.VISIBLE
            tvReviewsHeaderCount.text = "0 Reviews"
            reviewAdapter.submitList(emptyList())
        } else {
            tvNoReviews.visibility = View.GONE
            tvReviewsHeaderCount.text = "${shop.reviews.size} Reviews"
            reviewAdapter.submitList(shop.reviews)
        }
    }

    /**
     * Triggers Google Maps using geo:lat,lng?q=Shop+Name
     */
    private fun openGoogleMaps(shop: RentalShop) {
        try {
            val encodedQuery = Uri.encode("${shop.name}, ${shop.city}")
            val geoUri = Uri.parse("geo:${shop.lat},${shop.lng}?q=${shop.lat},${shop.lng}($encodedQuery)")
            val mapIntent = Intent(Intent.ACTION_VIEW, geoUri).apply {
                setPackage("com.google.android.apps.maps")
            }

            if (mapIntent.resolveActivity(requireContext().packageManager) != null) {
                startActivity(mapIntent)
            } else {
                // Fallback to web browser or generic map viewer
                val browserUri = Uri.parse("https://www.google.com/maps/search/?api=1&query=${shop.lat},${shop.lng}")
                startActivity(Intent(Intent.ACTION_VIEW, browserUri))
            }
        } catch (e: Exception) {
            Snackbar.make(
                requireView(),
                "Unable to launch maps. Location: ${shop.address}",
                Snackbar.LENGTH_LONG
            ).show()
        }
    }

    /**
     * Dials phone number using Intent.ACTION_DIAL
     */
    private fun dialPhoneNumber(phoneNumber: String) {
        try {
            val dialIntent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:$phoneNumber")
            }
            startActivity(dialIntent)
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Unable to open phone dialer for: $phoneNumber", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Opens WhatsApp chat directly to the rental shop
     */
    private fun openWhatsApp(shop: RentalShop) {
        val number = if (shop.whatsappNumber.isNotEmpty()) shop.whatsappNumber else shop.contactNumber
        val cleanNumber = number.replace("+", "").replace(" ", "").replace("-", "")

        try {
            val message = "Hello ${shop.name}, I found your rental listing on LankaGo and would like to inquire about vehicle availability."
            val url = "https://api.whatsapp.com/send?phone=$cleanNumber&text=${Uri.encode(message)}"
            val whatsappIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                setPackage("com.whatsapp")
            }

            if (whatsappIntent.resolveActivity(requireContext().packageManager) != null) {
                startActivity(whatsappIntent)
            } else {
                // Fallback to standard web browser WhatsApp link
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                startActivity(browserIntent)
            }
        } catch (e: Exception) {
            Snackbar.make(
                requireView(),
                "WhatsApp is not installed. Contact shop directly at: $number",
                Snackbar.LENGTH_LONG
            ).show()
        }
    }
}
