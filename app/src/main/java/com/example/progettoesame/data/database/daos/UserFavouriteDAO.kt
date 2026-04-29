package com.example.progettoesame.data.database.daos

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.example.progettoesame.data.database.UserFavourite

@Dao
interface UserFavouriteDAO {

    @Query("UPDATE user_favourite_recipes SET isSynced = 1")
    suspend fun markAllUsersFavouriteAsSynced()

    @Query("SELECT * FROM user_favourite_recipes WHERE isSynced = 0")
    suspend fun getUnsyncedUsersFavourites(): List<UserFavourite>

    @Query("UPDATE user_favourite_recipes SET isSynced = 1 WHERE userId = :id")
    suspend fun markAsSynced(id: String)

    @Query("SELECT MAX(updatedAt) FROM user_favourite_recipes")
    suspend fun getLastUpdateTimestamp(): String?

    @Upsert
    suspend fun upsert(userFavourite: UserFavourite)

    @Upsert
    suspend fun upsertAll(userFavourites : List<UserFavourite>)

}