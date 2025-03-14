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
import android.view.ViewTreeObserver
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationManagerCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.switchmaterial.SwitchMaterial
import com.microsoft.clarity.Clarity
import com.microsoft.clarity.ClarityConfig
import com.yandex.mobile.ads.banner.BannerAdEventListener
import com.yandex.mobile.ads.banner.BannerAdSize
import com.yandex.mobile.ads.banner.BannerAdView
import com.yandex.mobile.ads.common.AdRequest
import com.yandex.mobile.ads.common.AdRequestError
import com.yandex.mobile.ads.common.ImpressionData
import ru.wizand.sendernt.R
import ru.wizand.sendernt.data.service.NotificationLoggerService
import ru.wizand.sendernt.databinding.ActivityMainBinding
import ru.wizand.sendernt.presentation.SettingsGlobalActivity.Companion.KEY_SERVICE_ENABLED
import ru.wizand.sendernt.presentation.ViewUtils.showLongInstructionDialog
import kotlin.math.roundToInt


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private lateinit var bannerAdView: BannerAdView
    private var bannerAd: BannerAdView? = null

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


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        // implementation Microsoft Clarity
        val config = ClarityConfig("qn253qo57u")
        Clarity.initialize(applicationContext, config)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        // Since we're loading the banner based on the adContainerView size,
        // we need to wait until this view is laid out before we can get the width
        binding.bannerAdView.viewTreeObserver.addOnGlobalLayoutListener(object :
            ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                binding.bannerAdView.viewTreeObserver.removeOnGlobalLayoutListener(this);
                bannerAd = loadBannerAd(adSize)
            }
        })


        // Найдите Toolbar и задайте его в качестве ActionBar
//        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        val toolbar = binding.toolbar
        setSupportActionBar(toolbar)

        enableEdgeToEdge()
//        setContentView(R.layout.activity_main)


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
//        val switchService: SwitchMaterial = findViewById(R.id.switch_service)
        val switchService: SwitchMaterial = binding.switchService

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

    override fun onDestroy() {
        super.onDestroy()
        // Освобождаем ресурсы, связанные с рекламным блоком
        bannerAdView.destroy()
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

                override fun onAdFailedToLoad(adRequestError: AdRequestError) {
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


    companion object {
        val YOUR_BLOCK_ID: String = "R-M-14532326-1"
    }


}