package com.example.lankago.repository

import com.example.lankago.R
import com.example.lankago.data.CulturalFestival
import com.example.lankago.data.FestivalScheduleItem

object CulturalFestivalRepository {

    private val festivals = listOf(
        CulturalFestival(
            id = "fest_kandy_perahera",
            name = "Kandy Esala Perahera (The Festival of the Tooth)",
            category = "Sacred Buddhist Pageant",
            dates = "August 15 - August 25, 2026",
            time = "07:00 PM onwards nightly",
            locationName = "Temple of the Sacred Tooth Relic (Sri Dalada Maligawa), Kandy",
            coordinates = "7.2936° N, 80.6413° E",
            bannerRes = R.drawable.kandy_esala,
            historicalBackground = "The Kandy Esala Perahera is one of the oldest and grandest Buddhist festivals in the world, dating back to the 4th century AD when the sacred Tooth Relic of Gautama Buddha was brought to Sri Lanka from Kalinga, India, by Princess Hemamali and Prince Danta.\n\nHistorically, the pageant was conducted to invoke the blessings of the heavens for timely seasonal rains, agricultural fertility, and national peace. Today, the procession brings together the Temple of the Tooth Relic alongside the four guardian Devales (Natha, Vishnu, Kataragama, and Pattini). Over 100 sumptuously caparisoned elephants, thousands of drummers beating traditional Geta Beraya drums, acrobatic fire-spinners, whip-crackers, and resplendent Kandyan Ves dancers parade along illuminated streets in an awe-inspiring spectacle of faith and heritage.",
            rating = 4.9f,
            reviewCount = 520,
            visitorTips = listOf(
                "Dress in modest white or light-colored attire covering shoulders and knees",
                "Arrive along Queens Hotel or Dalada Veediya by 04:30 PM to secure prime viewing spots",
                "Photography without flash is welcomed; please stand when the sacred casket tusker passes",
                "Keep a water bottle and rain poncho handy for occasional cool evening hill country drizzles"
            ),
            schedule = listOf(
                FestivalScheduleItem(
                    dayLabel = "Day 1 - Auspicious Beginning",
                    time = "06:45 AM",
                    title = "Kap Situveema Ritual",
                    description = "Ceremonial planting of consecrated young jackfruit tree saplings at all four Devales to bless the festival."
                ),
                FestivalScheduleItem(
                    dayLabel = "Days 2 to 5 - Early Processions",
                    time = "07:00 PM - 09:30 PM",
                    title = "Kumbal Perahera",
                    description = "First five nights featuring inner courtyard devale processions with sacred flags, drums, and young elephants."
                ),
                FestivalScheduleItem(
                    dayLabel = "Days 6 to 9 - Grand Pageants",
                    time = "07:30 PM - 11:00 PM",
                    title = "Randoli Perahera",
                    description = "Magnificent nocturnal processions accompanied by royal palanquins (Randoli) carried by traditional custodians."
                ),
                FestivalScheduleItem(
                    dayLabel = "Day 10 - Grand Nikini Full Moon",
                    time = "08:00 PM - Midnight",
                    title = "Maha Randoli Final Night",
                    description = "The peak of the festival where the regal Raja Tusker carries the golden replica of the Sacred Tooth Relic casket."
                ),
                FestivalScheduleItem(
                    dayLabel = "Day 11 - Dawn Finale",
                    time = "06:00 AM",
                    title = "Diya Kepeema (Water Cutting Ceremony)",
                    description = "Sacred dawn water cutting ritual at the Mahaweli River in Getambe, followed by the concluding Day Perahera."
                )
            )
        ),
        CulturalFestival(
            id = "fest_vesak",
            name = "Vesak Poya Lantern & Light Festival",
            category = "National Buddhist Holiday",
            dates = "May 20 - May 24, 2026",
            time = "06:00 PM - Midnight",
            locationName = "Gangarama Temple, Beira Lake & Island-wide",
            coordinates = "6.9167° N, 79.8569° E",
            bannerRes = R.drawable.literacy_festival,
            historicalBackground = "Vesak commemorates the three most monumental events in the life of Gautama Buddha: His Birth, His Supreme Enlightenment beneath the Bodhi tree in Bodh Gaya, and His final Parinirvana. Across Sri Lanka, cities and quiet hamlets transform into radiant seas of light.\n\nDevotees handcraft geometric octagonal lanterns (Vesak Kudu) wrapped in translucent tissue paper, illuminated by gentle candles. Colossal illuminated electrical pandals (Thorana) depicting scenes from the 550 Jataka tales soar dozens of meters high. Free community food and drink stalls known as 'Dansalas' line roads, exemplifying the Buddhist virtues of unconditional generosity (Dana) and kindness.",
            rating = 4.8f,
            reviewCount = 380,
            visitorTips = listOf(
                "Stroll along Colombo's Beira Lake Vesak Kalapaya on foot to admire floating illuminated displays",
                "Politely accept tea, coffee, and ice cream treats offered at roadside Dansala stalls",
                "Temples offer serene silent meditation hours in the mornings before evening illuminations"
            ),
            schedule = listOf(
                FestivalScheduleItem(
                    dayLabel = "Day 1 - Vesak Full Moon Day",
                    time = "06:00 AM - 05:00 PM",
                    title = "Sil Observance & Spiritual Meditation",
                    description = "Hundreds of thousands of white-clad devotees observe the Eight Precepts at Buddhist temples nationwide."
                ),
                FestivalScheduleItem(
                    dayLabel = "Day 1 - Evening",
                    time = "06:30 PM",
                    title = "Lighting of Vesak Lanterns & Pandals",
                    description = "Simultaneous switching on of millions of lanterns, clay oil lamps, and illuminated animated electrical Thorana."
                ),
                FestivalScheduleItem(
                    dayLabel = "Days 2 to 4 - Festival Week",
                    time = "07:00 PM - 11:30 PM",
                    title = "Vesak Kalapaya & Dansala Hospitality",
                    description = "Walking street carnivals featuring devotional songs (Bhakthi Geetha), floating exhibitions, and community feasts."
                )
            )
        ),
        CulturalFestival(
            id = "fest_sinhala_tamil_new_year",
            name = "Sinhala & Tamil Traditional New Year (Aluth Avurudda)",
            category = "Solar Cultural Celebration",
            dates = "April 13 - April 15, 2026",
            time = "All Day Auspicious Timings",
            locationName = "Island-wide Countryside Villages & Heritage Centers",
            coordinates = "7.8731° N, 80.7718° E",
            bannerRes = R.drawable.product,
            historicalBackground = "Aluth Avurudda marks the transition of the Sun god from Meena Rashiya (Pisces) into Mesha Rashiya (Aries), ending the Maha harvest season. It is celebrated harmoniously by both the Sinhalese and Tamil communities.\n\nEvery ritual is performed simultaneously across the nation at astrologically determined auspicious moments (Nekath). From lighting the hearth to boil fresh milk in a new clay pot, to the first meal of traditional Kavum and Kokis, to greeting elders with sheaves of betel leaves, Avurudda renews kinship, communal harmony, and gratitude to nature.",
            rating = 4.9f,
            reviewCount = 295,
            visitorTips = listOf(
                "Wear the auspicious color of the year (usually cream, white, or green) during Nekath hours",
                "Participate in village pillow fights (Kotta Pora) and tug-of-war (Kaba Adeema)",
                "Taste freshly fried sweet honey oil cakes (Kevum) and crispy honeycomb Kokis"
            ),
            schedule = listOf(
                FestivalScheduleItem(
                    dayLabel = "April 13 - Neutral Transition",
                    time = "02:15 PM - 09:00 PM",
                    title = "Punya Kalaya (Nonagathaya)",
                    description = "Suspension of worldly chores and cooking; devotion to temple visits and spiritual contemplation."
                ),
                FestivalScheduleItem(
                    dayLabel = "April 14 - Dawn of New Year",
                    time = "06:17 AM",
                    title = "Lighting the Hearth & Boiling Milk",
                    description = "Auspicious lighting of hearth; boiling fresh milk in a new clay pot until it overflows to symbolize abundance."
                ),
                FestivalScheduleItem(
                    dayLabel = "April 14 - Family Feasting",
                    time = "07:05 AM",
                    title = "First Meal & Ahara Anubhava",
                    description = "Partaking in Kiribath (milk rice), sweetmeats, and exchange of betel leaves to ask forgiveness from parents."
                ),
                FestivalScheduleItem(
                    dayLabel = "April 15 - Village Games",
                    time = "10:00 AM - 05:00 PM",
                    title = "Avurudu Ulela Traditional Folk Games",
                    description = "Rhythmic raban drumming contests, climbing the greasy pole (Greesa Gaha), and choosing the Avurudu Kumara."
                )
            )
        ),
        CulturalFestival(
            id = "fest_vel_festival",
            name = "Vel Festival & Sacred Chariot Procession",
            category = "Hindu Religious Festival",
            dates = "July 28 - July 30, 2026",
            time = "08:00 AM - 10:00 PM",
            locationName = "Pettah Kathiresan Kovil to Bambalapitiya, Colombo",
            coordinates = "6.9497° N, 79.8587° E",
            bannerRes = R.drawable.vel_festival,
            historicalBackground = "The Colombo Vel Festival venerates Lord Murugan (Skanda), the Hindu deity of victory and wisdom. The centerpiece of the celebration is the ornate, towering silver chariot adorned with fresh flower garlands, carrying the sacred golden spear (Vel).\n\nThe chariot is drawn through the bustling commercial boulevards of Colombo by hundreds of barefoot devotees. Reverberating Nadaswaram woodwind melodies and rhythmic Thavil drums accompany ecstatic Kavadi dancers balancing peacock-feathered arches on their shoulders.",
            rating = 4.7f,
            reviewCount = 210,
            visitorTips = listOf(
                "Remove footwear and maintain silence when entering Hindu Kovil inner sanctums",
                "Try fresh warm Laddu and Murukku distributed during chariot procession stops",
                "Watch the breathtaking Kavadi balancing dancers performing along Galle Road"
            ),
            schedule = listOf(
                FestivalScheduleItem(
                    dayLabel = "Day 1 - Morning Departure",
                    time = "08:30 AM",
                    title = "Chariot Rollout from Pettah Kovil",
                    description = "Silver chariot departure from Kathiresan Kovil amidst ceremonial conch shells and sacred chants."
                ),
                FestivalScheduleItem(
                    dayLabel = "Day 2 - Main Day",
                    time = "10:00 AM - 08:00 PM",
                    title = "Kavadi Attam & Coconut Smashing",
                    description = "Devotees perform vow fulfillments by smashing coconuts on stone altars to ward off negative energies."
                ),
                FestivalScheduleItem(
                    dayLabel = "Day 3 - Return Journey",
                    time = "06:00 PM - Midnight",
                    title = "Return of the Sacred Vel",
                    description = "Illuminated nighttime return journey of the sacred chariot with fireworks and spiritual blessings."
                )
            )
        )
    )

