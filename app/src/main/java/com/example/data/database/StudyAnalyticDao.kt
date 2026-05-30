package com.example.data.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface StudyAnalyticDao {
    @Query("SELECT * FROM study_analytics")
    fun getAllAnalyticsFlow(): Flow<List<StudyAnalyticEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnalytic(analytic: StudyAnalyticEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnalytics(analytics: List<StudyAnalyticEntity>)
}
