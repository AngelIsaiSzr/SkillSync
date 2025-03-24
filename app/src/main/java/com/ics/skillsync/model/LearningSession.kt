package com.ics.skillsync.model

import java.util.Date

data class LearningSession(
    val id: String = "",
    val mentorId: String = "",
    val learnerId: String = "",
    val skill: String = "",
    val title: String = "",
    val description: String = "",
    val date: Date = Date(),
    val duration: Int = 60, // en minutos
    val status: SessionStatus = SessionStatus.PENDING,
    val price: Double = 0.0,
    val meetingLink: String = "",
    val notes: String = ""
)

enum class SessionStatus {
    PENDING,
    CONFIRMED,
    COMPLETED,
    CANCELLED
} 