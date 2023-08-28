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

import android.app.Notification
import android.service.notification.StatusBarNotification
import org.jraf.android.renotify.repository.RenotifyPrefs
import org.jraf.android.renotify.util.logd
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

class NotificationListenerService : android.service.notification.NotificationListenerService() {
    companion object {
        private const val SHOW_NOTIFICATION_DELAY_SECONDS = 30L
        private const val CANCEL_NOTIFICATION_AFTER_SHON_DELAY_SECONDS = 4L

        private val ignoredPackages = setOf(
            "com.google.android.deskclock",
        )
    }

    private val renotifyPrefs by lazy { RenotifyPrefs(application) }

    private val scheduler: ScheduledExecutorService = Executors.newScheduledThreadPool(1)
    private var scheduledFuture: ScheduledFuture<*>? = null


    override fun onNotificationPosted(sbn: StatusBarNotification) {
        super.onNotificationPosted(sbn)
        logd("onNotificationPosted")
        scheduleOrUnscheduleAlert()
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification, rankingMap: RankingMap?, reason: Int) {
        super.onNotificationRemoved(sbn, rankingMap, reason)
        logd("onNotificationRemoved")
        scheduleOrUnscheduleAlert()
    }

    private fun scheduleOrUnscheduleAlert() {
        val isServiceEnabled = renotifyPrefs.isServiceEnabled.value
        val activeNotificationsCount = getActiveNotificationCount()
        logd("scheduleOrUnscheduleAlert isServiceEnabled=$isServiceEnabled activeNotificationsCount=$activeNotificationsCount")
        if (!isServiceEnabled || activeNotificationsCount == 0) {
            logd("scheduleOrUnscheduleAlert unscheduling")
            unscheduleAlert()
            return
        }
        if (scheduledFuture != null) {
            logd("scheduleOrUnscheduleAlert already scheduled")
            return
        }
        logd("scheduleOrUnscheduleAlert scheduling")
        scheduledFuture = scheduler.schedule({
            maybeAlert()
        }, SHOW_NOTIFICATION_DELAY_SECONDS, TimeUnit.SECONDS)
    }

    private fun unscheduleAlert() {
        scheduledFuture?.cancel(false)
        scheduledFuture = null
    }

    private fun maybeAlert() {
        val isServiceEnabled = renotifyPrefs.isServiceEnabled.value
        val activeNotificationsCount = getActiveNotificationCount()
        logd("maybeAlert isServiceEnabled=$isServiceEnabled activeNotificationsCount=$activeNotificationsCount")
        if (!isServiceEnabled || activeNotificationsCount == 0) {
            logd("maybeAlert service not enabled or no active notifications, not alerting and unscheduling")
            unscheduleAlert()
            return
        }
        logd("maybeAlert show notification")
        try {
            showNotification(this)
            scheduleCancelNotification()
        } catch (e: Exception) {
            logd(e, "Error showing notification")
        }
        scheduledFuture = null
        scheduleOrUnscheduleAlert()
    }

    private fun scheduleCancelNotification() {
        val notificationCancelDelay = CANCEL_NOTIFICATION_AFTER_SHON_DELAY_SECONDS
        logd("scheduleCancelNotification")
        scheduler.schedule({
            cancelNotification(this)
        }, notificationCancelDelay, TimeUnit.SECONDS)
    }

    private fun getActiveNotificationCount(): Int {
        val activeNotifications = activeNotifications.filter { statusBarNotification ->
            val notification = statusBarNotification.notification!!
            val hasMediaSession = notification.extras.containsKey(Notification.EXTRA_MEDIA_SESSION)
            val isOwnNotification = statusBarNotification.packageName == packageName
            val isIgnoredPackage = statusBarNotification.packageName in ignoredPackages
            val hasOverrideGroupKey = statusBarNotification.overrideGroupKey != null
            !isOwnNotification &&
                    !isIgnoredPackage &&
                    !statusBarNotification.isOngoing &&
                    statusBarNotification.isClearable &&
                    !hasOverrideGroupKey &&
                    !hasMediaSession
        }
        return activeNotifications.size
    }
}
