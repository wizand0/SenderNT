package ru.wizand.sendernt.presentation

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.ViewTreeObserver
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import com.google.android.material.textfield.TextInputEditText
import com.microsoft.clarity.Clarity
import com.microsoft.clarity.ClarityConfig
import com.yandex.mobile.ads.banner.BannerAdEventListener
import com.yandex.mobile.ads.banner.BannerAdSize
import com.yandex.mobile.ads.banner.BannerAdView
import com.yandex.mobile.ads.common.AdRequest
import com.yandex.mobile.ads.common.AdRequestError
import com.yandex.mobile.ads.common.ImpressionData
import okhttp3.Call
import okhttp3.Callback
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import ru.wizand.sendernt.R
import ru.wizand.sendernt.databinding.ActivitySettingsGlobalBinding
import ru.wizand.sendernt.presentation.MainActivity.Companion.YOUR_BLOCK_ID
import ru.wizand.sendernt.presentation.ViewUtils.showShortInstructionDialog
import java.io.IOException
import java.util.Date
import kotlin.math.roundToInt

class SettingsGlobalActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsGlobalBinding

    private lateinit var editTextBotID: TextInputEditText
    private lateinit var editTextChatID: TextInputEditText
    private lateinit var buttonTest: Button

    private val httpClient = OkHttpClient()

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
        // setContentView(R.layout.activity_settings_global)
        binding = ActivitySettingsGlobalBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // implementation Microsoft Clarity
        val config = ClarityConfig("qn253qo57u")
        Clarity.initialize(applicationContext, config)

        // Найдите Toolbar и задайте его в качестве ActionBar
        val toolbar = binding.toolbar
        setSupportActionBar(toolbar)

        val btnSettings = binding.btnApps
        btnSettings.setOnClickListener {
            // Создаем Intent для перехода в SettingsActivity
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }

        val btnBatteryOptimization = binding.btnBatteryOptimization
        btnBatteryOptimization.setOnClickListener {
            requestIgnoreBatteryOptimizations(this)
        }

//        val editTextBotID = findViewById<EditText>(R.id.editTextBotID)
//        val editTextChatID = findViewById<EditText>(R.id.editTextChatID)
//        val buttonSave = findViewById<Button>(R.id.buttonSave)
//        val buttonOpenBotFather = findViewById<ImageButton>(R.id.buttonOpenBotFather)
//        val buttonOpenGetIdBot = findViewById<ImageButton>(R.id.buttonOpenGetIdBot)

        // Получаем ссылки на контейнеры TextInputLayout и сам редактируемый текст
        val textInputLayoutBot = binding.textInputLayoutBot
        val textInputLayoutChat = binding.textInputLayoutChat

        editTextBotID = binding.editTextBotID
        editTextChatID = binding.editTextChatID
        buttonTest = binding.buttonTest
        val buttonSave = binding.buttonSave
        val buttonOpenBotFather = binding.buttonOpenBotFather
        val buttonOpenGetIdBot = binding.buttonOpenGetIdBot

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

//        // Если одно из значений отсутствует или равно "No_data", то устанавливаем "No_data"
//        if (savedBotId == null || savedBotId == "No_data") {
//            editTextBotID.setText(getString(R.string.no_data))
//        } else {
//            editTextBotID.setText(savedBotId)
//        }
//
//        if (savedChatId == null || savedChatId == "No_data") {
//            editTextChatID.setText(getString(R.string.no_data))
//        } else {
//            editTextChatID.setText(savedChatId)
//        }
//
//        if (savedChatId == null || savedChatId == "No_data" || savedBotId == null || savedBotId == "No_data") {
//            showShortInstructionDialog(this)
//        }

        //После смены EditText данная проверка избыточна
        // Устанавливаем сохранённые значения в поля ввода
//        if (savedBotId == null || savedBotId == "No_data") {
//            editTextBotID.setHint(getString(R.string.no_data))
//        } else {
//            editTextBotID.setText(savedBotId)
//        }
//        if (savedChatId == null || savedChatId == "No_data") {
//            editTextChatID.setText(getString(R.string.no_data))
//        } else {
//            editTextChatID.setText(savedChatId)
//        }

        if (savedBotId != "No_data") {
            editTextBotID.setText(savedBotId)
        }

        if (savedChatId != "No_data") {
            editTextChatID.setText(savedChatId)
        }

        // Если данные не заданы, можно отобразить диалог с инструкцией
        if ((savedChatId == null || savedChatId == "No_data")
            && (savedBotId == null || savedBotId == "No_data")
        ) {
            showShortInstructionDialog(this)
        }

