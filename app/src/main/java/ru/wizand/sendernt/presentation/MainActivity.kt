package ru.wizand.sendernt.presentation

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewTreeObserver
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationManagerCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.materialswitch.MaterialSwitch
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
import ru.wizand.sendernt.data.TelegramRepository
import ru.wizand.sendernt.data.service.NotificationLoggerService
import ru.wizand.sendernt.databinding.ActivityMainBinding
import ru.wizand.sendernt.presentation.SettingsGlobalActivity.Companion.KEY_SERVICE_ENABLED
import ru.wizand.sendernt.presentation.ViewUtils.showLongInstructionDialog
import kotlin.math.roundToInt

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: MainViewModel

    private var bannerAd: BannerAdView? = null

    private val adSize: BannerAdSize
        get() {
            var adWidthPixels = binding.bannerAdView.width
            if (adWidthPixels == 0) {
                adWidthPixels = resources.displayMetrics.widthPixels
            }
            val adWidth = (adWidthPixels / resources.displayMetrics.density).roundToInt()
            return BannerAdSize.stickySize(this, adWidth)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Telegram ViewModel + репозиторий
        viewModel = MainViewModel(TelegramRepository())

        // Microsoft Clarity
        val config = ClarityConfig("qn253qo57u")
        Clarity.initialize(applicationContext, config)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Настройка тулбара
        setSupportActionBar(binding.toolbar)

        // Подписка на результат отправки в Telegram
        viewModel.messageStatus.observe(this) { success ->
            val msg = if (success) "Сообщение успешно отправлено 📬" else "Ошибка отправки ❌"
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
        }

        // Кнопка тестовой отправки
        binding.btnSendTest.setOnClickListener {
            val sharedPref = getSharedPreferences(SettingsGlobalActivity.PREFS_NAME, Context.MODE_PRIVATE)
            val botToken = sharedPref.getString(SettingsGlobalActivity.KEY_BOT_ID, null)
            val chatId = sharedPref.getString(SettingsGlobalActivity.KEY_CHAT_ID, null)

            if (!botToken.isNullOrEmpty() && !chatId.isNullOrEmpty()) {
                viewModel.sendTestMessage(botToken, chatId, getString(R.string.test_message))
            } else {
                Toast.makeText(this, "Укажите токен и chatId в настройках", Toast.LENGTH_SHORT).show()
            }
        }

        // Разрешение на уведомления
        if (isNotificationServiceEnabled()) {
            binding.btnEnableNotificationAccess.visibility = View.GONE
        } else {
            binding.btnEnableNotificationAccess.setOnClickListener {
                startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
            }
        }

        // Состояние службы
        val sharedPref = getSharedPreferences(SettingsGlobalActivity.PREFS_NAME, Context.MODE_PRIVATE)
        val isServiceEnabled = sharedPref.getBoolean(KEY_SERVICE_ENABLED, false)
        val switchService: MaterialSwitch = binding.switchService
        switchService.isChecked = isServiceEnabled
        setNotificationServiceEnabled(isServiceEnabled)

        switchService.setOnCheckedChangeListener { _, isChecked ->
            setNotificationServiceEnabled(isChecked)
            with(sharedPref.edit()) {
                putBoolean(KEY_SERVICE_ENABLED, isChecked)
                apply()
            }
        }

        // Кнопка настроек
        binding.btnSettings.setOnClickListener {
            startActivity(Intent(this, SettingsGlobalActivity::class.java))
        }

        // Insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Баннер
        binding.bannerAdView.viewTreeObserver.addOnGlobalLayoutListener(object :
            ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                if (isFinishing || isDestroyed) {
                    binding.bannerAdView.viewTreeObserver.removeOnGlobalLayoutListener(this)
                    return
                }
                binding.bannerAdView.viewTreeObserver.removeOnGlobalLayoutListener(this)
                bannerAd = loadBannerAd(adSize)
            }
        })
    }

    override fun onResume() {
        super.onResume()
        val rateDialogHelper = RateDialogHelper(this)
        rateDialogHelper.showRateDialogIfNeeded()
    }

    override fun onDestroy() {
        super.onDestroy()
        bannerAd?.destroy()
    }

    private fun isNotificationServiceEnabled(): Boolean {
        val enabledPackages = NotificationManagerCompat.getEnabledListenerPackages(this)
        return enabledPackages.contains(packageName)
    }

    private fun setNotificationServiceEnabled(enabled: Boolean) {
        val componentName = ComponentName(this, NotificationLoggerService::class.java)
        val sharedPref = getSharedPreferences(SettingsGlobalActivity.PREFS_NAME, Context.MODE_PRIVATE)
        val botId = sharedPref.getString(SettingsGlobalActivity.KEY_BOT_ID, "No_data")
        val chatId = sharedPref.getString(SettingsGlobalActivity.KEY_CHAT_ID, "No_data")

        if (botId.isNullOrEmpty() || chatId.isNullOrEmpty() || botId == "No_data" || chatId == "No_data") {
            showLongInstructionDialog(this)
            Toast.makeText(this, getString(R.string.preferences_for_telegram), Toast.LENGTH_SHORT).show()
        } else {
            val newState = if (enabled)
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED
            else
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED

            packageManager.setComponentEnabledSetting(
                componentName,
                newState,
                PackageManager.DONT_KILL_APP
            )
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_about -> {
                startActivity(Intent(this, AboutActivity::class.java))
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
                    if (isDestroyed) {
                        bannerAd?.destroy()
                        return
                    }
                }
                override fun onAdFailedToLoad(error: AdRequestError) {}
                override fun onAdClicked() {}
                override fun onLeftApplication() {}
                override fun onReturnedToApplication() {}
                override fun onImpression(impressionData: ImpressionData?) {}
            })
            loadAd(AdRequest.Builder().build())
        }
    }

    companion object {
        const val YOUR_BLOCK_ID: String = "R-M-14532326-1"
        private const val TAG = "MainActivity"
    }
}
