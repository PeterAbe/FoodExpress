package com.example.fooddelivery_project

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.fooddelivery_project.R

class RestaurantAdapter(
    private val restaurantList: List<Restaurant>,
    private val onItemClick: (Restaurant) -> Unit
) : RecyclerView.Adapter<RestaurantAdapter.RestaurantViewHolder>() {

    inner class RestaurantViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val restaurantName: TextView = itemView.findViewById(R.id.restaurantName)
        val restaurantAddress: TextView = itemView.findViewById(R.id.restaurantAddress)
        val restaurantRating: RatingBar = itemView.findViewById(R.id.restaurantRating)
        val restaurantLogo: ImageView = itemView.findViewById(R.id.restaurantLogo)
        val restaurantBanner: ImageView = itemView.findViewById(R.id.restaurantBanner)

        fun bind(restaurant: Restaurant) {
            restaurantName.text = restaurant.name
            restaurantAddress.text = restaurant.address
            restaurantRating.rating = restaurant.rating.toFloat()

            // Glide for logo
            Glide.with(itemView.context)
                .load(restaurant.logo)
                .placeholder(R.drawable.ic_placeholder_logo)
                .error(R.drawable.ic_error_logo)
                .centerCrop()
                .into(restaurantLogo)

            // Glide for banner
            Glide.with(itemView.context)
                .load(restaurant.bannerImage)
                .placeholder(R.drawable.ic_placeholder_banner)
                .error(R.drawable.ic_error_banner)
                .centerCrop()
                .into(restaurantBanner)

            itemView.setOnClickListener {
                onItemClick(restaurant)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RestaurantViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_restaurant, parent, false)
        return RestaurantViewHolder(view)
    }

    override fun onBindViewHolder(holder: RestaurantViewHolder, position: Int) {
        holder.bind(restaurantList[position])
    }

    override fun getItemCount(): Int = restaurantList.size
}
