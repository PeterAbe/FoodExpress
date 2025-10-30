package com.example.fooddelivery_project

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.android.material.bottomnavigation.BottomNavigationView

class HomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        val profileButton = findViewById<ImageButton>(R.id.buttonProfile)
        val dealsBanner = findViewById<ImageView>(R.id.dealsBanner)
        val featuredContainer = findViewById<LinearLayout>(R.id.featuredContainer)

        // --- Deals banner ---
        Glide.with(this)
            .load("https://www.doncasterfreepress.co.uk/webimg/b25lY21zOjFmYzUxODIzLWU4MzYtNGY2OS05ZTk5LTE5YmU0ZDA1NjI5ZDphNmMwNjY5Ni02NmM5LTRlZDYtOTZhMy04NzBmODA3OGRmNDM=.jpg?crop=3:2,smart&trim=&width=640&quality=65")
            .into(dealsBanner)

        // --- Featured restaurants (hardcoded) ---
        val restaurantBanners = listOf(
            "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQiJ5cMOZUqLmUP_xykwtnWmPQFo6-0gYns6A&s",
            "https://d2w46d36moy248.cloudfront.net/media/dine/Burger_King.jpg",
            "https://loveincorporated.blob.core.windows.net/contentimages/gallery/a24d9978-8e34-4b04-93d8-24ce20757e69-98167cb1-de61-4169-a9a9-c81cd6d0b219-wendys-breakfast-menu.jpg"
        )

        for (url in restaurantBanners) {
            val imageView = ImageView(this)
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                180
            )
            params.setMargins(0, 12, 0, 12)
            imageView.layoutParams = params
            imageView.scaleType = ImageView.ScaleType.CENTER_CROP
            imageView.clipToOutline = true

            Glide.with(this).load(url).into(imageView)
            featuredContainer.addView(imageView)
        }

        // --- Bottom navigation ---
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> true
                R.id.nav_search -> {
                    startActivity(Intent(this, MenuActivity::class.java))
                    true
                }
                R.id.nav_cart -> {
                    startActivity(Intent(this, CartActivity::class.java))
                    true
                }
                R.id.nav_map -> {
                    startActivity(Intent(this, MapActivity::class.java))
                    true
                }
                else -> false
            }
        }

        profileButton.setOnClickListener {
            startActivity(Intent(this, AccountActivity::class.java))
        }
    }
}
