package com.example.fooddelivery_project

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.android.material.bottomnavigation.BottomNavigationView
import android.net.Uri
import android.widget.VideoView
import android.graphics.Color
import android.graphics.Typeface
import android.util.TypedValue

import android.widget.TextView

import android.media.MediaPlayer


class HomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        val profileButton = findViewById<ImageButton>(R.id.buttonProfile)

        val featuredContainer = findViewById<LinearLayout>(R.id.featuredContainer)

        val dealsVideo = findViewById<VideoView>(R.id.dealsVideo)

        val videoPath = "android.resource://" + packageName + "/" + R.raw.restaurant_atmosphere
        dealsVideo.setVideoURI(Uri.parse(videoPath))

        dealsVideo.setOnPreparedListener { mp ->
            mp.isLooping = true
            mp.setVolume(0f, 0f)  // mute optional
            dealsVideo.start()
        }


        // --- Featured restaurants (hardcoded) ---

        data class Restaurant(val name: String, val imageUrl: String)

        val restaurants = listOf(
            Restaurant("McDonald's", "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQiJ5cMOZUqLmUP_xykwtnWmPQFo6-0gYns6A&s"),
            Restaurant("Burger King", "https://d2w46d36moy248.cloudfront.net/media/dine/Burger_King.jpg"),
            Restaurant("Wendy's", "https://loveincorporated.blob.core.windows.net/contentimages/gallery/a24d9978-8e34-4b04-93d8-24ce20757e69-98167cb1-de61-4169-a9a9-c81cd6d0b219-wendys-breakfast-menu.jpg")
        )


        for (restaurant in restaurants) {

            // Parent layout (vertical)
            val itemLayout = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 24, 0, 24)
                }
            }

            // ImageView
            val imageView = ImageView(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    180   // same height you were using
                )
                scaleType = ImageView.ScaleType.CENTER_CROP
                clipToOutline = true
            }

            // Load image with Glide
            Glide.with(this).load(restaurant.imageUrl).into(imageView)

            // TextView for restaurant name
            val nameView = TextView(this).apply {
                text = restaurant.name
                textSize = 16f
                setTypeface(null, Typeface.BOLD)
                setTextColor(Color.BLACK)
                setPadding(8, 8, 8, 16)
            }

            // Add views to parent layout
            itemLayout.addView(imageView)
            itemLayout.addView(nameView)

            // Add item to the container in your XML
            featuredContainer.addView(itemLayout)
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
