package com.ics.skillsync.model

data class User(
    val id: String,
    val name: String,
    val userType: UserType,
    val skills: List<String>,
    val rating: Float,
    val reviews: Int
)

enum class UserType {
    MENTOR,
    LEARNER
} 