package com.example.lankago.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lankago.R
import com.example.lankago.data.ActivityItem
import com.example.lankago.data.Booking
import com.example.lankago.data.BookingStatus
import com.example.lankago.data.BookingType
import com.example.lankago.data.LocalProduct
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

/**
 * Single-event UI notification bus for the Admin panel.
 */
sealed class AdminSharedUiEvent {
    data class ShowSnackbar(val message: String, val isError: Boolean = false) : AdminSharedUiEvent()
    data class OperationSuccess(val message: String) : AdminSharedUiEvent()
}

/**
 * Comprehensive Shared ViewModel for the entire LankaGo Admin module.
 *
 * Scoped to the Admin Navigation Graph / Activity.
 * Holds reactive [StateFlow] in-memory stores for:
 * 1. Rentals ([RentalShop])
 * 2. Local Products ([LocalProduct])
 * 3. Bookable Activities ([ActivityItem])
 * 4. User Reservations ([Booking])
 */
class AdminSharedViewModel : ViewModel() {

    // --- Authentication ---
    private val _authState = MutableStateFlow<AdminAuthState>(AdminAuthState.Idle)
    val authState: StateFlow<AdminAuthState> = _authState.asStateFlow()

    // --- One-off UI Events ---
    private val _uiEvent = MutableSharedFlow<AdminSharedUiEvent>()
    val uiEvent: SharedFlow<AdminSharedUiEvent> = _uiEvent.asSharedFlow()

    // =========================================================================
    // 1. RENTALS STATE & CRUD
    // =========================================================================
    private val _rentals = MutableStateFlow<List<RentalShop>>(defaultMockRentals())
    val rentals: StateFlow<List<RentalShop>> = _rentals.asStateFlow()

    fun getRentalById(id: String): RentalShop? = _rentals.value.find { it.id == id }

    fun addRental(
        name: String,
        description: String,
        operatingHours: String,
        contactNumber: String,
        whatsappNumber: String,
        priceStartingFrom: String = "LKR 3,500/day",
        city: String = "Colombo",
        address: String = "Sri Lanka",
        imageUrl: String = ""
    ): RentalShop {
        val newRental = RentalShop(
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
            fleetAvailable = listOf("125cc Automatic Scooters", "Helmets Included", "24/7 Roadside Assistance"),
            priceStartingFrom = priceStartingFrom.trim()
        )
        _rentals.value = listOf(newRental) + _rentals.value
        emitSuccess("Rental \"${newRental.name}\" created successfully")
        return newRental
    }

    fun updateRental(
        id: String,
        name: String,
        description: String,
        operatingHours: String,
        contactNumber: String,
        whatsappNumber: String,
        priceStartingFrom: String,
        city: String,
        address: String = "Sri Lanka",
        imageUrl: String = ""
    ) {
        val current = _rentals.value.toMutableList()
        val index = current.indexOfFirst { it.id == id }
        if (index != -1) {
            val existing = current[index]
            val updated = existing.copy(
                name = name.trim(),
                description = description.trim(),
                operatingHours = operatingHours.trim(),
                contactNumber = contactNumber.trim(),
                whatsappNumber = whatsappNumber.trim(),
                priceStartingFrom = priceStartingFrom.trim(),
                city = city.trim(),
                address = address.trim(),
                imageUrl = if (imageUrl.isNotBlank()) imageUrl.trim() else existing.imageUrl
            )
            current[index] = updated
            _rentals.value = current
            emitSuccess("Rental \"${updated.name}\" updated successfully")
        }
    }

    fun deleteRental(id: String) {
        val target = _rentals.value.find { it.id == id }
        _rentals.value = _rentals.value.filterNot { it.id == id }
        emitSuccess("Deleted rental \"${target?.name ?: id}\"")
    }

    // =========================================================================
    // 2. PRODUCTS STATE & CRUD
    // =========================================================================
    private val _products = MutableStateFlow<List<LocalProduct>>(defaultMockProducts())
    val products: StateFlow<List<LocalProduct>> = _products.asStateFlow()

    fun getProductById(id: String): LocalProduct? = _products.value.find { it.id == id }

    fun addProduct(
        name: String,
        category: String,
        description: String,
        priceLkr: Double,
        originRegion: String = "Sri Lanka",
        stockQuantity: Int = 50,
        imageUrl: String = ""
    ): LocalProduct {
        val newProduct = LocalProduct(
            id = "prod_${UUID.randomUUID().toString().take(8)}",
            name = name.trim(),
            category = category.trim(),
            description = description.trim(),
            priceLkr = priceLkr,
            originRegion = originRegion.trim(),
            inStock = stockQuantity > 0,
            stockQuantity = stockQuantity,
            imageUrl = imageUrl.trim(),
            imageRes = R.drawable.ceylon_tea_set,
            rating = 5.0f,
            reviewCount = 1
        )
        _products.value = listOf(newProduct) + _products.value
        emitSuccess("Product \"${newProduct.name}\" added successfully")
        return newProduct
    }

