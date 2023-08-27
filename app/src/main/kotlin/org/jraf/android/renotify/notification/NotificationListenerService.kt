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
import org.jraf.android.renotify.util.logd
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

class NotificationListenerService : android.service.notification.NotificationListenerService() {
    companion object {
        private const val SHOW_NOTIFICATION_DELAY_SECONDS = 15L
        private const val CANCEL_NOTIFICATION_AFTER_SHON_DELAY_SECONDS = 4L
    }

    private val scheduler: ScheduledExecutorService = Executors.newScheduledThreadPool(1)
    private var scheduledFuture: ScheduledFuture<*>? = null

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        super.onNotificationPosted(sbn)
        val notification = sbn.notification!!
        val title = notification.extras.getString(Notification.EXTRA_TITLE)
        val text = notification.extras.getCharSequence(Notification.EXTRA_TEXT, "").toString()
        val hasMediaSession = notification.extras.containsKey(Notification.EXTRA_MEDIA_SESSION)
        val flags = notification.flags
        val flagDescription = notification.flagDescription()
        logd("onNotificationPosted tag=${sbn.tag} key=${sbn.key} title=$title text=$text hasMediaSession=$hasMediaSession flags=$flags flagDescription=$flagDescription")
        scheduleOrUnscheduleAlert()
    }

    private fun scheduleOrUnscheduleAlert() {
        val activeNotificationsCount = getActiveNotificationCount()
        logd("scheduleOrUnscheduleAlert activeNotificationsCount=$activeNotificationsCount")
        if (activeNotificationsCount == 0) {
            logd("scheduleOrUnscheduleAlert unscheduling")
            scheduledFuture?.cancel(false)
            scheduledFuture = null
            return
        }
        if (scheduledFuture != null) {
            logd("scheduleOrUnscheduleAlert already scheduled")
            return
        }
        scheduledFuture = scheduler.schedule({
            maybeAlert()
        }, SHOW_NOTIFICATION_DELAY_SECONDS, TimeUnit.SECONDS)
    }

    private fun maybeAlert() {
        val activeNotificationsCount = getActiveNotificationCount()
        logd("maybeAlert activeNotificationsCount=$activeNotificationsCount")
        if (activeNotificationsCount == 0) {
            logd("maybeAlert no active notifications, not alerting")
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

    override fun onNotificationRemoved(sbn: StatusBarNotification, rankingMap: RankingMap?, reason: Int) {
        super.onNotificationRemoved(sbn, rankingMap, reason)
        logd("onNotificationRemoved tag=${sbn.tag} key=${sbn.key}")
        scheduleOrUnscheduleAlert()
    }

    private fun getActiveNotificationCount(): Int {
        val activeNotifications = activeNotifications.filter { statusBarNotification ->
            val notification = statusBarNotification.notification!!
            val hasMediaSession = notification.extras.containsKey(Notification.EXTRA_MEDIA_SESSION)
            val isOwnNotification = statusBarNotification.packageName == packageName
            !isOwnNotification &&
                    !statusBarNotification.isOngoing &&
                    statusBarNotification.isClearable &&
                    !hasMediaSession
        }
        return activeNotifications.size
    }

    private fun Notification.flagDescription(): String {
        return buildString {
            if (flags and Notification.FLAG_SHOW_LIGHTS != 0) append("1 (SHOW_LIGHTS), ")
            if (flags and Notification.FLAG_ONGOING_EVENT != 0) append("2 (ONGOING_EVENT), ")
            if (flags and Notification.FLAG_INSISTENT != 0) append("4 (INSISTENT), ")
            if (flags and Notification.FLAG_ONLY_ALERT_ONCE != 0) append("8 (ONLY_ALERT_ONCE), ")
            if (flags and Notification.FLAG_AUTO_CANCEL != 0) append("16 (AUTO_CANCEL), ")
            if (flags and Notification.FLAG_NO_CLEAR != 0) append("32 (NO_CLEAR), ")
            if (flags and Notification.FLAG_FOREGROUND_SERVICE != 0) append("64 (FOREGROUND_SERVICE), ")
            if (flags and Notification.FLAG_HIGH_PRIORITY != 0) append("128 (HIGH_PRIORITY), ")
            if (flags and Notification.FLAG_LOCAL_ONLY != 0) append("256 (LOCAL_ONLY), ")
            if (flags and Notification.FLAG_GROUP_SUMMARY != 0) append("512 (GROUP_SUMMARY), ")
            if (flags and 1024 != 0) append("1024 (AUTOGROUP_SUMMARY), ")
            if (flags and 2048 != 0) append("2048 (CAN_COLORIZE), ")
            if (flags and Notification.FLAG_BUBBLE != 0) append("4096 (BUBBLE), ")
            if (flags and 8192 != 0) append("8192 (UNKNOWN), ")
        }
    }
}
