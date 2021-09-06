package com.sec.android.app.shealth

import android.app.ActivityOptions
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import androidx.core.widget.doOnTextChanged

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val sharedPref = getSharedPreferences("com.carudibu.subuibrowser.PREFS", Context.MODE_PRIVATE)

        findViewById<EditText>(R.id.startingUrl).setText(sharedPref.getString("starting_url", "https://reddit.com") ?: "https://reddit.com")

        findViewById<EditText>(R.id.startingUrl).doOnTextChanged { text, start, before, count ->
            with (sharedPref.edit()) {
                putString("starting_url", text.toString())
                apply()
            }
        }

        findViewById<Button>(R.id.openBrowser).setOnClickListener {
            val intent = Intent()
            intent.component = ComponentName("com.sec.android.app.shealth","com.sec.android.app.shealth.BrowserActivity")
            val options = ActivityOptions.makeBasic().setLaunchDisplayId(0)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

            startActivity(intent, options.toBundle())
        }
    }
}