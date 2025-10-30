package com.example.fooddelivery_project

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.firestore.FirebaseFirestore

class MenuActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNav.selectedItemId = R.id.nav_search

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> startActivity(Intent(this, HomeActivity::class.java))
                R.id.nav_search -> {} // already here
                R.id.nav_cart -> startActivity(Intent(this, CartActivity::class.java))
                R.id.nav_map -> startActivity(Intent(this, MapActivity::class.java))
            }
            true
        }

        val searchInput = findViewById<EditText>(R.id.editTextSearch)
        val searchBtn = findViewById<Button>(R.id.buttonSearch)

        searchBtn.setOnClickListener {
            val query = searchInput.text.toString().trim()
            if (query.isEmpty()) {
                searchInput.error = "Enter a restaurant or food name"
                return@setOnClickListener
            }

            // Step 1: Check for restaurant name match
            db.collection("restaurants")
                .whereEqualTo("name", query)
                .get()
                .addOnSuccessListener { restaurantDocs ->
                    if (!restaurantDocs.isEmpty) {
                        // Restaurant found → go to RestaurantMenuActivity
                        val intent = Intent(this, RestaurantMenuActivity::class.java)
                        intent.putExtra("restaurantName", query)
                        startActivity(intent)
                    } else {
                        // Step 2: Check for food item name or category match
                        db.collection("restaurants").get()
                            .addOnSuccessListener { docs ->
                                var found = false
                                var pendingQueries = docs.size()  // track remaining queries

                                if (pendingQueries == 0) {
                                    searchInput.error = "No results found"
                                }

                                for (doc in docs) {
                                    val menuRef = doc.reference.collection("menu")

                                    // Check for exact food name match
                                    menuRef.whereEqualTo("name", query).get()
                                        .addOnSuccessListener { menuDocs ->
                                            if (!menuDocs.isEmpty && !found) {
                                                found = true
                                                val intent = Intent(this, ItemActivity::class.java)
                                                intent.putExtra("foodItem", query)
                                                startActivity(intent)
                                            }
                                            pendingQueries--
                                            if (pendingQueries == 0 && !found) {
                                                searchInput.error = "No results found"
                                            }
                                        }
                                        .addOnFailureListener { pendingQueries--; if (pendingQueries == 0 && !found) searchInput.error = "No results found" }

                                    // Also check category match
                                    menuRef.whereEqualTo("category", query).get()
                                        .addOnSuccessListener { categoryDocs ->
                                            if (!categoryDocs.isEmpty && !found) {
                                                found = true
                                                val intent = Intent(this, ItemActivity::class.java)
                                                intent.putExtra("foodItem", query)
                                                startActivity(intent)
                                            }
                                            pendingQueries--
                                            if (pendingQueries == 0 && !found) {
                                                searchInput.error = "No results found"
                                            }
                                        }
                                        .addOnFailureListener { pendingQueries--; if (pendingQueries == 0 && !found) searchInput.error = "No results found" }
                                }
                            }
                    }
                }
        }
    }
}
