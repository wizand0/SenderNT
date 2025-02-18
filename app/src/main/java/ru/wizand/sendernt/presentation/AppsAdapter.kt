package ru.wizand.sendernt.presentation

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.ToggleButton
import androidx.recyclerview.widget.RecyclerView
import ru.wizand.sendernt.R
import ru.wizand.sendernt.domain.AppInfo

class AppsAdapter(
    private val apps: List<AppInfo>,
    private val onToggleChanged: (AppInfo, Boolean) -> Unit
) : RecyclerView.Adapter<AppsAdapter.AppViewHolder>() {

    class AppViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val iconImageView: ImageView = view.findViewById(R.id.appIcon)
        val appNameTextView: TextView = view.findViewById(R.id.appName)
        val toggleButton: ToggleButton = view.findViewById(R.id.toggleButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_app, parent, false)
        return AppViewHolder(view)
    }
    override fun onBindViewHolder(holder: AppViewHolder, position: Int) {
        val app = apps[position]
        holder.iconImageView.setImageDrawable(app.icon)
        holder.appNameTextView.text = app.appName
        holder.toggleButton.isChecked = app.isAllowed

        holder.toggleButton.setOnCheckedChangeListener { _, isChecked ->
            // Обновляем состояние в модели
            app.isAllowed = isChecked
            onToggleChanged(app, isChecked)
        }
    }

    override fun getItemCount(): Int = apps.size
}