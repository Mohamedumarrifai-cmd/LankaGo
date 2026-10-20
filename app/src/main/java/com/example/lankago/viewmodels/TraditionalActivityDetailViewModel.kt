package com.example.lankago.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lankago.data.TraditionalActivity
import com.example.lankago.repository.TraditionalActivityRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TraditionalActivityDetailViewModel : ViewModel() {

    private val _activity = MutableStateFlow<TraditionalActivity?>(null)
    val activity: StateFlow<TraditionalActivity?> = _activity.asStateFlow()

    fun loadActivity(id: String) {
        viewModelScope.launch {
            val item = TraditionalActivityRepository.getActivityById(id)
            _activity.value = item
        }
    }

    fun calculateBookingTotal(unitPrice: Double, guests: Int, serviceFee: Double = 500.0): Double {
        return (unitPrice * guests) + serviceFee
    }
}
