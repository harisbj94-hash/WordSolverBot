package com.example.wordsolverbot

import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast

class FloatingOverlayService : Service() {

    private lateinit var windowManager: WindowManager
    private lateinit var overlayView: View

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()

        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(20, 20, 20, 20)
            setBackgroundColor(0xCC000000.toInt())
        }

        val inputEditText = EditText(this).apply {
            hint = "Letters (e.g. PUOC)"
            setHintTextColor(0x88FFFFFF.toInt())
            setTextColor(0xFFFFFFFF.toInt())
            setSingleLine()
        }

        val btnSolve = Button(this).apply {
            text = "⚡ AUTO SOLVE"
            setBackgroundColor(0xFF4CAF50.toInt())
            setTextColor(0xFFFFFFFF.toInt())
        }

        val btnClose = Button(this).apply {
            text = "✕ Close"
            setBackgroundColor(0xFFF44336.toInt())
            setTextColor(0xFFFFFFFF.toInt())
        }

        layout.addView(inputEditText)
        layout.addView(btnSolve)
        layout.addView(btnClose)

        overlayView = layout

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 100
            y = 100
        }

        // Tap karne par keyboard enable hoga
        inputEditText.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                params.flags = params.flags and WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE.inv()
                windowManager.updateViewLayout(overlayView, params)
            }
            false
        }

        btnSolve.setOnClickListener {
            val letters = inputEditText.text.toString().trim()
            if (letters.isNotEmpty()) {
                val intent = Intent(this, WordSolverService::class.java).apply {
                    action = "SOLVE_WORDS"
                    putExtra("LETTERS", letters)
                }
                startService(intent)
                Toast.makeText(this, "Solving for: $letters", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Letters enter karein!", Toast.LENGTH_SHORT).show()
            }
        }

        btnClose.setOnClickListener {
            stopSelf()
        }

        windowManager.addView(overlayView, params)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::overlayView.isInitialized) {
            windowManager.removeView(overlayView)
        }
    }
}

