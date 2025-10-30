package com.example.fooddelivery_project

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore

class RestaurantMenuActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var restaurantNameText: TextView
    private lateinit var restaurantBanner: ImageView
    private val menuItems = mutableListOf<MenuItem>()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_restaurant_menu)

        restaurantNameText = findViewById(R.id.restaurantName)
        restaurantBanner = findViewById(R.id.restaurantBanner)
        recyclerView = findViewById(R.id.recyclerViewMenu)

        recyclerView.layoutManager = LinearLayoutManager(this)
        val adapter = MenuAdapter(menuItems)
        recyclerView.adapter = adapter

        val restaurantName = intent.getStringExtra("restaurantName")
        restaurantNameText.text = restaurantName ?: "Menu"

        if (restaurantName != null) {
            db.collection("restaurants")
                .whereEqualTo("name", restaurantName)
                .get()
                .addOnSuccessListener { documents ->
                    for (doc in documents) {
                        val bannerUrl = doc.getString("bannerImage")
                        Glide.with(this).load(bannerUrl).into(restaurantBanner)

                        val menuRef = doc.reference.collection("menu")
                        menuRef.get().addOnSuccessListener { menuDocs ->
                            menuItems.clear()
                            for (menuDoc in menuDocs) {
                                val item = menuDoc.toObject(MenuItem::class.java)
                                menuItems.add(item)
                            }
                            adapter.notifyDataSetChanged()
                        }
                    }
                }
        }
    }
}
