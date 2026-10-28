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
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import android.util.Base64
import com.bumptech.glide.Glide
import com.example.lankago.R
import com.example.lankago.data.LocalItem
import com.example.lankago.data.LocalItemDataProvider
import com.example.lankago.viewmodels.LocalItemDetailsViewModel
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch


 // Detailed Screen for Sri Lankan Local Cultural Products, Markets, and Traditional Medicine.

class LocalItemDetailsFragment : Fragment() {

    private val viewModel: LocalItemDetailsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_local_item_details, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Retrieve item ID passed via SafeArgs / bundle with resilient fallback
        val itemId = arguments?.getString("itemId")
            ?: arguments?.getString("productId")
            ?: "item_pottery"

        // Initialize UI Elements
        val toolbar = view.findViewById<Toolbar>(R.id.toolbar_local_item)
        val tvToolbarTitle = view.findViewById<TextView>(R.id.tv_toolbar_item_title)
        val ivCover = view.findViewById<ImageView>(R.id.ivCoverImage)
        val btnFavorite = view.findViewById<ImageButton>(R.id.btn_item_favorite)

        val tvCategoryBadge = view.findViewById<TextView>(R.id.tv_item_category_badge)
        val tvPriceEstimate = view.findViewById<TextView>(R.id.tv_item_price_estimate)
        val tvTitle = view.findViewById<TextView>(R.id.tv_item_title)
        val tvRating = view.findViewById<TextView>(R.id.tv_item_rating)
        val tvReviewsCount = view.findViewById<TextView>(R.id.tv_item_reviews_count)

        // 4 Core Required Sections
        val tvDescription = view.findViewById<TextView>(R.id.tv_item_description)
        val tvPrimaryUsage = view.findViewById<TextView>(R.id.tv_item_primary_usage)
        val tvFamousCity = view.findViewById<TextView>(R.id.tv_item_famous_city)
        val tvCulturalImportance = view.findViewById<TextView>(R.id.tv_item_cultural_importance)

        // Bottom Bar Elements
        val tvBottomTitle = view.findViewById<TextView>(R.id.tv_bottom_item_title)
        val tvBottomCity = view.findViewById<TextView>(R.id.tv_bottom_famous_city)
        val btnExploreMap = view.findViewById<MaterialButton>(R.id.btn_explore_on_map)

