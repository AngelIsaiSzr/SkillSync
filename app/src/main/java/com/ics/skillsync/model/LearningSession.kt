package com.ics.skillsync.model

import java.util.Date

enum class SessionStatus {
    PENDING,
    CONFIRMED,
    COMPLETED,
    CANCELLED
}

data class LearningSession(
    val id: String = "",
    val title: String = "",
    val date: Date = Date(),
    val duration: Int = 0,
    val mentorId: String = "",
    val learnerId: String = "",
    val learnerName: String = "",
    val status: SessionStatus = SessionStatus.PENDING,
    val description: String = "",
    val price: Double = 0.0,
    val meetingLink: String = "",
    val notes: String = ""
) 