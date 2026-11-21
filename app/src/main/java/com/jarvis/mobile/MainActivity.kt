package com.jarvis.mobile

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Settings
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.widget.Button
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.util.Locale

class MainActivity : Activity(), TextToSpeech.OnInitListener {
    private lateinit var statusText: TextView
    private lateinit var transcriptText: TextView
    private lateinit var listenButton: Button
    private lateinit var tts: TextToSpeech
    private var recognizer: SpeechRecognizer? = null
    private var isListening = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        statusText = findViewById(R.id.statusText)
        transcriptText = findViewById(R.id.transcriptText)
        listenButton = findViewById(R.id.listenButton)
        tts = TextToSpeech(this, this)
        findViewById<Button>(R.id.accessibilityButton).setOnClickListener { startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) }
        listenButton.setOnClickListener { if (isListening) stopListening() else requestMicAndListen() }
        if (!SpeechRecognizer.isRecognitionAvailable(this)) statusText.text = "Speech recognition is not available on this device."
        else { recognizer = SpeechRecognizer.createSpeechRecognizer(this); recognizer?.setRecognitionListener(listener) }
    }

    private fun requestMicAndListen() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), 21); return
        }
        startListening()
    }
    private fun startListening() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US")
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false)
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Say a J.A.R.V.I.S command")
        }
        isListening = true; listenButton.text = "STOP LISTENING"; statusText.text = "Listening…"
        recognizer?.startListening(intent)
    }
    private fun stopListening() {
        isListening = false; listenButton.text = "START LISTENING"; recognizer?.cancel(); statusText.text = "Listening stopped."
    }
    private val listener = object : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) {}
        override fun onBeginningOfSpeech() { statusText.text = "Hearing you…" }
        override fun onRmsChanged(rmsdB: Float) {}
        override fun onBufferReceived(buffer: ByteArray?) {}
        override fun onEndOfSpeech() { statusText.text = "Processing command…" }
        override fun onError(error: Int) { isListening = false; listenButton.text = "START LISTENING"; statusText.text = "Speech error ($error). Tap Start Listening to retry." }
        override fun onResults(results: Bundle?) {
            isListening = false; listenButton.text = "START LISTENING"
            val phrase = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull().orEmpty()
            transcriptText.text = "Heard: $phrase"
            if (phrase.isNotBlank()) handleCommand(phrase)
        }
        override fun onPartialResults(partialResults: Bundle?) {}
        override fun onEvent(eventType: Int, params: Bundle?) {}
    }
    private fun handleCommand(raw: String) {
        val command = raw.lowercase(Locale.ROOT).replace("hey jarvis", "").replace("jarvis", "").trim()
        when {
            command.contains("go home") || command == "home" -> { JarvisAccessibilityService.instance?.performHome(); respond("Going home.") }
            command.contains("go back") || command == "back" -> { JarvisAccessibilityService.instance?.performBack(); respond("Going back.") }
            command.contains("recent apps") || command.contains("show recent") -> { JarvisAccessibilityService.instance?.performRecents(); respond("Opening recent apps.") }
            command.contains("scroll down") -> { JarvisAccessibilityService.instance?.scrollDown(); respond("Scrolling down.") }
            command.contains("scroll up") -> { JarvisAccessibilityService.instance?.scrollUp(); respond("Scrolling up.") }
            command.startsWith("tap ") || command.startsWith("click ") -> {
                val target = command.substringAfter(" ").trim()
                val success = JarvisAccessibilityService.instance?.clickText(target) == true
                respond(if (success) "Tapped $target." else "I couldn't find an accessible item named $target.")
            }
            command.startsWith("open ") || command.startsWith("launch ") -> launchApp(command.removePrefix("open ").removePrefix("launch ").trim())
            else -> respond("I don't know that command yet. Try open WhatsApp, go home, go back, or scroll down.")
        }
    }
    private fun launchApp(name: String) {
        val apps = mapOf("whatsapp" to "com.whatsapp", "youtube" to "com.google.android.youtube", "gmail" to "com.google.android.gm", "chrome" to "com.android.chrome", "google maps" to "com.google.android.apps.maps", "maps" to "com.google.android.apps.maps", "messages" to "com.google.android.apps.messaging", "settings" to "com.android.settings", "phone" to "com.google.android.dialer")
        val packageName = apps[name]
        if (packageName == null) { respond("I don't have an app mapping for $name yet."); return }
        val launch = packageManager.getLaunchIntentForPackage(packageName)
        if (launch == null) respond("$name is not installed or Android didn't expose its launch activity.")
        else { launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); startActivity(launch); respond("Opening $name.") }
    }
    private fun respond(message: String) {
        statusText.text = message
        if (::tts.isInitialized) tts.speak(message, TextToSpeech.QUEUE_FLUSH, null, "jarvis-response")
    }
    override fun onInit(status: Int) { if (status == TextToSpeech.SUCCESS) tts.language = Locale.US }
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 21 && grantResults.firstOrNull() == PackageManager.PERMISSION_GRANTED) startListening()
        else statusText.text = "Microphone permission is required for voice commands."
    }
    override fun onDestroy() { recognizer?.destroy(); if (::tts.isInitialized) tts.shutdown(); super.onDestroy() }
}
