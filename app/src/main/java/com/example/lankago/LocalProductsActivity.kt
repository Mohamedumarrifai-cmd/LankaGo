package com.example.lankago

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.widget.NestedScrollView

class LocalProductsActivity : AppCompatActivity() {

    private lateinit var tabArtsCrafts: TextView
    private lateinit var tabProducts: TextView
    private lateinit var tabMarkets: TextView
    private lateinit var tabMedicines: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }
        window.statusBarColor = Color.parseColor("#FFFFFF")

        setContentView(R.layout.local_products)

        setupTopBar()
        setupCategoryTabs()
        setupProductCards()
        setupBottomNavigation()
    }

    private fun setupTopBar() {
        findViewById<ImageView>(R.id.btn_back_local_products)?.setOnClickListener {
            finish()
        }
    }

    private fun setupCategoryTabs() {
        tabArtsCrafts = findViewById(R.id.tab_arts_crafts)
        tabProducts = findViewById(R.id.tab_products)
        tabMarkets = findViewById(R.id.tab_markets)
        tabMedicines = findViewById(R.id.tab_medicines)

        val tabs = listOf(tabArtsCrafts, tabProducts, tabMarkets, tabMedicines)

        fun selectTab(selectedTab: TextView, category: String) {
            for (tab in tabs) {
                if (tab == selectedTab) {
                    tab.setBackgroundResource(R.drawable.bg_chip_selected)
                    tab.setTextColor(ContextCompat.getColor(this, R.color.white))
                    tab.setTypeface(null, Typeface.BOLD)
                } else {
                    tab.setBackgroundResource(R.drawable.bg_chip_unselected)
                    tab.setTextColor(0xFF666666.toInt())
                    tab.setTypeface(null, Typeface.NORMAL)
                }
            }
            filterProductsByCategory(category)
        }

        tabArtsCrafts.setOnClickListener { selectTab(tabArtsCrafts, "Arts & Crafts") }
        tabProducts.setOnClickListener { selectTab(tabProducts, "Products") }
        tabMarkets.setOnClickListener { selectTab(tabMarkets, "Markets") }
        tabMedicines.setOnClickListener { selectTab(tabMedicines, "Medicines") }
    }

    private fun filterProductsByCategory(category: String) {
        val cardPottery = findViewById<CardView>(R.id.card_pottery)
        val cardSpices = findViewById<CardView>(R.id.card_spices)
        val cardElephant = findViewById<CardView>(R.id.card_elephant)
        val cardTextiles = findViewById<CardView>(R.id.card_textiles)
        val cardShellBowls = findViewById<CardView>(R.id.card_shell_bowls)
        val cardTeaSet = findViewById<CardView>(R.id.card_tea_set)

        when (category) {
            "Arts & Crafts" -> {
                cardPottery?.visibility = View.VISIBLE
                cardSpices?.visibility = View.VISIBLE
                cardElephant?.visibility = View.VISIBLE
                cardTextiles?.visibility = View.VISIBLE
                cardShellBowls?.visibility = View.VISIBLE
                cardTeaSet?.visibility = View.VISIBLE
            }
            "Products" -> {
                cardPottery?.visibility = View.VISIBLE
                cardSpices?.visibility = View.VISIBLE
                cardElephant?.visibility = View.GONE
                cardTextiles?.visibility = View.GONE
                cardShellBowls?.visibility = View.GONE
                cardTeaSet?.visibility = View.VISIBLE
            }
            "Markets" -> {
                cardPottery?.visibility = View.GONE
                cardSpices?.visibility = View.VISIBLE
                cardElephant?.visibility = View.VISIBLE
                cardTextiles?.visibility = View.VISIBLE
                cardShellBowls?.visibility = View.GONE
                cardTeaSet?.visibility = View.GONE
            }
            "Medicines" -> {
                cardPottery?.visibility = View.GONE
                cardSpices?.visibility = View.VISIBLE
                cardElephant?.visibility = View.GONE
                cardTextiles?.visibility = View.GONE
                cardShellBowls?.visibility = View.GONE
                cardTeaSet?.visibility = View.VISIBLE
            }
        }
    }

    private fun setupProductCards() {
        val clickAction = View.OnClickListener {
            val intent = Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
            startActivity(intent)
        }

        findViewById<CardView>(R.id.card_pottery)?.setOnClickListener(clickAction)
        findViewById<CardView>(R.id.card_spices)?.setOnClickListener(clickAction)
        findViewById<CardView>(R.id.card_elephant)?.setOnClickListener(clickAction)
        findViewById<CardView>(R.id.card_textiles)?.setOnClickListener(clickAction)
        findViewById<CardView>(R.id.card_shell_bowls)?.setOnClickListener(clickAction)
        findViewById<CardView>(R.id.card_tea_set)?.setOnClickListener(clickAction)
    }

    private fun setupBottomNavigation() {
        // Home
        findViewById<View>(R.id.nav_home)?.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
            startActivity(intent)
            finish()
        }

        // Products (re-center and scroll top)
        findViewById<View>(R.id.nav_products)?.setOnClickListener {
            findViewById<NestedScrollView>(R.id.scroll_products)?.smoothScrollTo(0, 0)
            if (::tabArtsCrafts.isInitialized) {
                tabArtsCrafts.performClick()
            }
        }

        // Maps
        findViewById<View>(R.id.nav_maps)?.setOnClickListener {
            val intent = Intent(this, MapRouteActivity::class.java)
            startActivity(intent)
        }

        // Profile
        findViewById<View>(R.id.nav_profile)?.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }
    }
}
