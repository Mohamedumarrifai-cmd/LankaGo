package com.example.lankago.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lankago.data.LocalItem
import com.example.lankago.data.LocalItemDataProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel managing deep details for a specific cultural item, market, or traditional medicine.
 */
class LocalItemDetailsViewModel : ViewModel() {

    private val _selectedItem = MutableStateFlow<LocalItem?>(null)
    val selectedItem: StateFlow<LocalItem?> = _selectedItem.asStateFlow()

    private val _isBookmarked = MutableStateFlow(false)
    val isBookmarked: StateFlow<Boolean> = _isBookmarked.asStateFlow()

    fun loadItemDetails(itemId: String) {
        viewModelScope.launch {
            val item = LocalItemDataProvider.getItemById(itemId)
            _selectedItem.value = item
        }
    }

    fun toggleBookmark() {
        _isBookmarked.value = !_isBookmarked.value
    }
}
