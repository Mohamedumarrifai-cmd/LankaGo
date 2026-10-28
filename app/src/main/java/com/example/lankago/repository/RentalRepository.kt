package com.example.lankago.repository

import com.example.lankago.data.RentalProvider
import com.example.lankago.data.RentalVehicle
import com.example.lankago.data.Review
import com.example.lankago.data.Vehicle

object RentalRepository {

    private const val RES_PREFIX = "android.resource://com.example.lankago/drawable/"

    private val providers = listOf(
        RentalProvider(
            id = "prov_ella_moto",
            name = "Ella Mountain Moto & Bike Rentals",
            phone = "+94771234567",
            whatsapp = "94771234567",
            websiteUrl = "https://www.lankago.lk/rentals/ella-moto",
            location = "Ella Town, Passara Road",
            rating = 4.9f,
            reviewCount = 142
        ),
        RentalProvider(
            id = "prov_ceylon_cabs",
            name = "Ceylon Self-Drive Cars & Tours",
            phone = "+94719876543",
            whatsapp = "94719876543",
            websiteUrl = "https://www.lankago.lk/rentals/ceylon-cars",
            location = "Colombo & Galle Hub",
            rating = 4.8f,
            reviewCount = 210
        ),
        RentalProvider(
            id = "prov_southern_tuktuk",
            name = "Southern Coast TukTuk Express",
            phone = "+94765554321",
            whatsapp = "94765554321",
            websiteUrl = "https://www.lankago.lk/rentals/southern-tuktuk",
            location = "Mirissa & Weligama",
            rating = 4.7f,
            reviewCount = 98
        )
    )

