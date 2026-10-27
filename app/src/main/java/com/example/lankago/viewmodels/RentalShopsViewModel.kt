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

    // 1. All items fetched from Firebase
    private val _allRentals = MutableStateFlow<List<RentalShop>>(emptyList())
    val allRentals: StateFlow<List<RentalShop>> = _allRentals.asStateFlow()

    // 2. Real-time filtered search results for the UI to observe
    private val _searchResultsRentals = MutableStateFlow<List<RentalShop>>(emptyList())
    val searchResultsRentals: StateFlow<List<RentalShop>> = _searchResultsRentals.asStateFlow()

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
                    val resolved = (if (firebaseShops.isNotEmpty()) {
                        val combined = firebaseShops.toMutableList()
                        val firebaseIds = firebaseShops.map { it.id }.toSet()
                        for (defaultShop in RentalShopDataProvider.getAllShops()) {
                            if (!firebaseIds.contains(defaultShop.id)) {
                                combined.add(defaultShop)
                            }
                        }
                        combined
                    } else {
                        // Fallback to initial local catalogue if firebase node is empty
                        RentalShopDataProvider.getAllShops()
                    }).sortedBy { it.name.trim().lowercase(java.util.Locale.ROOT) }
                    allCachedShops = resolved
                    _allRentals.value = resolved

                    val currentQuery = _uiState.value.searchQuery
                    if (currentQuery.isBlank()) {
                        _searchResultsRentals.value = resolved
                        _uiState.value = _uiState.value.copy(shops = resolved, isLoading = false)
                    } else {
                        searchRentals(currentQuery)
                    }
                }
            } catch (e: Exception) {
                val fallback = RentalShopDataProvider.getAllShops()
                allCachedShops = fallback
                _allRentals.value = fallback
                _searchResultsRentals.value = fallback
                _uiState.value = RentalShopsUiState(shops = fallback, isLoading = false)
            }
        }
    }

    /**
     * Real-time search function filtering allRentals by name, description, city, address, or fleet.
     * Updates searchResultsRentals StateFlow instantly.
     */
    fun searchRentals(query: String) {
        val trimmed = query.trim()
        _uiState.value = _uiState.value.copy(searchQuery = trimmed)

        if (trimmed.isEmpty()) {
            _searchResultsRentals.value = _allRentals.value
            _uiState.value = _uiState.value.copy(shops = _allRentals.value)
            return
        }

        val filtered = _allRentals.value.filter { shop ->
            shop.name.contains(trimmed, ignoreCase = true) ||
                    shop.description.contains(trimmed, ignoreCase = true) ||
                    shop.city.contains(trimmed, ignoreCase = true) ||
                    shop.address.contains(trimmed, ignoreCase = true) ||
                    shop.fleetAvailable.any { it.contains(trimmed, ignoreCase = true) }
        }

        _searchResultsRentals.value = filtered
        _uiState.value = _uiState.value.copy(shops = filtered)
    }

    // Alias for backward compatibility
    fun searchShops(query: String) = searchRentals(query)

    fun loadShopDetails(shopId: String) {
        viewModelScope.launch {
            val fromCache = allCachedShops.find { it.id == shopId }
            val shop = fromCache ?: RentalShopDataProvider.getShopById(shopId)
            _uiState.value = _uiState.value.copy(selectedShop = shop)
        }
    }
}
