package com.example.lankago.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lankago.data.RentalShop
import com.example.lankago.data.RentalShopDataProvider
import com.example.lankago.repository.AdminRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * UI State for the Rental Shops Listing screen.
 */
data class RentalShopsUiState(
    val shops: List<RentalShop> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val selectedShop: RentalShop? = null
)

/**
 * ViewModel managing rental shops discovery and details, connected to live Firebase Realtime Database.
 */
class RentalShopsViewModel : ViewModel() {

    private val adminRepository = AdminRepository()
    private val _uiState = MutableStateFlow(RentalShopsUiState())
    val uiState: StateFlow<RentalShopsUiState> = _uiState.asStateFlow()
    private var allCachedShops: List<RentalShop> = emptyList()

    init {
        loadShops()
    }

    fun loadShops() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                adminRepository.observeRentals().collect { firebaseShops ->
                    val resolved = if (firebaseShops.isNotEmpty()) {
                        firebaseShops
                    } else {
                        // Fallback to initial local catalogue if firebase node is empty
                        RentalShopDataProvider.getAllShops()
                    }
                    allCachedShops = resolved

                    val currentQuery = _uiState.value.searchQuery
                    if (currentQuery.isBlank()) {
                        _uiState.value = _uiState.value.copy(shops = resolved, isLoading = false)
                    } else {
                        applySearchFilter(currentQuery)
                    }
                }
            } catch (e: Exception) {
                val fallback = RentalShopDataProvider.getAllShops()
                allCachedShops = fallback
                _uiState.value = RentalShopsUiState(shops = fallback, isLoading = false)
            }
        }
    }

    fun searchShops(query: String) {
        val trimmed = query.trim()
        _uiState.value = _uiState.value.copy(searchQuery = trimmed)
        applySearchFilter(trimmed)
    }

    private fun applySearchFilter(query: String) {
        if (query.isEmpty()) {
            _uiState.value = _uiState.value.copy(shops = allCachedShops)
            return
        }

        val filtered = allCachedShops.filter { shop ->
            shop.name.contains(query, ignoreCase = true) ||
                    shop.city.contains(query, ignoreCase = true) ||
                    shop.address.contains(query, ignoreCase = true) ||
                    shop.description.contains(query, ignoreCase = true) ||
                    shop.fleetAvailable.any { it.contains(query, ignoreCase = true) }
        }

        _uiState.value = _uiState.value.copy(shops = filtered)
    }

    fun loadShopDetails(shopId: String) {
        viewModelScope.launch {
            val fromCache = allCachedShops.find { it.id == shopId }
            val shop = fromCache ?: RentalShopDataProvider.getShopById(shopId)
            _uiState.value = _uiState.value.copy(selectedShop = shop)
        }
    }
}
