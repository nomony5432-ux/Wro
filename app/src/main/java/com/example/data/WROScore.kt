package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "scores")
data class WROScore(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val teamName: String,
    val round: String,
    val timestamp: Long = System.currentTimeMillis(),
    val timeSeconds: Int,
    
    // Visitor points details (max 4 visitors)
    val visitorsUpright: Int, // 10 pts each
    val visitorsPartial: Int, // 5 pts each
    
    // Red Tower details (max 2 red towers)
    val redTowerComplete: Int, // 15 pts each
    val redTowerPartial: Int, // 10 pts each
    
    // Yellow Tower details (max 2 yellow towers)
    val yellowTowerComplete: Int, // 25 pts each
    val yellowTowerPartial: Int, // 15 pts each
    
    // Artefacts details (max 4 artefacts)
    val artefactsComplete: Int, // 15 pts each
    val artefactsPartial: Int, // 5 pts each
    
    // Dirt details (max 10 dirt particles)
    val dirtCleaned: Int, // 2 pts each
    
    // Bonuses (max 2 barriers, 1 parrot)
    val barrierBonus: Int, // 10 pts each
    val parrotBonus: Int // 10 pts each
) {
    // Helper functions to calculate score of each task, capped at task max
    val visitorsScore: Int
        get() = (visitorsUpright * 10 + visitorsPartial * 5).coerceAtMost(40)
        
    val redTowerScore: Int
        get() = (redTowerComplete * 15 + redTowerPartial * 10).coerceAtMost(30)
        
    val yellowTowerScore: Int
        get() = (yellowTowerComplete * 25 + yellowTowerPartial * 15).coerceAtMost(50)
        
    val artefactsScore: Int
        get() = (artefactsComplete * 15 + artefactsPartial * 5).coerceAtMost(60)
        
    val dirtScore: Int
        get() = (dirtCleaned * 2).coerceAtMost(20)
        
    val barrierScore: Int
        get() = (barrierBonus * 10).coerceAtMost(20)
        
    val parrotScore: Int
        get() = (parrotBonus * 10).coerceAtMost(10)
        
    val totalScore: Int
        get() = visitorsScore + redTowerScore + yellowTowerScore + artefactsScore + dirtScore + barrierScore + parrotScore
}
