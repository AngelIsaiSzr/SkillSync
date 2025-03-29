package com.ics.skillsync.data.database.dao

import androidx.room.*
import com.ics.skillsync.data.database.entity.User
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertUser(user: User): Long

    @Query("SELECT * FROM users WHERE username = :username AND password = :password LIMIT 1")
    suspend fun getUser(username: String, password: String): User?

    @Query("SELECT EXISTS(SELECT 1 FROM users WHERE username = :username)")
    suspend fun isUsernameExists(username: String): Boolean

    @Query("SELECT EXISTS(SELECT 1 FROM users WHERE email = :email)")
    suspend fun isEmailExists(email: String): Boolean

    @Query("SELECT * FROM users WHERE id = (SELECT value FROM current_user LIMIT 1)")
    suspend fun getCurrentUser(): User?

    @Query("INSERT OR REPLACE INTO current_user (id, value) VALUES (1, :userId)")
    suspend fun setCurrentUser(userId: Long)

    @Query("DELETE FROM current_user")
    suspend fun clearCurrentUser()
} 