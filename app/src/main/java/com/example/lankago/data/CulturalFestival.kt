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
    val id: String,
    val name: String,
    val category: String,
    val dates: String,
    val time: String,
    val locationName: String,
    val coordinates: String,
    val bannerRes: Int,
    val historicalBackground: String,
    val rating: Float,
    val reviewCount: Int,
    val visitorTips: List<String>,
    val schedule: List<FestivalScheduleItem>,
    val isReminderSet: Boolean = false
)
