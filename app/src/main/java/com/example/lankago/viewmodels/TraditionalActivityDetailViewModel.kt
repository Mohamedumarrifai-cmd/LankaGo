package com.example.lankago.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lankago.data.TraditionalActivity
import com.example.lankago.repository.AdminRepository
import com.example.lankago.repository.TraditionalActivityRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel managing detailed view for cultural activities, workshops, and tours.
 * Checks live Firebase Realtime Database (/activities) with fallback to local catalogue.
 */
class TraditionalActivityDetailViewModel : ViewModel() {

    private val adminRepository = AdminRepository()
    private val _activity = MutableStateFlow<TraditionalActivity?>(null)
    val activity: StateFlow<TraditionalActivity?> = _activity.asStateFlow()

    fun loadActivity(id: String) {
        viewModelScope.launch {
            try {
                val firebaseActivities = adminRepository.getAllActivitiesOnce()
                val found = firebaseActivities.find { it.id == id }
                if (found != null) {
                    _activity.value = found
                    return@launch
                }
            } catch (_: Exception) {
                // Ignore and fall back to local repository
            }

            // Fallback to static catalogue
            val item = TraditionalActivityRepository.getActivityById(id)
            _activity.value = item
        }
    }

    fun calculateBookingTotal(unitPrice: Double, guests: Int, serviceFee: Double = 500.0): Double {
        return (unitPrice * guests) + serviceFee
    }
}