    fun updateProduct(
        id: String,
        name: String,
        category: String,
        description: String,
        priceLkr: Double,
        originRegion: String,
        inStock: Boolean,
        stockQuantity: Int,
        imageUrl: String = ""
    ) {
        val current = _products.value.toMutableList()
        val index = current.indexOfFirst { it.id == id }
        if (index != -1) {
            val existing = current[index]
            val updated = existing.copy(
                name = name.trim(),
                category = category.trim(),
                description = description.trim(),
                priceLkr = priceLkr,
                originRegion = originRegion.trim(),
                inStock = inStock,
                stockQuantity = stockQuantity,
                imageUrl = if (imageUrl.isNotBlank()) imageUrl.trim() else existing.imageUrl
            )
            current[index] = updated
            _products.value = current
            emitSuccess("Product \"${updated.name}\" updated successfully")
        }
    }

    fun deleteProduct(id: String) {
        val target = _products.value.find { it.id == id }
        _products.value = _products.value.filterNot { it.id == id }
        emitSuccess("Deleted product \"${target?.name ?: id}\"")
    }

    // =========================================================================
    // 3. ACTIVITIES STATE & CRUD
    // =========================================================================
    private val _activities = MutableStateFlow<List<ActivityItem>>(defaultMockActivities())
    val activities: StateFlow<List<ActivityItem>> = _activities.asStateFlow()

    fun getActivityById(id: String): ActivityItem? = _activities.value.find { it.id == id }

    fun addActivity(
        title: String,
        category: String,
        location: String,
        address: String,
        durationHours: Double,
        basePriceLkr: Double,
        description: String,
        imageUrl: String = ""
    ): ActivityItem {
        val newActivity = ActivityItem(
            id = "act_${UUID.randomUUID().toString().take(8)}",
            title = title.trim(),
            category = category.trim(),
            location = location.trim(),
            address = address.trim(),
            durationHours = durationHours,
            basePriceLkr = basePriceLkr,
            serviceFeeLkr = basePriceLkr * 0.1,
            rating = 5.0f,
            reviewCount = 1,
            isAvailable = true,
            imageUrl = imageUrl.trim(),
            description = description.trim(),
            includedItems = listOf("Expert Local Guide", "Safety Equipment", "Hydration pack")
        )
        _activities.value = listOf(newActivity) + _activities.value
        emitSuccess("Activity \"${newActivity.title}\" created successfully")
        return newActivity
    }

    fun updateActivity(
        id: String,
        title: String,
        category: String,
        location: String,
        address: String,
        durationHours: Double,
        basePriceLkr: Double,
        description: String,
        isAvailable: Boolean,
        imageUrl: String = ""
    ) {
        val current = _activities.value.toMutableList()
        val index = current.indexOfFirst { it.id == id }
        if (index != -1) {
            val existing = current[index]
            val updated = existing.copy(
                title = title.trim(),
                category = category.trim(),
                location = location.trim(),
                address = address.trim(),
                durationHours = durationHours,
                basePriceLkr = basePriceLkr,
                serviceFeeLkr = basePriceLkr * 0.1,
                description = description.trim(),
                isAvailable = isAvailable,
                imageUrl = if (imageUrl.isNotBlank()) imageUrl.trim() else existing.imageUrl
            )
            current[index] = updated
            _activities.value = current
            emitSuccess("Activity \"${updated.title}\" updated successfully")
        }
    }

    fun deleteActivity(id: String) {
        val target = _activities.value.find { it.id == id }
        _activities.value = _activities.value.filterNot { it.id == id }
        emitSuccess("Deleted activity \"${target?.title ?: id}\"")
    }

    // =========================================================================
    // 4. USER BOOKINGS STATE & ACTIONS
    // =========================================================================
    private val _bookings = MutableStateFlow<List<Booking>>(defaultMockBookings())
    val bookings: StateFlow<List<Booking>> = _bookings.asStateFlow()

    fun updateBookingStatus(id: String, newStatus: BookingStatus) {
        val current = _bookings.value.toMutableList()
        val index = current.indexOfFirst { it.id == id }
        if (index != -1) {
            val updated = current[index].copy(status = newStatus)
            current[index] = updated
            _bookings.value = current
            emitSuccess("Booking #${id.take(8)} marked as ${newStatus.name}")
        }
    }

