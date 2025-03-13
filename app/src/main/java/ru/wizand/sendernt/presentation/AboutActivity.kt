package ru.wizand.sendernt.presentation

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.microsoft.clarity.Clarity
import com.microsoft.clarity.ClarityConfig
import ru.wizand.sendernt.R

class AboutActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_about)

        // implementation Microsoft Clarity
        val config = ClarityConfig("qn253qo57u")
        Clarity.initialize(applicationContext, config)

        // Устанавливаем номер версии приложения.
//        val tvVersion = findViewById<TextView>(R.id.tvVersion)
        // BuildConfig.VERSION_NAME получает версию, указанную в build.gradle (например, "1.0.0")
//        tvVersion.text = buildString {
//            append("Версия: ")
//            append(BuildConfig.VERSION_NAME)
//        }

        // Чтобы ссылки в TextView стали кликабельными
        findViewById<TextView>(R.id.tvGithubAuthor).movementMethod =
            LinkMovementMethod.getInstance()
        findViewById<TextView>(R.id.tvGithubProject).movementMethod =
            LinkMovementMethod.getInstance()
        findViewById<TextView>(R.id.tvEmail).movementMethod = LinkMovementMethod.getInstance()


        // Формируем юридическую информацию с дополнительным текстом
        val legalInfoText = getString(R.string.legals).trimIndent()

        val tvLegalInfo = findViewById<TextView>(R.id.tvLegalInfo)

        tvLegalInfo.text = legalInfoText
        // Найти ImageView по ID
        val imDonate: ImageView = findViewById(R.id.imDonate)

        // Установить обработчик кликов
        imDonate.setOnClickListener {
            // Ссылка, которую нужно открыть
            val url = "https://pay.cloudtips.ru/p/ae98679b"

            // Создать Intent для открытия ссылки
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse(url)
            }

// Проверяем, есть ли приложения для обработки Intent
            if (intent.resolveActivity(packageManager) != null) {
                startActivity(intent)
            } else {
                // Обработайте случай, если нет подходящих приложений
                Toast.makeText(this, "Нет приложений для открытия ссылки", Toast.LENGTH_SHORT)
                    .show()
            }
        }
        // Если требуется сделать ссылку на email дополнительным обработчиком, можно установить onClickListener:
        // val tvEmail = findViewById<TextView>(R.id.tvEmail)
        // tvEmail.setOnClickListener {
        //     val intent = Intent(Intent.ACTION_SENDTO).apply {
        //         data = Uri.parse("mailto:makandrei@gmail.com")
        //         putExtra(Intent.EXTRA_SUBJECT, "Обратная связь")
        //     }
        //     startActivity(intent)
        // }
    }
}
