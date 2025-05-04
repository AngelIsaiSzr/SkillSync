package com.ics.skillsync.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "skills")
data class Skill(
    @PrimaryKey
    val id: String,
    val userId: String,
    val name: String,
    val type: SkillType, // TEACH o LEARN
    val level: Int // 1-Básico, 2-Intermedio, 3-Avanzado
) {
    enum class SkillType {
        TEACH, LEARN
    }
} 