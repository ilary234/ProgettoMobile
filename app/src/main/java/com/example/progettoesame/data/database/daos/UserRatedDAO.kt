package com.example.progettoesame.data.database.daos

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.example.progettoesame.data.database.UserRated

@Dao
interface UserRatedDAO {
    @Query("UPDATE user_rated_recipes SET isSynced = 1")
    suspend fun markAllUsersRatedAsSynced()

    @Query("SELECT * FROM user_rated_recipes WHERE isSynced = 0")
    suspend fun getUnsyncedUsersRated(): List<UserRated>

    @Query("UPDATE user_rated_recipes SET isSynced = 1 WHERE userId = :userId AND recipeId = :recipeId")
    suspend fun markAsSynced(userId: String, recipeId: String)

    @Query("SELECT MAX(updatedAt) FROM user_rated_recipes")
    suspend fun getLastUpdateTimestamp(): String?

    @Query("SELECT rating FROM user_rated_recipes WHERE userId = :userId AND recipeId = :recipeId AND isDeleted = 0")
    suspend fun getRating(userId: String, recipeId: String): Int?

    @Query("SELECT COUNT(*) FROM user_rated_recipes WHERE recipeId = :recipeId AND isDeleted = 0")
    suspend fun getNumberOfRatings(recipeId: String): Int
    @Upsert
    suspend fun upsert(userRated : UserRated)

    @Upsert
    suspend fun upsertAll(userRated : List<UserRated>)
}