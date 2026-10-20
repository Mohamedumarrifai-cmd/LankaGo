package com.example.lankago.data

import com.example.lankago.R

/**
 * Data model representing local Sri Lankan heritage products, vibrant artisan markets,
 * or indigenous traditional medicine (Hela Wedakama / Ayurveda).
 */
data class LocalItem(
    val id: String,
    val title: String,
    val category: String, // "Local Products", "Markets", "Traditional Medicine"
    val shortDescription: String,
    val importance: String, // Cultural and historical significance ("Why it is important?")
    val marketLocation: String, // Precise location details and where to buy
    val address: String,
    val originRegion: String,
    val priceIndication: String,
    val rating: Float = 4.9f,
    val reviewCount: Int = 85,
    val imageUrl: String = "",
    val imageRes: Int = 0,
    val latitude: Double = 6.9271,
    val longitude: Double = 79.8612,
    val isFeatured: Boolean = false,
    val tags: List<String> = emptyList()
)

/**
 * Realistic Mock Data Provider for Sri Lanka's Local Culture, Markets and Traditional Medicine.
 */
object LocalItemDataProvider {

    val items: List<LocalItem> = listOf(
        // CATEGORY: Local Products
        LocalItem(
            id = "item_ceylon_tea",
            title = "Pure Ceylon Single-Estate Black Tea & Silver Tips",
            category = "Local Products",
            shortDescription = "Handpicked high-altitude orthodox black tea celebrated globally for delicate golden liquor, aroma, and natural antioxidants.",
            importance = "Ceylon Tea remains one of Sri Lanka's greatest pride and cultural emblems. Introduced in 1867 by James Taylor at Loolecondera Estate, the unique climate of Sri Lanka's misty central highlands produces distinctive polyphenols and flavor profiles that cannot be replicated anywhere else on earth. Drinking Ceylon tea is woven into every household's daily rhythm and represents genuine Sri Lankan hospitality.",
            marketLocation = "Available at heritage tea factory boutique outlets in Nuwara Eliya, Mlesna Tea Fortress (Kiribathkumbura), and the Ceylon Tea Museum sales counter in Hantana.",
            address = "Hantana Estate, Kandy / Pedro Tea Estate, Nuwara Eliya",
            originRegion = "Nuwara Eliya & Central Highlands",
            priceIndication = "LKR 1,500 – 6,500 (approx. $5 – $22)",
            rating = 4.95f,
            reviewCount = 340,
            imageRes = R.drawable.ceylon_tea_set,
            latitude = 6.9497,
            longitude = 80.7891,
            isFeatured = true,
            tags = listOf("Ceylon Tea", "Highland Harvest", "Single Estate", "Fair Trade")
        ),
        LocalItem(
            id = "item_ceylon_cinnamon",
            title = "True Ceylon Alba Cinnamon (Cinnamomum Verum)",
            category = "Local Products",
            shortDescription = "Ultra-premium quills of genuine sweet cinnamon cultivated and peeled with generational craftsmanship in the southern coastal belt.",
            importance = "Sri Lanka supplies over 85% of the world's 'True Cinnamon' (Cinnamomum verum), distinct from harsh cassia bark. Historically coveted by Phoenicians, ancient Egyptians, and European empires, true Ceylon cinnamon contains negligible coumarin, rendering it completely safe for liver health while delivering exquisite sweetness and deep medicinal antibacterial properties.",
            marketLocation = "Directly from artisanal spice gardens in Matara & Galle, or Colombo Dutch Hospital precinct organic spice boutiques.",
            address = "Mirissa-Matara Cinnamon Belt, Southern Province",
            originRegion = "Matara & Galle District",
            priceIndication = "LKR 2,200 / 250g bundle (approx. $7.50)",
            rating = 4.9f,
            reviewCount = 210,
            imageRes = R.drawable.sri_lankan_spices,
            latitude = 5.9549,
            longitude = 80.5550,
            isFeatured = true,
            tags = listOf("True Cinnamon", "Alba Grade", "Export Quality", "Spices")
        ),
        LocalItem(
            id = "item_ambalangoda_masks",
            title = "Handcrafted Ambalangoda Raksha & Kolam Masks",
            category = "Local Products",
            shortDescription = "Intricately carved soft kaduru wood masks adorned with natural mineral dyes, symbolizing ancient protective folklore.",
            importance = "Originating from southern coastal ritual dances (Tovil and Kolam ceremonies), these masks are believed to ward off evil spirits and heal afflictions. Master carvers in Ambalangoda preserve centuries-old geometric guidelines and botanical pigments passed down through family lineages.",
            marketLocation = "Ariyapala Traditional Masks Museum & Workshop, Main Street, Ambalangoda.",
            address = "No. 426 Main Street, Ambalangoda 80300",
            originRegion = "Ambalangoda, Southern Coast",
            priceIndication = "LKR 4,500 – 28,000 (approx. $15 – $95)",
            rating = 4.85f,
            reviewCount = 175,
            imageRes = R.drawable.mask_painting,
            latitude = 6.2358,
            longitude = 80.0539,
            isFeatured = false,
            tags = listOf("Wood Craft", "Folk Art", "Raksha Mask", "Living Heritage")
        ),

        // CATEGORY: Markets
        LocalItem(
            id = "market_pettah_floating",
            title = "Pettah Floating Market & Gold Street Bazaars",
            category = "Markets",
            shortDescription = "A vibrant waterside promenade and historic trading hub bustling with street produce, gems, textiles, and local street delicacies.",
            importance = "Restored along the historic Beira Lake canal, the Floating Market transformed a former commercial bottleneck into an urban scenic gem. Adjoining Pettah's century-old streets (Keyzer St, Sea St), it serves as the beating commercial heartbeat of Sri Lanka, where multi-ethnic communities have engaged in free-market commerce for centuries.",
            marketLocation = "Pettah Waterside along Bastian Mawatha, a 3-minute stroll from Colombo Fort Railway Terminal.",
            address = "Bastian Mawatha, Pettah, Colombo 01100",
            originRegion = "Colombo Central",
            priceIndication = "Free entry / Street purchases from LKR 200",
            rating = 4.7f,
            reviewCount = 480,
            imageRes = R.drawable.home,
            latitude = 6.9360,
            longitude = 79.8550,
            isFeatured = true,
            tags = listOf("Night Market", "Street Food", "Historic Bazaars", "Waterfront")
        ),
        LocalItem(
            id = "market_kandy_central",
            title = "Kandy Central Heritage Market",
            category = "Markets",
            shortDescription = "Two-story colonial indoor market filled with hill-country vegetables, fresh spices, traditional sweetmeats, and brass handicrafts.",
            importance = "Built in colonial architecture overlooking the historic Kandy Lake valley, this bustling bazaar is the central pantry for Kandy residents and highland farmers. Here you can witness traditional Sri Lankan bartering and sample authentic jaggery, buffalo curd, and rare hill-country herbs.",
            marketLocation = "Located in the heart of Kandy downtown, walking distance from the sacred Temple of the Tooth Relic.",
            address = "Market Square, Dalada Veediya, Kandy 20000",
            originRegion = "Central Province (Kandy)",
            priceIndication = "Free entry / Produce priced by weight",
            rating = 4.8f,
            reviewCount = 265,
            imageRes = R.drawable.discover,
            latitude = 7.2936,
            longitude = 80.6350,
            isFeatured = false,
            tags = listOf("Fresh Produce", "Spice Baskets", "Colonial Market", "Kandy")
        ),

        // CATEGORY: Traditional Medicine
        LocalItem(
            id = "med_hela_wedakama_oils",
            title = "Hela Wedakama Herbal Oils & Pinda Thailaya",
            category = "Traditional Medicine",
            shortDescription = "Authentic indigenous Sri Lankan medicinal oil concocted with over 40 endemic wild herbs, sesame oil, and bee honey.",
            importance = "Sri Lanka's 'Hela Wedakama' is an ancient indigenous healing system distinct from Indian Ayurveda, practiced on the island for over 3,000 years with prehistoric roots dating to King Ravana. Pinda Thailaya and Sarva Wishadi oils are boiled continuously for 7 days in copper cauldrons according to traditional astrological horoscopes, delivering deep natural relief for joint inflammation and nerve vitality.",
            marketLocation = "Ayurvedic Department Approved Pharmacies (Osu Sala) across Kandy, Matale Spice Gardens, and certified Ayurvedic wellness centers.",
            address = "Rajaye Osu Sala, Colombo 07 & Matale Spice Herbal Valley",
            originRegion = "Matale Herbal Belt & Anuradhapura",
            priceIndication = "LKR 950 – 3,200 (approx. $3.20 – $11)",
            rating = 4.92f,
            reviewCount = 195,
            imageRes = R.drawable.spice_garden,
            latitude = 7.4675,
            longitude = 80.6234,
            isFeatured = true,
            tags = listOf("Hela Wedakama", "Herbal Medicine", "Natural Healing", "Ayurveda")
        ),
        LocalItem(
            id = "med_samahan_herbal_brew",
            title = "Peyava 14-Herb Immune Elixir & King Coconut Nectar",
            category = "Traditional Medicine",
            shortDescription = "Concentrated soluble extract of 14 time-tested tropical medicinal herbs, ginger, coriander, and black pepper for respiratory vitality.",
            importance = "Derived from the ancient 'Peyava' decoction drunk by Sri Lankan families during monsoon changes. The golden combination of raw ginger, long pepper, and Pathpadagam is recognized by herbal scientists for clearing upper-respiratory passages and building long-lasting natural immunity.",
            marketLocation = "Every verified local grocery, pharmacy, and herbal apothecary islandwide.",
            address = "Distributed throughout all 9 provinces in Sri Lanka",
            originRegion = "Western & Sabaragamuwa Provinces",
            priceIndication = "LKR 500 / 10-sachet pack (approx. $1.70)",
            rating = 4.88f,
            reviewCount = 420,
            imageRes = R.drawable.green_son,
            latitude = 6.9271,
            longitude = 79.8612,
            isFeatured = false,
            tags = listOf("Peyava", "Immunity Boost", "Botanical Tea", "Herbal Remedy")
        )
    )

    fun getItemById(id: String): LocalItem? {
        return items.firstOrNull { it.id == id } ?: items.firstOrNull()
    }

    fun getItemsByCategory(category: String): List<LocalItem> {
        return items.filter { it.category.equals(category, ignoreCase = true) }
    }
}
