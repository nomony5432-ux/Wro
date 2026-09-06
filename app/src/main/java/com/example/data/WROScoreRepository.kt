package com.example.data

import kotlinx.coroutines.flow.Flow

class WROScoreRepository(private val scoreDao: WROScoreDao) {
    val allScores: Flow<List<WROScore>> = scoreDao.getAllScores()

    suspend fun getScoreById(id: Long): WROScore? {
        return scoreDao.getScoreById(id)
    }

    suspend fun insertScore(score: WROScore): Long {
        return scoreDao.insertScore(score)
    }

    suspend fun deleteScoreById(id: Long) {
        scoreDao.deleteScoreById(id)
    }
}
