package ru.wizand.sendernt.presentation

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationManagerCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import ru.wizand.sendernt.R
import ru.wizand.sendernt.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        enableEdgeToEdge()
//        setContentView(R.layout.activity_main)

        setContentView(binding.root)

        // Если разрешение на доступ к уведомлениям уже дано, скрываем кнопку
        if (isNotificationServiceEnabled()) {
            binding.btnEnableNotificationAccess.visibility = View.GONE
        } else {
            // Если разрешения нет, назначаем обработчик клика для перехода к настройкам
            binding.btnEnableNotificationAccess.setOnClickListener {
                // Переход к настройкам уведомлений для данного приложения
                startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
            }
        }

        // Достаем данные из SharedPreferences
        val sharedPref = getSharedPreferences(SettingsGlobalActivity.PREFS_NAME, Context.MODE_PRIVATE)
        val botId = sharedPref.getString(SettingsGlobalActivity.KEY_BOT_ID, "Нет данных")
        val chatId = sharedPref.getString(SettingsGlobalActivity.KEY_CHAT_ID, "Нет данных")

        // Проверка на null или значение по умолчанию
        if (botId.isNullOrEmpty() || chatId.isNullOrEmpty() || botId == "Нет данных" || chatId == "Нет данных") {
            Toast.makeText(this, "Пожалуйста, укажите значения в настройках", Toast.LENGTH_SHORT).show()
        }
//        else {
//            // Данные получены, можно работать с botId и chatId
//        }



        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Находим кнопку по идентификатору
        val btnSettings = findViewById<Button>(R.id.btnSettings)
        btnSettings.setOnClickListener {
            // Создаем Intent для перехода в SettingsActivity
            val intent = Intent(this, SettingsGlobalActivity::class.java)
            startActivity(intent)
        }




    }

    /**
     * Проверяет, включен ли доступ к уведомлениям для данного приложения.
     */
    private fun isNotificationServiceEnabled(): Boolean {
        val enabledPackages = NotificationManagerCompat.getEnabledListenerPackages(this)
        return enabledPackages.contains(packageName)
    }
}