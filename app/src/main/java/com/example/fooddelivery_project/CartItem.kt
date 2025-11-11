package com.example.fooddelivery_project
data class CartItem(
    var id: String? = null,
    var name: String = "",
    var price: Double = 0.0,
    var quantity: Int = 1,
    var restaurant: String = ""
)
