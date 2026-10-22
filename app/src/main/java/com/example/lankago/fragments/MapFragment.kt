package com.example.lankago.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.lankago.R
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
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
 * 100% Free - Requires NO API Key, NO Credit Card, NO Cloud Billing.
 * 
 * Features:
 * 1. Interactive full-screen OpenStreetMap with custom tiles and smooth zoom.
 * 2. Real-time GPS location tracking via FusedLocationProviderClient + LocationCallback.
 * 3. Live coordinate display (Latitude & Longitude) on UI.
 * 4. Destination Geocoder search and animated route plotting.
 * 5. Re-center to user location, distance & ETA calculator, and turn-by-turn navigation launcher.
 */
open class MapFragment : Fragment() {

    private lateinit var mapWebView: WebView
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var isMapLoaded: Boolean = false

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
    private lateinit var locationTextView: TextView

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
    private var isFirstLocationFix: Boolean = true

    // Permission Launcher
    private val locationPermissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false

        if (fineGranted || coarseGranted) {
            startLocationUpdates()
            fetchInitialLocation()
        } else {
            Toast.makeText(
                requireContext(),
                "Location permission denied. Showing Colombo Fort as default.",
                Toast.LENGTH_SHORT
            ).show()
            val fallback = Location("fallback").apply {
                latitude = 6.9344
                longitude = 79.8428
            }
            currentUserLocation = fallback
            updateLocationUI(fallback)
            updateMapLocation(fallback.latitude, fallback.longitude, shouldCenter = true)
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
        locationTextView = view.findViewById(R.id.locationTextView)

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
                isMapLoaded = true
                currentUserLocation?.let { loc ->
                    updateMapLocation(loc.latitude, loc.longitude, shouldCenter = true)
                }
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

        fabRecenterLocation.setOnClickListener {
            mapWebView.evaluateJavascript("recenterToUser();", null)
            Toast.makeText(requireContext(), "📍 Centering on your location", Toast.LENGTH_SHORT).show()
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
        locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000)
            .setMinUpdateIntervalMillis(5000)
            .build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                for (location in locationResult.locations) {
                    currentUserLocation = location
                    updateLocationUI(location)
                    updateMapLocation(location.latitude, location.longitude, shouldCenter = isFirstLocationFix)
                    if (isFirstLocationFix) {
                        isFirstLocationFix = false
                    }
                }
            }
        }
    }

    private fun checkLocationPermissionAndStart() {
        val fineGranted = ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarseGranted = ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (fineGranted || coarseGranted) {
            startLocationUpdates()
            fetchInitialLocation()
        } else {
            locationPermissionsLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    @SuppressLint("MissingPermission")
    private fun fetchInitialLocation() {
        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
            .addOnSuccessListener { location: Location? ->
                if (location != null) {
                    currentUserLocation = location
                    updateLocationUI(location)
                    updateMapLocation(location.latitude, location.longitude, shouldCenter = true)
                } else {
                    fusedLocationClient.lastLocation.addOnSuccessListener { lastLoc: Location? ->
                        val loc = lastLoc ?: Location("fallback").apply {
                            latitude = 6.9344
                            longitude = 79.8428
                        }
                        currentUserLocation = loc
                        updateLocationUI(loc)
                        updateMapLocation(loc.latitude, loc.longitude, shouldCenter = true)
                    }
                }
            }
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        val fineGranted = ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarseGranted = ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (fineGranted || coarseGranted) {
            val req = locationRequest ?: return
            val cb = locationCallback ?: return
            fusedLocationClient.requestLocationUpdates(req, cb, Looper.getMainLooper())
        }
    }

    private fun stopLocationUpdates() {
        locationCallback?.let { cb ->
            fusedLocationClient.removeLocationUpdates(cb)
        }
    }

    private fun updateLocationUI(location: Location) {
        val lat = String.format(Locale.US, "%.5f", location.latitude)
        val lng = String.format(Locale.US, "%.5f", location.longitude)
        locationTextView.text = "Latitude: $lat\nLongitude: $lng"
    }

    private fun updateMapLocation(lat: Double, lng: Double, shouldCenter: Boolean) {
        if (!isMapLoaded) return
        val js = "updateUserLocation($lat, $lng, $shouldCenter);"
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
        startLocationUpdates()
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

    private data class Quadruple<A, B, C, D>(
        val first: A,
        val second: B,
        val third: C,
        val fourth: D
    )
}
