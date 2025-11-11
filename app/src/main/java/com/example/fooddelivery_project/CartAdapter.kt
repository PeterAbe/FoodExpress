package com.example.fooddelivery_project

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

private const val VIEW_TYPE_HEADER = 0
private const val VIEW_TYPE_ITEM = 1

class CartAdapter(private val displayList: List<Any>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val db = FirebaseFirestore.getInstance()
    private val userId = FirebaseAuth.getInstance().currentUser?.uid
    private val headerColors = listOf("#E3F2FD", "#F1F8E9", "#FFF3E0", "#FCE4EC", "#EDE7F6")

    // Header (Restaurant name)
    class HeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val restaurantName: TextView = itemView.findViewById(R.id.restaurantHeader)
    }

    // Cart item row
    class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val itemName: TextView = itemView.findViewById(R.id.itemName)
        val itemPrice: TextView = itemView.findViewById(R.id.itemPrice)
        val quantityText: TextView = itemView.findViewById(R.id.quantityText)
        val plusButton: Button = itemView.findViewById(R.id.increaseButton)
        val minusButton: Button = itemView.findViewById(R.id.decreaseButton)
        val removeButton: Button = itemView.findViewById(R.id.deleteButton)
    }

    override fun getItemViewType(position: Int): Int {
        return if (displayList[position] is String) VIEW_TYPE_HEADER else VIEW_TYPE_ITEM
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_HEADER) {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_cart_header, parent, false)
            HeaderViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_cart, parent, false)
            ItemViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is HeaderViewHolder) {
            val restaurant = displayList[position] as String
            holder.restaurantName.text = restaurant
            holder.itemView.setBackgroundColor(
                Color.parseColor(headerColors[position % headerColors.size])
            )
            return
        }

        val h = holder as ItemViewHolder
        val context = h.itemView.context
        val item = displayList[position] as CartItem
        val cartRef = db.collection("users").document(userId!!)
            .collection("cart").document(item.id!!)

        // Bind item info
        h.itemName.text = item.name
        h.itemPrice.text = String.format("$%.2f", item.price * item.quantity)
        h.quantityText.text = item.quantity.toString()

        // Increase quantity
        h.plusButton.setOnClickListener {
            val newQty = item.quantity + 1
            cartRef.update("quantity", newQty)
                .addOnSuccessListener {
                    Toast.makeText(context, "Increased to $newQty", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Failed to update quantity", Toast.LENGTH_SHORT).show()
                }
        }

        // Decrease quantity
        h.minusButton.setOnClickListener {
            val newQty = item.quantity - 1
            if (newQty > 0) {
                cartRef.update("quantity", newQty)
                    .addOnSuccessListener {
                        Toast.makeText(context, "Reduced to $newQty", Toast.LENGTH_SHORT).show()
                    }
            } else {
                cartRef.delete()
                    .addOnSuccessListener {
                        Toast.makeText(context, "${item.name} removed", Toast.LENGTH_SHORT).show()
                    }
            }
        }

        // Remove entire item
        h.removeButton.setOnClickListener {
            cartRef.delete()
                .addOnSuccessListener {
                    Toast.makeText(context, "${item.name} removed from cart", Toast.LENGTH_SHORT)
                        .show()
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Failed to remove item", Toast.LENGTH_SHORT).show()
                }
        }
    }

    override fun getItemCount(): Int = displayList.size
}
