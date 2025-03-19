package ru.wizand.sendernt.presentation

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import android.view.LayoutInflater
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import ru.wizand.sendernt.R

object ViewUtils {

    fun showLongInstructionDialog(context: Context) {
        // Инфлейтим наш кастомный layout для диалога
        val dialogView = LayoutInflater.from(context).inflate(R.layout.instruction_long_dialog, null)

        // Создаем диалог с использованием MaterialAlertDialogBuilder (для Material стиля)
        val dialog = MaterialAlertDialogBuilder(context)
            .setView(dialogView)
            .setCancelable(false) // делаем диалог обязательным к прочтению
            .create()

        // Обработка клика по кнопке «Понятно»
        dialogView.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnDialogOk)
            .setOnClickListener { dialog.dismiss() }

        dialog.show()
    }

    fun showShortInstructionDialog(context: Context) {
        // Инфлейтим наш кастомный layout для диалога
        val dialogView = LayoutInflater.from(context).inflate(R.layout.instruction_dialog, null)

        // Создаем диалог с использованием MaterialAlertDialogBuilder (для Material стиля)
        val dialog = MaterialAlertDialogBuilder(context)
            .setView(dialogView)
            .setCancelable(false) // делаем диалог обязательным к прочтению
            .create()

        // Обработка клика по кнопке «Понятно»
        dialogView.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnDialogOk)
            .setOnClickListener { dialog.dismiss() }

        dialog.show()
    }


}