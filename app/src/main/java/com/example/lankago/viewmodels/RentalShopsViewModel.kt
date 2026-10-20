package com.example.lankago.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lankago.data.RentalShop
import com.example.lankago.data.RentalShopDataProvider
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
 * ViewModel managing rental shops discovery and details.
 */
class RentalShopsViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(RentalShopsUiState())
    val uiState: StateFlow<RentalShopsUiState> = _uiState.asStateFlow()

    init {
        loadShops()
    }

    fun loadShops() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val all = RentalShopDataProvider.getAllShops()
            _uiState.value = RentalShopsUiState(shops = all, isLoading = false)
        }
    }

    fun searchShops(query: String) {
        viewModelScope.launch {
            val trimmed = query.trim()
            val all = RentalShopDataProvider.getAllShops()
            if (trimmed.isEmpty()) {
                _uiState.value = _uiState.value.copy(shops = all, searchQuery = "")
                return@launch
            }

            val filtered = all.filter { shop ->
                shop.name.contains(trimmed, ignoreCase = true) ||
                        shop.city.contains(trimmed, ignoreCase = true) ||
                        shop.address.contains(trimmed, ignoreCase = true) ||
                        shop.description.contains(trimmed, ignoreCase = true) ||
                        shop.fleetAvailable.any { it.contains(trimmed, ignoreCase = true) }
            }

            _uiState.value = _uiState.value.copy(shops = filtered, searchQuery = trimmed)
        }
    }

    fun loadShopDetails(shopId: String) {
        viewModelScope.launch {
            val shop = RentalShopDataProvider.getShopById(shopId)
            _uiState.value = _uiState.value.copy(selectedShop = shop)
        }
    }
}
