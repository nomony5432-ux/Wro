package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface WROScoreDao {
    @Query("SELECT * FROM scores ORDER BY timestamp DESC")
    fun getAllScores(): Flow<List<WROScore>>

    @Query("SELECT * FROM scores WHERE id = :id LIMIT 1")
    suspend fun getScoreById(id: Long): WROScore?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScore(score: WROScore): Long

    @Query("DELETE FROM scores WHERE id = :id")
    suspend fun deleteScoreById(id: Long)
}
