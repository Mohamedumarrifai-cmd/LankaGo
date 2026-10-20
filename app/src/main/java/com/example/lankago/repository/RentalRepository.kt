package com.example.lankago.repository

import com.example.lankago.data.RentalProvider
import com.example.lankago.data.RentalVehicle
import com.example.lankago.data.Review
import com.example.lankago.data.Vehicle

object RentalRepository {

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
        Vehicle(
            id = "rental_honda_pcx",
            name = "Honda PCX 150 Scooter",
            type = "Bike / Scooter",
            imageUrl = "https://images.unsplash.com/photo-1558981403-c5f9899a28bc?auto=format&fit=crop&w=800&q=80",
            images = listOf(
                "https://images.unsplash.com/photo-1558981403-c5f9899a28bc?auto=format&fit=crop&w=800&q=80",
                "https://images.unsplash.com/photo-1568772585407-9361f9bf3a87?auto=format&fit=crop&w=800&q=80"
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
            id = "rental_toyota_aqua",
            name = "Toyota Aqua Hybrid (Automatic)",
            type = "Car / Sedan",
            imageUrl = "https://images.unsplash.com/photo-1549399542-7e3f8b79c341?auto=format&fit=crop&w=800&q=80",
            images = listOf(
                "https://images.unsplash.com/photo-1549399542-7e3f8b79c341?auto=format&fit=crop&w=800&q=80",
                "https://images.unsplash.com/photo-1552519507-da3b142c6e3d?auto=format&fit=crop&w=800&q=80"
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
            id = "rental_tuktuk_mirissa",
            name = "Bajaj RE 4S Coastal Tuk-Tuk",
            type = "Tuk-Tuk",
            imageUrl = "https://images.unsplash.com/photo-1544620347-c4fd4a3d5957?auto=format&fit=crop&w=800&q=80",
            images = listOf(
                "https://images.unsplash.com/photo-1544620347-c4fd4a3d5957?auto=format&fit=crop&w=800&q=80",
                "https://images.unsplash.com/photo-1588258524675-c61917b2bce2?auto=format&fit=crop&w=800&q=80"
            ),
            pricePerDayLkr = 5200.0,
            depositLkr = 15000.0,
            rating = 4.7f,
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
            id = "rental_enfield_kandy",
            name = "Royal Enfield Himalayan 411cc",
            type = "Adventure Bike",
            imageUrl = "https://images.unsplash.com/photo-1558981806-ec527fa84c39?auto=format&fit=crop&w=800&q=80",
            images = listOf(
                "https://images.unsplash.com/photo-1558981806-ec527fa84c39?auto=format&fit=crop&w=800&q=80"
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
            id = "rental_nissan_xtrail",
            name = "Nissan X-Trail 4WD Safari Edition",
            type = "SUV / 4x4",
            imageUrl = "https://images.unsplash.com/photo-1533473359331-0135ef1b58bf?auto=format&fit=crop&w=800&q=80",
            images = listOf(
                "https://images.unsplash.com/photo-1533473359331-0135ef1b58bf?auto=format&fit=crop&w=800&q=80"
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
}
