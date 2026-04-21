package com.example.progettoesame.data.database.daos

import androidx.room.Dao
import androidx.room.Insert
import com.example.progettoesame.data.database.UserFavourite

@Dao
interface UserFavouriteDAO {

    @Insert
    suspend fun insertAll(userFavourites : List<UserFavourite>)
}