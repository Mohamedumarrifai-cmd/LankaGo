package com.example.lankago.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lankago.data.RentalVehicle
import com.example.lankago.repository.RentalRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RentalDetailsViewModel : ViewModel() {

    private val _rental = MutableStateFlow<RentalVehicle?>(null)
    val rental: StateFlow<RentalVehicle?> = _rental.asStateFlow()

    private val _nearbyRentals = MutableStateFlow<List<RentalVehicle>>(emptyList())
    val nearbyRentals: StateFlow<List<RentalVehicle>> = _nearbyRentals.asStateFlow()

    fun loadRentalDetails(rentalId: String) {
        viewModelScope.launch {
            val selected = RentalRepository.getRentalById(rentalId)
            _rental.value = selected
            if (selected != null) {
                _nearbyRentals.value = RentalRepository.getNearbyRentals(selected.id)
            }
        }
    }
}
