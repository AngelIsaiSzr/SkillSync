package com.ics.skillsync.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey
    val id: String,
    val firstName: String,
    val lastName: String,
    val username: String,
    val email: String,
    val password: String,
    val role: String
) {
    // Constructor sin argumentos requerido por Firestore
    constructor() : this(
        id = "",
        firstName = "",
        lastName = "",
        username = "",
        email = "",
        password = "",
        role = ""
    )
} 