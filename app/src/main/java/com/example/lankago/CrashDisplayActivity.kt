package com.example.lankago

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

class CrashDisplayActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_crash_display)

        val errorMessage = intent.getStringExtra(EXTRA_ERROR_MESSAGE)
            ?: "LankaGo encountered an unexpected issue and safely recovered."

        findViewById<TextView>(R.id.tv_crash_message).text = errorMessage

        findViewById<MaterialButton>(R.id.btn_restart_app).setOnClickListener {
            val intent = Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
            finish()
        }
    }

    companion object {
        const val EXTRA_ERROR_MESSAGE = "extra_error_message"
    }
}
