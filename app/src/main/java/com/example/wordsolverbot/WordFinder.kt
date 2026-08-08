package com.example.wordsolverbot

import android.content.Context

class WordFinder(context: Context) {

    private val wordSet = mutableSetOf<String>()

    init {
        loadDefaultWords()
    }

    private fun loadDefaultWords() {
        val basicWords = listOf(
            "CAT", "ACT", "DOG", "GOD", "BAT", "TAB", "RAT", "ART", "TAR",
            "WORD", "GAMES", "COLLECT", "SOLVE", "LETTER", "WHEEL", "STAR",
            "ARTS", "RATS", "TARS", "CATS", "ACTS", "DOGS", "GODS", "BATS",
            "PLAY", "LOKI", "GAME", "LOVE", "LION", "KING", "BIRD", "FISH"
        )
        wordSet.addAll(basicWords)
    }

    fun solve(letters: String): List<String> {
        val upperLetters = letters.uppercase().trim()
        val letterCounts = upperLetters.groupingBy { it }.eachCount()
        
        return wordSet.filter { word ->
            if (word.length > upperLetters.length || word.length < 3) return@filter false
            val wordCounts = word.groupingBy { it }.eachCount()
            wordCounts.all { (char, count) -> (letterCounts[char] ?: 0) >= count }
        }.sortedByDescending { it.length }
    }
}

