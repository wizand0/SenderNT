package ru.wizand.sendernt.data.service

import android.os.Looper
import android.os.Handler
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import okhttp3.*
import ru.wizand.sendernt.data.utils.AllowedAppsPreferences
import java.io.IOException

class NotificationLoggerServiceSmall : NotificationListenerService() {

    companion object {
        private const val TAG = "NotificationLogger"

        // Замените YOUR_BOT_TOKEN и YOUR_CHAT_ID на реальные данные
        private const val TELEGRAM_BOT_TOKEN = ""
        private const val TELEGRAM_CHAT_ID = ""
    }

    // Карта, которая хранит для каждого пакета время последней отправки уведомления
    private val lastNotificationTimePerPackage = mutableMapOf<String, Long>()

    // Карта для хранения запланированных Runnable для каждого пакета
    private val pendingTasks = mutableMapOf<String, Runnable>()

    // Handler для отложенных запусков (можно использовать main-looper, если такая логика подходит)
    private val handler = Handler(Looper.getMainLooper())

    // Вызывается, когда новое уведомление появляется в панели уведомлений
    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        sbn?.let {

            // Cписок пакетов от которых можно пересылать уведомления
            val allowedPackages = AllowedAppsPreferences.getAllowedPackages(applicationContext)


            val packageName = it.packageName
            val currentTime = System.currentTimeMillis()
            val lastSentTime = lastNotificationTimePerPackage[packageName] ?: 0L

            // Логирование информации об уведомлении
            Log.d(TAG, "Уведомление получено от пакета: ${it.packageName}")
            Log.d(TAG, "ID уведомления: ${it.id}")
            Log.d(TAG, "Время: ${it.postTime}")

            // Здесь можно выполнить отправку уведомления на сервер
            if (sbn.packageName != "com.google.android.gms") {
                sendNotificationToServer(it)
            }
        }
    }


    // Вызывается, когда уведомление удаляется из панели уведомлений
    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        super.onNotificationRemoved(sbn)
        sbn?.let {
            Log.d(TAG, "Уведомление удалено из пакета: ${it.packageName}")
        }
    }

    // Создаем экземпляр OkHttpClient
    private val httpClient = OkHttpClient()

    private fun sendNotificationToServer(sbn: StatusBarNotification) {
        // Здесь можно реализовать отправку данных на сервер с помощью Retrofit, Volley или любой другой библиотеки.
        // Для простоты выводим лог.
        val message = sbn.toString()
        Log.d(TAG, "Отправка уведомления от ${sbn.packageName} на сервер...")

        // Реализация
        Log.d(TAG, "Подготавливаем отправку уведомления в Telegram: $message")

        // Формируем URL запроса к Telegram Bot API
        val url = "https://api.telegram.org/bot$TELEGRAM_BOT_TOKEN/sendMessage"

        // Формируем тело запроса с параметрами chat_id и text
        val requestBody = FormBody.Builder()
            .add("chat_id", TELEGRAM_CHAT_ID)
            .add("text", message)
            .build()

        // Создаем POST-запрос
        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        // Выполняем запрос асинхронно
        httpClient.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e(TAG, "Ошибка при отправке сообщения в Telegram: ${e.localizedMessage}")
            }

            override fun onResponse(call: Call, response: Response) {
                if (!response.isSuccessful) {
                    Log.e(TAG, "Ошибка при отправке сообщения в Telegram: ${response.message}")
                } else {
                    Log.d(TAG, "Сообщение успешно отправлено через Telegram")
                }
                response.close()
            }
        })
    }
}