    private const val PREFS_NAME = "lankago_festivals_pref"
    private const val KEY_FESTIVALS_JSON = "saved_festivals_json"
    private val gson = com.google.gson.Gson()

    // In-memory cache
    private val cachedFestivals = ArrayList<CulturalFestival>()

    init {
        cachedFestivals.addAll(festivals)
    }

    fun getFestivals(context: android.content.Context): List<CulturalFestival> {
        val prefs = context.getSharedPreferences(PREFS_NAME, android.content.Context.MODE_PRIVATE)
        val json = prefs.getString(KEY_FESTIVALS_JSON, null)
        if (!json.isNullOrEmpty()) {
            try {
                val type = object : com.google.gson.reflect.TypeToken<ArrayList<CulturalFestival>>() {}.type
                val savedList: ArrayList<CulturalFestival>? = gson.fromJson(json, type)
                if (!savedList.isNullOrEmpty()) {
                    cachedFestivals.clear()
                    cachedFestivals.addAll(savedList)
                    return savedList
                }
            } catch (_: Exception) {}
        }
        // Save initial defaults
        saveToPrefs(context, festivals)
        cachedFestivals.clear()
        cachedFestivals.addAll(festivals)
        return festivals
    }

    fun addFestival(context: android.content.Context, festival: CulturalFestival) {
        val current = getFestivals(context).toMutableList()
        current.add(0, festival)
        saveToPrefs(context, current)
        cachedFestivals.clear()
        cachedFestivals.addAll(current)
    }

