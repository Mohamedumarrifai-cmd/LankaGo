package com.example.lankago

import android.graphics.Color
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat

class LocalProductsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars = true
        window.statusBarColor = Color.WHITE

        setContentView(R.layout.local_products)

        findViewById<ImageView>(R.id.btn_back)?.setOnClickListener {
            finish()
        }
    }
}
