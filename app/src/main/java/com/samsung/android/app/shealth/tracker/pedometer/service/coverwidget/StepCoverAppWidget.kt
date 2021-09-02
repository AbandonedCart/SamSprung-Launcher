package com.samsung.android.app.shealth.tracker.pedometer.service.coverwidget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.sec.android.app.shealth.R


class StepCoverAppWidget: AppWidgetProvider() {

    private val shealth = "com.sec.android.app.shealth"

    private val onClickTag = "OnClickTag"

    protected fun getPendingSelfIntent(context: Context?, action: String?): PendingIntent? {
        val intent = Intent(context, StepCoverAppWidget()::class.java)
        intent.action = action
        return PendingIntent.getBroadcast(context, 0, intent, 0)
    }

    override fun onReceive(context: Context?, intent: Intent) {
        if (onClickTag.equals(intent.action)) {
            val intent = Intent()
            intent.component = ComponentName(shealth, "$shealth.AppLauncherService")
            context?.startService(intent)
        }
        else{
            super.onReceive(context, intent)
        }
    }

    override fun onUpdate(
        context: Context?,
        appWidgetManager: AppWidgetManager?,
        appWidgetIds: IntArray?
    ) {
        appWidgetIds?.forEach { appWidgetId ->
            val views = RemoteViews(
                context?.packageName,
                R.layout.cover_widget_layout
            ).apply{
                this.setOnClickPendingIntent(R.id.launch, getPendingSelfIntent(context, onClickTag))
            }

            appWidgetManager?.updateAppWidget(appWidgetId, views)

        }
    }
}