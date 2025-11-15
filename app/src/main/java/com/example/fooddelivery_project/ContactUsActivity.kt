package com.example.fooddelivery_project

import android.Manifest
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class ContactUsActivity : AppCompatActivity() {

    private lateinit var editIssue: EditText
    private lateinit var buttonVoiceNote: ImageButton
    private lateinit var buttonAddPhoto: ImageButton
    private lateinit var buttonSend: Button
    private lateinit var imagePreview: ImageView

    private var mediaRecorder: MediaRecorder? = null
    private var isRecording = false
    private lateinit var audioFilePath: String

    private var selectedImageUri: Uri? = null

    companion object {
        private const val RECORD_AUDIO_PERMISSION_CODE = 1001
    }

    // Image picker using Activity Result API
    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            if (uri != null) {
                selectedImageUri = uri
                imagePreview.visibility = View.VISIBLE
                imagePreview.setImageURI(uri)
                Toast.makeText(this, "Photo selected", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contact_us)

        // Toolbar / back arrow
        val toolbar = findViewById<Toolbar>(R.id.contactToolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Contact Us"

        editIssue = findViewById(R.id.editIssue)
        buttonVoiceNote = findViewById(R.id.buttonVoiceNote)
        buttonAddPhoto = findViewById(R.id.buttonAddPhoto)
        buttonSend = findViewById(R.id.buttonSend)
        imagePreview = findViewById(R.id.imagePreview)

        // where the audio will be saved (inside app cache)
        audioFilePath = "${externalCacheDir?.absolutePath}/contact_voice_note.3gp"

        // Voice note button: start/stop recording
        buttonVoiceNote.setOnClickListener {
            if (isRecording) {
                stopRecording()
            } else {
                // check permission first
                if (hasRecordAudioPermission()) {
                    startRecording()
                } else {
                    requestRecordAudioPermission()
                }
            }
        }

        // Photo button: open gallery
        buttonAddPhoto.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        // Send button: just validate and show confirmation for now
        buttonSend.setOnClickListener {
            val message = editIssue.text.toString().trim()

            if (message.isEmpty() && !isRecording && selectedImageUri == null) {
                Toast.makeText(
                    this,
                    "Please enter a message, record audio, or attach a photo",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                // For a school project, it's enough to just confirm
                Toast.makeText(this, "Your message has been sent. Thank you!", Toast.LENGTH_LONG)
                    .show()
                editIssue.text.clear()
                imagePreview.setImageDrawable(null)
                imagePreview.visibility = View.GONE
                selectedImageUri = null
            }
        }
    }

    // ---- Recording helpers ----

    private fun hasRecordAudioPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestRecordAudioPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.RECORD_AUDIO),
            RECORD_AUDIO_PERMISSION_CODE
        )
    }

    private fun startRecording() {
        mediaRecorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
            setOutputFile(audioFilePath)
            prepare()
            start()
        }
        isRecording = true
        Toast.makeText(this, "Recording started...", Toast.LENGTH_SHORT).show()
    }

    private fun stopRecording() {
        mediaRecorder?.apply {
            stop()
            reset()
            release()
        }
        mediaRecorder = null
        isRecording = false
        Toast.makeText(this, "Recording saved", Toast.LENGTH_SHORT).show()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == RECORD_AUDIO_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startRecording()
            } else {
                Toast.makeText(
                    this,
                    "Microphone permission is required to record a voice note",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaRecorder?.release()
        mediaRecorder = null
    }
}
