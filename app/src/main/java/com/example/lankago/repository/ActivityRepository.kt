package com.example.lankago.repository

import com.example.lankago.data.ActivityItem
import com.example.lankago.data.Review

object ActivityRepository {

    private val sampleActivities = listOf(
        ActivityItem(
            id = "act_sigiriya_hike",
            title = "Sigiriya Sunrise Hike & Ancient Frescoes Guided Tour",
            category = "Heritage & Archeology",
            location = "Sigiriya, Central Province",
            address = "Sigiriya Ancient Citadel Road, Dambulla 21120",
            durationHours = 3.5,
            basePriceLkr = 8500.0,
            serviceFeeLkr = 800.0,
            rating = 4.9f,
            reviewCount = 210,
            isAvailable = true,
            imageUrl = "https://images.unsplash.com/photo-1586861635167-e5223aadc9fe?auto=format&fit=crop&w=800&q=80",
            description = "Ascend the iconic 200m granite monolith of King Kashyapa before the midday tropical heat. Discover 5th-century mirror wall verses, royal terraced gardens, and summit palace foundations with an English-certified heritage guide.",
            includedItems = listOf(
                "Archeological Site Entry Ticket included",
                "Certified Archeological Tour Guide",
                "Mineral Water & Herbal King Coconut",
                "Frescoes conservation viewing access"
            ),
            reviews = listOf(
                Review("rev_a1", "Elena Rostova", null, 5.0f, "15 Oct 2026", "Guide was extremely knowledgeable on 5th century engineering. Sunrise view was unreal!"),
                Review("rev_a2", "Chaminda Wickramasinghe", null, 4.8f, "08 Oct 2026", "Well organized ticketing. Avoided the big tourist crowd.")
            )
        ),
        ActivityItem(
            id = "act_mirissa_whales",
            title = "Mirissa Blue Whale & Dolphin Watching Cruise",
            category = "Ocean & Marine",
            location = "Mirissa Harbor, Southern Province",
            address = "Harbor Road, Mirissa 81740",
            durationHours = 4.0,
            basePriceLkr = 11000.0,
            serviceFeeLkr = 1000.0,
            rating = 4.8f,
            reviewCount = 175,
            isAvailable = true,
            imageUrl = "https://images.unsplash.com/photo-1544551763-46a013bb70d5?auto=format&fit=crop&w=800&q=80",
            description = "Board a Coast Guard certified double-decker passenger catamaran from Mirissa Harbor into the Indian Ocean migratory trench. Spot majestic Blue Whales, Bryde's Whales, and pods of Spinner Dolphins.",
            includedItems = listOf(
                "Catamaran Cruise Ticket & Life Jacket",
                "Breakfast Pack & Fresh Ceylon Fruits on board",
                "Naturalist Marine Guide narration",
                "Motion sickness pills provided if needed"
            ),
            reviews = listOf(
                Review("rev_a3", "Liam O'Connor", null, 5.0f, "10 Oct 2026", "We saw 3 blue whales breaching within 50 meters! The crew respects safe marine distance."),
                Review("rev_a4", "Pooja Sharma", null, 4.5f, "02 Oct 2026", "Comfortable boat with clean washrooms and polite crew.")
            )
        ),
        ActivityItem(
            id = "act_yala_safari",
            title = "Yala National Park Full-Day Leopard & Wildlife Safari",
            category = "Wildlife Safari",
            location = "Yala / Tissamaharama",
            address = "Palatupana Park Gate, Kirinda Road",
            durationHours = 7.0,
            basePriceLkr = 19500.0,
            serviceFeeLkr = 1500.0,
            rating = 4.9f,
            reviewCount = 280,
            isAvailable = true,
            imageUrl = "https://images.unsplash.com/photo-1534177616072-ef7dc120449d?auto=format&fit=crop&w=800&q=80",
            description = "Explore Block 1 and Block 2 of Yala National Park in a specialized safari jeep with an expert wildlife tracker. High likelihood of spotting Sri Lankan leopards, sloth bears, wild elephants, and spotted deer.",
            includedItems = listOf(
                "Department of Wildlife Conservation Entrance Fee",
                "Modified 4x4 Safari Cruiser with Elevated Seats",
                "Professional Park Tracker & Driver",
                "Picnic Lunch at Patanangala Beach inside park"
            ),
            reviews = listOf(
                Review("rev_a5", "Tobias Meyer", null, 5.0f, "19 Oct 2026", "Spotted two leopards resting on rocky outcrops near Sithulpawwa. Outstanding experience.")
            )
        )
    )

    fun getAllActivities(): List<ActivityItem> = sampleActivities

    fun getActivityById(id: String): ActivityItem? {
        return sampleActivities.firstOrNull { it.id == id } ?: sampleActivities.firstOrNull()
    }
}
