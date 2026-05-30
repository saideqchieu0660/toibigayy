package com.example.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "study_analytics")
data class StudyAnalyticEntity(
    @PrimaryKey val id: String, // e.g. "Mon_math", "Tue_physics" etc.
    val dayOfWeek: String, // Mon, Tue, Wed, Thu, Fri, Sat, Sun
    val subject: String,
    val cardsLearned: Int
)
