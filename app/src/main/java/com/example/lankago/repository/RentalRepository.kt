package com.example.lankago.repository

import com.example.lankago.data.RentalVehicle
import com.example.lankago.data.Review

object RentalRepository {

    private val sampleRentals = listOf(
        RentalVehicle(
            id = "rental_scooter_ella",
            name = "Yamaha NMAX 155 Touring Scooter",
            type = "Scooter",
            location = "Ella, Badulla District",
            transmission = "Automatic",
            seatingCapacity = 2,
            fuelType = "Petrol",
            dailyRateLkr = 3800.0,
            depositLkr = 10000.0,
            rating = 4.9f,
            reviewCount = 124,
            isAvailable = true,
            vendorName = "Ella Mountain Bikes & Scooters",
            vendorPhone = "+94771234567",
            vendorWhatsapp = "94771234567",
            images = listOf(
                "https://images.unsplash.com/photo-1558981403-c5f9899a28bc?auto=format&fit=crop&w=800&q=80",
                "https://images.unsplash.com/photo-1568772585407-9361f9bf3a87?auto=format&fit=crop&w=800&q=80"
            ),
            reviews = listOf(
                Review("rev_1", "Sarah Jenkins", null, 5.0f, "12 Oct 2026", "Smooth engine for climbing Little Adam's Peak roads. Helmets were spotless."),
                Review("rev_2", "Kasun Perera", null, 4.8f, "05 Oct 2026", "Quick deposit return, very polite vendor in Ella town.")
            )
        ),
        RentalVehicle(
            id = "rental_tuktuk_mirissa",
            name = "Bajaj RE 4S Coastal Tuk-Tuk",
            type = "Tuk-Tuk",
            location = "Mirissa, Matara District",
            transmission = "Manual 4-Speed",
            seatingCapacity = 3,
            fuelType = "Petrol",
            dailyRateLkr = 5200.0,
            depositLkr = 15000.0,
            rating = 4.8f,
            reviewCount = 89,
            isAvailable = true,
            vendorName = "Ceylon TukTuk Rental Hub",
            vendorPhone = "+94719876543",
            vendorWhatsapp = "94719876543",
            images = listOf(
                "https://images.unsplash.com/photo-1544620347-c4fd4a3d5957?auto=format&fit=crop&w=800&q=80",
                "https://images.unsplash.com/photo-1588258524675-c61917b2bce2?auto=format&fit=crop&w=800&q=80"
            ),
            reviews = listOf(
                Review("rev_3", "Markus Weber", null, 5.0f, "18 Oct 2026", "Driving a tuk-tuk down the southern coast from Galle to Mirissa was the highlight of our vacation!"),
                Review("rev_4", "Dilani Silva", null, 4.5f, "01 Oct 2026", "Includes Sri Lanka temporary driving endorsement assistance.")
            )
        ),
        RentalVehicle(
            id = "rental_enfield_kandy",
            name = "Royal Enfield Himalayan 411cc",
            type = "Adventure Bike",
            location = "Kandy / Nuwara Eliya",
            transmission = "Manual 5-Speed",
            seatingCapacity = 2,
            fuelType = "Petrol",
            dailyRateLkr = 7500.0,
            depositLkr = 25000.0,
            rating = 4.9f,
            reviewCount = 64,
            isAvailable = false, // SOLD OUT DEMONSTRATION
            vendorName = "Highland Moto Adventures",
            vendorPhone = "+94812233445",
            vendorWhatsapp = "94812233445",
            images = listOf(
                "https://images.unsplash.com/photo-1558981806-ec527fa84c39?auto=format&fit=crop&w=800&q=80"
            ),
            reviews = listOf(
                Review("rev_5", "Alexandre Dumas", null, 5.0f, "28 Sep 2026", "Incredible torque for steep hairpins around Nuwara Eliya. High demand, book weeks ahead.")
            )
        ),
        RentalVehicle(
            id = "rental_prado_yala",
            name = "Toyota Land Cruiser Prado 4x4",
            type = "SUV / 4x4",
            location = "Tissamaharama, Yala",
            transmission = "Automatic",
            seatingCapacity = 7,
            fuelType = "Diesel",
            dailyRateLkr = 18000.0,
            depositLkr = 40000.0,
            rating = 4.9f,
            reviewCount = 142,
            isAvailable = true,
            vendorName = "Yala Safari Fleet Services",
            vendorPhone = "+94472211990",
            vendorWhatsapp = "94472211990",
            images = listOf(
                "https://images.unsplash.com/photo-1533473359331-0135ef1b58bf?auto=format&fit=crop&w=800&q=80"
            ),
            reviews = listOf(
                Review("rev_6", "David Chen", null, 5.0f, "14 Oct 2026", "High clearance made spotting leopards and elephants inside Yala Block 1 effortless.")
            )
        )
    )

    fun getAllRentals(): List<RentalVehicle> = sampleRentals

    fun getRentalById(id: String): RentalVehicle? {
        return sampleRentals.firstOrNull { it.id == id } ?: sampleRentals.firstOrNull()
    }

    fun getNearbyRentals(excludeId: String): List<RentalVehicle> {
        return sampleRentals.filter { it.id != excludeId }
    }
}
