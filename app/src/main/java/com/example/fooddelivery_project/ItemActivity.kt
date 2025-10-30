package com.example.fooddelivery_project

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore

class ItemActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private val restaurantList = mutableListOf<Restaurant>()
    private val matchedCategories = mutableListOf<String>()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_item)

        recyclerView = findViewById(R.id.recyclerViewRestaurants)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val adapter = RestaurantAdapter(restaurantList) { restaurant ->
            val intent = Intent(this, RestaurantMenuActivity::class.java)
            intent.putExtra("restaurantName", restaurant.name)
            startActivity(intent)
        }

        recyclerView.adapter = adapter

        val foodItem = intent.getStringExtra("foodItem")

        if (foodItem != null) {
            db.collection("restaurants").get().addOnSuccessListener { docs ->
                restaurantList.clear()
                matchedCategories.clear()

                for (doc in docs) {
                    val restaurantName = doc.getString("name") ?: ""
                    val address = doc.getString("address") ?: ""
                    val rating = doc.getDouble("rating") ?: 0.0
                    val logo = doc.getString("logo") ?: ""
                    val bannerImage = doc.getString("bannerImage") ?: ""   // Added
                    val deliveryFee = doc.getDouble("deliveryFee") ?: 0.0

                    val menuRef = doc.reference.collection("menu")

                    // Check for exact item name match first
                    menuRef.whereEqualTo("name", foodItem).get()
                        .addOnSuccessListener { menuDocs ->
                            if (!menuDocs.isEmpty) {
                                val category = menuDocs.documents[0].getString("category") ?: ""
                                restaurantList.add(
                                    Restaurant(
                                        name = restaurantName,
                                        rating = rating,
                                        address = address,
                                        deliveryFee = deliveryFee,
                                        logo = logo,
                                        bannerImage = bannerImage    // Added
                                    )
                                )
                                matchedCategories.add(category)
                                adapter.notifyDataSetChanged()
                            } else {
                                // Check for exact category match
                                menuRef.whereEqualTo("category", foodItem).get()
                                    .addOnSuccessListener { categoryDocs ->
                                        if (!categoryDocs.isEmpty) {
                                            val category = categoryDocs.documents[0].getString("category") ?: ""
                                            restaurantList.add(
                                                Restaurant(
                                                    name = restaurantName,
                                                    rating = rating,
                                                    address = address,
                                                    deliveryFee = deliveryFee,
                                                    logo = logo,
                                                    bannerImage = bannerImage    // Added
                                                )
                                            )
                                            matchedCategories.add(category)
                                            adapter.notifyDataSetChanged()
                                        }
                                    }
                            }
                        }
                }

            }.addOnFailureListener {
                Toast.makeText(this, "Error loading data", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
