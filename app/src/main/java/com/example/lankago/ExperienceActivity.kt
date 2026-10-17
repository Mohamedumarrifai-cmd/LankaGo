package com.example.lankago

import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class ExperienceActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Status bar eke icon kalu karanna
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }

        // Status bar eka sudu karanna
        window.statusBarColor = Color.parseColor("#FFFFFF")

        setContentView(R.layout.experience)

        // "Next" button click -> PlanTrip screen ekata yanna
        findViewById<View>(R.id.btn_next)?.setOnClickListener {
            startActivity(Intent(this, PlanTripActivity::class.java))
        }

        // "Skip" text click -> Direct Login screen ekata yanna
        findViewById<TextView>(R.id.tv_skip)?.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
}
