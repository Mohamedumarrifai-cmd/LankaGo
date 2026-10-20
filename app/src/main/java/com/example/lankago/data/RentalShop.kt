package com.example.lankago.data

import com.example.lankago.R

/**
 * Domain model representing a verified physical vehicle/bike rental shop.
 *
 * Specific required schema:
 * - [id]: Unique shop identifier
 * - [name]: Rental business name
 * - [description]: Short and detailed business overview
 * - [lat]: Latitude for Google Maps navigation
 * - [lng]: Longitude for Google Maps navigation
 * - [contactNumber]: Phone number for ACTION_DIAL
 * - [operatingHours]: Daily operating time range
 * - [imageUrl]: Cover image web URL
 */
data class RentalShop(
    val id: String,
    val name: String,
    val description: String,
    val lat: Double,
    val lng: Double,
    val contactNumber: String,
    val operatingHours: String,
    val imageUrl: String = "",
    val imageRes: Int = 0,
    val rating: Float = 4.9f,
    val reviewCount: Int = 184,
    val address: String = "",
    val city: String = "Sri Lanka",
    val whatsappNumber: String = "",
    val fleetAvailable: List<String> = emptyList(),
    val priceStartingFrom: String = "$12 / Day",
    val reviews: List<Review> = emptyList()
)

/**
 * Realistic Mock Data Provider for Sri Lanka's Verified Rental Shops.
 */
object RentalShopDataProvider {

    private val galleReviews = listOf(
        Review(
            id = "rev_galle_1",
            userName = "Marcus Vance",
            rating = 5.0f,
            date = "15 Oct 2024",
            comment = "Rented two 125cc scooters for four days to tour Galle, Unawatuna, and Weligama. Helmets were brand new and the bikes were immaculate!"
        ),
        Review(
            id = "rev_galle_2",
            userName = "Amara Silva",
            rating = 4.8f,
            date = "03 Oct 2024",
            comment = "Transparent pricing with no hidden charges. Very quick WhatsApp assistance when we wanted to extend our booking."
        ),
        Review(
            id = "rev_galle_3",
            userName = "Julian Meier",
            rating = 4.9f,
            date = "22 Sep 2024",
            comment = "Top tier rental shop in Galle Fort. Comprehensive insurance included and easy return process."
        )
    )

    private val ellaReviews = listOf(
        Review(
            id = "rev_ella_1",
            userName = "Liam O'Connor",
            rating = 5.0f,
            date = "12 Oct 2024",
            comment = "Rented an adventure trail bike to explore Ravana Falls and Nine Arch Bridge. Brakes and hill performance were great!"
        ),
        Review(
            id = "rev_ella_2",
            userName = "Elena Rostova",
            rating = 4.7f,
            date = "28 Sep 2024",
            comment = "Friendly staff who also recommended scenic off-the-beaten-path tea plantation viewpoints. Highly recommended!"
        )
    )

    private val colomboReviews = listOf(
        Review(
            id = "rev_cmb_1",
            userName = "Sophia Dupont",
            rating = 4.9f,
            date = "09 Oct 2024",
            comment = "Booked a self-drive SUV for our coastal expressway trip to Mirissa. Air conditioning was ice-cold and vehicle was spotless."
        ),
        Review(
            id = "rev_cmb_2",
            userName = "Kasun Jayasuriya",
            rating = 4.8f,
            date = "01 Oct 2024",
            comment = "Convenient pickup counter right next to Fort station. Smooth documentation and deposit return."
        )
    )

