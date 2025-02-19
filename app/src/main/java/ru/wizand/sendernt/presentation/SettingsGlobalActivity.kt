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
import android.os.PowerManager
import android.os.Build
import android.provider.Settings
import android.net.Uri

class SettingsGlobalActivity : AppCompatActivity() {

    // Ключи для SharedPreferences
    companion object {
        const val PREFS_NAME = "MyPrefs"
        // Ключ для настройки пересылки уведомлений в телеграм
        const val KEY_BOT_ID = "BOT_ID"
        const val KEY_CHAT_ID = "CHAT_ID"
        // Ключ для сохранения состояния работы службы
        const val KEY_SERVICE_ENABLED = "SERVICE_ENABLED"
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

        val btnBatteryOptimization = findViewById<Button>(R.id.btnBatteryOptimization)
        btnBatteryOptimization.setOnClickListener {
            requestIgnoreBatteryOptimizations(this)
        }

        val editTextBotID = findViewById<EditText>(R.id.editTextBotID)
        val editTextChatID = findViewById<EditText>(R.id.editTextChatID)
        val buttonSave = findViewById<Button>(R.id.buttonSave)

        buttonSave.setOnClickListener {
            // Получаем текст из EditText и обрезаем пробелы
            val botId = editTextBotID.text?.toString()?.trim()
            val chatId = editTextChatID.text?.toString()?.trim()

            // Проверяем, что поля не пустые или null
            if (botId.isNullOrEmpty() || chatId.isNullOrEmpty() || botId == "No_data" || chatId == "No_data" || botId == getString(R.string.no_data) || chatId == getString(R.string.no_data)) {
                Toast.makeText(this, getString(R.string.enter_all_fields), Toast.LENGTH_SHORT).show()
            } else {
                // Сохраняем данные в SharedPreferences
                val sharedPref = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                with(sharedPref.edit()) {
                    putString(KEY_BOT_ID, botId)
                    putString(KEY_CHAT_ID, chatId)
                    apply() // или commit(), если нужно синхронно сохранить записи
                }
                Toast.makeText(this, getString(R.string.data_saved), Toast.LENGTH_SHORT).show()
            }
        }

    }

    private fun requestIgnoreBatteryOptimizations(context: Context) {
        // Проверяем, что версия Android >= Marshmallow (API 23)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val packageName = context.packageName
            val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager

//            val someTest = !powerManager.isIgnoringBatteryOptimizations(packageName)
//
//            Toast.makeText(this, "$someTest", Toast.LENGTH_SHORT).show()

            // Если приложение не исключено из оптимизаций, запускаем запрос
            if (!powerManager.isIgnoringBatteryOptimizations(packageName)) {
                val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                    data = Uri.parse("package:$packageName")
                }
                // Запускаем активити для запроса у пользователя
                context.startActivity(intent)
            } else {
                Toast.makeText(this, getString(R.string.permission_granted), Toast.LENGTH_SHORT).show()
            }
        }
    }
}