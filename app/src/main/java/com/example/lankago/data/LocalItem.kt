package com.example.lankago.data

import com.example.lankago.R

/**
 * Domain model representing a Sri Lankan local cultural product, artisanal bazaar/market,
 * or indigenous traditional medicine (Hela Wedakama / Ayurveda).
 *
 * Exact required schema:
 * - [id]: Unique identifier
 * - [title]: Product / Market / Medicine name
 * - [category]: "Local Products", "Markets", or "Traditional Medicine"
 * - [description]: Detailed description of what the product actually is
 * - [primaryUsage]: Primary usage, applications, and health/lifestyle benefits
 * - [famousCity]: The exact local city or region best known for this product
 * - [culturalImportance]: Cultural & historical significance to Sri Lankan heritage
 * - [imageUrl]: High-resolution image URL (or fallback resource identifier)
 */
data class LocalItem(
    val id: String,
    val title: String,
    val category: String, // "Local Products", "Markets", "Traditional Medicine"
    val description: String,
    val primaryUsage: String,
    val famousCity: String,
    val culturalImportance: String,
    val imageUrl: String = "",
    val imageRes: Int = 0,
    val priceIndication: String = "",
    val rating: Float = 4.9f,
    val reviewCount: Int = 120,
    val latitude: Double = 6.9271,
    val longitude: Double = 79.8612
)

/**
 * Comprehensive Mock Data Repository for Sri Lankan Heritage & Cultural Artifacts.
 */
object LocalItemDataProvider {

