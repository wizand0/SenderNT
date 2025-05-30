package ru.wizand.sendernt.data.service

import android.os.Handler
import android.os.HandlerThread
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import ru.wizand.sendernt.data.utils.AllowedAppsPreferences
import ru.wizand.sendernt.data.utils.TelegramUtils

class NotificationLoggerService : NotificationListenerService() {

    companion object {
        const val TAG = "NotificationLogger"
        private const val DELAY_DURATION_MS = 15_000L
    }

    private val lastNotificationTimePerPackage = mutableMapOf<String, Long>()
    private val pendingTasks = mutableMapOf<String, Runnable>()

    private val handlerThread = HandlerThread("NotificationDelayThread").apply { start() }
    private val handler = Handler(handlerThread.looper)

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        sbn ?: return
        val packageName = sbn.packageName

        if (!isPackageAllowed(packageName)) {
            Log.d(TAG, "Уведомление от $packageName проигнорировано")
            return
        }

        val currentTime = System.currentTimeMillis()
        val lastSentTime = lastNotificationTimePerPackage[packageName] ?: 0L

        if (currentTime - lastSentTime >= DELAY_DURATION_MS) {
            cancelPendingTask(packageName)
            lastNotificationTimePerPackage[packageName] = currentTime
            TelegramUtils.sendNotificationToServer(applicationContext, sbn)
        } else {
            val remainingDelay = DELAY_DURATION_MS - (currentTime - lastSentTime)
            scheduleNotification(packageName, sbn, remainingDelay)
        }
    }

    private fun scheduleNotification(packageName: String, sbn: StatusBarNotification, delay: Long) {
        cancelPendingTask(packageName)

        val runnable = Runnable {
            lastNotificationTimePerPackage[packageName] = System.currentTimeMillis()
            pendingTasks.remove(packageName)
            TelegramUtils.sendNotificationToServer(applicationContext, sbn)
        }

        pendingTasks[packageName] = runnable
        handler.postDelayed(runnable, delay)
    }

    private fun cancelPendingTask(packageName: String) {
        pendingTasks.remove(packageName)?.let { handler.removeCallbacks(it) }
    }

    private fun isPackageAllowed(packageName: String): Boolean {
        return AllowedAppsPreferences.getAllowedPackages(applicationContext).contains(packageName)
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        sbn?.let {
            Log.d(TAG, "Уведомление удалено из пакета: ${it.packageName}")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        pendingTasks.values.forEach { handler.removeCallbacks(it) }
        pendingTasks.clear()
        handlerThread.quitSafely()
        Log.d(TAG, "NotificationLoggerService уничтожен и ресурсы очищены")
    }
}
