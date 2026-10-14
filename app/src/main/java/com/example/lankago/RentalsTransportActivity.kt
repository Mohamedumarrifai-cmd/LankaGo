package com.example.lankago

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView

class RentalsTransportActivity : AppCompatActivity() {

    private lateinit var allCards: List<CardView>
    private lateinit var allChips: List<TextView>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }
        window.statusBarColor = Color.parseColor("#FFFFFF")

        setContentView(R.layout.rentals_and_transport)

        // Back button
        findViewById<ImageView>(R.id.btn_back).setOnClickListener { finish() }

        // Initialize Chips
        val chipAll = findViewById<TextView>(R.id.chip_all)
        val chipScooters = findViewById<TextView>(R.id.chip_scooters)
        val chipBikes = findViewById<TextView>(R.id.chip_bikes)
        val chipCars = findViewById<TextView>(R.id.chip_cars)
        val chipVans = findViewById<TextView>(R.id.chip_vans)
        val chipTuktuk = findViewById<TextView>(R.id.chip_tuktuk)
        val chipBuses = findViewById<TextView>(R.id.chip_buses)

        allChips = listOf(chipAll, chipScooters, chipBikes, chipCars, chipVans, chipTuktuk, chipBuses)

        // Initialize Cards
        allCards = listOf(
            findViewById(R.id.card_epic_ride),
            findViewById(R.id.card_colombo_scooter),
            findViewById(R.id.card_unawatuna),
            findViewById(R.id.card_dambulla_scooter),
            findViewById(R.id.card_hikkaduwa_scooter),
            findViewById(R.id.card_kandy_bike),
            findViewById(R.id.card_mirissa_bike),
            findViewById(R.id.card_ella_bike),
            findViewById(R.id.card_arugam_bike),
            findViewById(R.id.card_malkey_car),
            findViewById(R.id.card_casons_car),
            findViewById(R.id.card_kings_car),
            findViewById(R.id.card_euro_car),
            findViewById(R.id.card_casons_van),
            findViewById(R.id.card_malkey_van),
            findViewById(R.id.card_kandy_van),
            findViewById(R.id.card_negombo_tuktuk),
            findViewById(R.id.card_galle_tuktuk),
            findViewById(R.id.card_colombo_tuktuk),
            findViewById(R.id.card_lanka_bus)
        )

        // Attach Chip Listeners
        chipAll.setOnClickListener { filterCards("all", chipAll) }
        chipScooters.setOnClickListener { filterCards("scooter", chipScooters) }
        chipBikes.setOnClickListener { filterCards("bike", chipBikes) }
        chipCars.setOnClickListener { filterCards("car", chipCars) }
        chipVans.setOnClickListener { filterCards("van", chipVans) }
        chipTuktuk.setOnClickListener { filterCards("tuktuk", chipTuktuk) }
        chipBuses.setOnClickListener { filterCards("bus", chipBuses) }

        // Attach Card Website Click Listeners
        findViewById<CardView>(R.id.card_epic_ride).setOnClickListener { openUrl("https://www.epicride.lk/") }
        findViewById<CardView>(R.id.card_colombo_scooter).setOnClickListener { openUrl("https://www.google.com/search?q=Colombo+Scooter+Rentals") }
        findViewById<CardView>(R.id.card_unawatuna).setOnClickListener { openUrl("https://www.google.com/search?q=Unawatuna+Scooter+Rental") }
        findViewById<CardView>(R.id.card_dambulla_scooter).setOnClickListener { openUrl("https://www.google.com/search?q=Dambulla+Scooter+Rentals") }
        findViewById<CardView>(R.id.card_hikkaduwa_scooter).setOnClickListener { openUrl("https://www.google.com/search?q=Hikkaduwa+Scooter+Rental") }

        findViewById<CardView>(R.id.card_kandy_bike).setOnClickListener { openUrl("https://www.google.com/search?q=Kandy+Bicycle+Rental") }
        findViewById<CardView>(R.id.card_mirissa_bike).setOnClickListener { openUrl("https://www.google.com/search?q=Mirissa+Bicycle+Rental") }
        findViewById<CardView>(R.id.card_ella_bike).setOnClickListener { openUrl("https://www.google.com/search?q=Ella+Adventure+Rentals+Sri+Lanka") }
        findViewById<CardView>(R.id.card_arugam_bike).setOnClickListener { openUrl("https://www.google.com/search?q=Arugam+Bay+Surf+and+Bike+Rental") }

        findViewById<CardView>(R.id.card_malkey_car).setOnClickListener { openUrl("https://www.malkey.lk/") }
        findViewById<CardView>(R.id.card_casons_car).setOnClickListener { openUrl("https://www.casons.lk/") }
        findViewById<CardView>(R.id.card_kings_car).setOnClickListener { openUrl("https://www.kingsrentacar.lk/") }
        findViewById<CardView>(R.id.card_euro_car).setOnClickListener { openUrl("https://www.eurocarrental.lk/") }

        findViewById<CardView>(R.id.card_casons_van).setOnClickListener { openUrl("https://www.casons.lk/") }
        findViewById<CardView>(R.id.card_malkey_van).setOnClickListener { openUrl("https://www.malkey.lk/") }
        findViewById<CardView>(R.id.card_kandy_van).setOnClickListener { openUrl("https://www.google.com/search?q=Kandy+Van+Tours") }

        findViewById<CardView>(R.id.card_negombo_tuktuk).setOnClickListener { openUrl("https://www.google.com/search?q=Negombo+Tuk+Tuk+Rentals") }
        findViewById<CardView>(R.id.card_galle_tuktuk).setOnClickListener { openUrl("https://www.google.com/search?q=Galle+Tuk+Tuk+Rental") }
        findViewById<CardView>(R.id.card_colombo_tuktuk).setOnClickListener { openUrl("https://www.google.com/search?q=Colombo+Tuk+Tuk+Tours") }

        findViewById<CardView>(R.id.card_lanka_bus).setOnClickListener { openUrl("https://www.google.com/search?q=Lanka+Coach+Tours+Sri+Lanka") }
    }

    private fun filterCards(targetTag: String, activeChip: TextView) {
        setChipActive(activeChip, allChips)
        allCards.forEach { card ->
            if (targetTag == "all" || card.tag == targetTag) {
                card.visibility = View.VISIBLE
            } else {
                card.visibility = View.GONE
            }
        }
    }

    private fun setChipActive(activeChip: TextView, allChips: List<TextView>) {
        allChips.forEach { chip ->
            if (chip == activeChip) {
                chip.setBackgroundResource(R.drawable.chip_active)
                chip.setTextColor(Color.WHITE)
            } else {
                chip.setBackgroundResource(R.drawable.chip_inactive)
                chip.setTextColor(Color.parseColor("#1A2B4C"))
            }
        }
    }

    private fun openUrl(url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "Cannot open link", Toast.LENGTH_SHORT).show()
        }
    }
}
