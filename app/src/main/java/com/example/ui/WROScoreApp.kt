package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.WROScore
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WROScoreApp(viewModel: WROScoreViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val scoresList by viewModel.scoresList.collectAsStateWithLifecycle()
    val editingId by viewModel.editingId.collectAsStateWithLifecycle()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "WRO 2026 Scorer",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Heritage Heroes • Junior Division",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                        )
                    }
                },
                navigationIcon = {
                    Icon(
                        imageVector = Icons.Default.SportsEsports,
                        contentDescription = "App Icon",
                        modifier = Modifier.padding(start = 12.dp, end = 8.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)
                )
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = currentScreen == WROScoreViewModel.Screen.Scorer,
                    onClick = { viewModel.setScreen(WROScoreViewModel.Screen.Scorer) },
                    icon = { 
                        Icon(
                            imageVector = if (currentScreen == WROScoreViewModel.Screen.Scorer) Icons.Filled.Calculate else Icons.Outlined.Calculate, 
                            contentDescription = "Scorer"
                        ) 
                    },
                    label = { Text("คำนวณคะแนน") },
                    modifier = Modifier.testTag("nav_scorer")
                )
                NavigationBarItem(
                    selected = currentScreen == WROScoreViewModel.Screen.History,
                    onClick = { viewModel.setScreen(WROScoreViewModel.Screen.History) },
                    icon = { 
                        Icon(
                            imageVector = if (currentScreen == WROScoreViewModel.Screen.History) Icons.Filled.History else Icons.Outlined.History, 
                            contentDescription = "History"
                        ) 
                    },
                    label = { Text("ประวัติการบันทึก") },
                    modifier = Modifier.testTag("nav_history")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when (currentScreen) {
                WROScoreViewModel.Screen.Scorer -> {
                    ScorerScreen(viewModel = viewModel)
                }
                WROScoreViewModel.Screen.History -> {
                    HistoryScreen(viewModel = viewModel, scores = scoresList)
                }
            }
        }
    }
}

