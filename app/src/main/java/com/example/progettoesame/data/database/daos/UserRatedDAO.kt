package com.example.progettoesame.data.database.daos

import androidx.room.Query
import androidx.room.Upsert
import com.example.progettoesame.data.database.UserRated

interface UserRatedDAO {
    @Query("UPDATE user_rated_recipes SET isSynced = 1")
    suspend fun markAllUsersRatedAsSynced()

    @Query("SELECT * FROM user_rated_recipes WHERE isSynced = 0")
    suspend fun getUnsyncedUsersRated(): List<UserRated>

    @Query("UPDATE user_rated_recipes SET isSynced = 1 WHERE userId = :userId AND recipeId = :recipeId")
    suspend fun markAsSynced(userId: String, recipeId: String)

    @Query("SELECT MAX(updatedAt) FROM user_rated_recipes")
    suspend fun getLastUpdateTimestamp(): String?

    @Upsert
    suspend fun upsertAll(userRated : List<UserRated>)
}