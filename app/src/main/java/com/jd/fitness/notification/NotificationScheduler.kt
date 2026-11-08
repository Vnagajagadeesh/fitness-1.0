package com.jd.fitness.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.jd.fitness.data.preferences.UserPreferencesRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Calendar

object NotificationScheduler {

    const val ID_MORNING_SKINCARE = 101
    const val ID_NIGHT_SKINCARE = 102
    const val ID_STUDY = 103
    const val ID_WORKOUT = 104
    const val ID_DAILY_REVIEW = 105
    const val ID_WEIGHT = 106
    const val ID_STEP_GOAL = 107

    fun scheduleDailyReminder(
        context: Context,
        reminderId: Int,
        timeHHmm: String,
        title: String,
        message: String
    ) {
        val parts = timeHHmm.split(":")
        if (parts.size != 2) return
        val hour = parts[0].toIntOrNull() ?: return
        val minute = parts[1].toIntOrNull() ?: return

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(context, NotificationReceiver::class.java).apply {
            action = "com.jd.fitness.ACTION_REMINDER"
            putExtra(NotificationReceiver.EXTRA_REMINDER_ID, reminderId)
            putExtra(NotificationReceiver.EXTRA_TITLE, title)
            putExtra(NotificationReceiver.EXTRA_MESSAGE, message)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminderId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (before(Calendar.getInstance())) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        } else {
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        }
    }

    fun cancelReminder(context: Context, reminderId: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, NotificationReceiver::class.java).apply {
            action = "com.jd.fitness.ACTION_REMINDER"
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminderId,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
        }
    }

    fun rescheduleAll(context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            val repo = UserPreferencesRepository(context)
            val prefs = repo.preferencesFlow.first()
            if (!prefs.isNotificationsEnabled) return@launch

            scheduleDailyReminder(
                context, ID_MORNING_SKINCARE, prefs.morningSkincareReminder,
                "Morning Skincare Routine", "Start the day fresh: Cleanser, Moisturizer & Sunscreen."
            )
            scheduleDailyReminder(
                context, ID_NIGHT_SKINCARE, prefs.nightSkincareReminder,
                "Night Skincare Routine", "Complete your night skincare regimen before rest."
            )
            scheduleDailyReminder(
                context, ID_STUDY, prefs.studyReminder,
                "Study Discipline (1 Hour)", "Lock in your deep-focus distraction-free study session."
            )
            scheduleDailyReminder(
                context, ID_WORKOUT, prefs.workoutReminder,
                "Workout Call", "Time for your daily workout. Stay relentless!"
            )
            scheduleDailyReminder(
                context, ID_DAILY_REVIEW, prefs.dailyReviewReminder,
                "FITNESS 1.0 Daily Review", "Review and check off your habits for today."
            )
            scheduleDailyReminder(
                context, ID_WEIGHT, prefs.weightReminder,
                "Morning Weigh-In", "Log your weight to keep your trend accurate."
            )
            scheduleDailyReminder(
                context, ID_STEP_GOAL, prefs.stepGoalReminder,
                "Step Goal Check", "Review your progress toward 10,000 steps today."
            )
        }
    }
}

