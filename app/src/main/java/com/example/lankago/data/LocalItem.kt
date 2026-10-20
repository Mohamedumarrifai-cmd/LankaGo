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
        // 1. Handmade Pottery (Arts & Crafts)
        LocalItem(
            id = "item_pottery",
            title = "Handmade Clay Pottery & Terracotta Cookware",
            category = "Arts & Crafts",
            description = "Authentic handmade earthen pots crafted from riverbed clay on traditional kick-wheels and pit-fired with dried coconut husks. The porous clay allows moisture and heat to circulate evenly during cooking.",
            primaryUsage = "Used across Sri Lankan households for slow-cooking traditional fish ambul thiyal, curries, and setting thick creamy buffalo curd. Enhances natural aroma, preserves nutrients, and imparts an earthy flavor.",
            famousCity = "Trincomalee & Molagoda (Kegalle)",
            culturalImportance = "Dating back to the Anuradhapura Kingdom (circa 4th century BC), clay pottery making is one of Sri Lanka's oldest indigenous craft traditions passed down through generations of rural artisan families.",
            imageUrl = "https://images.unsplash.com/photo-1565193566173-7a0ee3dbe261",
            imageRes = R.drawable.pottery,
            priceIndication = "LKR 450 – 1,800",
            rating = 4.6f,
            reviewCount = 142,
            latitude = 8.5874,
            longitude = 81.2152
        ),

        // 2. Sri Lankan Spices (Products)
        LocalItem(
            id = "item_spices",
            title = "Pure Sri Lankan Spices & Alba True Cinnamon",
            category = "Products",
            description = "Grade Alba true Ceylon cinnamon quills, fragrant green cardamom pods, pungent black peppercorns, cloves, and whole nutmeg freshly harvested from organic highland spice gardens.",
            primaryUsage = "Essential culinary foundation for signature Sri Lankan curries, herbal infusions, desserts, and ancient Ayurvedic remedies. True cinnamon lowers blood sugar and possesses powerful antioxidant properties.",
            famousCity = "Matale & Kandy (Central Spice Belt)",
            culturalImportance = "Sri Lanka's true cinnamon (Cinnamomum verum) and spice wealth attracted Arab, Portuguese, Dutch, and British merchant voyagers, fundamentally shaping the island's colonial history and maritime trade routes.",
            imageUrl = "https://images.unsplash.com/photo-1596040033229-a9821ebd058d",
            imageRes = R.drawable.sri_lankan_spices,
            priceIndication = "LKR 950 – 3,500",
            rating = 4.5f,
            reviewCount = 268,
            latitude = 7.4675,
            longitude = 80.6234
        ),

        // 3. Wooden Elephant (Arts & Crafts)
        LocalItem(
            id = "item_elephant",
            title = "Hand-Carved Wooden Elephant & Ebony Sculptures",
            category = "Arts & Crafts",
            description = "Finely sculpted Sri Lankan tusker figurine hand-carved by master woodworkers from seasoned teak, mahogany, and sustainably harvested rosewood with intricate ceremonial blankets.",
            primaryUsage = "Cherished symbol of good fortune, royalty, and majestic Sri Lankan wildlife. Displayed as a premium decorative home centerpiece and beloved souvenir.",
            famousCity = "Colombo & Galle Fort",
            culturalImportance = "The Asian elephant is deeply revered in Sri Lankan culture, Buddhism, and the sacred Esala Perahera where tuskers carry the sacred tooth relic of the Buddha.",
            imageUrl = "https://images.unsplash.com/photo-1534567153574-2b12153a87f0",
            imageRes = R.drawable.elephent,
            priceIndication = "LKR 1,300 – 8,500",
            rating = 4.5f,
            reviewCount = 195,
            latitude = 6.9271,
            longitude = 79.8612
        ),

        // 4. Woven Textiles (Arts & Crafts)
        LocalItem(
            id = "item_textiles",
            title = "Traditional Handloom Woven Textiles & Sarongs",
            category = "Arts & Crafts",
            description = "Brightly colored artisanal fabrics, cotton sarongs, table runners, and tapestries woven on wooden pit-looms and handlooms using natural vegetable-dyed yarns.",
            primaryUsage = "Worn as traditional festive attire during National New Year (Avurudu) and weddings. Also used for vibrant bohemian home furnishings and eco-friendly garments.",
            famousCity = "Embilipitiya & Thalagune (Kandy)",
            culturalImportance = "Tracing roots to ancient Indian weavers who arrived with Queen Kuveni, Sri Lanka's handloom industry is a cottage-craft heritage preserving intricate geometric motifs like 'Gal Bindu'.",
            imageUrl = "https://images.unsplash.com/photo-1606760227091-3dd870d97f1d",
            imageRes = R.drawable.textiles,
            priceIndication = "LKR 2,500 – 12,000",
            rating = 4.7f,
            reviewCount = 176,
            latitude = 6.3421,
            longitude = 80.8522
        ),

        // 5. Coconut Shell Bowls (Arts & Crafts)
        LocalItem(
            id = "item_shell_bowls",
            title = "Eco-Friendly Polished Coconut Shell Bowls & Utensils",
            category = "Arts & Crafts",
            description = "Natural reclaimed coconut shells cut, sanded by hand to a silky finish, and conditioned with organic virgin coconut oil without artificial lacquers or chemicals.",
            primaryUsage = "Used as smoothie bowls, soup basins, salad servers, and eco-friendly kitchenware. Lightweight, 100% biodegradable, and sustainable.",
            famousCity = "Galle & Southern Coastal Belt",
            culturalImportance = "Represents the traditional Sri Lankan ethos of zero-waste living, where every part of the coconut palm ('Kapruka' - tree of life) serves purpose in daily island life.",
            imageUrl = "https://images.unsplash.com/photo-1546069901-ba9599a7e63c",
            imageRes = R.drawable.shell_bowls,
            priceIndication = "LKR 1,850 – 4,200",
            rating = 4.8f,
            reviewCount = 154,
            latitude = 6.0535,
            longitude = 80.2210
        ),

        // 6. Pure Ceylon Tea (Products)
        LocalItem(
            id = "item_ceylon_tea",
            title = "Pure Ceylon Single-Estate Black Tea & Ceramic Set",
            category = "Products",
            description = "Hand-plucked tender two leaves and a bud, processed using traditional orthodox rolling and fermentation techniques at high-elevation plantations surrounded by misty cloud forests.",
            primaryUsage = "Brewed as a world-renowned energizing daily beverage, rich in natural polyphenols, theaflavins, and protective antioxidants. Packaged with ceramic handcrafted tea cups as Sri Lanka's most cherished gift.",
            famousCity = "Kandy & Nuwara Eliya",
            culturalImportance = "Introduced in 1867 by James Taylor at Loolecondera Estate, Ceylon Tea transformed Sri Lanka into the tea capital of the world. Offering freshly brewed tea with jug of hot milk and kitul jaggery is the ultimate national symbol of hospitality ('Ayubowan') in every Sri Lankan household.",
            imageUrl = "https://images.unsplash.com/photo-1576092768241-dec231879fc3",
            imageRes = R.drawable.ceylon_tea_set,
            priceIndication = "LKR 3,400 – 9,500",
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
