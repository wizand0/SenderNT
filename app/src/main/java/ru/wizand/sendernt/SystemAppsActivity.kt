package ru.wizand.sendernt

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ru.wizand.sendernt.R
import ru.wizand.sendernt.domain.AppInfo
import ru.wizand.sendernt.data.utils.AllowedAppsPreferences
import ru.wizand.sendernt.data.utils.AppUtils
import ru.wizand.sendernt.presentation.AppsAdapter

class SystemAppsActivity : AppCompatActivity() {

    private val TAG = "NotificationLogger"

    private lateinit var recyclerView: RecyclerView
    private lateinit var appsAdapter: AppsAdapter
    private var appsList: List<AppInfo> = emptyList()
    val appUtils = AppUtils()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
//        setContentView(R.layout.activity_system_apps)
        setContentView(R.layout.activity_settings)

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Получаем список установленных приложений
        appsList = appUtils.getAllApps(this)
        appsAdapter = AppsAdapter(appsList) { app, isChecked ->
            // При изменении ToggleButton обновляем сохранённый список в SharedPreferences.

            Toast.makeText(
                this,
                "${getString(R.string.text1)} ${app.appName} ${getString(R.string.text3)}",
                Toast.LENGTH_SHORT
            ).show()


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