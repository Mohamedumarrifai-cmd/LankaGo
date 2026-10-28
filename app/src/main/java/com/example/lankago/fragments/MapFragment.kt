package com.example.lankago.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.webkit.GeolocationPermissions
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.lankago.R
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.Priority
import com.google.android.gms.location.SettingsClient
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

/**
 * Principal Map Screen for LankaGo powered by OpenStreetMap (OSM) & Leaflet.js.
 *
 * Guarantees that whenever opened (from Bottom Navigation or Destination Detail),
 * it starts directly at the user's real-time current GPS location.
 */
open class MapFragment : Fragment() {

    private lateinit var mapWebView: WebView
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var isMapLoaded: Boolean = false
    private var hasCenteredOnUser: Boolean = false

    // Location State
    private var currentUserLocation: Location? = null
    private var destLat: Double? = null
    private var destLng: Double? = null
    private var destTitle: String = "Destination"
    private var destAddress: String = ""

    // UI View References
    private lateinit var etDestinationQuery: EditText
    private lateinit var btnClearQuery: ImageView
    private lateinit var btnFindRoute: MaterialButton
    private lateinit var pbMapLoading: ProgressBar
    private lateinit var fabRecenterLocation: FloatingActionButton
    private lateinit var cardCurrentLocationStatus: MaterialCardView
    private lateinit var locationTextView: TextView
    private lateinit var tvGpsLiveBadge: TextView

    private lateinit var cardRouteSummary: MaterialCardView
    private lateinit var tvDestTitle: TextView
    private lateinit var tvDestSubtitle: TextView
    private lateinit var tvRouteDistance: TextView
    private lateinit var tvRouteEta: TextView
    private lateinit var btnCloseSummary: ImageView
    private lateinit var btnLaunchNavigation: MaterialButton

    // Location Updates
    private var locationRequest: LocationRequest? = null
    private var locationCallback: LocationCallback? = null
    private var systemLocationListener: android.location.LocationListener? = null
    private var cancellationTokenSource: CancellationTokenSource? = null

    // Permission Launcher
    private val locationPermissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false

