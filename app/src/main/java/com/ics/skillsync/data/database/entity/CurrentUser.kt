package com.ics.skillsync.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "current_user")
data class CurrentUser(
    @PrimaryKey
    val id: Int = 1,
    val value: String
) 