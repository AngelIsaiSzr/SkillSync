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
} 