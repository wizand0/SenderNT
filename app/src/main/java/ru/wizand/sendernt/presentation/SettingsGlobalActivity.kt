package ru.wizand.sendernt.presentation

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
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
import okhttp3.OkHttpClient
import ru.wizand.sendernt.R
import ru.wizand.sendernt.data.utils.TelegramUtils
import ru.wizand.sendernt.databinding.ActivitySettingsGlobalBinding
import ru.wizand.sendernt.presentation.ViewUtils.showShortInstructionDialog
import kotlin.math.roundToInt

class SettingsGlobalActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsGlobalBinding

    private lateinit var bannerAdView: BannerAdView
    private var bannerAd: BannerAdView? = null

    private lateinit var editTextBotID: TextInputEditText
    private lateinit var editTextChatID: TextInputEditText
    private lateinit var buttonTest: Button

    private val httpClient = OkHttpClient()

    private val adSize: BannerAdSize
        get() {
            // Calculate the width of the ad, taking into account the padding in the ad container.
            var adWidthPixels = binding.bannerAdView.width
            if (adWidthPixels == 0) {
                // If the ad hasn't been laid out, default to the full screen width
                adWidthPixels = resources.displayMetrics.widthPixels
            }
            val adWidth = (adWidthPixels / resources.displayMetrics.density).roundToInt()

            return BannerAdSize.stickySize(this, adWidth)
        }

    // Ключи для SharedPreferences
    companion object {

        const val YOUR_BLOCK_ID: String = "R-M-14532326-2"

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
        binding = ActivitySettingsGlobalBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.bannerAdView.viewTreeObserver.addOnGlobalLayoutListener(object :
            ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                // Сначала убеждаемся, что активность ещё жива.
                if (isFinishing || isDestroyed) {
                    binding.bannerAdView.viewTreeObserver.removeOnGlobalLayoutListener(this)
                    return
                }
                binding.bannerAdView.viewTreeObserver.removeOnGlobalLayoutListener(this)
                bannerAd = loadBannerAd(adSize)
            }
        })

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
            if(buttonTest.isEnabled) {
                val botId = editTextBotID.text.toString().trim()
                val chatId = editTextChatID.text.toString().trim()
                Toast.makeText(this, getString(R.string.data_send), Toast.LENGTH_SHORT).show()
                TelegramUtils.sendTestTextToServer(this, botId, chatId, buttonTest)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Освобождаем ресурсы, связанные с рекламным блоком
        if(::bannerAdView.isInitialized) {
            bannerAdView.destroy()
        }
    }

    private fun loadBannerAd(adSize: BannerAdSize): BannerAdView {
        return binding.bannerAdView.apply {
            setAdSize(adSize)
            setAdUnitId(YOUR_BLOCK_ID)
            setBannerAdEventListener(object : BannerAdEventListener {
                override fun onAdLoaded() {
                    // If this callback occurs after the activity is destroyed, you
                    // must call destroy and return or you may get a memory leak.
                    // Note `isDestroyed` is a method on Activity.
                    if (isDestroyed) {
                        bannerAd?.destroy()
                        return
                    }
                }

                override fun onAdFailedToLoad(error: AdRequestError) {
                    // Ad failed to load with AdRequestError.
                    // Attempting to load a new ad from the onAdFailedToLoad() method is strongly discouraged.
                }

                override fun onAdClicked() {
                    // Called when a click is recorded for an ad.
                }

                override fun onLeftApplication() {
                    // Called when user is about to leave application (e.g., to go to the browser), as a result of clicking on the ad.
                }

                override fun onReturnedToApplication() {
                    // Called when user returned to application after click.
                }

                override fun onImpression(impressionData: ImpressionData?) {
                    // Called when an impression is recorded for an ad.
                }
            })
            loadAd(
                AdRequest.Builder()
                    // Methods in the AdRequest.Builder class can be used here to specify individual options settings.
                    .build()
            )
        }
    }


    // Функция проверки полей
    private fun checkFields() {
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


}