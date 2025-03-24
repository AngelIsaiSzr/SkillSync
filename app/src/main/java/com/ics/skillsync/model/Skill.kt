package com.ics.skillsync.model

data class Skill(
    val id: String,
    val name: String,
    val description: String,
    val category: SkillCategory,
    val imageUrl: String,
    val mentorsCount: Int = 0,
    val learnersCount: Int = 0
)

enum class SkillCategory {
    TECNOLOGIA,
    IDIOMAS,
    MUSICA,
    COCINA,
    ARTE,
    DEPORTES,
    OTROS
}

data class SkillStats(
    val mentorsAvailable: Int,
    val activeStudents: Int
) 