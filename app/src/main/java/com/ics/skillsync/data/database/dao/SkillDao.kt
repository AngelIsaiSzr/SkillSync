package com.ics.skillsync.data.database.dao

import androidx.room.*
import com.ics.skillsync.data.database.entity.Skill
import kotlinx.coroutines.flow.Flow

@Dao
interface SkillDao {
    @Query("SELECT * FROM skills WHERE userId = :userId")
    fun getSkillsByUserId(userId: String): Flow<List<Skill>>

    @Query("SELECT * FROM skills WHERE userId = :userId AND type = :type")
    fun getSkillsByType(userId: String, type: Skill.SkillType): Flow<List<Skill>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSkill(skill: Skill)

    @Delete
    suspend fun deleteSkill(skill: Skill)

    @Query("DELETE FROM skills WHERE userId = :userId")
    suspend fun deleteAllUserSkills(userId: String)
} 