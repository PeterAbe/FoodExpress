package com.example.fooddelivery_project

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.RectangularBounds
import com.google.android.libraries.places.api.model.TypeFilter
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.material.bottomnavigation.BottomNavigationView

class MapActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var placesClient: PlacesClient
    private val searchRadiusKm = 5.0

    // Only these restaurants are allowed
    private val allowedRestaurants = listOf(
        "Pizza Pizza",
        "Domino's Pizza",
        "McDonald's",
        "Wendy's",
        "Burger King",
        "Osmow’s Shawarma",
        "Taco Bell",
        "Popeyes",
        "Tim Hortons",
        "Starbucks"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        // Bottom navigation
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNav.selectedItemId = R.id.nav_map
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> startActivity(Intent(this, HomeActivity::class.java))
                R.id.nav_search -> startActivity(Intent(this, MenuActivity::class.java))
                R.id.nav_cart -> startActivity(Intent(this, CartActivity::class.java))
            }
            true
        }

        // Initialize Map & Location
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // Initialize Places SDK
        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, getString(R.string.google_maps_key))
        }
        placesClient = Places.createClient(this)

        setupSearchBar()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Enable zoom and gestures
        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.uiSettings.isZoomGesturesEnabled = true
        mMap.uiSettings.isScrollGesturesEnabled = true
        mMap.uiSettings.isTiltGesturesEnabled = true
        mMap.uiSettings.isRotateGesturesEnabled = true

        enableUserLocation()
    }

    private fun enableUserLocation() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            mMap.isMyLocationEnabled = true
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                1
            )
        }
    }

    private fun setupSearchBar() {
        val searchView = findViewById<SearchView>(R.id.searchBar)
        searchView.queryHint = "Search restaurants…"

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (query.isNullOrBlank()) {
                    Toast.makeText(this@MapActivity, "Enter a restaurant name", Toast.LENGTH_SHORT).show()
                    return false
                }

                val matchedName = matchRestaurant(query)
                if (matchedName == null) {
                    Toast.makeText(this@MapActivity, "Restaurant not found", Toast.LENGTH_SHORT).show()
                    return false
                }

                searchForNearbyRestaurants(matchedName)
                return true
            }

            override fun onQueryTextChange(newText: String?) = false
        })
    }

    private fun matchRestaurant(input: String): String? {
        val lower = input.lowercase()
        return allowedRestaurants.firstOrNull { it.lowercase().contains(lower) }
    }

    private fun searchForNearbyRestaurants(brandName: String) {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Toast.makeText(this, "Location permission not granted.", Toast.LENGTH_SHORT).show()
            return
        }

        // Request a fresh location
        fusedLocationClient.getCurrentLocation(
            LocationRequest.PRIORITY_HIGH_ACCURACY,
            null
        ).addOnSuccessListener { location: Location? ->
            if (location == null) {
                Toast.makeText(this, "Couldn't get current location.", Toast.LENGTH_LONG).show()
                return@addOnSuccessListener
            }

            val bounds = createBoundsForLocation(location.latitude, location.longitude, searchRadiusKm)

            val request = FindAutocompletePredictionsRequest.builder()
                .setQuery(brandName)
                .setLocationBias(bounds)
                .setTypeFilter(TypeFilter.ESTABLISHMENT)
                .build()

            placesClient.findAutocompletePredictions(request)
                .addOnSuccessListener { response ->

                    mMap.clear()
                    val predictions = response.autocompletePredictions
                    if (predictions.isEmpty()) {
                        Toast.makeText(this, "No nearby locations found.", Toast.LENGTH_SHORT).show()
                        return@addOnSuccessListener
                    }

                    val builder = LatLngBounds.Builder()

                    predictions.forEach { prediction ->
                        val placeId = prediction.placeId
                        val fields = listOf(
                            Place.Field.ID,
                            Place.Field.NAME,
                            Place.Field.LAT_LNG,
                            Place.Field.TYPES,
                            Place.Field.ADDRESS
                        )
                        val fetchRequest = FetchPlaceRequest.builder(placeId, fields).build()

                        placesClient.fetchPlace(fetchRequest)
                            .addOnSuccessListener { placeResponse ->
                                val place = placeResponse.place
                                val pos = place.latLng ?: return@addOnSuccessListener

                                val types = place.types ?: emptyList()
                                val isRestaurantLike = types.any {
                                    it == Place.Type.RESTAURANT ||
                                            it == Place.Type.CAFE ||
                                            it == Place.Type.FOOD ||
                                            it == Place.Type.BAKERY
                                }
                                if (!isRestaurantLike) return@addOnSuccessListener

                                mMap.addMarker(
                                    MarkerOptions()
                                        .position(pos)
                                        .title(place.name)
                                        .snippet(place.address)
                                )
                                builder.include(pos)

                                val bounds = builder.build()
                                mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100))
                            }
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Search failed: ${it.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun createBoundsForLocation(lat: Double, lng: Double, radiusKm: Double): RectangularBounds {
        val latOffset = radiusKm / 111.0
        val lngOffset = radiusKm / (111.0 * kotlin.math.cos(Math.toRadians(lat)))
        return RectangularBounds.newInstance(
            LatLng(lat - latOffset, lng - lngOffset),
            LatLng(lat + latOffset, lng + lngOffset)
        )
    }
}
