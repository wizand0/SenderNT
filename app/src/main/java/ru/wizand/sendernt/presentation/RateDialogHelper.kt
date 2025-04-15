package ru.wizand.sendernt.presentation

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AlertDialog
import ru.wizand.sendernt.R
import android.content.pm.PackageManager

class RateDialogHelper(private val context: Context) {

    companion object {
        private const val PREFS_NAME = "rate_prefs"
        private const val KEY_NEXT_PROMPT_TIME = "next_prompt_time"
        private const val KEY_DO_NOT_ASK = "do_not_ask"
        private const val ONE_WEEK_MILLIS = 7 * 24 * 60 * 60 * 1000L

        // Константы для выбора магазина приложений
        private val GOOGLE_PLAY_PACKAGE = "com.android.vending"
        private val RUSTORE_PACKAGE = "ru.vk.store"
    }

    // Метод для проверки условий показа диалога (прошло больше недели после установки)
    private fun shouldShowRateDialog(): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        if (prefs.getBoolean(KEY_DO_NOT_ASK, false)) {
            return false
        }
        val nextPromptTime = prefs.getLong(KEY_NEXT_PROMPT_TIME, 0)
        return System.currentTimeMillis() >= nextPromptTime
    }

    fun showRateDialogIfNeeded() {
        if (shouldShowRateDialog()) {
            showRateDialog()
        }
    }

    // Метод для отображения диалога
    private fun showRateDialog() {
        AlertDialog.Builder(context)
            .setTitle(context.getString(R.string.feedback_title_dialog))
            .setMessage(context.getString(R.string.feedback_body_dialog))
            // Кнопка "Оставить отзыв"
            .setPositiveButton(context.getString(R.string.market)) { dialog, _ ->

//                openAppInStore()
//                openAppInRuStore()
                onStoreChoiceDialogRequested()
                setDoNotAskAgain(true)
                dialog.dismiss()
            }
            // Кнопка "Спросить позднее"
            .setNeutralButton(context.getString(R.string.ask_later)) { dialog, _ ->
                setNextPromptTime(System.currentTimeMillis() + ONE_WEEK_MILLIS)
                dialog.dismiss()
            }
            // Кнопка "Написать на почту"
            .setNegativeButton(context.getString(R.string.enter_feedback)) { dialog, _ ->
                sendFeedbackEmail()
                setDoNotAskAgain(true)
                dialog.dismiss()
            }
            .create()
            .show()
    }

//     For Google Play
    private fun openAppInStore() {
        val appPackageName = context.packageName
        try {
            context.startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("market://details?id=$appPackageName")
                )
            )
        } catch (e: ActivityNotFoundException) {
            context.startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/details?id=$appPackageName")
                )
            )
        }
    }

    // For RuStore
    private fun openAppInRuStore() {
        val appPackageName = context.packageName
        try {
            context.startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://www.rustore.ru/catalog/app/$appPackageName")
                )
            )
        } catch (e: ActivityNotFoundException) {
            // Если на устройстве нет приложения RuStore или оно не откликается,
            // открываем RuStore в браузере
            context.startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://www.rustore.ru/catalog/app/$appPackageName")
                )
            )
        }
    }

    // Метод, вызываемый при нажатии на кнопку в основном AlertDialog
    private fun onStoreChoiceDialogRequested() {
        val isGooglePlayAvailable = isPackageInstalled(GOOGLE_PLAY_PACKAGE)
        val isRuStoreAvailable = isPackageInstalled(RUSTORE_PACKAGE)

        when {
            isGooglePlayAvailable && isRuStoreAvailable -> {
                // Если оба магазина доступны, покажем диалог выбора
                showStoreSelectionDialog()
            }
            isGooglePlayAvailable -> {
                // Если доступен только Google Play, открываем его
                openAppInStore()
            }
            isRuStoreAvailable -> {
                // Если доступен только RuStore, открываем его
                openAppInRuStore()
            }
            else -> {
                // Если ни одного нет, можно, например, показать сообщение об ошибке
                showErrorStoreNotFound()
            }
        }
    }

    // Функция для проверки, установлен ли пакет
    private fun isPackageInstalled(packageName: String): Boolean {
        return try {
            context.packageManager.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }

    // Диалог выбора между магазинами
    private fun showStoreSelectionDialog() {
        val items = arrayOf("Google Play", "RuStore")
        AlertDialog.Builder(context)
            .setTitle(context.getString(R.string.choose_market))
            .setItems(items) { dialog, which ->
                when (which) {
                    0 -> openAppInStore()
                    1 -> openAppInRuStore()
                }
            }
            .setCancelable(true)
            .show()
    }

    private fun showErrorStoreNotFound() {
        AlertDialog.Builder(context)
            .setTitle("Ошибка")
            .setMessage(context.getString(R.string.not_found))
            .setPositiveButton("OK", null)
            .show()
    }



    // Сохраняем дату следующего показа окна
    private fun setNextPromptTime(timeInMillis: Long) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putLong(KEY_NEXT_PROMPT_TIME, timeInMillis).apply()
    }

    // Сохраняем флаг "Не спрашивать более"
    private fun setDoNotAskAgain(doNotAsk: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_DO_NOT_ASK, doNotAsk).apply()
    }


    private fun sendFeedbackEmail() {
        // Собираем информацию об устройстве
        val deviceInfo = """
        Model: ${Build.MANUFACTURER} ${Build.MODEL}
        Android: ${Build.VERSION.RELEASE} (SDK ${Build.VERSION.SDK_INT})
    """.trimIndent()

        // Текст письма может содержать дополнительный текст с просьбой
        val feedbackBody = context.getString(R.string.feedback_body_mail, deviceInfo).trimIndent()

        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:") // Только почтовые клиенты
            putExtra(Intent.EXTRA_EMAIL, arrayOf("makandrei@gmail.com"))
            putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.feedback_mail_text))
            putExtra(Intent.EXTRA_TEXT, feedbackBody)
        }
        if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(intent)
        }
    }


}