    val items: List<LocalItem> = listOf(
        // 1. Pure Ceylon Tea (Local Products)
        LocalItem(
            id = "item_ceylon_tea",
            title = "Pure Ceylon Single-Estate Black Tea & Silver Tips",
            category = "Local Products",
            description = "Hand-plucked tender two leaves and a bud, processed using traditional orthodox rolling and fermentation techniques at high-elevation plantations surrounded by misty cloud forests.",
            primaryUsage = "Brewed as a world-renowned energizing daily beverage, rich in natural polyphenols, theaflavins, and protective antioxidants. Also packaged in handcrafted wooden boxes as Sri Lanka's most cherished souvenir for travelers worldwide.",
            famousCity = "Nuwara Eliya (Central Highlands)",
            culturalImportance = "Introduced in 1867 by James Taylor at Loolecondera Estate, Ceylon Tea transformed Sri Lanka into the tea capital of the world. Offering freshly brewed tea with jug of hot milk and kitul jaggery is the ultimate national symbol of hospitality ('Ayubowan') in every Sri Lankan household.",
            imageUrl = "https://images.unsplash.com/photo-1576092768241-dec231879fc3",
            imageRes = R.drawable.ceylon_tea_set,
            priceIndication = "LKR 1,500 – 6,500 (approx. $5 – $22)",
            rating = 4.96f,
            reviewCount = 385,
            latitude = 6.9497,
            longitude = 80.7891
        ),

        // 2. Ambalangoda Hand-Carved Masks (Local Products)
        LocalItem(
            id = "item_ambalangoda_masks",
            title = "Authentic Ambalangoda Kaduru Raksha Masks",
            category = "Local Products",
            description = "Intricately hand-sculpted wooden masks made from lightweight balsa-like 'Kaduru' (Strychnos nux-vomica) wood, smoked for termite resistance, and hand-painted with organic mineral and plant pigments.",
            primaryUsage = "Worn in ancient theatrical performances (Kolam drama) and exorcism dance rituals (Tovil). Today, widely mounted in homes, hotels, and entrance archways as protective amulets that ward off the evil eye and attract positive energy.",
            famousCity = "Ambalangoda (Galle District)",
            culturalImportance = "Ambalangoda's mask carving tradition dates back over three centuries. Master carvers follow strict iconographic rules handed down through generation lineages. The Ginidella (Fire Demon), Gurulu (Bird of Vishnu), and Naga (Cobra) Raksha masks represent Sri Lanka's living prehistoric demonological and theatrical heritage.",
            imageUrl = "https://images.unsplash.com/photo-1582560475093-ba66accbc424",
            imageRes = R.drawable.mask_painting,
            priceIndication = "LKR 4,500 – 25,000 (approx. $15 – $85)",
            rating = 4.88f,
            reviewCount = 210,
            latitude = 6.2358,
            longitude = 80.0539
        ),

        // 3. Pettah Floating Market & Historic Bazaars (Markets)
        LocalItem(
            id = "market_pettah_floating",
            title = "Pettah Floating Market & Heritage Bazaars",
            category = "Markets",
            description = "An open-air waterside market complex consisting of 92 stalls suspended over Beira Lake, flanked by centuries-old multi-ethnic trading streets selling street spices, handloom fabrics, electronics, and authentic street eats.",
            primaryUsage = "A vibrant trading and leisure hub where travelers and locals bargain for fresh spices, tropical fruits, handmade brass souvenirs, and savor authentic street snacks like kottu roti and spiced king coconut water.",
            famousCity = "Colombo Fort & Pettah (Colombo)",
            culturalImportance = "Pettah has been Sri Lanka's international trading nexus since Arab, Portuguese, Dutch, and British eras. The Floating Market reflects the country's modern urban revitalization while celebrating the historic cross-cultural mercantile traditions of Sinhala, Tamil, Muslim, and Burgher communities.",
            imageUrl = "https://images.unsplash.com/photo-1555396273-367ea4eb4db5",
            imageRes = R.drawable.home,
            priceIndication = "Free Entry / Purchases from LKR 200",
            rating = 4.75f,
            reviewCount = 520,
            latitude = 6.9360,
            longitude = 79.8550
        ),

        // 4. Kandy Central Heritage Market (Markets)
        LocalItem(
            id = "market_kandy_central",
            title = "Kandy Central Heritage Market & Spice Bazaar",
            category = "Markets",
            description = "A classic two-story colonial courtyard marketplace filled with colorful hill-country vegetable pyramids, fresh Ceylon spices, woven cane baskets, and brassware.",
            primaryUsage = "Central purchasing hub for highland spices (cardamom, clove, alba cinnamon), fresh artisanal jaggery, pure bee honey, and traditional handicrafts directly from rural artisan cooperatives.",
            famousCity = "Kandy (Central Province)",
            culturalImportance = "Located in the royal capital near the sacred Temple of the Tooth, this market has served as the highland kingdom's central pantry for centuries, preserving age-old barter traditions and generational spice-merchant families.",
            imageUrl = "https://images.unsplash.com/photo-1533900298318-6b8da08a523e",
            imageRes = R.drawable.discover,
            priceIndication = "Free Entry / Spices from LKR 500",
            rating = 4.82f,
            reviewCount = 290,
            latitude = 7.2936,
            longitude = 80.6350
        ),

        // 5. Hela Wedakama Herbal Oils & Pinda Thailaya (Traditional Medicine)
        LocalItem(
            id = "med_hela_wedakama_oils",
            title = "Hela Wedakama Pinda Thailaya & Herbal Balms",
            category = "Traditional Medicine",
            description = "An authentic indigenous Sri Lankan medicinal oil concocted with over 40 wild endemic herbs, bee wax, sesame oil, and sarsaparilla, continuously brewed over wood fires for seven days according to ancient Sanskrit formulas.",
            primaryUsage = "Topically massaged to relieve chronic rheumatoid arthritis, muscular soreness, varicose veins, and body heat exhaustion. Used in rejuvenating Ayurvedic Panchakarma treatments across coastal and highland wellness centers.",
            famousCity = "Matale & Anuradhapura (Ancient Herbal Belt)",
            culturalImportance = "'Hela Wedakama' is Sri Lanka's unique 3,000-year-old indigenous medicine system, distinct from Indian Ayurveda and dating back to legendary King Ravana. The knowledge of boiling roots, bark, and seeds according to lunar phases represents one of the world's oldest surviving traditional herbal pharmacopeias.",
            imageUrl = "https://images.unsplash.com/photo-1608571423902-eed4a5ad8108",
            imageRes = R.drawable.spice_garden,
            priceIndication = "LKR 950 – 3,200 (approx. $3.20 – $11)",
            rating = 4.94f,
            reviewCount = 230,
            latitude = 7.4675,
            longitude = 80.6234
        ),

        // 6. Peyava 14-Herb Immune Tonic & Samahan (Traditional Medicine)
        LocalItem(
            id = "med_samahan_herbal_brew",
            title = "Peyava 14-Herb Indigenous Immune Decoction",
            category = "Traditional Medicine",
            description = "A 100% natural water-soluble granulate derived from 14 time-tested tropical botanicals including Coriander, Ginger, Black Pepper, Katuwelbatu, and Venivel (Coscinium fenestratum).",
            primaryUsage = "Consumed hot dissolved in water or black tea to instantly relieve sneezing, nasal congestion, seasonal flu, body aches, and fatigue. Suitable for all age groups as an everyday immune booster.",
            famousCity = "Colombo & Sabaragamuwa Province",
            culturalImportance = "Derived from ancient 'Peyava' brews that royal physicians prescribed to maintain the vitality of warrior legions and monastery monks during torrential monsoon downpours.",
            imageUrl = "https://images.unsplash.com/photo-1544367567-0f2fcb009e0b",
            imageRes = R.drawable.green_son,
            priceIndication = "LKR 500 / 10-sachet pack",
            rating = 4.90f,
            reviewCount = 450,
            latitude = 6.9271,
            longitude = 79.8612
        )
    )

    fun getAllItems(): List<LocalItem> = items

    fun getItemById(id: String): LocalItem? {
        return items.firstOrNull { it.id == id } ?: items.firstOrNull()
    }

    fun getItemsByCategory(category: String): List<LocalItem> {
        return items.filter { it.category.equals(category, ignoreCase = true) }
    }
}
