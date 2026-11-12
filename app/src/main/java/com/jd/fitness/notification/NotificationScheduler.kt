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

    // FITNESS 1.2 Notifications
    const val ID_MORNING_MOTIVATION = 201
    const val ID_AFTERNOON_MOTIVATION = 202
    const val ID_EVENING_MOTIVATION = 203
    const val ID_WEEKLY_REPORT = 204
    const val ID_JOURNAL_REMINDER = 205
    const val ID_RECOVERY_REMINDER = 206

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

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        calendar.timeInMillis,
                        pendingIntent
                    )
                } else {
                    alarmManager.set(
                        AlarmManager.RTC_WAKEUP,
                        calendar.timeInMillis,
                        pendingIntent
                    )
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
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
        } catch (e: SecurityException) {
            // Safe fallback if exact alarm permission was revoked
            alarmManager.set(
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

            // 1. Skincare
            scheduleDailyReminder(
                context,
                ID_MORNING_SKINCARE,
                prefs.morningSkincareReminder,
                "Morning Skincare Routine",
                "Start your day with clean discipline: Face Wash, Moisturizer, and Sunscreen."
            )
            scheduleDailyReminder(
                context,
                ID_NIGHT_SKINCARE,
                prefs.nightSkincareReminder,
                "Night Skincare & Cellular Recovery",
                "Time for evening skin repair: Cleanser, Moisturizer, Treatment."
            )

            // 2. Study & Workout
            scheduleDailyReminder(
                context,
                ID_STUDY,
                prefs.studyReminder,
                "Focused Study Session",
                "Your 1-hour home study block is starting now. Eliminate distractions and lock in."
            )
            scheduleDailyReminder(
                context,
                ID_WORKOUT,
                prefs.workoutReminder,
                "Daily Workout Command",
                "Calisthenics, weights, or high-intensity movement. Build physical resilience."
            )

            // 3. Daily Habits Review
            scheduleDailyReminder(
                context,
                ID_DAILY_REVIEW,
                prefs.dailyReviewReminder,
                "Daily Discipline Check-in",
                "Review your 13 core foundation pillars and secure your streak before midnight."
            )

            // 4. Weight & Steps
            scheduleDailyReminder(
                context,
                ID_WEIGHT,
                prefs.weightReminder,
                "Morning Weigh-In",
                "Log your morning body weight in kg for composition trend tracking."
            )
            scheduleDailyReminder(
                context,
                ID_STEP_GOAL,
                prefs.stepGoalReminder,
                "Step Goal Target",
                "Check your steps: push towards ${prefs.stepGoal} steps before winding down."
            )

            // 5. FITNESS 1.2: Motivation reminders
            if (prefs.isMorningMotivationEnabled) {
                scheduleDailyReminder(
                    context,
                    ID_MORNING_MOTIVATION,
                    prefs.morningMotivationTime,
                    "Morning Discipline Protocol",
                    "Discipline is doing what must be done, even when motivation has long gone. Conquer the morning."
                )
            } else {
                cancelReminder(context, ID_MORNING_MOTIVATION)
            }

            if (prefs.isAfternoonMotivationEnabled) {
                scheduleDailyReminder(
                    context,
                    ID_AFTERNOON_MOTIVATION,
                    prefs.afternoonMotivationTime,
                    "Midday Focus Calibration",
                    "Protect your afternoon momentum. Stay consistent with your diet, hydration, and study goals."
                )
            } else {
                cancelReminder(context, ID_AFTERNOON_MOTIVATION)
            }

            if (prefs.isEveningMotivationEnabled) {
                scheduleDailyReminder(
                    context,
                    ID_EVENING_MOTIVATION,
                    prefs.eveningMotivationTime,
                    "Evening Strength & Accountability",
                    "Consistency isn't about perfection; it is about never giving up on the standard you set."
                )
            } else {
                cancelReminder(context, ID_EVENING_MOTIVATION)
            }

            // 6. FITNESS 1.2: Journal Reflection
            if (prefs.isJournalReminderEnabled) {
                scheduleDailyReminder(
                    context,
                    ID_JOURNAL_REMINDER,
                    prefs.journalReminderTime,
                    "Private Reflection Journal",
                    "Take 5 minutes to capture what went well today, lessons learned, and tomorrow's focus."
                )
            } else {
                cancelReminder(context, ID_JOURNAL_REMINDER)
            }

            // 7. FITNESS 1.2: Recovery Review
            if (prefs.isRecoveryReminderEnabled) {
                scheduleDailyReminder(
                    context,
                    ID_RECOVERY_REMINDER,
                    prefs.recoveryReminderTime,
                    "Recovery & Sleep Check-in",
                    "Log today's perceived energy and prepare your environment for restorative sleep."
                )
            } else {
                cancelReminder(context, ID_RECOVERY_REMINDER)
            }
        }
    }
}