@Composable
fun ScorerScreen(viewModel: WROScoreViewModel) {
    val teamName by viewModel.teamName.collectAsStateWithLifecycle()
    val round by viewModel.round.collectAsStateWithLifecycle()
    val timeText by viewModel.timeInputText.collectAsStateWithLifecycle()
    val isTimerRunning by viewModel.isTimerRunning.collectAsStateWithLifecycle()
    val editingId by viewModel.editingId.collectAsStateWithLifecycle()
    
    // Scoring fields
    val vUpright by viewModel.visitorsUpright.collectAsStateWithLifecycle()
    val vPartial by viewModel.visitorsPartial.collectAsStateWithLifecycle()
    
    val rComplete by viewModel.redTowerComplete.collectAsStateWithLifecycle()
    val rPartial by viewModel.redTowerPartial.collectAsStateWithLifecycle()
    
    val yComplete by viewModel.yellowTowerComplete.collectAsStateWithLifecycle()
    val yPartial by viewModel.yellowTowerPartial.collectAsStateWithLifecycle()
    
    val aComplete by viewModel.artefactsComplete.collectAsStateWithLifecycle()
    val aPartial by viewModel.artefactsPartial.collectAsStateWithLifecycle()
    
    val dCleaned by viewModel.dirtCleaned.collectAsStateWithLifecycle()
    val bBonus by viewModel.barrierBonus.collectAsStateWithLifecycle()
    val pBonus by viewModel.parrotBonus.collectAsStateWithLifecycle()
    
    var showClearConfirm by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    
    // Live calculations
    val visitorsScore = (vUpright * 10 + vPartial * 5).coerceAtMost(40)
    val redTowerScore = (rComplete * 15 + rPartial * 10).coerceAtMost(30)
    val yellowTowerScore = (yComplete * 25 + yPartial * 15).coerceAtMost(50)
    val artefactsScore = (aComplete * 15 + aPartial * 5).coerceAtMost(60)
    val dirtScore = (dCleaned * 2).coerceAtMost(20)
    val barrierScore = (bBonus * 10).coerceAtMost(20)
    val parrotScore = (pBonus * 10).coerceAtMost(10)
    val totalScore = visitorsScore + redTowerScore + yellowTowerScore + artefactsScore + dirtScore + barrierScore + parrotScore

    if (showClearConfirm) {
        AlertDialog(
            onDismissRequest = { showClearConfirm = false },
            title = { Text("ล้างข้อมูลคะแนนทั้งหมด?") },
            text = { Text("คุณต้องการรีเซ็ตตัวนับภารกิจและช่องกรอกข้อมูลทั้งหมดเป็นค่าเริ่มต้นใช่หรือไม่?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearForm()
                        showClearConfirm = false
                    },
                    modifier = Modifier.testTag("confirm_clear_button")
                ) {
                    Text("ล้างข้อมูล", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirm = false }) {
                    Text("ยกเลิก")
                }
            }
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Main scoring controls
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Edit status banner
            if (editingId != null) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Edit, contentDescription = "Editing Mode", tint = MaterialTheme.colorScheme.onSecondaryContainer)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "กำลังแก้ไขประวัติคะแนนเดิม",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                            IconButton(
                                onClick = { viewModel.clearForm() },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Cancel edit", modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }

            // 1. Team & Round Details Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "ข้อมูลทีมการแข่งขัน",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        OutlinedTextField(
                            value = teamName,
                            onValueChange = { viewModel.teamName.value = it },
                            label = { Text("ชื่อทีม (Team Name)") },
                            placeholder = { Text("ระบุชื่อทีม") },
                            leadingIcon = { Icon(Icons.Default.Group, contentDescription = "Team") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("team_name_input")
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Text(
                            text = "รอบการแข่งขัน (Round)",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        
                        // Round selection chips
                        val rounds = listOf("Round 1", "Round 2", "Round 3")
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            rounds.forEach { r ->
                                FilterChip(
                                    selected = round == r,
                                    onClick = { 
                                        viewModel.round.value = r 
                                        focusManager.clearFocus()
                                    },
                                    label = { Text(r) },
                                    modifier = Modifier.testTag("round_chip_$r")
                                )
                            }
                            FilterChip(
                                selected = !rounds.contains(round) && round.isNotBlank(),
                                onClick = { 
                                    if (rounds.contains(round) || round.isBlank()) {
                                        viewModel.round.value = "Round 4"
                                    }
                                },
                                label = { Text("อื่นๆ") },
                                modifier = Modifier.testTag("round_chip_other")
                            )
                        }
                        
                        if (!rounds.contains(round)) {
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = round,
                                onValueChange = { viewModel.round.value = it },
                                label = { Text("ระบุรอบเพิ่มเติม") },
                                leadingIcon = { Icon(Icons.Default.Tag, contentDescription = "Round") },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("round_input")
                            )
                        }
                    }
                }
            }

            // 2. Stopwatch & Timer Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "นาฬิกาจับเวลา & บันทึกเวลา",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            // Large Stopwatch Display
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                val secondsInt = timeText.toIntOrNull() ?: 0
                                val mins = secondsInt / 60
                                val secs = secondsInt % 60
                                val formattedTime = String.format("%02d:%02d", mins, secs)
                                
                                Text(
                                    text = formattedTime,
                                    style = MaterialTheme.typography.displayMedium,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isTimerRunning) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "วินาทีรวม: $secondsInt s",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            
                            // Live controls
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = { 
                                        focusManager.clearFocus()
                                        viewModel.startStopwatch() 
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isTimerRunning) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                                    ),
                                    modifier = Modifier
                                        .height(48.dp)
                                        .testTag("stopwatch_toggle")
                                ) {
                                    Icon(
                                        imageVector = if (isTimerRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                                        contentDescription = "Stopwatch Play/Pause"
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(if (isTimerRunning) "หยุด" else "จับเวลา")
                                }
                                
                                OutlinedButton(
                                    onClick = { 
                                        focusManager.clearFocus()
                                        viewModel.resetStopwatch() 
                                    },
                                    modifier = Modifier
                                        .height(48.dp)
                                        .testTag("stopwatch_reset")
                                ) {
                                    Icon(Icons.Default.Refresh, contentDescription = "Stopwatch Reset")
                                }
                            }
                        }
                        
                        // Manual Override
                        OutlinedTextField(
                            value = timeText,
                            onValueChange = { 
                                if (it.isEmpty() || it.all { char -> char.isDigit() }) {
                                    viewModel.timeInputText.value = it
                                }
                            },
                            label = { Text("เวลาที่ใช้แข่งขันจริง (วินาที)") },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Done
                            ),
                            leadingIcon = { Icon(Icons.Default.Timer, contentDescription = "Timer") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("time_seconds_input")
                        )
                    }
                }
            }

            // Mission 1: Visitors Card
            item {
                MissionCard(
                    title = "1. นำ Visitors ชมสถานที่",
                    icon = Icons.Default.DirectionsWalk,
                    colorAccent = Color(0xFF4CAF50), // Green for Excavation site visitors
                    scoreText = "$visitorsScore / 40",
                    description = "พา Visitors ไปยังพื้นที่สีที่ตรงกัน (เขียว = Excavation Site, แดง = Museum, ดำ/น้ำเงิน = Cobblestone)",
                    maxItems = 4,
                    currentTotalItems = vUpright + vPartial
                ) {
                    CounterRow(
                        label = "อยู่ภายในโดยสมบูรณ์ + ตั้งตรง (10 คะแนน/คน)",
                        subLabel = "Visitor สัมผัสเฉพาะในพื้นที่เป้าหมายเท่านั้น",
                        count = vUpright,
                        onIncrement = { viewModel.changeVisitorsUpright(1) },
                        onDecrement = { viewModel.changeVisitorsUpright(-1) },
                        modifierTag = "visitors_upright"
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                    CounterRow(
                        label = "สัมผัสบางส่วน / ไม่ตั้งตรง (5 คะแนน/คน)",
                        subLabel = "สัมผัสภายในบางส่วน หรือล้มอยู่ภายในพื้นที่",
                        count = vPartial,
                        onIncrement = { viewModel.changeVisitorsPartial(1) },
                        onDecrement = { viewModel.changeVisitorsPartial(-1) },
                        modifierTag = "visitors_partial"
                    )
                }
            }

            // Mission 2: Red Tower Card
            item {
                MissionCard(
                    title = "2. สร้าง Red Tower",
                    icon = Icons.Default.AccountBalance,
                    colorAccent = Color(0xFFF44336), // Red for Red Tower
                    scoreText = "$redTowerScore / 30",
                    description = "บูรณะซ่อมแซม Red Tower สร้างใหม่ให้สมบูรณ์",
                    maxItems = 2,
                    currentTotalItems = rComplete + rPartial
                ) {
                    CounterRow(
                        label = "อยู่ภายในสมบูรณ์ + ตั้งตรง (15 คะแนน/ชิ้น)",
                        subLabel = "สัมผัสเฉพาะพื้นที่เป้าหมายสีแดง (รวมขอบสีส้ม)",
                        count = rComplete,
                        onIncrement = { viewModel.changeRedTowerComplete(1) },
                        onDecrement = { viewModel.changeRedTowerComplete(-1) },
                        modifierTag = "red_tower_complete"
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                    CounterRow(
                        label = "สัมผัสบางส่วน + ตั้งตรง (10 คะแนน/ชิ้น)",
                        subLabel = "ตั้งตรงอยู่แต่บางส่วนสัมผัสขอบสีแดง",
                        count = rPartial,
                        onIncrement = { viewModel.changeRedTowerPartial(1) },
                        onDecrement = { viewModel.changeRedTowerPartial(-1) },
                        modifierTag = "red_tower_partial"
                    )
                }
            }

            // Mission 3: Yellow Tower Card
            item {
                MissionCard(
                    title = "3. สร้าง Yellow Tower",
                    icon = Icons.Default.HomeWork,
                    colorAccent = Color(0xFFFFEB3B), // Yellow for Yellow Tower
                    scoreText = "$yellowTowerScore / 50",
                    description = "ประกอบส่วนบนของ Yellow Tower (Tops) บนฐาน (Bases) และจัดวางในพื้นที่สีเหลือง",
                    maxItems = 2,
                    currentTotalItems = yComplete + yPartial
                ) {
                    CounterRow(
                        label = "วางถูกต้อง + Base ในพื้นที่สมบูรณ์ (25 คะแนน/ชิ้น)",
                        subLabel = "วางประกบกันถูกต้อง และฐานสัมผัสเฉพาะในพื้นที่เหลือง",
                        count = yComplete,
                        onIncrement = { viewModel.changeYellowTowerComplete(1) },
                        onDecrement = { viewModel.changeYellowTowerComplete(-1) },
                        modifierTag = "yellow_tower_complete"
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                    CounterRow(
                        label = "วางถูกต้อง + Base สัมผัสบางส่วน (15 คะแนน/ชิ้น)",
                        subLabel = "วางประกอบเสร็จถูกต้อง แต่ตัวฐานเฉียงสัมผัสบางส่วนขอบ",
                        count = yPartial,
                        onIncrement = { viewModel.changeYellowTowerPartial(1) },
                        onDecrement = { viewModel.changeYellowTowerPartial(-1) },
                        modifierTag = "yellow_tower_partial"
                    )
                }
            }

            // Mission 4: Artefacts Card
            item {
                MissionCard(
                    title = "4. นำ Artefacts ไปจัดแสดง",
                    icon = Icons.Default.Museum,
                    colorAccent = Color(0xFF03A9F4), // Sky Blue for artefacts
                    scoreText = "$artefactsScore / 60",
                    description = "รวบรวมโบราณวัตถุ (Artefacts) จากแหล่งขุดค้น นำมาวางที่ Exhibition Spot ใน Museum",
                    maxItems = 4,
                    currentTotalItems = aComplete + aPartial
                ) {
                    CounterRow(
                        label = "อยู่ภายในสมบูรณ์ + ตั้งตรง (15 คะแนน/ชิ้น)",
                        subLabel = "วางตรงสี จับคู่สีถูกต้อง โดยสมบูรณ์ไม่แตะนอกพื้นที่",
                        count = aComplete,
                        onIncrement = { viewModel.changeArtefactsComplete(1) },
                        onDecrement = { viewModel.changeArtefactsComplete(-1) },
                        modifierTag = "artefacts_complete"
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                    CounterRow(
                        label = "สัมผัสบางส่วน / ไม่ตั้งตรง (5 คะแนน/ชิ้น)",
                        subLabel = "สีถูกต้อง แต่สัมผัสบางส่วน หรือล้มเอียงอยู่ในพื้นที่",
                        count = aPartial,
                        onIncrement = { viewModel.changeArtefactsPartial(1) },
                        onDecrement = { viewModel.changeArtefactsPartial(-1) },
                        modifierTag = "artefacts_partial"
                    )
                }
            }

            // Mission 5: Dirt Card
            item {
                MissionCard(
                    title = "5. ทำความสะอาด Dirt",
                    icon = Icons.Default.CleaningServices,
                    colorAccent = Color(0xFF795548), // Brown for dirt particles
                    scoreText = "$dirtScore / 20",
                    description = "กวาดทำความสะอาดเม็ดฝุ่น (Dirt Particles) ออกไปจากแนว Cobblestone",
                    maxItems = 10,
                    currentTotalItems = dCleaned
                ) {
                    CounterRow(
                        label = "ไม่สัมผัส Cobblestone (2 คะแนน/ชิ้น)",
                        subLabel = "ขยับพ้นแนวออกไปโดยสมบูรณ์ (สูงสุด 10 ชิ้น)",
                        count = dCleaned,
                        onIncrement = { viewModel.changeDirtCleaned(1) },
                        onDecrement = { viewModel.changeDirtCleaned(-1) },
                        modifierTag = "dirt_cleaned"
                    )
                }
            }

            // Bonus: Barrier & Parrot Card
            item {
                MissionCard(
                    title = "6. คะแนนโบนัสรักษาความปลอดภัย",
                    icon = Icons.Default.CardMembership,
                    colorAccent = Color(0xFF9C27B0), // Purple for bonuses
                    scoreText = "${barrierScore + parrotScore} / 30",
                    description = "หุ่นยนต์ต้องแข่งขันโดยรักษาสิ่งแวดล้อมโดยไม่ทำให้ส่วนประกอบเสียหาย/ขยับออกจากจุดสีเทา",
                    maxItems = 3,
                    currentTotalItems = bBonus + pBonus
                ) {
                    CounterRow(
                        label = "โบนัส Barrier ไม่เสียหาย/ไม่เคลื่อนย้าย (10 คะแนน/ชิ้น)",
                        subLabel = "รักษาแผงกั้นสีแดง/ขาวไม่พ้นพื้นที่ (สูงสุด 2 ชิ้น)",
                        count = bBonus,
                        onIncrement = { viewModel.changeBarrierBonus(1) },
                        onDecrement = { viewModel.changeBarrierBonus(-1) },
                        modifierTag = "barrier_bonus"
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                    CounterRow(
                        label = "โบนัส Parrot ไม่เสียหาย/ไม่เคลื่อนย้าย (10 คะแนน)",
                        subLabel = "เจ้านกแก้วสัตว์ป่าไม่เสียหาย/ไม่เลื่อนพ้นสีเทา (สูงสุด 1 ตัว)",
                        count = pBonus,
                        onIncrement = { viewModel.changeParrotBonus(1) },
                        onDecrement = { viewModel.changeParrotBonus(-1) },
                        modifierTag = "parrot_bonus"
                    )
                }
            }
        }

        // 3. Persistent Sticky Summary Bottom Bar
        Surface(
            tonalElevation = 8.dp,
            shadowElevation = 16.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.surface)
                    .navigationBarsPadding()
                    .padding(16.dp)
            ) {
                // Live Score Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "คะแนนรวมสุทธิ",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = "$totalScore",
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.testTag("live_total_score")
                            )
                            Text(
                                text = " / 230",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(bottom = 4.dp, start = 2.dp)
                            )
                        }
                    }
                    
                    // Quick Mini Stats Bar Chart
                    Column(
                        horizontalAlignment = Alignment.End,
                        modifier = Modifier.width(180.dp)
                    ) {
                        LinearProgressIndicator(
                            progress = { totalScore / 230f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "ประสิทธิภาพ: ${String.format("%.1f", (totalScore / 230f) * 100)}%",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Save and clear buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = { showClearConfirm = true },
                        modifier = Modifier
                            .weight(0.4f)
                            .height(50.dp)
                            .testTag("clear_button")
                    ) {
                        Icon(Icons.Default.DeleteSweep, contentDescription = "Clear All")
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("รีเซ็ต")
                    }
                    
                    Button(
                        onClick = { viewModel.saveScore() },
                        modifier = Modifier
                            .weight(0.6f)
                            .height(50.dp)
                            .testTag("save_score_button")
                    ) {
                        Icon(Icons.Default.Save, contentDescription = "Save score")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (editingId == null) "บันทึกผลการวิ่ง" else "อัปเดตประวัติ")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MissionCard(
    title: String,
    icon: ImageVector,
    colorAccent: Color,
    scoreText: String,
    description: String,
    maxItems: Int,
    currentTotalItems: Int,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                shape = RoundedCornerShape(16.dp)
            )
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                colorAccent.copy(alpha = 0.15f),
                                Color.Transparent
                            )
                        )
                    )
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Surface(
                            color = colorAccent.copy(alpha = 0.2f),
                            shape = CircleShape,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    tint = colorAccent,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    
                    // Task subscore bubble
                    Surface(
                        color = colorAccent.copy(alpha = 0.1f),
                        border = BorderStroke(1.dp, colorAccent.copy(alpha = 0.4f)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        Text(
                            text = scoreText,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = colorAccent,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }
            
            Column(modifier = Modifier.padding(16.dp)) {
                // Rule text description
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                
                // Inner layout counter content
                content()
                
                // Bottom count limiter feedback
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = if (currentTotalItems > maxItems) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f) 
                                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(6.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = if (currentTotalItems > maxItems) Icons.Default.Warning else Icons.Default.Info,
                        contentDescription = "Status info",
                        tint = if (currentTotalItems > maxItems) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "จำนวนชิ้นงานรวมบนบอร์ด: $currentTotalItems / $maxItems ชิ้น (จำกัดตามกติกาจริง)",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (currentTotalItems > maxItems) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun CounterRow(
    label: String,
    subLabel: String,
    count: Int,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    modifierTag: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
            if (subLabel.isNotBlank()) {
                Text(
                    text = subLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )
            }
        }
        
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            FilledIconButton(
                onClick = onDecrement,
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                modifier = Modifier
                    .size(36.dp)
                    .testTag("${modifierTag}_decrement")
            ) {
                Icon(Icons.Default.Remove, contentDescription = "Decrement", modifier = Modifier.size(16.dp))
            }
            
            Text(
                text = "$count",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .width(24.dp)
                    .testTag("${modifierTag}_value")
            )
            
            FilledIconButton(
                onClick = onIncrement,
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                modifier = Modifier
                    .size(36.dp)
                    .testTag("${modifierTag}_increment")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Increment", modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
fun HistoryScreen(viewModel: WROScoreViewModel, scores: List<WROScore>) {
    val sortBy by viewModel.sortBy.collectAsStateWithLifecycle()
    var scoreToDelete by remember { mutableStateOf<WROScore?>(null) }
    
    if (scoreToDelete != null) {
        AlertDialog(
            onDismissRequest = { scoreToDelete = null },
            title = { Text("ยืนยันการลบข้อมูล?") },
            text = { Text("คุณต้องการลบสถิติของทีม '${scoreToDelete?.teamName}' รอบ '${scoreToDelete?.round}' ใช่หรือไม่? ข้อมูลประวัตินี้จะถูกลบถาวร") },
            confirmButton = {
                TextButton(
                    onClick = {
                        scoreToDelete?.let { viewModel.deleteScore(it.id) }
                        scoreToDelete = null
                    },
                    modifier = Modifier.testTag("confirm_delete_button")
                ) {
                    Text("ลบรายการ", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { scoreToDelete = null }) {
                    Text("ยกเลิก")
                }
            }
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Stats Overview Headers Card
        if (scores.isNotEmpty()) {
            val maxScore = scores.maxOf { it.totalScore }
            val avgScore = scores.map { it.totalScore }.average()
            val totalRuns = scores.size
            
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                        Text("จำนวนรันทั้งหมด", style = MaterialTheme.typography.labelSmall)
                        Text("$totalRuns", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                    }
                    Box(modifier = Modifier
                        .width(1.dp)
                        .height(30.dp)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)))
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                        Text("คะแนนสูงสุด (Max)", style = MaterialTheme.typography.labelSmall)
                        Text("$maxScore", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                    }
                    Box(modifier = Modifier
                        .width(1.dp)
                        .height(30.dp)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)))
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                        Text("ค่าเฉลี่ย (Avg)", style = MaterialTheme.typography.labelSmall)
                        Text(String.format("%.1f", avgScore), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
        
        // Sorting filter bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "ประวัติผลคะแนน",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            // Sorting Chips
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                FilterChip(
                    selected = sortBy == WROScoreViewModel.SortBy.Date,
                    onClick = { viewModel.setSortBy(WROScoreViewModel.SortBy.Date) },
                    label = { Text("ล่าสุด") },
                    modifier = Modifier.testTag("sort_date")
                )
                FilterChip(
                    selected = sortBy == WROScoreViewModel.SortBy.Score,
                    onClick = { viewModel.setSortBy(WROScoreViewModel.SortBy.Score) },
                    label = { Text("คะแนน") },
                    modifier = Modifier.testTag("sort_score")
                )
                FilterChip(
                    selected = sortBy == WROScoreViewModel.SortBy.Team,
                    onClick = { viewModel.setSortBy(WROScoreViewModel.SortBy.Team) },
                    label = { Text("ชื่อทีม") },
                    modifier = Modifier.testTag("sort_team")
                )
            }
        }
        
        // Empty state
        if (scores.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Analytics,
                        contentDescription = "No history",
                        modifier = Modifier.size(72.dp),
                        tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "ยังไม่มีประวัติบันทึกคะแนน",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "ทำการคิดคะแนนในหน้าหลักและกดบันทึก ข้อมูลประวัติการวิ่งซ้อมจะถูกบันทึกเก็บไว้ที่นี่เพื่อประเมินผล",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(scores, key = { it.id }) { score ->
                    HistoryItemCard(
                        score = score,
                        onEdit = { viewModel.loadScoreForEdit(score) },
                        onDelete = { scoreToDelete = score }
                    )
                }
            }
        }
    }
}

@Composable
fun HistoryItemCard(
    score: WROScore,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val dateString = remember(score.timestamp) {
        val sdf = SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault())
        sdf.format(Date(score.timestamp))
    }
    
    val minutes = score.timeSeconds / 60
    val remainingSeconds = score.timeSeconds % 60
    val formattedTime = String.format("%02d:%02d", minutes, remainingSeconds)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("history_item_${score.id}"),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Team Name + Score
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = score.teamName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SuggestionChip(
                            onClick = {},
                            label = { Text(score.round) },
                            modifier = Modifier.height(24.dp)
                        )
                        Text(
                            text = "เวลา: $formattedTime (${score.timeSeconds}s)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                // Score Box
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "${score.totalScore}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "เต็ม 230",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 9.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                }
            }
            
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            
            // Detailed Mission breakdown points wrap-row
            Text(
                text = "คะแนนรายภารกิจ:",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 6.dp)
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Miniature badge bars
                ScoreBadge(name = "นักท่องเที่ยว", pts = score.visitorsScore, color = Color(0xFF4CAF50))
                ScoreBadge(name = "หอคอยแดง", pts = score.redTowerScore, color = Color(0xFFF44336))
                ScoreBadge(name = "หอคอยเหลือง", pts = score.yellowTowerScore, color = Color(0xFFFFC107))
                ScoreBadge(name = "วัตถุโบราณ", pts = score.artefactsScore, color = Color(0xFF03A9F4))
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ScoreBadge(name = "เม็ดฝุ่น", pts = score.dirtScore, color = Color(0xFF795548))
                ScoreBadge(name = "แผงกั้น", pts = score.barrierScore, color = Color(0xFF9C27B0))
                ScoreBadge(name = "นกแก้ว", pts = score.parrotScore, color = Color(0xFFE91E63))
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Footer with date and actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = dateString,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
                
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(
                        onClick = onEdit,
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier
                            .height(36.dp)
                            .testTag("edit_button_${score.id}")
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("แก้ไข")
                    }
                    
                    TextButton(
                        onClick = onDelete,
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        ),
                        modifier = Modifier
                            .height(36.dp)
                            .testTag("delete_button_${score.id}")
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("ลบ")
                    }
                }
            }
        }
    }
}

@Composable
fun ScoreBadge(name: String, pts: Int, color: Color) {
    Surface(
        color = color.copy(alpha = 0.08f),
        shape = RoundedCornerShape(4.dp),
        border = BorderStroke(1.dp, color.copy(alpha = 0.3f)),
        modifier = Modifier.padding(vertical = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .background(color, CircleShape)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "$name: $pts",
                style = MaterialTheme.typography.labelSmall,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )
        }
    }
}
