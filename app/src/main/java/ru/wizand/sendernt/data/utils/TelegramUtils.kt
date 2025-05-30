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
        val appContext = context.applicationContext
        val sharedPref = appContext.getSharedPreferences(SettingsGlobalActivity.PREFS_NAME, Context.MODE_PRIVATE)
        val botId = sharedPref.getString(SettingsGlobalActivity.KEY_BOT_ID, null)
        val chatId = sharedPref.getString(SettingsGlobalActivity.KEY_CHAT_ID, null)

        if (botId.isNullOrBlank() || chatId.isNullOrBlank()) {
            Log.e(TAG, "Bot ID или Chat ID не указаны. Прерывание отправки.")
            return
        }

        val packageName = sbn.packageName
        val notification = sbn.notification
        val time = AppUtils.convertTimestampToReadableFormat(sbn.postTime)
        val extras = notification.extras

        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: ""
        val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString() ?: ""
        val rawMessage = " -> $packageName:$time - $title - $text"
        val message = if (rawMessage.length > 4000) rawMessage.take(4000) + "…" else rawMessage

        val url = "https://api.telegram.org/bot$botId/sendMessage"
        val requestBody = FormBody.Builder()
            .add("chat_id", chatId)
            .add("text", message)
            .build()

        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

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