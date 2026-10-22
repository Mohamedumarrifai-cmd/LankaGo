package com.example.lankago

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class MapRouteActivity : AppCompatActivity() {

    private lateinit var mapWebView: WebView

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }
        window.statusBarColor = Color.TRANSPARENT

        setContentView(R.layout.map_route)

        mapWebView = findViewById(R.id.map_webview)
        setupWebView()

        findViewById<ImageView>(R.id.btn_back)?.setOnClickListener {
            finish()
        }

        findViewById<View>(R.id.btn_start_nav)?.setOnClickListener {
            val uri = Uri.parse("geo:7.9570,80.7603?q=7.9570,80.7603(Sigiriya Rock Fortress)")
            startActivity(Intent(Intent.ACTION_VIEW, uri))
        }

        setupBottomNavigation()
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView() {
        mapWebView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            databaseEnabled = true
            useWideViewPort = true
            loadWithOverviewMode = true
            setSupportZoom(true)
            builtInZoomControls = false
            displayZoomControls = false
            cacheMode = WebSettings.LOAD_DEFAULT
        }

        mapWebView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                // Plot default destination: Sigiriya Rock Fortress
                mapWebView.evaluateJavascript(
                    "plotDestinationAndRoute(7.9570, 80.7603, 'Sigiriya Rock Fortress', 'Matale District, Central Province');",
                    null
                )
            }
        }

        mapWebView.loadUrl("file:///android_asset/leaflet_map.html")
    }

    private fun setupBottomNavigation() {
        findViewById<View>(R.id.nav_home)?.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
            startActivity(intent)
            finish()
        }

        findViewById<View>(R.id.nav_products)?.setOnClickListener {
            startActivity(Intent(this, LocalProductsActivity::class.java))
        }

        findViewById<View>(R.id.nav_profile)?.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        mapWebView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapWebView.onPause()
    }

    override fun onDestroy() {
        mapWebView.destroy()
        super.onDestroy()
    }
}
