package ru.wizand.sendernt.presentation

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.SearchView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.microsoft.clarity.Clarity
import com.microsoft.clarity.ClarityConfig
import ru.wizand.sendernt.R
import ru.wizand.sendernt.domain.AppInfo
import ru.wizand.sendernt.data.utils.AllowedAppsPreferences
import ru.wizand.sendernt.data.utils.AppUtils

class SystemAppsActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var appsAdapter: AppsAdapter
    private var appsList: List<AppInfo> = emptyList()
    private val appUtils = AppUtils

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
//        setContentView(R.layout.activity_system_apps)
        setContentView(R.layout.activity_settings)

        // implementation Microsoft Clarity
        val config = ClarityConfig("qn253qo57u")
        Clarity.initialize(applicationContext, config)

        // Найдите Toolbar и задайте его в качестве ActionBar
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

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

    // Метод для отображения меню на тулбаре/аппбаре
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_settings, menu)

        val searchItem = menu?.findItem(R.id.action_search)
        val searchView = searchItem?.actionView as SearchView

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterApps(newText)
                return true
            }
        })

        return true
    }

    private fun filterApps(query: String?) {
        val filteredList = if (query.isNullOrEmpty()) {
            appsList // Если строка поиска пустая, показываем все приложения
        } else {
            appsList.filter { app ->
                app.appName.contains(query, ignoreCase = true) // Фильтруем по имени приложения
            }
        }
        appsAdapter.updateApps(filteredList) // Обновляем адаптер с анимацией
    }

    // Обработка нажатий элементов меню
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
//            R.id.action_system_apps -> {
//                // Переход к SystemAppsActivity
//                val intent = Intent(this, SystemAppsActivity::class.java)
//                startActivity(intent)
//                true
//            }
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