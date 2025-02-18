package ru.wizand.sendernt.presentation

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Button
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