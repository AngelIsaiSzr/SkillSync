package com.ics.skillsync.model

data class Skill(
    val id: String,
    val name: String,
    val description: String,
    val category: SkillCategory,
    val imageUrl: String,
    val mentorsCount: Int = 0,
    val learnersCount: Int = 0,
    val mentorName: String = "",
    val level: Int = 1
)

enum class SkillCategory {
    TECNOLOGIA,
    DISENO,
    MARKETING,
    IDIOMAS,
    ARTE,
    MUSICA,
    GASTRONOMIA,
    DEPORTES,
    CIENCIAS,
    HUMANIDADES,
    FINANZAS,
    DERECHO,
    SALUD,
    EDUCACION,
    OTROS;

    fun getDisplayName(): String {
        return when (this) {
            TECNOLOGIA -> "Tecnología"
            DISENO -> "Diseño"
            MARKETING -> "Marketing"
            IDIOMAS -> "Idiomas"
            ARTE -> "Arte"
            MUSICA -> "Música"
            GASTRONOMIA -> "Gastronomía"
            DEPORTES -> "Deportes"
            CIENCIAS -> "Ciencias"
            HUMANIDADES -> "Humanidades"
            FINANZAS -> "Finanzas"
            DERECHO -> "Derecho"
            SALUD -> "Salud"
            EDUCACION -> "Educación"
            OTROS -> "Otros"
        }
    }

    companion object {
        fun fromString(value: String): SkillCategory {
            return when (value) {
                "Tecnología" -> TECNOLOGIA
                "Diseño" -> DISENO
                "Marketing" -> MARKETING
                "Idiomas" -> IDIOMAS
                "Arte" -> ARTE
                "Música" -> MUSICA
                "Gastronomía" -> GASTRONOMIA
                "Deportes" -> DEPORTES
                "Ciencias" -> CIENCIAS
                "Humanidades" -> HUMANIDADES
                "Finanzas" -> FINANZAS
                "Derecho" -> DERECHO
                "Salud" -> SALUD
                "Educación" -> EDUCACION
                else -> OTROS
            }
        }
    }
}

data class SkillStats(
    val mentorsAvailable: Int,
    val activeStudents: Int
) 