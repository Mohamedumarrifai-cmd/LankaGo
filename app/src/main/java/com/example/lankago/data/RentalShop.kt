package com.example.lankago.data

import com.example.lankago.R


 // Domain model representing a verified physical vehicle/bike rental shop.

data class RentalShop(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val lat: Double = 6.9271,
    val lng: Double = 79.8612,
    val contactNumber: String = "",
    val operatingHours: String = "",
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
            id = "shop_epic_ride",
            name = "Epic Ride Scooter Rental",
            description = "Premier coastal scooter rental in Galle Fort. Spotless Honda Dio & Yamaha RayZR scooters, sterilized helmets, full comprehensive insurance, and free 24/7 roadside assistance.",
            lat = 6.0267,
            lng = 80.2170,
            contactNumber = "+94912248899",
            operatingHours = "07:30 AM – 09:30 PM (Daily)",
            imageUrl = "",
            imageRes = R.drawable.epic_ride,
            rating = 4.9f,
            reviewCount = 196,
            address = "No. 28 Church Street, Galle Fort",
            city = "Galle Fort",
            whatsappNumber = "+94771248899",
            fleetAvailable = listOf("125cc Automatic Scooters", "Honda Dio 110cc", "Yamaha RayZR", "Surf Rack Scooters"),
            priceStartingFrom = "LKR 3,500/day",
            reviews = galleReviews
        ),
        RentalShop(
            id = "shop_colombo_scooter",
            name = "Colombo Scooter Rentals",
            description = "Convenient two-wheeler rentals right in the heart of Colombo. Perfect for commuting, city sightseeing, and beating expressway traffic. Flexible hourly and daily packages.",
            lat = 6.9067,
            lng = 79.8510,
            contactNumber = "+94112573344",
            operatingHours = "08:00 AM – 09:00 PM (Daily)",
            imageUrl = "",
            imageRes = R.drawable.colombo_scotter,
            rating = 4.6f,
            reviewCount = 142,
            address = "No. 15 Galle Road, Colombo 03",
            city = "Colombo 03",
            whatsappNumber = "+94772573344",
            fleetAvailable = listOf("Automatic City Scooters", "Yamaha Fascino 125cc", "TVS Ntorq 125", "Delivery Scooters"),
            priceStartingFrom = "LKR 3,800/day",
            reviews = colomboReviews
        ),
        RentalShop(
            id = "shop_unawatuna",
            name = "Unawatuna Scooter Rental",
            description = "Beachside scooter & motorbike hire specialist. Custom racks for surfboards, beach gear storage, and curated South Coast maps for coastal riding towards Mirissa and Weligama.",
            lat = 6.0108,
            lng = 80.2486,
            contactNumber = "+94912281122",
            operatingHours = "07:00 AM – 10:00 PM (Daily)",
            imageUrl = "",
            imageRes = R.drawable.unawatune_bike,
            rating = 4.7f,
            reviewCount = 178,
            address = "Yaddehimulla Road, Unawatuna Beach",
            city = "Unawatuna",
            whatsappNumber = "+94712281122",
            fleetAvailable = listOf("Scooters with Surf Racks", "Honda Navi 110cc", "125cc Automatic Scooters", "Electric Beach Cruisers"),
            priceStartingFrom = "LKR 3,300/day",
            reviews = galleReviews
        ),
        RentalShop(
            id = "shop_dambulla_scooter",
            name = "Dambulla Scooter Rentals",
            description = "Explore the Cultural Triangle at your own pace. Reliable scooters and bikes to ride between Dambulla Cave Temple, Sigiriya Rock Fortress, and Pidurangala viewpoints.",
            lat = 7.8731,
            lng = 80.6517,
            contactNumber = "+94662284455",
            operatingHours = "06:30 AM – 08:30 PM (Daily)",
            imageUrl = "",
            imageRes = R.drawable.dambulla_bike,
            rating = 4.5f,
            reviewCount = 120,
            address = "Kurunegala-Dambulla Road, Dambulla",
            city = "Dambulla",
            whatsappNumber = "+94762284455",
            fleetAvailable = listOf("Honda Activa 125cc", "Hero Pleasure", "TVS Jupiter", "Mountain Bikes"),
            priceStartingFrom = "LKR 3,400/day",
            reviews = ellaReviews
        ),
        RentalShop(
            id = "shop_hikkaduwa",
            name = "Hikkaduwa Scooter Rental",
            description = "Top-rated scooter and surfboard rental station in Hikkaduwa. Friendly multilingual staff, transparent rates, full insurance, and free breakdown assistance across Galle District.",
            lat = 6.1395,
            lng = 80.1063,
            contactNumber = "+94912277788",
            operatingHours = "07:30 AM – 09:30 PM (Daily)",
            imageUrl = "",
            imageRes = R.drawable.hikka_rent,
            rating = 4.7f,
            reviewCount = 164,
            address = "Galle Road, Narigama, Hikkaduwa",
            city = "Hikkaduwa",
            whatsappNumber = "+94772277788",
            fleetAvailable = listOf("125cc Scooters", "Yamaha RayZR", "Classic Vespa", "Surfboard Racks"),
            priceStartingFrom = "LKR 3,200/day",
            reviews = galleReviews
        ),
        RentalShop(
            id = "shop_kandy_bike",
            name = "Kandy Bike Rental",
            description = "Hill country motorbike and scooter specialists. Powerful engines tuned for highland climbs, scenic mountain viewpoints, and Knuckles trekking expeditions.",
            lat = 7.2906,
            lng = 80.6337,
            contactNumber = "+94812239900",
            operatingHours = "07:00 AM – 09:00 PM (Daily)",
            imageUrl = "",
            imageRes = R.drawable.kandy_bike,
            rating = 4.8f,
            reviewCount = 152,
            address = "No. 54 Peradeniya Road, Kandy",
            city = "Kandy",
            whatsappNumber = "+94773344556",
            fleetAvailable = listOf("Honda Dio 125cc", "Yamaha FZ 150cc", "Royal Enfield Classic", "Highland Touring Bikes"),
            priceStartingFrom = "LKR 4,000/day",
            reviews = colomboReviews
        ),
        RentalShop(
            id = "shop_galle_bike",
            name = "Galle Fort Bike & Scooter Hub",
            description = "Southern Province's premier two-wheeler rental specialist. Offering serviced automatic scooters, classic Royal Enfields, and high-traction mountain bicycles for exploring coastal ramparts and beaches.",
            lat = 6.0267,
            lng = 80.2170,
            contactNumber = "+94912234890",
            operatingHours = "07:30 AM – 09:30 PM (Daily)",
            imageUrl = "",
            imageRes = R.drawable.epic_ride,
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
            imageUrl = "",
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
            imageUrl = "",
            imageRes = R.drawable.aqua,
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
            imageUrl = "",
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
            imageUrl = "",
            imageRes = R.drawable.galle_tuktuk,
            rating = 4.94f,
            reviewCount = 205,
            address = "Harbour Road, Mirissa Bay",
            city = "Mirissa",
            whatsappNumber = "+94778899001",
            fleetAvailable = listOf("Self-Drive Licensed Tuk-Tuks", "Scooters with Surf Racks", "Electric Fat Bikes"),
            priceStartingFrom = "LKR 3,500/day",
            reviews = emptyList()
        ),
        RentalShop(
            id = "shop_arugam_bike",
            name = "Arugam Bay Surf & Bike Rentals",
            description = "Beach cruisers, surf-rack scooters, and rugged off-road motorbikes for exploring surf points from Main Point to Peanut Farm and Panama.",
            lat = 6.8415,
            lng = 81.8340,
            contactNumber = "+94632225544",
            operatingHours = "06:30 AM – 09:00 PM (Daily)",
            imageUrl = "",
            imageRes = R.drawable.beach_bentota,
            rating = 4.7f,
            reviewCount = 148,
            address = "Main Street, Arugam Bay",
            city = "Arugam Bay",
            whatsappNumber = "+94772225544",
            fleetAvailable = listOf("Scooters with Surf Racks", "125cc Automatic Scooters", "Trail Bikes"),
            priceStartingFrom = "LKR 3,600/day",
            reviews = emptyList()
        ),
        RentalShop(
            id = "shop_malkey_car",
            name = "Malkey Rent A Car",
            description = "Sri Lanka's trusted vehicle rental fleet offering self-drive and chauffeur-driven modern sedans, hybrid hatchbacks, and executive cars.",
            lat = 6.9067,
            lng = 79.8600,
            contactNumber = "+94112365251",
            operatingHours = "Open 24 Hours / 7 Days",
            imageUrl = "",
            imageRes = R.drawable.aqua,
            rating = 4.8f,
            reviewCount = 340,
            address = "No. 58 Pamankada Road, Colombo 06",
            city = "Colombo & Islandwide",
            whatsappNumber = "+94772365251",
            fleetAvailable = listOf("Toyota Aqua Hybrid", "Toyota Axio", "Suzuki Swift", "Toyota Prius"),
            priceStartingFrom = "LKR 8,500/day",
            reviews = colomboReviews
        ),
        RentalShop(
            id = "shop_casons_car",
            name = "Casons Rent A Car",
            description = "Leading car hire agency with airport counter and islandwide delivery. Comprehensive insurance, zero hidden deposits, and well-maintained fleet.",
            lat = 6.9271,
            lng = 79.8612,
            contactNumber = "+94112574488",
            operatingHours = "Open 24 Hours / 7 Days",
            imageUrl = "",
            imageRes = R.drawable.alto,
            rating = 4.9f,
            reviewCount = 420,
            address = "Airport Delivery & No. 34 Kollupitiya, Colombo 03",
            city = "Airport & Islandwide",
            whatsappNumber = "+94772574488",
            fleetAvailable = listOf("Suzuki Alto Auto", "Toyota Vitz", "Toyota Corolla", "4x4 SUV"),
            priceStartingFrom = "LKR 9,000/day",
            reviews = colomboReviews
        ),
        RentalShop(
            id = "shop_kings_car",
            name = "Kings Rent A Car",
            description = "Premium and luxury vehicle rentals for executive transfers, weddings, and self-drive luxury expressway touring.",
            lat = 6.9000,
            lng = 79.8660,
            contactNumber = "+94112699888",
            operatingHours = "07:30 AM – 09:30 PM (Daily)",
            imageUrl = "",
            imageRes = R.drawable.aqua,
            rating = 4.7f,
            reviewCount = 180,
            address = "No. 12 Torrington Avenue, Colombo 07",
            city = "Colombo 07",
            whatsappNumber = "+94772699888",
            fleetAvailable = listOf("Mercedes-Benz C-Class", "BMW 3 Series", "Toyota Prado 4x4", "Audi A4"),
            priceStartingFrom = "LKR 10,500/day",
            reviews = emptyList()
        ),
        RentalShop(
            id = "shop_euro_car",
            name = "Europcar Sri Lanka",
            description = "International standard car rental network with full roadside rescue, unlimited mileage options, and expressway ETC tags.",
            lat = 6.9300,
            lng = 79.8500,
            contactNumber = "+94112445566",
            operatingHours = "Open 24 Hours / 7 Days",
            imageUrl = "",
            imageRes = R.drawable.alto,
            rating = 4.8f,
            reviewCount = 210,
            address = "World Trade Center, Fort, Colombo 01",
            city = "Colombo",
            whatsappNumber = "+94772445566",
            fleetAvailable = listOf("Compact Sedans", "Family SUVs", "Electric Vehicles"),
            priceStartingFrom = "LKR 12,000/day",
            reviews = emptyList()
        ),
        RentalShop(
            id = "shop_casons_van",
            name = "Casons Vans & Passenger Fleet",
            description = "Luxury Toyota KDH Super GL and high-roof commuter vans with rear AC, reclining captain seats, and professional tourist drivers.",
            lat = 6.9200,
            lng = 79.8600,
            contactNumber = "+94112574499",
            operatingHours = "Open 24 Hours / 7 Days",
            imageUrl = "",
            imageRes = R.drawable.casons_rent,
            rating = 4.9f,
            reviewCount = 390,
            address = "Kollupitiya & BIA Airport Counter",
            city = "Islandwide",
            whatsappNumber = "+94772574499",
            fleetAvailable = listOf("Toyota KDH High-Roof 14 Seats", "Toyota Regius Van 9 Seats", "Luxury Mini-Coach"),
            priceStartingFrom = "LKR 14,000/day",
            reviews = colomboReviews
        ),
        RentalShop(
            id = "shop_malkey_van",
            name = "Malkey Vans & Safari Fleet",
            description = "Spacious tourist vans and 4x4 safari modified Land Cruisers for national parks and family round-island tours.",
            lat = 6.9067,
            lng = 79.8600,
            contactNumber = "+94112365252",
            operatingHours = "07:00 AM – 10:00 PM (Daily)",
            imageUrl = "",
            imageRes = R.drawable.casons_rent,
            rating = 4.8f,
            reviewCount = 275,
            address = "Pamankada Road, Colombo 06",
            city = "Islandwide",
            whatsappNumber = "+94772365252",
            fleetAvailable = listOf("Toyota KDH Flat-Roof", "Hiace Super GL", "Safari 4x4 Cruiser"),
            priceStartingFrom = "LKR 15,000/day",
            reviews = colomboReviews
        ),
        RentalShop(
            id = "shop_kandy_van",
            name = "Kandy Tours & Vans",
            description = "Hill country van specialists with experienced highland chauffeurs for Ella, Nuwara Eliya, Knuckles, and Sigiriya excursions.",
            lat = 7.2900,
            lng = 80.6300,
            contactNumber = "+94812235566",
            operatingHours = "06:00 AM – 10:00 PM (Daily)",
            imageUrl = "",
            imageRes = R.drawable.kandy_bike,
            rating = 4.7f,
            reviewCount = 160,
            address = "Dalada Veediya, Kandy",
            city = "Kandy",
            whatsappNumber = "+94772235566",
            fleetAvailable = listOf("Toyota KDH 10-Seater", "High-Roof Van", "Micro MPV"),
            priceStartingFrom = "LKR 13,500/day",
            reviews = emptyList()
        ),
        RentalShop(
            id = "shop_negombo_tuktuk",
            name = "Negombo Tuk Tuk Tours",
            description = "Self-drive licensed tourist tuk-tuks with luggage racks, Bluetooth speakers, and full driving lesson included upon pickup.",
            lat = 7.2083,
            lng = 79.8358,
            contactNumber = "+94312234455",
            operatingHours = "07:00 AM – 09:00 PM (Daily)",
            imageUrl = "",
            imageRes = R.drawable.galle_tuktuk,
            rating = 4.8f,
            reviewCount = 220,
            address = "Lewis Place, Negombo Beach",
            city = "Negombo",
            whatsappNumber = "+94772234455",
            fleetAvailable = listOf("Bajaj 4-Stroke Tuk-Tuk", "Custom Open-Air Tuk-Tuk", "Electric Three-Wheeler"),
            priceStartingFrom = "LKR 2,500/day",
            reviews = emptyList()
        ),
        RentalShop(
            id = "shop_galle_tuktuk",
            name = "Galle Tuk Tuk Rental",
            description = "Iconic three-wheelers tailored for travelers wanting to cruise along Unawatuna, Thalpe, and Mirissa coastal roads at their own pace.",
            lat = 6.0300,
            lng = 80.2200,
            contactNumber = "+94912233445",
            operatingHours = "07:30 AM – 09:30 PM (Daily)",
            imageUrl = "",
            imageRes = R.drawable.galle_tuktuk,
            rating = 4.9f,
            reviewCount = 280,
            address = "Main Street, Galle Fort",
            city = "Galle Fort",
            whatsappNumber = "+94772233445",
            fleetAvailable = listOf("Bajaj RE Compact Tuk-Tuk", "Tuk-Tuk with Surf Racks"),
            priceStartingFrom = "LKR 2,800/day",
            reviews = galleReviews
        ),
        RentalShop(
            id = "shop_colombo_tuktuk",
            name = "Colombo Tuk Tuk Tours",
            description = "Certified metered city tuk-tuks and self-drive holiday rentals with tourist permits and full roadside insurance.",
            lat = 6.9200,
            lng = 79.8500,
            contactNumber = "+94112334455",
            operatingHours = "07:00 AM – 11:00 PM (Daily)",
            imageUrl = "",
            imageRes = R.drawable.galle_tuktuk,
            rating = 4.6f,
            reviewCount = 190,
            address = "Galle Face Centre Road, Colombo 03",
            city = "Colombo",
            whatsappNumber = "+94772334455",
            fleetAvailable = listOf("City Tuk-Tuks", "Retro Three-Wheelers"),
            priceStartingFrom = "LKR 2,400/day",
            reviews = colomboReviews
        ),
        RentalShop(
            id = "shop_lanka_bus",
            name = "Lanka Coach & Bus Tours",
            description = "Air-conditioned 28 to 45 seat luxury touring coaches for large group excursions, corporate retreats, and school heritage pilgrimages.",
            lat = 6.9344,
            lng = 79.8540,
            contactNumber = "+94112345679",
            operatingHours = "Open 24 Hours / 7 Days",
            imageUrl = "",
            imageRes = R.drawable.colombo_scotter,
            rating = 4.7f,
            reviewCount = 140,
            address = "Central Coach Terminal, Bastian Mawatha, Colombo",
            city = "Colombo",
            whatsappNumber = "+94772345679",
            fleetAvailable = listOf("28-Seater Luxury Mini-Coach", "45-Seater Highway Tour Bus"),
            priceStartingFrom = "LKR 25,000/day",
            reviews = emptyList()
        )
    )

    fun getAllShops(): List<RentalShop> = shops

    fun getShopById(id: String): RentalShop? {
        val normalized = id.trim().lowercase()
        return shops.firstOrNull { it.id.equals(normalized, ignoreCase = true) }
            ?: shops.firstOrNull { it.id.contains(normalized, ignoreCase = true) || normalized.contains(it.id, ignoreCase = true) }
            ?: when {
                normalized.contains("mirissa") -> shops.find { it.id == "shop_mirissa_tuktuk" }
                normalized.contains("ella") -> shops.find { it.id == "shop_ella_moto" }
                normalized.contains("hikka") -> shops.find { it.id == "shop_hikkaduwa" }
                normalized.contains("kandy") && normalized.contains("bike") -> shops.find { it.id == "shop_kandy_bike" }
                normalized.contains("kandy") && normalized.contains("van") -> shops.find { it.id == "shop_kandy_van" }
                normalized.contains("colombo") && normalized.contains("scooter") -> shops.find { it.id == "shop_colombo_scooter" }
                normalized.contains("malkey") && normalized.contains("van") -> shops.find { it.id == "shop_malkey_van" }
                normalized.contains("malkey") -> shops.find { it.id == "shop_malkey_car" }
                normalized.contains("casons") && normalized.contains("van") -> shops.find { it.id == "shop_casons_van" }
                normalized.contains("casons") -> shops.find { it.id == "shop_casons_car" }
                normalized.contains("kings") -> shops.find { it.id == "shop_kings_car" }
                else -> null
            }
            ?: shops.firstOrNull()
    }
}
