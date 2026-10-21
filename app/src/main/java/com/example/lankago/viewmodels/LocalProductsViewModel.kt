package com.example.lankago.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lankago.R
import com.example.lankago.data.LocalItem
import com.example.lankago.data.LocalItemDataProvider
import com.example.lankago.data.LocalProduct
import com.example.lankago.repository.AdminRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

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
 * ViewModel managing the state and search filtering for the Local Culture Hub,
 * synchronized live with Firebase Realtime Database (/products).
 */
class LocalProductsViewModel : ViewModel() {

    private val adminRepository = AdminRepository()
    private val _uiState = MutableStateFlow(LocalProductsUiState())
    val uiState: StateFlow<LocalProductsUiState> = _uiState.asStateFlow()

    private var allCombinedItems: List<LocalItem> = emptyList()

    init {
        loadCultureItems()
    }

    fun loadCultureItems() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val staticItems = LocalItemDataProvider.getAllItems()

            try {
                adminRepository.observeProducts().collect { firebaseProducts ->
                    val dynamicItems = firebaseProducts.map { mapProductToLocalItem(it) }
                    // Combine dynamic Firebase products with static bazaars & medicine items
                    allCombinedItems = dynamicItems + staticItems

                    filterAndEmit(allCombinedItems, _uiState.value.searchQuery)
                }
            } catch (e: Exception) {
                allCombinedItems = staticItems
                filterAndEmit(allCombinedItems, _uiState.value.searchQuery)
            }
        }
    }

    private fun mapProductToLocalItem(product: LocalProduct): LocalItem {
        val formattedPrice = NumberFormat.getNumberInstance(Locale.US).format(product.priceLkr)
        return LocalItem(
            id = product.id,
            title = product.name,
            category = "Local Products",
            description = product.description,
            primaryUsage = "Category: ${product.category}. Origin: ${product.originRegion}.",
            famousCity = product.originRegion.ifBlank { "Sri Lanka" },
            culturalImportance = "Locally crafted artisan product registered in LankaGo catalogue.",
            imageUrl = product.imageUrl,
            imageRes = if (product.imageRes != 0) product.imageRes else R.drawable.ceylon_tea_set,
            priceIndication = "LKR $formattedPrice",
            rating = product.rating,
            reviewCount = product.reviewCount
        )
    }

    fun searchItems(query: String) {
        val trimmed = query.trim()
        _uiState.value = _uiState.value.copy(searchQuery = trimmed)
        filterAndEmit(allCombinedItems, trimmed)
    }

    private fun filterAndEmit(items: List<LocalItem>, query: String) {
        val filtered = if (query.isEmpty()) {
            items
        } else {
            items.filter { item ->
                item.title.contains(query, ignoreCase = true) ||
                        item.description.contains(query, ignoreCase = true) ||
                        item.famousCity.contains(query, ignoreCase = true) ||
                        item.primaryUsage.contains(query, ignoreCase = true) ||
                        item.culturalImportance.contains(query, ignoreCase = true)
            }
        }

        _uiState.value = _uiState.value.copy(
            localProducts = filtered.filter { it.category.equals("Local Products", ignoreCase = true) },
            markets = filtered.filter { it.category.equals("Markets", ignoreCase = true) },
            traditionalMedicine = filtered.filter { it.category.equals("Traditional Medicine", ignoreCase = true) },
            searchQuery = query,
            isLoading = false
        )
    }
}
