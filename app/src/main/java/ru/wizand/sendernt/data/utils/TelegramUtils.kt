package ru.wizand.sendernt.data.utils

import android.app.Activity
import android.app.Notification
import android.content.Context
import android.service.notification.StatusBarNotification
import android.util.Log
import android.widget.Button
import androidx.core.content.ContextCompat
import okhttp3.Call
import okhttp3.Callback
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import ru.wizand.sendernt.R
import ru.wizand.sendernt.data.service.NotificationLoggerService.Companion.TAG
import ru.wizand.sendernt.presentation.SettingsGlobalActivity
import java.io.IOException
import java.util.Date

object TelegramUtils {
    // Если httpClient используется только здесь, можно создать его здесь. Либо передать извне.
    private val httpClient = OkHttpClient()

    /**
     * Отправляет тестовое сообщение через Telegram Bot API.
     *
     * @param context Контекст (Activity), необходим для обновления UI.
     * @param botId Идентификатор бота (без префикса "bot").
     * @param chatId Идентификатор чата.
     * @param buttonTest Кнопка, внешний вид которой изменяется при успешной отправке.
     */
    fun sendTestTextToServer(
        context: Context,
        botId: String,
        chatId: String,
        buttonTest: Button
    ) {
        val time = Date()
        val text = "Test message"
        val title = "Test title"
        val message = " -> $time - $title - $text"

        // Формируем URL запроса к Telegram Bot API
        val url = "https://api.telegram.org/bot$botId/sendMessage"

        // Формируем тело запроса с параметрами chat_id и text
        val requestBody = FormBody.Builder()
            .add("chat_id", chatId)
            .add("text", message)
            .build()

        // Создаем POST-запрос
        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        httpClient.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("TAG", "Ошибка при отправке сообщения в Telegram: ${e.localizedMessage}")
            }

            override fun onResponse(call: Call, response: Response) {
                if (!response.isSuccessful) {
                    Log.e("TAG", "Ошибка при отправке сообщения в Telegram: ${response.message}")
                } else {
                    Log.d("TAG", "Сообщение успешно отправлено через Telegram")
                    // Поскольку изменение UI должно происходить в UI-потоке, используем runOnUiThread.
                    if (context is Activity) {
                        context.runOnUiThread {
                            buttonTest.setBackgroundColor(
                                ContextCompat.getColor(context, R.color.gray)
                            )
                            buttonTest.isClickable = false
                        }
                    }
                }
                response.close()
            }
        })
    }

    // функция отправки уведомления на сервер
    fun sendNotificationToServer(
        context: Context,
        sbn: StatusBarNotification
    ) {

        // Достаем данные из SharedPreferences
        val sharedPref = context.getSharedPreferences(SettingsGlobalActivity.PREFS_NAME, Context.MODE_PRIVATE)
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
        val time = AppUtils.convertTimestampToReadableFormat(sbn.getPostTime())
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


}