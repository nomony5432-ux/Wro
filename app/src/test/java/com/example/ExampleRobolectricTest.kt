package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.WROScore
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("WRO 2026 Scorer", appName)
  }

  @Test
  fun `test scoring calculation and capping logic`() {
    // 1. Let's test standard values
    val scoreNormal = WROScore(
        teamName = "Test Team",
        round = "Round 1",
        timeSeconds = 85,
        visitorsUpright = 2, // 2 * 10 = 20 pts
        visitorsPartial = 1, // 1 * 5 = 5 pts -> Total 25
        redTowerComplete = 1, // 1 * 15 = 15 pts
        redTowerPartial = 1,  // 1 * 10 = 10 pts -> Total 25
        yellowTowerComplete = 1, // 1 * 25 = 25 pts
        yellowTowerPartial = 0,  // Total 25
        artefactsComplete = 2, // 2 * 15 = 30 pts
        artefactsPartial = 1,  // 1 * 5 = 5 pts -> Total 35
        dirtCleaned = 5,       // 5 * 2 = 10 pts
        barrierBonus = 2,      // 2 * 10 = 20 pts
        parrotBonus = 1        // 1 * 10 = 10 pts
    )
    
    assertEquals(25, scoreNormal.visitorsScore)
    assertEquals(25, scoreNormal.redTowerScore)
    assertEquals(25, scoreNormal.yellowTowerScore)
    assertEquals(35, scoreNormal.artefactsScore)
    assertEquals(10, scoreNormal.dirtScore)
    assertEquals(20, scoreNormal.barrierScore)
    assertEquals(10, scoreNormal.parrotScore)
    assertEquals(150, scoreNormal.totalScore)

    // 2. Let's test overflow capping values to ensure capping logic works correctly
    val scoreOverflow = WROScore(
        teamName = "Test Overflow",
        round = "Round 2",
        timeSeconds = 120,
        visitorsUpright = 5, // 5 * 10 = 50 -> Capped at 40
        visitorsPartial = 2,
        redTowerComplete = 3, // 3 * 15 = 45 -> Capped at 30
        redTowerPartial = 1,
        yellowTowerComplete = 3, // 3 * 25 = 75 -> Capped at 50
        yellowTowerPartial = 1,
        artefactsComplete = 5, // 5 * 15 = 75 -> Capped at 60
        artefactsPartial = 2,
        dirtCleaned = 15,       // 15 * 2 = 30 -> Capped at 20
        barrierBonus = 3,      // 3 * 10 = 30 -> Capped at 20
        parrotBonus = 2        // 2 * 10 = 20 -> Capped at 10
    )
    
    assertEquals(40, scoreOverflow.visitorsScore)
    assertEquals(30, scoreOverflow.redTowerScore)
    assertEquals(50, scoreOverflow.yellowTowerScore)
    assertEquals(60, scoreOverflow.artefactsScore)
    assertEquals(20, scoreOverflow.dirtScore)
    assertEquals(20, scoreOverflow.barrierScore)
    assertEquals(10, scoreOverflow.parrotScore)
    assertEquals(230, scoreOverflow.totalScore)
  }
}
