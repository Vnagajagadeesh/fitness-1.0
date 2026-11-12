package com.jd.fitness.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.jd.fitness.MainActivity
import com.jd.fitness.R
import com.jd.fitness.data.preferences.UserPreferencesRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class NotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            // Reschedule reminders on phone reboot
            NotificationScheduler.rescheduleAll(context)
            return
        }

        val reminderId = intent.getIntExtra(EXTRA_REMINDER_ID, 0)
        val title = intent.getStringExtra(EXTRA_TITLE) ?: "FITNESS 1.2"
        val message = intent.getStringExtra(EXTRA_MESSAGE) ?: "Time for your daily discipline check-in!"

        CoroutineScope(Dispatchers.IO).launch {
            val prefs = UserPreferencesRepository(context).preferencesFlow.first()
            if (!prefs.isNotificationsEnabled) return@launch

            // Check Quiet Hours
            if (prefs.isQuietHoursEnabled) {
                if (isInQuietHours(prefs.quietHoursStart, prefs.quietHoursEnd)) {
                    return@launch // Suppress notification during quiet hours
                }
            }

            postNotification(context, reminderId, title, message)
        }
    }

    private fun isInQuietHours(startStr: String, endStr: String): Boolean {
        return try {
            val formatter = DateTimeFormatter.ofPattern("HH:mm")
            val start = LocalTime.parse(startStr, formatter)
            val end = LocalTime.parse(endStr, formatter)
            val now = LocalTime.now()

            if (start.isBefore(end)) {
                now.isAfter(start) && now.isBefore(end)
            } else {
                // Overnight quiet hours, e.g. 23:00 to 06:30
                now.isAfter(start) || now.isBefore(end)
            }
        } catch (e: Exception) {
            false
        }
    }

    private fun postNotification(context: Context, reminderId: Int, title: String, message: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "fitness_reminders_channel"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "FITNESS 1.2 Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Daily discipline, motivation, habits, planner, and recovery reminders"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val tapIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            reminderId,
            tapIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(message)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(reminderId, notification)
    }

    companion object {
        const val EXTRA_REMINDER_ID = "reminder_id"
        const val EXTRA_TITLE = "extra_title"
        const val EXTRA_MESSAGE = "extra_message"
    }
}
