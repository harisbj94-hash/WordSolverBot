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
    private var initialX = 0
    private var initialY = 0
    private var initialTouchX = 0f
    private var initialTouchY = 0f

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()

        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(20, 20, 20, 20)
            setBackgroundColor(0xDD000000.toInt())
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

        val btnAiMode = Button(this).apply {
            text = "🤖 START AI AUTO"
            setBackgroundColor(0xFF2196F3.toInt())
            setTextColor(0xFFFFFFFF.toInt())
        }

        val btnClose = Button(this).apply {
            text = "✕ Close"
            setBackgroundColor(0xFFF44336.toInt())
            setTextColor(0xFFFFFFFF.toInt())
        }

        layout.addView(inputEditText)
        layout.addView(btnSolve)
        layout.addView(btnAiMode)
        layout.addView(btnClose)

        overlayView = layout

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 100
            y = 200
        }

        // 1. Drag & Move Box Anywhere
        layout.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = params.x
                    initialY = params.y
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    params.x = initialX + (event.rawX - initialTouchX).toInt()
                    params.y = initialY + (event.rawY - initialTouchY).toInt()
                    windowManager.updateViewLayout(overlayView, params)
                    true
                }
                else -> false
            }
        }

        // 2. Focus for Typing Keyboard
        inputEditText.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                params.flags = params.flags and WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE.inv()
                windowManager.updateViewLayout(overlayView, params)
                inputEditText.requestFocus()
            }
            false
        }

        // 3. Manual Solve Button
        btnSolve.setOnClickListener {
            val letters = inputEditText.text.toString().trim()
            if (letters.isNotEmpty()) {
                params.flags = params.flags or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                windowManager.updateViewLayout(overlayView, params)

                val intent = Intent(this, WordSolverService::class.java).apply {
                    action = "SOLVE_WORDS"
                    putExtra("LETTERS", letters)
                }
                startService(intent)
                Toast.makeText(this, "Solving: $letters", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Letters enter karein!", Toast.LENGTH_SHORT).show()
            }
        }

        // 4. AI Auto Mode Button
        btnAiMode.setOnClickListener {
            params.flags = params.flags or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
            windowManager.updateViewLayout(overlayView, params)

            val intent = Intent(this, WordSolverService::class.java).apply {
                action = "AUTO_AI_MODE"
            }
            startService(intent)
            Toast.makeText(this, "AI Auto Mode Activated!", Toast.LENGTH_SHORT).show()
        }

        // 5. Close Button
        btnClose.setOnClickListener {
            removeOverlayAndStop()
        }

        windowManager.addView(overlayView, params)
    }

    private fun removeOverlayAndStop() {
        try {
            if (::overlayView.isInitialized && overlayView.windowToken != null) {
                windowManager.removeView(overlayView)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        stopSelf()
    }

    override fun onDestroy() {
        super.onDestroy()
        removeOverlayAndStop()
    }
}

