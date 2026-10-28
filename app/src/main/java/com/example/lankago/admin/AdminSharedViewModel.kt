package com.example.lankago.admin

import androidx.lifecycle.ViewModel
import com.example.lankago.model.Rental
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AdminSharedViewModel : ViewModel() {

    private val _rentals = MutableStateFlow<List<Rental>>(emptyList())
    val rentals: StateFlow<List<Rental>> = _rentals.asStateFlow()

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        _rentals.value = listOf(
            Rental(
                id = "R001",
                shopName = "Ella Ride Tuk-Tuk & Bike Rentals",
                category = "Tuk-Tuk & Scooters",
                location = "Ella Town Main St",
                contactNumber = "+94 77 123 4567",
                pricePerDay = 15.0,
                imageUrl = "",
                description = "Self-drive tuk-tuks and modern scooters for exploring Ella."
            ),
            Rental(
                id = "R002",
                shopName = "Mirissa Wave Scooter Hub",
                category = "Scooters & Motorbikes",
                location = "Beach Road, Mirissa",
                contactNumber = "+94 71 987 6543",
                pricePerDay = 12.0,
                imageUrl = "",
                description = "Automatic 125cc scooters with helmet included."
            ),
            Rental(
                id = "R003",
                shopName = "Kandy City Car & Van Rentals",
                category = "Cars & Vans",
                location = "Peradeniya Rd, Kandy",
                contactNumber = "+94 81 222 3344",
                pricePerDay = 45.0,
                imageUrl = "",
                description = "Comfortable sedan cars and 12-seater vans for island trips."
            )
        )
    }

    fun addOrUpdateRental(rental: Rental) {
        val currentList = _rentals.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == rental.id }
        if (index != -1) {
            currentList[index] = rental
        } else {
            val newId = if (rental.id.isBlank()) "R${System.currentTimeMillis()}" else rental.id
            currentList.add(0, rental.copy(id = newId))
        }
        _rentals.value = currentList
    }

    fun deleteRental(rentalId: String) {
        val currentList = _rentals.value.toMutableList()
        currentList.removeAll { it.id == rentalId }
        _rentals.value = currentList
    }

    fun getRentalById(rentalId: String): Rental? {
        return _rentals.value.find { it.id == rentalId }
    }
}
