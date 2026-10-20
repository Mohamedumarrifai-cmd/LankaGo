package com.example.lankago.data

import com.example.lankago.R

/**
 * Data model representing a verified Rental Agency or Transport Hub in Sri Lanka.
 * Models physical hubs/shops (e.g., "Galle Bike Rentals", "Colombo Transport Hub")
 * rather than individual single vehicles.
 */
data class RentalAgency(
    val id: String,
    val name: String,
    val tagline: String,
    val category: String, // "Rental Agencies", "Taxis & Cabs", "Public Transport"
    val location: String,
    val city: String,
    val address: String,
    val rating: Float,
    val reviewCount: Int,
    val coverImageUrl: String = "",
    val imageRes: Int = 0,
    val phone: String,
    val whatsapp: String,
    val websiteUrl: String,
    val operatingHours: String,
    val isVerified: Boolean = true,
    val description: String,
    val priceRange: String,
    val fleetTypes: List<String> = emptyList(),
    val latitude: Double = 6.0535,
    val longitude: Double = 80.2210,
    val reviews: List<Review> = emptyList(),
    val nearbyAgencyIds: List<String> = emptyList()
)

/**
 * Realistic Mock Data Provider for Sri Lankan Rental Agencies and Transport Hubs.
 */
object RentalAgencyDataProvider {

    private val reviewsGalle = listOf(
        Review(
            id = "rev_1",
            userName = "Marcus Vance",
            rating = 5.0f,
            date = "14 Oct 2024",
            comment = "Rented two 125cc Scooters for 4 days exploring Galle Fort, Unawatuna, and Weligama. Flawless service, helmets included, and WhatsApp support was instant!"
        ),
        Review(
            id = "rev_2",
            userName = "Amara Silva",
            rating = 4.8f,
            date = "02 Oct 2024",
            comment = "Great rates, honest staff with zero hidden fees. Fuel tank was full upon pickup."
        ),
        Review(
            id = "rev_3",
            userName = "Julian Meier",
            rating = 4.5f,
            date = "21 Sep 2024",
            comment = "Smooth return process. Highly recommend booking via WhatsApp in advance during high season."
        )
    )

    private val reviewsColombo = listOf(
        Review(
            id = "rev_4",
            userName = "Sophia Dupont",
            rating = 4.9f,
            date = "08 Oct 2024",
            comment = "Colombo Transport Hub organized an airport transfer van and city tour cabs. Professional, punctual, and clean air-conditioned vehicles."
        ),
        Review(
            id = "rev_5",
            userName = "Dinesh Weerasinghe",
            rating = 4.7f,
            date = "29 Sep 2024",
            comment = "Very centralized place in Fort. Easy coordination between intercity buses and local taxis."
        )
    )

    private val reviewsElla = listOf(
        Review(
            id = "rev_6",
            userName = "Liam O'Connor",
            rating = 5.0f,
            date = "11 Oct 2024",
            comment = "Traveled across Nine Arch Bridge and Little Adam's Peak on their Royal Enfield. Unmatched scenic memories!"
        )
    )

