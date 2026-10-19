package com.example.lankago

import android.graphics.Color
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat

class TraditionalActivitiesActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars = true
        window.statusBarColor = Color.WHITE

        setContentView(R.layout.traditional_activities)

        findViewById<ImageView>(R.id.btn_back)?.setOnClickListener {
            finish()
        }
    }
}