    val shops: List<RentalShop> = listOf(
        RentalShop(
            id = "shop_galle_bike",
            name = "Galle Fort Bike & Scooter Hub",
            description = "Southern Province's premier two-wheeler rental specialist. Offering serviced automatic scooters, classic Royal Enfields, and high-traction mountain bicycles for exploring coastal ramparts and beaches.",
            lat = 6.0267,
            lng = 80.2170,
            contactNumber = "+94912234890",
            operatingHours = "07:30 AM – 09:30 PM (Daily)",
            imageUrl = "https://images.unsplash.com/photo-1558981403-c5f9899a28bc",
            imageRes = R.drawable.casons_rent,
            rating = 4.92f,
            reviewCount = 184,
            address = "No. 42 Light House Street, Galle Fort",
            city = "Galle",
            whatsappNumber = "+94771234567",
            fleetAvailable = listOf("125cc Automatic Scooters", "Royal Enfield Classic 350", "Electric City Bikes", "Mountain Trail Bikes"),
            priceStartingFrom = "$12 / Day",
            reviews = galleReviews
        ),
        RentalShop(
            id = "shop_ella_moto",
            name = "Ella Mountain Moto Adventures",
            description = "High-altitude dual-sport motorbikes and scooters engineered specifically for steep hill-country gradients, tea plantation trails, and scenic mountain passes.",
            lat = 6.8667,
            lng = 81.0466,
            contactNumber = "+94572228811",
            operatingHours = "07:00 AM – 08:30 PM (Daily)",
            imageUrl = "https://images.unsplash.com/photo-1568772585407-9361f9bf3a87",
            imageRes = R.drawable.ella_rental,
            rating = 4.88f,
            reviewCount = 142,
            address = "No. 18 Passara Road, Ella Town",
            city = "Ella",
            whatsappNumber = "+94715553322",
            fleetAvailable = listOf("Honda Dio 110cc", "Yamaha FZ 150cc", "Dual-Sport Trail 250cc", "Touring Mountain Bikes"),
            priceStartingFrom = "$15 / Day",
            reviews = ellaReviews
        ),
        RentalShop(
            id = "shop_colombo_rentals",
            name = "Colombo Central Car & SUV Rentals",
            description = "Islandwide fleet of modern hybrid sedans, 4x4 safari SUVs, and luxury passenger vans. Ideal for family road trips, business transfers, and expressway touring.",
            lat = 6.9344,
            lng = 79.8540,
            contactNumber = "+94112345678",
            operatingHours = "Open 24 Hours / 7 Days",
            imageUrl = "https://images.unsplash.com/photo-1549399542-7e3f8b79c341",
            imageRes = R.drawable.colombo_scotter,
            rating = 4.85f,
            reviewCount = 310,
            address = "Olcott Mawatha, Opposite Fort Station, Colombo 01",
            city = "Colombo",
            whatsappNumber = "+94779876543",
            fleetAvailable = listOf("Toyota Prius Hybrid", "Toyota Prado 4x4 SUV", "KDH High-Roof Luxury Van", "Metered City Tuk-Tuks"),
            priceStartingFrom = "$35 / Day",
            reviews = colomboReviews
        ),
        RentalShop(
            id = "shop_kandy_rides",
            name = "Kandy Heritage Wheels & Cab Network",
            description = "Specialized in hill-country touring bikes and reliable private rental vehicles for excursions to Knuckles Mountain Range, Sigiriya, and Nuwara Eliya.",
            lat = 7.2906,
            lng = 80.6337,
            contactNumber = "+94812239900",
            operatingHours = "06:30 AM – 10:00 PM (Daily)",
            imageUrl = "https://images.unsplash.com/photo-1558981806-ec527fa84c39",
            imageRes = R.drawable.kandy_bike,
            rating = 4.79f,
            reviewCount = 115,
            address = "No. 88 Dalada Veediya, Kandy City Center",
            city = "Kandy",
            whatsappNumber = "+94773344556",
            fleetAvailable = listOf("Automatic Scooters", "Toyota Axio Sedan", "Compact City Cars"),
            priceStartingFrom = "$14 / Day",
            reviews = emptyList()
        ),
        RentalShop(
            id = "shop_mirissa_tuktuk",
            name = "Mirissa Coastline Tuk-Tuk & Bike Rentals",
            description = "Self-drive convertible tuk-tuks and surfboard-fitted scooters. The ultimate way to hop between Weligama, Mirissa, Hiriketiya, and Tangalle beach breaks.",
            lat = 5.9482,
            lng = 80.4578,
            contactNumber = "+94412255678",
            operatingHours = "08:00 AM – 09:00 PM (Daily)",
            imageUrl = "https://images.unsplash.com/photo-1590483736622-39da86788790",
            imageRes = R.drawable.galle_tuktuk,
            rating = 4.94f,
            reviewCount = 205,
            address = "Harbour Road, Mirissa Bay",
            city = "Mirissa",
            whatsappNumber = "+94778899001",
            fleetAvailable = listOf("Self-Drive Licensed Tuk-Tuks", "Scooters with Surf Racks", "Electric Fat Bikes"),
            priceStartingFrom = "$16 / Day",
            reviews = emptyList()
        )
    )

    fun getAllShops(): List<RentalShop> = shops

    fun getShopById(id: String): RentalShop? {
        return shops.firstOrNull { it.id == id } ?: shops.firstOrNull()
    }
}
