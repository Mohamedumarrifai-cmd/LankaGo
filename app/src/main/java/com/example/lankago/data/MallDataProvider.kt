package com.example.lankago.data

import com.example.lankago.R


 // Preloaded catalog of Sri Lanka's prime shopping malls.
  //Used as immediate fallback and baseline if Firebase Realtime Database is cold.

object MallDataProvider {
    val defaultMalls: List<Mall> = listOf(
        Mall(
            id = "mall_havelock_city",
            name = "Havelock City Mall",
            city = "Colombo 05",
            address = "Havelock City, Havelock Road, Colombo 05",
            description = "Sri Lanka's premier lifestyle and entertainment mall spanning 200,000+ sq ft. Features Scope Cinemas IMAX multiplex, international food court, luxury fashion, and rooftop terrace restaurants.",
            operatingHours = "10:00 AM - 10:00 PM",
            imageUrl = "",
            imageRes = R.drawable.havelock_city_mall,
            rating = 4.8,
            storeCount = 200,
            contactNumber = "+94 11 258 5555",
            category = "Mega Mall & IMAX",
            features = listOf("Scope IMAX", "200+ Stores", "International Food Court", "Rooftop Dining", "Car Park")
        ),
        Mall(
            id = "mall_one_galle_face",
            name = "One Galle Face Mall (GCC / OGF)",
            city = "Colombo 02",
            address = "1A Centre Road, Galle Face, Colombo 02",
            description = "Colombo's iconic 7-storey oceanfront luxury mega mall developed by Shangri-La. Boasts PVR Cinemas multiplex, Adventure Zone, Food Studio, and prestigious international luxury brands.",
            operatingHours = "10:00 AM - 10:00 PM",
            imageUrl = "",
            imageRes = R.drawable.one_galle_face,
            rating = 4.9,
            storeCount = 250,
            contactNumber = "+94 11 777 5555",
            category = "Luxury Oceanfront Mega Mall",
            features = listOf("PVR Cinemas", "Luxury Fashion", "Food Studio", "Adventure Zone", "Ocean View")
        ),
        Mall(
            id = "mall_colombo_city_centre",
            name = "Colombo City Centre (CCC)",
            city = "Colombo 02",
            address = "137 Sir James Pieris Mawatha, Colombo 02",
            description = "Overlooking scenic Beira Lake, CCC is a cutting-edge international mall featuring Scope Cinemas, Food Studio with global culinary options, high-end lifestyle stores, and kids entertainment.",
            operatingHours = "10:00 AM - 10:00 PM",
            imageUrl = "",
            imageRes = R.drawable.colombo_city_center,
            rating = 4.7,
            storeCount = 130,
            contactNumber = "+94 11 475 7000",
            category = "Lakefront Shopping & Leisure",
            features = listOf("Scope Cinemas", "Beira Lake Views", "Food Studio", "Apple Store", "Bowling Alley")
        ),
        Mall(
            id = "mall_marino",
            name = "Marino Mall",
            city = "Colombo 03",
            address = "590 Galle Road, Kollupitiya, Colombo 03",
            description = "Modern seaside shopping hub famous for its state-of-the-art 9D VR Gaming Arena, Damro megastore, panoramic sea-view food court, electronic pavilions, and fashion.",
            operatingHours = "10:00 AM - 09:30 PM",
            imageUrl = "",
            imageRes = R.drawable.marino_mall,
            rating = 4.5,
            storeCount = 90,
            contactNumber = "+94 11 259 5555",
            category = "VR Gaming & Retail",
            features = listOf("VR Gaming Arena", "9D Cinema", "Sea-View Food Court", "Damro Mega Store", "Electronics")
        ),
        Mall(
            id = "mall_crescat",
            name = "Crescat Boulevard",
            city = "Colombo 03",
            address = "89 Galle Road, Colombo 03",
            description = "One of Colombo's most beloved boutique shopping centres adjacent to Cinnamon Grand Colombo. Renowned for its multicultural gourmet basement food court, luxury perfumes, and boutiques.",
            operatingHours = "10:00 AM - 09:00 PM",
            imageUrl = "",
            imageRes = R.drawable.crescat_boulevard,
            rating = 4.4,
            storeCount = 70,
            contactNumber = "+94 11 243 7437",
            category = "Boutique & Gourmet Food",
            features = listOf("Basement Food Court", "Cinnamon Grand Access", "Gourmet Keells", "Boutiques", "Spas")
        ),
        Mall(
            id = "mall_galle_city_center",
            name = "Galle City Centre",
            city = "Galle",
            address = "Wakwella Road, Galle",
            description = "The premier commercial and shopping landmark in the Southern province capital. Offers departmental stores, electronics, local fashion retailers, and family dining near the historic Galle Fort.",
            operatingHours = "09:30 AM - 09:00 PM",
            imageUrl = "",
            imageRes = R.drawable.galle_city_center,
            rating = 4.6,
            storeCount = 85,
            contactNumber = "+94 91 224 5555",
            category = "Southern Commercial Hub",
            features = listOf("Departmental Stores", "Electronics Hub", "Family Dining", "Ample Parking", "Retail Fashion")
        ),
        Mall(
            id = "mall_arcade_independence",
            name = "Arcade Independence Square",
            city = "Colombo 07",
            address = "Independence Square, Cinnamon Gardens, Colombo 07",
            description = "A masterfully restored colonial heritage shopping and dining precinct in Colombo's most prestigious neighborhood. Features lush courtyards, Empire Cinema, and open-air upscale bistros.",
            operatingHours = "10:00 AM - 10:00 PM",
            imageUrl = "",
            imageRes = R.drawable.heritage_galle_fort,
            rating = 4.7,
            storeCount = 45,
            contactNumber = "+94 11 267 2670",
            category = "Colonial Heritage Arcade",
            features = listOf("Empire Cinema", "Open-air Courtyard", "Colonial Architecture", "Fine Dining", "Lion Statues")
        )
    )
}
