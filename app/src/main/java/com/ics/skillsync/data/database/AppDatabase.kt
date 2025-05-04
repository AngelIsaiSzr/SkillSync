package com.ics.skillsync.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.ics.skillsync.data.database.dao.UserDao
import com.ics.skillsync.data.database.dao.SkillDao
import com.ics.skillsync.data.database.entity.User
import com.ics.skillsync.data.database.entity.CurrentUser
import com.ics.skillsync.data.database.entity.Skill

@Database(
    entities = [User::class, CurrentUser::class, Skill::class],
    version = 7,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun skillDao(): SkillDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS current_user (
                        id INTEGER PRIMARY KEY NOT NULL,
                        value INTEGER NOT NULL
                    )
                """)
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Crear tabla temporal de usuarios
                database.execSQL("""
                    CREATE TABLE users_new (
                        id TEXT PRIMARY KEY NOT NULL,
                        firstName TEXT NOT NULL,
                        lastName TEXT NOT NULL,
                        username TEXT NOT NULL,
                        email TEXT NOT NULL,
                        password TEXT NOT NULL,
                        role TEXT NOT NULL
                    )
                """)

                // Copiar datos de la tabla antigua a la nueva
                database.execSQL("""
                    INSERT INTO users_new (id, firstName, lastName, username, email, password, role)
                    SELECT CAST(id AS TEXT), firstName, lastName, username, email, password, role
                    FROM users
                """)

                // Eliminar tabla antigua
                database.execSQL("DROP TABLE users")

                // Renombrar tabla nueva
                database.execSQL("ALTER TABLE users_new RENAME TO users")

                // Actualizar tabla current_user para usar TEXT
                database.execSQL("""
                    CREATE TABLE current_user_new (
                        id INTEGER PRIMARY KEY NOT NULL,
                        value TEXT NOT NULL
                    )
                """)

                // Copiar datos de current_user
                database.execSQL("""
                    INSERT INTO current_user_new (id, value)
                    SELECT id, CAST(value AS TEXT)
                    FROM current_user
                """)

                // Eliminar tabla antigua
                database.execSQL("DROP TABLE current_user")

                // Renombrar tabla nueva
                database.execSQL("ALTER TABLE current_user_new RENAME TO current_user")
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Crear tabla temporal de usuarios con el nuevo campo photoUrl
                database.execSQL("""
                    CREATE TABLE users_new (
                        id TEXT PRIMARY KEY NOT NULL,
                        firstName TEXT NOT NULL,
                        lastName TEXT NOT NULL,
                        username TEXT NOT NULL,
                        email TEXT NOT NULL,
                        password TEXT NOT NULL,
                        role TEXT NOT NULL,
                        photoUrl TEXT NOT NULL DEFAULT ''
                    )
                """)

                // Copiar datos de la tabla antigua a la nueva
                database.execSQL("""
                    INSERT INTO users_new (id, firstName, lastName, username, email, password, role, photoUrl)
                    SELECT id, firstName, lastName, username, email, password, role, ''
                    FROM users
                """)

                // Eliminar tabla antigua
                database.execSQL("DROP TABLE users")

                // Renombrar tabla nueva
                database.execSQL("ALTER TABLE users_new RENAME TO users")
            }
        }

        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Crear tabla temporal de usuarios con el nuevo campo verificationLevel
                database.execSQL("""
                    CREATE TABLE users_new (
                        id TEXT PRIMARY KEY NOT NULL,
                        firstName TEXT NOT NULL,
                        lastName TEXT NOT NULL,
                        username TEXT NOT NULL,
                        email TEXT NOT NULL,
                        password TEXT NOT NULL,
                        role TEXT NOT NULL,
                        photoUrl TEXT NOT NULL DEFAULT '',
                        verificationLevel INTEGER NOT NULL DEFAULT 0
                    )
                """)

                // Copiar datos de la tabla antigua a la nueva
                database.execSQL("""
                    INSERT INTO users_new (id, firstName, lastName, username, email, password, role, photoUrl, verificationLevel)
                    SELECT id, firstName, lastName, username, email, password, role, photoUrl, 0
                    FROM users
                """)

                // Eliminar tabla antigua
                database.execSQL("DROP TABLE users")

                // Renombrar tabla nueva
                database.execSQL("ALTER TABLE users_new RENAME TO users")
            }
        }

        private val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS skills (
                        id TEXT PRIMARY KEY NOT NULL,
                        userId TEXT NOT NULL,
                        name TEXT NOT NULL,
                        type TEXT NOT NULL,
                        level INTEGER NOT NULL
                    )
                """)
            }
        }

        private val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Crear tabla temporal de usuarios con los nuevos campos
                database.execSQL("""
                    CREATE TABLE users_new (
                        id TEXT PRIMARY KEY NOT NULL,
                        firstName TEXT NOT NULL,
                        lastName TEXT NOT NULL,
                        username TEXT NOT NULL,
                        email TEXT NOT NULL,
                        password TEXT NOT NULL,
                        role TEXT NOT NULL,
                        photoUrl TEXT NOT NULL DEFAULT '',
                        verificationLevel INTEGER NOT NULL DEFAULT 0,
                        biography TEXT NOT NULL DEFAULT '',
                        availability TEXT NOT NULL DEFAULT ''
                    )
                """)

                // Copiar datos de la tabla antigua a la nueva
                database.execSQL("""
                    INSERT INTO users_new (
                        id, firstName, lastName, username, email, password, 
                        role, photoUrl, verificationLevel, biography, availability
                    )
                    SELECT 
                        id, firstName, lastName, username, email, password,
                        role, photoUrl, verificationLevel, '', ''
                    FROM users
                """)

                // Eliminar tabla antigua
                database.execSQL("DROP TABLE users")

                // Renombrar tabla nueva
                database.execSQL("ALTER TABLE users_new RENAME TO users")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "skillsync_database"
                )
                .addMigrations(
                    MIGRATION_1_2,
                    MIGRATION_2_3,
                    MIGRATION_3_4,
                    MIGRATION_4_5,
                    MIGRATION_5_6,
                    MIGRATION_6_7
                )
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
} 