//        buttonSave.setOnClickListener {
//            // Получаем текст из EditText и обрезаем пробелы
//            val botId = editTextBotID.text?.toString()?.trim()
//            val chatId = editTextChatID.text?.toString()?.trim()
//
//            // Проверяем, что поля не пустые или null
//            if (botId.isNullOrEmpty() || chatId.isNullOrEmpty() || botId == "No_data" || chatId == "No_data" || botId == getString(
//                    R.string.no_data
//                ) || chatId == getString(R.string.no_data)
//            ) {
//                Toast.makeText(this, getString(R.string.enter_all_fields), Toast.LENGTH_SHORT)
//                    .show()
//            } else {
//                // Сохраняем данные в SharedPreferences
//                @Suppress("NAME_SHADOWING") val sharedPref = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
//                with(sharedPref.edit()) {
//                    putString(KEY_BOT_ID, botId)
//                    putString(KEY_CHAT_ID, chatId)
//                    apply() // или commit(), если нужно синхронно сохранить записи
//                }
//                Toast.makeText(this, getString(R.string.data_saved), Toast.LENGTH_SHORT).show()
//            }
//        }

        // Убираем ошибку если ранее появилась при неправильном вводе у editTextBotID
        editTextBotID.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Ничего не делаем
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Сбрасываем ошибку при каждом изменении текста
                textInputLayoutBot.error = null
            }

            override fun afterTextChanged(s: Editable?) {
                // Ничего не делаем
            }
        })

        // Убираем ошибку если ранее появилась при неправильном вводе у editTextChatID
        editTextChatID.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Ничего не делаем
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Сбрасываем ошибку при каждом изменении текста
                textInputLayoutChat.error = null
            }

            override fun afterTextChanged(s: Editable?) {
                // Ничего не делаем
            }
        })

        // Первичная проверка:
        checkFields()

        // ??? For testing
        editTextBotID.addTextChangedListener { checkFields() }
        editTextChatID.addTextChangedListener { checkFields() }

        buttonSave.setOnClickListener {
            // Получаем текст и обрезаем пробелы
            val botId = editTextBotID.text?.toString()?.trim()
            val chatId = editTextChatID.text?.toString()?.trim()

            var isValid = true

            // Проверяем, что поле для BotID заполнено корректно
            if (botId.isNullOrEmpty() || botId == "No_data" || botId == getString(R.string.no_data)) {
                // Устанавливаем ошибку в TextInputLayout - появится анимация и/или подсказка об ошибке
                textInputLayoutBot.error = getString(R.string.enter_bot_id_error)

                isValid = false
            } else {
                // Убираем ошибку, если введённое значение корректно
                textInputLayoutBot.error = null
            }

            // Проверяем поле для ChatID
            if (chatId.isNullOrEmpty() || chatId == "No_data" || chatId == getString(R.string.no_data)) {
                textInputLayoutChat.error = getString(R.string.enter_chat_id_error)
                isValid = false
            } else {
                textInputLayoutChat.error = null
            }

            // Если все поля заполнены корректно, сохраняем данные
            if (isValid) {
                with(sharedPref.edit()) {
                    putString(KEY_BOT_ID, botId)
                    putString(KEY_CHAT_ID, chatId)
                    apply() // Или commit(), если нужна синхронная запись
                }
                Toast.makeText(this, getString(R.string.data_saved), Toast.LENGTH_SHORT).show()
                checkFields()
            } else {
                Toast.makeText(this, getString(R.string.enter_all_fields), Toast.LENGTH_SHORT)
                    .show()
            }
        }

        // Добавить обработку нажатия для buttonTest
        buttonTest.setOnClickListener {
            // Например, можно открыть новое Activity или выполнить тестовую операцию
            Toast.makeText(this, getString(R.string.data_send), Toast.LENGTH_SHORT).show()
            sendTestTextToServer()

        }
    }


    // Функция проверки полей
    fun checkFields() {
        val botId = editTextBotID.text.toString().trim()
        val chatId = editTextChatID.text.toString().trim()

        if (botId.isEmpty() || chatId.isEmpty()) {
            buttonTest.isEnabled = false
            buttonTest.setBackgroundColor(ContextCompat.getColor(this, R.color.gray))
        } else {
            buttonTest.isEnabled = true
            buttonTest.setBackgroundColor(ContextCompat.getColor(this, R.color.purple_500))
        }
    }

    private fun requestIgnoreBatteryOptimizations(context: Context) {
        // Проверяем, что версия Android >= Marshmallow (API 23)
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

    // Отправка тестового сообщения
    private fun sendTestTextToServer() {
        val botId = editTextBotID.text.toString().trim()
        val chatId = editTextChatID.text.toString().trim()

        val time = Date()
        val text = "Test message"
        val title = "Test title"

        val message = " -> $time - $title - $text"

        // Формируем URL запроса к Telegram Bot API
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
        httpClient.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("TAG", "Ошибка при отправке сообщения в Telegram: ${e.localizedMessage}")
            }

            override fun onResponse(call: Call, response: Response) {
                if (!response.isSuccessful) {
                    Log.e("TAG", "Ошибка при отправке сообщения в Telegram: ${response.message}")
                } else {
                    Log.d("TAG", "Сообщение успешно отправлено через Telegram")
                    buttonTest.setBackgroundColor(getColor(R.color.gray))
                    buttonTest.isClickable = false
                }
                response.close()
            }
        })
    }

}