    private val sampleVehicles = listOf(
        // === BIKES & SCOOTERS (BIKE RENTALS: BIKE PICTURES & DETAILS ONLY) ===
        Vehicle(
            id = "rental_honda_pcx",
            name = "Honda PCX 150 Scooter",
            type = "Bike / Scooter",
            imageUrl = "${RES_PREFIX}pcx",
            images = listOf(
                "${RES_PREFIX}pcx",
                "https://images.unsplash.com/photo-1558981403-c5f9899a28bc?auto=format&fit=crop&w=800&q=80",
                "https://images.unsplash.com/photo-1568772585407-9361f9bf3a87?auto=format&fit=crop&w=800&q=80",
                "https://images.unsplash.com/photo-1558981408-db0ecd8a1ee4?auto=format&fit=crop&w=800&q=80",
                "https://images.unsplash.com/photo-1558981359-219d6364c9c8?auto=format&fit=crop&w=800&q=80",
                "https://images.unsplash.com/photo-1558980664-769d59546b3d?auto=format&fit=crop&w=800&q=80"
            ),
            pricePerDayLkr = 3800.0,
            depositLkr = 10000.0,
            rating = 4.9f,
            reviewCount = 124,
            isAvailable = true,
            seats = 2,
            transmission = "Automatic",
            fuelType = "Petrol",
            provider = providers[0],
            reviews = listOf(
                Review("rev_v1", "Sarah Jenkins", null, 5.0f, "12 Oct 2026", "Smooth engine for climbing Little Adam's Peak roads. Helmets were spotless."),
                Review("rev_v2", "Kasun Perera", null, 4.8f, "05 Oct 2026", "Quick deposit return, very polite staff at the Ella branch.")
            )
        ),
        Vehicle(
            id = "rental_yamaha_nmax",
            name = "Yamaha NMAX 155cc Maxi Scooter",
            type = "Bike / Scooter",
            imageUrl = "${RES_PREFIX}nmax",
            images = listOf(
                "${RES_PREFIX}nmax",
                "https://images.unsplash.com/photo-1568772585407-9361f9bf3a87?auto=format&fit=crop&w=800&q=80",
                "https://images.unsplash.com/photo-1558981403-c5f9899a28bc?auto=format&fit=crop&w=800&q=80",
                "https://images.unsplash.com/photo-1558981408-db0ecd8a1ee4?auto=format&fit=crop&w=800&q=80",
                "https://images.unsplash.com/photo-1558981359-219d6364c9c8?auto=format&fit=crop&w=800&q=80",
                "https://images.unsplash.com/photo-1558980664-769d59546b3d?auto=format&fit=crop&w=800&q=80"
            ),
            pricePerDayLkr = 4000.0,
            depositLkr = 12000.0,
            rating = 4.9f,
            reviewCount = 89,
            isAvailable = true,
            seats = 2,
            transmission = "Automatic",
            fuelType = "Petrol",
            provider = providers[0],
            reviews = listOf(
                Review("rev_nm1", "Nadeera Wijesinghe", null, 5.0f, "20 Oct 2026", "Dual disc brakes and large under-seat storage. Exceptional ride.")
            )
        ),
        Vehicle(
            id = "rental_honda_dio",
            name = "Honda Dio 110cc City Scooter",
            type = "Bike / Scooter",
            imageUrl = "${RES_PREFIX}dio",
            images = listOf(
                "${RES_PREFIX}dio",
                "https://images.unsplash.com/photo-1558981403-c5f9899a28bc?auto=format&fit=crop&w=800&q=80",
                "https://images.unsplash.com/photo-1568772585407-9361f9bf3a87?auto=format&fit=crop&w=800&q=80",
                "https://images.unsplash.com/photo-1558981408-db0ecd8a1ee4?auto=format&fit=crop&w=800&q=80",
                "https://images.unsplash.com/photo-1558981359-219d6364c9c8?auto=format&fit=crop&w=800&q=80",
                "https://images.unsplash.com/photo-1558980664-3a031cf67ea8?auto=format&fit=crop&w=800&q=80"
            ),
            pricePerDayLkr = 3200.0,
            depositLkr = 8000.0,
            rating = 4.7f,
            reviewCount = 95,
            isAvailable = true,
            seats = 2,
            transmission = "Automatic",
            fuelType = "Petrol",
            provider = providers[2],
            reviews = listOf(
                Review("rev_dio1", "Matthias Berg", null, 4.8f, "17 Oct 2026", "Lightweight, easy to maneuver in narrow beach lanes.")
            )
        ),
        Vehicle(
            id = "rental_enfield_kandy",
            name = "Royal Enfield Himalayan 411cc",
            type = "Adventure Bike",
            imageUrl = "${RES_PREFIX}royalenflied",
            images = listOf(
                "${RES_PREFIX}royalenflied",
                "https://images.unsplash.com/photo-1558981806-ec527fa84c39?auto=format&fit=crop&w=800&q=80",
                "https://images.unsplash.com/photo-1558981403-c5f9899a28bc?auto=format&fit=crop&w=800&q=80",
                "https://images.unsplash.com/photo-1558981852-426c6c22a06a?auto=format&fit=crop&w=800&q=80",
                "https://images.unsplash.com/photo-1558981420-87aa9210d993?auto=format&fit=crop&w=800&q=80",
                "https://images.unsplash.com/photo-1558981285-6f0c94958bb6?auto=format&fit=crop&w=800&q=80"
            ),
            pricePerDayLkr = 7500.0,
            depositLkr = 25000.0,
            rating = 4.9f,
            reviewCount = 64,
            isAvailable = true,
            seats = 2,
            transmission = "Manual 5-Speed",
            fuelType = "Petrol",
            provider = providers[0],
            reviews = listOf(
                Review("rev_v7", "Alexandre Dumas", null, 5.0f, "28 Sep 2026", "Incredible torque for steep hairpins around Nuwara Eliya. High demand, book ahead.")
            )
        ),
        Vehicle(
            id = "rental_yamaha_fzs",
            name = "Yamaha FZ-S 150cc Touring Bike",
            type = "Bike / Motorcycle",
            imageUrl = "${RES_PREFIX}fz",
            images = listOf(
                "${RES_PREFIX}fz",
                "https://images.unsplash.com/photo-1568772585407-9361f9bf3a87?auto=format&fit=crop&w=800&q=80",
                "https://images.unsplash.com/photo-1558981403-c5f9899a28bc?auto=format&fit=crop&w=800&q=80",
                "https://images.unsplash.com/photo-1558981408-db0ecd8a1ee4?auto=format&fit=crop&w=800&q=80",
                "https://images.unsplash.com/photo-1558981359-219d6364c9c8?auto=format&fit=crop&w=800&q=80",
                "https://images.unsplash.com/photo-1558980664-769d59546b3d?auto=format&fit=crop&w=800&q=80"
            ),
            pricePerDayLkr = 4500.0,
            depositLkr = 15000.0,
            rating = 4.8f,
            reviewCount = 48,
            isAvailable = true,
            seats = 2,
            transmission = "Manual 5-Speed",
            fuelType = "Petrol",
            provider = providers[0],
            reviews = listOf(
                Review("rev_v9", "Michael Chen", null, 5.0f, "19 Oct 2026", "Reliable suspension and great fuel economy across southern routes.")
            )
        ),
        Vehicle(
            id = "rental_pulsar_ns200",
            name = "Bajaj Pulsar NS200 Street Sport",
            type = "Bike / Motorcycle",
            imageUrl = "${RES_PREFIX}ns200",
            images = listOf(
                "${RES_PREFIX}ns200",
                "https://images.unsplash.com/photo-1568772585407-9361f9bf3a87?auto=format&fit=crop&w=800&q=80",
                "https://images.unsplash.com/photo-1558981403-c5f9899a28bc?auto=format&fit=crop&w=800&q=80",
                "https://images.unsplash.com/photo-1558981408-db0ecd8a1ee4?auto=format&fit=crop&w=800&q=80",
                "https://images.unsplash.com/photo-1558981359-219d6364c9c8?auto=format&fit=crop&w=800&q=80",
                "https://images.unsplash.com/photo-1558980664-769d59546b3d?auto=format&fit=crop&w=800&q=80"
            ),
            pricePerDayLkr = 4800.0,
            depositLkr = 16000.0,
            rating = 4.8f,
            reviewCount = 57,
            isAvailable = true,
            seats = 2,
            transmission = "Manual 6-Speed",
            fuelType = "Petrol",
            provider = providers[0],
            reviews = listOf(
                Review("rev_ns1", "Sanath Kulatunga", null, 5.0f, "16 Oct 2026", "Fast response, liquid cooled engine handled central highlands comfortably.")
            )
        ),

        // === VEHICLES (CARS, VANS, 4X4, TUK-TUKS: VEHICLE PICTURES & DETAILS) ===
        Vehicle(
            id = "rental_suzuki_wagonr",
            name = "Suzuki Wagon R FX Hybrid",
            type = "Car / Hatchback",
            imageUrl = "${RES_PREFIX}wagonr",
            images = listOf(
                "${RES_PREFIX}wagonr",
                "https://images.unsplash.com/photo-1552519507-da3b142c6e3d?auto=format&fit=crop&w=800&q=80",
                "https://images.unsplash.com/photo-1549399542-7e3f8b79c341?auto=format&fit=crop&w=800&q=80",
                "https://images.unsplash.com/photo-1503376780353-7e6692767b70?auto=format&fit=crop&w=800&q=80",
                "https://images.unsplash.com/photo-1541348263662-e0c8de4259ba?auto=format&fit=crop&w=800&q=80",
                "https://images.unsplash.com/photo-1486006920555-c77dce18193b?auto=format&fit=crop&w=800&q=80"
            ),
            pricePerDayLkr = 8000.0,
            depositLkr = 20000.0,
            rating = 4.8f,
            reviewCount = 43,
            isAvailable = true,
            seats = 4,
            transmission = "Automatic",
            fuelType = "Hybrid",
            provider = providers[1],
            reviews = listOf(
                Review("rev_v12", "Chaminda Vaas", null, 4.8f, "03 Oct 2026", "Compact, easy to park in crowded city centers, and super economical.")
            )
        ),
        Vehicle(
            id = "rental_suzuki_alto",
            name = "Suzuki Alto 800 Economy City Car",
            type = "Car / Hatchback",
            imageUrl = "${RES_PREFIX}alto",
            images = listOf(
                "${RES_PREFIX}alto",
                "https://images.unsplash.com/photo-1549399542-7e3f8b79c341?auto=format&fit=crop&w=800&q=80",
                "https://images.unsplash.com/photo-1552519507-da3b142c6e3d?auto=format&fit=crop&w=800&q=80",
                "https://images.unsplash.com/photo-1503376780353-7e6692767b70?auto=format&fit=crop&w=800&q=80",
                "https://images.unsplash.com/photo-1541348263662-e0c8de4259ba?auto=format&fit=crop&w=800&q=80",
                "https://images.unsplash.com/photo-1449965408869-eaa3f722e40d?auto=format&fit=crop&w=800&q=80"
            ),
            pricePerDayLkr = 6500.0,
            depositLkr = 15000.0,
            rating = 4.7f,
            reviewCount = 68,
            isAvailable = true,
            seats = 4,
            transmission = "Manual 5-Speed",
            fuelType = "Petrol",
            provider = providers[1],
            reviews = listOf(
                Review("rev_alt1", "Pradeep Kumara", null, 4.9f, "11 Oct 2026", "Extremely fuel-efficient budget rental, ideal for city hops.")
            )
        ),
        Vehicle(
            id = "rental_toyota_aqua",
            name = "Toyota Aqua Hybrid (Automatic)",
            type = "Car / Sedan",
            imageUrl = "${RES_PREFIX}aqua",
            images = listOf(
                "${RES_PREFIX}aqua",
                "https://images.unsplash.com/photo-1549399542-7e3f8b79c341?auto=format&fit=crop&w=800&q=80",
                "https://images.unsplash.com/photo-1552519507-da3b142c6e3d?auto=format&fit=crop&w=800&q=80",
                "https://images.unsplash.com/photo-1503376780353-7e6692767b70?auto=format&fit=crop&w=800&q=80",
                "https://images.unsplash.com/photo-1541348263662-e0c8de4259ba?auto=format&fit=crop&w=800&q=80",
                "https://images.unsplash.com/photo-1486006920555-c77dce18193b?auto=format&fit=crop&w=800&q=80"
            ),
            pricePerDayLkr = 9500.0,
            depositLkr = 25000.0,
            rating = 4.8f,
            reviewCount = 89,
            isAvailable = true,
            seats = 5,
            transmission = "Automatic",
            fuelType = "Hybrid",
            provider = providers[1],
            reviews = listOf(
                Review("rev_v3", "David Miller", null, 5.0f, "15 Oct 2026", "Outstanding fuel efficiency. Covered Colombo to Kandy on less than a quarter tank!"),
                Review("rev_v4", "Niroshan Fernando", null, 4.6f, "08 Oct 2026", "Clean AC and bluetooth sound system. Perfect family rental.")
            )
        ),
        Vehicle(
            id = "rental_toyota_prius",
            name = "Toyota Prius Luxury Hybrid",
            type = "Car / Sedan",
            imageUrl = "${RES_PREFIX}prius",
            images = listOf(
                "${RES_PREFIX}prius",
                "https://images.unsplash.com/photo-1549399542-7e3f8b79c341?auto=format&fit=crop&w=800&q=80",
                "https://images.unsplash.com/photo-1552519507-da3b142c6e3d?auto=format&fit=crop&w=800&q=80",
                "https://images.unsplash.com/photo-1503376780353-7e6692767b70?auto=format&fit=crop&w=800&q=80",
                "https://images.unsplash.com/photo-1541348263662-e0c8de4259ba?auto=format&fit=crop&w=800&q=80",
                "https://images.unsplash.com/photo-1449965408869-eaa3f722e40d?auto=format&fit=crop&w=800&q=80"
            ),
            pricePerDayLkr = 12000.0,
            depositLkr = 30000.0,
            rating = 4.9f,
            reviewCount = 74,
            isAvailable = true,
            seats = 5,
            transmission = "Automatic",
            fuelType = "Hybrid",
            provider = providers[1],
            reviews = listOf(
                Review("rev_pri1", "Liam O'Connor", null, 5.0f, "14 Oct 2026", "Whisper-quiet hybrid drive, premium comfort for long expressway cruises.")
            )
        ),
        Vehicle(
            id = "rental_honda_grace",
            name = "Honda Grace Hybrid Sedan",
            type = "Car / Sedan",
            imageUrl = "${RES_PREFIX}grace",
            images = listOf(
                "${RES_PREFIX}grace",
                "https://images.unsplash.com/photo-1549399542-7e3f8b79c341?auto=format&fit=crop&w=800&q=80",
                "https://images.unsplash.com/photo-1552519507-da3b142c6e3d?auto=format&fit=crop&w=800&q=80",
                "https://images.unsplash.com/photo-1503376780353-7e6692767b70?auto=format&fit=crop&w=800&q=80",
                "https://images.unsplash.com/photo-1541348263662-e0c8de4259ba?auto=format&fit=crop&w=800&q=80",
                "https://images.unsplash.com/photo-1486006920555-c77dce18193b?auto=format&fit=crop&w=800&q=80"
            ),
            pricePerDayLkr = 11500.0,
            depositLkr = 28000.0,
            rating = 4.9f,
            reviewCount = 63,
            isAvailable = true,
            seats = 5,
            transmission = "Dual Clutch Automatic",
            fuelType = "Hybrid",
            provider = providers[1],
            reviews = listOf(
                Review("rev_gr1", "Anushka Wickrama", null, 4.9f, "18 Oct 2026", "Sporty steering, plush interior, and excellent fuel economy.")
            )
        ),
        Vehicle(
            id = "rental_tuktuk_mirissa",
            name = "Sri Lankan Tourist Tuk-Tuk",
            type = "Tuk-Tuk",
            imageUrl = "${RES_PREFIX}tourist_tuktuk",
            images = listOf(
                "${RES_PREFIX}tourist_tuktuk",
                "https://images.unsplash.com/photo-1544620347-c4fd4a3d5957?auto=format&fit=crop&w=800&q=80",
                "https://images.unsplash.com/photo-1588258524675-c61917b2bce2?auto=format&fit=crop&w=800&q=80",
                "https://images.unsplash.com/photo-1549399542-7e3f8b79c341?auto=format&fit=crop&w=800&q=80",
                "https://images.unsplash.com/photo-1552519507-da3b142c6e3d?auto=format&fit=crop&w=800&q=80",
                "https://images.unsplash.com/photo-1503376780353-7e6692767b70?auto=format&fit=crop&w=800&q=80"
            ),
            pricePerDayLkr = 5200.0,
            depositLkr = 15000.0,
            rating = 4.8f,
            reviewCount = 98,
            isAvailable = true,
            seats = 3,
            transmission = "Manual 4-Speed",
            fuelType = "Petrol",
            provider = providers[2],
            reviews = listOf(
                Review("rev_v5", "Markus Weber", null, 5.0f, "18 Oct 2026", "Driving a tuk-tuk down the southern coast from Galle to Mirissa was the highlight of our vacation!"),
                Review("rev_v6", "Dilani Silva", null, 4.5f, "01 Oct 2026", "Includes Sri Lanka temporary driving license endorsement assistance.")
            )
        ),
        Vehicle(
            id = "rental_nissan_xtrail",
            name = "Nissan X-Trail 4WD Safari Edition",
            type = "SUV / 4x4",
            imageUrl = "${RES_PREFIX}xtrail",
            images = listOf(
                "${RES_PREFIX}xtrail",
                "https://images.unsplash.com/photo-1533473359331-0135ef1b58bf?auto=format&fit=crop&w=800&q=80",
                "https://images.unsplash.com/photo-1503376780353-7e6692767b70?auto=format&fit=crop&w=800&q=80",
                "https://images.unsplash.com/photo-1541348263662-e0c8de4259ba?auto=format&fit=crop&w=800&q=80",
                "https://images.unsplash.com/photo-1486006920555-c77dce18193b?auto=format&fit=crop&w=800&q=80",
                "https://images.unsplash.com/photo-1552519507-da3b142c6e3d?auto=format&fit=crop&w=800&q=80"
            ),
            pricePerDayLkr = 16000.0,
            depositLkr = 35000.0,
            rating = 4.9f,
            reviewCount = 52,
            isAvailable = true,
            seats = 7,
            transmission = "Tiptronic Automatic",
            fuelType = "Diesel",
            provider = providers[1],
            reviews = listOf(
                Review("rev_v8", "Hans Zimmer", null, 5.0f, "10 Oct 2026", "Robust 4x4. Handled Yala national park approach and knuckles ranges with ease.")
            )
        ),
        Vehicle(
            id = "rental_mitsubishi_montero",
            name = "Mitsubishi Montero 4x4 Expedition",
            type = "SUV / 4x4",
            imageUrl = "${RES_PREFIX}montero",
            images = listOf(
                "${RES_PREFIX}montero",
                "https://images.unsplash.com/photo-1533473359331-0135ef1b58bf?auto=format&fit=crop&w=800&q=80",
                "https://images.unsplash.com/photo-1503376780353-7e6692767b70?auto=format&fit=crop&w=800&q=80",
                "https://images.unsplash.com/photo-1541348263662-e0c8de4259ba?auto=format&fit=crop&w=800&q=80",
                "https://images.unsplash.com/photo-1486006920555-c77dce18193b?auto=format&fit=crop&w=800&q=80",
                "https://images.unsplash.com/photo-1552519507-da3b142c6e3d?auto=format&fit=crop&w=800&q=80"
            ),
            pricePerDayLkr = 19500.0,
            depositLkr = 40000.0,
            rating = 5.0f,
            reviewCount = 38,
            isAvailable = true,
            seats = 7,
            transmission = "Automatic 4WD",
            fuelType = "Diesel",
            provider = providers[1],
            reviews = listOf(
                Review("rev_mon1", "Gregor Samsa", null, 5.0f, "15 Oct 2026", "True off-road beast. Handled river crossings and muddy tracks effortlessly.")
            )
        ),
        Vehicle(
            id = "rental_toyota_kdh",
            name = "Toyota KDH Commuter High-Roof Van",
            type = "Van / Minibus",
            imageUrl = "${RES_PREFIX}kdh",
            images = listOf(
                "${RES_PREFIX}kdh",
                "https://images.unsplash.com/photo-1549399542-7e3f8b79c341?auto=format&fit=crop&w=800&q=80",
                "https://images.unsplash.com/photo-1541348263662-e0c8de4259ba?auto=format&fit=crop&w=800&q=80",
                "https://images.unsplash.com/photo-1503376780353-7e6692767b70?auto=format&fit=crop&w=800&q=80",
                "https://images.unsplash.com/photo-1552519507-da3b142c6e3d?auto=format&fit=crop&w=800&q=80",
                "https://images.unsplash.com/photo-1486006920555-c77dce18193b?auto=format&fit=crop&w=800&q=80"
            ),
            pricePerDayLkr = 18500.0,
            depositLkr = 35000.0,
            rating = 4.9f,
            reviewCount = 61,
            isAvailable = true,
            seats = 14,
            transmission = "Manual 5-Speed",
            fuelType = "Diesel",
            provider = providers[1],
            reviews = listOf(
                Review("rev_v11", "Rohan Jayawardene", null, 5.0f, "14 Oct 2026", "Spacious family van with dual AC. Ideal for 10-14 passengers.")
            )
        ),
        Vehicle(
            id = "rental_tuktuk_galle",
            name = "Galle Heritage Coastal Tuk-Tuk",
            type = "Tuk-Tuk",
            imageUrl = "${RES_PREFIX}galle_tuktuk",
            images = listOf(
                "${RES_PREFIX}galle_tuktuk",
                "${RES_PREFIX}tourist_tuktuk"
            ),
            pricePerDayLkr = 5200.0,
            depositLkr = 15000.0,
            rating = 4.9f,
            reviewCount = 84,
            isAvailable = true,
            seats = 3,
            transmission = "Manual 4-Speed",
            fuelType = "Petrol",
            provider = providers[2],
            reviews = listOf(
                Review("rev_gtk1", "Oliver Twist", null, 5.0f, "19 Oct 2026", "Exploring Galle Fort and coastal viewpoints with this authentic tuk-tuk was magical.")
            )
        )
    )

