package com.example.pedometer.domain.viewModel.stats

import com.example.pedometer.room_db.DailyStepsEntity

//klasa  za predstavljanje različitih stanja/rezultata operacije -  učitavanje, uspjeh,neuspjeh za dohvaćanje iz repositorya
sealed class Response {
    object  Loading : Response()
    data class  Success (val data: List<DailyStepsEntity>) : Response()
}
