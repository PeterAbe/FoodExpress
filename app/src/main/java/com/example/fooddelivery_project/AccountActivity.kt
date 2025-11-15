package com.example.fooddelivery_project

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.firebase.auth.FirebaseAuth

class AccountActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account)

        // Toolbar
        val toolbar = findViewById<Toolbar>(R.id.accountToolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Account"

        auth = FirebaseAuth.getInstance()
        val user = auth.currentUser

        // Top info
        val emailText = findViewById<TextView>(R.id.textEmail)
        val logoutBtn = findViewById<Button>(R.id.buttonLogout)

        // Buttons
        val buttonAbout = findViewById<Button>(R.id.buttonAbout)
        val buttonEditProfile = findViewById<Button>(R.id.buttonEditProfile)
        val buttonOrders = findViewById<Button>(R.id.buttonOrders)
        val buttonContactUs = findViewById<Button>(R.id.buttonContactUs)

        // Dropdown layouts
        val layoutAbout = findViewById<View>(R.id.layoutAbout)
        val layoutEdit = findViewById<View>(R.id.layoutEdit)
        val layoutOrders = findViewById<View>(R.id.layoutOrders)

        // Edit fields
        val editName = findViewById<EditText>(R.id.editName)
        val editEmail = findViewById<EditText>(R.id.editEmail)
        val editPhone = findViewById<EditText>(R.id.editPhone)
        val buttonSaveProfile = findViewById<Button>(R.id.buttonSaveProfile)

        // Set initial email
        emailText.text = user?.email ?: "Unknown"
        editEmail.setText(user?.email ?: "")

        logoutBtn.setOnClickListener {
            auth.signOut()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        // Helper: only one section open at a time
        fun showSection(section: View) {
            val views = listOf(layoutAbout, layoutEdit, layoutOrders)
            views.forEach { view ->
                view.visibility = if (view == section && view.visibility == View.GONE) {
                    View.VISIBLE
                } else if (view == section && view.visibility == View.VISIBLE) {
                    View.GONE   // clicking again hides it
                } else {
                    View.GONE
                }
            }
        }

        buttonAbout.setOnClickListener {
            showSection(layoutAbout)
        }

        buttonEditProfile.setOnClickListener {
            showSection(layoutEdit)
        }

         buttonOrders.setOnClickListener {
            showSection(layoutOrders)
        }

        buttonContactUs.setOnClickListener {
            startActivity(Intent(this, ContactUsActivity::class.java))
        }

        // Save profile (for now just show a Toast – you can connect Firebase later)
        buttonSaveProfile.setOnClickListener {
            val name = editName.text.toString().trim()
            val email = editEmail.text.toString().trim()
            val phone = editPhone.text.toString().trim()

            // For the project demo, we just update the label and show a message
            if (email.isNotEmpty()) {
                emailText.text = email
            }
            Toast.makeText(
                this,
                "Profile updated (local only for demo)",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}
