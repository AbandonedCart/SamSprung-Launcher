package com.sec.android.app.shealth

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.webkit.WebView
import android.widget.Button
import android.widget.Toast
import org.mozilla.geckoview.GeckoRuntime
import org.mozilla.geckoview.GeckoSession
import org.mozilla.geckoview.GeckoSessionSettings
import org.mozilla.geckoview.GeckoView

class BrowserActivity : AppCompatActivity() {
    private val defaultUrl = "https://forum.xda-developers.com/f/samsung-galaxy-z-flip-3.12351/"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_browser)

        supportActionBar?.hide()

        val view: GeckoView = findViewById(R.id.geckoview)

        val settings = GeckoSessionSettings.Builder()
            .usePrivateMode(true)
            .useTrackingProtection(true)
            .suspendMediaWhenInactive(true)
            .allowJavascript(true)
            .userAgentMode(GeckoSessionSettings.USER_AGENT_MODE_MOBILE)
            .displayMode(GeckoSessionSettings.DISPLAY_MODE_STANDALONE)
            .viewportMode(GeckoSessionSettings.VIEWPORT_MODE_DESKTOP)

        val geckoSession = GeckoSession(settings.build())

        if (geckoRuntime == null) {
            geckoRuntime = GeckoRuntime.create(this)
        }

        geckoSession.open(geckoRuntime!!)

        view.setSession(geckoSession)

        val sharedPref = getSharedPreferences("com.carudibu.subuibrowser.PREFS", Context.MODE_PRIVATE)

        geckoSession.loadUri(sharedPref.getString("starting_url", defaultUrl) ?: defaultUrl)

        findViewById<Button>(R.id.back).setOnClickListener {
            geckoSession.goBack()
        }
        findViewById<Button>(R.id.forward).setOnClickListener {
            geckoSession.goForward()
        }
        findViewById<Button>(R.id.quit).setOnClickListener {
            finish()
        }

    }

    companion object {
        var geckoRuntime: GeckoRuntime? = null
    }
}