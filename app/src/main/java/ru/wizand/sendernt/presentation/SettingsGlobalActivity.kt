package ru.wizand.sendernt.presentation

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import ru.wizand.sendernt.R
import ru.wizand.sendernt.data.service.NotificationLoggerService

class SettingsGlobalActivity : AppCompatActivity() {

    // Ключи для SharedPreferences
    companion object {
        const val PREFS_NAME = "MyPrefs"
        const val KEY_BOT_ID = "BOT_ID"
        const val KEY_CHAT_ID = "CHAT_ID"
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_settings_global)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val btnBack = findViewById<Button>(R.id.btnBack)
        btnBack.setOnClickListener {
            // Завершаем активность, возвращаясь к предыдущей (MainActivity)
            finish()
        }

        val btnSettings = findViewById<Button>(R.id.btnApps)
        btnSettings.setOnClickListener {
            // Создаем Intent для перехода в SettingsActivity
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }

        val editTextBotID = findViewById<EditText>(R.id.editTextBotID)
        val editTextChatID = findViewById<EditText>(R.id.editTextChatID)
        val buttonSave = findViewById<Button>(R.id.buttonSave)

        buttonSave.setOnClickListener {
            // Получаем текст из EditText и обрезаем пробелы
            var botId = editTextBotID.text?.toString()?.trim()
            var chatId = editTextChatID.text?.toString()?.trim()

            // Проверяем, что поля не пустые или null
            if (botId.isNullOrEmpty() || chatId.isNullOrEmpty()) {
                Toast.makeText(this, "Пожалуйста, заполните оба поля", Toast.LENGTH_SHORT).show()
            } else {
                // Сохраняем данные в SharedPreferences
                val sharedPref = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                with(sharedPref.edit()) {
                    putString(KEY_BOT_ID, botId)
                    putString(KEY_CHAT_ID, chatId)
                    apply() // или commit(), если нужно синхронно сохранить записи
                }
                Toast.makeText(this, "Данные сохранены", Toast.LENGTH_SHORT).show()
            }
        }


    }
}