package com.example.fooddelivery_project

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

data class MenuItem(
    val name: String = "",
    val description: String = "",
    val price: Double = 0.0,
    val imageUrl: String = "",
    var restaurant: String = ""

)

class MenuAdapter(private val menuItems: List<MenuItem>) :
    RecyclerView.Adapter<MenuAdapter.MenuViewHolder>() {

    private val db = FirebaseFirestore.getInstance()

    class MenuViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val itemImage: ImageView = itemView.findViewById(R.id.itemImage)
        val itemName: TextView = itemView.findViewById(R.id.itemName)
        val itemDesc: TextView = itemView.findViewById(R.id.itemDescription)
        val itemPrice: TextView = itemView.findViewById(R.id.itemPrice)
        val addButton: Button = itemView.findViewById(R.id.addToCartButton)
        val plusButton: Button = itemView.findViewById(R.id.increaseButton)
        val minusButton: Button = itemView.findViewById(R.id.decreaseButton)
        val quantityText: TextView = itemView.findViewById(R.id.quantityText)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MenuViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_menu, parent, false)
        return MenuViewHolder(view)
    }

    override fun onBindViewHolder(holder: MenuViewHolder, position: Int) {
        val item = menuItems[position]
        val context = holder.itemView.context
        var quantity = 0

        holder.itemName.text = item.name
        holder.itemDesc.text = item.description
        holder.itemPrice.text = "$${item.price}"

        Glide.with(context).load(item.imageUrl).into(holder.itemImage)

        fun updateUI() {
            if (quantity > 0) {
                holder.addButton.visibility = View.GONE
                holder.plusButton.visibility = View.VISIBLE
                holder.minusButton.visibility = View.VISIBLE
                holder.quantityText.visibility = View.VISIBLE
                holder.quantityText.text = quantity.toString()
            } else {
                holder.addButton.visibility = View.VISIBLE
                holder.plusButton.visibility = View.GONE
                holder.minusButton.visibility = View.GONE
                holder.quantityText.visibility = View.GONE
            }
        }

        // Load existing quantity
        val currentUser = FirebaseAuth.getInstance().currentUser?.uid ?: "test_user_123"
        db.collection("users").document(currentUser)
            .collection("cart")
            .whereEqualTo("name", item.name)
            .get()
            .addOnSuccessListener { snapshot ->
                if (!snapshot.isEmpty) {
                    quantity = (snapshot.documents[0].getLong("quantity") ?: 1L).toInt()
                }
                updateUI()
            }

        // Add button
        holder.addButton.setOnClickListener {
            val userId = FirebaseAuth.getInstance().currentUser?.uid ?: "test_user_123"
            Log.d("CART_DEBUG", "User ID: $userId")

            val cartRef = db.collection("users").document(userId).collection("cart")
            cartRef.whereEqualTo("name", item.name).get()
                .addOnSuccessListener { snapshot ->
                    if (snapshot.isEmpty) {
                        val cartItem = hashMapOf(
                            "name" to item.name,
                            "price" to item.price,
                            "quantity" to 1,
                            "restaurant" to item.restaurant
                        )
                        cartRef.add(cartItem)
                        quantity = 1
                        updateUI()
                        Toast.makeText(context, "${item.name} added to cart", Toast.LENGTH_SHORT).show()
                    }
                }
        }

        // Increase quantity
        holder.plusButton.setOnClickListener {
            val userId = FirebaseAuth.getInstance().currentUser?.uid ?: "test_user_123"
            val cartRef = db.collection("users").document(userId).collection("cart")

            cartRef.whereEqualTo("name", item.name).get()
                .addOnSuccessListener { snapshot ->
                    if (!snapshot.isEmpty) {
                        val doc = snapshot.documents[0].reference
                        quantity++
                        doc.update("quantity", quantity)
                        updateUI()
                    }
                }
        }

        // Decrease quantity
        holder.minusButton.setOnClickListener {
            val userId = FirebaseAuth.getInstance().currentUser?.uid ?: "test_user_123"
            val cartRef = db.collection("users").document(userId).collection("cart")

            cartRef.whereEqualTo("name", item.name).get()
                .addOnSuccessListener { snapshot ->
                    if (!snapshot.isEmpty) {
                        val doc = snapshot.documents[0].reference
                        quantity--
                        if (quantity <= 0) {
                            doc.delete()
                            quantity = 0
                            Toast.makeText(context, "${item.name} removed from cart", Toast.LENGTH_SHORT).show()
                        } else {
                            doc.update("quantity", quantity)
                        }
                        updateUI()
                    }
                }
        }

        updateUI()
    }

    override fun getItemCount(): Int = menuItems.size
}
