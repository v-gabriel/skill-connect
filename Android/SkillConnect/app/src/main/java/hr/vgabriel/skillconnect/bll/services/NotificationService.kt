package hr.vgabriel.skillconnect.bll.services

import android.Manifest
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import hr.vgabriel.skillconnect.MainActivity
import hr.vgabriel.skillconnect.R


class NotificationService(
) {
    private var notificationId = 0
    private val notificationIds = mutableListOf<Int>()

    fun displayMessageNotification(title: String, content: String) {
        val intent = Intent(MainActivity.appContext, MainActivity::class.java).apply {}

        val pendingIntent = getPendingIntent(intent)
        val builder = getBuilder(title, content, pendingIntent)

        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK

        executeNotification(builder)
    }

    companion object {
        fun getBuilder(
            title: String,
            content: String,
            pendingIntent: PendingIntent
        ): NotificationCompat.Builder {
            return NotificationCompat.Builder(
                MainActivity.appContext,
                MainActivity.appContext.getString(R.string.channel_id)
            )
                .setSmallIcon(R.drawable.app_notification_icon)
                .setContentTitle(title)
                .setContentText(content)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
        }

        fun getPendingIntent(intent: Intent): PendingIntent {

            val pendingIntent: PendingIntent = PendingIntent.getActivity(
                MainActivity.appContext,
                0,
                intent,
                PendingIntent.FLAG_IMMUTABLE
            )

            return pendingIntent
        }
    }

    private fun executeNotification(builder: NotificationCompat.Builder) {
        notificationId += 1

        with(NotificationManagerCompat.from(MainActivity.appContext)) {
            if (ActivityCompat.checkSelfPermission(
                    MainActivity.appContext,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
            notify(notificationId, builder.build())
            notificationIds.add(notificationId)
        }
    }
}