        // Toolbar Navigation
        toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        // Bookmark Toggle
        btnFavorite.setOnClickListener {
            viewModel.toggleBookmark()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.isBookmarked.collect { isBookmarked ->
                    btnFavorite.setImageResource(
                        if (isBookmarked) R.drawable.ic_heart_filled else R.drawable.ic_heart_outline
                    )
                }
            }
        }

        // Instant pre-binding to guarantee top hero section is never blank
        val immediateItem = LocalItemDataProvider.getItemById(itemId)
            ?: LocalItemDataProvider.items.firstOrNull()
        if (immediateItem != null && ivCover != null) {
            bindUi(
                item = immediateItem,
                tvToolbarTitle = tvToolbarTitle,
                ivCover = ivCover,
                tvCategoryBadge = tvCategoryBadge,
                tvPriceEstimate = tvPriceEstimate,
                tvTitle = tvTitle,
                tvRating = tvRating,
                tvReviewsCount = tvReviewsCount,
                tvDescription = tvDescription,
                tvPrimaryUsage = tvPrimaryUsage,
                tvFamousCity = tvFamousCity,
                tvCulturalImportance = tvCulturalImportance,
                tvBottomTitle = tvBottomTitle,
                tvBottomCity = tvBottomCity,
                btnExploreMap = btnExploreMap
            )
        }

        // Bind Item Details from ViewModel StateFlow
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.selectedItem.collect { item ->
                    val targetItem = item ?: LocalItemDataProvider.getItemById(itemId)
                    if (targetItem != null && ivCover != null) {
                        bindUi(
                            item = targetItem,
                            tvToolbarTitle = tvToolbarTitle,
                            ivCover = ivCover,
                            tvCategoryBadge = tvCategoryBadge,
                            tvPriceEstimate = tvPriceEstimate,
                            tvTitle = tvTitle,
                            tvRating = tvRating,
                            tvReviewsCount = tvReviewsCount,
                            tvDescription = tvDescription,
                            tvPrimaryUsage = tvPrimaryUsage,
                            tvFamousCity = tvFamousCity,
                            tvCulturalImportance = tvCulturalImportance,
                            tvBottomTitle = tvBottomTitle,
                            tvBottomCity = tvBottomCity,
                            btnExploreMap = btnExploreMap
                        )
                    }
                }
            }
        }

        viewModel.loadItemDetails(itemId)
    }

    private fun bindUi(
        item: LocalItem,
        tvToolbarTitle: TextView,
        ivCover: ImageView,
        tvCategoryBadge: TextView,
        tvPriceEstimate: TextView,
        tvTitle: TextView,
        tvRating: TextView,
        tvReviewsCount: TextView,
        tvDescription: TextView,
        tvPrimaryUsage: TextView,
        tvFamousCity: TextView,
        tvCulturalImportance: TextView,
        tvBottomTitle: TextView,
        tvBottomCity: TextView,
        btnExploreMap: MaterialButton
    ) {
        tvToolbarTitle.text = item.title
        tvTitle.text = item.title
        tvCategoryBadge.text = item.category.uppercase()
        tvPriceEstimate.text = item.priceIndication
        tvRating.text = String.format("%.2f", item.rating)
        tvReviewsCount.text = "(${item.reviewCount} verified reviews)"

        // Seamless image loading: Use Glide for web URLs and Base64 data with local fallback
        if (!item.imageUrl.isNullOrBlank()) {
            if (item.imageUrl.startsWith("data:image") || item.imageUrl.contains("base64,")) {
                try {
                    val base64Data = item.imageUrl.substringAfter("base64,")
                    val imageBytes = Base64.decode(base64Data, Base64.DEFAULT)
                    Glide.with(this)
                        .asBitmap()
                        .load(imageBytes)
                        .placeholder(if (item.imageRes != 0) item.imageRes else R.drawable.ceylon_tea_set)
                        .error(if (item.imageRes != 0) item.imageRes else R.drawable.ceylon_tea_set)
                        .centerCrop()
                        .into(ivCover)
                } catch (_: Exception) {
                    ivCover.setImageResource(if (item.imageRes != 0) item.imageRes else R.drawable.ceylon_tea_set)
                }
            } else {
                Glide.with(this)
                    .load(item.imageUrl)
                    .placeholder(if (item.imageRes != 0) item.imageRes else R.drawable.ceylon_tea_set)
                    .error(if (item.imageRes != 0) item.imageRes else R.drawable.ceylon_tea_set)
                    .centerCrop()
                    .into(ivCover)
            }
        } else if (item.imageRes != 0) {
            ivCover.setImageResource(item.imageRes)
        } else {
            ivCover.setImageResource(R.drawable.ceylon_tea_set)
        }

        // 1. Detailed Description (What the product actually is)
        tvDescription.text = item.description

        // 2. Primary Usage & Benefits (What it is used for)
        tvPrimaryUsage.text = item.primaryUsage

        // 3. Famous City / Region (Exact local city/region best known for this product)
        tvFamousCity.text = item.famousCity

        // 4. Cultural / Historical Importance (Significance to local heritage)
        tvCulturalImportance.text = item.culturalImportance

        // Bottom Bar Preview
        tvBottomTitle.text = item.title
        tvBottomCity.text = item.famousCity

        // 5. Explore on Map / Find Sellers Action Button
        btnExploreMap.setOnClickListener {
            openMapLocation(item)
        }
    }

    private fun openMapLocation(item: LocalItem) {
        try {
            val uri = Uri.parse("geo:${item.latitude},${item.longitude}?q=${item.latitude},${item.longitude}(${Uri.encode(item.title)})")
            val mapIntent = Intent(Intent.ACTION_VIEW, uri).apply {
                setPackage("com.google.android.apps.maps")
            }
            if (mapIntent.resolveActivity(requireContext().packageManager) != null) {
                startActivity(mapIntent)
            } else {
                val genericIntent = Intent(Intent.ACTION_VIEW, uri)
                startActivity(genericIntent)
            }
        } catch (e: Exception) {
            Snackbar.make(
                requireView(),
                "Navigating to coordinates: ${item.latitude}, ${item.longitude}",
                Snackbar.LENGTH_SHORT
            ).show()
        }
    }
}
