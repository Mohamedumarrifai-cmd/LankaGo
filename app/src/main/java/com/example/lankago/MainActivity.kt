package com.example.lankago

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Full screen karanna
        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )

        setContentView(R.layout.activity_main)

        // Splash screen eka tparama 2k thibila Discover screen ekata yanna
        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, DiscoverActivity::class.java))
            finish()
        }, 2000)
    }
}
