package com.example.wordsolverbot

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(50, 50, 50, 50)
        }

        val btnAccessibility = Button(this).apply {
            text = "1. Enable Accessibility Service"
        }

        val btnOverlay = Button(this).apply {
            text = "2. Enable Display Over Apps"
        }

        val btnStartOverlay = Button(this).apply {
            text = "3. Launch Floating Bot UI"
        }

        btnAccessibility.setOnClickListener {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        }

        btnOverlay.setOnClickListener {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            startActivity(intent)
        }

        btnStartOverlay.setOnClickListener {
            if (!Settings.canDrawOverlays(this)) {
                Toast.makeText(this, "Pehle Overlay Permission dein!", Toast.LENGTH_SHORT).show()
            } else {
                startService(Intent(this, FloatingOverlayService::class.java))
                Toast.makeText(this, "Floating Bot Started! Game open karein.", Toast.LENGTH_SHORT).show()
            }
        }

        layout.addView(btnAccessibility)
        layout.addView(btnOverlay)
        layout.addView(btnStartOverlay)

        setContentView(layout)
    }
}

