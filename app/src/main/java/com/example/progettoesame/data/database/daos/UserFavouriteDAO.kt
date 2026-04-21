package com.example.progettoesame.data.database.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.progettoesame.data.database.UserFavourite

@Dao
interface UserFavouriteDAO {

    @Insert
    suspend fun insertAll(userFavourites : List<UserFavourite>)

    @Query("UPDATE user_favourite_recipes SET isSynced = 1")
    suspend fun markAllUsersFavouriteAsSynced()
}