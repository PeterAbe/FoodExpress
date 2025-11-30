package com.example.fooddelivery_project

import android.Manifest
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import java.io.File

class ContactUsActivity : AppCompatActivity() {

    private lateinit var editIssue: EditText
    private lateinit var buttonVoiceNote: ImageButton
    private lateinit var buttonAddPhoto: ImageButton
    private lateinit var buttonSend: Button
    private lateinit var imagePreview: ImageView
    private lateinit var recordingTimerText: TextView

    private var mediaRecorder: MediaRecorder? = null
    private var isRecording = false
    private var hasRecordedAudio = false
    private lateinit var audioFilePath: String

    private var selectedImageUri: Uri? = null

    // Timer fields
    private val timerHandler = Handler(Looper.getMainLooper())
    private var recordingStartTime: Long = 0L
    private var lastRecordingDurationMs: Long = 0L

    private val timerRunnable = object : Runnable {
        override fun run() {
            val elapsed = System.currentTimeMillis() - recordingStartTime
            val seconds = (elapsed / 1000).toInt()
            val minutes = seconds / 60
            val remainingSeconds = seconds % 60
            recordingTimerText.text = String.format("%02d:%02d", minutes, remainingSeconds)
            timerHandler.postDelayed(this, 1000)
        }
    }

    // Firebase (Firestore only, no Storage)
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

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
        recordingTimerText = findViewById(R.id.textRecordingTimer)

        // Firebase
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // where the audio will be saved (inside app cache)
        audioFilePath = "${externalCacheDir?.absolutePath}/contact_voice_note.3gp"

        // Voice note button: start/stop recording
        buttonVoiceNote.setOnClickListener {
            if (isRecording) {
                stopRecording()
            } else {
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

        // Send button: save metadata + local paths to Firestore
        buttonSend.setOnClickListener {
            val message = editIssue.text.toString().trim()

            if (message.isEmpty() && !hasRecordedAudio && selectedImageUri == null) {
                Toast.makeText(
                    this,
                    "Please enter a message, record audio, or attach a photo",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            val userId = auth.currentUser?.uid ?: "anonymous"

            buttonSend.isEnabled = false
            Toast.makeText(this, "Sending issue...", Toast.LENGTH_SHORT).show()

            val ticket = hashMapOf(
                "userId" to userId,
                "issueText" to message,
                "hasImage" to (selectedImageUri != null),
                "imageLocalUri" to (selectedImageUri?.toString() ?: ""),
                "hasAudio" to hasRecordedAudio,
                "audioLocalPath" to if (hasRecordedAudio) audioFilePath else "",
                "recordingDurationMs" to lastRecordingDurationMs,
                "createdAt" to FieldValue.serverTimestamp(),
                "status" to "open"
            )

            db.collection("supportTickets")
                .add(ticket)
                .addOnSuccessListener {
                    Toast.makeText(
                        this,
                        "Your message has been saved. Thank you!",
                        Toast.LENGTH_LONG
                    ).show()

                    // Reset UI
                    editIssue.text.clear()
                    imagePreview.setImageDrawable(null)
                    imagePreview.visibility = View.GONE
                    selectedImageUri = null
                    hasRecordedAudio = false
                    lastRecordingDurationMs = 0L
                    recordingTimerText.text = ""
                    recordingTimerText.visibility = View.GONE

                    buttonSend.isEnabled = true
                }
                .addOnFailureListener { e ->
                    Toast.makeText(
                        this,
                        "Failed to save ticket: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                    buttonSend.isEnabled = true
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
        hasRecordedAudio = false

        recordingStartTime = System.currentTimeMillis()
        recordingTimerText.visibility = View.VISIBLE
        recordingTimerText.text = "00:00"
        timerHandler.post(timerRunnable)

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
        hasRecordedAudio = true

        timerHandler.removeCallbacks(timerRunnable)

        val elapsed = System.currentTimeMillis() - recordingStartTime
        lastRecordingDurationMs = elapsed

        val seconds = (elapsed / 1000).toInt()
        val minutes = seconds / 60
        val remainingSeconds = seconds % 60
        val finalText = String.format("%02d:%02d", minutes, remainingSeconds)
        recordingTimerText.text = "Recorded: $finalText"

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
        timerHandler.removeCallbacks(timerRunnable)
    }
}
