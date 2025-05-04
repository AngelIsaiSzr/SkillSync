package com.ics.skillsync.model

data class TeachingCard(
    val id: String = "",
    val mentorId: String = "",
    val mentorName: String = "",
    val mentorPhotoUrl: String = "",
    val mentorBio: String = "",
    val title: String = "",
    val description: String = "",
    val category: String = "",
    val experienceLevel: ExperienceLevel = ExperienceLevel.PRINCIPIANTE,
    val availability: String = "",
    val learnerCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val isActive: Boolean = true,
    val imageUrl: String = ""
) {
    enum class ExperienceLevel {
        PRINCIPIANTE,
        BASICO,
        INTERMEDIO,
        AVANZADO,
        EXPERTO;

        fun getDisplayName(): String {
            return when (this) {
                PRINCIPIANTE -> "Principiante"
                BASICO -> "Básico"
                INTERMEDIO -> "Intermedio"
                AVANZADO -> "Avanzado"
                EXPERTO -> "Experto"
            }
        }
    }
    
    companion object {
        fun fromMap(map: Map<String, Any>): TeachingCard {
            val experienceLevelStr = (map["experienceLevel"] as? String ?: ExperienceLevel.PRINCIPIANTE.name)
            val experienceLevel = when (experienceLevelStr) {
                "Principiante" -> ExperienceLevel.PRINCIPIANTE
                "Básico", "Basico" -> ExperienceLevel.BASICO
                "Intermedio" -> ExperienceLevel.INTERMEDIO
                "Avanzado" -> ExperienceLevel.AVANZADO
                "Experto" -> ExperienceLevel.EXPERTO
                else -> ExperienceLevel.valueOf(experienceLevelStr)
            }

            return TeachingCard(
                id = map["id"] as? String ?: "",
                mentorId = map["mentorId"] as? String ?: "",
                mentorName = map["mentorName"] as? String ?: "",
                mentorPhotoUrl = map["mentorPhotoUrl"] as? String ?: "",
                mentorBio = map["mentorBio"] as? String ?: "",
                title = map["title"] as? String ?: "",
                description = map["description"] as? String ?: "",
                category = map["category"] as? String ?: "",
                experienceLevel = experienceLevel,
                availability = map["availability"] as? String ?: "",
                learnerCount = (map["learnerCount"] as? Long)?.toInt() ?: 0,
                createdAt = map["createdAt"] as? Long ?: System.currentTimeMillis(),
                isActive = map["isActive"] as? Boolean ?: true,
                imageUrl = map["imageUrl"] as? String ?: ""
            )
        }
    }

    fun toMap(): Map<String, Any> {
        return hashMapOf(
            "id" to id,
            "mentorId" to mentorId,
            "mentorName" to mentorName,
            "mentorPhotoUrl" to mentorPhotoUrl,
            "mentorBio" to mentorBio,
            "title" to title,
            "description" to description,
            "category" to category,
            "experienceLevel" to experienceLevel.getDisplayName(),
            "availability" to availability,
            "learnerCount" to learnerCount,
            "createdAt" to createdAt,
            "isActive" to isActive,
            "imageUrl" to imageUrl
        )
    }
} 