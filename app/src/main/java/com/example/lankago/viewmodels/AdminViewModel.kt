package com.example.lankago.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lankago.R
import com.example.lankago.data.RentalShop
import com.example.lankago.data.RentalShopDataProvider
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID


  //Authentication states for the Admin module.

sealed class AdminAuthState {
    object Idle : AdminAuthState()
    data class Authenticated(val username: String) : AdminAuthState()
    data class Error(val message: String) : AdminAuthState()
}

/**
 * One-off UI Events for Admin actions (Snackbars, navigation hints, success toasts).
 */
sealed class AdminUiEvent {
    data class ShowSnackbar(val message: String, val isError: Boolean = false) : AdminUiEvent()
    object ShopSavedSuccess : AdminUiEvent()
    object ShopDeletedSuccess : AdminUiEvent()
}

/**
 * Shared ViewModel scoped across the Admin flow (Admin NavGraph or Activity).
 *
 * Manages:
 * - Mock authentication (admin / admin123)
 * - Reactive [StateFlow] collection of [RentalShop] items for simulated CRUD operations
 * - In-memory persistence without requiring external Room or Firebase backends during UI prototyping
 */
class AdminViewModel : ViewModel() {

    // --- Authentication State ---
    private val _authState = MutableStateFlow<AdminAuthState>(AdminAuthState.Idle)
    val authState: StateFlow<AdminAuthState> = _authState.asStateFlow()

    // --- One-off events (Snackbars, Navigation alerts) ---
    private val _uiEvent = MutableSharedFlow<AdminUiEvent>()
    val uiEvent: SharedFlow<AdminUiEvent> = _uiEvent.asSharedFlow()

    // --- Rental Shops Mock Data StateFlow ---
    private val initialShops: List<RentalShop> = try {
        RentalShopDataProvider.shops.ifEmpty { defaultMockRentalShops() }
    } catch (_: Exception) {
        defaultMockRentalShops()
    }

    private val _rentalShops = MutableStateFlow<List<RentalShop>>(initialShops)
    val rentalShops: StateFlow<List<RentalShop>> = _rentalShops.asStateFlow()

    /**
     * Authenticate Admin using mock hardcoded credentials.
     * @return true if credentials match "admin" / "admin123"
     */
    fun authenticate(username: String, password: String): Boolean {
        val trimmedUser = username.trim()
        val trimmedPass = password.trim()

        return if (trimmedUser == "admin" && trimmedPass == "admin123") {
            _authState.value = AdminAuthState.Authenticated(username = trimmedUser)
            true
        } else {
            val errorMsg = when {
                trimmedUser.isEmpty() -> "Please enter your admin username"
                trimmedPass.isEmpty() -> "Please enter your password"
                else -> "Invalid credentials. Use 'admin' and 'admin123'"
            }
            _authState.value = AdminAuthState.Error(errorMsg)
            viewModelScope.launch {
                _uiEvent.emit(AdminUiEvent.ShowSnackbar(errorMsg, isError = true))
            }
            false
        }
    }

    /**
     * Clear active admin authentication session.
     */
    fun logout() {
        _authState.value = AdminAuthState.Idle
    }

    /**
     * Retrieve a specific shop by ID.
     */
    fun getRentalShopById(shopId: String): RentalShop? {
        return _rentalShops.value.find { it.id == shopId }
    }

    /**
     * Add a newly created rental shop to the top of the list.
     */
    fun addRentalShop(
        name: String,
        description: String,
        operatingHours: String,
        contactNumber: String,
        whatsappNumber: String,
        imageUrl: String = "",
        address: String = "Sri Lanka",
        city: String = "Colombo"
    ): RentalShop {
        val newShop = RentalShop(
            id = "shop_${UUID.randomUUID().toString().take(8)}",
            name = name.trim(),
            description = description.trim(),
            lat = 6.9271,
            lng = 79.8612,
            contactNumber = contactNumber.trim(),
            operatingHours = operatingHours.trim(),
            imageUrl = imageUrl.trim(),
            imageRes = R.drawable.colombo_scotter,
            rating = 5.0f,
            reviewCount = 1,
            address = address.trim(),
            city = city.trim(),
            whatsappNumber = whatsappNumber.trim(),
            fleetAvailable = listOf("125cc Automatic Scooters", "Helmets Included", "Full Insurance"),
            priceStartingFrom = "LKR 3,500/day"
        )

        val updatedList = listOf(newShop) + _rentalShops.value
        _rentalShops.value = updatedList

        viewModelScope.launch {
            _uiEvent.emit(AdminUiEvent.ShopSavedSuccess)
            _uiEvent.emit(AdminUiEvent.ShowSnackbar("Rental shop \"${newShop.name}\" created successfully!"))
        }

        return newShop
    }

