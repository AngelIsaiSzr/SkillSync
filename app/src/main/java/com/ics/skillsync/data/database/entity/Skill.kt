package com.ics.skillsync.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "skills")
data class Skill(
    @PrimaryKey
    val id: String,
    val userId: String,
    val name: String,
    val type: SkillType,
    val level: Int
) {
    enum class SkillType {
        TEACH, LEARN
    }
} 