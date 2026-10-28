package com.example.lankago.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lankago.R
import com.example.lankago.data.LocalItem
import com.example.lankago.data.LocalItemDataProvider
import com.example.lankago.data.LocalProduct
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await


  //ViewModel managing deep details for a specific cultural item, market, or traditional medicine.
  //Resiliently connects to both local mock data and live Firebase Realtime Database `/products/{id}`.

class LocalItemDetailsViewModel : ViewModel() {

    private val _selectedItem = MutableStateFlow<LocalItem?>(null)
    val selectedItem: StateFlow<LocalItem?> = _selectedItem.asStateFlow()

    private val _isBookmarked = MutableStateFlow(false)
    val isBookmarked: StateFlow<Boolean> = _isBookmarked.asStateFlow()

    fun loadItemDetails(itemId: String) {
        viewModelScope.launch {
            // 1. Try local catalog first
            val localItem = LocalItemDataProvider.getItemById(itemId)
            if (localItem != null) {
                _selectedItem.value = localItem
                return@launch
            }

            // 2. Fetch live product from Firebase Realtime Database /products/{itemId}
            try {
                val snapshot = FirebaseDatabase.getInstance().reference
                    .child("products").child(itemId).get().await()
                val prod = snapshot.getValue(LocalProduct::class.java)
                if (prod != null) {
                    val convertedItem = LocalItem(
                        id = prod.id,
                        title = prod.name,
                        category = prod.category.ifBlank { "Local Product" },
                        description = prod.description.ifBlank { "Authentic Sri Lankan artisan product handcrafted in ${prod.originRegion}." },
                        priceIndication = "LKR ${String.format(java.util.Locale.US, "%,.2f", prod.priceLkr)}",
                        famousCity = prod.originRegion.ifBlank { "Sri Lanka" },
                        imageUrl = prod.imageUrl,
                        imageRes = if (prod.imageRes != 0) prod.imageRes else R.drawable.ceylon_tea_set,
                        rating = prod.rating,
                        reviewCount = prod.reviewCount,
                        primaryUsage = "High quality ${prod.category} sourced directly from local producers.",
                        culturalImportance = "Centuries of traditional craftsmanship and sustainable regional sourcing in ${prod.originRegion}."
                    )
                    _selectedItem.value = convertedItem
                    return@launch
                }
            } catch (_: Exception) {
                // Fallback to first available item
            }

            _selectedItem.value = LocalItemDataProvider.items.firstOrNull()
        }
    }

    fun toggleBookmark() {
        _isBookmarked.value = !_isBookmarked.value
    }
}
