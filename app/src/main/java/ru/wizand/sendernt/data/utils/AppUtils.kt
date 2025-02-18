package ru.wizand.sendernt.data.utils

import android.content.Context
import android.content.pm.PackageManager
import ru.wizand.sendernt.domain.AppInfo


// Здесь функция getInstalledApps()
class AppUtils {

    fun getInstalledApps(context: Context): List<AppInfo> {

        val packageManager = context.packageManager

        val installedApplications = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)

        val allowedPackages = AllowedAppsPreferences.getAllowedPackages(context)
        // Получаем все приложения, для которых можно запустить MainActivity (то есть имеющие launcher intent)
//        val intent = Intent(Intent.ACTION_MAIN).apply { addCategory(Intent.CATEGORY_LAUNCHER) }
//
//        val resolveInfos = packageManager.queryIntentActivities(intent, 0)
//        return resolveInfos.mapNotNull { resolveInfo ->
//            val activityInfo = resolveInfo.activityInfo
//            try {
//                AppInfo(
//                    packageName = activityInfo.packageName,
//                    appName = activityInfo.loadLabel(packageManager).toString(),
//                    icon = activityInfo.loadIcon(packageManager),
//                    isAllowed = allowedPackages.contains(activityInfo.packageName)
//                )
//            } catch (e: Exception) {
//                null
//            }
//        }.sortedBy { it.appName }

        // Пример получения всех установленных приложений
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

}