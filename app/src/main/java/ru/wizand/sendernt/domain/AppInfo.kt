package ru.wizand.sendernt.domain

import android.graphics.drawable.Drawable

// Модель для представления приложения
data class AppInfo(
    val packageName: String,
    val appName: String,
    val icon: Drawable,
    var isAllowed: Boolean
)
