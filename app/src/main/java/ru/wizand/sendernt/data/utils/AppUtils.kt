package ru.wizand.sendernt.data.utils

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import ru.wizand.sendernt.domain.AppInfo
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Здесь функция getInstalledApps()
object AppUtils {

    fun getInstalledApps(context: Context): List<AppInfo> {
        val packageManager = context.packageManager
        val installedApplications =
            packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
        val allowedPackages = AllowedAppsPreferences.getAllowedPackages(context)

        // Фильтруем список: исключаем системные приложения, если они не обновлены, и приложения без лаунчера
        val filteredApps = installedApplications.filter { appInfo ->
            // Если приложение вообще можно запустить (есть launcher-интент)
            val launchIntent = packageManager.getLaunchIntentForPackage(appInfo.packageName)
            val hasLauncher = launchIntent != null

            // Определяем, является ли приложение чисто системным
            val isSystemApp = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0 &&
                    (appInfo.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) == 0

            hasLauncher && !isSystemApp
        }

        // Пример получения всех установленных приложений

        // Или вообще все-все (для этого нужно раскомментировать строку а наже закомментировать)
//        return installedApplications.mapNotNull { appInfo ->
        // Или отфильтрованные "не системные"
        return filteredApps.mapNotNull { appInfo ->
            try {
                AppInfo(
                    packageName = appInfo.packageName,
                    appName = packageManager.getApplicationLabel(appInfo).toString(),
                    icon = packageManager.getApplicationIcon(appInfo),
                    isAllowed = allowedPackages.contains(appInfo.packageName)
                )
            } catch (e: Exception) {
                null
            }
        }.sortedBy { it.appName }
    }

    fun getAllApps(context: Context): List<AppInfo> {
        val packageManager = context.packageManager
        val installedApplications =
            packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
        val allowedPackages = AllowedAppsPreferences.getAllowedPackages(context)

        // Фильтруем список: исключаем системные приложения, если они не обновлены, и приложения без лаунчера
        val filteredApps = installedApplications.filter { appInfo ->
            // Если приложение вообще можно запустить (есть launcher-интент)
            val launchIntent = packageManager.getLaunchIntentForPackage(appInfo.packageName)
            val hasLauncher = launchIntent != null

            // Определяем, является ли приложение чисто системным
            val isSystemApp = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0 &&
                    (appInfo.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) == 0

            hasLauncher && !isSystemApp
        }

        return installedApplications.mapNotNull { appInfo ->
            try {
                AppInfo(
                    packageName = appInfo.packageName,
                    appName = packageManager.getApplicationLabel(appInfo).toString(),
                    icon = packageManager.getApplicationIcon(appInfo),
                    isAllowed = allowedPackages.contains(appInfo.packageName)
                )
            } catch (e: Exception) {
                null
            }
        }.sortedBy { it.appName }
    }

    public fun convertTimestampToReadableFormat(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault())
        val date = Date(timestamp)
        return sdf.format(date)
    }
}