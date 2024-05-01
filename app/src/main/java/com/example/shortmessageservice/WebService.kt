package com.example.shortmessageservice
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import fi.iki.elonen.NanoHTTPD
import java.net.SocketException

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import fi.iki.elonen.NanoHTTPD.newFixedLengthResponse

class WebService : Service() {

    private var server: NanoHTTPD? = null
    private var messageSender: MessageSender = MessageSender()
    private lateinit var wakeLock: PowerManager.WakeLock
    override fun onCreate() {
        super.onCreate()

        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "WebService::WakeLock")
    }
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        createNotificationChannel()
        val notification = createNotification()
        startForeground(NOTIFICATION_ID, notification.build())

        // Acquire the wake lock
        wakeLock.acquire()

        if (server == null) {
            server = object : NanoHTTPD(8080) {
                override fun serve(session: IHTTPSession): Response {
                    val response = when {
                        (session.method == Method.GET) -> handleGetRequest(session)
                        (session.method == Method.POST && session.uri == "/sendMessage") -> handlePostEndpoint1(session)
                        else -> newFixedLengthResponse(Response.Status.NOT_FOUND, NanoHTTPD.MIME_PLAINTEXT, "Not found")
                    }

                    // Send a notification every time a request is completed
                    sendNotification("Request Completed", "SMS has been sent")

                    return response
                }
            }
            try {
                server?.start()
                println("Web server started")
            } catch (e: Exception) {
                println("Error starting web server: ${e.message}")
            }
        }
        return START_STICKY
    }

    override fun onDestroy() {
        server?.stop()
        println("Web server stopped")
        // Release the wake lock
        if (wakeLock.isHeld) {
            wakeLock.release()
        }
        super.onDestroy()
    }

    private fun sendNotification(title: String, content: String) {
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            notificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(content)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    private fun handleGetRequest(session: NanoHTTPD.IHTTPSession): NanoHTTPD.Response {
        val uri = session.uri
        return when (uri) {
            "/health" -> handleHealthCheck()
            else -> newFixedLengthResponse(NanoHTTPD.Response.Status.NOT_FOUND, NanoHTTPD.MIME_PLAINTEXT, "Not found")
        }
    }

    private fun handlePostEndpoint1(session: NanoHTTPD.IHTTPSession): NanoHTTPD.Response {
        try {
            val phoneNumber = session.parms.get("phoneNumber")
            val message = session.parms.get("message")

            if (phoneNumber.isNullOrEmpty()) {
                return newFixedLengthResponse(
                    NanoHTTPD.Response.Status.BAD_REQUEST,
                    NanoHTTPD.MIME_PLAINTEXT,
                    "Missing phoneNumber Parameter"
                )
            }
            if (message.isNullOrEmpty()) {
                return newFixedLengthResponse(
                    NanoHTTPD.Response.Status.BAD_REQUEST,
                    NanoHTTPD.MIME_PLAINTEXT,
                    "Missing message Parameter"
                )
            }

            messageSender.sendSMS(phoneNumber, message)
            return newFixedLengthResponse("Message Sent")
        } catch (e: SocketException) {
            e.printStackTrace()
            return newFixedLengthResponse(NanoHTTPD.Response.Status.INTERNAL_ERROR, NanoHTTPD.MIME_PLAINTEXT, "Socket closed")
        } catch (e: Exception) {
            e.printStackTrace()
            return newFixedLengthResponse(NanoHTTPD.Response.Status.INTERNAL_ERROR, NanoHTTPD.MIME_PLAINTEXT, "Error handling request")
        }
    }

    private fun handleHealthCheck(): NanoHTTPD.Response {
        return newFixedLengthResponse("Server is up and running!")
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Foreground Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): NotificationCompat.Builder {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Foreground Service")
            .setContentText("Web service is running")
            .setSmallIcon(R.drawable.ic_notification)
    }

    companion object {
        private const val CHANNEL_ID = "WebServiceChannel"
        private const val NOTIFICATION_ID = 1

    }
}

