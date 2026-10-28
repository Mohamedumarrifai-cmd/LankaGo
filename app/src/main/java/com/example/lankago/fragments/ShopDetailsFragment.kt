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
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.util.Base64
import com.bumptech.glide.Glide
import com.example.lankago.R
import com.example.lankago.adapters.ShopReviewAdapter
import com.example.lankago.data.RentalShop
import com.example.lankago.data.RentalShopDataProvider
import com.example.lankago.viewmodels.RentalShopsViewModel
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch


 // Separate, full-page screen displaying complete details for a selected Rental Shop.

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

        // Retrieve shop ID passed via SafeArgs / bundle with resilient fallback
        val shopId = arguments?.getString("shopId")
            ?: arguments?.getString("rentalShopId")
            ?: "shop_epic_ride"

        // Initialize Views
        val toolbar = view.findViewById<Toolbar>(R.id.toolbar_shop_details)
        val tvToolbarName = view.findViewById<TextView>(R.id.tv_toolbar_shop_name)
        val ivCover = view.findViewById<ImageView>(R.id.ivCoverImage)
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
                ?: RentalShopDataProvider.getShopById(shopId)
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

        // Instant pre-binding to guarantee the top section is never blank while StateFlow initializes
        val immediateShop = RentalShopDataProvider.getShopById(shopId)
            ?: RentalShopDataProvider.shops.firstOrNull()
        if (immediateShop != null && ivCover != null) {
            bindShopDetails(
                shop = immediateShop,
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

        // Observe ViewModel State
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    val shop = state.selectedShop
                        ?: RentalShopDataProvider.getShopById(shopId)
                    if (shop != null && ivCover != null) {
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

        // Seamless image loading: Use Glide for web/base64 URLs with accurate authentic fallback
        val fallbackRes = com.example.lankago.adapters.RentalTransportAdapter.resolveShopDrawable(shop)
        if (!shop.imageUrl.isNullOrBlank()) {
            if (shop.imageUrl.startsWith("data:image") || shop.imageUrl.contains("base64,")) {
                try {
                    val base64Data = shop.imageUrl.substringAfter("base64,")
                    val imageBytes = Base64.decode(base64Data, Base64.DEFAULT)
                    Glide.with(this)
                        .asBitmap()
                        .load(imageBytes)
                        .placeholder(fallbackRes)
                        .error(fallbackRes)
                        .centerCrop()
                        .into(ivCover)
                } catch (e: Exception) {
                    ivCover.setImageResource(fallbackRes)
                }
            } else {
                Glide.with(this)
                    .load(shop.imageUrl)
                    .placeholder(fallbackRes)
                    .error(fallbackRes)
                    .centerCrop()
                    .into(ivCover)
            }
        } else {
            ivCover.setImageResource(fallbackRes)
        }

        // Fleet Chips
        chipGroupFleet.removeAllViews()
        for (fleet in shop.fleetAvailable) {
            val chip = Chip(requireContext()).apply {
                text = fleet
                isClickable = false
                isCheckable = false
                setChipBackgroundColorResource(R.color.admin_indigo_soft)
                setTextColor(requireContext().getColor(R.color.admin_primary_navy))
            }
            chipGroupFleet.addView(chip)
        }

        // Action Buttons: WhatsApp Intent
        btnWhatsApp.setOnClickListener {
            val phone = if (shop.whatsappNumber.isNotBlank()) shop.whatsappNumber else shop.contactNumber
            val cleaned = phone.replace("+", "").replace(" ", "").replace("-", "")
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("https://api.whatsapp.com/send?phone=$cleaned&text=Hello%20${Uri.encode(shop.name)},%20I%20am%20inquiring%20about%20rentals%20via%20LankaGo")
            }
            try {
                startActivity(intent)
            } catch (_: Exception) {
                Toast.makeText(requireContext(), "WhatsApp is not installed on this device", Toast.LENGTH_SHORT).show()
            }
        }

        // Action Buttons: Phone Call Intent (ACTION_DIAL)
        btnCall.setOnClickListener {
            val intent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:${shop.contactNumber}")
            }
            try {
                startActivity(intent)
            } catch (_: Exception) {
                Toast.makeText(requireContext(), "Unable to open phone dialer", Toast.LENGTH_SHORT).show()
            }
        }

        // Action Buttons: Route Tracking & Live GPS Navigation
        btnGoogleMaps.setOnClickListener {
            val bundle = bundleOf(
                "destLat" to shop.lat,
                "destLng" to shop.lng,
                "destTitle" to shop.name,
                "destAddress" to "${shop.address}, ${shop.city}"
            )
            findNavController().navigate(R.id.routeTrackingFragment, bundle)
        }

        // Reviews Binding
        if (shop.reviews.isEmpty()) {
            tvNoReviews.visibility = View.VISIBLE
            tvReviewsHeaderCount.text = "(0)"
            reviewAdapter.submitList(emptyList())
        } else {
            tvNoReviews.visibility = View.GONE
            tvReviewsHeaderCount.text = "(${shop.reviews.size})"
            reviewAdapter.submitList(shop.reviews)
        }
    }
}
