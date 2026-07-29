package com.forgeflow.core.platform.notification

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.forgeflow.core.platform.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ActiveWorkoutNotifier @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    fun show(
        workoutName: String,
        completedSets: Int,
        totalSets: Int,
        startedAtEpochMillis: Long,
    ) {
        if (!canPostNotifications()) return
        val launchIntent = context.packageManager
            .getLaunchIntentForPackage(context.packageName)
            ?.apply {
                flags = android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
        val contentIntent = launchIntent?.let {
            PendingIntent.getActivity(
                context,
                ACTIVE_WORKOUT_NOTIFICATION_ID,
                it,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
        }
        val safeTotal = totalSets.coerceAtLeast(1)
        val notification = NotificationCompat.Builder(
            context,
            ForgeFlowNotificationChannels.ACTIVE_WORKOUT,
        )
            .setSmallIcon(R.drawable.ic_stat_forgeflow)
            .setContentTitle(workoutName)
            .setContentText(
                context.getString(
                    R.string.active_workout_notification_progress,
                    completedSets,
                    totalSets,
                ),
            )
            .setCategory(NotificationCompat.CATEGORY_WORKOUT)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setSilent(true)
            .setWhen(startedAtEpochMillis)
            .setUsesChronometer(true)
            .setProgress(safeTotal, completedSets.coerceIn(0, safeTotal), false)
            .setContentIntent(contentIntent)
            .build()

        NotificationManagerCompat.from(context).notify(
            ACTIVE_WORKOUT_NOTIFICATION_ID,
            notification,
        )
    }

    fun cancel() {
        NotificationManagerCompat.from(context).cancel(ACTIVE_WORKOUT_NOTIFICATION_ID)
    }

    private fun canPostNotifications(): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS,
            ) == PackageManager.PERMISSION_GRANTED

    private companion object {
        const val ACTIVE_WORKOUT_NOTIFICATION_ID = 4101
    }
}