    // Legacy RentalVehicle mapping for backwards-compatibility
    private val sampleRentals: List<RentalVehicle> by lazy {
        sampleVehicles.map { v ->
            RentalVehicle(
                id = v.id,
                name = v.name,
                type = v.type,
                location = v.provider.location,
                transmission = v.transmission,
                seatingCapacity = v.seats,
                fuelType = v.fuelType,
                dailyRateLkr = v.pricePerDayLkr,
                depositLkr = v.depositLkr,
                rating = v.rating,
                reviewCount = v.reviewCount,
                isAvailable = v.isAvailable,
                vendorName = v.provider.name,
                vendorPhone = v.provider.phone,
                vendorWhatsapp = v.provider.whatsapp,
                images = v.images,
                reviews = v.reviews
            )
        }
    }

    fun getAllVehicles(): List<Vehicle> = sampleVehicles

    fun getVehiclesByLocation(locationQuery: String): List<Vehicle> {
        if (locationQuery.isBlank()) return sampleVehicles
        val filtered = sampleVehicles.filter {
            it.provider.location.contains(locationQuery, ignoreCase = true) ||
            it.name.contains(locationQuery, ignoreCase = true) ||
            it.type.contains(locationQuery, ignoreCase = true)
        }
        return if (filtered.isNotEmpty()) filtered else sampleVehicles
    }

