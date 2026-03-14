package com.example.spamguard

import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import androidx.core.app.NotificationCompat

class SpamNotificationListener : NotificationListenerService() {

    private val processedNotifications = HashMap<String, Long>()
    private val DEDUP_WINDOW_MS = 1500L

    private lateinit var engine: SpamClassifierEngine

    override fun onCreate() {
        super.onCreate()

        engine = SpamClassifierEngine(applicationContext)

        Log.d("SpamGuard", "SpamClassifierEngine initialized")
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {

        val packageName = sbn.packageName

        // Ignore notifications from our own app
        if (packageName == applicationContext.packageName) {
            return
        }

        val key = sbn.key
        val now = System.currentTimeMillis()

        val lastTime = processedNotifications[key]

        if (lastTime != null && now - lastTime < DEDUP_WINDOW_MS) {
            return
        }

        processedNotifications[key] = now

        val notification = sbn.notification
        val extras = notification.extras

        val title = extras.getString("android.title") ?: ""
        val text = extras.getCharSequence("android.text")?.toString() ?: ""

        val fullMessage = "$title $text"

        val result = engine.predict(fullMessage)

        val prediction = result.first
        val confidence = result.second

        Log.d("SpamGuard", "Prediction → $prediction ($confidence%)")

        when (prediction) {
            "spam" -> showSpamNotification(fullMessage, confidence)
            "potential_spam" -> showPotentialSpamNotification(fullMessage, confidence)
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        // Not required
    }

    private fun createChannel(channelId: String) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            val channel = NotificationChannel(
                channelId,
                "Spam Alerts",
                NotificationManager.IMPORTANCE_HIGH
            )

            val notificationManager =
                getSystemService(NOTIFICATION_SERVICE) as NotificationManager

            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun showSpamNotification(message: String, confidence: Int) {

        val channelId = "spam_warning_channel"

        createChannel(channelId)

        val notificationManager =
            getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        val builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle("⚠ Spam Detected ($confidence%)")
            .setContentText("Highly suspicious message detected")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(message)
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        notificationManager.notify(1001, builder.build())
    }

    private fun showPotentialSpamNotification(message: String, confidence: Int) {

        val channelId = "spam_warning_channel"

        createChannel(channelId)

        val notificationManager =
            getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        val builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("⚠ Potential Spam ($confidence%)")
            .setContentText("This message may be suspicious")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(message)
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        notificationManager.notify(1002, builder.build())
    }
}
