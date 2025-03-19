package ru.wizand.sendernt.presentation

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AlertDialog
import ru.wizand.sendernt.R

class RateDialogHelper(private val context: Context) {

    companion object {
        private const val PREFS_NAME = "rate_prefs"
        private const val KEY_NEXT_PROMPT_TIME = "next_prompt_time"
        private const val KEY_DO_NOT_ASK = "do_not_ask"
        private const val ONE_WEEK_MILLIS = 7 * 24 * 60 * 60 * 1000L
    }

    // Метод для проверки условий показа диалога
    fun shouldShowRateDialog(): Boolean {
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
            .setTitle("Оцените наше приложение")
            .setMessage("Если вам нравится приложение, оставьте, пожалуйста, отзыв. Это не займет много времени.")
            // Кнопка "Оставить отзыв"
            .setPositiveButton(context.getString(R.string.google_play)) { dialog, _ ->
                openAppInStore()
                setDoNotAskAgain(true)
                dialog.dismiss()
            }
            // Кнопка "Написать на почту"
            .setNeutralButton(context.getString(R.string.enter_feedback)) { dialog, _ ->
                sendFeedbackEmail()
                setDoNotAskAgain(true)
                dialog.dismiss()
            }
            // Кнопка "Спросить позднее"
            .setNegativeButton("Спросить позднее") { dialog, _ ->
                setNextPromptTime(System.currentTimeMillis() + ONE_WEEK_MILLIS)
                dialog.dismiss()
            }
            .create()
            .show()
    }

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
