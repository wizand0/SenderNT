package ru.wizand.sendernt.presentation

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import ru.wizand.sendernt.R

class SettingsGlobalActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_settings_global)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val btnBack = findViewById<Button>(R.id.btnBack)
        btnBack.setOnClickListener {
            // Завершаем активность, возвращаясь к предыдущей (MainActivity)
            finish()
        }

        val btnSettings = findViewById<Button>(R.id.btnApps)
        btnSettings.setOnClickListener {
            // Создаем Intent для перехода в SettingsActivity
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }


    }
}