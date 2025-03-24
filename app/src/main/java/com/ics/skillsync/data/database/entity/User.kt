package com.ics.skillsync.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val firstName: String,
    val lastName: String,
    val username: String,
    val email: String,
    val password: String,
    val role: String
) 