package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.WRODatabase
import com.example.data.WROScore
import com.example.data.WROScoreRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class WROScoreViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: WROScoreRepository
    
    init {
        val database = WRODatabase.getDatabase(application)
        repository = WROScoreRepository(database.scoreDao())
    }
    
    // UI Navigation/Screen state
    enum class Screen {
        Scorer, History
    }
    private val _currentScreen = MutableStateFlow(Screen.Scorer)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()
    
    fun setScreen(screen: Screen) {
        _currentScreen.value = screen
    }
    
    // Sort options
    enum class SortBy {
        Date, Score, Team
    }
    private val _sortBy = MutableStateFlow(SortBy.Date)
    val sortBy: StateFlow<SortBy> = _sortBy.asStateFlow()
    
    fun setSortBy(sort: SortBy) {
        _sortBy.value = sort
    }
    
    // Live scores data stream sorted reactively
    val scoresList: StateFlow<List<WROScore>> = repository.allScores
        .combine(_sortBy) { list, sort ->
            when (sort) {
                SortBy.Date -> list.sortedByDescending { it.timestamp }
                SortBy.Score -> list.sortedByDescending { it.totalScore }
                SortBy.Team -> list.sortedWith(compareBy<WROScore> { it.teamName.lowercase() }.thenByDescending { it.timestamp })
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
        
    // --- Scorer Form States ---
    val editingId = MutableStateFlow<Long?>(null)
    
    val teamName = MutableStateFlow("")
    val round = MutableStateFlow("")
    val timeInputText = MutableStateFlow("0")
    
    // 1. Visitors (Max 4 visitors)
    val visitorsUpright = MutableStateFlow(0)
    val visitorsPartial = MutableStateFlow(0)
    
    // 2. Red Tower (Max 2 towers)
    val redTowerComplete = MutableStateFlow(0)
    val redTowerPartial = MutableStateFlow(0)
    
    // 3. Yellow Tower (Max 2 towers)
    val yellowTowerComplete = MutableStateFlow(0)
    val yellowTowerPartial = MutableStateFlow(0)
    
    // 4. Artefacts (Max 4 artefacts)
    val artefactsComplete = MutableStateFlow(0)
    val artefactsPartial = MutableStateFlow(0)
    
    // 5. Dirt Cleaned (Max 10 particles)
    val dirtCleaned = MutableStateFlow(0)
    
    // 6. Barriers (Max 2, defaults to 2 as undamaged)
    val barrierBonus = MutableStateFlow(2)
    
    // 7. Parrot (Max 1, defaults to 1 as undamaged)
    val parrotBonus = MutableStateFlow(1)
    
    // Stopwatch states
    private val _isTimerRunning = MutableStateFlow(false)
    val isTimerRunning = _isTimerRunning.asStateFlow()
    
    private var timerJob: Job? = null
    
    fun startStopwatch() {
        if (_isTimerRunning.value) {
            _isTimerRunning.value = false
            timerJob?.cancel()
        } else {
            _isTimerRunning.value = true
            val initialSeconds = timeInputText.value.toIntOrNull() ?: 0
            timerJob = viewModelScope.launch {
                var seconds = initialSeconds
                while (true) {
                    delay(1000)
                    seconds++
                    timeInputText.value = seconds.toString()
                }
            }
        }
    }
    
    fun pauseStopwatch() {
        _isTimerRunning.value = false
        timerJob?.cancel()
    }
    
    fun resetStopwatch() {
        _isTimerRunning.value = false
        timerJob?.cancel()
        timeInputText.value = "0"
    }
    
    // --- Safe Increment/Decrement Methods ---
    fun changeVisitorsUpright(delta: Int) {
        val currentUpright = visitorsUpright.value
        val currentPartial = visitorsPartial.value
        val next = (currentUpright + delta).coerceIn(0, 4)
        if (next + currentPartial <= 4) {
            visitorsUpright.value = next
        } else {
            // Auto-adjust partial to fit physical limit
            visitorsUpright.value = next
            visitorsPartial.value = 4 - next
        }
    }
    
    fun changeVisitorsPartial(delta: Int) {
        val currentUpright = visitorsUpright.value
        val currentPartial = visitorsPartial.value
        val next = (currentPartial + delta).coerceIn(0, 4)
        if (next + currentUpright <= 4) {
            visitorsPartial.value = next
        } else {
            // Auto-adjust upright to fit physical limit
            visitorsPartial.value = next
            visitorsUpright.value = 4 - next
        }
    }
    
    fun changeRedTowerComplete(delta: Int) {
        val currentComp = redTowerComplete.value
        val currentPart = redTowerPartial.value
        val next = (currentComp + delta).coerceIn(0, 2)
        if (next + currentPart <= 2) {
            redTowerComplete.value = next
        } else {
            redTowerComplete.value = next
            redTowerPartial.value = 2 - next
        }
    }
    
    fun changeRedTowerPartial(delta: Int) {
        val currentComp = redTowerComplete.value
        val currentPart = redTowerPartial.value
        val next = (currentPart + delta).coerceIn(0, 2)
        if (next + currentComp <= 2) {
            redTowerPartial.value = next
        } else {
            redTowerPartial.value = next
            redTowerComplete.value = 2 - next
        }
    }
    
    fun changeYellowTowerComplete(delta: Int) {
        val currentComp = yellowTowerComplete.value
        val currentPart = yellowTowerPartial.value
        val next = (currentComp + delta).coerceIn(0, 2)
        if (next + currentPart <= 2) {
            yellowTowerComplete.value = next
        } else {
            yellowTowerComplete.value = next
            yellowTowerPartial.value = 2 - next
        }
    }
    
    fun changeYellowTowerPartial(delta: Int) {
        val currentComp = yellowTowerComplete.value
        val currentPart = yellowTowerPartial.value
        val next = (currentPart + delta).coerceIn(0, 2)
        if (next + currentComp <= 2) {
            yellowTowerPartial.value = next
        } else {
            yellowTowerPartial.value = next
            yellowTowerComplete.value = 2 - next
        }
    }
    
    fun changeArtefactsComplete(delta: Int) {
        val currentComp = artefactsComplete.value
        val currentPart = artefactsPartial.value
        val next = (currentComp + delta).coerceIn(0, 4)
        if (next + currentPart <= 4) {
            artefactsComplete.value = next
        } else {
            artefactsComplete.value = next
            artefactsPartial.value = 4 - next
        }
    }
    
    fun changeArtefactsPartial(delta: Int) {
        val currentComp = artefactsComplete.value
        val currentPart = artefactsPartial.value
        val next = (currentPart + delta).coerceIn(0, 4)
        if (next + currentComp <= 4) {
            artefactsPartial.value = next
        } else {
            artefactsPartial.value = next
            artefactsComplete.value = 4 - next
        }
    }
    
    fun changeDirtCleaned(delta: Int) {
        dirtCleaned.value = (dirtCleaned.value + delta).coerceIn(0, 10)
    }
    
    fun changeBarrierBonus(delta: Int) {
        barrierBonus.value = (barrierBonus.value + delta).coerceIn(0, 2)
    }
    
    fun changeParrotBonus(delta: Int) {
        parrotBonus.value = (parrotBonus.value + delta).coerceIn(0, 1)
    }
    
    // Reset Form to baseline defaults
    fun clearForm() {
        editingId.value = null
        teamName.value = ""
        round.value = ""
        timeInputText.value = "0"
        visitorsUpright.value = 0
        visitorsPartial.value = 0
        redTowerComplete.value = 0
        redTowerPartial.value = 0
        yellowTowerComplete.value = 0
        yellowTowerPartial.value = 0
        artefactsComplete.value = 0
        artefactsPartial.value = 0
        dirtCleaned.value = 0
        barrierBonus.value = 2 // Defaults to maximum (undamaged)
        parrotBonus.value = 1  // Defaults to maximum (undamaged)
        _isTimerRunning.value = false
        timerJob?.cancel()
    }
    
    // Save or Update entry in database
    fun saveScore() {
        val currentTeam = teamName.value.ifBlank { "Unknown Team" }
        val currentRound = round.value.ifBlank { "Round 1" }
        val seconds = timeInputText.value.toIntOrNull() ?: 0
        
        val score = WROScore(
            id = editingId.value ?: 0,
            teamName = currentTeam,
            round = currentRound,
            timeSeconds = seconds,
            visitorsUpright = visitorsUpright.value,
            visitorsPartial = visitorsPartial.value,
            redTowerComplete = redTowerComplete.value,
            redTowerPartial = redTowerPartial.value,
            yellowTowerComplete = yellowTowerComplete.value,
            yellowTowerPartial = yellowTowerPartial.value,
            artefactsComplete = artefactsComplete.value,
            artefactsPartial = artefactsPartial.value,
            dirtCleaned = dirtCleaned.value,
            barrierBonus = barrierBonus.value,
            parrotBonus = parrotBonus.value,
            timestamp = System.currentTimeMillis()
        )
        
        viewModelScope.launch {
            repository.insertScore(score)
            clearForm()
            setScreen(Screen.History)
        }
    }
    
    // Edit Score (Loads data back into scorer interface)
    fun loadScoreForEdit(score: WROScore) {
        editingId.value = score.id
        teamName.value = score.teamName
        round.value = score.round
        timeInputText.value = score.timeSeconds.toString()
        visitorsUpright.value = score.visitorsUpright
        visitorsPartial.value = score.visitorsPartial
        redTowerComplete.value = score.redTowerComplete
        redTowerPartial.value = score.redTowerPartial
        yellowTowerComplete.value = score.yellowTowerComplete
        yellowTowerPartial.value = score.yellowTowerPartial
        artefactsComplete.value = score.artefactsComplete
        artefactsPartial.value = score.artefactsPartial
        dirtCleaned.value = score.dirtCleaned
        barrierBonus.value = score.barrierBonus
        parrotBonus.value = score.parrotBonus
        
        _isTimerRunning.value = false
        timerJob?.cancel()
        
        setScreen(Screen.Scorer)
    }
    
    fun deleteScore(id: Long) {
        viewModelScope.launch {
            repository.deleteScoreById(id)
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}