    fun updateFestival(context: android.content.Context, festival: CulturalFestival) {
        val current = getFestivals(context).toMutableList()
        val index = current.indexOfFirst { it.id == festival.id }
        if (index != -1) {
            current[index] = festival
            saveToPrefs(context, current)
            cachedFestivals.clear()
            cachedFestivals.addAll(current)
        }
    }

    fun deleteFestival(context: android.content.Context, festivalId: String) {
        val current = getFestivals(context).toMutableList()
        val removed = current.removeAll { it.id == festivalId }
        if (removed) {
            saveToPrefs(context, current)
            cachedFestivals.clear()
            cachedFestivals.addAll(current)
        }
    }

    fun getFestivalById(context: android.content.Context, id: String): CulturalFestival? {
        val all = getFestivals(context)
        return all.find { it.id.equals(id, ignoreCase = true) } ?: all.firstOrNull()
    }

    fun getAllFestivals(): List<CulturalFestival> {
        return if (cachedFestivals.isNotEmpty()) cachedFestivals else festivals
    }

    fun getFestivalById(id: String): CulturalFestival? {
        val list = getAllFestivals()
        return list.find { it.id.equals(id, ignoreCase = true) } ?: list.firstOrNull()
    }

    private fun saveToPrefs(context: android.content.Context, list: List<CulturalFestival>) {
        val prefs = context.getSharedPreferences(PREFS_NAME, android.content.Context.MODE_PRIVATE)
        val json = gson.toJson(list)
        prefs.edit().putString(KEY_FESTIVALS_JSON, json).apply()
    }
}
