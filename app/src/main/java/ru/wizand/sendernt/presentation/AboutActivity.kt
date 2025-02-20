package ru.wizand.sendernt.presentation

import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import ru.wizand.sendernt.R
import android.content.Intent
import android.net.Uri
import android.text.method.LinkMovementMethod

class AboutActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_about)

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