        if (fineGranted || coarseGranted) {
            checkGpsAndAcquireLocation()
        } else {
            Toast.makeText(
                requireContext(),
                "Location permission denied. Please enable GPS in Settings to see your live position.",
                Toast.LENGTH_SHORT
            ).show()
            locationTextView.text = "📍 Tap here to enable Location permissions"
            tvGpsLiveBadge.text = "OFF"
        }
    }

    // Google Play Services GPS Resolution Launcher
    private val enableGpsLauncher = registerForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == AppCompatActivity.RESULT_OK) {
            startLocationUpdates()
            fetchInitialLocation()
        } else {
            locationTextView.text = "⚠️ GPS is turned off. Tap to turn on Location."
            tvGpsLiveBadge.text = "OFF"
            Toast.makeText(requireContext(), "Please turn on device location to view your position.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        initLocationCallback()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_map, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        destLat = arguments?.getDouble("destLat", 0.0)?.takeIf { it != 0.0 }
        destLng = arguments?.getDouble("destLng", 0.0)?.takeIf { it != 0.0 }
        destTitle = arguments?.getString("destTitle") ?: "Destination"
        destAddress = arguments?.getString("destAddress") ?: ""

        initViews(view)
        setupWebView()
        setupListeners()
        checkLocationPermissionAndStart()
    }

    private fun initViews(view: View) {
        mapWebView = view.findViewById(R.id.map_webview)
        etDestinationQuery = view.findViewById(R.id.et_destination_query)
        btnClearQuery = view.findViewById(R.id.btn_clear_query)
        btnFindRoute = view.findViewById(R.id.btn_find_route)
        pbMapLoading = view.findViewById(R.id.pb_map_loading)
        fabRecenterLocation = view.findViewById(R.id.fab_recenter_location)
        cardCurrentLocationStatus = view.findViewById(R.id.card_current_location_status)
        locationTextView = view.findViewById(R.id.locationTextView)
        tvGpsLiveBadge = view.findViewById(R.id.tv_gps_live_badge)

        cardRouteSummary = view.findViewById(R.id.card_route_summary)
        tvDestTitle = view.findViewById(R.id.tv_dest_title)
        tvDestSubtitle = view.findViewById(R.id.tv_dest_subtitle)
        tvRouteDistance = view.findViewById(R.id.tv_route_distance)
        tvRouteEta = view.findViewById(R.id.tv_route_eta)
        btnCloseSummary = view.findViewById(R.id.btn_close_summary)
        btnLaunchNavigation = view.findViewById(R.id.btn_launch_navigation)
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView() {
        mapWebView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            useWideViewPort = true
            loadWithOverviewMode = true
            setSupportZoom(true)
            builtInZoomControls = false
            displayZoomControls = false
            cacheMode = WebSettings.LOAD_DEFAULT
            setGeolocationEnabled(true)
        }

        mapWebView.webChromeClient = object : WebChromeClient() {
            override fun onGeolocationPermissionsShowPrompt(
                origin: String?,
                callback: GeolocationPermissions.Callback?
            ) {
                callback?.invoke(origin, true, false)
            }
        }

        // Bridge connecting Leaflet Javascript events back to Android
        mapWebView.addJavascriptInterface(AndroidBridge(), "AndroidBridge")

        mapWebView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                onMapReadyCallback()
            }
        }

        mapWebView.loadUrl("file:///android_asset/leaflet_map.html")
    }

    private fun setupListeners() {
        btnClearQuery.setOnClickListener {
            etDestinationQuery.text.clear()
        }

        etDestinationQuery.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                btnClearQuery.visibility = if (s.isNullOrBlank()) View.GONE else View.VISIBLE
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        etDestinationQuery.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE) {
                hideKeyboard()
                triggerDestinationSearch()
                true
            } else {
                false
            }
        }

        btnFindRoute.setOnClickListener {
            hideKeyboard()
            triggerDestinationSearch()
        }

        // Recenter FAB button
        fabRecenterLocation.setOnClickListener {
            val loc = currentUserLocation
            if (loc != null) {
                updateMapLocation(loc.latitude, loc.longitude, shouldCenter = true, accuracy = loc.accuracy)
                Toast.makeText(requireContext(), "📍 Centering on your location", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "📍 Pinpointing your GPS location...", Toast.LENGTH_SHORT).show()
                checkGpsAndAcquireLocation()
            }
        }

        // Location Info Card click
        cardCurrentLocationStatus.setOnClickListener {
            if (!hasLocationPermission()) {
                checkLocationPermissionAndStart()
            } else if (!isGpsEnabled()) {
                promptEnableGps()
            } else {
                currentUserLocation?.let { loc ->
                    updateMapLocation(loc.latitude, loc.longitude, shouldCenter = true, accuracy = loc.accuracy)
                    Toast.makeText(requireContext(), "📍 Centering on your location", Toast.LENGTH_SHORT).show()
                } ?: run {
                    fetchInitialLocation()
                }
            }
        }

        btnCloseSummary.setOnClickListener {
            cardRouteSummary.visibility = View.GONE
            destLat = null
            destLng = null
            mapWebView.evaluateJavascript("clearRoute();", null)
        }

        btnLaunchNavigation.setOnClickListener {
            val lat = destLat
            val lng = destLng
            if (lat != null && lng != null) {
                launchExternalNavigation(lat, lng, destTitle)
            }
        }
    }

    private fun initLocationCallback() {
        locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 2000)
            .setMinUpdateIntervalMillis(1000)
            .build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                for (location in locationResult.locations) {
                    onNewLocationReceived(location, isImmediateFix = false)
                }
            }
        }
    }

    private fun hasLocationPermission(): Boolean {
        val fineGranted = ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarseGranted = ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        return fineGranted || coarseGranted
    }

    private fun isGpsEnabled(): Boolean {
        val lm = context?.getSystemService(Context.LOCATION_SERVICE) as? LocationManager ?: return false
        return lm.isProviderEnabled(LocationManager.GPS_PROVIDER) || lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    private fun checkLocationPermissionAndStart() {
        if (hasLocationPermission()) {
            checkGpsAndAcquireLocation()
        } else {
            locationPermissionsLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    private fun checkGpsAndAcquireLocation() {
        if (!isGpsEnabled()) {
            locationTextView.text = "⚠️ Location is turned off. Tap here to enable GPS."
            tvGpsLiveBadge.text = "OFF"
            promptEnableGps()
        } else {
            tvGpsLiveBadge.text = "LIVE GPS"
            startLocationUpdates()
            fetchInitialLocation()
        }
    }

    private fun promptEnableGps() {
        val req = locationRequest ?: LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 2000).build()
        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(req)
            .setAlwaysShow(true)

        val client: SettingsClient = LocationServices.getSettingsClient(requireActivity())
        client.checkLocationSettings(builder.build())
            .addOnSuccessListener {
                tvGpsLiveBadge.text = "LIVE GPS"
                startLocationUpdates()
                fetchInitialLocation()
            }
            .addOnFailureListener { exception ->
                if (exception is ResolvableApiException) {
                    try {
                        val intentSenderRequest = IntentSenderRequest.Builder(exception.resolution).build()
                        enableGpsLauncher.launch(intentSenderRequest)
                    } catch (_: Exception) {
                        openLocationSettingsFallback()
                    }
                } else {
                    openLocationSettingsFallback()
                }
            }
    }

    private fun openLocationSettingsFallback() {
        try {
            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            startActivity(intent)
            Toast.makeText(requireContext(), "Please turn on Location / GPS in Settings", Toast.LENGTH_SHORT).show()
        } catch (_: Exception) {}
    }

    @SuppressLint("MissingPermission")
    private fun fetchInitialLocation() {
        if (!hasLocationPermission()) return

        locationTextView.text = "📍 Finding your current GPS location..."
        pbMapLoading.visibility = View.VISIBLE

        // 1. Immediately query hardware LocationManager (fastest on-device cache)
        val lm = requireContext().getSystemService(Context.LOCATION_SERVICE) as? LocationManager
        val lastGps = try { lm?.getLastKnownLocation(LocationManager.GPS_PROVIDER) } catch (_: Exception) { null }
        val lastNet = try { lm?.getLastKnownLocation(LocationManager.NETWORK_PROVIDER) } catch (_: Exception) { null }
        val lastPassive = try { lm?.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER) } catch (_: Exception) { null }

        val bestInitial = listOfNotNull(lastGps, lastNet, lastPassive).maxByOrNull { it.time }
        if (bestInitial != null) {
            onNewLocationReceived(bestInitial, isImmediateFix = true)
        }

        // 2. Query FusedLocationProviderClient cached last location
        fusedLocationClient.lastLocation.addOnSuccessListener { lastLoc: Location? ->
            if (lastLoc != null) {
                val isNewer = currentUserLocation == null || lastLoc.time > (currentUserLocation?.time ?: 0)
                if (isNewer) {
                    onNewLocationReceived(lastLoc, isImmediateFix = true)
                }
            }
        }

        // 3. Request fresh high-accuracy location fix
        cancellationTokenSource?.cancel()
        val cts = CancellationTokenSource()
        cancellationTokenSource = cts

        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cts.token)
            .addOnSuccessListener { location: Location? ->
                pbMapLoading.visibility = View.GONE
                if (location != null) {
                    onNewLocationReceived(location, isImmediateFix = true)
                }
            }
            .addOnFailureListener {
                pbMapLoading.visibility = View.GONE
            }
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        if (!hasLocationPermission()) return

        val req = locationRequest ?: return
        val cb = locationCallback ?: return
        fusedLocationClient.requestLocationUpdates(req, cb, Looper.getMainLooper())

        // Hardware LocationManager listener fallback
        try {
            val lm = requireContext().getSystemService(Context.LOCATION_SERVICE) as? LocationManager
            if (systemLocationListener == null) {
                systemLocationListener = android.location.LocationListener { loc ->
                    onNewLocationReceived(loc, isImmediateFix = false)
                }
            }
            systemLocationListener?.let { listener ->
                if (lm?.isProviderEnabled(LocationManager.GPS_PROVIDER) == true) {
                    lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000L, 2f, listener, Looper.getMainLooper())
                }
                if (lm?.isProviderEnabled(LocationManager.NETWORK_PROVIDER) == true) {
                    lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 2000L, 2f, listener, Looper.getMainLooper())
                }
            }
        } catch (_: Exception) {}
    }

    private fun stopLocationUpdates() {
        locationCallback?.let { cb ->
            fusedLocationClient.removeLocationUpdates(cb)
        }
        systemLocationListener?.let { listener ->
            try {
                val lm = requireContext().getSystemService(Context.LOCATION_SERVICE) as? LocationManager
                lm?.removeUpdates(listener)
            } catch (_: Exception) {}
        }
        cancellationTokenSource?.cancel()
    }

    /**
     * Unified location receiver handler ensuring the map is centered on current location.
     */
    private fun onNewLocationReceived(location: Location, isImmediateFix: Boolean) {
        currentUserLocation = location
        updateLocationUI(location)

        // Centering policy: If user navigated here via Bottom Nav Map (destLat == null),
        // we MUST center on their current location on first fix or immediate request.
        val shouldCenter = (!hasCenteredOnUser && destLat == null) || isImmediateFix
        if (shouldCenter) {
            hasCenteredOnUser = true
        }

        if (isMapLoaded) {
            updateMapLocation(location.latitude, location.longitude, shouldCenter = shouldCenter, accuracy = location.accuracy)
            val dLat = destLat
            val dLng = destLng
            if (dLat != null && dLng != null) {
                plotRouteToDestination(dLat, dLng, destTitle, destAddress)
            }
        }
    }

    /**
     * Triggered when WebView Leaflet map has finished loading and is ready for coordinates.
     */
    private fun onMapReadyCallback() {
        isMapLoaded = true
        activity?.runOnUiThread {
            pbMapLoading.visibility = View.GONE
            val loc = currentUserLocation
            if (loc != null) {
                val shouldCenter = !hasCenteredOnUser || destLat == null
                hasCenteredOnUser = true
                updateMapLocation(loc.latitude, loc.longitude, shouldCenter = shouldCenter, accuracy = loc.accuracy)

                val dLat = destLat
                val dLng = destLng
                if (dLat != null && dLng != null) {
                    plotRouteToDestination(dLat, dLng, destTitle, destAddress)
                }
            } else {
                // If map loaded before GPS resolved, request GPS right away
                fetchInitialLocation()
            }
        }
    }

    @Suppress("DEPRECATION")
    private fun updateLocationUI(location: Location) {
        val lat = String.format(Locale.US, "%.4f", location.latitude)
        val lng = String.format(Locale.US, "%.4f", location.longitude)
        locationTextView.text = "📍 Lat: $lat, Lng: $lng"
        tvGpsLiveBadge.text = "LIVE GPS"

        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            try {
                val geocoder = Geocoder(requireContext(), Locale.ENGLISH)
                val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                val address = addresses?.firstOrNull()
                val city = address?.locality
                    ?: address?.subAdminArea
                    ?: address?.adminArea
                val label = if (city != null) "📍 $city\n$lat° N, $lng° E" else "📍 Lat: $lat, Lng: $lng"
                withContext(Dispatchers.Main) {
                    locationTextView.text = label
                }
            } catch (_: Exception) {}
        }
    }

    private fun updateMapLocation(lat: Double, lng: Double, shouldCenter: Boolean, accuracy: Float? = null) {
        if (!isMapLoaded) return
        val accParam = accuracy ?: 40f
        val js = "updateUserLocation($lat, $lng, $shouldCenter, $accParam);"
        mapWebView.evaluateJavascript(js, null)
    }

    private fun triggerDestinationSearch() {
        val query = etDestinationQuery.text.toString().trim()
        if (query.isBlank()) {
            Toast.makeText(requireContext(), "Please enter a destination to search", Toast.LENGTH_SHORT).show()
            return
        }

        pbMapLoading.visibility = View.VISIBLE

        viewLifecycleOwner.lifecycleScope.launch {
            val resolvedLocation = resolveDestinationWithGeocoder(query)
            pbMapLoading.visibility = View.GONE

            if (resolvedLocation != null) {
                val (lat, lng, title, address) = resolvedLocation
                destLat = lat
                destLng = lng
                destTitle = title
                destAddress = address

                plotRouteToDestination(lat, lng, title, address)
            } else {
                Toast.makeText(
                    requireContext(),
                    "Could not find coordinates for \"$query\". Please verify spelling.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun plotRouteToDestination(lat: Double, lng: Double, title: String, address: String) {
        val escapedTitle = title.replace("'", "\\'")
        val escapedAddress = address.replace("'", "\\'")
        val js = "plotDestinationAndRoute($lat, $lng, '$escapedTitle', '$escapedAddress');"
        mapWebView.evaluateJavascript(js, null)

        // Calculate Distance & ETA
        val userLoc = currentUserLocation
        val distanceKm = if (userLoc != null) {
            val results = FloatArray(1)
            Location.distanceBetween(userLoc.latitude, userLoc.longitude, lat, lng, results)
            results[0] / 1000.0
        } else {
            12.5
        }

        val formattedDistance = String.format(Locale.US, "%.1f km", distanceKm)
        val durationMinutes = (distanceKm / 40.0 * 60).toInt().coerceAtLeast(5)
        val formattedEta = if (durationMinutes >= 60) {
            "${durationMinutes / 60} hr ${durationMinutes % 60} mins"
        } else {
            "$durationMinutes mins"
        }

        tvDestTitle.text = title
        tvDestSubtitle.text = address
        tvRouteDistance.text = formattedDistance
        tvRouteEta.text = formattedEta
        cardRouteSummary.visibility = View.VISIBLE
    }

    @Suppress("DEPRECATION")
    private suspend fun resolveDestinationWithGeocoder(query: String): Quadruple<Double, Double, String, String>? =
        withContext(Dispatchers.IO) {
            try {
                val geocoder = Geocoder(requireContext(), Locale.ENGLISH)
                val addresses: List<Address>? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    var result: List<Address>? = null
                    geocoder.getFromLocationName("$query, Sri Lanka", 1) { addressesList ->
                        result = addressesList
                    }
                    var attempts = 0
                    while (result == null && attempts < 15) {
                        Thread.sleep(100)
                        attempts++
                    }
                    result
                } else {
                    geocoder.getFromLocationName("$query, Sri Lanka", 1)
                }

                if (!addresses.isNullOrEmpty()) {
                    val address = addresses[0]
                    val title = address.featureName ?: query
                    val fullAddress = address.getAddressLine(0) ?: "$title, Sri Lanka"
                    return@withContext Quadruple(address.latitude, address.longitude, title, fullAddress)
                }
            } catch (_: Exception) {}

            // Pre-programmed famous Sri Lanka tourist spots fallback
            val lower = query.lowercase(Locale.ROOT)
            return@withContext when {
                lower.contains("sigiriya") -> Quadruple(7.9570, 80.7603, "Sigiriya Rock Fortress", "Matale District, Central Province")
                lower.contains("galle") -> Quadruple(6.0329, 80.2168, "Galle Dutch Fort", "Galle, Southern Province")
                lower.contains("ella") -> Quadruple(6.8667, 81.0466, "Nine Arches Bridge, Ella", "Badulla District, Uva Province")
                lower.contains("kandy") -> Quadruple(7.2936, 80.6413, "Temple of the Sacred Tooth Relic", "Kandy, Central Province")
                lower.contains("mirissa") -> Quadruple(5.9482, 80.4578, "Mirissa Beach & Whale Watching", "Matara, Southern Province")
                lower.contains("yala") -> Quadruple(6.3725, 81.5173, "Yala National Park", "Hambantota, Southern Province")
                lower.contains("colombo") -> Quadruple(6.9271, 79.8612, "Galle Face Green, Colombo", "Western Province")
                lower.contains("nuwara eliya") -> Quadruple(6.9497, 80.7891, "Gregory Lake, Nuwara Eliya", "Central Province")
                lower.contains("trincomalee") -> Quadruple(8.5874, 81.2152, "Nilaveli Beach, Trincomalee", "Eastern Province")
                lower.contains("anuradhapura") -> Quadruple(8.3114, 80.4037, "Ruwanwelisaya, Anuradhapura", "North Central Province")
                else -> null
            }
        }

    private fun launchExternalNavigation(lat: Double, lng: Double, title: String) {
        try {
            val uri = Uri.parse("geo:$lat,$lng?q=$lat,$lng($title)")
            val intent = Intent(Intent.ACTION_VIEW, uri)
            startActivity(intent)
        } catch (_: Exception) {
            val webUri = Uri.parse("https://www.google.com/maps/dir/?api=1&destination=$lat,$lng")
            startActivity(Intent(Intent.ACTION_VIEW, webUri))
        }
    }

    private fun hideKeyboard() {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        view?.let { imm?.hideSoftInputFromWindow(it.windowToken, 0) }
    }

    override fun onResume() {
        super.onResume()
        mapWebView.onResume()
        if (hasLocationPermission() && isGpsEnabled()) {
            startLocationUpdates()
        }
    }

    override fun onPause() {
        super.onPause()
        mapWebView.onPause()
        stopLocationUpdates()
    }

    override fun onDestroyView() {
        mapWebView.destroy()
        super.onDestroyView()
    }

    /**
     * JavaScript Interface facilitating communication from Leaflet HTML page to Android.
     */
    inner class AndroidBridge {
        @JavascriptInterface
        fun onMapReady() {
            onMapReadyCallback()
        }

        @JavascriptInterface
        fun onHtml5LocationReceived(lat: Double, lng: Double) {
            activity?.runOnUiThread {
                if (currentUserLocation == null) {
                    val fallbackLoc = Location("HTML5_GEOLOCATION").apply {
                        latitude = lat
                        longitude = lng
                        time = System.currentTimeMillis()
                    }
                    onNewLocationReceived(fallbackLoc, isImmediateFix = true)
                }
            }
        }
    }

    private data class Quadruple<A, B, C, D>(
        val first: A,
        val second: B,
        val third: C,
        val fourth: D
    )
}
