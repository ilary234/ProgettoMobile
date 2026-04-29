package com.example.progettoesame.data.database.daos

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.example.progettoesame.data.database.User

@Dao
interface UserDAO {

    @Query("UPDATE users SET isSynced = 1")
    suspend fun markAllUsersAsSynced()

    @Query("SELECT * FROM users WHERE isSynced = 0")
    suspend fun getUnsyncedUsers(): List<User>

    @Query("UPDATE users SET isSynced = 1 WHERE userId = :id")
    suspend fun markAsSynced(id: String)

    @Query("SELECT MAX(updatedAt) FROM users")
    suspend fun getLastUpdateTimestamp(): String?

    @Upsert
    suspend fun upsert(user: User)

    @Upsert
    suspend fun upsertAll(users : List<User>)
}