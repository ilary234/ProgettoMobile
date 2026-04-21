package com.example.progettoesame.data.database.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.progettoesame.data.database.User

@Dao
interface UserDAO {

    @Insert
    suspend fun insertAll(users : List<User>)

    @Query("UPDATE users SET isSynced = 1")
    suspend fun markAllUsersAsSynced()
}