package ru.wizand.sendernt.data.utils

import android.content.Context

// Утилита для сохранения/выбора разрешённых приложений
object AllowedAppsPreferences {
    private const val PREFS_NAME = "allowed_apps_prefs"
    private const val KEY_ALLOWED_PACKAGES = "allowed_packages"

    fun saveAllowedPackages(context: Context, allowedPackages: Set<String>) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putStringSet(KEY_ALLOWED_PACKAGES, allowedPackages).apply()
    }

    fun getAllowedPackages(context: Context): Set<String> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getStringSet(KEY_ALLOWED_PACKAGES, emptySet()) ?: emptySet()
    }
}
