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
 * UI State for the Local Products, Markets, and Traditional Medicine Hub.
 */
data class LocalProductsUiState(
    val localProducts: List<LocalItem> = emptyList(),
    val markets: List<LocalItem> = emptyList(),
    val traditionalMedicine: List<LocalItem> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = false
)

/**
 * ViewModel managing the state and search filtering for the Local Culture Hub.
 */
class LocalProductsViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(LocalProductsUiState())
    val uiState: StateFlow<LocalProductsUiState> = _uiState.asStateFlow()

    init {
        loadCultureItems()
    }

    fun loadCultureItems() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val allItems = LocalItemDataProvider.getAllItems()

            _uiState.value = LocalProductsUiState(
                localProducts = allItems.filter { it.category.equals("Local Products", ignoreCase = true) },
                markets = allItems.filter { it.category.equals("Markets", ignoreCase = true) },
                traditionalMedicine = allItems.filter { it.category.equals("Traditional Medicine", ignoreCase = true) },
                searchQuery = "",
                isLoading = false
            )
        }
    }

    fun searchItems(query: String) {
        viewModelScope.launch {
            val trimmed = query.trim()
            val allItems = LocalItemDataProvider.getAllItems()

            if (trimmed.isEmpty()) {
                loadCultureItems()
                return@launch
            }

            val filtered = allItems.filter { item ->
                item.title.contains(trimmed, ignoreCase = true) ||
                        item.description.contains(trimmed, ignoreCase = true) ||
                        item.famousCity.contains(trimmed, ignoreCase = true) ||
                        item.primaryUsage.contains(trimmed, ignoreCase = true) ||
                        item.culturalImportance.contains(trimmed, ignoreCase = true)
            }

            _uiState.value = _uiState.value.copy(
                localProducts = filtered.filter { it.category.equals("Local Products", ignoreCase = true) },
                markets = filtered.filter { it.category.equals("Markets", ignoreCase = true) },
                traditionalMedicine = filtered.filter { it.category.equals("Traditional Medicine", ignoreCase = true) },
                searchQuery = trimmed,
                isLoading = false
            )
        }
    }
}
