package com.sec.android.app.shealth

import android.app.*
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.hardware.display.DisplayManager
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat


class DisplayListenerService() : Service() {

    private val coverLock = "cover_lock"
    private var mDisplayListener: DisplayManager.DisplayListener? = null

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        @Suppress("DEPRECATION")
        val mKeyguardLock = (applicationContext.getSystemService(
            Context.KEYGUARD_SERVICE) as KeyguardManager)
            .newKeyguardLock(coverLock)
        val manager = applicationContext.getSystemService(
            Context.DISPLAY_SERVICE) as DisplayManager

        if (intent.action.equals("samsprung.launcher.STOP")) {
            try {
                stopForeground(true)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            if (mDisplayListener != null) {
                manager.unregisterDisplayListener(mDisplayListener)
                mDisplayListener = null
                @Suppress("DEPRECATION") mKeyguardLock.reenableKeyguard()
            }
            try {
                stopSelf()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return START_NOT_STICKY
        }

        val launchPackage = intent.getStringExtra("launchPackage")
        val launchActivity = intent.getStringExtra("launchActivity")

        showForegroundNotification(startId)

        mDisplayListener = object : DisplayManager.DisplayListener {
            override fun onDisplayAdded(display: Int) {}
            override fun onDisplayChanged(display: Int) {
                if (display == 0) {
                    stopForeground(true)
                    manager.unregisterDisplayListener(this)
                    mDisplayListener = null
                    @Suppress("DEPRECATION") mKeyguardLock.reenableKeyguard()
                    stopSelf()
                } else {
                    @Suppress("DEPRECATION") mKeyguardLock.disableKeyguard()
                }
                val displayIntent = Intent(Intent.ACTION_MAIN)
                displayIntent.addCategory(Intent.CATEGORY_LAUNCHER)
                displayIntent.component = ComponentName(launchPackage!!, launchActivity!!)
                val launchDisplay = ActivityOptions.makeBasic().setLaunchDisplayId(display)
                displayIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(displayIntent, launchDisplay.toBundle())
            }

            override fun onDisplayRemoved(display: Int) {}
        }
        manager.registerDisplayListener(mDisplayListener, Handler(Looper.getMainLooper()))
        return START_STICKY
    }

    private var iconNotification: Bitmap? = null
    private var mNotificationManager: NotificationManager? = null
    private val mNotificationId = 123

    private fun showForegroundNotification(startId: Int) {
        val stopIntent = Intent(this, DisplayListenerService::class.java)
        stopIntent.action = "samsprung.launcher.STOP"
        val pendingIntent = PendingIntent.getForegroundService(
            this, startId, stopIntent, 0)
        iconNotification = BitmapFactory.decodeResource(resources, R.mipmap.s_health_icon)
        if (mNotificationManager == null) {
            mNotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        }
        mNotificationManager!!.createNotificationChannelGroup(
            NotificationChannelGroup("services_group", "Services")
        )
        val notificationChannel = NotificationChannel("service_channel",
            "Service Notification", NotificationManager.IMPORTANCE_LOW)
        notificationChannel.enableLights(false)
        notificationChannel.lockscreenVisibility = Notification.VISIBILITY_SECRET
        mNotificationManager!!.createNotificationChannel(notificationChannel)
        val builder = NotificationCompat.Builder(this, "service_channel")

        val notificationText = StringBuilder(resources.getString(R.string.app_name))
            .append(R.string.display_service).toString()
        builder.setContentTitle(notificationText).setTicker(notificationText)
            .setContentText(getString(R.string.click_stop_service))
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setWhen(0).setOnlyAlertOnce(true)
            .setContentIntent(pendingIntent).setOngoing(true)
        if (iconNotification != null) {
            builder.setLargeIcon(Bitmap.createScaledBitmap(
                iconNotification!!, 128, 128, false))
        }
        builder.color = ContextCompat.getColor(this, R.color.purple_200)
        startForeground(mNotificationId, builder.build())
    }

    override fun onDestroy() {
        try {
            stopForeground(true)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        if (mDisplayListener != null) {
            @Suppress("DEPRECATION")
            val mKeyguardLock = (applicationContext.getSystemService(
                Context.KEYGUARD_SERVICE) as KeyguardManager)
                .newKeyguardLock(coverLock)
            val manager = applicationContext.getSystemService(
                Context.DISPLAY_SERVICE) as DisplayManager
            manager.unregisterDisplayListener(mDisplayListener)
            mDisplayListener = null
            @Suppress("DEPRECATION") mKeyguardLock.reenableKeyguard()
        }
        try {
            stopSelf()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        super.onDestroy()
    }
}