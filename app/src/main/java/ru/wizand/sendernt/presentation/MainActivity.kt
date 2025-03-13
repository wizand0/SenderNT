package ru.wizand.sendernt.presentation

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Settings
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.Toast
import android.widget.ToggleButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationManagerCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.switchmaterial.SwitchMaterial
import com.microsoft.clarity.Clarity
import com.microsoft.clarity.ClarityConfig
import com.microsoft.clarity.models.LogLevel
import ru.wizand.sendernt.R
import ru.wizand.sendernt.data.service.NotificationLoggerService
import ru.wizand.sendernt.databinding.ActivityMainBinding
import ru.wizand.sendernt.presentation.SettingsGlobalActivity.Companion.KEY_SERVICE_ENABLED
import ru.wizand.sendernt.presentation.ViewUtils.showLongInstructionDialog

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // implementation Microsoft Clarity
        val config = ClarityConfig("qn253qo57u")
        Clarity.initialize(applicationContext, config)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Найдите Toolbar и задайте его в качестве ActionBar
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

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
        val sharedPref =
            getSharedPreferences(SettingsGlobalActivity.PREFS_NAME, Context.MODE_PRIVATE)
//        val botId = sharedPref.getString(SettingsGlobalActivity.KEY_BOT_ID, "No_data")
//        val chatId = sharedPref.getString(SettingsGlobalActivity.KEY_CHAT_ID, "No_data")

        // Кнопка инициализации службы
//        val toggleService = findViewById<ToggleButton>(R.id.toggle_service)
        val switchService: SwitchMaterial = findViewById(R.id.switch_service)





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


        // Получаем сохраненное состояние работы службы, по умолчанию false
        val isServiceEnabled = sharedPref.getBoolean(KEY_SERVICE_ENABLED, false)

        // Устанавливаем положение переключателя
//        toggleService.isChecked = isServiceEnabled
        switchService.isChecked = isServiceEnabled

        // Применяем состояние службы: включаем или выключаем компонент
        setNotificationServiceEnabled(isServiceEnabled)

        // Слушатель переключения toggleButton
//        toggleService.setOnCheckedChangeListener { _, isChecked ->
            // Изменяем состояние службы
//            setNotificationServiceEnabled(isChecked)
            // Сохраняем новое состояние в SharedPreferences
//            with(sharedPref.edit()) {
//                putBoolean(KEY_SERVICE_ENABLED, isChecked)
//                apply() // можно использовать commit(), если нужна синхронная запись
//            }
//        }

        switchService.setOnCheckedChangeListener { _, isChecked ->
            // Изменяем состояние службы
            setNotificationServiceEnabled(isChecked)
            // Сохраняем новое состояние в SharedPreferences
            with(sharedPref.edit()) {
                putBoolean(KEY_SERVICE_ENABLED, isChecked)
                apply() // можно использовать commit(), если нужна синхронная запись
            }
        }




    }

    /**
     * Проверяет, включен ли доступ к уведомлениям для данного приложения.
     */
    private fun isNotificationServiceEnabled(): Boolean {
        val enabledPackages = NotificationManagerCompat.getEnabledListenerPackages(this)
        return enabledPackages.contains(packageName)
    }

    /**
     * Включает или отключает компонент NotificationLoggerService.
     *
     * При включении компонент переводится в состояние
     * PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
     * при отключении – в состояние COMPONENT_ENABLED_STATE_DISABLED.
     *
     *
     * Note: При отключенном состоянии служба не будет активироваться системой.
     */
    private fun setNotificationServiceEnabled(enabled: Boolean) {
        val componentName = ComponentName(this, NotificationLoggerService::class.java)

        // Достаем данные из SharedPreferences
        val sharedPref =
            getSharedPreferences(SettingsGlobalActivity.PREFS_NAME, Context.MODE_PRIVATE)
        val botId = sharedPref.getString(
            SettingsGlobalActivity.KEY_BOT_ID,
            "No_data"
        )
        val chatId = sharedPref.getString(SettingsGlobalActivity.KEY_CHAT_ID, "No_data")

//        Toast.makeText(this, "botId: $botId; chatId: $chatId", Toast.LENGTH_SHORT).show()

        val newState = if (enabled)
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED
        else
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED


        // Проверка на null настроек чата telegram или значение по умолчанию
        if (botId.isNullOrEmpty() || chatId.isNullOrEmpty() || botId == "No_data" || chatId == "No_data") {

            showLongInstructionDialog(this)

            Toast.makeText(this, getString(R.string.preferences_for_telegram), Toast.LENGTH_SHORT)
                .show()
        } else {

//            Toast.makeText(this, "botId: $botId; chatId: $chatId", Toast.LENGTH_SHORT).show()
            // Данные получены, можно работать с botId и chatId
            packageManager.setComponentEnabledSetting(
                componentName,
                newState,
                PackageManager.DONT_KILL_APP
            )
        }
    }


    // Метод для отображения меню на тулбаре/аппбаре
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    // Обработка нажатий элементов меню
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_about -> {
                // Переход к SystemAppsActivity
                val intent = Intent(this, AboutActivity::class.java)
                startActivity(intent)
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }
}