    /**
     * Update an existing rental shop.
     */
    fun updateRentalShop(
        shopId: String,
        name: String,
        description: String,
        operatingHours: String,
        contactNumber: String,
        whatsappNumber: String,
        imageUrl: String = ""
    ) {
        val currentList = _rentalShops.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == shopId }
        if (index != -1) {
            val existing = currentList[index]
            val updated = existing.copy(
                name = name.trim(),
                description = description.trim(),
                operatingHours = operatingHours.trim(),
                contactNumber = contactNumber.trim(),
                whatsappNumber = whatsappNumber.trim(),
                imageUrl = if (imageUrl.isNotBlank()) imageUrl.trim() else existing.imageUrl
            )
            currentList[index] = updated
            _rentalShops.value = currentList

            viewModelScope.launch {
                _uiEvent.emit(AdminUiEvent.ShopSavedSuccess)
                _uiEvent.emit(AdminUiEvent.ShowSnackbar("Rental shop \"${updated.name}\" updated successfully!"))
            }
        }
    }

    /**
     * Delete a rental shop from the in-memory mock repository.
     */
    fun deleteRentalShop(shopId: String) {
        val shopToDelete = _rentalShops.value.find { it.id == shopId }
        _rentalShops.value = _rentalShops.value.filterNot { it.id == shopId }
        viewModelScope.launch {
            _uiEvent.emit(AdminUiEvent.ShopDeletedSuccess)
            _uiEvent.emit(AdminUiEvent.ShowSnackbar("Removed \"${shopToDelete?.name ?: "Shop"}\""))
        }
    }

    /**
     * Fallback mock rental shops for immediate testing.
     */
    private fun defaultMockRentalShops(): List<RentalShop> = listOf(
        RentalShop(
            id = "shop_epic_ride",
            name = "Epic Ride Scooter Rental",
            description = "Premier coastal scooter rental in Galle Fort. Spotless Honda Dio & Yamaha RayZR scooters, sterilized helmets, full comprehensive insurance, and free 24/7 roadside assistance.",
            lat = 6.0267,
            lng = 80.2170,
            contactNumber = "+94912248899",
            operatingHours = "07:30 AM – 09:30 PM (Daily)",
            imageUrl = "https://images.unsplash.com/photo-1558981403-c5f9899a28bc",
            imageRes = R.drawable.epic_ride,
            rating = 4.9f,
            reviewCount = 196,
            address = "No. 28 Church Street, Galle Fort",
            city = "Galle Fort",
            whatsappNumber = "+94771248899",
            fleetAvailable = listOf("125cc Automatic Scooters", "Honda Dio 110cc", "Surf Rack Scooters"),
            priceStartingFrom = "LKR 3,500/day"
        ),
        RentalShop(
            id = "shop_colombo_scooter",
            name = "Colombo Scooter Rentals",
            description = "Convenient two-wheeler rentals right in the heart of Colombo. Perfect for commuting, city sightseeing, and beating expressway traffic. Flexible hourly and daily packages.",
            lat = 6.9067,
            lng = 79.8510,
            contactNumber = "+94112573344",
            operatingHours = "08:00 AM – 09:00 PM (Daily)",
            imageUrl = "https://images.unsplash.com/photo-1568772585407-9361f9bf3a87",
            imageRes = R.drawable.colombo_scotter,
            rating = 4.6f,
            reviewCount = 142,
            address = "No. 15 Galle Road, Colombo 03",
            city = "Colombo 03",
            whatsappNumber = "+94772573344",
            fleetAvailable = listOf("Automatic City Scooters", "Yamaha Fascino 125cc", "TVS Ntorq 125"),
            priceStartingFrom = "LKR 3,800/day"
        ),
        RentalShop(
            id = "shop_unawatuna",
            name = "Unawatuna Scooter Rental",
            description = "Beachside scooter & motorbike hire specialist. Custom racks for surfboards, beach gear storage, and curated South Coast maps for coastal riding towards Mirissa and Weligama.",
            lat = 6.0108,
            lng = 80.2486,
            contactNumber = "+94912281122",
            operatingHours = "07:00 AM – 10:00 PM (Daily)",
            imageUrl = "https://images.unsplash.com/photo-1558981806-ec527fa84c39",
            imageRes = R.drawable.unawatune_bike,
            rating = 4.7f,
            reviewCount = 178,
            address = "Yaddehimulla Road, Unawatuna Beach",
            city = "Unawatuna",
            whatsappNumber = "+94712281122",
            fleetAvailable = listOf("Scooters with Surf Racks", "Honda Navi 110cc", "125cc Automatic Scooters"),
            priceStartingFrom = "LKR 3,300/day"
        )
    )
}
