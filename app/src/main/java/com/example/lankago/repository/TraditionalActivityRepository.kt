package com.example.lankago.repository

import com.example.lankago.R
import com.example.lankago.data.TraditionalActivity

object TraditionalActivityRepository {

    private val activities = listOf(
        TraditionalActivity(
            id = "act_pottery_making",
            title = "Clay Pot Making & Terracotta Art Workshop",
            category = "Pottery & Ceramic Crafts",
            location = "Kalutara, Western Province",
            address = "No. 42 Pottery Village Road, Molagoda, Kalutara 12000",
            duration = "2.5 Hours",
            durationHours = 2.5,
            hostName = "Master Potter Somapala",
            hostTitle = "Master Artisan (35+ years heritage craft experience)",
            hostAvatarRes = R.drawable.ic_admin_profile,
            priceLkr = 4500.0,
            rating = 4.9f,
            reviewCount = 184,
            imageRes = R.drawable.pot_making,
            description = "Immerse yourself in Sri Lanka's millennia-old clay pottery tradition. Under the patient guidance of Master Somapala, learn how to shape raw river clay on a manual potter's wheel, master traditional paddle-and-anvil techniques, and create your own terracotta vessel. After molding, practice intricate line carvings and natural slip glazing before your piece is placed in the firewood kiln.",
            highlights = listOf(
                "Hands-on manual potter's wheel shaping session",
                "Learn traditional Sri Lankan paddle-and-anvil molding",
                "Decorate and take home your personalized terracotta pot",
                "Complimentary warm Ceylon spiced tea and village jaggery"
            ),
            timeSlots = listOf(
                "09:00 AM - 11:30 AM",
                "01:30 PM - 04:00 PM",
                "04:30 PM - 07:00 PM"
            )
        ),
        TraditionalActivity(
            id = "act_wood_carving",
            title = "Traditional Devil Mask & Wood Carving Class",
            category = "Woodcraft & Folk Masks",
            location = "Ambalangoda, Southern Province",
            address = "12 Main Street, Heritage Mask Center, Ambalangoda 80300",
            duration = "3.0 Hours",
            durationHours = 3.0,
            hostName = "Artisan Karunadasa",
            hostTitle = "Third-generation Master Carver & Folk Historian",
            hostAvatarRes = R.drawable.ic_admin_profile,
            priceLkr = 5500.0,
            rating = 4.8f,
            reviewCount = 142,
            imageRes = R.drawable.mask_painting,
            description = "Discover the sacred myths and folklore behind Sri Lanka's famed Kolam and Raksha masks. Work with soft Kaduru (balsa) timber using antique chisels to carve traditional facial features of Gurulu Raksha (bird demon) or Maha Kola Sanni. Finish your masterpiece with natural vegetable pigments and organic lacquers.",
            highlights = listOf(
                "Authentic seasoned Kaduru timber mask block provided",
                "Step-by-step guidance on traditional chisel sculpting",
                "Mixing natural pigments from turmeric, charcoal, and clay",
                "Historical walkthrough of ritual healing ceremonies"
            ),
            timeSlots = listOf(
                "09:30 AM - 12:30 PM",
                "02:00 PM - 05:00 PM"
            )
        ),
        TraditionalActivity(
            id = "act_cooking_class",
            title = "Authentic Village Cooking Class with Clay Pots",
            category = "Culinary Heritage",
            location = "Sigiriya / Kandy Countryside",
            address = "Habarana Village Byway, Sigiriya Cultural Triangle 21120",
            duration = "3.5 Hours",
            durationHours = 3.5,
            hostName = "Chef Mallika Bandara",
            hostTitle = "Village Culinary Master & Organic Farmer",
            hostAvatarRes = R.drawable.ic_admin_profile,
            priceLkr = 6000.0,
            rating = 5.0f,
            reviewCount = 226,
            imageRes = R.drawable.spice_garden,
            description = "Experience true Sri Lankan culinary magic in a rustic wattle-and-daub kitchen. Harvest fresh gotukola, bird's eye chilies, and curry leaves straight from the organic garden. Grate fresh coconuts using the traditional 'hiramanaya' scraper, grind roasted curry spices on a granite grinding stone (miris gala), and cook over an open wood-fired hearth in clay pots.",
            highlights = listOf(
                "Harvesting fresh organic herbs & vegetables from farm",
                "Hand-grinding roasted curry powder on traditional miris gala",
                "Cooking 5 authentic curries, coconut sambol, and crispy papadam",
                "Buffet feast served on fresh banana leaves with buffalo curd & treacle"
            ),
            timeSlots = listOf(
                "10:00 AM - 01:30 PM (Lunch Experience)",
                "04:00 PM - 07:30 PM (Sunset Feast)"
            )
        ),
        TraditionalActivity(
            id = "act_tea_estate",
            title = "Hand-Plucked Ceylon Tea Tasting & Factory Tour",
            category = "Plantation Heritage",
            location = "Nuwara Eliya, Central Highlands",
            address = "Pedro Estate Valley, Nuwara Eliya 22200",
            duration = "2.0 Hours",
            durationHours = 2.0,
            hostName = "Master Brewer Anthony",
            hostTitle = "Senior Tea Sommelier (28 years tea crafting)",
            hostAvatarRes = R.drawable.ic_admin_profile,
            priceLkr = 3800.0,
            rating = 4.7f,
            reviewCount = 195,
            imageRes = R.drawable.tea_state,
            description = "Dress in traditional plucking attire with a woven cane basket and head into misty hillside terraces. Learn the art of plucking 'two leaves and a bud'. Follow the leaves into the colonial tea factory for withering, rolling, oxidation, and firing before enjoying a curated tea flight of Silver Tips, Orange Pekoe, and BOP.",
            highlights = listOf(
                "Guided field plucking through high-elevation clonal estates",
                "Colonial-era machinery operation walkthrough",
                "Professional cupping session featuring 6 single-estate teas",
                "Gift pouch of fresh premium loose-leaf Ceylon tea"
            ),
            timeSlots = listOf(
                "09:00 AM - 11:00 AM",
                "11:30 AM - 01:30 PM",
                "02:30 PM - 04:30 PM"
            )
        ),
        TraditionalActivity(
            id = "act_batik_workshop",
            title = "Heritage Wax Batik Dyeing & Fabric Art Class",
            category = "Textile & Craft Design",
            location = "Colombo / Mahawewa",
            address = "78 Galle Road, Artisan Arts Studio, Colombo 00300",
            duration = "2.5 Hours",
            durationHours = 2.5,
            hostName = "Designer Niluka Senanayake",
            hostTitle = "Contemporary Batik Artist & Textile Designer",
            hostAvatarRes = R.drawable.ic_admin_profile,
            priceLkr = 5200.0,
            rating = 4.7f,
            reviewCount = 110,
            imageRes = R.drawable.batik_workshop,
            description = "Explore the vibrant wax-resist dyeing techniques that define Sri Lankan batik. Draw intricate motifs using molten beeswax with a copper 'tjanting' tool onto pure organic cotton. Submerge your fabric in progressive dye baths to achieve hypnotic contrasts and radiant tropical patterns.",
            highlights = listOf(
                "Pure combed cotton canvas banner provided",
                "Hands-on wax pen drawing and hot wax brushing",
                "Multi-layer color immersion dyeing technique",
                "Boil-out wax removal and ready-to-hang finished art"
            ),
            timeSlots = listOf(
                "10:00 AM - 12:30 PM",
                "02:30 PM - 05:00 PM"
            )
        ),
        TraditionalActivity(
            id = "act_traditional_dance",
            title = "Upcountry Kandyan Dance & Geta Beraya Drumming",
            category = "Performing Arts & Music",
            location = "Kandy Cultural Center",
            address = "Sangharaja Mawatha, Kandy Lake Round, Kandy 20000",
            duration = "2.0 Hours",
            durationHours = 2.0,
            hostName = "Guru Prasanna Wijesinghe",
            hostTitle = "State Awarded Kandyan Dance Master",
            hostAvatarRes = R.drawable.ic_admin_profile,
            priceLkr = 4800.0,
            rating = 4.9f,
            reviewCount = 168,
            imageRes = R.drawable.traditional_dance,
            description = "Learn the ritual foundations of traditional Kandyan dance (Uda Rata Natum). Practice essential postural steps (Mandiya), hand mudras, and dynamic acrobatics. Guru Prasanna will teach you basic acoustic rhythms on the hand-tuned double-ended Geta Beraya drum.",
            highlights = listOf(
                "Traditional practice sarong and waist sash provided",
                "Step-by-step tutorial on Mandiya and rhythmic foot stomps",
                "Hands-on drumming lessons on genuine Geta Beraya drums",
                "Exclusive up-close view of ceremonial silver Ves headdresses"
            ),
            timeSlots = listOf(
                "09:30 AM - 11:30 AM",
                "03:30 PM - 05:30 PM"
            )
        )
    )

    fun getAllActivities(): List<TraditionalActivity> = activities

    fun getActivityById(id: String): TraditionalActivity? {
        return activities.find { it.id.equals(id, ignoreCase = true) } ?: activities.firstOrNull()
    }
}
