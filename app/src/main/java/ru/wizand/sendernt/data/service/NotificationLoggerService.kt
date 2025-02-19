package ru.wizand.sendernt.data.service

import android.app.Notification
import android.content.Context
import android.os.Looper
import android.os.Handler
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import okhttp3.*
import ru.wizand.sendernt.data.utils.AllowedAppsPreferences
import ru.wizand.sendernt.presentation.SettingsGlobalActivity
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class NotificationLoggerService : NotificationListenerService() {

    companion object {
        private const val TAG = "NotificationLogger"

        // Замените YOUR_BOT_TOKEN и YOUR_CHAT_ID на реальные данные (для тестирования)
//        private const val TELEGRAM_BOT_TOKEN = ""
//        private const val TELEGRAM_CHAT_ID = ""

        // Задержка 20 секунд в миллисекундах
        private const val DELAY_DURATION_MS = 15_000L
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



            // Тестирование работоспособности. Удалить после азвершения тестов
//            val messageTest = sbn.toString()
//            Log.e(TAG, "сформировано:  $messageTest")

            // Логирование информации об уведомлении
//            Log.d(TAG, "Уведомление получено от пакета: ${it.packageName}")
//            Log.d(TAG, "ID уведомления: ${it.id}")
//            Log.d(TAG, "Время: ${it.postTime}")

            // Здесь можно выполнить отправку уведомления на сервер

//            if (sbn.packageName != "com.google.android.gms") {
//                sendNotificationToServer(it)
//            }

            Log.e(TAG, "Пакет:  $packageName")
            Log.e(TAG, "Есть ли он в разрешенных:  ${allowedPackages.contains(packageName)}")

            // Проверка, что уведомление приходит от разрешенного пакета
            if (allowedPackages.contains(packageName)) {

                Log.d(TAG, "Уведомление разрешено и отправлено: ${it.packageName}")
//                sendNotificationToServer(it)

                // Проверка, что уведомления не частят (константа DELAY_DURATION_MS)
                // Если с момента последней отправки уведомления прошло не менее 20 секунд, отправляем уведомление сразу
                if (currentTime - lastSentTime >= DELAY_DURATION_MS) {
//                lastNotificationTimePerPackage[packageName] = currentTime
//                sendNotificationToServer(it)
                    // Отменяем предыдущую запланированную задачу, если она существует.
                    pendingTasks[packageName]?.let { pendingTask ->
                        handler.removeCallbacks(pendingTask)
                        pendingTasks.remove(packageName)
//                        Log.d(TAG, "Отмена предыдущей запланированной задачи для пакета $packageName")
                    }
                    lastNotificationTimePerPackage[packageName] = currentTime

                    sendNotificationToServer(it)

                } else {
                    // Если менее 20 секунд – рассчитываем оставшуюся задержку и планируем задачу
                    val remainingDelay = DELAY_DURATION_MS - (currentTime - lastSentTime)
                    Log.d(TAG, "Не частим: ${it.packageName} - $remainingDelay")
//                    Log.d(
//                        TAG,
//                        "Запланирована отправка уведомления от пакета $packageName через $remainingDelay мс"
//                    )

                    // Если ранее уже была запланирована задача для этого пакета - удаляем её.
                    pendingTasks[packageName]?.let { pendingTask ->
                        handler.removeCallbacks(pendingTask)
                        Log.d(TAG, "Отмена ранее запланированной задачи для пакета $packageName")
                    }



                    // Чтобы избежать повторных запусков для одного и того же уведомления,
                    // можно отменять ранее запланированные задачи для этого пакета.
                    // В данном примере мы опускаем логику отмены, но она может быть добавлена при необходимости.
//                handler.postDelayed({
//                    lastNotificationTimePerPackage[packageName] = System.currentTimeMillis()
//                    sendNotificationToServer(it)
//                }, remainingDelay)

                    // Создаем новый Runnable, который выполнит отправку уведомления по истечении задержки.
                    val runnable = Runnable {
                        // Обновляем время последней отправки уведомления.
                        lastNotificationTimePerPackage[packageName] = System.currentTimeMillis()
                        // Удаляем задачу из pendingTasks после её выполнения.
                        pendingTasks.remove(packageName)
                        sendNotificationToServer(it)
                    }

                    // Сохраняем новый Runnable в карте и планируем его выполнение.
                    pendingTasks[packageName] = runnable
                    handler.postDelayed(runnable, remainingDelay)

                }


            } else {
                // Можно, например, просто игнорировать уведомление
                Log.d("MyNLS", "Уведомление от $packageName проигнорировано")
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

    /**
     * Отправка сообщения через Telegram Bot API.
     *
     * @param message Текст уведомления, который нужно отправить.
     */


    // Пример функции отправки уведомления на сервер
    private fun sendNotificationToServer(sbn: StatusBarNotification) {

        // Достаем данные из SharedPreferences
        val sharedPref = getSharedPreferences(SettingsGlobalActivity.PREFS_NAME, Context.MODE_PRIVATE)
        val botId = sharedPref.getString(SettingsGlobalActivity.KEY_BOT_ID, "No_data")
        val chatId = sharedPref.getString(SettingsGlobalActivity.KEY_CHAT_ID, "No_data")



        // Здесь можно реализовать отправку данных на сервер с помощью Retrofit, Volley или любой другой библиотеки.
        // Для простоты выводим лог.

        // Тестирование работоспособности. Удалить после азвершения тестов
        val messageTest = sbn.toString()
        Log.e(TAG, "сформировано:  $messageTest")

        val packageName = sbn.packageName
        val notification = sbn.notification
//        val time = sbn.postTime
        val time = convertTimestampToReadableFormat(sbn.getPostTime())
        val extras = notification.extras




        // Получаем текст уведомления
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: ""
        // Также можно получить заголовок уведомления
        val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString() ?: ""

        val message = " -> $packageName:$time - $title - $text"


        // Реализация
//        Log.d(TAG, "Подготавливаем отправку уведомления в Telegram: $message")

        // Формируем URL запроса к Telegram Bot API
//        val url = "https://api.telegram.org/bot$TELEGRAM_BOT_TOKEN/sendMessage"
        val url = "https://api.telegram.org/bot$botId/sendMessage"

        // Формируем тело запроса с параметрами chat_id и text
        val requestBody = FormBody.Builder()
//            .add("chat_id", TELEGRAM_CHAT_ID)
            .add("chat_id", chatId!!)
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

    fun convertTimestampToReadableFormat(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault())
        val date = Date(timestamp)
        return sdf.format(date)
    }

}

