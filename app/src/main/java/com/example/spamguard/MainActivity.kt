package com.example.spamguard

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView

class MainActivity : AppCompatActivity() {

    private lateinit var classifierEngine: SpamClassifierEngine

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(
                arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                1001
            )
        }

        classifierEngine = SpamClassifierEngine(this)

        val inputMessage = findViewById<EditText>(R.id.inputMessage)
        val classifyButton = findViewById<Button>(R.id.classifyButton)
        val resultText = findViewById<TextView>(R.id.resultText)

        classifyButton.setOnClickListener {

            val message = inputMessage.text.toString()

            val result = classifierEngine.predict(message)

            val label = result.first
            val confidence = result.second

            val formattedLabel = when(label) {
                "spam" -> "Spam"
                "potential_spam" -> "Potential Spam"
                else -> "Ham"
            }

            resultText.text = "Prediction: $formattedLabel - $confidence%"
        }
    }
}
