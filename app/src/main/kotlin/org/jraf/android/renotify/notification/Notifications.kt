/*
 * This source is part of the
 *      _____  ___   ____
 *  __ / / _ \/ _ | / __/___  _______ _
 * / // / , _/ __ |/ _/_/ _ \/ __/ _ `/
 * \___/_/|_/_/ |_/_/ (_)___/_/  \_, /
 *                              /___/
 * repository.
 *
 * Copyright (C) 2023-present Benoit 'BoD' Lubek (BoD@JRAF.org)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.jraf.android.renotify.notification

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Process
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import org.jraf.android.renotify.R
import org.jraf.android.renotify.util.logd

private const val NOTIFICATION_CHANNEL_MAIN = "NOTIFICATION_CHANNEL_MAIN"
private const val NOTIFICATION_ID = 1

private fun createNotificationChannel(context: Context) {
    val channel = NotificationChannel(
        NOTIFICATION_CHANNEL_MAIN,
        context.getString(R.string.notification_channel_main_name),
        NotificationManager.IMPORTANCE_DEFAULT
    ).apply {
        description = context.getString(R.string.notification_channel_main_description)
    }
    val notificationManager = NotificationManagerCompat.from(context)
    notificationManager.createNotificationChannel(channel)
}

private fun createNotification(context: Context): Notification {
    return NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_MAIN)
        .setSmallIcon(R.drawable.ic_notification_24)
        .setAutoCancel(true)
        .setShowWhen(false)
        .build()
}

fun showNotification(context: Context) {
    createNotificationChannel(context)
    val notification = createNotification(context)
    val notificationManager = NotificationManagerCompat.from(context)
    if (context.checkPermission(Manifest.permission.POST_NOTIFICATIONS, Process.myPid(), Process.myUid()) != PackageManager.PERMISSION_GRANTED) {
        // TODO request permission
        logd("showNotification POST_NOTIFICATIONS permission not granted")
        return
    }
    notificationManager.notify(NOTIFICATION_ID, notification)
}

fun cancelNotification(context: Context) {
    val notificationManager = NotificationManagerCompat.from(context)
    notificationManager.cancel(NOTIFICATION_ID)
}