    // =========================================================================
    // AUTH & HELPERS
    // =========================================================================
    fun authenticate(username: String, password: String): Boolean {
        val trimmedUser = username.trim()
        val trimmedPass = password.trim()
        return if (trimmedUser == "admin" && trimmedPass == "admin123") {
            _authState.value = AdminAuthState.Authenticated(username = trimmedUser)
            true
        } else {
            val errorMsg = if (trimmedUser.isEmpty() || trimmedPass.isEmpty()) {
                "Username and password are required"
            } else {
                "Invalid admin credentials. Use 'admin' and 'admin123'"
            }
            _authState.value = AdminAuthState.Error(errorMsg)
            viewModelScope.launch {
                _uiEvent.emit(AdminSharedUiEvent.ShowSnackbar(errorMsg, isError = true))
            }
            false
        }
    }

    fun logout() {
        _authState.value = AdminAuthState.Idle
    }

    private fun emitSuccess(message: String) {
        viewModelScope.launch {
            _uiEvent.emit(AdminSharedUiEvent.OperationSuccess(message))
            _uiEvent.emit(AdminSharedUiEvent.ShowSnackbar(message, isError = false))
        }
    }

    // =========================================================================
    // REALISTIC DEFAULT MOCK DATA PROVIDERS
    // =========================================================================
    private fun defaultMockRentals(): List<RentalShop> = try {
        RentalShopDataProvider.shops.ifEmpty { fallbackRentals() }
    } catch (_: Exception) {
        fallbackRentals()
    }

