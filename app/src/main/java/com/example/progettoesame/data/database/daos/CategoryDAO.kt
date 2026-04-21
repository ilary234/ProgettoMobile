package com.example.progettoesame.data.database.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.progettoesame.data.database.Category

@Dao
interface CategoryDAO {

    @Query("SELECT COUNT(*) FROM categories")
    suspend fun getCount(): Int

    @Insert
    suspend fun insertAll(categories : List<Category>)
}