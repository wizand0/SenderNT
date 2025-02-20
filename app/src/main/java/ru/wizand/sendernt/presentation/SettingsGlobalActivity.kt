package ru.wizand.sendernt.presentation

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import ru.wizand.sendernt.R
import ru.wizand.sendernt.presentation.ViewUtils.showShortInstructionDialog

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

        // Найдите Toolbar и задайте его в качестве ActionBar
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
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

        val buttonOpenBotFather = findViewById<ImageButton>(R.id.buttonOpenBotFather)
        val buttonOpenGetIdBot = findViewById<ImageButton>(R.id.buttonOpenGetIdBot)

        buttonOpenBotFather.setOnClickListener {
            openTelegramBot("BotFather")
        }

        buttonOpenGetIdBot.setOnClickListener {
            openTelegramBot("get_id_bot")
        }

        // Читаем сохранённые значения из SharedPreferences с дефолтным значением "No_data"
        val sharedPref = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val savedBotId = sharedPref.getString(KEY_BOT_ID, "No_data")
        val savedChatId = sharedPref.getString(KEY_CHAT_ID, "No_data")

        // Если одно из значений отсутствует или равно "No_data", то устанавливаем "No_data"
        if (savedBotId == null || savedBotId == "No_data") {
            editTextBotID.setText(getString(R.string.no_data))
        } else {
            editTextBotID.setText(savedBotId)
        }

        if (savedChatId == null || savedChatId == "No_data") {
            editTextChatID.setText(getString(R.string.no_data))
        } else {
            editTextChatID.setText(savedChatId)
        }

        if (savedChatId == null || savedChatId == "No_data" || savedBotId == null || savedBotId == "No_data") {
            showShortInstructionDialog(this)
        }



        buttonSave.setOnClickListener {
            // Получаем текст из EditText и обрезаем пробелы
            val botId = editTextBotID.text?.toString()?.trim()
            val chatId = editTextChatID.text?.toString()?.trim()

            // Проверяем, что поля не пустые или null
            if (botId.isNullOrEmpty() || chatId.isNullOrEmpty() || botId == "No_data" || chatId == "No_data" || botId == getString(
                    R.string.no_data
                ) || chatId == getString(R.string.no_data)
            ) {
                Toast.makeText(this, getString(R.string.enter_all_fields), Toast.LENGTH_SHORT)
                    .show()
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

            // Если приложение не исключено из оптимизаций, запускаем запрос
            if (!powerManager.isIgnoringBatteryOptimizations(packageName)) {
                val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                    data = Uri.parse("package:$packageName")
                }
                // Запускаем активити для запроса у пользователя
                context.startActivity(intent)
            } else {
                Toast.makeText(this, getString(R.string.permission_granted), Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    private fun openTelegramBot(botUsername: String) {
        val uri = Uri.parse("https://t.me/$botUsername")
        Toast.makeText(this, uri.toString(), Toast.LENGTH_SHORT).show()
        val intent = Intent(Intent.ACTION_VIEW, uri)
        startActivity(intent)
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