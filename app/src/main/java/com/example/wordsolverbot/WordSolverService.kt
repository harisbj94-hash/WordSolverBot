package com.example.wordsolverbot

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.graphics.Rect
import android.os.Handler
import android.os.Looper
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.Toast
import kotlin.math.cos
import kotlin.math.sin

class WordSolverService : AccessibilityService() {

    private val handler = Handler(Looper.getMainLooper())
    private var isAutoModeActive = false

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // Continuous screen monitoring
    }

    override fun onInterrupt() {}

    override fun onStartCommand(intent: android.content.Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            "SOLVE_WORDS" -> {
                val letters = intent.getStringExtra("LETTERS") ?: ""
                if (letters.isNotEmpty()) {
                    startAutoSolveEngine(letters.uppercase())
                }
            }
            "AUTO_AI_MODE" -> {
                isAutoModeActive = true
                scanAndSolveScreen()
            }
            "STOP_AI" -> {
                isAutoModeActive = false
                handler.removeCallbacksAndMessages(null)
            }
        }
        return START_STICKY
    }

    // 1. AI Screen Text Scanner (Reads screen without typing)
    private fun scanAndSolveScreen() {
        val rootNode = rootInActiveWindow ?: return
        val detectedLetters = mutableListOf<String>()

        findTextNodes(rootNode, detectedLetters)

        val combinedLetters = detectedLetters.joinToString("").uppercase()
            .filter { it.isLetter() }

        if (combinedLetters.length >= 3) {
            startAutoSolveEngine(combinedLetters)
        } else {
            Toast.makeText(this, "Screen scanning...", Toast.LENGTH_SHORT).show()
        }
    }

    private fun findTextNodes(node: AccessibilityNodeInfo?, list: MutableList<String>) {
        if (node == null) return
        if (!node.text.isNullOfEmpty()) {
            list.add(node.text.toString())
        }
        for (i in 0 until node.childCount) {
            findTextNodes(node.getChild(i), list)
        }
    }

    // 2. AI Word Generator & Swipe Execution
    private fun startAutoSolveEngine(letters: String) {
        val dictionary = listOf("COUP", "PUOC", "COP", "CUP", "OUT", "PUT", "SOU", "COU")
        val validWords = generateValidWords(letters, dictionary)

        var delay = 500L
        for (word in validWords) {
            handler.postDelayed({
                executeSwipeForWord(letters, word)
            }, delay)
            delay += 1200L // 1.2s delay between words
        }

        // 3. Auto Level-Up Loop
        if (isAutoModeActive) {
            handler.postDelayed({
                scanAndSolveScreen()
            }, delay + 3000L) // Wait for level animation, then start next level
        }
    }

    private fun generateValidWords(letters: String, dict: List<String>): List<String> {
        val letterMap = letters.groupingBy { it }.eachCount()
        return dict.filter { word ->
            val wordMap = word.groupingBy { it }.eachCount()
            wordMap.all { (char, count) -> (letterMap[char] ?: 0) >= count }
        }
    }

    // 4. Circle Wheel Geometric Coordinate Calculator
    private fun executeSwipeForWord(letters: String, word: String) {
        val displayMetrics = resources.displayMetrics
        val screenWidth = displayMetrics.widthPixels
        val screenHeight = displayMetrics.heightPixels

        // Wheel center position estimation (Bottom center of screen)
        val centerX = screenWidth / 2f
        val centerY = screenHeight * 0.78f
        val radius = screenWidth * 0.25f

        val charPositions = mutableMapOf<Char, Pair<Float, Float>>()
        val totalChars = letters.length

        for (i in 0 until totalChars) {
            val angle = Math.toRadians((i * (360.0 / totalChars) - 90))
            val x = (centerX + radius * cos(angle)).toFloat()
            val y = (centerY + radius * sin(angle)).toFloat()
            charPositions[letters[i]] = Pair(x, y)
        }

        // Build Gesture Path
        val path = Path()
        var first = true

        for (char in word) {
            val pos = charPositions[char] ?: continue
            if (first) {
                path.moveTo(pos.first, pos.second)
                first = false
            } else {
                path.lineTo(pos.first, pos.second)
            }
        }

        val gestureBuilder = GestureDescription.Builder()
        gestureBuilder.addStroke(GestureDescription.StrokeDescription(path, 0, 400))
        dispatchGesture(gestureBuilder.build(), null, null)
    }
}
