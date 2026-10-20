package com.example.lankago.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lankago.data.CulturalFestival
import com.example.lankago.repository.CulturalFestivalRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class FestivalDetailViewModel : ViewModel() {

    private val _festival = MutableStateFlow<CulturalFestival?>(null)
    val festival: StateFlow<CulturalFestival?> = _festival.asStateFlow()

    private val _isReminderSet = MutableStateFlow(false)
    val isReminderSet: StateFlow<Boolean> = _isReminderSet.asStateFlow()

    private val PREF_NAME = "lankago_festival_reminders"

    fun loadFestival(id: String, context: Context) {
        viewModelScope.launch {
            val item = CulturalFestivalRepository.getFestivalById(id)
            _festival.value = item

            val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            val savedReminder = prefs.getBoolean("remind_$id", false)
            _isReminderSet.value = savedReminder
        }
    }

    fun toggleReminder(context: Context): Boolean {
        val currentFestival = _festival.value ?: return false
        val newStatus = !_isReminderSet.value
        _isReminderSet.value = newStatus

        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean("remind_${currentFestival.id}", newStatus).apply()
        return newStatus
    }
}
