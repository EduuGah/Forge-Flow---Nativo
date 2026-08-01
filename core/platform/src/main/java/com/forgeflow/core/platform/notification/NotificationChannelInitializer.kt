package com.forgeflow.core.platform.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import com.forgeflow.core.platform.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class NotificationChannelInitializer @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    fun initialize() {
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannels(
            listOf(
                channel(
                    id = ForgeFlowNotificationChannels.REST_TIMER,
                    name = R.string.notification_channel_rest,
                    description = R.string.notification_channel_rest_description,
                    importance = NotificationManager.IMPORTANCE_HIGH,
                ),
                channel(
                    id = ForgeFlowNotificationChannels.ACTIVE_WORKOUT,
                    name = R.string.notification_channel_active,
                    description = R.string.notification_channel_active_description,
                    importance = NotificationManager.IMPORTANCE_DEFAULT,
                ),
                channel(
                    id = ForgeFlowNotificationChannels.SCHEDULED_WORKOUTS,
                    name = R.string.notification_channel_schedule,
                    description = R.string.notification_channel_schedule_description,
                    importance = NotificationManager.IMPORTANCE_DEFAULT,
                ),
                channel(
                    id = ForgeFlowNotificationChannels.PROGRESS,
                    name = R.string.notification_channel_progress,
                    description = R.string.notification_channel_progress_description,
                    importance = NotificationManager.IMPORTANCE_LOW,
                ),
                channel(
                    id = ForgeFlowNotificationChannels.APP_UPDATES,
                    name = R.string.notification_channel_updates,
                    description = R.string.notification_channel_updates_description,
                    importance = NotificationManager.IMPORTANCE_LOW,
                ),
                channel(
                    id = ForgeFlowNotificationChannels.WELLNESS,
                    name = R.string.notification_channel_wellness,
                    description = R.string.notification_channel_wellness_description,
                    importance = NotificationManager.IMPORTANCE_DEFAULT,
                ),
            ),
        )
    }

    private fun channel(
        id: String,
        name: Int,
        description: Int,
        importance: Int,
    ): NotificationChannel = NotificationChannel(
        id,
        context.getString(name),
        importance,
    ).apply {
        this.description = context.getString(description)
    }
}
