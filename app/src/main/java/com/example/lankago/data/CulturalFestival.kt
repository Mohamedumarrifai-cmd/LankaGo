package com.example.lankago.data

/**
 * Schedule event or ritual occurring in the festival timeline.
 */
data class FestivalScheduleItem(
    val dayLabel: String,
    val time: String,
    val title: String,
    val description: String
)

/**
 * Data model for cultural and historical festivals across Sri Lanka.
 */
data class CulturalFestival(
    val id: String = "",
    val name: String = "",
    val category: String = "",
    val dates: String = "",
    val time: String = "",
    val locationName: String = "",
    val coordinates: String = "",
    val bannerRes: Int = 0,
    val imageUrl: String = "",
    val historicalBackground: String = "",
    val rating: Float = 4.8f,
    val reviewCount: Int = 100,
    val visitorTips: List<String> = emptyList(),
    val schedule: List<FestivalScheduleItem> = emptyList(),
    val isReminderSet: Boolean = false
)
