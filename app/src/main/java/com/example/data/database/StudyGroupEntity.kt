package com.example.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "study_groups")
data class StudyGroupEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val description: String,
    val code: String, // Unique join code
    val ownerEmail: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "group_members")
data class GroupMemberEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val groupCode: String,
    val userEmail: String,
    val userName: String,
    val points: Int = 0
)

@Entity(tableName = "shared_flashcards")
data class SharedFlashcardEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val groupCode: String,
    val front: String,
    val back: String,
    val subject: String,
    val sharedBy: String,
    val timestamp: Long = System.currentTimeMillis()
)
