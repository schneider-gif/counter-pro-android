package com.counterpro.app.domain.model

data class CountEntry(
    val id: Long = 0,
    val label: String,
    val count: Int,
    val timestamp: Long = System.currentTimeMillis()
)