    private fun fallbackRentals(): List<RentalShop> = listOf(
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

    private fun defaultMockProducts(): List<LocalProduct> = listOf(
        LocalProduct(
            id = "prod_ceylon_tea",
            name = "Nuwara Eliya Orange Pekoe Ceylon Tea",
            category = "Ceylon Tea",
            description = "Single-origin artisan high-grown black tea hand-plucked from the mist-covered slopes of Nuwara Eliya. Delicate floral aroma and bright golden liquor.",
            priceLkr = 2850.0,
            originRegion = "Nuwara Eliya",
            inStock = true,
            stockQuantity = 85,
            imageUrl = "https://images.unsplash.com/photo-1576092768241-dec231879fc3",
            imageRes = R.drawable.ceylon_tea_set,
            rating = 4.9f,
            reviewCount = 148
        ),
        LocalProduct(
            id = "prod_pure_cinnamon",
            name = "True Ceylon Cinnamon Quills (Alba Grade)",
            category = "Spices",
            description = "Finest hand-rolled Alba grade sweet cinnamon from Mirissa plantations. Naturally low in coumarin and certified authentic Sri Lankan export grade.",
            priceLkr = 3400.0,
            originRegion = "Southern Province",
            inStock = true,
            stockQuantity = 60,
            imageUrl = "https://images.unsplash.com/photo-1509358271058-acd22cc93898",
            imageRes = R.drawable.sri_lankan_spices,
            rating = 5.0f,
            reviewCount = 94
        ),
        LocalProduct(
            id = "prod_wooden_mask",
            name = "Traditional Ambalangoda Mayura Raksha Mask",
            category = "Handicrafts",
            description = "Hand-carved Kaduru wood ceremonial peacock mask hand-painted with organic pigments by master craftsmen in Ambalangoda to ward off misfortune.",
            priceLkr = 7500.0,
            originRegion = "Ambalangoda",
            inStock = true,
            stockQuantity = 14,
            imageUrl = "https://images.unsplash.com/photo-1578328819058-b69f3a3b0f6b",
            imageRes = R.drawable.mask_painting,
            rating = 4.8f,
            reviewCount = 57
        ),
        LocalProduct(
            id = "prod_batik_sarong",
            name = "Pure Cotton Hand-Dyed Heritage Batik",
            category = "Batik",
            description = "Artisanal wax-resist dyed cotton wall tapestry depicting ancient Esala Perahera temple processions in deep indigo and marigold hues.",
            priceLkr = 5200.0,
            originRegion = "Kandy",
            inStock = true,
            stockQuantity = 22,
            imageUrl = "https://images.unsplash.com/photo-1607344645866-009c320c5ab8",
            imageRes = R.drawable.batik_workshop,
            rating = 4.7f,
            reviewCount = 41
        )
    )

    private fun defaultMockActivities(): List<ActivityItem> = listOf(
        ActivityItem(
            id = "act_sigiriya_sunrise",
            title = "Sigiriya Rock Fortress Sunrise & Village Walk",
            category = "Heritage",
            location = "Sigiriya",
            address = "Sigiriya Rock Entrance Road, Matale",
            durationHours = 4.5,
            basePriceLkr = 8500.0,
            serviceFeeLkr = 850.0,
            rating = 4.9f,
            reviewCount = 210,
            isAvailable = true,
            imageUrl = "https://images.unsplash.com/photo-1586861635167-e5223aadc9fe",
            description = "Ascend the iconic 5th-century UNESCO World Heritage rock fortress with an archeological guide followed by traditional catamaran ride and chena breakfast.",
            includedItems = listOf("Site Entrance Coordination", "Certified English Guide", "Traditional Breakfast", "Catamaran Ride")
        ),
        ActivityItem(
            id = "act_yala_safari",
            title = "Yala Leopard & Elephant Dawn Safari",
            category = "Wildlife Safari",
            location = "Yala National Park",
            address = "Palatupana Gate, Yala",
            durationHours = 5.0,
            basePriceLkr = 16000.0,
            serviceFeeLkr = 1600.0,
            rating = 4.8f,
            reviewCount = 185,
            isAvailable = true,
            imageUrl = "https://images.unsplash.com/photo-1544735716-392fe2489ffa",
            description = "Private customized 4x4 open safari jeep excursion exploring Block 1 of Yala for Sri Lankan leopards, sloth bears, wild boars, and Asian elephants.",
            includedItems = listOf("4x4 Safari Jeep Hire", "National Park Ranger", "Binoculars & Field Guide", "Chilled Refreshments")
        ),
        ActivityItem(
            id = "act_ella_train_hike",
            title = "Nine Arch Bridge & Little Adam's Peak Trek",
            category = "Adventure",
            location = "Ella",
            address = "Passara Road, Ella",
            durationHours = 3.5,
            basePriceLkr = 4500.0,
            serviceFeeLkr = 450.0,
            rating = 4.9f,
            reviewCount = 320,
            isAvailable = true,
            imageUrl = "https://images.unsplash.com/photo-1589182373726-e4f658ab50f0",
            description = "Scenic morning walk traversing lush green tea estates to witness the blue train cross Nine Arch Bridge, culminating with 360-degree views atop Little Adam's Peak.",
            includedItems = listOf("Local Hiking Naturalist", "Tasting Cup of Fresh Ceylon Tea", "First Aid Kit", "Rain Poncho")
        )
    )

    private fun defaultMockBookings(): List<Booking> = listOf(
        Booking(
            id = "bk_rent_101",
            userName = "Marcus Vance",
            userEmail = "marcus.vance@example.com",
            userPhone = "+44 7911 123456",
            itemName = "Epic Ride Scooter (125cc)",
            itemCategory = "Rental Scooters",
            bookingType = BookingType.RENTAL,
            bookingDate = "2024-11-04",
            bookingTime = "09:00 AM",
            totalPriceLkr = 10500.0,
            status = BookingStatus.CONFIRMED,
            specialNotes = "Renter requested surf-rack and 2 large helmets"
        ),
        Booking(
            id = "bk_act_201",
            userName = "Sophia Dupont",
            userEmail = "sophia.dupont@example.fr",
            userPhone = "+33 6 12 34 56 78",
            itemName = "Sigiriya Rock Fortress Sunrise & Village Walk",
            itemCategory = "Heritage",
            bookingType = BookingType.ACTIVITY,
            bookingDate = "2024-11-06",
            bookingTime = "05:30 AM",
            totalPriceLkr = 9350.0,
            status = BookingStatus.CONFIRMED,
            specialNotes = "Vegetarian breakfast requested"
        ),
        Booking(
            id = "bk_rent_102",
            userName = "Liam O'Connor",
            userEmail = "liam.oconnor@example.ie",
            userPhone = "+353 87 123 4567",
            itemName = "Colombo Scooter Rentals (TVS Ntorq)",
            itemCategory = "Rental Scooters",
            bookingType = BookingType.RENTAL,
            bookingDate = "2024-11-08",
            bookingTime = "10:30 AM",
            totalPriceLkr = 7600.0,
            status = BookingStatus.PENDING,
            specialNotes = "Airport pickup near Katunayake"
        ),
        Booking(
            id = "bk_act_202",
            userName = "Julian Meier",
            userEmail = "julian.meier@example.de",
            userPhone = "+49 170 1234567",
            itemName = "Yala Leopard & Elephant Dawn Safari",
            itemCategory = "Wildlife Safari",
            bookingType = BookingType.ACTIVITY,
            bookingDate = "2024-11-10",
            bookingTime = "05:00 AM",
            totalPriceLkr = 17600.0,
            status = BookingStatus.CONFIRMED,
            specialNotes = "Telephoto lens clamp requested for the jeep"
        ),
        Booking(
            id = "bk_rent_103",
            userName = "Amara Silva",
            userEmail = "amara.silva@example.com",
            userPhone = "+94 77 123 4567",
            itemName = "Unawatuna Beach Cruiser (Honda Dio)",
            itemCategory = "Rental Scooters",
            bookingType = BookingType.RENTAL,
            bookingDate = "2024-11-12",
            bookingTime = "02:00 PM",
            totalPriceLkr = 6600.0,
            status = BookingStatus.CONFIRMED,
            specialNotes = "Dropoff at Weligama bay"
        )
    )
}
