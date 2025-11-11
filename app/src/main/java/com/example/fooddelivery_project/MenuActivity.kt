package com.example.fooddelivery_project

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.firestore.FirebaseFirestore

class MenuActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private lateinit var recyclerView: RecyclerView
    private val restaurantList = mutableListOf<Restaurant>()
    private lateinit var adapter: RestaurantAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)

        // --- Bottom Navigation ---
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNav.selectedItemId = R.id.nav_search
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> startActivity(Intent(this, HomeActivity::class.java))
                R.id.nav_search -> {}
                R.id.nav_cart -> startActivity(Intent(this, CartActivity::class.java))
                R.id.nav_map -> startActivity(Intent(this, MapActivity::class.java))
            }
            true
        }

        // --- RecyclerView setup ---
        recyclerView = findViewById(R.id.recyclerViewRestaurants)  // make sure the ID matches your XML
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = RestaurantAdapter(restaurantList) { restaurant ->
            val intent = Intent(this, RestaurantMenuActivity::class.java)
            intent.putExtra("restaurantName", restaurant.name)
            startActivity(intent)
        }

        recyclerView.adapter = adapter


        // --- Search functionality ---
        val searchInput = findViewById<EditText>(R.id.editTextSearch)
        val searchBtn = findViewById<Button>(R.id.buttonSearch)

        searchBtn.setOnClickListener {
            val query = searchInput.text.toString().trim()
            if (query.isEmpty()) {
                searchInput.error = "Enter a restaurant or food name"
                return@setOnClickListener
            }

            // Try to match restaurant first
            db.collection("restaurants")
                .whereEqualTo("name", query)
                .get()
                .addOnSuccessListener { restaurantDocs ->
                    if (!restaurantDocs.isEmpty) {
                        val intent = Intent(this, RestaurantMenuActivity::class.java)
                        intent.putExtra("restaurantName", query)
                        startActivity(intent)
                    } else {
                        searchFoodItem(query, searchInput)
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Error searching. Try again.", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun searchFoodItem(query: String, searchInput: EditText) {
        db.collection("restaurants").get()
            .addOnSuccessListener { docs ->
                var found = false
                var pendingQueries = docs.size()

                if (pendingQueries == 0) {
                    searchInput.error = "No results found"
                }

                for (doc in docs) {
                    val menuRef = doc.reference.collection("menu")

                    menuRef.whereEqualTo("name", query).get()
                        .addOnSuccessListener { menuDocs ->
                            if (!menuDocs.isEmpty && !found) {
                                found = true
                                val intent = Intent(this, ItemActivity::class.java)
                                intent.putExtra("foodItem", query)
                                startActivity(intent)
                            }
                            pendingQueries--
                            if (pendingQueries == 0 && !found)
                                searchInput.error = "No results found"
                        }
                        .addOnFailureListener {
                            pendingQueries--
                            if (pendingQueries == 0 && !found)
                                searchInput.error = "No results found"
                        }

                    menuRef.whereEqualTo("category", query).get()
                        .addOnSuccessListener { categoryDocs ->
                            if (!categoryDocs.isEmpty && !found) {
                                found = true
                                val intent = Intent(this, ItemActivity::class.java)
                                intent.putExtra("foodItem", query)
                                startActivity(intent)
                            }
                            pendingQueries--
                            if (pendingQueries == 0 && !found)
                                searchInput.error = "No results found"
                        }
                        .addOnFailureListener {
                            pendingQueries--
                            if (pendingQueries == 0 && !found)
                                searchInput.error = "No results found"
                        }
                }
            }
    }
}