    fun getVehicleById(id: String): Vehicle? {
        return sampleVehicles.find { it.id.equals(id, ignoreCase = true) }
            ?: sampleVehicles.firstOrNull()
    }

    fun getAllRentals(): List<RentalVehicle> = sampleRentals

    fun getRentalById(id: String): RentalVehicle? {
        return sampleRentals.find { it.id.equals(id, ignoreCase = true) }
            ?: sampleRentals.firstOrNull()
    }

    fun getNearbyRentals(excludeId: String): List<RentalVehicle> {
        return sampleRentals.filter { it.id != excludeId }
    }

    fun getVehiclesForShop(
        shopId: String?,
        shopCategory: String?,
        location: String?
    ): List<Vehicle> {
        val sId = shopId?.lowercase() ?: ""
        val cat = shopCategory?.lowercase() ?: ""

        val fleet = when {
            // Bike & Scooter Shops: Show bikes only (Dio, PCX, NMAX, FZ-S, Pulsar NS200, Royal Enfield)
            sId.contains("epic_ride") || sId.contains("colombo_scooter") ||
            sId.contains("unawatuna") || sId.contains("dambulla") ||
            sId.contains("hikkaduwa") || sId.contains("kandy_bike") ||
            sId.contains("ella_bike") || sId.contains("arugam") ||
            cat == "scooter" || cat == "bike" || cat.contains("scooter") || cat.contains("bike") || cat.contains("moto") -> {
                sampleVehicles.filter {
                    it.type.contains("Bike", ignoreCase = true) ||
                    it.type.contains("Scooter", ignoreCase = true) ||
                    it.type.contains("Motorcycle", ignoreCase = true)
                }
            }

            // Car Rental Shops: Show cars only (Alto, Wagon R, Aqua, Prius, Grace, X-Trail, Montero)
            sId.contains("malkey_car") || sId.contains("casons_car") ||
            sId.contains("kings_car") || sId.contains("euro_car") ||
            cat == "car" || cat.contains("car") || cat.contains("sedan") || cat.contains("suv") -> {
                sampleVehicles.filter {
                    it.type.contains("Car", ignoreCase = true) ||
                    it.type.contains("Sedan", ignoreCase = true) ||
                    it.type.contains("Hatchback", ignoreCase = true) ||
                    it.type.contains("SUV", ignoreCase = true) ||
                    it.type.contains("4x4", ignoreCase = true)
                }
            }

            // Van & Coach Shops: Show vans & large passenger transport
            sId.contains("van") || sId.contains("bus") || cat == "van" || cat == "bus" || cat.contains("van") || cat.contains("bus") || cat.contains("coach") -> {
                sampleVehicles.filter {
                    it.type.contains("Van", ignoreCase = true) ||
                    it.name.contains("KDH", ignoreCase = true) ||
                    it.type.contains("SUV", ignoreCase = true) ||
                    it.type.contains("4x4", ignoreCase = true)
                }
            }

            // Tuk-Tuk Shops: Show tuk-tuks
            (sId.contains("tuktuk") && !sId.contains("colombo_tuktuk") && !sId.contains("mirissa")) || cat == "tuktuk" || cat.contains("tuk") -> {
                sampleVehicles.filter {
                    it.type.contains("Tuk", ignoreCase = true)
                }
            }

            // Combined Shops (Bikes + Tuk-Tuks or Cars + Tuk-Tuks): Show both!
            sId.contains("mirissa") -> {
                sampleVehicles.filter {
                    it.type.contains("Bike", ignoreCase = true) ||
                    it.type.contains("Scooter", ignoreCase = true) ||
                    it.type.contains("Tuk", ignoreCase = true)
                }
            }
            sId.contains("colombo_tuktuk") -> {
                sampleVehicles.filter {
                    it.type.contains("Tuk", ignoreCase = true) ||
                    it.name.contains("Alto", ignoreCase = true) ||
                    it.name.contains("Wagon", ignoreCase = true) ||
                    it.name.contains("Dio", ignoreCase = true)
                }
            }

            !location.isNullOrBlank() -> getVehiclesByLocation(location)
            else -> sampleVehicles
        }

        return if (fleet.isNotEmpty()) fleet else sampleVehicles
    }
}