    val agencies: List<RentalAgency> = listOf(
        RentalAgency(
            id = "agency_galle_bike",
            name = "Galle Bike & Scooter Hub",
            tagline = "Southern Coast's Premier Two-Wheeler Specialists",
            category = "Rental Agencies",
            location = "Galle Fort & Rampart Street",
            city = "Galle",
            address = "No. 42 Light House Street, Galle Fort, Sri Lanka",
            rating = 4.9f,
            reviewCount = 184,
            imageRes = R.drawable.casons_rent,
            phone = "+94912234890",
            whatsapp = "+94771234567",
            websiteUrl = "https://gallebikerentals.lk",
            operatingHours = "Open Daily: 07:30 AM – 09:30 PM",
            isVerified = true,
            description = "Galle Bike & Scooter Hub is Southern Sri Lanka's leading verified rental agency. Offering well-serviced automatic scooters, dual-sport adventure bikes, and classic cruisers for touring the southern coastline. Full comprehensive insurance, roadside assistance, and certified helmets provided with every rental.",
            priceRange = "$12 – $35 / Day",
            fleetTypes = listOf("125cc Automatic Scooters", "Royal Enfield 350", "Yamaha Mountain Bikes", "Electric Scooters"),
            latitude = 6.0267,
            longitude = 80.2170,
            reviews = reviewsGalle,
            nearbyAgencyIds = listOf("agency_unawatuna_rides", "agency_colombo_hub", "agency_ella_moto")
        ),
        RentalAgency(
            id = "agency_colombo_hub",
            name = "Colombo Central Transport Hub",
            tagline = "Integrated Islandwide Cabs, Sedans & Van Fleets",
            category = "Taxis & Cabs",
            location = "Fort Central, Colombo 01",
            city = "Colombo",
            address = "Olcott Mawatha, Opposite Fort Railway Station, Colombo 00100",
            rating = 4.8f,
            reviewCount = 312,
            imageRes = R.drawable.colombo_scotter,
            phone = "+94112345678",
            whatsapp = "+94779876543",
            websiteUrl = "https://colombotransporthub.lk",
            operatingHours = "Open 24/7 (Desk & Dispatch)",
            isVerified = true,
            description = "Serving as Colombo's premier intermodal transit base, Colombo Central Transport Hub connects travelers with licensed air-conditioned city cabs, inter-district highway vans, and luxury chauffeur cars for corporate or leisurely excursions. Fixed honest tariffs with digital receipts.",
            priceRange = "LKR 120/km or $45 / Day Chauffeur",
            fleetTypes = listOf("Hybrid Sedans (Toyota Prius)", "KDH High-Roof Luxury Vans", "Metered Tuk-Tuks", "Airport Express Coaches"),
            latitude = 6.9344,
            longitude = 79.8540,
            reviews = reviewsColombo,
            nearbyAgencyIds = listOf("agency_galle_bike", "agency_kandy_transport")
        ),
        RentalAgency(
            id = "agency_ella_moto",
            name = "Ella Hill Country Moto Explorers",
            tagline = "High-Altitude Scooters & Trail Bikes for Central Peaks",
            category = "Rental Agencies",
            location = "Passara Road, Ella Town",
            city = "Ella",
            address = "No. 18 Passara Road, Ella 90090, Badulla District",
            rating = 4.9f,
            reviewCount = 142,
            imageRes = R.drawable.ella_rental,
            phone = "+94572228811",
            whatsapp = "+94715553322",
            websiteUrl = "https://ellamotoadventures.com",
            operatingHours = "Open Daily: 07:00 AM – 08:30 PM",
            isVerified = true,
            description = "Designed for hill-country travelers wanting to conquer Ravana Falls, Nine Arch Bridge, and Haputale tea trails. Every motorbike is specially tuned for steep inclines and fitted with heavy-duty mountain brakes and mobile phone mounts.",
            priceRange = "$15 – $40 / Day",
            fleetTypes = listOf("Honda Dio 110cc", "Yamaha FZ 150cc", "Mountain Dual-Sport", "Touring Cycles"),
            latitude = 6.8667,
            longitude = 81.0466,
            reviews = reviewsElla,
            nearbyAgencyIds = listOf("agency_galle_bike", "agency_kandy_transport")
        ),
        RentalAgency(
            id = "agency_kandy_transport",
            name = "Kandy Royal Cabs & Transit Hub",
            tagline = "Official Heritage City Private Tours & Chauffeur Network",
            category = "Taxis & Cabs",
            location = "Dalada Veediya, Kandy City Center",
            city = "Kandy",
            address = "No. 88 Dalada Veediya, Kandy 20000",
            rating = 4.7f,
            reviewCount = 98,
            imageRes = R.drawable.kandy_bike,
            phone = "+94812239900",
            whatsapp = "+94773344556",
            websiteUrl = "https://kandycabs.lk",
            operatingHours = "Open Daily: 06:00 AM – 11:00 PM",
            isVerified = true,
            description = "Providing private transfers to Sigiriya, Knuckles Mountain Range, and Nuwara Eliya. Verified English-speaking tour drivers and clean, fully insured passenger vehicles.",
            priceRange = "$40 – $75 / Day Tour",
            fleetTypes = listOf("Toyota Axio / Allion", "7-Seater Mini Vans", "Metered City Taxis"),
            latitude = 7.2906,
            longitude = 80.6337,
            reviews = emptyList(),
            nearbyAgencyIds = listOf("agency_colombo_hub", "agency_ella_moto")
        ),
        RentalAgency(
            id = "agency_sl_railway_hub",
            name = "Sri Lanka Railways & Express Coach Hub",
            tagline = "Government Certified Public Transport Terminals",
            category = "Public Transport",
            location = "Colombo Fort & Central Bus Stand Pettah",
            city = "Colombo",
            address = "Bastian Mawatha Bus Station & Fort Railway Station, Colombo",
            rating = 4.6f,
            reviewCount = 520,
            imageRes = R.drawable.bridge,
            phone = "+94112434215",
            whatsapp = "+94770001919",
            websiteUrl = "http://railway.gov.lk",
            operatingHours = "Open 24/7 (Ticket Counters: 05:00 AM - 10:00 PM)",
            isVerified = true,
            description = "Central booking desk for the famous Kandy-to-Ella Scenic Main Line observation cars, Southern Coastal Express, and inter-provincial air-conditioned luxury highway buses.",
            priceRange = "$2 – $12 / Seat",
            fleetTypes = listOf("Blue Observation Train", "AC Expressway Luxury Bus", "Intercity Semi-Express"),
            latitude = 6.9333,
            longitude = 79.8500,
            reviews = emptyList(),
            nearbyAgencyIds = listOf("agency_colombo_hub")
        ),
        RentalAgency(
            id = "agency_unawatuna_rides",
            name = "Unawatuna Coastline Cabs & Bikes",
            tagline = "Surf & Beach Hop Beachside Transport Desk",
            category = "Rental Agencies",
            location = "Yaddehimulla Road, Unawatuna",
            city = "Unawatuna",
            address = "Beach Road, Unawatuna Bay, Galle",
            rating = 4.8f,
            reviewCount = 87,
            imageRes = R.drawable.unawatune_bike,
            phone = "+94912241515",
            whatsapp = "+94772223344",
            websiteUrl = "https://unawatunacabs.com",
            operatingHours = "Open Daily: 08:00 AM – 10:00 PM",
            isVerified = true,
            description = "Specialized in surfboard racks for scooters, beach-hop tuk tuks, and night airport runs along the southern expressway.",
            priceRange = "$10 – $28 / Day",
            fleetTypes = listOf("Scooters with Surf Racks", "Electric Fat Bikes", "Safari Open Tuk-Tuks"),
            latitude = 6.0108,
            longitude = 80.2492,
            reviews = emptyList(),
            nearbyAgencyIds = listOf("agency_galle_bike")
        )
    )

    fun getAgencyById(id: String): RentalAgency? {
        return agencies.firstOrNull { it.id == id } ?: agencies.firstOrNull()
    }

    fun getAgenciesByCategory(category: String): List<RentalAgency> {
        return agencies.filter { it.category.equals(category, ignoreCase = true) }
    }

    fun getNearbyAgencies(agency: RentalAgency): List<RentalAgency> {
        return agencies.filter { agency.nearbyAgencyIds.contains(it.id) && it.id != agency.id }
    }
}
