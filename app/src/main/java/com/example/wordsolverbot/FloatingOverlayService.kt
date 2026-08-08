package com.example.wordsolverbot

import android.app.Service
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.IBinder
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast

class FloatingOverlayService : Service() {

    private lateinit var windowManager: WindowManager
    private lateinit var overlayView: View
    private lateinit var wordFinder: WordFinder

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        wordFinder = WordFinder(this)
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.parseColor("#CC000000"))
            setPadding(20, 20, 20, 20)
        }

        val inputLetters = EditText(this).apply {
            hint = "Letters (e.g. CATS)"
            setHintTextColor(Color.GRAY)
            setTextColor(Color.WHITE)
        }

        val btnSolve = Button(this).apply {
            text = "⚡ AUTO SOLVE"
            setBackgroundColor(Color.parseColor("#4CAF50"))
            setTextColor(Color.WHITE)
        }

        layout.addView(inputLetters)
        layout.addView(btnSolve)

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 100
            y = 200
        }

        btnSolve.setOnClickListener {
            val letters = inputLetters.text.toString().trim()
            if (letters.isEmpty()) {
                Toast.makeText(this, "Pehle letters enter karein!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val service = WordSolverService.instance
            if (service == null) {
                Toast.makeText(this, "Accessibility Service OFF hai!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val validWords = wordFinder.solve(letters)
            if (validWords.isEmpty()) {
                Toast.makeText(this, "Koi word nahi mila!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            Thread {
                for (word in validWords) {
                    service.executeWordSwipe(word, letters)
                    Thread.sleep(600)
                }
            }.start()
        }

        overlayView = layout
        windowManager.addView(overlayView, params)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::overlayView.isInitialized) {
            windowManager.removeView(overlayView)
        }
    }
}

