package com.example.data.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface StudyGroupDao {
    // Group Queries
    @Query("SELECT * FROM study_groups ORDER BY createdAt DESC")
    fun getAllGroupsFlow(): Flow<List<StudyGroupEntity>>

    @Query("SELECT * FROM study_groups WHERE code = :code LIMIT 1")
    suspend fun getGroupByCode(code: String): StudyGroupEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGroup(group: StudyGroupEntity): Long

    // Group Member Queries (Leaderboard)
    @Query("SELECT * FROM group_members WHERE groupCode = :groupCode ORDER BY points DESC")
    fun getMembersByGroupFlow(groupCode: String): Flow<List<GroupMemberEntity>>

    @Query("SELECT * FROM group_members WHERE groupCode = :groupCode ORDER BY points DESC")
    suspend fun getMembersByGroup(groupCode: String): List<GroupMemberEntity>

    @Query("SELECT * FROM group_members WHERE groupCode = :groupCode AND userEmail = :userEmail LIMIT 1")
    suspend fun getMemberByGroupAndEmail(groupCode: String, userEmail: String): GroupMemberEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMember(member: GroupMemberEntity): Long

    @Query("UPDATE group_members SET points = points + :addPoints WHERE groupCode = :groupCode AND userEmail = :userEmail")
    suspend fun addMemberPoints(groupCode: String, userEmail: String, addPoints: Int)

    // Shared Flashcard Queries
    @Query("SELECT * FROM shared_flashcards WHERE groupCode = :groupCode ORDER BY timestamp DESC")
    fun getSharedFlashcardsFlow(groupCode: String): Flow<List<SharedFlashcardEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSharedFlashcard(sharedCard: SharedFlashcardEntity): Long
}
