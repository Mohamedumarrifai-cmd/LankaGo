package com.example.lankago.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lankago.data.ActivityItem
import com.example.lankago.repository.ActivityRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ActivityBookingViewModel : ViewModel() {

    private val _activity = MutableStateFlow<ActivityItem?>(null)
    val activity: StateFlow<ActivityItem?> = _activity.asStateFlow()

    fun loadActivityDetails(activityId: String) {
        viewModelScope.launch {
            val selected = ActivityRepository.getActivityById(activityId)
            _activity.value = selected
        }
    }
}
