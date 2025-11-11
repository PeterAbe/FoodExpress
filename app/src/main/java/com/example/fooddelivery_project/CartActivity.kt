package com.example.fooddelivery_project

import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class CartActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var totalText: TextView
    private lateinit var checkoutButton: Button
    private lateinit var adapter: CartAdapter

    private val db = FirebaseFirestore.getInstance()
    private val userId = FirebaseAuth.getInstance().currentUser?.uid
    private val groupedCartItems = mutableMapOf<String, MutableList<CartItem>>() // restaurant → items

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cart)

        // --- Bottom Navigation ---
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNav.selectedItemId = R.id.nav_cart
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> startActivity(Intent(this, HomeActivity::class.java))
                R.id.nav_search -> startActivity(Intent(this, MenuActivity::class.java))
                R.id.nav_cart -> {} // already here
                R.id.nav_map -> startActivity(Intent(this, MapActivity::class.java))
            }
            true
        }

        // --- Cart Setup ---
        recyclerView = findViewById(R.id.cartRecyclerView)
        totalText = findViewById(R.id.totalTextView)
        checkoutButton = findViewById(R.id.checkoutButton)
        recyclerView.layoutManager = LinearLayoutManager(this)

        if (userId == null) {
            Toast.makeText(this, "Please log in first.", Toast.LENGTH_SHORT).show()
            return
        }

        // --- Load Cart Items & Group by Restaurant ---
        db.collection("users").document(userId)
            .collection("cart")
            .addSnapshotListener { snapshot, e ->
                if (e != null || snapshot == null) {
                    Toast.makeText(this, "Error loading cart", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                groupedCartItems.clear()

                // Capture Firestore document IDs
                val allCartItems = snapshot.documents.mapNotNull { doc ->
                    val item = doc.toObject(CartItem::class.java)
                    item?.id = doc.id
                    item
                }

                // Group items by restaurant
                allCartItems.forEach { item ->
                    val restaurant = if (item.restaurant.isNotEmpty()) item.restaurant else "Unknown Restaurant"
                    groupedCartItems.getOrPut(restaurant) { mutableListOf() }.add(item)
                }

                // Flatten grouped items (headers + items)
                val displayList = mutableListOf<Any>()
                for ((restaurant, items) in groupedCartItems) {
                    displayList.add(restaurant)
                    displayList.addAll(items)
                }

                // Update adapter
                adapter = CartAdapter(displayList)
                recyclerView.adapter = adapter

                // Calculate total
                val total = allCartItems.sumOf { it.price * it.quantity }
                totalText.text = "Total: $${String.format("%.2f", total)}"
            }

        // --- Checkout Logic ---
        checkoutButton.setOnClickListener {
            db.collection("users").document(userId)
                .collection("cart")
                .get()
                .addOnSuccessListener { snapshot ->
                    val orderItems = snapshot.documents.map { it.data }
                    val total = orderItems.sumOf { (it?.get("price") as Double) }

                    val order = hashMapOf(
                        "items" to orderItems,
                        "total" to total,
                        "timestamp" to Timestamp.now()
                    )

                    db.collection("users").document(userId)
                        .collection("orders").add(order)
                        .addOnSuccessListener {
                            // Clear the cart after successful order
                            for (doc in snapshot) doc.reference.delete()
                            MediaPlayer.create(this, R.raw.order_success_chime).start()
                            Toast.makeText(this, "Order placed successfully!", Toast.LENGTH_SHORT).show()
                        }
                }
        }
    }
}