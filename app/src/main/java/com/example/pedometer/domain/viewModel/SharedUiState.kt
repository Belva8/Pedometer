package com.example.pedometer.domain.viewModel

import com.example.pedometer.room_db.DailyStepsEntity


//Klasa  predstavlja stanje korisničkog sučelja u vezi s dijeljenjem informacija o broju koraka.
data class ShareUiState(
    val stepsToday: Long = 0,
    val stepsAllDays: List<DailyStepsEntity> = emptyList()
)
