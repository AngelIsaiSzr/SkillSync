package com.ics.skillsync.data.repository

import android.app.Application
import com.google.firebase.firestore.FirebaseFirestore
import com.ics.skillsync.data.database.dao.SkillDao
import com.ics.skillsync.data.database.entity.Skill
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await
import java.util.UUID
import android.util.Log

class SkillRepository(
    private val skillDao: SkillDao,
    private val application: Application
) {
    private val firestore = FirebaseFirestore.getInstance()

    fun getUserSkills(userId: String): Flow<List<Skill>> {
        return skillDao.getSkillsByUserId(userId)
    }

    fun getSkillsByType(userId: String, type: Skill.SkillType): Flow<List<Skill>> {
        return skillDao.getSkillsByType(userId, type)
    }

    suspend fun addSkill(userId: String, name: String, type: Skill.SkillType, level: Int): Result<Unit> {
        return try {
            val skill = Skill(
                id = UUID.randomUUID().toString(),
                userId = userId,
                name = name,
                type = type,
                level = level
            )

            // Guardar en Firestore primero
            firestore.collection("skills")
                .document(skill.id)
                .set(skill)
                .await()

            // Si la operación en Firestore fue exitosa, guardar en Room
            skillDao.insertSkill(skill)

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("SkillRepository", "Error adding skill", e)
            Result.failure(e)
        }
    }

    suspend fun deleteSkill(skill: Skill): Result<Unit> {
        return try {
            // Eliminar de Firestore primero
            firestore.collection("skills")
                .document(skill.id)
                .delete()
                .await()

            // Si la operación en Firestore fue exitosa, eliminar de Room
            skillDao.deleteSkill(skill)

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("SkillRepository", "Error deleting skill", e)
            Result.failure(e)
        }
    }

    suspend fun syncUserSkills(userId: String) {
        try {
            // Obtener habilidades de Firestore
            val skillsSnapshot = firestore.collection("skills")
                .whereEqualTo("userId", userId)
                .get()
                .await()

            // Convertir documentos a habilidades
            val firestoreSkills = skillsSnapshot.documents.mapNotNull { document ->
                document.toObject(Skill::class.java)
            }

            // Obtener habilidades locales
            val localSkills = skillDao.getSkillsByUserId(userId).first()

            // Identificar habilidades a agregar y eliminar
            val skillsToAdd = firestoreSkills.filter { firestoreSkill ->
                localSkills.none { localSkill: Skill -> localSkill.id == firestoreSkill.id }
            }

            val skillsToDelete = localSkills.filter { localSkill: Skill ->
                firestoreSkills.none { firestoreSkill: Skill -> firestoreSkill.id == localSkill.id }
            }

            // Aplicar cambios en Room
            for (skill in skillsToDelete) {
                skillDao.deleteSkill(skill)
            }

            for (skill in skillsToAdd) {
                skillDao.insertSkill(skill)
            }
        } catch (e: Exception) {
            Log.e("SkillRepository", "Error syncing skills", e)
            // No lanzamos la excepción para evitar interrumpir el flujo de la app
        }
    }
} 