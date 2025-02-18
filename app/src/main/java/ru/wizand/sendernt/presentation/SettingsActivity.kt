package ru.wizand.sendernt.presentation

import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ru.wizand.sendernt.R
import ru.wizand.sendernt.domain.AppInfo
import ru.wizand.sendernt.data.utils.AllowedAppsPreferences
import ru.wizand.sendernt.data.utils.AppUtils

class SettingsActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var appsAdapter: AppsAdapter
    private var appsList: List<AppInfo> = emptyList()
    val appUtils = AppUtils()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_settings) // оформите свою разметку

        val btnBack = findViewById<Button>(R.id.btnBack)
        btnBack.setOnClickListener {
            // Завершаем активность, возвращаясь к предыдущей (MainActivity)
            finish()
        }

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Получаем список установленных приложений
        appsList = appUtils.getInstalledApps(this)
        appsAdapter = AppsAdapter(appsList) { app, isChecked ->
            // При изменении ToggleButton обновляем сохранённый список в SharedPreferences.
            updateAllowedPackages(app.packageName, isChecked)
        }
        recyclerView.adapter = appsAdapter

    }

    private fun updateAllowedPackages(packageName: String, isAllowed: Boolean) {
        val allowedPackages = AllowedAppsPreferences.getAllowedPackages(this).toMutableSet()
        if (isAllowed) {
            allowedPackages.add(packageName)
        } else {
            allowedPackages.remove(packageName)
        }
        AllowedAppsPreferences.saveAllowedPackages(this, allowedPackages)
    }
}