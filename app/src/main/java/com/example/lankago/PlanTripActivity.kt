package com.example.lankago

import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity

class PlanTripActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Status bar eke icon kalu karanna
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }

        // Status bar eka sudu karanna
        window.statusBarColor = Color.parseColor("#FFFFFF")

        setContentView(R.layout.plan_trip)

        // "Get Started" button click -> LoginActivity ekata yanna
        findViewById<View>(R.id.btn_get_started)?.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        // Back button
        findViewById<View>(R.id.btn_back)?.setOnClickListener {
            finish()
        }
    }
}
