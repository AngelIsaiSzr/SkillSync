package com.ics.skillsync.data.database.dao

import androidx.room.*
import com.ics.skillsync.data.database.entity.CurrentUser
import com.ics.skillsync.data.database.entity.User
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE id = :userId")
    suspend fun getUserById(userId: String): User?

    @Query("SELECT * FROM users WHERE username = :username")
    suspend fun getUserByUsername(username: String): User?

    @Query("SELECT * FROM users WHERE email = :email")
    suspend fun getUserByEmail(email: String): User?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User)

    @Query("SELECT * FROM users")
    fun getAllUsers(): Flow<List<User>>

    @Query("DELETE FROM users")
    suspend fun deleteAllUsers()

    @Query("DELETE FROM users WHERE id = :userId")
    suspend fun deleteUser(userId: String)

    @Query("SELECT * FROM current_user LIMIT 1")
    suspend fun getCurrentUser(): CurrentUser?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setCurrentUser(currentUser: CurrentUser)

    @Query("DELETE FROM current_user")
    suspend fun clearCurrentUser()

    @Update
    suspend fun updateUser(user: User)

    @Delete
    suspend fun deleteUser(user: User)

    @Query("UPDATE users SET photoUrl = :photoUrl WHERE id = :userId")
    suspend fun updateUserPhoto(userId: String, photoUrl: String)

    @Query("UPDATE users SET verificationLevel = :level WHERE id = :userId")
    suspend fun updateUserVerificationLevel(userId: String, level: Int)
} 