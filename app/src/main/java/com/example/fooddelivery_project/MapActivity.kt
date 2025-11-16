package com.example.fooddelivery_project

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.bottomnavigation.BottomNavigationView

class MapActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        // --- Setup Bottom Navigation ---
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNav.selectedItemId = R.id.nav_map

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> startActivity(Intent(this, HomeActivity::class.java))
                R.id.nav_search -> startActivity(Intent(this, MenuActivity::class.java))
                R.id.nav_cart -> startActivity(Intent(this, CartActivity::class.java))
                R.id.nav_map -> { /* already here */ }
            }
            true
        }

        // --- Setup Map ---
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Add restaurant markers FIRST
        val restaurants = listOf(
            LatLng(43.89865, -78.91307) to "Pizza Pizza",
            LatLng(43.90771, -78.82020) to "Domino's Pizza",
            LatLng(43.89855, -78.84622) to "McDonald's",
            LatLng(43.89659, -78.87526) to "Wendy's",
            LatLng(43.89460, -78.87560) to "Burger King",
            LatLng(43.89849, -78.86167) to "Osmow’s Shawarma",
            LatLng(43.90103, -78.86507) to "Taco Bell",
            LatLng(43.91984, -78.85820) to "Popeyes Louisiana Kitchen",
            LatLng(43.89434, -78.86548) to "Tim Hortons",
            LatLng(43.88700, -78.85900) to "Starbucks"
        )

        for ((location, name) in restaurants) {
            mMap.addMarker(
                MarkerOptions()
                    .position(location)
                    .title(name)
            )
        }

        // Move camera to one of the restaurants FIRST
        val firstRestaurant = restaurants[0].first
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(firstRestaurant, 13f))

        // THEN enable user location (blue dot), but DO NOT move the camera
        enableUserLocation()
    }

    private fun enableUserLocation() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            mMap.isMyLocationEnabled = true
            // No manual camera move here, so restaurants stay in view
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                1
            )
        }
    }
}
