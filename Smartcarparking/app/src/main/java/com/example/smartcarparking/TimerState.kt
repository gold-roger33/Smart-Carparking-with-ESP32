package com.example.smartcarparking

data class TimerState(
    val spotName: String,
    val remainingTime: Long, // in seconds
    val isRunning: Boolean
)