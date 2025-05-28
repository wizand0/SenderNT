package ru.wizand.sendernt.presentation

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ru.wizand.sendernt.data.TelegramRepository

/**
 * ViewModel для MainActivity.
 * Управляет логикой отправки сообщений в Telegram.
 */
class MainViewModel(
    private val telegramRepository: TelegramRepository
) : ViewModel() {

    private val _messageStatus = MutableLiveData<Boolean>()
    val messageStatus: LiveData<Boolean> = _messageStatus

    /**
     * Отправляет тестовое сообщение в Telegram.
     *
     * @param botToken Токен бота
     * @param chatId ID Telegram-чата
     * @param message Текст сообщения
     */
    fun sendTestMessage(botToken: String, chatId: String, message: String) {
        viewModelScope.launch(Dispatchers.IO) {
            Log.d("MainViewModel", "🚀 Отправка сообщения в Telegram...")
            val success = telegramRepository.sendMessage(botToken, chatId, message)
            _messageStatus.postValue(success)
        }
    }
}
