package com.jd.fitness

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.jd.fitness.data.backup.BackupManager
import com.jd.fitness.data.backup.LegacyMigrationManager
import com.jd.fitness.data.health.HealthConnectManager
import com.jd.fitness.data.local.FitnessDatabase
import com.jd.fitness.data.preferences.UserPreferencesRepository
import com.jd.fitness.data.repository.FitnessRepository
import com.jd.fitness.data.sync.SupabaseManager
import com.jd.fitness.notification.NotificationScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FitnessApp : Application() {

    lateinit var database: FitnessDatabase
        private set

    lateinit var fitnessRepository: FitnessRepository
        private set

    lateinit var userPreferencesRepository: UserPreferencesRepository
        private set

    lateinit var healthConnectManager: HealthConnectManager
        private set

    lateinit var supabaseManager: SupabaseManager
        private set

    lateinit var backupManager: BackupManager
        private set

    lateinit var legacyMigrationManager: LegacyMigrationManager
        private set

    override fun onCreate() {
        super.onCreate()

        // Initialize Room Database
        database = FitnessDatabase.getDatabase(this)

        // Initialize Core Singletons
        fitnessRepository = FitnessRepository(database)
        userPreferencesRepository = UserPreferencesRepository(this)
        healthConnectManager = HealthConnectManager(this)
        supabaseManager = SupabaseManager(database, userPreferencesRepository)
        backupManager = BackupManager(database, userPreferencesRepository)
        legacyMigrationManager = LegacyMigrationManager(database, userPreferencesRepository)

        // Create Notification Channel
        createNotificationChannels()

        // Schedule Daily Discipline Reminders
        CoroutineScope(Dispatchers.IO).launch {
            NotificationScheduler.rescheduleAll(this@FitnessApp)
        }
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "fitness_reminders_channel",
                "FITNESS 1.0 Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Daily discipline notifications for habits, skincare, workout, and steps"
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }
}

