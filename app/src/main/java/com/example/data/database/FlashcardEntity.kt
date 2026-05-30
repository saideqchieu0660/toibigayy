package com.example.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "flashcards")
data class FlashcardEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val front: String,
    val back: String,
    val subject: String, // math, physics, chemistry, biology, english, history, other
    val mastered: Boolean = false,
    val smRepetitions: Int = 0,
    val smInterval: Int = 1,
    val smDifficulty: Float = 2.5f,
    val lastStudied: Long = 0,
    val aiDetails: String? = null,
    val totalAttempts: Int = 0,
    val failedAttempts: Int = 0
)
