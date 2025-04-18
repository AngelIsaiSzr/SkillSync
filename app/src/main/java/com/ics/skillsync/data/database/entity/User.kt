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
    val role: String,
    val photoUrl: String = "",
    val verificationLevel: Int = 0 // 0 = No verificado, 1 = Email verificado, 2 = Imagen verificada, 3 = Examen aprobado
) {
    // Constructor sin argumentos requerido por Firestore
    constructor() : this(
        id = "",
        firstName = "",
        lastName = "",
        username = "",
        email = "",
        password = "",
        role = "",
        photoUrl = "",
        verificationLevel = 0
    )
} 