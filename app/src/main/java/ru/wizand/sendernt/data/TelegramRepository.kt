package ru.wizand.sendernt.data

import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.URLEncoder

/**
 * Репозиторий для взаимодействия с Telegram Bot API.
 *
 * Отвечает за отправку текстовых уведомлений через Telegram-бота в указанный чат.
 */
class TelegramRepository(
    private val client: OkHttpClient = OkHttpClient()
) {

    companion object {
        private const val BASE_URL = "https://api.telegram.org/bot"
        private const val TAG = "TelegramRepository"
    }

    /**
     * Отправляет сообщение в указанный Telegram-чат.
     *
     * @param botToken Токен Telegram-бота.
     * @param chatId ID чата (можно получить через /getUpdates или /start).
     * @param message Текст сообщения для отправки.
     *
     * @return true если успешно, false если ошибка.
     */
    fun sendMessage(botToken: String, chatId: String, message: String): Boolean {
        // Кодируем текст, чтобы не было проблем с символами
        val encodedMessage = URLEncoder.encode(message, "UTF-8")
        val url = "$BASE_URL$botToken/sendMessage?chat_id=$chatId&text=$encodedMessage"

        val request = Request.Builder()
            .url(url)
            .get()
            .build()

        return try {
            val response = client.newCall(request).execute()
            val success = response.isSuccessful
            if (!success) {
                Log.w(TAG, "📡 Ошибка отправки в Telegram: ${response.code} ${response.message}")
            }
            success
        } catch (e: Exception) {
            Log.e(TAG, "❌ Ошибка при запросе к Telegram API", e)
            false
        }
    }
}
