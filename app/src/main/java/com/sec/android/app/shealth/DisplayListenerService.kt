package com.sec.android.app.shealth

/* ====================================================================
 * Copyright (c) 2012-2022 AbandonedCart.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. All advertising materials mentioning features or use of this
 *    software and redistributions of any form whatsoever
 *    must display the following acknowledgment:
 *    "This product includes software developed by AbandonedCart" unless
 *    otherwise displayed by tagged, public repository entries.
 *
 * 4. The names "8-Bit Dream", "TwistedUmbrella" and "AbandonedCart"
 *    must not be used in any form to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact enderinexiledc@gmail.com
 *
 * 5. Products derived from this software may not be called "8-Bit Dream",
 *    "TwistedUmbrella" or "AbandonedCart" nor may these labels appear
 *    in their names without prior written permission of AbandonedCart.
 *
 * THIS SOFTWARE IS PROVIDED BY AbandonedCart ``AS IS'' AND ANY
 * EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE OpenSSL PROJECT OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 * ====================================================================
 *
 * The license and distribution terms for any publicly available version or
 * derivative of this code cannot be changed.  i.e. this code cannot simply be
 * copied and put under another distribution license
 * [including the GNU Public License.] Content not subject to these terms is
 * subject to to the terms and conditions of the Apache License, Version 2.0.
 */

import android.annotation.SuppressLint
import android.app.*
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.hardware.display.DisplayManager
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.provider.Settings
import android.view.WindowManager
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat


class DisplayListenerService : Service() {

    private val coverLock = "cover_lock"
    private var mDisplayListener: DisplayManager.DisplayListener? = null

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        val launchPackage = intent?.getStringExtra("launchPackage")
        val launchActivity = intent?.getStringExtra("launchActivity")

        @Suppress("DEPRECATION")
        val mKeyguardLock = (getSystemService(Context.KEYGUARD_SERVICE)
                as KeyguardManager).newKeyguardLock(coverLock)
        val displayManager = getSystemService(Context.DISPLAY_SERVICE) as DisplayManager

        if (null == launchPackage)
            return dismissDisplayService(displayManager, mKeyguardLock)

        showForegroundNotification(startId)

        mDisplayListener = object : DisplayManager.DisplayListener {
            override fun onDisplayAdded(display: Int) {}
            override fun onDisplayChanged(display: Int) {
                if (display == 0) {
                    dismissDisplayListener(displayManager, mKeyguardLock)
                    resetLaunchedApplication(launchPackage, launchActivity)
                } else {
                    if (SamSprung.isKeyguardLocked)
                        @Suppress("DEPRECATION") mKeyguardLock.disableKeyguard()
                }
            }

            override fun onDisplayRemoved(display: Int) {}
        }
        displayManager.registerDisplayListener(
            mDisplayListener, Handler(Looper.getMainLooper())
        )
        return START_STICKY
    }

    private fun resetLaunchedApplication(launchPackage: String, launchActivity: String?) {
        if (null != launchActivity) {
            val options = ActivityOptions.makeBasic().setLaunchDisplayId(0)

            val coverIntent = Intent(Intent.ACTION_MAIN)
            coverIntent.addCategory(Intent.CATEGORY_LAUNCHER)
            coverIntent.component = ComponentName(launchPackage, launchActivity)
            coverIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TASK or
                    Intent.FLAG_ACTIVITY_FORWARD_RESULT or
                    Intent.FLAG_ACTIVITY_NO_ANIMATION
            startActivity(coverIntent, options.toBundle())

            val homeLauncher = Intent(Intent.ACTION_MAIN)
            homeLauncher.addCategory(Intent.CATEGORY_HOME)
            homeLauncher.flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_FORWARD_RESULT or
                    Intent.FLAG_ACTIVITY_NO_ANIMATION
            startActivity(homeLauncher, options.toBundle())
        }
    }

    private fun dismissDisplayListener(
        displayManager: DisplayManager,
        @Suppress("DEPRECATION")
        mKeyguardLock: KeyguardManager.KeyguardLock
    ) {
        if (null != mDisplayListener) {
            displayManager.unregisterDisplayListener(mDisplayListener)
        }
        if (Settings.System.canWrite(applicationContext)) {
            try {
                Settings.System.putInt(applicationContext.contentResolver,
                    Settings.System.ACCELEROMETER_ROTATION,
                    SamSprung.prefs.getBoolean(SamSprung.autoRotate, true).int
                )
            } catch (ignored: Settings.SettingNotFoundException) { }
        }
        if (SamSprung.isKeyguardLocked)
            @Suppress("DEPRECATION") mKeyguardLock.reenableKeyguard()
        try {
            stopForeground(true)
            stopSelf()
        } catch (ignored: Exception) { }
    }

    private fun dismissDisplayService(
        displayManager: DisplayManager,
        @Suppress("DEPRECATION")
        mKeyguardLock: KeyguardManager.KeyguardLock
    ): Int {

        dismissDisplayListener(displayManager, mKeyguardLock)
        return START_NOT_STICKY
    }

    @SuppressLint("LaunchActivityFromNotification")
    private fun showForegroundNotification(startId: Int) {
        var mNotificationManager: NotificationManager? = null
        val pendingIntent = PendingIntent.getService(this, 0,
            Intent(this, DisplayListenerService::class.java),
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                PendingIntent.FLAG_IMMUTABLE else 0)
        val iconNotification = BitmapFactory.decodeResource(resources, R.mipmap.s_health_icon)
        if (null == mNotificationManager) {
            mNotificationManager = getSystemService(
                Context.NOTIFICATION_SERVICE) as NotificationManager
        }
        mNotificationManager.createNotificationChannelGroup(
            NotificationChannelGroup("services_group", "Services")
        )
        val notificationChannel = NotificationChannel("service_channel",
            "Service Notification", NotificationManager.IMPORTANCE_LOW)
        notificationChannel.enableLights(false)
        notificationChannel.lockscreenVisibility = Notification.VISIBILITY_SECRET
        mNotificationManager.createNotificationChannel(notificationChannel)
        val builder = NotificationCompat.Builder(this, "service_channel")

        val notificationText = getString(R.string.display_service, getString(R.string.app_name))
        builder.setContentTitle(notificationText).setTicker(notificationText)
            .setContentText(getString(R.string.click_stop_service))
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setWhen(0).setOnlyAlertOnce(true)
            .setContentIntent(pendingIntent).setOngoing(true)
        if (null != iconNotification) {
            builder.setLargeIcon(Bitmap.createScaledBitmap(
                iconNotification, 128, 128, false))
        }
        builder.color = ContextCompat.getColor(this, R.color.primary_light)
        startForeground(startId, builder.build())
    }

    private val Boolean.int get() = if (this) 1 else 0
}