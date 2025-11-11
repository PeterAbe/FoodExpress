package com.example.fooddelivery_project

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class GroupedCartAdapter(private val groupedItems: Map<String, List<CartItem>>) :
    RecyclerView.Adapter<GroupedCartAdapter.GroupedViewHolder>() {

    private val restaurantNames = groupedItems.keys.toList()

    class GroupedViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val restaurantName: TextView = itemView.findViewById(R.id.restaurantTitle)
        val itemsText: TextView = itemView.findViewById(R.id.restaurantItems)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupedViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_grouped_cart, parent, false)
        return GroupedViewHolder(view)
    }

    override fun onBindViewHolder(holder: GroupedViewHolder, position: Int) {
        val restaurant = restaurantNames[position]
        val items = groupedItems[restaurant] ?: emptyList()

        //Display the restaurant name
        holder.restaurantName.text = restaurant

        // Display each item line with quantity and price
        holder.itemsText.text = items.joinToString("\n") {
            "${it.name} x${it.quantity} - $${String.format("%.2f", it.price * it.quantity)}"
        }
    }

    override fun getItemCount(): Int = restaurantNames.size
}
