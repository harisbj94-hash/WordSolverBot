package com.example.wordsolverbot

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.view.accessibility.AccessibilityEvent
import kotlin.math.cos
import kotlin.math.sin

class WordSolverService : AccessibilityService() {

    companion object {
        var instance: WordSolverService? = null
    }

    data class Point(val x: Float, val y: Float)

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}
    override fun onInterrupt() {}

    fun executeWordSwipe(word: String, letterOrder: String) {
        val metrics = resources.displayMetrics
        val screenWidth = metrics.widthPixels.toFloat()
        val screenHeight = metrics.heightPixels.toFloat()

        // Word Collect Wheel Center (Bottom Half)
        val centerX = screenWidth * 0.5f
        val centerY = screenHeight * 0.74f
        val radius = screenWidth * 0.23f

        val totalLetters = letterOrder.length
        val letterMap = mutableMapOf<Char, MutableList<Point>>()

        for (i in 0 until totalLetters) {
            val angle = -Math.PI / 2 + (2 * Math.PI * i / totalLetters)
            val px = (centerX + radius * cos(angle)).toFloat()
            val py = (centerY + radius * sin(angle)).toFloat()
            
            val char = letterOrder[i].uppercaseChar()
            if (!letterMap.containsKey(char)) {
                letterMap[char] = mutableListOf()
            }
            letterMap[char]?.add(Point(px, py))
        }

        val path = Path()
        var isFirst = true
        val usedPoints = mutableSetOf<Point>()

        for (char in word) {
            val availablePoints = letterMap[char] ?: continue
            val point = availablePoints.firstOrNull { it !in usedPoints } ?: continue
            
            usedPoints.add(point)
            if (isFirst) {
                path.moveTo(point.x, point.y)
                isFirst = false
            } else {
                path.lineTo(point.x, point.y)
            }
        }

        val stroke = GestureDescription.StrokeDescription(path, 0, (word.length * 150).toLong())
        val gesture = GestureDescription.Builder().addStroke(stroke).build()
        dispatchGesture(gesture, null, null)
    }

    override fun onDestroy() {
        super.onDestroy()
        instance = null
    }
}

